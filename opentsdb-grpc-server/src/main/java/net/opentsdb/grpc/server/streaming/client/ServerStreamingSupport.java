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

import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;

/**
 * <p>Title: ServerStreamingSupport</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.streaming.client.ServerStreamingSupport</code></p>
 */

public abstract class ServerStreamingSupport<T, R> extends AbstractStreamer<T, R> {

	/**
	 * Creates a new ServerStreamingSupport
	 * @param builder
	 */
	public ServerStreamingSupport(StreamerBuilder<T, R> builder) {
		super(builder);
	}

	
	protected StreamObserver<R> responseObserver() {
		return new ClientResponseObserver<T, R>() {

			@Override
			public void onNext(R message) {
				boolean isFinal = isFinalFx.apply(message);
				if(isFinal) {
					LOG.info("Final: {}", message);
					completion.countDown();
				} else {
					int outCount = subItemsOut.apply(message);
					responsesReceived.add(outCount);
					inFlight.add(outCount * -1);
					try {
						outConsumer.accept(message);
					} catch (Exception ex) {
						acceptErrors.increment();
					}
					
					if(clientClosed.get() && (inQueue==null || inQueue.isEmpty())) {
						checkComplete();
					}
//					LOG.info("REC: {}, final: {}", responsesReceived.longValue(), isFinal);
				}
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

			@Override
			public void beforeStart(ClientCallStreamObserver<T> rs) {
				requestStream = rs;
//				rs.disableAutoInboundFlowControl();
				rs.setOnReadyHandler(() -> {
					onReadyEvents.increment();
					if(inQueue != null) {
						LOG.info("Request Stream Ready. Queued: {}", inQueue.size());
						long writes = 0;
						while(rs.isReady()) {
							T t = inQueue.poll();
							if(t!=null) {
								int inCount = subItemsIn.apply(t);
								rs.onNext(t);
								rs.request(1);
								requestsSent.add(inCount);
								inFlight.add(inCount);
								writes++;
							} else {
								break;
							}
						}
						LOG.info("Request Queue Flushed. Queued: {}, Flushed: {}", inQueue.size(), writes);
						if(clientClosed.get() && inQueue.isEmpty()) {
							checkComplete();
						}
						LOG.info("");
					}
				});
			}
		};
	}
	

}
