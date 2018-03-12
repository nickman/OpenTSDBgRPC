/**
 * 
 */
package net.opentsdb.grpc.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.ByteString;

import io.grpc.stub.StreamObserver;
import net.opentsdb.core.Aggregators;
import net.opentsdb.core.TSDB;
import net.opentsdb.grpc.AggregatorName;
import net.opentsdb.grpc.AggregatorNames;
import net.opentsdb.grpc.AnnotationRequest;
import net.opentsdb.grpc.Assignment;
import net.opentsdb.grpc.BulkAnnotationRequest;
import net.opentsdb.grpc.BulkAnnotationResponse;
import net.opentsdb.grpc.Content;
import net.opentsdb.grpc.ContentName;
import net.opentsdb.grpc.Count;
import net.opentsdb.grpc.CreateAnnotationResponse;
import net.opentsdb.grpc.Empty;
import net.opentsdb.grpc.FilterMeta;
import net.opentsdb.grpc.FilterMetas;
import net.opentsdb.grpc.KeyValues;
import net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceImplBase;
import net.opentsdb.grpc.PutDatapoints;
import net.opentsdb.grpc.PutDatapointsResponse;
import net.opentsdb.grpc.Query;
import net.opentsdb.grpc.QueryResponse;
import net.opentsdb.grpc.Reassignment;
import net.opentsdb.grpc.SubQueryResponse;
import net.opentsdb.grpc.TSDBAnnotation;
import net.opentsdb.grpc.Uid;
import net.opentsdb.grpc.server.handlers.AnnotationHandler;
import net.opentsdb.grpc.server.handlers.DataPointPutHandler;
import net.opentsdb.grpc.server.handlers.DataPointStreamHandler;
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
	private final TSDB tsdb;
	private final Configuration cfg;
	private final File staticDir;
	private final Path staticPath;
	private final AggregatorNames aggrNames;
	private final DataPointStreamHandler putHandler;
	private final AnnotationHandler annHandler;


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
		annHandler = new AnnotationHandler(tsdb, cfg);
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
	 * @see net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceImplBase#getAnnotation(net.opentsdb.grpc.AnnotationRequest, io.grpc.stub.StreamObserver)
	 */
	@Override
	public void getAnnotation(AnnotationRequest request, StreamObserver<TSDBAnnotation> responseObserver) {
		annHandler.getAnnotation(request, responseObserver);
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceImplBase#createAnnotations(io.grpc.stub.StreamObserver)
	 */
	@Override
	public StreamObserver<TSDBAnnotation> createAnnotations(StreamObserver<CreateAnnotationResponse> responseObserver) {
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
	
	

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceImplBase#deleteAnnotations(net.opentsdb.grpc.TSDBAnnotation, io.grpc.stub.StreamObserver)
	 */
	@Override
	public void deleteAnnotations(TSDBAnnotation request, StreamObserver<TSDBAnnotation> responseObserver) {
		annHandler.deleteAnnotations(request, responseObserver);
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.grpc.OpenTSDBServiceGrpc.OpenTSDBServiceImplBase#bulkDeleteAnnotations(net.opentsdb.grpc.BulkAnnotationRequest, io.grpc.stub.StreamObserver)
	 */
	@Override
	public void bulkDeleteAnnotations(BulkAnnotationRequest request,
			StreamObserver<BulkAnnotationResponse> responseObserver) {
		annHandler.bulkDeleteAnnotations(request, responseObserver);
	}

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

	@Override
	public void executeQuery(Query request, StreamObserver<QueryResponse> responseObserver) {
		// TODO Auto-generated method stub
		super.executeQuery(request, responseObserver);
	}

	@Override
	public void executeQueries(Query request, StreamObserver<SubQueryResponse> responseObserver) {
		// TODO Auto-generated method stub
		super.executeQueries(request, responseObserver);
	}

}
