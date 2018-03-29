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

import java.util.concurrent.CompletableFuture;

import io.grpc.stub.StreamObserver;
import net.opentsdb.grpc.TXTime;
import reactor.core.publisher.Flux;

/**
 * <p>Title: BidiServerStreamer</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.streaming.server.ServerStreamer</code></p>
 */

public class ServerStreamer<T, R> extends AbstractServerStreamer<T, R> {
	protected final T request;
	/**
	 * Creates a new ServerStreamer
	 * @param builder
	 * @param ss
	 * @param responseObserver
	 */
	public ServerStreamer(StreamerBuilder<T, R> builder, StreamerContext streamerContext, StreamObserver<R> responseObserver, T request) {
		super(builder, streamerContext, responseObserver);
		this.request = request;
	}
	
	public ServerStreamer<T,R> start() {
		super.start();
		return this;
	}
	
	
	@Override
	public void onNext(T value) {
		final long startTime = System.currentTimeMillis();
		
		final boolean hasTxTime = rpcTypes.hasTXTime;
		final long sentTime;
		final long sentElapsed;
		
		final TXTime txTime;
		if(hasTxTime) {
			txTime = rpcTypes.getTXTimeT(value);
			sentTime = txTime.getTxtime();
			sentElapsed = startTime - sentTime;
		} else {
			txTime = null;
			sentTime = -1L;
			sentElapsed = -1L;			
		}
		final int itemCount = subItemsIn.apply(value);
		LOG.info("Received Message: items={}", itemCount);
		sc.received(itemCount);
		try {
			CompletableFuture<Flux<R>> pendingResponse = handler.invokeForFlux(value, sc);
			if(pendingResponse!=null) {
				pendingResponse.whenComplete((f, t) -> {
					if(t!=null) {
						LOG.error("Failed to process message", t);
						sc.failed(itemCount);						
					} else {
						f.subscribe(
								r -> {
									if(hasTxTime) {
										long now = System.currentTimeMillis();
										TXTime tx = TXTime.newBuilder()
												.setStime(sentElapsed)
												.setTxtime(txTime.getTxtime())
												.setPtime(now - startTime)
												.setRtime(now)
												.build();
										
										R modR = rpcTypes.setTXTimeR(r, tx);
										responseObserver.onNext(modR);
									} else {
										responseObserver.onNext(r);
									}
									sc.sent(subItemsOut.apply(r));
								},
								err -> responseObserver.onError(err),
								() -> responseObserver.onCompleted()
						);
					}
				});
			}
		} catch (Exception ex) {
			LOG.error("Failed to process message", ex);
		}		
	}
	


}
