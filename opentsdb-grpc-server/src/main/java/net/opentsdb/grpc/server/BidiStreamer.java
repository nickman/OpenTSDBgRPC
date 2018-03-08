// This file is part of OpenTSDB.
// Copyright (C) 2010-2012  The OpenTSDB Authors.
//
// This program is free software: you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 2.1 of the License, or (at your
// option) any later version.  This program is distributed in the hope that it
// will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
// of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
// General Public License for more details.  You should have received a copy
// of the GNU Lesser General Public License along with this program.  If not,
// see <http://www.gnu.org/licenses/>.
package net.opentsdb.grpc.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;

/**
 * <p>Title: BidiStreamer</p>
 * <p>Description: Encapsulates a generic bidirectional client call</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.Streamer</code></p>
 */

public class BidiStreamer<T, R> {
	protected final Logger LOG;
	protected final Channel channel;
	protected final MethodDescriptor<T,R> md;
	protected final Consumer<R> outConsumer;
	protected final Consumer<BidiStreamer<T,R>> onComplete;
	
	final LongAdder requestsSent = new LongAdder();
	final LongAdder responsesReceived = new LongAdder();		
	final LongAdder requestsDropped = new LongAdder();
	final LongAdder acceptErrors = new LongAdder();
	final LongAdder inFlight = new LongAdder();
	final LongAdder onReadyEvents = new LongAdder();
	
	final AtomicBoolean clientClosed = new AtomicBoolean(true);
	final AtomicBoolean streamerClosed = new AtomicBoolean(true);
	
	final BlockingQueue<T> outQueue = new ArrayBlockingQueue<>(10000, true);
	
	final Function<R, Boolean> isFinalFx;
	final Function<T, Integer> subItemsIn;
	final Function<R, Integer> subItemsOut;
	
	final AtomicReference<CountDownLatch> completion = new AtomicReference<>(new CountDownLatch(1));
	
	final ClientCall<T,R> clientCall;
	ClientCallStreamObserver<T> requestObserver;
	StreamObserver<R> responseObserver;
	ClientCallStreamObserver<T> requestStream;
	
	long startTime = -1;
	
	
	public BidiStreamer(Channel channel, MethodDescriptor<T, R> md, Consumer<R> outConsumer,
			Consumer<BidiStreamer<T, R>> onComplete, Function<R, Boolean> isFinalFx,
			Function<T, Integer> subItemsIn, Function<R, Integer> subItemsOut) {
		LOG = LoggerFactory.getLogger(getClass().getSimpleName() + "." + md.getFullMethodName());
		this.channel = channel;
		this.md = md;
		this.isFinalFx = isFinalFx;
		this.outConsumer = outConsumer;
		this.onComplete = onComplete;
		this.subItemsIn = subItemsIn != null ? subItemsIn : t -> 1;
		this.subItemsOut = subItemsOut != null ? subItemsOut : r -> 1;
		clientCall = channel.newCall(md, CallOptions.DEFAULT
				.withWaitForReady()
				.withCompression("gzip")
		);
		
	}
	
	public String printStats() {
		StringBuilder b = new StringBuilder();
		b.append("\nSent:").append(requestsSent.longValue());
		b.append("\nReceived:").append(responsesReceived.longValue());
		b.append("\nQueued:").append(outQueue.size());
		b.append("\nDropped:").append(requestsDropped.longValue());
		b.append("\nInFlight:").append(inFlight.longValue());
		b.append("\nOnReadyEvents:").append(onReadyEvents.longValue());
		b.append("\n=================================================");
		return b.toString();
	}
	
	
	
	public void startStream() {
		startTime = System.currentTimeMillis();
		streamerClosed.set(false);
		clientClosed.set(false);
		responseObserver = responseObserver();
		requestObserver = (ClientCallStreamObserver<T>)ClientCalls.asyncBidiStreamingCall(clientCall, responseObserver);
		LOG.info("Stream Started");
	}
	
	public void send(T t) {
		int inCount = subItemsIn.apply(t);
		if(clientClosed.get()) {
			throw new IllegalStateException("Streamer client is closed");
		}
		if(requestStream.isReady() || true) {
			requestStream.onNext(t);
			requestStream.request(1);
			requestsSent.add(inCount);
			inFlight.add(inCount);
		} else {
			if(!outQueue.offer(t)) {
				requestsDropped.increment();
			}
		}
	}
	
	public void clientComplete() {
		if(clientClosed.compareAndSet(false, true)) {
//			clientCall.halfClose();
			LOG.info("Client Half Closed");
			LOG.info(printStats());
		}
	}
	
	protected void checkComplete() {
		long inf = inFlight.longValue();
		if(inf==0) {
			if(streamerClosed.compareAndSet(false, true)) {
				LOG.info("Closing Streamer....");
				requestObserver.onCompleted();
				LOG.info(printStats());							
			}
		}		
	}
	
	public boolean waitForCompletion(long time, TimeUnit unit) {
		try {
			return completion.get().await(time, unit);
		} catch (InterruptedException iex) {
			throw new RuntimeException("Thread interrupted while waiting for completion", iex);
		}
	}
	
	public void waitForCompletion() {
		try {
			completion.get().await();
		} catch (InterruptedException iex) {
			throw new RuntimeException("Thread interrupted while waiting for completion", iex);
		}
	}
	
	
	protected StreamObserver<R> responseObserver() {
		return new ClientResponseObserver<T, R>() {

			@Override
			public void onNext(R message) {
				boolean isFinal = isFinalFx.apply(message);
				if(isFinal) {
					LOG.info("Final: {}", message);
					completion.get().countDown();
				} else {
					int outCount = subItemsOut.apply(message);
					responsesReceived.add(outCount);
					inFlight.add(outCount * -1);
					try {
						outConsumer.accept(message);
					} catch (Exception ex) {
						acceptErrors.increment();
					}
					
					if(clientClosed.get() && outQueue.isEmpty()) {
						checkComplete();
					}
//					LOG.info("REC: {}, final: {}", responsesReceived.longValue(), isFinal);
				}
			}

			@Override
			public void onError(Throwable t) {
				LOG.error("Response Observer: Stream error", t);
				completion.get().countDown();
			}

			@Override
			public void onCompleted() {
				LOG.info("Response Observer:Stream Complete\n{}", printStats());
				
				completion.get().countDown();
			}

			@Override
			public void beforeStart(ClientCallStreamObserver<T> rs) {
				requestStream = rs;
//				rs.disableAutoInboundFlowControl();
				rs.setOnReadyHandler(() -> {
					onReadyEvents.increment();
					LOG.info("Request Stream Ready. Queued: {}", outQueue.size());
					long writes = 0;
					while(rs.isReady()) {
						T t = outQueue.poll();
						if(t!=null) {
							int inCount = subItemsIn.apply(t);
							rs.onNext(t);
							rs.request(1);
							requestsSent.add(inCount);
							inFlight.add(inCount);
							writes++;
						} else {
							break;
						}
					}
					LOG.info("Request Queue Flushed. Queued: {}, Flushed: {}", outQueue.size(), writes);
					if(clientClosed.get() && outQueue.isEmpty()) {
						checkComplete();
					}

					LOG.info("");
				});
				
				
				
			}
			
		};
	}
	


	
}
