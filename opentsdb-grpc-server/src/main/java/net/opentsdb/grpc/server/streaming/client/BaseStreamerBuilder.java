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
package net.opentsdb.grpc.server.streaming.client;

import java.util.function.Consumer;
import java.util.function.Function;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

/**
 * <p>Title: BaseStreamerBuilder</p>
 * <p>Description: A base class for Streamer Builders</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.streaming.client.BaseStreamerBuilder</code></p>
 */

public class BaseStreamerBuilder<T,R, B extends BaseStreamerBuilder<T,R,B>> {
	protected final Channel channel;
	protected final MethodDescriptor<T, R> methodDescriptor;
	protected final Consumer<R> outConsumer;

	
	protected Function<T, Integer> subItemsIn = (t) -> 1;
	protected Function<R, Integer> subItemsOut = (r) -> 1;

	
	
	
	/**
	 * Creates a new BaseStreamerBuilder
	 * @param channel The managed channel to build the streamer with
	 * @param methodDescriptor The method to call
	 * @param outConsumer The outboud message consumer
	 */
	protected BaseStreamerBuilder(Channel channel, MethodDescriptor<T, R> methodDescriptor, Consumer<R> outConsumer) {	
		this.channel = channel;
		this.methodDescriptor = methodDescriptor;
		this.outConsumer = outConsumer;
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
	
	public Function<T, Integer> subItemsIn() {
		return subItemsIn;
	}

	public B subItemsIn(Function<T, Integer> subItemsIn) {
		this.subItemsIn = subItemsIn;
		return (B) this;
	}

	public Function<R, Integer> getSubItemsOut() {
		return subItemsOut;
	}

	public B subItemsOut(Function<R, Integer> subItemsOut) {
		this.subItemsOut = subItemsOut;
		return (B) this;
	}
	
	
	
	/*
	 *  client ---> server
	 *         <----
	 *         <----
	 *         
	 *  client ---> server
	 *         --->
	 *         --->
	 *         
	 *  client ---> server
	 *         --->
	 *         <---
	 *         --->
	 *         <---

	 */
	

}
