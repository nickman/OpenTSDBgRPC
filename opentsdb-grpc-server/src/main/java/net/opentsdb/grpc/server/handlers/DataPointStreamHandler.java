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
import net.opentsdb.grpc.DataPoint;
import net.opentsdb.grpc.OpenTSDBServiceGrpc;
import net.opentsdb.grpc.PutDatapointError;
import net.opentsdb.grpc.PutDatapoints;
import net.opentsdb.grpc.PutDatapointsResponse;
import net.opentsdb.grpc.server.SuAsyncHelpers;
import net.opentsdb.grpc.server.streaming.server.StreamerBuilder;
import net.opentsdb.grpc.server.streaming.server.StreamerContainer;
import net.opentsdb.grpc.server.streaming.server.StreamerContext;
import net.opentsdb.plugin.common.Configuration;
import net.opentsdb.stats.StatsCollector;

/**
 * <p>Title: DataPointStreamHandler</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.handlers.DataPointStreamHandler</code></p>
 */

public class DataPointStreamHandler extends AbstractHandler<PutDatapoints, PutDatapointsResponse> {
	/** An empty put response constant */
	protected static final PutDatapointsResponse EMPTY_PUT_RESPONSE = PutDatapointsResponse.newBuilder()
			.setSuccess(-1)
			.setFailed(-1)
			.build();
	
	
	protected final StreamerContainer<PutDatapoints, PutDatapointsResponse> putDataPointsStreamContainer = 
			new StreamerContainer<>(new StreamerBuilder<>(OpenTSDBServiceGrpc.getPutsMethod(), this)
				.subItemsIn(this::countIn)
				.subItemsOut(this::countOut)
			);
	
	public int countIn(PutDatapoints pd) { return pd.getDataPointsCount(); }
	public int countOut(PutDatapointsResponse pdr) { return (int)(pdr.getFailed() + pdr.getSuccess()); }

	/**
	 * Creates a new DataPointStreamHandler
	 * @param tsdb
	 * @param cfg
	 */
	public DataPointStreamHandler(TSDB tsdb, Configuration cfg) {
		super(tsdb, cfg);
	}
	
	public StreamObserver<PutDatapoints> puts(StreamObserver<PutDatapointsResponse> responseObserver) {
		return putDataPointsStreamContainer.newBidiStreamer(responseObserver).start();
	}

	
	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.server.handlers.AbstractHandler#doStats(net.opentsdb.grpc.server.handlers.GrpcStatsCollector)
	 */
	@Override
	protected void doStats(StatsCollector collector) {
		putDataPointsStreamContainer.doStats(collector);
	}
	

	@Override
	public CompletableFuture<PutDatapointsResponse> invoke(PutDatapoints putDatapoints, StreamerContext sc) {
		final boolean details = putDatapoints.getDetails();
		CompletableFuture<PutDatapointsResponse> cf = new CompletableFuture<PutDatapointsResponse>();
		final LongAdder _okDataPoints = sc.accProcessedItems();
		final LongAdder _failedDataPoints = sc.accFailedItems();
		final List<PutDatapointError> errors = details ? new ArrayList<>() : null;
		int dpSize = putDatapoints.getDataPointsCount();
		LOG.info("Putting Datapoints: count={}, details={}", dpSize, details);
		try {
			List<Deferred<Object>> defs = new ArrayList<>(dpSize);
			for(int idx = 0; idx < dpSize; idx ++) {
				if(!sc.isOpen()) {
					LOG.info("Puts cancelled. Breaking.");
					break;
				}
				DataPoint dp = putDatapoints.getDataPoints(idx);
				try {						
					double dval = dp.getValue();
					long lval = (long)dval;
					double fpart = dval - lval;
					boolean isDouble = fpart==0D;
					Deferred<Object> def = null;
					if(isDouble) {
						def = tsdb.addPoint(dp.getMetric(), dp.getTimestamp(), dval, dp.getMetricTags().getTagsMap());
					} else {
						def = tsdb.addPoint(dp.getMetric(), dp.getTimestamp(), lval, dp.getMetricTags().getTagsMap());
					}
					
					SuAsyncHelpers.singleTCallbacks(def, 
							obj -> {
								_okDataPoints.increment();
							}, 
							err -> {
								_failedDataPoints.increment();
								if(details) {
									errors.add(PutDatapointError.newBuilder()
											.setDataPoint(dp)
											.setError(err.getMessage())
											.build());
								}
								if(LOG.isDebugEnabled()) {
									LOG.warn("DataPoint put failed: dp={}", dp, err);
								} else {
									LOG.warn("DataPoint put failed: dp={}, err={}", dp, err);
								}
							}
					);
					defs.add(def);
				} catch (Exception err) {
					_failedDataPoints.increment();
					if(details) {
						errors.add(PutDatapointError.newBuilder()
								.setDataPoint(dp)
								.setError(err.getMessage())
								.build());
					}
					if(LOG.isDebugEnabled()) {
						LOG.warn("DataPoint put failed: dp={}", dp, err);
					} else {
						LOG.warn("DataPoint put failed: dp={}, err={}", dp, err);
					}
				}
			}
			LOG.info("Grouping DPoint Deferreds: {}", defs.size());
			if(sc.isOpen()) {
				SuAsyncHelpers.singleTBoth(Deferred.group(defs), (o,t) -> {
					if(t!=null) {
						LOG.error("Batch Write Failed", t);							
					}
					if(sc.isOpen()) {
						cf.complete(response(_okDataPoints.longValue(), _failedDataPoints.longValue(), errors, false));
					} else {
						LOG.info("Response not sent due to cancellation");
					}
				});
			} else {
				LOG.info("Response not prepared due to cancellation");
				cf.complete(null);
			}
		} catch (Exception ex) {			
			LOG.error("DP Failure", ex);
			cf.completeExceptionally(ex);
		}
		return cf;
		
	}

	@Override
	public PutDatapointsResponse closer(StreamerContext sc) {
		return response(sc.getProcessedItems(), sc.getFailedItems(), null, true);
	}
	
	/**
	 * Builds and returns a PutDatapointsResponse
	 * @param ok The number of successful datapoints
	 * @param err The number of failed datapoints
	 * @param finalResponse true if final, false otherwise
	 * @return the final PutDatapointsResponse
	 */
	protected PutDatapointsResponse response(long ok, long err, List<PutDatapointError> errors, boolean finalResponse) {
		PutDatapointsResponse.Builder pdr = PutDatapointsResponse.newBuilder()
				.setFailed(err)
				.setSuccess(ok)
				.setFinalResponse(finalResponse);
		if(errors!=null && !errors.isEmpty()) {
			pdr.addAllErrors(errors);
		}
		LOG.info("Sending PDR: ok={}, failed={}, final={}", ok, err, finalResponse);
		return pdr.build();
	}
	
	/**
	 * Builds and returns a failed DataPoint PutDatapointsResponse
	 * @param dp The failed DataPoint
	 * @param t The DataPoint failure cause
	 * @return the failed DataPoint PutDatapointsResponse
	 */
	protected PutDatapointsResponse response(DataPoint dp, Throwable t) {
		return PutDatapointsResponse.newBuilder()
				.setFailed(1)
				.addErrors(PutDatapointError.newBuilder()
					.setDataPoint(dp)
					.setError(t.getMessage())
					.build()
				)
				.build();
	}

}
