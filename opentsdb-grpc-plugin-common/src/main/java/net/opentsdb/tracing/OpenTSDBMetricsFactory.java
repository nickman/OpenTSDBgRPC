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
package net.opentsdb.tracing;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uber.jaeger.metrics.Counter;
import com.uber.jaeger.metrics.Gauge;
import com.uber.jaeger.metrics.MetricsFactory;
import com.uber.jaeger.metrics.Timer;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.stub.StreamObserver;
import net.opentsdb.grpc.DataPoint;
import net.opentsdb.grpc.MetricTags;
import net.opentsdb.grpc.OpenTSDBServiceGrpc;
import net.opentsdb.grpc.PutDatapoints;
import net.opentsdb.grpc.PutDatapointsResponse;

/**
 * <p>Title: OpenTSDBMetricsFactory</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.tracing.OpenTSDBMetricsFactory</code></p>
 */

public class OpenTSDBMetricsFactory implements MetricsFactory {
	protected final Logger LOG = LoggerFactory.getLogger(getClass());
	protected static final String HOST_NAME = ManagementFactory.getRuntimeMXBean().getName().split("@")[1];
	protected final LongAdder pointsProcessed = new LongAdder();
	protected final LongAdder pointsFailed = new LongAdder();
	protected final LongAdder pointsSent = new LongAdder();
	protected final String inProcessServer;
	protected final String componentName;
	protected final LocalDatapointStreamer streamer;
	protected final BlockingQueue<DataPoint> dpQueue;

	/**
	 * Creates a new server side OpenTSDBMetricsFactory
	 * @param inProcessServer
	 * @param componentName
	 */
	OpenTSDBMetricsFactory(String inProcessServer, String componentName) {
		this.inProcessServer = inProcessServer;
		this.componentName = componentName;
		streamer  = new LocalDatapointStreamer(null);
		dpQueue = null;
	}
	
	/**
	 * Creates a new client side OpenTSDBMetricsFactory
	 * @param inProcessServer
	 * @param componentName
	 * @param dpQueue
	 */
	OpenTSDBMetricsFactory(String inProcessServer, String componentName, BlockingQueue<DataPoint> dpQueue) {
		this.inProcessServer = inProcessServer;
		this.componentName = componentName;
		streamer  = new LocalDatapointStreamer(dpQueue);
		this.dpQueue = dpQueue;
	}
	
	
	
	private class LocalDatapointStreamer {
		protected ManagedChannel channel;
		protected OpenTSDBServiceGrpc.OpenTSDBServiceStub stub;
		protected StreamObserver<PutDatapointsResponse> responseObserver;
		protected final AtomicBoolean inited = new AtomicBoolean(false);
		protected final BlockingQueue<DataPoint> dpQueue;
		
		LocalDatapointStreamer(BlockingQueue<DataPoint> dpQueue) {
			this.dpQueue = dpQueue;
		}
		
		private void initialize() {
			if(inited.compareAndSet(false, true)) {
				if(dpQueue==null) {
					channel = InProcessChannelBuilder.forName(inProcessServer)
					.directExecutor()
					.build();
					stub = OpenTSDBServiceGrpc.newStub(channel);
					responseObserver = new StreamObserver<PutDatapointsResponse>() {
						@Override
						public void onNext(PutDatapointsResponse value) {
							pointsProcessed.add(value.getSuccess());
							pointsFailed.add(value.getFailed());
						}
			
						@Override
						public void onError(Throwable t) {
							//LOG.error("OpenTSDBMetricsFactory putDatapoints stream failure", t);
						}
			
						@Override
						public void onCompleted() {
							LOG.info("OpenTSDBMetricsFactory putDatapoints stream complete");
						}
					};
				}
						
			}
		}
		
		public void close() {
			if(channel!= null) {
				channel.shutdown();
			}
		}
		
		public void send(DataPoint... dps) {
			List<DataPoint> xps = new ArrayList<>(Arrays.asList(dps));
			initialize();
			if(dpQueue==null) {
				stub.put(PutDatapoints.newBuilder().addAllDataPoints(xps).setDetails(true).build(), 
						responseObserver);
			} else {
				dpQueue.addAll(Arrays.asList(dps));
				Collections.addAll(dpQueue, dps);
			}
		}

	}
	

	/**
	 * {@inheritDoc}
	 * @see com.uber.jaeger.metrics.MetricsFactory#createCounter(java.lang.String, java.util.Map)
	 */
	@Override
	public Counter createCounter(String name, Map<String, String> tags) {
		
		final String mname = name + tags;
		LOG.info("Requesting Counter: {}", mname);
		final DataPoint dpTemplate = DataPoint.newBuilder()
				.setMetric(name.replace(':', '.'))
				.setMetricTags(MetricTags.newBuilder().putAllTags(tags)
						.putTags("mtype", "counter")
						.putTags("app", componentName)
						.putTags("host", HOST_NAME)
				)
				.build();
		return new Counter() {			
			final AtomicLong counter = new AtomicLong(0);
			@Override
			public void inc(long delta) {
				long value = counter.addAndGet(delta);
				LOG.info("Counter metric: name={}, value={}, total={}", mname, delta, value);
				streamer.send(DataPoint.newBuilder(dpTemplate)
					.setTimestamp(System.currentTimeMillis())
					.setValue(value)
					.build());							
			}			
		};
	}

	/**
	 * {@inheritDoc}
	 * @see com.uber.jaeger.metrics.MetricsFactory#createTimer(java.lang.String, java.util.Map)
	 */
	@Override
	public Timer createTimer(String name, Map<String, String> tags) {
		final String mname = name + tags;
		LOG.info("Requesting Timer: {}", mname);
		final DataPoint dpTemplate = DataPoint.newBuilder()
				.setMetric(name.replace(':', '.'))
				.setMetricTags(
						MetricTags.newBuilder()
							.putAllTags(tags)
							.putTags("mtype", "timer")
							.putTags("unit", "micros")
							.putTags("app", componentName)
							.putTags("host", HOST_NAME)							
						)
				.build();
		return new Timer() {
			@Override
			public void durationMicros(long time) {
				LOG.info("Timer metric: name={}, value={}", mname, time);
				streamer.send(DataPoint.newBuilder(dpTemplate)
						.setTimestamp(System.currentTimeMillis())
						.setValue(time)
						.build());							
				
			}			
		};
	}

	/**
	 * {@inheritDoc}
	 * @see com.uber.jaeger.metrics.MetricsFactory#createGauge(java.lang.String, java.util.Map)
	 */
	@Override
	public Gauge createGauge(String name, Map<String, String> tags) {
		final String mname = name + tags;
		LOG.info("Requesting Gauge: {}", mname);
		final DataPoint dpTemplate = DataPoint.newBuilder()
				.setMetric(name.replace(':', '.'))
				.setMetricTags(
						MetricTags.newBuilder()
							.putAllTags(tags)
							.putTags("mtype", "gauge")
							.putTags("app", componentName)
							.putTags("host", HOST_NAME)
						)
				.build();
		return new Gauge() {
			@Override
			public void update(long amount) {
				LOG.info("Gauge metric: name={}, value={}", mname, amount);
				streamer.send(DataPoint.newBuilder(dpTemplate)
						.setTimestamp(System.currentTimeMillis())
						.setValue(amount)
						.build());							
				
			}
		};
	}

}
	
	
//	@Override
//	public Counter createCounter(String name, Map<String, String> tags) {	
//		final String mname = name + tags;
//		return new Counter() {
//			@Override
//			public void inc(long delta) {
//				LOG.info("DELTA: {} : {}", mname, delta);				
//			}
//		};
//	}
//	@Override
//	public Timer createTimer(String name, Map<String, String> tags) {
//		final String mname = name + tags;
//		return new Timer() {
//			@Override
//			public void durationMicros(long time) {
//				LOG.info("TIMER: {} : {}", mname, time);				
//			}
//		};
//	}
//	@Override
//	public Gauge createGauge(String name, Map<String, String> tags) {
//		final String mname = name + tags;
//		return new Gauge() {
//			@Override
//			public void update(long amount) {
//				LOG.info("GAUGE: {} : {}", mname, amount);					
//			}
//		};
//	}
	


/*
2018-03-31 19:32:25,537 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:traces{state=started, sampled=y}
2018-03-31 19:32:25,583 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:traces{state=started, sampled=n}
2018-03-31 19:32:25,583 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:traces{state=joined, sampled=y}
2018-03-31 19:32:25,583 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:traces{state=joined, sampled=n}
2018-03-31 19:32:25,584 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:started_spans{sampled=y}
2018-03-31 19:32:25,584 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:started_spans{sampled=n}
2018-03-31 19:32:25,584 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:finished_spans{}
2018-03-31 19:32:25,584 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:span_context_decoding_errors{}
2018-03-31 19:32:25,584 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:reporter_spans{result=ok}
2018-03-31 19:32:25,584 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:reporter_spans{result=err}
2018-03-31 19:32:25,584 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:reporter_spans{result=dropped}
2018-03-31 19:32:25,584 INFO  [main] OpenTSDBMetricsFactory: Requesting Gauge: jaeger:reporter_queue_length{}
2018-03-31 19:32:25,584 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:sampler_queries{result=ok}
2018-03-31 19:32:25,584 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:sampler_queries{result=err}
2018-03-31 19:32:25,584 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:sampler_updates{result=ok}
2018-03-31 19:32:25,584 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:sampler_updates{result=err}
2018-03-31 19:32:25,584 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:baggage_updates{result=ok}
2018-03-31 19:32:25,584 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:baggage_updates{result=err}
2018-03-31 19:32:25,584 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:baggage_truncations{}
2018-03-31 19:32:25,585 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:baggage_restrictions_updates{result=ok}
2018-03-31 19:32:25,585 INFO  [main] OpenTSDBMetricsFactory: Requesting Counter: jaeger:baggage_restrictions_updates{result=err}
*/