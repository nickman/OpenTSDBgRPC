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

/**
 * <p>Title: BidiStreamer</p>
 * <p>Description: Encapsulates a generic bidirectional client call</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.Streamer</code></p>
 */

public class BidiStreamer<T, R> extends ServerStreamingSupport<T,R> {
	protected final boolean noQueue;
	
	public BidiStreamer(StreamerBuilder<T,R> builder) {
		super(builder);
		noQueue = inQueue == null;
	}
	
	public BidiStreamer<T,R> start() {
		super.start();
		LOG.info("BidiStreamer Started: {}", md.getFullMethodName());
		return this;
	}
	
	public boolean send(T t) {
		if(clientClosed.get()) {
			throw new IllegalStateException("Streamer client is closed");
		}
		long inCount = subItemsIn.apply(t);
		if(noQueue || requestStream.isReady() ) {
			requestStream.onNext(t);
			requestStream.request(1);
			requestsSent.add(inCount);
			inFlight.addAndGet(inCount);			
		} else {
			if(!inQueue.offer(t)) {
				requestsDropped.increment();
				return false;
			}
		}
		return true;
	}
	
	

	
}
