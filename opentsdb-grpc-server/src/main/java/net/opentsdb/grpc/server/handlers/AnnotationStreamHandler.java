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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.LongAdder;

import com.stumbleupon.async.Deferred;

import io.grpc.stub.StreamObserver;
import net.opentsdb.core.TSDB;
import net.opentsdb.grpc.CreateAnnotationError;
import net.opentsdb.grpc.CreateAnnotationResponse;
import net.opentsdb.grpc.OpenTSDBServiceGrpc;
import net.opentsdb.grpc.TSDBAnnotation;
import net.opentsdb.grpc.TSDBAnnotations;
import net.opentsdb.grpc.server.ProtoConverters;
import net.opentsdb.grpc.server.SuAsyncHelpers;
import net.opentsdb.grpc.server.streaming.server.StreamerBuilder;
import net.opentsdb.grpc.server.streaming.server.StreamerContainer;
import net.opentsdb.grpc.server.streaming.server.StreamerContext;
import net.opentsdb.meta.Annotation;
import net.opentsdb.plugin.common.Configuration;
import net.opentsdb.stats.StatsCollector;

/**
 * <p>Title: AnnotationStreamHandler</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.handlers.AnnotationStreamHandler</code></p>
 */

public class AnnotationStreamHandler extends AbstractHandler<TSDBAnnotations, CreateAnnotationResponse> {

	public AnnotationStreamHandler(TSDB tsdb, Configuration cfg, boolean local) {
		super(tsdb, cfg, local);
	}
	
	protected final StreamerContainer<TSDBAnnotations, CreateAnnotationResponse> createAnnotationsStreamContainer = 
			new StreamerContainer<>(new StreamerBuilder<>(OpenTSDBServiceGrpc.getCreateAnnotationsMethod(), this), local);
	
	public StreamObserver<TSDBAnnotations> createAnnotations(StreamObserver<CreateAnnotationResponse> responseObserver) {
		return createAnnotationsStreamContainer.newBidiStreamer(responseObserver).start();
	}
	
	@Override
	public CompletableFuture<CreateAnnotationResponse> invoke(TSDBAnnotations t, StreamerContext sc) {
		final boolean details = t.getDetails();
		CompletableFuture<CreateAnnotationResponse> cf = new CompletableFuture<>();
		final LongAdder _okAnnotations = sc.accProcessedItems();
		final LongAdder _failedAnnotations = sc.accFailedItems();
		final List<CreateAnnotationError> errors = details ? new ArrayList<>() : null;
		int annSize = t.getAnnotationsCount();
		LOG.info("Creating Annotations: count={}, details={}", annSize, details);
		List<Deferred<Boolean>> defs = new ArrayList<>(annSize);
		for(int idx = 0; idx < annSize; idx ++) {
			if(!sc.isOpen()) {
				LOG.info("Annotations creation cancelled. Breaking.");
				break;
			}
			TSDBAnnotation tann = t.getAnnotations(idx);
			Annotation ann = ProtoConverters.from(tann);
			try {
				Deferred<Boolean> def = ann.syncToStorage(tsdb, true);
				SuAsyncHelpers.singleTCallbacks(def, 
						bool -> {
							if(bool) {
								_okAnnotations.increment();
							} else {
								_failedAnnotations.increment();
								errors.add(CreateAnnotationError.newBuilder()
									.setAnnotation(tann)
									.setError("CAS Failure")
									.build()	
								);
							}							
						}, 
						err -> {
							LOG.error("Failed to save annotation", err);
							_failedAnnotations.increment();
							errors.add(CreateAnnotationError.newBuilder()
									.setAnnotation(tann)
									.setError(err.getMessage())
									.build()	
								);
							
						}
				);
				defs.add(def);
			} catch (Exception ex) {
				LOG.error("Failed to save annotation", ex);
				_failedAnnotations.increment();
				errors.add(CreateAnnotationError.newBuilder()
						.setAnnotation(tann)
						.setError(ex.getMessage())
						.build()	
					);
			}
			
		}
		LOG.info("Grouping TSDBAnnotation Deferreds: {}", defs.size());
		if(sc.isOpen()) {
			SuAsyncHelpers.singleTBoth(Deferred.group(defs), (o,err) -> {
				if(err!=null) {
					LOG.error("Batch Write Failed", err);							
				}
				if(sc.isOpen()) {
					cf.complete(response(_okAnnotations.longValue(), _failedAnnotations.longValue(), errors, false));
				} else {
					LOG.info("Response not sent due to cancellation");
				}
			});
		} else {
			LOG.info("Response not prepared due to cancellation");
			cf.complete(null);
		}
		
		return cf;
	}
	
	/**
	 * Builds and returns a PutDatapointsResponse
	 * @param ok The number of successful datapoints
	 * @param err The number of failed datapoints
	 * @param finalResponse true if final, false otherwise
	 * @return the final PutDatapointsResponse
	 */
	protected CreateAnnotationResponse response(long ok, long err, List<CreateAnnotationError> errors, boolean finalResponse) {
		CreateAnnotationResponse.Builder pdr = CreateAnnotationResponse.newBuilder()
				.setFailed(err)
				.setSuccess(ok)
				.setFinalResponse(finalResponse);
		if(errors!=null && !errors.isEmpty()) {
			pdr.addAllErrors(errors);
		}
		LOG.info("Sending PDR: ok={}, failed={}, final={}", ok, err, finalResponse);
		return pdr.build();
	}
	

	@Override
	protected void doStats(StatsCollector collector) {
		createAnnotationsStreamContainer.doStats(collector);		
	}

}
