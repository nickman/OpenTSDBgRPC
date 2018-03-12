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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.grpc.MethodDescriptor;


/**
 * <p>Title: StreamerBuilder</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.streaming.server.StreamerBuilder</code></p>
 */

public class StreamerBuilder<T,R> {

	protected Function<T, Integer> subItemsIn = (t) -> 1;
	protected Function<R, Integer> subItemsOut = (r) -> 1;
	
	protected final MethodDescriptor<T,R> md;
	protected final BiFunction<T,StreamerContext,CompletableFuture<R>> streamerFx;
	

	/**
	 * Creates a new StreamerBuilder
	 */
	public StreamerBuilder(MethodDescriptor<T,R> md, BiFunction<T,StreamerContext,CompletableFuture<R>> streamerFx) {
		this.md = md;
		this.streamerFx = streamerFx;
	}
	
	
	public MethodDescriptor<T,R> methodDescriptor() {
		return md;
	}
	
	public Function<T, Integer> subItemsIn() {
		return subItemsIn;
	}

	public StreamerBuilder<T,R> subItemsIn(Function<T, Integer> subItemsIn) {
		this.subItemsIn = Objects.requireNonNull(subItemsIn, "Passed Inbound SubItem Counter was null");;
		return this;
	}

	public Function<R, Integer> subItemsOut() {
		return subItemsOut;
	}

	public StreamerBuilder<T,R> subItemsOut(Function<R, Integer> subItemsOut) {
		this.subItemsOut = Objects.requireNonNull(subItemsOut, "Passed Outbound SubItem Counter was null");
		return this;
	}

}
