/**
 * 
 */
package net.opentsdb.grpc.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;
import com.stumbleupon.async.Deferred;

import io.grpc.stub.StreamObserver;
import net.opentsdb.core.Aggregators;
import net.opentsdb.core.DataPoint;
import net.opentsdb.core.Query;
import net.opentsdb.core.SeekableView;
import net.opentsdb.core.TSDB;
import net.opentsdb.grpc.AggregatorName;
import net.opentsdb.grpc.AggregatorNames;
import net.opentsdb.grpc.Assignment;
import net.opentsdb.grpc.Content;
import net.opentsdb.grpc.ContentName;
import net.opentsdb.grpc.Count;
import net.opentsdb.grpc.CreateAnnotationResponse;
import net.opentsdb.grpc.DPoint;
import net.opentsdb.grpc.DataPointQuery;
import net.opentsdb.grpc.Empty;
import net.opentsdb.grpc.FQMetric;
import net.opentsdb.grpc.FQMetricQuery;
import net.opentsdb.grpc.FQMetrics;
import net.opentsdb.grpc.FilterMeta;
import net.opentsdb.grpc.FilterMetas;
import net.opentsdb.grpc.KeyValues;
import net.opentsdb.grpc.MetricTags;
import net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceImplBase;
import net.opentsdb.grpc.Ping;
import net.opentsdb.grpc.Pong;
import net.opentsdb.grpc.PutDatapoints;
import net.opentsdb.grpc.PutDatapointsResponse;
import net.opentsdb.grpc.QueryResponse;
import net.opentsdb.grpc.Reassignment;
import net.opentsdb.grpc.TSDBAnnotation;
import net.opentsdb.grpc.TSDBAnnotations;
import net.opentsdb.grpc.Tsuid;
import net.opentsdb.grpc.Tsuids;
import net.opentsdb.grpc.Uid;
import net.opentsdb.grpc.server.handlers.AnnotationStreamHandler;
import net.opentsdb.grpc.server.handlers.DataPointStreamHandler;
import net.opentsdb.grpc.server.handlers.MetricLookupHandler;
import net.opentsdb.meta.Annotation;
import net.opentsdb.plugin.common.Configuration;
import net.opentsdb.query.filter.TagVFilter;
import net.opentsdb.stats.StatsCollector;

/**
 * <p>Title: OpenTSDBServer</p>
 * <p>Description: The gRPC server implementation</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.OpenTSDBServer</code></p>
 */
public class OpenTSDBServer extends OpenTSDBServiceImplBase {
	private static final Logger LOG = LoggerFactory.getLogger(OpenTSDBServer.class);
	private static final Pattern SLASH_SPLITTER = Pattern.compile("/");
	private static final String host = ManagementFactory.getRuntimeMXBean().getName().split("@")[1];
	private final TSDB tsdb;
	private final Configuration cfg;
	private final File staticDir;
	private final Path staticPath;
	private final AggregatorNames aggrNames;
	private final DataPointStreamHandler putHandler;
	private final AnnotationStreamHandler annHandler;
	private final MetricLookupHandler metricLookupHandler;
	private final String serverId; 
	


	/**
	 * Creates a new OpenTSDBServer
	 * @param tsdb The TSDB instance
	 * @param cfg The configuration manager
	 */
	public OpenTSDBServer(TSDB tsdb, Configuration cfg) {
		this.tsdb = tsdb;
		this.cfg = cfg;
		staticDir = new File(cfg.config("tsd.http.staticroot", System.getProperty("java.io.tmpdir") + File.separator + "opentsdb" + File.separator + "static"));
		staticPath = staticDir.toPath();
		aggrNames = buildAggregatorNames();
		putHandler = new DataPointStreamHandler(tsdb, cfg);
		annHandler = new AnnotationStreamHandler(tsdb, cfg);
		metricLookupHandler = new MetricLookupHandler(tsdb, cfg);
		serverId = host + ":" + cfg.config(Configuration.GRPC_PORT, -1);
	}

	private AggregatorNames buildAggregatorNames() {
		AggregatorNames.Builder aggrBuilder = AggregatorNames.newBuilder();
		for(String s : Aggregators.set()) {
			aggrBuilder.addAggregatorName(AggregatorName.newBuilder().setName(s).build());
		}
		return aggrBuilder.build();

	}
	
	public void collectStats(StatsCollector collector) {
		putHandler.collectStats(collector);
	}
	
	@Override
	public void ping(Ping ping, StreamObserver<Pong> responseObserver) {
		long receivedTime = System.currentTimeMillis();
		LOG.info("Ping: {}", ping);		
		long sendElapsed = receivedTime - ping.getSendTime();
		responseObserver.onNext(Pong.newBuilder()
				.setHost(serverId)
				.setMsg(ping.getMsg() + "-->pong")
				.setReceiveTime(receivedTime)
				.setSendElapsedTime(sendElapsed)
				.build()
		);
		responseObserver.onCompleted();
	}


	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceImplBase#s(net.opentsdb.grpc.ContentName, io.grpc.stub.StreamObserver)
	 */
	@Override
	public void s(ContentName request, StreamObserver<Content> responseObserver) {
		LOG.debug("staticContent: {}", request.getName());
		Path p = Paths.get(staticDir.getAbsolutePath(), SLASH_SPLITTER.split(request.getName()));
		if(p.toFile().isFile()) {
			try {
				responseObserver.onNext(Content.newBuilder()
						.setContent(ByteString.copyFrom(Files.readAllBytes(p)))
						.build()
						);
				responseObserver.onCompleted();
			} catch (Exception ex) {
				LOG.error("Failed to read file: f={}", request.getName(), ex);
				responseObserver.onError(ex);
			}
		} else {
			responseObserver.onError(new FileNotFoundException(request.getName()));			
		}
		super.s(request, responseObserver);
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceImplBase#getAggregators(net.opentsdb.grpc.Empty, io.grpc.stub.StreamObserver)
	 */
	@Override
	public void getAggregators(Empty request, StreamObserver<AggregatorNames> responseObserver) {
		LOG.debug("getAggregators");
		responseObserver.onNext(aggrNames);
		responseObserver.onCompleted();
	}
	
	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceImplBase#executeQuery(net.opentsdb.grpc.DataPointQuery, io.grpc.stub.StreamObserver)
	 */
	@Override
	public void executeQuery(DataPointQuery request, StreamObserver<QueryResponse> responseObserver) {
		LOG.info("Executing Query: {}", request);
		final Query q = ProtoConverters.from(request, tsdb.newQuery());
		List<Deferred<Void>> allDefs = Collections.synchronizedList(new ArrayList<>());
		allDefs.add(SuAsyncHelpers.singleTBoth(q.runAsync(), (dps, err) -> { 
			if(err != null) {
				LOG.error("Query Execution Error: {}", request, err);
				responseObserver.onError(new Exception(err.getMessage()));
				responseObserver.onCompleted();
			} else {
				LOG.info("Processing Internal Responses: {}", dps.length);
				final AtomicInteger cnt =  new AtomicInteger();
				Arrays.stream(dps).forEach(dp -> {					
					final int idx =  cnt.incrementAndGet();
					final QueryResponse.Builder builder = QueryResponse.newBuilder();
					
					List<Annotation> annots = dp.getAnnotations();
					if(annots != null && !annots.isEmpty()) {
						List<TSDBAnnotation> anns = annots.stream()
							.map(a -> ProtoConverters.from(a))
							.collect(Collectors.toList());
						builder.setAnnotations(TSDBAnnotations.newBuilder().addAllAnnotations(anns).build());						
					}				
					
					builder.setQueryIndex(dp.getQueryIndex());										
					builder.setTsuids(Tsuids.newBuilder().addAllTsuids(
							dp.getTSUIDs().stream()
								.map(s -> Tsuid.newBuilder().setTsuidName(s).build())
								.collect(Collectors.toList())
					).build());
					try {
						SeekableView view = dp.iterator();
						while(view.hasNext()) {
							DataPoint d = view.next();
							builder.addDataPoints(DPoint.newBuilder().setValue(d.toDouble()).setTimestamp(d.timestamp()).build());
						}
					} catch (Exception ex) {
						ex.printStackTrace(System.err);
					}
					
					final AtomicReference<Map<String, String>> tags = new AtomicReference<>(null);
					final AtomicReference<List<String>> atags = new AtomicReference<>(null);
					final AtomicReference<String> metric = new AtomicReference<>(null);
					
					allDefs.add(SuAsyncHelpers.group((o, t) -> {
						if(t != null) {
							LOG.error("Failed to get async query values", t);
							responseObserver.onError(new Exception(t.getMessage()));
							responseObserver.onCompleted();
						} else {
							builder.setTags(MetricTags.newBuilder().putAllTags(tags.get()));
							builder.setMetric(metric.get());
							builder.addAllAggregatedTags(atags.get());
							
							try {
								responseObserver.onNext(builder.build());
								LOG.info("Sent Query Response: qindex={}, dp={}, size={}, aggsize={}", dp.getQueryIndex(), idx, dp.size(), dp.aggregatedSize());
							} catch (Exception ex) {
								LOG.error("Failed to send dp", ex);
								responseObserver.onError(new Exception(ex.getMessage()));
								responseObserver.onCompleted();
								
							}
						}
					},
							dp.getTagsAsync().addCallback((t) -> {
								tags.set(t); return null;
							}),
							dp.metricNameAsync().addCallback((s) -> { 
								metric.set(s); return null;
							}),
							dp.getAggregatedTagsAsync().addCallback((s) -> { 
								atags.set(s); return null;
							})
					));
				});
			}
		}));
		SuAsyncHelpers.singleTBoth(Deferred.group(allDefs), (v,t) -> {
			if(t!=null) {
				LOG.error("Failed on group all defs", t);
				responseObserver.onError(new Exception(t.getMessage()));
				responseObserver.onCompleted();
			} else {
				responseObserver.onCompleted();
			}
		});
		
	}
	
	
	
//	message DPoint {
//		double value = 1;
//		int64 timestamp = 2;
//	}
//
//	message QueryResponse {  // Represents a net.opentsdb.core.DataPoints[]
//		repeated string aggregatedTags = 1;
//		TSDBAnnotations annotations = 2;
//		int32 queryIndex = 3;
//		string metric = 4;
//		MetricTags tags = 5;
//		Tsuids tsuids = 6;
//		repeated DPoint dataPoints = 7;
//	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceImplBase#createAnnotations(io.grpc.stub.StreamObserver)
	 */
	@Override
	public StreamObserver<TSDBAnnotations> createAnnotations(StreamObserver<CreateAnnotationResponse> responseObserver) {
		return annHandler.createAnnotations(responseObserver);
	}

//	/**
//	 * {@inheritDoc}
//	 * @see net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceImplBase#updateAnnotations(io.grpc.stub.StreamObserver)
//	 */
//	@Override
//	public StreamObserver<TSDBAnnotation> updateAnnotations(StreamObserver<TSDBAnnotation> responseObserver) {
//		return annHandler.updateAnnotations(responseObserver);
//	}
	
	

//	/**
//	 * {@inheritDoc}
//	 * @see net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceImplBase#deleteAnnotations(net.opentsdb.grpc.TSDBAnnotation, io.grpc.stub.StreamObserver)
//	 */
//	@Override
//	public void deleteAnnotations(TSDBAnnotation request, StreamObserver<TSDBAnnotation> responseObserver) {
//		annHandler.deleteAnnotations(request, responseObserver);
//	}
//
//	/**
//	 * {@inheritDoc}
//	 * @see net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceImplBase#bulkDeleteAnnotations(net.opentsdb.grpc.BulkAnnotationRequest, io.grpc.stub.StreamObserver)
//	 */
//	@Override
//	public void bulkDeleteAnnotations(BulkAnnotationRequest request,
//			StreamObserver<BulkAnnotationResponse> responseObserver) {
//		annHandler.bulkDeleteAnnotations(request, responseObserver);
//	}

	@Override
	public void getConfiguration(Empty request, StreamObserver<KeyValues> responseObserver) {
		// TODO Auto-generated method stub
		super.getConfiguration(request, responseObserver);
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceImplBase#getFilterMetas(net.opentsdb.grpc.Empty, io.grpc.stub.StreamObserver)
	 */
	@Override
	public void getFilterMetas(Empty request, StreamObserver<FilterMetas> responseObserver) {
		LOG.debug("getFilterMetas");
		// public static Map<String, Map<String, String>> loadedFilters()
		Map<String, Map<String, String>> meta = TagVFilter.loadedFilters();
		Map<String, FilterMeta> filterMetas = new HashMap<>(meta.size());
		for(Map.Entry<String, Map<String, String>> entry: meta.entrySet()) {
			String name = entry.getKey();
			String desc = entry.getValue().get("description");
			String examp = entry.getValue().get("examples");
			filterMetas.put(name, FilterMeta.newBuilder().setDescription(desc).setExamples(examp).build());
		}
		responseObserver.onNext(
			FilterMetas.newBuilder().putAllFilters(filterMetas).build()
		);
		responseObserver.onCompleted();
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceImplBase#dropCaches(net.opentsdb.grpc.Empty, io.grpc.stub.StreamObserver)
	 */
	@Override
	public void dropCaches(Empty request, StreamObserver<Empty> responseObserver) {
		LOG.debug("dropCaches");
		tsdb.dropCaches();
		responseObserver.onNext(Empty.getDefaultInstance());
		responseObserver.onCompleted();
	}
	
	

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceImplBase#put(net.opentsdb.grpc.PutDatapoints, io.grpc.stub.StreamObserver)
	 */
	@Override
	public void put(PutDatapoints request, StreamObserver<PutDatapointsResponse> responseObserver) {
		//putHandler.put(request, responseObserver);		
	}
	
	
	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceImplBase#puts(io.grpc.stub.StreamObserver)
	 */
	@Override
	public StreamObserver<PutDatapoints> puts(StreamObserver<PutDatapointsResponse> responseObserver) {
		return putHandler.puts(responseObserver);
	}
	
	@Override
	public void metricsLookup(FQMetricQuery request, StreamObserver<FQMetric> responseObserver) {		
		metricLookupHandler.metricsLookup(request, responseObserver);
	}

	

	@Override
	public void assignUid(Assignment request, StreamObserver<Uid> responseObserver) {
		// TODO Auto-generated method stub
		super.assignUid(request, responseObserver);
	}

	@Override
	public StreamObserver<Assignment> assignUids(StreamObserver<Uid> responseObserver) {
		// TODO Auto-generated method stub
		return super.assignUids(responseObserver);
	}

	@Override
	public void deleteUid(Assignment request, StreamObserver<Empty> responseObserver) {
		// TODO Auto-generated method stub
		super.deleteUid(request, responseObserver);
	}

	@Override
	public StreamObserver<Assignment> deleteUids(StreamObserver<Count> responseObserver) {
		// TODO Auto-generated method stub
		return super.deleteUids(responseObserver);
	}

	@Override
	public void renameUid(Reassignment request, StreamObserver<Empty> responseObserver) {
		// TODO Auto-generated method stub
		super.renameUid(request, responseObserver);
	}

	@Override
	public StreamObserver<Reassignment> renameUids(StreamObserver<Count> responseObserver) {
		// TODO Auto-generated method stub
		return super.renameUids(responseObserver);
	}


}
