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

import io.grpc.stub.StreamObserver;

/**
 * <p>Title: ClientStreamer</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.streaming.ClientStreamer</code></p>
 */

public class ClientStreamer<T, R> extends AbstractStreamer<T, R> {
	protected final boolean noQueue;
	/**
	 * Creates a new ClientStreamer
	 * @param builder
	 */
	public ClientStreamer(StreamerBuilder<T, R> builder) {
		super(builder);
		noQueue = inQueue == null;
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.server.streaming.Streamer#send(java.lang.Object)
	 */
	public boolean send(T t) {
		if(clientClosed.get()) {
			throw new IllegalStateException("Streamer client is closed");
		}
		int inCount = subItemsIn.apply(t);
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


	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.server.streaming.AbstractStreamer#responseObserver()
	 */
	@Override
	protected StreamObserver<R> responseObserver() {
		return new StreamObserver<R>() {

			@Override
			public void onNext(R value) {
				outConsumer.accept(value);
			}

			@Override
			public void onError(Throwable t) {
				LOG.error("Response Observer: Stream error", t);
				completion.countDown();
			}

			@Override
			public void onCompleted() {
				LOG.info("Response Observer:Stream Complete\n{}", printStats());				
				completion.countDown();
			}
			
		};
	}

}
