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
package net.opentsdb.grpc.client.streaming;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.Marshaller;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.MethodDescriptor.ReflectableMarshaller;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;
import net.opentsdb.grpc.common.RPCTypes;

/**
 * <p>Title: AbstractStreamer</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.streaming.AbstractStreamer</code></p>
 */

public abstract class AbstractStreamer<T, R> implements Streamer<T, R>, Closeable {
	private static final Set<MethodType> METHOD_TYPES = Collections.unmodifiableSet(
			EnumSet.of(MethodType.BIDI_STREAMING, MethodType.CLIENT_STREAMING, MethodType.SERVER_STREAMING));
	
	
	protected final Logger LOG;
	protected final Channel channel;
	protected final MethodDescriptor<T,R> md;
	protected final MethodType methodType;
	protected final Consumer<R> outConsumer;
	
	protected Consumer<Streamer<T,R>> onComplete = r -> {};
	
	protected final LongAdder requestsSent = new LongAdder();
	protected final LongAdder responsesReceived = new LongAdder();		
	protected final LongAdder requestsDropped = new LongAdder();
	protected final LongAdder acceptErrors = new LongAdder();
	protected final AtomicLong inFlight = new AtomicLong();
	protected final LongAdder onReadyEvents = new LongAdder();
	
	protected final AtomicBoolean clientClosed = new AtomicBoolean(true);
	protected final AtomicBoolean streamerClosed = new AtomicBoolean(true);
	
	protected final Function<R, Boolean> isFinalFx;
	protected final Function<T, Long> subItemsIn;
	protected final Function<R, Long> subItemsOut;
	protected final Function<R, Boolean> finalResponse;
	
	protected BiConsumer<Throwable, Streamer<T,R>> onErrorAction = (t, s) -> {};
	
	
	protected final CountDownLatch completion = new CountDownLatch(1);
	
	protected long startTime = -1;

	protected final RPCTypes<T,R> rpcTypes;
	protected final boolean expectsFinalResponse;

	protected final AtomicBoolean finalResponseReceived = new AtomicBoolean(false);
	
	protected final CallOptions callOptions;
	
	protected final BlockingQueue<T> inQueue;
	protected final ClientCall<T,R> clientCall;
	
	protected final StreamObserver<T> requestObserver;
	protected final StreamObserver<R> responseObserver;
	
	
	protected ClientCallStreamObserver<T> requestStream;
	
	@Override
	public void close() throws IOException {
		if(clientClosed.compareAndSet(false, true)) {
			try { clientCall.halfClose(); } catch (Exception x) {/* No Op */}
		}
		if(streamerClosed.compareAndSet(false, true)) {
			try { requestObserver.onCompleted(); } catch (Exception x) {/* No Op */}
			try { responseObserver.onCompleted(); } catch (Exception x) {/* No Op */}
			try { requestStream.onCompleted(); } catch (Exception x) {/* No Op */}
			if(inQueue!=null) {
				inQueue.clear();
			}
			onComplete.accept(this);
		}
		LOG.info("Streamer Closed: {}", md.getFullMethodName());
	}

	
	
	
	
	/**
	 * Creates a new AbstractStreamer
	 * @param builder The streamer builder
	 */
	protected AbstractStreamer(StreamerBuilder<T,R> builder) {
		md = builder.methodDescriptor();
		if(!METHOD_TYPES.contains(Objects.requireNonNull(md, "The passed MethodDescriptor was null").getType())) {
			throw new IllegalArgumentException("The method [" + md.getFullMethodName() + "] is " + md.getType());
		}
		LOG = LoggerFactory.getLogger(getClass().getSimpleName() + "." + md.getFullMethodName());
		methodType = md.getType();
		this.channel = Objects.requireNonNull(builder.channel(), "The passed ManagedChannel was null");
		
		this.outConsumer = Objects.requireNonNull(builder.outConsumer(), "The passed Consumer was null");
		rpcTypes = RPCTypes.getRpcTypesFor(md);
		expectsFinalResponse = rpcTypes.providesFinal;
		
		// If CLIENT_STREAMING, then first response is the final response
		isFinalFx = methodType == MethodType.CLIENT_STREAMING ? (r) -> true :  builder.finalResponse();
		subItemsIn = builder.subItemsIn();
		subItemsOut = builder.subItemsOut();
		finalResponse = builder.finalResponse();
		
		callOptions = builder.callOptions();
		
		int queueSize = builder.inQueueSize();
		if(queueSize==0) {
			inQueue = null;
		} else {
			inQueue = new ArrayBlockingQueue<>(queueSize, true);
		}
		
		clientCall = channel.newCall(md, callOptions);
		responseObserver = responseObserver();
		switch(methodType) {
			case BIDI_STREAMING:
				requestObserver = ClientCalls.asyncBidiStreamingCall(clientCall, responseObserver);
				break;
			case CLIENT_STREAMING:
				requestObserver = ClientCalls.asyncClientStreamingCall(clientCall, responseObserver);
				break;
			default:
				requestObserver = null;
		}		
	}
	
	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.server.streaming.Streamer#start()
	 */
	public AbstractStreamer<T,R> start() {
		startTime = System.currentTimeMillis();
		streamerClosed.set(false);
		clientClosed.set(false);
		return this;
	}
	
	
	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.server.streaming.Streamer#printStats()
	 */
	@Override
	public String printStats() {
		StringBuilder b = new StringBuilder();
		b.append("\nSent:").append(requestsSent.longValue())
			.append("\nReceived:").append(responsesReceived.longValue())
			.append("\nDropped:").append(requestsDropped.longValue())
			.append("\nInFlight:").append(inFlight.longValue())
			.append("\nOnReadyEvents:").append(onReadyEvents.longValue());
		if(inQueue != null) {
			b.append("\nQueued:").append(inQueue.size());
		}
		b.append("\n=================================================");
		return b.toString();
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
			return completion.await(time, unit);
		} catch (InterruptedException iex) {
			throw new RuntimeException("Thread interrupted while waiting for completion", iex);
		}
	}
	
	public void waitForCompletion() {
		try {
			completion.await();
		} catch (InterruptedException iex) {
			throw new RuntimeException("Thread interrupted while waiting for completion", iex);
		}
	}	
	
	public void clientComplete() {
		if(clientClosed.compareAndSet(false, true)) {
//			clientCall.halfClose();
			LOG.info("Client Half Closed");
			LOG.info(printStats());
		}
	}
	
	
	protected abstract StreamObserver<R> responseObserver();

	
	public long getRequestsSent() {
		return requestsSent.longValue();
	}

	public long getResponsesReceived() {
		return responsesReceived.longValue();
	}

	public long getRequestsDropped() {
		return requestsDropped.longValue();
	}

	public long getInFlight() {
		return inFlight.longValue();
	}

	public long getOnReadyEvents() {
		return onReadyEvents.longValue();
	}
	
	public boolean isExpectsFinalResponse() {
		return expectsFinalResponse;
	}

	/**
	 * @param onErrorAction the onErrorAction to set
	 * @return 
	 */
	public AbstractStreamer<T,R> onErrorAction(BiConsumer<Throwable, Streamer<T,R>> onErrorAction) {
		this.onErrorAction = onErrorAction;
		return this;
	}


}
