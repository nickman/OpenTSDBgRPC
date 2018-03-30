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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.ObjectName;
import javax.xml.bind.DatatypeConverter;

import org.hbase.async.HBaseClient;
import org.hbase.async.KeyValue;
import org.hbase.async.Scanner;

import com.stumbleupon.async.Deferred;

import io.grpc.stub.StreamObserver;
import net.opentsdb.core.TSDB;
import net.opentsdb.grpc.FQMetric;
import net.opentsdb.grpc.FQMetricQuery;
import net.opentsdb.grpc.OpenTSDBServiceGrpc;
import net.opentsdb.grpc.server.StreamMappers;
import net.opentsdb.grpc.server.streaming.server.StreamerBuilder;
import net.opentsdb.grpc.server.streaming.server.StreamerContainer;
import net.opentsdb.grpc.server.streaming.server.StreamerContext;
import net.opentsdb.plugin.common.Configuration;
import net.opentsdb.search.SearchQuery;
import net.opentsdb.search.TimeSeriesLookup;
import net.opentsdb.stats.StatsCollector;
import net.opentsdb.uid.UniqueId;
import net.opentsdb.uid.UniqueId.UniqueIdType;
import net.opentsdb.utils.Pair;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * <p>Title: MetricLookupHandler</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.handlers.MetricLookupHandler</code></p>
 */

public class MetricLookupHandler extends AbstractHandler<FQMetricQuery, FQMetric> {
	private static final Charset CHARSET = Charset.forName("ISO-8859-1");
	private static final byte[] ID_FAMILY = "id".getBytes(CHARSET);
	private static final byte[] Q_METRIC = "metrics".getBytes(CHARSET);
	private static final byte[] Q_TAGK = "tagk".getBytes(CHARSET);
	private static final byte[] Q_TAGV = "tagv".getBytes(CHARSET);
	
	private final HBaseClient hbaseClient;
	
	protected final StreamerContainer<FQMetricQuery, FQMetric> metricLookupStreamContainer = 
			new StreamerContainer<>(new StreamerBuilder<>(OpenTSDBServiceGrpc.getMetricsLookupMethod(), this)
			);


	public MetricLookupHandler(TSDB tsdb, Configuration cfg) {
		super(tsdb, cfg);
		hbaseClient = tsdb.getClient();
	}
	
	public void metricsLookup(FQMetricQuery query, StreamObserver<FQMetric> responseObserver) {
		metricLookupStreamContainer.newServerStreamer(query, responseObserver).start();
		
	}

	
	@Override
	public Flux<FQMetric> invokeForFlux(FQMetricQuery t, StreamerContext sc) {		
		final ObjectName compiledQuery;
		final int max = t.getMaxValues() < 1 ? Integer.MAX_VALUE : t.getMaxValues();
		final boolean includeTsuids = t.getIncludeTsuids();
		try {
			compiledQuery = compileExpr(t);
		} catch (Exception ex) {
			LOG.error("Failed to compile FQMetricQuery: {}", t, ex);			
			return Flux.error(new Exception("Failed to compile FQMetricQuery: " + t, ex));
		}
		final boolean exactTags = compiledQuery.isPropertyListPattern();
		final AtomicReference<String> wcMetric = new AtomicReference<>();
		final AtomicReference<List<Entry<String, String>>> wcTags  = new AtomicReference<>();
		final SearchQuery searchQuery = buildSearchQuery(compiledQuery, wcMetric, wcTags);
		if(searchQuery==null) {
			LOG.error("FQMetricQuery is all wildcards [{}]", compiledQuery);
			return Flux.error(new Exception("FQMetricQuery is all wildcards [" + compiledQuery + "]"));
		}
		final TimeSeriesLookup lookup = new TimeSeriesLookup(tsdb, searchQuery);
		
		return Flux.create(sink -> {
			
			StreamMappers.monoFromDeferred(lookup.lookupAsync()).subscribe(
				byteArrs -> {
					Flux.fromStream(byteArrs.stream()).subscribe(
							tsuidBytes -> resolveSearchQuery(tsuidBytes)
								.subscribe(
										fq -> sink.next(fq),
										err -> sink.error(err)
								)
								
					);
				},
				err -> {
					LOG.error("SearchQuery Failure for [{}]", compiledQuery, err);
					sink.error(err);
				}
			);
		});
		
//		
//		
//		
//		final Flux<String> metricFlux = lookupNames(Q_METRIC, compiledQuery.getDomain(), max);
//		final Flux<String> tagKeyFlux = Flux.concat(
//				compiledQuery.getKeyPropertyList().keySet().stream()
//					.<Flux<String>>map(k -> lookupNames(Q_TAGK, k, max))
//					.collect(Collectors.toList())
//		);
//		final Flux<String> tagValueFlux = Flux.concat(
//				compiledQuery.getKeyPropertyList().values().stream()
//					.<Flux<String>>map(v -> lookupNames(Q_TAGV, v, max))
//					.collect(Collectors.toList())
//		);
//		
//		return Flux.create(sink -> {
////			Flux.concat(metricFlux, tagKeyFlux, tagValueFlux).parallel(3)
//			Flux.concat(metricFlux)
//			.doOnComplete(() -> {
//				processResults(metricFlux, tagKeyFlux, tagValueFlux);
//				LOG.info("MetricsLookup Complete");
//				sink.complete();
//			})
//			.doOnError(err -> {
//				LOG.error("Lookup FluxConcat Failure", err);
//				sink.error(err);
//			})
//			.subscribe();
//		});
	}
	
	private static class ResolvedSearchQuery {
		final Set<String> metrics = Collections.synchronizedSet(new LinkedHashSet<>());
		final Set<Map<String, String>> tags = Collections.synchronizedSet(new LinkedHashSet<>());
		
		
	}
	
	protected Mono<FQMetric> resolveSearchQuery(byte[] tsuid) {
		return Mono.create(sink -> {
			String tsuidStr = DatatypeConverter.printHexBinary(tsuid);
			List<byte[]> tagPairs = UniqueId.getTagsFromTSUID(tsuidStr);
			final FQMetric.Builder fqBuilder = FQMetric.newBuilder();
			byte[] metricUidBytes = Arrays.copyOfRange(tsuid, 0, 3);			
			Flux.concat(
					getUIDName(metricUidBytes, UniqueIdType.METRIC)
						.doOnSuccess(s -> fqBuilder.setMetric(s)),
					getTags(tagPairs)
						.doOnSuccess(m -> fqBuilder.putAllTags(m))
					
			)
			.doOnComplete(() -> sink.success(fqBuilder.build()))
			.doOnError(err -> sink.error(err));			
		});
	}
	
	protected Mono<String> getUIDName(byte[] uid, UniqueIdType uidType) {
		return StreamMappers.monoFromDeferred( tsdb.getUidName(uidType, uid));
	}
	
	protected Mono<Map<String, String>> getTags(List<byte[]> tagArray) {
		return Mono.create(sink -> {
			Iterator<byte[]> iter = tagArray.iterator();
			final Map<String, String> tags = new ConcurrentHashMap<>();
			List<Flux<String>> fluxes = new ArrayList<>();
			while(iter.hasNext()) {
				final String[] pair = new String[2];
				fluxes.add(Flux.<String>concat(
						getUIDName(iter.next(), UniqueIdType.TAGK)
							.doOnSuccess(s -> {pair[0] = s;}),
						getUIDName(iter.next(), UniqueIdType.TAGV)
							.doOnSuccess(s -> {pair[1] = s;})							
				).doOnComplete(() -> tags.put(pair[0], pair[1])));
			}
			Flux.concat(fluxes).subscribe(
					s -> {},
					err -> sink.error(err),
					() -> sink.success(tags)
			);
		});
	}

	
	/*
	 * SearchQuery:
	 * For metric and tags, split between wildcards and not
	 * 	SearchQuery runs with non-wc metric and any non-wc tags.
	 *  If none exist, it's an error. 
	 */
	
	protected SearchQuery buildSearchQuery(ObjectName compiledQuery, AtomicReference<String> wcMetric, AtomicReference<List<Entry<String, String>>> wcTags) {
		SearchQuery searchQuery = new SearchQuery();
		boolean hasNonWc = false;
		if(compiledQuery.getDomain().indexOf('*') == -1) {
			searchQuery.setMetric(compiledQuery.getDomain());
			hasNonWc = true;
		} else {
			wcMetric.set(compiledQuery.getDomain());
		}
		List<Pair<String, String>> tags = new ArrayList<>();
		List<Entry<String, String>> wtags = new ArrayList<>();
		for(Entry<String, String> es : compiledQuery.getKeyPropertyList().entrySet()) {
			String key = es.getKey();
			String value = es.getKey();
			if(key.indexOf('*')==-1 && value.indexOf('*')==-1) {
				tags.add(new Pair<String, String>(key, value));
				hasNonWc = true;
			} else {
				wtags.add(es);
			}			
		}
		if(!tags.isEmpty()) {
			searchQuery.setTags(tags);
		}
		if(!wtags.isEmpty()) {
			wcTags.set(wtags);
		}
		return hasNonWc ? null : searchQuery;
	}
	
	protected void processResults(Flux<String> metricFlux, Flux<String> tagKeyFlux, Flux<String> tagValueFlux) {
		LOG.info("\n\tMetric Lookup Results:\n\tMetrics:{}\n\tTag Keys:{}\n\tTag Values:{}",
				metricFlux.count().block(),
				tagKeyFlux.count().block(),
				tagValueFlux.count().block()
		);
	}
	
	protected Flux<String> lookupNames(byte[] qualifier, String regex, final int maxRows) {
		return Flux.create(sink -> {
			Scanner scanner = null;
			try {
				scanner = hbaseClient.newScanner(tsdb.uidTable());
				scanner.setFamily(ID_FAMILY);
				scanner.setQualifier(qualifier);
				scanner.setKeyRegexp(regex(regex));
				scanner.setMaxNumKeyValues(maxRows);
				final AtomicBoolean done = new AtomicBoolean(false);
				final AtomicInteger rowsSoFar = new AtomicInteger();
				Deferred<ArrayList<ArrayList<KeyValue>>> drows = null;
				while(!done.get() && rowsSoFar.get() <= maxRows && (drows = scanner.nextRows()) != null) {
					ArrayList<ArrayList<KeyValue>> k = drows.join();
					if(k != null && !k.isEmpty()) {
						k.stream().forEach(kvlist -> {
							kvlist.stream().forEach(kv -> {
								if(!done.get()) {
									sink.next(new String(kv.key()));
									rowsSoFar.incrementAndGet();
								}
							});
						});
					} else {
						done.set(true);
						sink.complete();
					}
					
//					SuAsyncHelpers.singleTBoth(drows, (k,t) -> {
//						if(done.get()) {
//							return;
//						}
//						if(t == null) {
//							if(k != null && !k.isEmpty()) {
//								k.stream().forEach(kvlist -> {
//									kvlist.stream().forEach(kv -> {
//										if(!done.get()) {
//											sink.next(new String(kv.key()));
//											rowsSoFar.incrementAndGet();
//										}
//									});
//								});
//							} else {
//								done.set(true);
//								sink.complete();
//							}
//						} else {
//							LOG.error("Scanner Error", t);
//							done.set(true);
//							sink.error(t);
//						}
//					});
				}
			} catch (Exception ex) {
				LOG.error("LookupName scanner exception", ex);
				sink.error(ex);
			} finally {
				if(scanner != null) {
					try { scanner.close(); } catch (Exception x) {}
				}
			}
		});
	}
	
	protected static String regex(String expr) {
		return expr
			.replace(".", "\\.")
			.replace("*", ".*");
	}
	
	protected static ObjectName compileExpr(FQMetricQuery query) throws Exception {
		return new ObjectName(query.getExpression());
	}

	@Override
	protected void doStats(StatsCollector collector) {
		
	}

}

/*
    m = UidManager.class.getDeclaredMethod("metaSync", TSDB.class);
    m.setAccessible(true);
//    cnt = m.invoke(null, tsdb);
//    println "CNT: $cnt";
    
    sq = new SearchQuery("sys.cpu");
    sq.setLimit(500);
    TimeSeriesLookup lookup = new TimeSeriesLookup(tsdb, sq);
    lookup.lookup().eachWithIndex() { t, i ->
        String tsuid = DatatypeConverter.printHexBinary(t);
        arrs = parts(tsdb, t);
        println "#$i: bytes: [${t.length}], parts:[$arrs]";
        byte[] tsuidBytes = [t[0], t[1], t[2]] as byte[];
        String name = getName(tsdb, tsuidBytes, UniqueIdType.METRIC);
//        println "Metric: $name";
        
    }
    

 */

/*
@Grab('org.json:json:20150729')
import net.opentsdb.core.*;
import net.opentsdb.utils.*;
import org.hbase.async.*;
import com.stumbleupon.async.*;
import org.json.*;
import java.nio.charset.*;

cfg = new JSONObject(new URL("http://localhost:10000/api/config").getText());
Properties p = new Properties();
cfg.keys().each() { k ->
    p.setProperty(k, cfg.get(k));
}
p.setProperty("tsd.startup.enable", "false");
p.setProperty("tsd.rpc.plugins", "");
File f = File.createTempFile("tsdbconfig", ".properties");
f.deleteOnExit();


p.each() { k,v ->
    f.append("$k:$v\n");
}

Config config = new Config(f.getAbsolutePath());
println "Config Loaded: ${p.size()}";
TSDB tsdb = null;
Scanner scanner = null;
final Charset CHARSET = Charset.forName("ISO-8859-1");
final byte[] ID_FAMILY = "id".getBytes(CHARSET);
final byte[] Q_METRIC = "metrics".getBytes(CHARSET);
final byte[] Q_TAGK = "tagk".getBytes(CHARSET);
final byte[] Q_TAGV = "tagv".getBytes(CHARSET);
try {
    tsdb = new TSDB(config);
    //tsdb.preFetchHBaseMeta();
    hbaseClient = tsdb.getClient();
    println "HBaseClient: $hbaseClient";
    scanner = hbaseClient.newScanner(tsdb.uidTable());
//    scanner.setKeyRegexp("sys\\..*");
//    scanner.setKeyRegexp("established");
    scanner.setKeyRegexp(".*68.*");
    scanner.setFamily(ID_FAMILY);
    scanner.setQualifier(Q_TAGV);
    Deferred<ArrayList<ArrayList<KeyValue>>> drows = null;
    int fetches = 0;
    long keys = 0;
    boolean done = false;
    long start = System.currentTimeMillis();
    while((drows = scanner.nextRows()) != null && done==false) {
        fetches++;        
        ArrayList<ArrayList<KeyValue>> values = drows.join();
        if(values != null && !values.isEmpty()) {
            values.each() { v -> 
                v.each() { kv ->
                    keys++;
                    String key = new String(kv.key());
                    String value = new String(kv.value());
                    String qual = new String(kv.qualifier());
                    //println "Key: [$key], Qual: [$qual]";
                }
            }
        } else {
            done = true;
        }
    }
    long elapsed = System.currentTimeMillis() - start;
    println "\n\t=============================\n\tFetches: $fetches, Keys: $keys, Elapsed: $elapsed ms.\n\t=============================";
} finally {
    try {
        println "Closing Scanner....";
        scanner.close().joinUninterruptibly();
        println "Scanner closed";
    } catch (x) {}

    try {
        println "Stopping TSDB....";
        tsdb.shutdown().joinUninterruptibly();
        println "TSDB Stopped";
    } catch (x) {}
}
return null;
*/