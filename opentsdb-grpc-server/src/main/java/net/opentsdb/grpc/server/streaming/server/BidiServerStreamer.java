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

import io.grpc.stub.StreamObserver;

/**
 * <p>Title: BidiServerStreamer</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.streaming.server.BidiServerStreamer</code></p>
 */

public class BidiServerStreamer<T, R> extends AbstractServerStreamer<T, R> {

	/**
	 * Creates a new BidiServerStreamer
	 * @param builder
	 * @param ss
	 * @param responseObserver
	 */
	public BidiServerStreamer(StreamerBuilder<T, R> builder, StreamerContext streamerContext, StreamObserver<R> responseObserver) {
		super(builder, streamerContext, responseObserver);
	}
	
	public BidiServerStreamer<T,R> start() {
		super.start();
		return this;
	}

}
