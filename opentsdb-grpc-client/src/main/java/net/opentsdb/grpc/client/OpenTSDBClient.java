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
package net.opentsdb.grpc.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import net.opentsdb.grpc.Aggregator;
import net.opentsdb.grpc.AggregatorNames;
import net.opentsdb.grpc.CreateAnnotationResponse;
import net.opentsdb.grpc.DataPointQuery;
import net.opentsdb.grpc.DateTime;
import net.opentsdb.grpc.Downsample;
import net.opentsdb.grpc.Empty;
import net.opentsdb.grpc.FQMetric;
import net.opentsdb.grpc.FQMetricQuery;
import net.opentsdb.grpc.MetricAndTags;
import net.opentsdb.grpc.MetricTags;
import net.opentsdb.grpc.OpenTSDBServiceGrpc;
import net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceBlockingStub;
import net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceStub;
import net.opentsdb.grpc.Ping;
import net.opentsdb.grpc.Pong;
import net.opentsdb.grpc.PutDatapoints;
import net.opentsdb.grpc.PutDatapointsResponse;
import net.opentsdb.grpc.QueryResponse;
import net.opentsdb.grpc.RelativeTime;
import net.opentsdb.grpc.TSDBAnnotations;
import net.opentsdb.grpc.client.streaming.BidiStreamer;
import net.opentsdb.grpc.client.streaming.ServerStreamer;
import net.opentsdb.grpc.client.streaming.StreamerBuilder;
import net.opentsdb.grpc.client.util.ClientConfiguration;
import net.opentsdb.grpc.common.StreamDescriptor;
import net.opentsdb.tracing.JaegerTracing;

/**
 * <p>Title: OpenTSDBClient</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.client.OpenTSDBClient</code></p>
 */

public class OpenTSDBClient implements Closeable {
	protected final Logger LOG = LoggerFactory.getLogger(getClass());
	protected final ClientConfiguration clientConfig;
	protected final String host;
	protected final int port;
	protected final AtomicBoolean open = new AtomicBoolean(false);
	protected ManagedChannel channel = null;
	protected OpenTSDBServiceStub stub;
	protected OpenTSDBServiceBlockingStub blockingStub = null;
	protected final AtomicReference<ConnectivityState> connState = new AtomicReference<>(ConnectivityState.IDLE); 


	/**
	 * Creates a new OpenTSDBClient using all default configuration
	 * @return a disconnected OpenTSDBClient
	 */
	public static OpenTSDBClient newInstance() {
		return new OpenTSDBClient(ClientConfiguration.clientConfiguration());
	}
	
	
	/**
	 * Creates a new OpenTSDBClient
	 * @param host The GRPC endpoint host
	 * @param port The GRPC endpoint port
	 * @return a disconnected OpenTSDBClient
	 */
	public static OpenTSDBClient newInstance(String host, int port) {
		return newInstance(new Properties(), host, port);
	}
	
	/**
	 * Creates a new OpenTSDBClient
	 * @param configProps Additional configuration properties for fine tuning the client
	 * @param host The GRPC endpoint hostreturn new OpenTSDBClient(ClientConfiguration.clientConfiguration(
	 * @param port The GRPC endpoint port
	 * @return a disconnected OpenTSDBClient
	 */
	public static OpenTSDBClient newInstance(Properties configProps, String host, int port) {
		return new OpenTSDBClient(ClientConfiguration.clientConfiguration(configProps, host, port));
	}
	
	/**
	 * Creates a new OpenTSDBClient
	 * @param resource A string pointing to configuration properties for fine tuning the client.
	 * The resource can represent a URL, a file or a class path resource containing properties.
	 * @param host The GRPC endpoint host
	 * @param port The GRPC endpoint port
	 * @return a disconnected OpenTSDBClient
	 */
	public static OpenTSDBClient newInstance(String resource, String host, int port) {
		return new OpenTSDBClient(ClientConfiguration.clientConfiguration(resource, host, port));
	}
	
	/**
	 * Creates a new OpenTSDBClient
	 * @param resource A string pointing to configuration properties for fine tuning the client.
	 * The resource can represent a URL, a file or a class path resource containing properties.
	 * @return a disconnected OpenTSDBClient
	 */
	public static OpenTSDBClient newInstance(String resource) {
		return new OpenTSDBClient(ClientConfiguration.clientConfiguration(resource));
	}
	
	/**
	 * Creates a new OpenTSDBClient
	 * @param configProps The client configuration properties
	 * @return a disconnected OpenTSDBClient
	 */
	public static OpenTSDBClient newInstance(Properties configProps) {
		return new OpenTSDBClient(ClientConfiguration.clientConfiguration(configProps));
	}
	

	/**
	 * Creates a new OpenTSDBClient
	 * @param clientConfig The client configuration
	 */
	private OpenTSDBClient(ClientConfiguration clientConfig) {
		this.clientConfig = clientConfig;
		host = clientConfig.getHost();
		port = clientConfig.getPort();		
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {		
		return new StringBuilder("OpenTSDBClient[host=")
			.append(host)
			.append(", port=").append(port)
			.append(", open=").append(open.get())
			.toString();
	}
	
	public void printConfig() {
		Map<String, String> c = clientConfig.activeConfig();
		StringBuilder b = new StringBuilder("\n\t=================================\n\tClient Configuration\n\t=================================");
		for(Map.Entry<String, String> entry: c.entrySet()) {
			b.append("\n\t").append(entry.getKey()).append(" : ").append(entry.getValue());
		}
		b.append("\n\t=================================");
		LOG.info(b.toString());
	}
	
	/**
	 * Connects the client if it is not connected
	 * @return this client
	 */
	public OpenTSDBClient open(ClientInterceptor...clientInterceptors) {
		if(open.compareAndSet(false, true)) {
			printConfig();
			channel = clientConfig.build();
			stub = OpenTSDBServiceGrpc.newStub(channel)
					.withInterceptors(JaegerTracing.getInstance().getClientTracingInterceptor())
					.withInterceptors(clientInterceptors);
			blockingStub = OpenTSDBServiceGrpc.newBlockingStub(channel)
					.withInterceptors(JaegerTracing.getInstance().getClientTracingInterceptor())
					.withInterceptors(clientInterceptors);
		}
		onStateChange();
//		LOG.info("AGGRS: {}", 
//				blockingStub.getAggregators(Empty.getDefaultInstance())
//		);
		try {
			Pong pong = ping(); //asyncPing().get();
			LOG.info("Pong: {}", pong);			
		} catch (Exception ex) {
			throw new IllegalStateException("Ping failed", ex);
		}
		return this;
	}
	
	protected void onStateChange() {
		ConnectivityState cs = channel.getState(true);
		ConnectivityState prior = connState.getAndSet(cs);
		if(cs != prior) {
			LOG.info("Connectivity State Change: {} --> {}", prior, cs);
		}
		channel.notifyWhenStateChanged(cs, () -> {
			onStateChange();
		});
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		if(open.compareAndSet(true, false)) {
			channel.shutdown();
			channel = null;
			stub = null;
			blockingStub = null;
		}
	}
	
	public boolean isConnected() {
		return connState.get()==ConnectivityState.READY;
	}
	
	public ConnectivityState connectivityState() {
		return connState.get();
	}
	
	protected void check() {
		if(!open.get()) {
			throw new IllegalStateException("OpenTSDBClient is not connected");
		}
	}
	
	public Pong ping() {
		check();
		return blockingStub.ping(Ping.newBuilder()
				.setMsg("ping")
				.setSendTime(System.currentTimeMillis())
				.build()
		);
	}
	
	public CompletableFuture<Pong> asyncPing() {
		check();
		final CompletableFuture<Pong> cf = new CompletableFuture<>(); 
		Ping ping = Ping.newBuilder()
				.setMsg("ping")
				.setSendTime(System.currentTimeMillis())
				.build();
		stub.ping(ping, new StreamObserver<Pong>() {

			@Override
			public void onNext(Pong pong) {
				cf.complete(pong);
			}

			@Override
			public void onError(Throwable t) {
				LOG.error("Async Ping Error", t);
			}

			@Override
			public void onCompleted() {
				LOG.info("Ping Complete");
			}
			
		});
		return cf;
	}
	
	
	public BidiStreamer<PutDatapoints,PutDatapointsResponse> putDatapoints() {
		return putDatapoints(CallOptions.DEFAULT);
	}
	
	public BidiStreamer<PutDatapoints,PutDatapointsResponse> putDatapoints(CallOptions callOptions) {
		return putDatapoints(callOptions, r ->  {});
	}	
	
	public BidiStreamer<PutDatapoints,PutDatapointsResponse> putDatapoints(Consumer<PutDatapointsResponse> outConsumer) {
		return putDatapoints(CallOptions.DEFAULT, outConsumer).onErrorAction((t,s) -> {
			LOG.error("BidiStreamer Error. Connectivity: {}", channel.getState(false), t);
		});
	}

	public BidiStreamer<PutDatapoints,PutDatapointsResponse> putDatapoints(CallOptions callOptions, Consumer<PutDatapointsResponse> outConsumer) {
		check();
		return StreamerBuilder.newBuilder(channel, OpenTSDBServiceGrpc.getPutsMethod(), outConsumer)
				.descriptor(StreamDescriptor.DatapointsDescriptor.INSTANCE)
				.callOptions(callOptions.withCompression("gzip"))
				.buildBidiStreamer();
	}
	
	public ServerStreamer<FQMetricQuery, FQMetric> metricsLookup(Consumer<FQMetric> outConsumer) {
		check();
		return StreamerBuilder.newBuilder(channel, OpenTSDBServiceGrpc.getMetricsLookupMethod(), outConsumer)
				.buildServerStreamer();
	}
	
	public String[] getAggregators() {
		AggregatorNames names = blockingStub.getAggregators(Empty.getDefaultInstance());
		return names.getAggregatorNameList().stream().map(ag -> ag.getName())
			.toArray(size -> new String[size]);
	}
	
	public BidiStreamer<TSDBAnnotations,CreateAnnotationResponse> createAnnotations(Consumer<CreateAnnotationResponse> outConsumer) {
		check();
		return StreamerBuilder.newBuilder(channel, OpenTSDBServiceGrpc.getCreateAnnotationsMethod(), outConsumer)
				.descriptor(StreamDescriptor.AnnotationsDescriptor.INSTANCE)
				.callOptions(CallOptions.DEFAULT.withCompression("gzip"))
				.buildBidiStreamer();
	}
	
	//public void executeQuery(DataPointQuery request, StreamObserver<QueryResponse> responseObserver) {
	
	public void executeQuery(DataPointQuery request, StreamObserver<QueryResponse> responseObserver) {
		stub.executeQuery(request, responseObserver);
	}
	

	/**
	 * @param args
	 */
	public static void mainX(String[] args) {
		
		OpenTSDBClient client = OpenTSDBClient.newInstance("localhost", 10000).open();
		final Logger log = client.LOG;
		log.info("Created OpenTSDBClient: {}", client);
		Map<String, String> tags = new HashMap<>();
//		tags.put("cpu", "0");
//		tags.put("type", "combined");
		final CountDownLatch latch = new CountDownLatch(1);
		DataPointQuery dpq = DataPointQuery.newBuilder()
			.setDownSample(Downsample.newBuilder().setAggregator(Aggregator.MAX).setRelTime(RelativeTime.newBuilder().setTime(1).setUnit("m").build()).build())
			.setAggregator(Aggregator.AVG)
			.setStartTime(DateTime.newBuilder().setRelTime(RelativeTime.newBuilder().setTime(1).setUnit("h").build()).build())
			.setEndTime(DateTime.newBuilder().setRelTime(RelativeTime.newBuilder().setTime(1).setUnit("m").build()).build())
			.setMetricAndTags(MetricAndTags.newBuilder()
				.setMetric("sys.net.iface")
				.setTags(MetricTags.newBuilder().putAllTags(tags).build())
				.build()
			)
			.build();
		final long start = System.currentTimeMillis();
		client.executeQuery(dpq, new StreamObserver<QueryResponse>() {

			@Override
			public void onNext(QueryResponse r) {
				log.info("Retrieved {} datapoints in {} ms.", r.getDataPointsCount(), System.currentTimeMillis() - start);
				log.info("{}", r);
				
			}

			@Override
			public void onError(Throwable t) {
				log.error("QueryExecution Error", t);
				latch.countDown();
			}

			@Override
			public void onCompleted() {
				log.info("QueryExecution Complete");
				latch.countDown();
			}
			
		});
		try {
			if(!latch.await(1000000, TimeUnit.MILLISECONDS)) {
				log.info("TIMEOUT");
			} else {
				log.info("OK");
			}
		} catch (Exception ex) {
			log.error("Interrupted", ex);
		}
//		try (BidiStreamer<TSDBAnnotations,CreateAnnotationResponse> streamer = client.createAnnotations(pdr -> {
//			log.info("RESPONSE: {}", pdr);
//		}).start()) {
//			tags = new HashMap<>();
//			tags.put("foo", "bar");
//			TSDBAnnotations.Builder builder = TSDBAnnotations.newBuilder().setDetails(true);
//			for(int i = 0; i < 3; i++) {
//				TSDBAnnotation t = TSDBAnnotation.newBuilder()
//					.setDescription("Hello World Description #" + i)
//					.setTsuid(Tsuid.newBuilder().setTsuidName("00049600000100001900000400001A00000900002600000A00001C").build())
////					.setEndTime(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1) + i)
//					.setStartTime(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5))
//					.setNotes("These are my notes #" + i)
//					.putAllCustom(tags)
//					.build();
//				builder.addAnnotations(t);
//			}
//			streamer.send(builder.build());
//			log.info("Sent...");
//			streamer.clientComplete();
//			streamer.waitForCompletion();
//		} catch (Exception e) {
//			log.error("Stream failed", e);
//		}
//		try (BidiStreamer<PutDatapoints,PutDatapointsResponse> streamer = client.putDatapoints(pdr -> {
//			log.info("RESPONSE: {}", pdr);
//		}).start()) {			
//			Map<String, String> tags = new HashMap<>();
//			tags.put("foo", "bar");
//			MetricTags mtags = MetricTags.newBuilder().putAllTags(tags).build();
//			PutDatapoints.Builder pdb = PutDatapoints.newBuilder().setDetails(true);
//			for(int i = 0; i < 100; i++) {
//				DataPoint dp = DataPoint.newBuilder()
////						.setMetric(i==30 ? ("x:xx" + i) : ("xxx" + i))
//						.setMetric(("xxx" + i))
//						.setMetricTags(mtags)
//						.setTimestamp(System.currentTimeMillis())
//						.setValue(i * 13)
//						.build();
//				pdb.addDataPoints(dp);
//				log.info("DP: {}", dp.toString());	
//				
//			}
//			streamer.send(pdb.build());
//			log.info("Sent...");
//			Thread.currentThread().join(10000);
//		} catch (Exception e) {
//			log.error("Stream failed", e);
//		}
		
	}

	public static void main(String[] args) {
		
		OpenTSDBClient client = OpenTSDBClient.newInstance("localhost", 10000).open();
		final Logger log = client.LOG;
		log.info("Created OpenTSDBClient: {}", client);
		final CountDownLatch latch = new CountDownLatch(1);
		ServerStreamer<FQMetricQuery, FQMetric> streamer = client.metricsLookup(fq -> {
			log.info("FQ: {}", fq);
		});
		streamer.start();
		streamer.send(FQMetricQuery.newBuilder().setExpression("sys.cpu:cpu=*").setMaxValues(100).build());
		try {
			latch.await();
			log.info("Done");
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}

	}
	
	
	protected class OpenTSDBClientInterceptor implements ClientInterceptor {

		/**
		 * {@inheritDoc}
		 * @see io.grpc.ClientInterceptor#interceptCall(io.grpc.MethodDescriptor, io.grpc.CallOptions, io.grpc.Channel)
		 */
		@Override
		public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
				CallOptions callOptions, Channel next) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
}
