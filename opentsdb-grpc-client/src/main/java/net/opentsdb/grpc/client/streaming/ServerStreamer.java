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

import io.grpc.stub.ClientCalls;

/**
 * <p>Title: ServerStreamer</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.streaming.ServerStreamer</code></p>
 * @param <T> The request type
 * @param <R> The response type
 */

public class ServerStreamer<T, R> extends ServerStreamingSupport<T, R> {

	/**
	 * Creates a new ServerStreamer
	 * @param builder
	 */
	public ServerStreamer(StreamerBuilder<T, R> builder) {
		super(builder);
	}
	
	public ServerStreamer<T,R> start() {
		super.start();
		LOG.info("ServerStreamer Started: {}", md.getFullMethodName());
		return this;
	}


	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.server.streaming.Streamer#send(java.lang.Object)
	 */
	@Override
	public boolean send(T t) {
		ClientCalls.asyncServerStreamingCall(clientCall, t, responseObserver);
		requestsSent.increment();
		return true;
	}


}
