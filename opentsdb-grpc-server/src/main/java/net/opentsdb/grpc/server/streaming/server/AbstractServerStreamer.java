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
package net.opentsdb.grpc.server.streaming.server;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message.Builder;

import io.grpc.MethodDescriptor;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.OpenTracingContextKey;
import net.opentsdb.grpc.TXTime;
import net.opentsdb.grpc.common.RPCTypes;
import net.opentsdb.grpc.server.handlers.Handler;
import net.opentsdb.tracing.JaegerTracing;

/**
 * <p>Title: BidiServerStreamer</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.streaming.server.AbstractServerStreamer</code></p>
 * @param <T> The GRPC parameter type
 * @param <R> The GRPC return type
 */

public abstract class AbstractServerStreamer<T, R> implements StreamObserver<T> {
	
	protected final AtomicBoolean open;
	protected final Function<T, Integer> subItemsIn;
	protected final Function<R, Integer> subItemsOut;
	protected final MethodDescriptor<T,R> md;   // e.g. opentsdb.OpenTSDBService/Puts
	protected final Handler<T,R> handler;
	protected final RPCTypes<T,R> rpcTypes;
	protected final ServerCallStreamObserver<R> responseObserver;
	protected final StreamerContext sc;
	protected final Logger LOG;
	protected final Tracer tracer;
	protected long startTime = -1;	
	
	/**
	 * Creates a new AbstractServerStreamer
	 * @param builder The streamer builder
	 * @param streamerContext The streamer context
	 * @param responseObserver The client response observer
	 */
	public AbstractServerStreamer(StreamerBuilder<T,R> builder, StreamerContext streamerContext, StreamObserver<R> responseObserver) {
		md = builder.methodDescriptor();
		rpcTypes = RPCTypes.getRpcTypesFor(md);
		open = streamerContext.openFlag();
		subItemsIn = builder.subItemsIn();
		subItemsOut = builder.subItemsOut();
		LOG = LoggerFactory.getLogger(md.getFullMethodName() + "." + getClass().getSimpleName());
		this.responseObserver = (ServerCallStreamObserver<R>)responseObserver;
		this.handler = builder.handler;
		sc = streamerContext; 
		tracer = JaegerTracing.getInstance().tracer();
		
		this.responseObserver.setOnCancelHandler(new Runnable() {
			@Override
			public void run() {
				sc.cancellation();
				LOG.warn("Stream Call Cancelled");
				close();
			}
		});
	}
	
	public AbstractServerStreamer<T,R> start() {
		startTime = System.currentTimeMillis();
		open.set(true);
		long active = sc.incrementStreams();
		LOG.info("Started Streamer: type={}, method={}, active={}", md.getType().name(), md.getFullMethodName(), active);
		return this;
		
	}
	
	 

	@Override
	public void onNext(T value) {
		final long startTime = System.currentTimeMillis();
		final boolean hasTxTime = rpcTypes.hasTXTime;
		final long sentTime;
		final long sentElapsed;
		
		final TXTime txTime;
		if(hasTxTime) {
			txTime = rpcTypes.getTXTimeT(value);
			sentTime = txTime.getTxtime();
			sentElapsed = startTime - sentTime;
		} else {
			txTime = null;
			sentTime = -1L;
			sentElapsed = -1L;			
		}
		final int itemCount = subItemsIn.apply(value);
		LOG.info("Received Message: items={}", itemCount);
		sc.received(itemCount);
		try {
			CompletableFuture<R> pendingResponse = handler.invoke(value, sc);
			if(pendingResponse!=null) {
				pendingResponse.whenComplete((r, t) -> {
					if(t!=null) {
						LOG.error("Failed to process message", t);
						sc.failed(itemCount);						
					} else {
						if(hasTxTime) {
							long now = System.currentTimeMillis();
							TXTime tx = TXTime.newBuilder()
									.setStime(sentElapsed)
									.setTxtime(txTime.getTxtime())
									.setPtime(now - startTime)
									.setRtime(now)
									.build();
							
							R modR = rpcTypes.setTXTimeR(r, tx);
							responseObserver.onNext(modR);
						} else {
							responseObserver.onNext(r);
						}
						sc.sent(subItemsOut.apply(r));
					}
				});
			}
		} catch (Exception ex) {
			LOG.error("Failed to process message", ex);
		}		
	}

	@SuppressWarnings("unchecked")
	// TODO:  Optimize Me
	protected R applyTXTime(R r, RPCTypes<T,R> rpcTypes, long txTime, long startTime, long sentElapsed) {
		long now = System.currentTimeMillis();
		GeneratedMessageV3 gm = (GeneratedMessageV3)r;
		Builder rBuilder = gm.newBuilderForType();
		rBuilder.mergeFrom(gm);
		FieldDescriptor fd = rBuilder.getDescriptorForType().findFieldByName("txTime");
		
		TXTime tx = TXTime.newBuilder()
			.setStime(sentElapsed)
			.setTxtime(txTime)
			.setPtime(now - startTime)
			.setRtime(now)
			.build();
			
		rBuilder.setField(fd, tx);
		
		return (R)rBuilder.build();
	}
	
	@Override
	public void onError(Throwable t) {
		if(StatusRuntimeException.class.isInstance(t) && t.getMessage().contains("CANCELLED:")) {
			// Handled by cancellation runnable
		} else {
			LOG.error("Streamer closing on error", t);
		}		
		close();
	}

	@Override
	public void onCompleted() {
		LOG.info("COMPLETE");
		R r = handler.closer(sc);
		if(r!=null) {
			responseObserver.onNext(r);
			LOG.info("FINAL SENT");
		}
		responseObserver.onCompleted();
		close();
	}

	protected void close() {
		if(open.compareAndSet(true, false)) {
			sc.decrementStreams();
		}
	}

}
