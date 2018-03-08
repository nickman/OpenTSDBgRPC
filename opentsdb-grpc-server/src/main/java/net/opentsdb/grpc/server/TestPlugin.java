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
import java.util.stream.IntStream;

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
	
	private static Thread main = null;

	public TestPlugin() {
		channel = ManagedChannelBuilder.forAddress("localhost", 10000)
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
//		Logger log = LoggerFactory.getLogger("io.grpc");
//		((ch.qos.logback.classic.Logger)log).setLevel(ch.qos.logback.classic.Level.DEBUG);
		
//		tp.aggrs();
//		tp.addDataPoints();
//		tp.streamDataPoints2();
		main = Thread.currentThread();
		IntStream.range(0, 1).parallel().forEach(idx -> {
			
			for(int i = 0; i < 200; i++) {
				TestPlugin tp = new TestPlugin();
				tp.streamer();
				try { Thread.sleep(1000); } catch (Exception x) {}
			}
		});
		
		try {
			try { Thread.currentThread().join(); } catch (Exception x) {}
			LOG.info("Shutting Down...");
			running.set(false);
		} catch (Exception ex) {
			LOG.error("Main Error", ex);
		}
	}
	
	public void streamAnnotations() {
		
	}
	
	public void streamer() {
		ManagedChannel mc = ManagedChannelBuilder.forAddress("localhost", 10000)
				.usePlaintext(true)				
				.build();
		try {
			BidiStreamer<PutDatapoints,PutDatapointsResponse> stream = new BidiStreamer<>(mc, OpenTSDBServiceGrpc.getPutsMethod(), 
					r -> {
						if(r.getFinalResponse()) {
							LOG.info("Final: {}", r);
						}
					},
					str -> {
						LOG.info("Complete");
						LOG.info("FINAL STATS: {}", str.printStats());
					},
					pdr -> {
						return pdr.getFinalResponse();
					},
					in -> in.getDataPointsCount(),
					out -> (int)(out.getFailed() + out.getSuccess())
			);		
			Map<String, String> tags = new HashMap<>();
			tags.put("foo", "bar");
			MetricTags mtags = MetricTags.newBuilder().putAllTags(tags).build();
			stream.startStream();		
			int total = 0;
			for(int x = 0; x < 10; x++) {
				PutDatapoints.Builder pdb = PutDatapoints.newBuilder();
				for(int i = 0; i < 10000; i++) {
					DataPoint dp = DataPoint.newBuilder()
							.setMetric(i==300 ? ("xxx" + i) : ("xxx" + i))
							.setMetricTags(mtags)
							.setTimestamp(System.currentTimeMillis())
							.setValue(i * 13)
							.build();
					pdb.addDataPoints(dp);
					total++;
				}
				stream.send(pdb.build());
			}
			stream.clientComplete();
			LOG.info("Sent {} Datapoints", total);
			if(stream.waitForCompletion(5, TimeUnit.SECONDS)) {
				LOG.info("Streamer Closed Successfully");
			} else {
				System.err.println("Streamer NOT Closed Successfully. Stats:" + stream.printStats());
			}
		} finally {
			try { mc.shutdown(); } catch (Exception x) {}
		}
		//main.interrupt();

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
						.setMetric("xxx" + i)
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
