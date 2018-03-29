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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.management.ObjectName;

import org.hbase.async.HBaseClient;
import org.hbase.async.KeyValue;
import org.hbase.async.Scanner;

import com.stumbleupon.async.Deferred;

import io.grpc.stub.StreamObserver;
import net.opentsdb.core.TSDB;
import net.opentsdb.grpc.FQMetricQuery;
import net.opentsdb.grpc.FQMetrics;
import net.opentsdb.grpc.OpenTSDBServiceGrpc;
import net.opentsdb.grpc.PutDatapoints;
import net.opentsdb.grpc.PutDatapointsResponse;
import net.opentsdb.grpc.server.SuAsyncHelpers;
import net.opentsdb.grpc.server.streaming.server.StreamerBuilder;
import net.opentsdb.grpc.server.streaming.server.StreamerContainer;
import net.opentsdb.grpc.server.streaming.server.StreamerContext;
import net.opentsdb.plugin.common.Configuration;
import net.opentsdb.stats.StatsCollector;
import reactor.core.publisher.Flux;

/**
 * <p>Title: MetricLookupHandler</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.handlers.MetricLookupHandler</code></p>
 */

public class MetricLookupHandler extends AbstractHandler<FQMetricQuery, FQMetrics> {
	private static final Charset CHARSET = Charset.forName("ISO-8859-1");
	private static final byte[] ID_FAMILY = "id".getBytes(CHARSET);
	private static final byte[] Q_METRIC = "metrics".getBytes(CHARSET);
	private static final byte[] Q_TAGK = "tagk".getBytes(CHARSET);
	private static final byte[] Q_TAGV = "tagv".getBytes(CHARSET);
	
	private final HBaseClient hbaseClient;
	
	protected final StreamerContainer<FQMetricQuery, FQMetrics> metricLookupStreamContainer = 
			new StreamerContainer<>(new StreamerBuilder<>(OpenTSDBServiceGrpc.getMetricsLookupMethod(), this)
				.subItemsIn(t -> 1)
				.subItemsOut(f -> f.getFqmetricsCount())
			);


	public MetricLookupHandler(TSDB tsdb, Configuration cfg) {
		super(tsdb, cfg);
		hbaseClient = tsdb.getClient();
	}
	
	public void metricsLookup(FQMetricQuery query, StreamObserver<FQMetrics> responseObserver) {
		metricLookupStreamContainer.newServerStreamer(query, responseObserver).start();
		
	}

	
	@Override
	public CompletableFuture<FQMetrics> invoke(FQMetricQuery t, StreamerContext sc) {
		final CompletableFuture<FQMetrics> cf = new CompletableFuture<FQMetrics>();
		final ObjectName compiledQuery;
		final int max = t.getMaxValues() < 1 ? Integer.MAX_VALUE : t.getMaxValues();
		final boolean includeTsuids = t.getIncludeTsuids();
		try {
			compiledQuery = compileExpr(t);
		} catch (Exception ex) {
			LOG.error("Failed to compile FQMetricQuery: {}", t, ex);
			cf.completeExceptionally(new Exception("Failed to compile FQMetricQuery expression: [" + t.getExpression() + "]", ex));
			return cf;
		}
		final boolean exactTags = compiledQuery.isPropertyListPattern();
		
		Flux<String> metricFlux = lookupNames(Q_METRIC, compiledQuery.getDomain(), max);
		Flux<String> tagKeyFlux = Flux.concat(
				compiledQuery.getKeyPropertyList().keySet().stream()
					.<Flux<String>>map(k -> lookupNames(Q_TAGK, k, max))
					.collect(Collectors.toList())
		);
		Flux<String> tagValueFlux = Flux.concat(
				compiledQuery.getKeyPropertyList().values().stream()
					.<Flux<String>>map(v -> lookupNames(Q_TAGV, v, max))
					.collect(Collectors.toList())
		);
		Flux.concat(metricFlux, tagKeyFlux, tagValueFlux).parallel(3)
			.doOnComplete(() -> {
				processResults(metricFlux, tagKeyFlux, tagValueFlux);
				cf.complete(FQMetrics.newBuilder().build());
			})
			.doOnError(err -> {
				LOG.error("Lookup FluxConcat Failure", err);
				cf.completeExceptionally(err);
			})
			.subscribe();
		
		return cf;
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
				final AtomicBoolean done = new AtomicBoolean(false);
				final AtomicInteger rowsSoFar = new AtomicInteger();
				Deferred<ArrayList<ArrayList<KeyValue>>> drows = null;
				while(!done.get() && rowsSoFar.get() <= maxRows && (drows = scanner.nextRows()) != null) {
					SuAsyncHelpers.singleTBoth(drows, (k,t) -> {
						if(t != null) {
							if(k != null && !k.isEmpty()) {
								k.stream().forEach(kvlist -> {
									kvlist.stream().forEach(kv -> {
										sink.next(new String(kv.key()));
										rowsSoFar.incrementAndGet();
									});
								});
							} else {
								done.set(true);
							}
						}
					});
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