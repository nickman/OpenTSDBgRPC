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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.protobuf.ByteString;

import net.opentsdb.core.Aggregator;
import net.opentsdb.core.Aggregators;
import net.opentsdb.core.FillPolicy;
import net.opentsdb.core.Query;
import net.opentsdb.grpc.DataPoint;
import net.opentsdb.grpc.DataPointQuery;
import net.opentsdb.grpc.DataPoints;
import net.opentsdb.grpc.DateTime;
import net.opentsdb.grpc.Downsample;
import net.opentsdb.grpc.MetricAndTags;
import net.opentsdb.grpc.MetricTags;
import net.opentsdb.grpc.RateOptions;
import net.opentsdb.grpc.RelativeTime;
import net.opentsdb.grpc.TSDBAnnotation;
import net.opentsdb.grpc.Tsuid;
import net.opentsdb.grpc.Tsuids;
import net.opentsdb.meta.Annotation;
import net.opentsdb.plugin.common.util.RelTime;
import net.opentsdb.uid.UniqueId;

/**
 * <p>Title: ProtoConverters</p>
 * <p>Description: Converters between the gRPC types and native OpenTSDB types</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.ProtoConverters</code></p>
 */

public class ProtoConverters {
	
	/**
	 * Builds a TSDBAnnotation gRPC object from an OpenTSDB annotation
	 * @param a The OpenTSDB annotation
	 * @return The TSDBAnnotation gRPC object
	 */
	public static TSDBAnnotation from(Annotation a) {
		Objects.requireNonNull(a, "The passed Annotation was null");
		TSDBAnnotation.Builder b = TSDBAnnotation.newBuilder();
		if(a.getDescription() != null) {
			b.setDescription(a.getDescription());
		}
		b.setEndTime(a.getEndTime());
		if(a.getNotes() != null) {
			b.setNotes(a.getNotes());
		}
		b.setStartTime(a.getStartTime());
		String tsuid = a.getTSUID();
		if(tsuid != null) {
			b.setTsuid(Tsuid.newBuilder()
					.setTsuidName(a.getTSUID())
					.setTsuidBytes(ByteString.copyFrom(UniqueId.stringToUid(a.getTSUID())))
					.build()
			);
		}
		Map<String, String> c = a.getCustom();
		if(c != null && !c.isEmpty()) {
			b.putAllCustom(c);
		}		
		return b.build();
	}
	
	public static Annotation from(TSDBAnnotation a) {
		Objects.requireNonNull(a, "The passed TSDBAnnotation was null");
		Annotation an = new Annotation();
		Map<String, String> c = a.getCustomMap();
		if(c != null && !c.isEmpty()) {
			an.setCustom(c);
		}
		an.setDescription(a.getDescription());		
		an.setEndTime(a.getEndTime());
		an.setNotes(a.getNotes());
		an.setStartTime(a.getStartTime());
		Tsuid tsuid = a.getTsuid();
		ByteString bs = tsuid.getTsuidBytes();
		String name = tsuid.getTsuidName();
		String ts = null;
		if(name==null) {
			if(bs!=null) {
				ts = UniqueId.uidToString(bs.toByteArray());
			}
		} else {
			ts = name;
		}
		an.setTSUID(ts);
		return an;
	}
	
	public static long timestamp(DateTime dt) {
		if(dt.hasRelTime()) {
			RelativeTime rt = dt.getRelTime();
			long ms = RelTime.from(rt.getTime() + rt.getUnit()).convertTo(TimeUnit.MILLISECONDS);
			return System.currentTimeMillis() - ms;
		} else {
			return dt.getUnitTime();
		}
	}
	
	public static long toAbs(RelativeTime rt) {
		if("now".equalsIgnoreCase(rt.getUnit())) {
			return System.currentTimeMillis();
		}
		long ms = RelTime.from(rt.getTime() + rt.getUnit()).convertTo(TimeUnit.MILLISECONDS);
		return System.currentTimeMillis() - ms;
	}
	
	
	public static Aggregator from(net.opentsdb.grpc.Aggregator aggr) {
		return Aggregators.get(aggr.name().toLowerCase());
	}
	
	public static List<String> tsuidsToStrings(Tsuids tsuids) {
		return tsuids.getTsuidsList().stream()
			.map(t -> t.getTsuidName())
			.collect(Collectors.toList());
	}
	
	public static net.opentsdb.core.RateOptions from(RateOptions rateOptions) {
		net.opentsdb.core.RateOptions ro = new net.opentsdb.core.RateOptions();
		ro.setIsCounter(rateOptions.getCounter());
		ro.setDropResets(rateOptions.getDropResets());
		if(rateOptions.getCounterMax() != 0L) {
			ro.setCounterMax(rateOptions.getCounterMax());
		}
		if(rateOptions.getResetValue() != 0) {
			ro.setResetValue(rateOptions.getResetValue());
		}
		return ro;
	}
	
	
	public static Query from(DataPointQuery dpq, Query q) {
		q.setStartTime(timestamp(dpq.getStartTime()));
		DateTime end = dpq.getEndTime();
		if(end!=null) {
			q.setEndTime(timestamp(end));
		}
		boolean rate = dpq.getRate();		
		net.opentsdb.grpc.Aggregator agg = dpq.getAggregator();
		
		Tsuids tsuids = dpq.getTsuids();
		MetricAndTags mtags = dpq.getMetricAndTags();
		
		RateOptions rateOptions = dpq.getRateOptions();
		
		Downsample ds = dpq.getDownSample();
		
		if(ds!=null && ds.isInitialized() && ds.getRelTime().getTime() != 0) {
			long ts = toAbs(ds.getRelTime());
			if(ts!=0) {
				FillPolicy fp = null;
				if(ds.getFillPolicy()!=null) {
					fp = FillPolicy.fromString(ds.getFillPolicy().name().toLowerCase());
					q.downsample(ts, from(ds.getAggregator()), fp);
				} else {
					q.downsample(ts, from(ds.getAggregator()));
				}
			}
		}
		
		
		if(tsuids != null && tsuids.getTsuidsCount() > 0) {
			if(rateOptions != null) {
				q.setTimeSeries(tsuidsToStrings(tsuids), from(agg), rate, from(rateOptions));
			} else {
				q.setTimeSeries(tsuidsToStrings(tsuids), from(agg), rate);
			}
		} else {
			if(rateOptions != null) {
				q.setTimeSeries(mtags.getMetric(), mtags.getTags().getTagsMap(), from(agg), rate, from(rateOptions));
			} else {
				q.setTimeSeries(mtags.getMetric(), mtags.getTags().getTagsMap(), from(agg), rate);
			}
		}
		return q;
	}
	
//	setTimeSeries(List<String> tsuids, Aggregator function, boolean rate)
//	setTimeSeries(List<String> tsuids, Aggregator function, boolean rate, RateOptions rate_options)
//	setTimeSeries(String metric, Map<String,String> tags, Aggregator function, boolean rate)
//	setTimeSeries(String metric, Map<String,String> tags, Aggregator function, boolean rate, RateOptions rate_options)
		
	
//	message DataPoint {
//		string metric = 1;
//		MetricTags metricTags = 2;
//		int64 timestamp = 3;
//		double value = 4;
//		Aggregation aggregation = 5;
//	}
//	message DataPointQuery {
//		DateTime startTime = 1;
//		DateTime endTime = 2;
//		oneof metrics {
//			MetricAndTags metricAndTags = 3;
//			Tsuids tsuids = 4;
//		}			
//		Aggregator aggregator = 5;
//		bool rate = 6;
//		RateOptions rateOptions = 7;
//		Downsample downSample = 8;
//	}
//
//	message MetricAndTags {
//		string metric = 1;
//		MetricTags tags = 2;
//	}
	
	
	
	public static DataPoints from(net.opentsdb.core.DataPoints dps) {
		DataPoints.Builder builder = DataPoints.newBuilder();
		final int size = dps.aggregatedSize();
		for(int i = 0; i < size; i++) {			
			DataPoint dp = DataPoint.newBuilder()
			.setMetric(dps.metricName())
			.setMetricTags(MetricTags.newBuilder().putAllTags(dps.getTags()))
			.setTimestamp(dps.timestamp(i))
			.setValue(dps.doubleValue(i))			
			.build();
			builder.addDatapoints(dp);
		}
		return builder.build();
	}
	
}
