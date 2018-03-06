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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.ClientResponseObserver;

/**
 * <p>Title: StreamingHelper</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.StreamingHelper</code></p>
 * @param <T> The client call request type
 * @param <R> The client call response type
 */

public class StreamingHelper2<T, R> extends ClientCall.Listener<R> {
	private final Logger LOG;
	final LongAdder requestsSent = new LongAdder();
	final LongAdder notReady = new LongAdder();
	final LongAdder responsesReceived = new LongAdder();		
	final LongAdder requestsDropped = new LongAdder();
	final LongAdder acceptErrors = new LongAdder();
	final LongAdder readyEvents = new LongAdder();
	final LongAdder inFlight = new LongAdder();
	long startTime = -1;
	final Channel channel;
	final BlockingQueue<T> inQueue = new ArrayBlockingQueue<T>(1024, true);
	final Consumer<R> outConsumer;
	final MethodDescriptor<T,R> md;
	final ClientCall<T,R> clientCall;
	final AtomicBoolean started = new AtomicBoolean(false);
	final Consumer<StreamingHelper2<T,R>> onComplete;
	final AtomicReference<Thread> onReadyThread = new AtomicReference<>();
	ClientCallStreamObserver<T> requestStream;
	
	public String printStats() {
		StringBuilder b = new StringBuilder();
		b.append("\nSent:").append(requestsSent.longValue());
		b.append("\nReceived:").append(responsesReceived.longValue());
		b.append("\nDropped:").append(requestsDropped.longValue());
		b.append("\nInFlight:").append(inFlight.longValue());
		b.append("\nQueued:").append(inQueue.size());
		b.append("\nNotReady:").append(notReady.longValue());
		b.append("\nDispatched:").append(requestsSent.longValue() + inQueue.size());
		b.append("\nReady:").append(readyEvents.longValue());
		b.append("\n=================================================");
		return b.toString();
	}
	
	/**
	 * Creates a new StreamingHelper
	 * @param channel
	 * @param md
	 * @param outConsumer
	 * @param onComplete
	 */
	public StreamingHelper2(Channel channel, MethodDescriptor<T,R> md, Consumer<R> outConsumer, Consumer<StreamingHelper2<T,R>> onComplete) {
		LOG = LoggerFactory.getLogger(StreamingHelper2.class);
		this.channel = channel;
		this.md = md;
		this.outConsumer = outConsumer;
		this.onComplete = onComplete;
		clientCall = channel.newCall(md, CallOptions.DEFAULT);
		
	}
	
	private ClientResponseObserver<T, R> clientResponseObserver() {
		return new ClientResponseObserver<T, R>() {

			@Override
			public void onNext(R message) {
				responsesReceived.increment();
				inFlight.decrement();
				try {
					outConsumer.accept(message);
				} catch (Exception ex) {
					acceptErrors.increment();
				}
				if(!started.get() && inQueue.isEmpty() && inFlight.longValue()==0) {
					LOG.info("***** Complete *****");
					//clientCall.cancel("Complete", null);
					clientCall.halfClose();
					onComplete.accept(StreamingHelper2.this);
				}
			}

			@Override
			public void onError(Throwable t) {
				LOG.error("ClientResponseObserver thrown error", t);
				
			}

			@Override
			public void onCompleted() {
				LOG.error("ClientResponseObserver complete");
				
			}

			@Override
			public void beforeStart(ClientCallStreamObserver<T> rs) {
				LOG.info("beforeStart ****");
				requestStream = rs;
				requestStream.disableAutoInboundFlowControl();
				requestStream.setOnReadyHandler(new Runnable() {
					public void run() {
						int msgs = 0;
						LOG.info("  >>>>>> Request Stream Ready");
		                while (requestStream.isReady()) {		                	
		                	T outbound =  null;
		                    if ((outbound = inQueue.poll())!=null) {
		                    	doSend(outbound);
		                    	msgs++;
		                    }
		                }
		                LOG.info("  <<<<<< Request Stream Unready. Msgs: {}", msgs);
					}
				});
				
			}
			
		};
	}
	
	private void doSend(T t) {
        requestStream.onNext(t);
        requestsSent.increment();
        inFlight.increment();
	}
	
	/**
	 * Sends a message, dropping it quietly if the in queue is full.
	 * @param t The message to send
	 * @throws IllegalStateException If the stream is not started
	 */
	public void send(T t) {
		if(!started.get()) {
			throw new IllegalStateException("Stream is not started");
		}
		if(requestStream.isReady()) {
			doSend(t);
		} else {
			if(!inQueue.offer(t)) {
				requestsDropped.increment();
			}
		}
	}
	
	public void blockingSend(T t) {
		if(!started.get()) {
			throw new IllegalStateException("Stream is not started");
		}
		if(requestStream.isReady()) {
			doSend(t);
		} else {
			try {
				inQueue.put(t);
			} catch (InterruptedException iex) {
				LOG.error("Thread interrupted while waiting on inQueue", iex);
			}
		}
	}
	
	/**
	 * Starts the stream
	 */
	public void startStream() {
		startTime = System.currentTimeMillis();
		started.set(true);
		ClientCalls.asyncClientStreamingCall(clientCall, clientResponseObserver());
//		clientCall.start(this, new Metadata());
//		clientCall.request(1);
//		onReady();
	}
	
	public void halfClose() {
		started.set(false);
//		Thread t = onReadyThread.get();
//		if(t!=null) {
//			t.interrupt();
//		}
	}
	
	

}
