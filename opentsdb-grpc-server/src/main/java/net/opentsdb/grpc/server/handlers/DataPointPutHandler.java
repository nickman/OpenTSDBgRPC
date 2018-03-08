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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

import com.stumbleupon.async.Callback;
import com.stumbleupon.async.Deferred;

import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import net.opentsdb.core.TSDB;
import net.opentsdb.grpc.Aggregation;
import net.opentsdb.grpc.DataPoint;
import net.opentsdb.grpc.PutDatapointError;
import net.opentsdb.grpc.PutDatapoints;
import net.opentsdb.grpc.PutDatapointsResponse;
import net.opentsdb.grpc.PutOptions;
import net.opentsdb.grpc.server.Configuration;
import net.opentsdb.grpc.server.SuAsyncHelpers;
import net.opentsdb.grpc.server.util.AccumulatingLongAdder;
import net.opentsdb.grpc.server.util.RelTime;

/**
 * <p>Title: DataPointPutHandler</p>
 * <p>Description: Server handler to handle data point puts</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.handlers.DataPointPutHandler</code></p>
 */

public class DataPointPutHandler extends AbstractHandler implements DataPointPutHandlerMBean {
	/** An empty put response constant */
	protected static final PutDatapointsResponse EMPTY_PUT_RESPONSE = PutDatapointsResponse.newBuilder()
			.setSuccess(-1)
			.setFailed(-1)
			.build();
	
	protected final LongAdder receivedDataPoints = new LongAdder();
	protected final LongAdder okDataPoints = new LongAdder();
	protected final LongAdder failedDataPoints = new LongAdder();
	
	
	public long getReceivedDataPoints() {
		return receivedDataPoints.longValue();
	}
	
	
	public long getProcessedDataPoints() {
		return okDataPoints.longValue();
	}
	
	public long getFailedDataPoints() {
		return failedDataPoints.longValue();
	}
	
	@Override
	protected void doStats(GrpcStatsCollector collector) {
		collector.recordGrpc("rcvdp", receivedDataPoints.longValue());
		collector.recordGrpc("okdp", okDataPoints.longValue());
		collector.recordGrpc("faileddp", failedDataPoints.longValue());
	}

	/**
	 * Creates a new DataPointPutHandler
	 * @param tsdb The parent TSDB instance
	 * @param cfg The extended configuration instance
	 */
	public DataPointPutHandler(TSDB tsdb, Configuration cfg) {
		super(tsdb, cfg);
	}
	
	public StreamObserver<PutDatapoints> puts(StreamObserver<PutDatapointsResponse> responseObserver) {
		LOG.info("Streaming DataPoints Initiated: ({})", responseObserver.getClass().getName());
		ServerCallStreamObserver<PutDatapointsResponse> ro = (ServerCallStreamObserver<PutDatapointsResponse>)responseObserver;
		final AtomicBoolean open = new AtomicBoolean(true);
		activeStreams.incrementAndGet();
		totalStreams.increment();
		
		return new StreamObserver<PutDatapoints>() {			
			final long startTime = System.currentTimeMillis();
			final LongAdder _receivedDataPoints = new AccumulatingLongAdder(receivedDataPoints);
			final LongAdder _okDataPoints = new AccumulatingLongAdder(okDataPoints);
			final LongAdder _failedDataPoints = new AccumulatingLongAdder(failedDataPoints);
			
			@Override
			public void onNext(PutDatapoints datapoints) {
				int dpSize = datapoints.getDataPointsCount();
				LOG.info("Received {} Datapoints", dpSize);
				_receivedDataPoints.add(dpSize);
				final LongAdder oks = new LongAdder();
				final LongAdder errs = new LongAdder();
				try {
					List<Deferred<Object>> defs = new ArrayList<>(dpSize);
					for(int idx = 0; idx < dpSize; idx ++) {
						if(!open.get()) {
							LOG.info("Puts cancelled. Breaking.");
							break;
						}
						DataPoint dp = datapoints.getDataPoints(idx);
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
										oks.increment();
	//									responseObserver.onNext(PutDatapointsResponse.newBuilder().setSuccess(1).build());
									}, 
									err -> {
	//									responseObserver.onNext(response(dp, err));
										_failedDataPoints.increment();
										errs.increment();
										LOG.warn("DataPoint put failed: {}", dp, err);
									}
							);
						} catch (Exception err) {
	//						responseObserver.onNext(response(dp, err));
							_failedDataPoints.increment();
							errs.increment();
							LOG.warn("Failed to handle DataPoint: {}", dp, err);
						}
					}
					if(!open.get()) {
						LOG.info("Puts cancelled.");	
						return;
					}
					
					SuAsyncHelpers.singleTCallbacks(Deferred.group(defs),
							obj -> responseObserver.onNext(response(oks.intValue(), errs.intValue(), false)),
							err -> responseObserver.onNext(response(oks.intValue(), errs.intValue(), false))
					);
					// send response				
					
				} catch (Exception ex) {
					LOG.error("DP Failure", ex);
				}
			}

			@Override
			public void onError(Throwable t) {
				if(t.getMessage().contains("CANCELLED:")) {
					LOG.info("Streaming DataPoints Closed By Client: ok={}, failed={}, elapsed={}", _okDataPoints.longValue(), _failedDataPoints.longValue(), System.currentTimeMillis()-startTime);
				} else {
					LOG.error("Streaming DataPoints Error", t);
					LOG.info("Streaming DataPoints Progress: ok={}, failed={}, elapsed={}", _okDataPoints.longValue(), _failedDataPoints.longValue(), System.currentTimeMillis()-startTime);				
				}
				if(open.compareAndSet(true, false)) {
					activeStreams.decrementAndGet();
				}
			}

			@Override
			public void onCompleted() {
				try {
					if(open.get()) {
						LOG.info("Streaming DataPoints Complete: dps={}, elapsed={}", _okDataPoints.longValue(), System.currentTimeMillis()-startTime);
						responseObserver.onNext(response(_okDataPoints.longValue(), _failedDataPoints.longValue(), true));
						responseObserver.onCompleted();
					} else {
						LOG.info("Supressed Response due to cancellation");
					}
				} finally {
					if(open.compareAndSet(true, false)) {
						activeStreams.decrementAndGet();
					}
				}
			}

		};
		
	}
	
	/**
	 * Builds and returns a PutDatapointsResponse
	 * @param ok The number of successful datapoints
	 * @param err The number of failed datapoints
	 * @param finalResponse true if final, false otherwise
	 * @return the final PutDatapointsResponse
	 */
	protected PutDatapointsResponse response(long ok, long err, boolean finalResponse) {
		PutDatapointsResponse pdr = PutDatapointsResponse.newBuilder()
				.setFailed(err)
				.setSuccess(ok)
				.setFinalResponse(finalResponse)
				.build();
		LOG.info("Sending PDR: ok={}, failed={}, final={}", ok, err, finalResponse);
		return pdr;
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

	
	/**
	 * Accepts a datapoint put request and returns an empty, summary or detailed response.
	 * @param request The datapoint put request
	 * @param responseObserver The response observer
	 */
	public void put(PutDatapoints request, StreamObserver<PutDatapointsResponse> responseObserver) {
		PutOptions opts = request.getOptions();		
		boolean details = opts.getDetails();
		boolean summary = opts.getSummary();
		boolean noresp = !details && !summary;
		LongAdder success = new AccumulatingLongAdder(okDataPoints);
		LongAdder fails = new AccumulatingLongAdder(failedDataPoints);
		Map<DataPoint, String> failedDps = details ? new ConcurrentHashMap<>() : null;
		
		List<DataPoint> dps = request.getDataPointsList();
		int size = dps.size();
		receivedDataPoints.add(size);
		LOG.info("Putting DataPoints: size={}, details={}, summary={}", size, details, summary);
		final long startTime = System.currentTimeMillis();
		List<Deferred<Object>> defs = putDps(dps, noresp, details, success, fails, failedDps);
		
		if(defs==null) {
			responseObserver.onNext(EMPTY_PUT_RESPONSE);
			responseObserver.onCompleted();
		} else {
			Deferred.group(defs).addCallbacks(
				new Callback<Void, ArrayList<Object>>() {
					@Override
					public Void call(ArrayList<Object> arg) throws Exception {
						long elapsed = System.currentTimeMillis() - startTime;
						LOG.info("DataPoints Put Complete: size={}, elapsed={}, s={}, f={}", size, elapsed, success.intValue(), fails.intValue());
						PutDatapointsResponse.Builder rb = PutDatapointsResponse.newBuilder()
								.setFailed(fails.intValue())
								.setSuccess(success.intValue());
						if(details && !failedDps.isEmpty()) {
							LOG.info("Building DataPoint Errors: {}", failedDps.size());
							for(Map.Entry<DataPoint, String> fentry : failedDps.entrySet()) {
								rb.addErrors(PutDatapointError.newBuilder()
									.setDataPoint(fentry.getKey())
									.setError(fentry.getValue())
									.build()
								);
							}
						}
						responseObserver.onNext(rb.build());
						responseObserver.onCompleted();
						return null;
					}
				}, 
				new Callback<Void, Exception>() {
					@Override
					public Void call(Exception ex) throws Exception {
						LOG.error("DataPoint Put Failed", ex);
						responseObserver.onError(ex);
						return null;
					}
				}
			);
		}
		LOG.info("Submitted DataPoints in {} ms", System.currentTimeMillis() - startTime);
	}
	
	private List<Deferred<Object>> putDps(List<DataPoint> dps, boolean noresp, boolean details, LongAdder success, LongAdder fails, Map<DataPoint, String> failedDps) {
		List<Deferred<Object>> defs = noresp ? null : new ArrayList<>(dps.size());
		Deferred<Object> def = null;
		for(DataPoint dp: dps) {
			double dval = dp.getValue();
			long lval = (long)dval;
			double fpart = dval - lval;
			boolean isDouble = fpart==0D;
			try {
				if(dp.hasAggregation()) {
					Aggregation aggr = dp.getAggregation();
					String rollup = aggr.getRollup()==null ? null : aggr.getRollup().name();
					String groupBy = aggr.getGrouping()==null ? null : aggr.getGrouping().name();
					if(isDouble) {
						def = tsdb.addAggregatePoint(dp.getMetric(), dp.getTimestamp(), dval, dp.getMetricTags().getTagsMap(), 
								aggr.getGroupBy(), RelTime.from(aggr.getInterval()).toString(), 
								rollup, groupBy);
					} else {
						def = tsdb.addAggregatePoint(dp.getMetric(), dp.getTimestamp(), lval, dp.getMetricTags().getTagsMap(), 
								aggr.getGroupBy(), RelTime.from(aggr.getInterval()).toString(), 
								rollup, groupBy);					
					}
				} else {
					if(isDouble) {
						def = tsdb.addPoint(dp.getMetric(), dp.getTimestamp(), dval, dp.getMetricTags().getTagsMap());
					} else {
						def = tsdb.addPoint(dp.getMetric(), dp.getTimestamp(), lval, dp.getMetricTags().getTagsMap());
					}
				}
				def.addCallbacks(
						new Callback<Void, Object>() {
							@Override
							public Void call(Object nothing) throws Exception {
								success.increment();
								return null;
							}
						},
						new Callback<Void, Exception>() {
							@Override
							public Void call(Exception ex) throws Exception {
								fails.increment();
								if(details) {
									failedDps.put(dp, ex.getMessage());
								}
								return null;
							}
						}
				);
				if(!noresp) {
					defs.add(def);
				}
			} catch (Exception ex) {
				def = null;
				fails.increment();
				if(details) {
					failedDps.put(dp, ex.getMessage());
				}
			}
		}
		return defs;
	}
	
	
}
