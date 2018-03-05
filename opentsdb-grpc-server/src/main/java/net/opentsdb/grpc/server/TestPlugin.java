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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.StreamObserver;
import net.opentsdb.grpc.AggregatorNames;
import net.opentsdb.grpc.DataPoint;
import net.opentsdb.grpc.Empty;
import net.opentsdb.grpc.MetricTags;
import net.opentsdb.grpc.OpenTSDBServiceGrpc;
import net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceStub;
import net.opentsdb.grpc.PutDatapoints;
import net.opentsdb.grpc.PutDatapointsResponse;
import net.opentsdb.grpc.PutOptions;

/**
 * <p>Title: TestPlugin</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.TestPlugin</code></p>
 */

public class TestPlugin {
	private static Logger LOG = LoggerFactory.getLogger(TestPlugin.class);
	private final OpenTSDBServiceStub stub;
	private final ManagedChannel channel;
	private static final AtomicBoolean running = new AtomicBoolean(true);

	public TestPlugin() {
		channel = ManagedChannelBuilder.forAddress("localhost", 4249)
				.usePlaintext(true)
				.build();
		stub = OpenTSDBServiceGrpc.newStub(channel);
		LOG.info("Stub Created");
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {		
		LOG.info("Testing gRPC Plugin");
		Logger log = LoggerFactory.getLogger("io.grpc");
		((ch.qos.logback.classic.Logger)log).setLevel(ch.qos.logback.classic.Level.DEBUG);
		TestPlugin tp = new TestPlugin();
		//tp.aggrs();
		//		tp.addDataPoints();
		tp.streamDataPoints2();
		try {
			System.in.read();
			LOG.info("Shutting Down...");
			running.set(false);
		} catch (Exception ex) {
			LOG.error("Main Error", ex);
		}
	}
	
	public void streamAnnotations() {
		
	}
	
	public void streamDataPoints2() {
		StreamingHelper<DataPoint,PutDatapointsResponse> stream = new StreamingHelper<>(channel, OpenTSDBServiceGrpc.getPutsMethod(), 
				r -> {
					if(r.getFinalResponse()) {
						LOG.info("Final: {}", r);
					}
				},
				str -> {
					LOG.info("Complete");
					LOG.info("FINAL STATS: {}", str.printStats());
				}
		);		
		Map<String, String> tags = new HashMap<>();
		tags.put("foo", "bar");
		MetricTags mtags = MetricTags.newBuilder().putAllTags(tags).build();
		stream.startStream();
		for(int i = 0; i < 10000; i++) {
			DataPoint dp = DataPoint.newBuilder()
					.setMetric(i==300 ? ("xxx" + i) : ("xxx" + i))
					.setMetricTags(mtags)
					.setTimestamp(System.currentTimeMillis())
					.setValue(i * 13)
					.build();
			stream.blockingSend(dp);
			if(i > 0 && i%1000==0) {
				LOG.info("Streamer Stats: \n{}", stream.printStats());
			}
		}
		stream.halfClose();

	}

	public void streamDataPoints() {
		Map<String, String> tags = new HashMap<>();
		tags.put("foo", "bar");
		MetricTags mtags = MetricTags.newBuilder().putAllTags(tags).build();
		final CountDownLatch latch = new CountDownLatch(1);
		final LongAdder dpCount = new LongAdder();
		final LongAdder dpFailed = new LongAdder();
		final LongAdder dpSent = new LongAdder();		
		final AtomicInteger inFlight = new AtomicInteger();
		final long startTime = System.currentTimeMillis();

		StreamObserver<DataPoint> stream = stub
				.withCompression("gzip")
				.puts(new StreamObserver<PutDatapointsResponse>() {

					@Override
					public void onNext(PutDatapointsResponse pdr) {
						//LOG.info("Puts Response: {}", pdr);

						if(pdr.getFinalResponse()) {
							LOG.info("DataPoint Stream Closing: {}", pdr);
							latch.countDown();
						} else {
							dpFailed.add(pdr.getFailed());
							dpCount.add(pdr.getSuccess());
						}
						inFlight.decrementAndGet();
					}

					@Override
					public void onError(Throwable t) {		try {
						LOG.info("Waiting for clean stream close");
						if(!latch.await(10, TimeUnit.SECONDS)) {
							LOG.error("Timed out waiting for clean stream completion");
						}
					} catch(Exception ex) {
						LOG.error("Interrupted while waiting for clean stream completion", ex);
					}

					dpCount.increment();
					LOG.warn("Puts Error", t);
					latch.countDown();
					}

					@Override
					public void onCompleted() {
						LOG.info("Done: elapsed:{}, sent={}, success={}, failed={}", 
								System.currentTimeMillis()-startTime, 
								dpSent.longValue(), dpCount.longValue(), 
								dpFailed.longValue()
								);
					}

				});
		ClientCallStreamObserver<DataPoint> so =
				(ClientCallStreamObserver<DataPoint>) stream;	
		while(running.get()) {
			for(int i = 0; i < 10000; i++) {
				so.onNext(DataPoint.newBuilder()
						.setMetric(i==300 ? ("x:xx" + i) : ("xxx" + i))
						.setMetricTags(mtags)
						.setTimestamp(System.currentTimeMillis())
						.setValue(i * 13)
						.build());
				dpSent.increment();
				inFlight.incrementAndGet();
				//			try {
				//				if(sending.get().await(1000, TimeUnit.MILLISECONDS)) {
				//					dpSent.increment();
				//				} else {			dpSent.increment();
				inFlight.incrementAndGet();

				//					LOG.info("Timeouts: t={}, sent={}", timeouts.incrementAndGet(), dpSent.longValue());
				//				}
				//			} catch (InterruptedException iex) {
				//				iex.printStackTrace(System.err);
				//			}
			}
			LOG.info("InFlight={}, sent={}, success={}, failed={}", inFlight.get(), dpSent.longValue(), dpCount.longValue(), dpFailed.longValue());
			try { Thread.sleep(2000); } catch (Exception x) {}
		}
		LOG.info("Waiting for {} in flight requests", inFlight.get());
		final long timeOutAt = System.currentTimeMillis() + 5000;
		while(inFlight.get() > 0) {
			try {
				Thread.currentThread().join(1);
				int pending = inFlight.get(); 
				if(pending < 1) {
					break;
				}
				if(System.currentTimeMillis() > timeOutAt) {
					LOG.error("Timedout waiting on {} pending in flight requests", pending);
				}
			} catch (InterruptedException ex) {
				LOG.error("Thread interrupted waiting on in flight requests");
			}
		}
		//		try {
		//			LOG.info("Waiting for clean stream close");
		//			if(!latch.await(10, TimeUnit.SECONDS)) {
		//				LOG.error("Timed out waiting for clean stream completion");
		//			}
		//		} catch(Exception ex) {
		//			LOG.error("Interrupted while waiting for clean stream completion", ex);
		//		}
		so.onCompleted();
		LOG.info("Done");
	}

	public void addDataPoints() {
		// put(PutDatapoints request, StreamObserver<PutDatapointsResponse> responseObserver) 
		PutDatapoints.Builder pd = PutDatapoints.newBuilder();
		pd.setOptions(PutOptions.newBuilder().setDetails(true).build());
		Map<String, String> tags = new HashMap<>();
		tags.put("foo", "bar");
		MetricTags mtags = MetricTags.newBuilder().putAllTags(tags).build();
		while(true) {
			for(int i = 0; i < 10000; i++) {
				pd.addDataPoints(
						DataPoint.newBuilder()
						.setMetric("xx:x" + i)
						.setMetricTags(mtags)
						.setTimestamp(System.currentTimeMillis())
						.setValue(i * 13)
						.build()
						);
			}
			stub.put(pd.build(), new StreamObserver<PutDatapointsResponse>() {

				@Override
				public void onNext(PutDatapointsResponse value) {
					LOG.info("Response:{}\nSize: {} bytes", value.toString(), value.getSerializedSize());				
				}

				@Override
				public void onError(Throwable t) {
					LOG.error("PutDataPoints Failure", t);				
				}

				@Override
				public void onCompleted() {
					LOG.info("Done");				
				}

			});
			try { Thread.sleep(2000); } catch (Exception x ) {}
		}
	}

	//	message DataPoint {
	//		string metric = 1;
	//		MetricTags metricTags = 2;
	//		int64 timestamp = 3;
	//		double value = 4;
	//		Aggregation aggregation = 5;
	//	}
	//
	//	message PutOptions {
	//		oneof detail {
	//			bool summary = 1;
	//			bool details = 2;
	//		}
	//	}
	//
	//	message PutDatapoints {
	//		PutOptions options = 1;
	//		repeated DataPoint dataPoints = 2;
	//	}


	public void aggrs() {
		stub.getAggregators(Empty.getDefaultInstance(), new StreamObserver<AggregatorNames>() {

			@Override
			public void onNext(AggregatorNames ans) {
				ans.getAggregatorNameList().stream().forEach(an -> {
					LOG.info("AggregatorName: {}", an.getName());
				});				
			}

			@Override
			public void onError(Throwable t) {
				LOG.error("getAggregators Error", t);				
			}

			@Override
			public void onCompleted() {
				LOG.info("getAggregators Complete");				
			}

		});
	}

}
