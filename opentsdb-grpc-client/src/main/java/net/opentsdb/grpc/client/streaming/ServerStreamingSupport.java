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

import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.StreamObserver;

/**
 * <p>Title: ServerStreamingSupport</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.streaming.ServerStreamingSupport</code></p>
 * @param <T> The request type
 * @param <R> The response type
 */

public abstract class ServerStreamingSupport<T, R> extends AbstractStreamer<T, R> {

	/**
	 * Creates a new ServerStreamingSupport
	 * @param builder
	 */
	public ServerStreamingSupport(StreamerBuilder<T, R> builder) {
		super(builder);
	}
	
	protected abstract class BaseStreamObserver implements ClientResponseObserver<T,R> {
		
		@Override
		public void onNext(R r) {
			try {
				outConsumer.accept(r);
			} catch (Exception ex) {
				acceptErrors.increment();
			}
			handleNext(r);
		}
		
		protected abstract void handleNext(R r);
		
		@Override
		public void onError(Throwable t) {
//			LOG.error("Response Observer: Stream error", t);
			onErrorAction.accept(t, ServerStreamingSupport.this);
			completion.countDown();			
		}
		
/*
 * TODO: Detect Unavailable
2018-03-14 11:56:58,457 ERROR [grpc-default-executor-0] OpenTSDBService/Puts: Response Observer: Stream error
io.grpc.StatusRuntimeException: UNAVAILABLE: HTTP/2 error code: NO_ERROR
Received Rst Stream
	at io.grpc.Status.asRuntimeException(Status.java:526) ~[grpc-core-1.9.0.jar:1.9.0]
	at io.grpc.stub.ClientCalls$StreamObserverToCallListenerAdapter.onClose(ClientCalls.java:419) [grpc-stub-1.9.0.jar:1.9.0]
 */

		@Override
		public void onCompleted() {
			LOG.info("Response Observer:Stream Complete\n{}", printStats());				
			completion.countDown();
		}


		@Override
		public void beforeStart(ClientCallStreamObserver<T> rs) {
			requestStream = rs;
//			rs.disableAutoInboundFlowControl();
			rs.setOnReadyHandler(() -> {
				onReadyEvents.increment();
				if(inQueue != null) {
					LOG.info("Request Stream Ready. Queued: {}", inQueue.size());
					long writes = 0;
					while(rs.isReady()) {
						T t = inQueue.poll();
						if(t!=null) {
							long inCount = subItemsIn.apply(t);
							rs.onNext(t);
							rs.request(1);
							requestsSent.add(inCount);
							inFlight.addAndGet(inCount);
							writes++;
						} else {
							break;
						}
					}
					LOG.info("Request Queue Flushed. Queued: {}, Flushed: {}", inQueue.size(), writes);
					if(clientClosed.get() && inQueue.isEmpty()) {
						checkComplete();
					}						
				}
			});
		}
		
	}
	
	
	protected class FinalProvidingStreamObserver extends BaseStreamObserver {

		@Override
		protected void handleNext(R r) {
			if(isFinalFx.apply(r)) {
				LOG.info("FINAL Response");
//				requestObserver.onCompleted();
//				requestStream.onCompleted();
				onCompleted();
				responseObserver.onCompleted();
				completion.countDown();
				
			} else {
				long outCount = subItemsOut.apply(r);
				responsesReceived.add(outCount);
				if(inFlight.addAndGet(-outCount)==0) {
					if(clientClosed.get()) {
						clientCall.halfClose();
//						requestStream.onCompleted();
						System.err.println("HalfClosed Client !!!");
					}
				}
			}
			
		}
		
	}
	
	protected class NonFinalProvidingStreamObserver extends BaseStreamObserver {
		@Override
		protected void handleNext(R r) {
			long outCount = subItemsOut.apply(r);
			responsesReceived.add(outCount);
			long inf = inFlight.addAndGet(-outCount);
			if(inf==0 && clientClosed.get()) {
				LOG.info("Client Close Complete");
				requestObserver.onCompleted();
				completion.countDown();				
			}
			// TODO: DO we need to check the out queue ?			
		}
		
	}
	
	
	protected StreamObserver<R> responseObserver() {
		/*
		 * If expectsFinalResponse is true, then we don't count down 
		 * until we receive the final response (or there's an error).
		 * Otherwise, count down when there are zero in-flight and the
		 * client is closed.
		 */
		if(expectsFinalResponse) {
			return new FinalProvidingStreamObserver();
		}
		return new NonFinalProvidingStreamObserver();
	}
	

}
