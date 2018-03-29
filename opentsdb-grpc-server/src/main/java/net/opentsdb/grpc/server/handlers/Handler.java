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
package net.opentsdb.grpc.server.handlers;

import java.util.concurrent.CompletableFuture;

import net.opentsdb.grpc.server.streaming.server.StreamerContext;
import reactor.core.publisher.Flux;

/**
 * <p>Title: Handler</p>
 * <p>Description: Top level interface for handler implementations. Handlers are the endpoints
 * that are invoked by gRPC servers</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.handlers.Handler</code></p>
 */

public interface Handler<T, R> {
	/**
	 * Invokes the handler
	 * @param t The invocation parameter
	 * @param sc The streamer context. Passes a placeholder if it's a Unary method
	 * @return a completable future that will resolve to a response
	 */
	public CompletableFuture<R> invoke(T t, StreamerContext sc);
	
	public CompletableFuture<Flux<R>> invokeForFlux(T t, StreamerContext sc);
	
	/**
	 * For handlers that support it, requests the final response.
	 * @param sc The streamer context. Passes a placeholder if it's a Unary method
	 * @return the final response, or null if not supported.
	 */
	public R closer(StreamerContext sc);
}
