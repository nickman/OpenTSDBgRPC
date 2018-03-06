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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

/**
 * <p>Title: Streamer</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.Streamer</code></p>
 */

public class Streamer<T, R> {
	protected final Logger LOG;
	protected final Channel channel;
	protected final MethodDescriptor<T,R> md;
	protected final Consumer<R> outConsumer;
	protected final Consumer<StreamingHelper2<T,R>> onComplete;
	
	final LongAdder requestsSent = new LongAdder();
	final LongAdder notReady = new LongAdder();
	final LongAdder responsesReceived = new LongAdder();		
	final LongAdder requestsDropped = new LongAdder();
	final LongAdder acceptErrors = new LongAdder();
	final LongAdder readyEvents = new LongAdder();
	final LongAdder inFlight = new LongAdder();
	
	final AtomicBoolean clientClosed = new AtomicBoolean(true);
	final AtomicBoolean streamerClosed = new AtomicBoolean(true);
	
	final ClientCall<T,R> clientCall;
	ClientCallStreamObserver<T> requestObserver;
	StreamObserver<R> responseObserver;
	
	long startTime = -1;
	
	
	public Streamer(Channel channel, MethodDescriptor<T, R> md, Consumer<R> outConsumer,
			Consumer<StreamingHelper2<T, R>> onComplete) {
		LOG = LoggerFactory.getLogger(getClass().getSimpleName() + "." + md.getFullMethodName());
		this.channel = channel;
		this.md = md;
		this.outConsumer = outConsumer;
		this.onComplete = onComplete;
		clientCall = channel.newCall(md, CallOptions.DEFAULT.withWaitForReady());
		
	}
	
	
	public void startStream() {
		startTime = System.currentTimeMillis();
		streamerClosed.set(false);
		clientClosed.set(false);
		responseObserver = responseObserver();
		requestObserver = (ClientCallStreamObserver<T>)ClientCalls.asyncClientStreamingCall(clientCall, responseObserver);
		LOG.info("Stream Started");
	}
	
	public void send(T t) {
		requestObserver.onNext(t);
		requestObserver.request(1);
		requestsSent.increment();
		inFlight.increment();
	}
	
	public void clientComplete() {
		if(clientClosed.compareAndSet(false, true)) {
			clientCall.halfClose();
			LOG.info("Client Half Closed");
		}
	}
	
	protected StreamObserver<R> responseObserver() {
		return new StreamObserver<R>() {

			@Override
			public void onNext(R message) {
				responsesReceived.increment();
				inFlight.decrement();
				try {
					outConsumer.accept(message);
				} catch (Exception ex) {
					acceptErrors.increment();
				}
				if(clientClosed.get()) {
					long inf = inFlight.longValue();
					if(inf==0) {
						if(streamerClosed.compareAndSet(false, true)) {
							// TODO:
						}
					}
				}
			}

			@Override
			public void onError(Throwable t) {
				LOG.error("Response Observer: Stream error", t);
			}

			@Override
			public void onCompleted() {
				LOG.info("Response Observer:Stream Complete"); 				
			}
			
		};
	}
	


	
}
