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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import com.stumbleupon.async.Callback;
import com.stumbleupon.async.Deferred;

import io.grpc.stub.ClientCallStreamObserver;
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
import net.opentsdb.stats.StatsCollector;

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
	
	protected final AtomicInteger activeStreams = new AtomicInteger();
	protected final LongAdder receivedDataPoints = new LongAdder();
	protected final LongAdder okDataPoints = new LongAdder();
	protected final LongAdder failedDataPoints = new LongAdder();
	
	public int getActiveStreams() {
		return activeStreams.get();
	}
	
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
	public void collectStats(StatsCollector collector) {
		try {
			collector.addExtraTag("grpchandler", name);
			collector.record("grpc.dpoints.activestreams", activeStreams.get());
			collector.record("grpc.dpoints.rcvdp", receivedDataPoints.longValue());
			collector.record("grpc.dpoints.okdp", okDataPoints.longValue());
			collector.record("grpc.dpoints.faileddp", failedDataPoints.longValue());
		} finally {
			collector.clearExtraTag("grpchandler");
		}
		
	}

	/**
	 * Creates a new DataPointPutHandler
	 * @param tsdb The parent TSDB instance
	 * @param cfg The extended configuration instance
	 */
	public DataPointPutHandler(TSDB tsdb, Configuration cfg) {
		super(tsdb, cfg);
	}
	
	public StreamObserver<DataPoint> puts(StreamObserver<PutDatapointsResponse> responseObserver) {
		LOG.info("Streaming DataPoints Initiated");
		final AtomicBoolean open = new AtomicBoolean(true);
		activeStreams.incrementAndGet();
		return new ClientCallStreamObserver<DataPoint>() {			
			final long startTime = System.currentTimeMillis();
			final LongAdder _receivedDataPoints = new AccumulatingLongAdder(receivedDataPoints);
			final LongAdder _okDataPoints = new AccumulatingLongAdder(okDataPoints);
			final LongAdder _failedDataPoints = new AccumulatingLongAdder(failedDataPoints);
			
			@Override
			public void onNext(DataPoint dp) {
				_receivedDataPoints.increment();
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
					SuAsyncHelpers.singleTCallback(def, 
							obj -> {
								_okDataPoints.increment();
								responseObserver.onNext(PutDatapointsResponse.newBuilder().setSuccess(1).build());
							}, 
							err -> {
								responseObserver.onNext(response(dp, err));
								_failedDataPoints.increment();
								LOG.warn("DataPoint put failed: {}", dp, err);
							}
					);
				} catch (Exception err) {
					responseObserver.onNext(response(dp, err));
					_failedDataPoints.increment();					
					LOG.warn("Failed to handle DataPoint: {}", dp, err);
				}
			}

			@Override
			public void onError(Throwable t) {
				LOG.error("Streaming DataPoints Error", t);
				LOG.info("Streaming DataPoints Progress: dps={}, elapsed={}", _okDataPoints.longValue(), System.currentTimeMillis()-startTime);				
				if(open.compareAndSet(true, false)) {
					activeStreams.decrementAndGet();
				}
			}

			@Override
			public void onCompleted() {
				try {
					LOG.info("Streaming DataPoints Complete: dps={}, elapsed={}", _okDataPoints.longValue(), System.currentTimeMillis()-startTime);
					responseObserver.onNext(response(_okDataPoints.longValue(), _failedDataPoints.longValue()));
					responseObserver.onCompleted();
				} finally {
					if(open.compareAndSet(true, false)) {
						activeStreams.decrementAndGet();
					}
				}
			}

			@Override
			public void cancel(String message, Throwable cause) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public boolean isReady() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void setOnReadyHandler(Runnable onReadyHandler) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void disableAutoInboundFlowControl() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void request(int count) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setMessageCompression(boolean enable) {
				// TODO Auto-generated method stub
				
			}

			
		};
		
	}
	
	/**
	 * Builds and returns a final PutDatapointsResponse
	 * @param ok The number of successful datapoints
	 * @param err The number of failed datapoints
	 * @return the final PutDatapointsResponse
	 */
	protected PutDatapointsResponse response(long ok, long err) {
		return PutDatapointsResponse.newBuilder()
				.setFailed(err)
				.setSuccess(ok)
				.setFinalResponse(true)
				.build();
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
		LongAdder success = new LongAdder();
		LongAdder fails = new LongAdder();
		Map<DataPoint, String> failedDps = details ? new ConcurrentHashMap<>() : null;
		
		List<DataPoint> dps = request.getDataPointsList();
		int size = dps.size();
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
