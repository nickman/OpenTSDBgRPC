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

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import net.opentsdb.grpc.common.StreamDescriptor;

/**
 * <p>Title: BaseStreamerBuilder</p>
 * <p>Description: A base class for Streamer Builders</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.streaming.BaseStreamerBuilder</code></p>
 * @param <T> The request type
 * @param <R> The response type 
 */

public class StreamerBuilder<T,R> {
	protected final Channel channel;
	protected final MethodDescriptor<T, R> methodDescriptor;
	protected final Consumer<R> outConsumer;

	
	protected Function<T, Long> subItemsIn = (t) -> 1L;
	protected Function<R, Long> subItemsOut = (r) -> 1L;
	protected Function<R, Boolean> finalResponse = (r) -> false;
	
	protected Consumer<Streamer<T,R>> onComplete = null;
	
	protected int inQueueSize = 0;

	protected CallOptions callOptions = CallOptions.DEFAULT;
	
	
	/**
	 * Creates a new StreamerBuilder
	 * @param channel The managed channel to build the streamer with
	 * @param methodDescriptor The method to call
	 * @param outConsumer The outboud message consumer
	 * @return The StreamerBuilder
	 */
	public static <T,R> StreamerBuilder<T,R> newBuilder(Channel channel, MethodDescriptor<T, R> methodDescriptor, Consumer<R> outConsumer) {
		return new StreamerBuilder<T,R>(channel, methodDescriptor, outConsumer);
	}
	
	/**
	 * Creates a new StreamerBuilder that discards incoming responses
	 * @param channel The managed channel to build the streamer with
	 * @param methodDescriptor The method to call
	 * @return The StreamerBuilder
	 */
	public static <T,R> StreamerBuilder<T,R> newBuilder(Channel channel, MethodDescriptor<T, R> methodDescriptor) {
		return new StreamerBuilder<T,R>(channel, methodDescriptor, r -> {});
	}
	
	
	/**
	 * Creates a new StreamerBuilder
	 * @param channel The managed channel to build the streamer with
	 * @param methodDescriptor The method to call
	 * @param outConsumer The outboud message consumer
	 */
	private StreamerBuilder(Channel channel, MethodDescriptor<T, R> methodDescriptor, Consumer<R> outConsumer) {	
		this.channel = channel;
		this.methodDescriptor = methodDescriptor;
		this.outConsumer = outConsumer;
	}
	
	public BidiStreamer<T,R> buildBidiStreamer() {
		return new BidiStreamer<T,R>(this);
	}
	
	public ClientStreamer<T,R> buildClientStreamer() {
		return new ClientStreamer<T,R>(this);
	}
	
	public ServerStreamer<T,R> buildServerStreamer() {
		return new ServerStreamer<T,R>(this);
	}
	
	public Channel channel() {
		return channel;
	}

	public MethodDescriptor<T, R> methodDescriptor() {
		return methodDescriptor;
	}

	public Consumer<R> outConsumer() {
		return outConsumer;
	}
	
	public Function<T, Long> subItemsIn() {
		return subItemsIn;
	}
	
	public StreamerBuilder<T,R> descriptor(StreamDescriptor<T,R> desc) {
		subItemsIn = desc::subItemsIn;
		subItemsOut = desc::subItemsOut;
		finalResponse = desc::finalResponse;
		return this;
	}

	public StreamerBuilder<T,R> subItemsIn(Function<T, Long> subItemsIn) {
		this.subItemsIn = Objects.requireNonNull(subItemsIn, "Passed Inbound SubItem Counter was null");;
		return this;
	}

	public Function<R, Long> subItemsOut() {
		return subItemsOut;
	}

	public StreamerBuilder<T,R> subItemsOut(Function<R, Long> subItemsOut) {
		this.subItemsOut = Objects.requireNonNull(subItemsOut, "Passed Outbound SubItem Counter was null");
		return this;
	}

	public int inQueueSize() {
		return inQueueSize;
	}

	public StreamerBuilder<T,R> inQueueSize(int inQueueSize) {
		if(inQueueSize < 0) {
			throw new IllegalArgumentException("Invalid Inbound Queue Size: " + inQueueSize);
		}
		this.inQueueSize = inQueueSize;
		return this;
	}

	public Consumer<Streamer<T, R>> onComplete() {
		return onComplete;
	}

	public StreamerBuilder<T,R> onComplete(Consumer<Streamer<T, R>> onComplete) {
		this.onComplete = Objects.requireNonNull(onComplete, "OnComplete Consumer was null");
		return this;
	}

	public CallOptions callOptions() {
		return callOptions;
	}

	public StreamerBuilder<T,R> callOptions(CallOptions callOptions) {
		this.callOptions = Objects.requireNonNull(callOptions, "CallOptions was null");
		this.inQueueSize = this.callOptions.getOption(StreamDescriptor.DatapointsDescriptor.QUEUE_SIZE);
		return this;
	}

	public Function<R, Boolean> finalResponse() {
		return finalResponse;
	}

	public StreamerBuilder<T,R> finalResponse(Function<R, Boolean> finalResponse) {
		this.finalResponse = Objects.requireNonNull(finalResponse, "FinalResponse Function was null");
		return this;
	}
	
	
	
	
	/*
	 * 	SERVER:
	 *  client ---> server
	 *         <----
	 *         <----
	 *         
	 *  CLIENT:
	 *  client ---> server
	 *         --->
	 *         --->
	 *         <---
	 *         
	 *  BIDI:
	 *  client ---> server
	 *         --->
	 *         <---
	 *         --->
	 *         <---

	 */
	

}
