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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

import com.stumbleupon.async.Callback;
import com.stumbleupon.async.Deferred;

import io.grpc.stub.StreamObserver;
import net.opentsdb.core.TSDB;
import net.opentsdb.grpc.AnnotationRequest;
import net.opentsdb.grpc.BulkAnnotationRequest;
import net.opentsdb.grpc.BulkAnnotationResponse;
import net.opentsdb.grpc.CreateAnnotationError;
import net.opentsdb.grpc.CreateAnnotationResponse;
import net.opentsdb.grpc.TSDBAnnotation;
import net.opentsdb.grpc.TsuidBorS;
import net.opentsdb.grpc.server.ProtoConverters;
import net.opentsdb.grpc.server.SuAsyncHelpers;
import net.opentsdb.meta.Annotation;
import net.opentsdb.plugin.common.Configuration;
import net.opentsdb.plugin.common.util.AccumulatingLongAdder;
import net.opentsdb.stats.StatsCollector;

/**
 * <p>Title: AnnotationHandler</p>
 * <p>Description: Server handler to handle Annotations</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.handlers.AnnotationHandler</code></p>
 */

public class AnnotationHandler extends AbstractHandler implements  AnnotationHandlerMBean {
	protected final LongAdder getRequests = new LongAdder();
	protected final LongAdder createRequests = new LongAdder();
	protected final LongAdder okCreates = new LongAdder();
	protected final LongAdder failedCreates = new LongAdder();
	
	/**
	 * Creates a new AnnotationHandler
	 * @param tsdb The parent TSDB instance
	 * @param cfg The extended configuration instance
	 */
	public AnnotationHandler(TSDB tsdb, Configuration cfg) {
		super(tsdb, cfg);
	}
	
	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.server.handlers.AnnotationHandlerMBean#getAnnotationRequests()
	 */
	public long getAnnotationRequests() {
		return getRequests.longValue();
	}
	
	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.server.handlers.AnnotationHandlerMBean#getCreateRequests()
	 */
	public long getCreateRequests() {
		return createRequests.longValue();
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.server.handlers.AnnotationHandlerMBean#getSuccessfulCreates()
	 */
	public long getSuccessfulCreates() {
		return okCreates.longValue();
	}
	
	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.server.handlers.AnnotationHandlerMBean#getFailedCreates()
	 */
	public long getFailedCreates() {
		return failedCreates.longValue();
	}
	
	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.server.handlers.AbstractHandler#doStats(net.opentsdb.grpc.server.handlers.GrpcStatsCollector)
	 */
	@Override
	protected void doStats(StatsCollector collector) {
		collector.record("getrequests", getRequests.longValue());
		
	}
	
	public void getAnnotation(AnnotationRequest request, StreamObserver<TSDBAnnotation> responseObserver) {
		LOG.debug("getAnnotation: {}", request);
		getRequests.increment();
		long startTime = request.getStartTime();
		TsuidBorS ts = request.getTsuid();
		try {
			Deferred<Annotation> def = null;
			switch(ts.getTsuidCase()) {
			case TSUIDBYTES:
				def = Annotation.getAnnotation(tsdb, ts.getTsuidBytes().toByteArray(), startTime);
				break;
			case TSUIDNAME:
				def = Annotation.getAnnotation(tsdb, ts.getTsuidName(), startTime);
				break;
			case TSUID_NOT_SET:
				def = Annotation.getAnnotation(tsdb, startTime);
			}
			def.addCallbacks(
					new Callback<Void, Annotation>(){
						@Override
						public Void call(Annotation ann) throws Exception {
							responseObserver.onNext(ProtoConverters.from(ann));
							responseObserver.onCompleted();
							return null;
						}
					},
					new Callback<Void, Exception>(){
						@Override
						public Void call(Exception ex) throws Exception {
							LOG.error("Failed to get annotation", ex);
							responseObserver.onError(ex);
							return null;
						}
					}
					);
		} catch (Exception ex) {
			responseObserver.onError(ex);
		}		
	}

	
	/**
	 * Starts a bi-directional stream, accepting TSDBAnnotations and returning CreateAnnotationResponses.
	 * @param responseObserver The response observer to return responses on
	 * @return The stream observer to handle incoming TSDBAnnotations
	 */
	public StreamObserver<TSDBAnnotation> createAnnotations(StreamObserver<CreateAnnotationResponse> responseObserver) {
		LOG.info("Streaming Annotations Initiated");
		final AtomicBoolean open = new AtomicBoolean(true);

		return new StreamObserver<TSDBAnnotation>() {
			final long startTime = System.currentTimeMillis();
			final LongAdder _okCreates = new AccumulatingLongAdder(okCreates);
			final LongAdder _failedCreates = new AccumulatingLongAdder(failedCreates);
			
			@Override
			public void onNext(TSDBAnnotation value) {
				LOG.debug("createAnnotation: {}", value);
				createRequests.increment();
				Annotation a = ProtoConverters.from(value);
				Deferred<Boolean> def = a.syncToStorage(tsdb, false);
				SuAsyncHelpers.singleTCallbacks(def, 
						bool -> {
							if(bool) {
								_okCreates.increment();
								responseObserver.onNext(CreateAnnotationResponse.newBuilder().setSuccess(1).build());
							} else {
								_failedCreates.increment();
								responseObserver.onNext(response(value, new Exception("CAS Failure saving annotation")));
							}							
						}, 
						err -> {
							LOG.error("Failed to save annotation", err);
							_failedCreates.increment();
							responseObserver.onNext(response(value, err));
						}
				);
			}

			@Override
			public void onError(Throwable t) {
				LOG.info("Streaming Annotations Progress: creates={}, failed={}, elapsed={}", _okCreates.longValue(), _failedCreates.longValue(), System.currentTimeMillis()-startTime);		
				if(open.compareAndSet(true, false)) {
					activeStreams.decrementAndGet();
				}
			}

			@Override
			public void onCompleted() {
				try {
					LOG.info("Streaming Annotations Complete: creates={}, failed={}, elapsed={}", _okCreates.longValue(), _failedCreates.longValue(), System.currentTimeMillis()-startTime);
					responseObserver.onNext(response(_okCreates.longValue(), _failedCreates.longValue()));
					responseObserver.onCompleted();
				} finally {
					if(open.compareAndSet(true, false)) {
						activeStreams.decrementAndGet();
					}
				}
				
			}

		};
	}

	/**
	 * Builds and returns a final CreateAnnotationResponse
	 * @param ok The number of successful annotations
	 * @param err The number of failed annotations
	 * @return the final CreateAnnotationResponse
	 */
	protected CreateAnnotationResponse response(long ok, long err) {
		return CreateAnnotationResponse.newBuilder()
				.setFailed(err)
				.setSuccess(ok)
				.setFinalResponse(true)
				.build();
	}
	
	/**
	 * Builds and returns a failed Annotation CreateAnnotationResponse
	 * @param ann The failed TSDBAnnotation
	 * @param t The Annotation create failure cause
	 * @return the failed TSDBAnnotation CreateAnnotationResponse
	 */
	protected CreateAnnotationResponse response(TSDBAnnotation ann, Throwable t) {
		return CreateAnnotationResponse.newBuilder()
				.setFailed(1)
				.addErrors(CreateAnnotationError.newBuilder()
					.setAnnotation(ann)
					.setError(t.getMessage())
					.build()
				)
				.build();
	}
	
	
	public StreamObserver<TSDBAnnotation> updateAnnotations(StreamObserver<TSDBAnnotation> responseObserver) {
		LOG.debug("updateAnnotations stream");
		return new StreamObserver<TSDBAnnotation>() {
			@Override
			public void onNext(TSDBAnnotation value) {
				LOG.debug("updateAnnotation: {}", value);
				Annotation a = ProtoConverters.from(value);
				a.syncToStorage(tsdb, true).addCallbacks(
						new Callback<Void, Boolean>(){
							@Override
							public Void call(Boolean result) throws Exception {
								if(result) {
									responseObserver.onNext(value);
								} else {
									responseObserver.onError(new Exception("CAS Failure saving annotation: " + value));
								}
								return null;
							}
						},
						new Callback<Void, Exception>(){
							@Override
							public Void call(Exception ex) throws Exception {
								LOG.error("Failed to save annotation", ex);
								responseObserver.onError(new Exception("Failed to save annotation: " + value, ex));
								return null;
							}
						}
						);

			}

			@Override
			public void onError(Throwable t) {
				LOG.error("updateAnnotations inbound stream failure", t);				
			}

			@Override
			public void onCompleted() {
				responseObserver.onCompleted();
			}

		};
	}

	
	public void deleteAnnotations(TSDBAnnotation request, StreamObserver<TSDBAnnotation> responseObserver) {
		LOG.debug("deleteAnnotations: {}", request);
		final Annotation ann = ProtoConverters.from(request);
		ann.delete(tsdb).addCallbacks(
				new Callback<Void, Object>(){
					@Override
					public Void call(Object result) throws Exception {
						responseObserver.onNext(request);
						return null;
					}
				},
				new Callback<Void, Exception>(){
					@Override
					public Void call(Exception ex) throws Exception {
						LOG.error("Failed to delete annotation", ex);
						responseObserver.onError(new Exception("Failed to delete annotation: " + request, ex));
						return null;
					}
				}
				
		);		
	}

	
	public void bulkDeleteAnnotations(BulkAnnotationRequest request,
			StreamObserver<BulkAnnotationResponse> responseObserver) {
		
		
	}
	

}
