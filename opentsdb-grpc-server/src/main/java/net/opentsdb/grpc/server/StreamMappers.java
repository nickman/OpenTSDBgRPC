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
package net.opentsdb.grpc.server;

import java.util.Objects;
import java.util.function.Function;

import com.stumbleupon.async.Callback;
import com.stumbleupon.async.Deferred;

import io.grpc.stub.StreamObserver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * <p>Title: StreamMappers</p>
 * <p>Description: Helpers to map between Fluxes/Monos and StreamObservers</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.StreamMappers</code></p>
 */

public class StreamMappers {

	public static class SinkPair<T> {
		private final Flux<T> flux;
		private final FluxSink<T>[] sink;

		private SinkPair(Flux<T> flux, FluxSink<T>[] sink) {
			this.flux = flux;
			this.sink = sink;
		}

		public Flux<T> flux() {
			return flux;
		}

		public FluxSink<T> sink() {
			if(sink[0]==null) {
				throw new IllegalArgumentException("Sink was null. Was flux subscribed  to ?");
			}
			return sink[0];
		}
	}

	public static <T> SinkPair<T> sinkPair() {
		@SuppressWarnings("unchecked")
		FluxSink<T>[] sinkRef = new FluxSink[1];
		Flux<T> flux = Flux.create(sink -> {
			sinkRef[0] = sink;
		});    
		return new SinkPair<T>(flux, sinkRef);
	}

	public static <T,E> void fluxToStreamObserver(
			StreamObserver<E> observer, Flux<T> flux, Function<T,E> fx) {
		Objects.requireNonNull(observer, "The passed observer was null");
		Objects.requireNonNull(flux, "The passed flux was null");
		Objects.requireNonNull(fx, "The passed function was null");
		flux.subscribe(
				t -> observer.onNext(fx.apply(t)),
				err -> observer.onError(err),
				() -> observer.onCompleted()        
				);
	}

	public static <T> void fluxToStreamObserver(
			StreamObserver<T> observer, Flux<T> flux) {
		fluxToStreamObserver(observer, flux, (a) -> a);
	}

	private static <T,E> Tuple2<SinkPair<T>, StreamObserver<E>> streamObserverToSinkPair(Function<E,T> fx) {
		Objects.requireNonNull(fx, "The passed function was null");
		SinkPair<T> sp = sinkPair();
		StreamObserver<E> observer = new StreamObserver<E>() {

			@Override
			public void onNext(E e) {
				sp.sink().next(fx.apply(e));        
			}

			@Override
			public void onError(Throwable t) {
				sp.sink().error(t);
			}

			@Override
			public void onCompleted() {
				sp.sink().complete();
			}

		};
		return Tuples.of(sp, observer);
	}


	public static <T,E> Tuple2<Flux<T>, StreamObserver<E>> streamObserverToFlux(Function<E,T> fx) {
		Objects.requireNonNull(fx, "The passed function was null");
		Tuple2<SinkPair<T>, StreamObserver<E>> t = streamObserverToSinkPair(fx);
		return Tuples.of(t.getT1().flux(), t.getT2());
	}

	public static <T> Tuple2<Flux<T>, StreamObserver<T>> streamObserverToFlux() {
		return streamObserverToFlux(a -> a);
	}
	
	public static <T> Mono<T> monoFromDeferred(Deferred<T> def) {
		return Mono.create(sink -> {
			def.addErrback(new Callback<Void, Throwable>() {
				public Void call(Throwable t) throws Exception {
					sink.error(t);
					return null;
				}
			})
			.addCallback(new Callback<Void, T>() {
				public Void call(T t) throws Exception {
					sink.success(t);
					return null;
				}				
			});			
		});
	}



	private StreamMappers() {}

}
