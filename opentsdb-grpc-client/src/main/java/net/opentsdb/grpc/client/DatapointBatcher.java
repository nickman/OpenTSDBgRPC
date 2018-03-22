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

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import net.opentsdb.grpc.DataPoint;
import net.opentsdb.grpc.MetricTags;
import net.opentsdb.grpc.PutDatapoints;
import net.opentsdb.grpc.PutDatapointsResponse;
import net.opentsdb.grpc.TXTime;
import net.opentsdb.grpc.client.streaming.BidiStreamer;
import net.opentsdb.grpc.client.streaming.Streamer;

/**
 * <p>Title: DatapointBatcher</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.client.DatapointBatcher</code></p>
 */

public class DatapointBatcher {
	protected final BidiStreamer<PutDatapoints,PutDatapointsResponse> streamer;
	protected final AtomicReference<PutDatapoints.Builder> batch = 
			new AtomicReference<>(PutDatapoints.newBuilder().setDetails(true)); 
	protected final AtomicLong serial = new AtomicLong();

	
	

	public DatapointBatcher(BidiStreamer<PutDatapoints, PutDatapointsResponse> streamer) {
		this.streamer = streamer;
	}

	public void record(String metric, Number value, Map<String, String> tags, long timestamp) {
		DataPoint dp = DataPoint.newBuilder()
				.setMetric(metric)
				.setMetricTags(MetricTags.newBuilder().putAllTags(tags))
				.setTimestamp(timestamp)
				.setValue(value.doubleValue())
				.build();
		batch.get().addDataPoints(dp);
	}
	
	public void record(String metric, Number value, Map<String, String> tags) {
		record(metric, value, tags, System.currentTimeMillis());
	}
	
	public int flush() {
		return flush(streamer);
	}
	
	public int flush(Streamer<PutDatapoints, PutDatapointsResponse> streamer) {
		TXTime tx = TXTime.newBuilder().setTxtime(System.currentTimeMillis()).build();
		PutDatapoints pd = batch.getAndSet(PutDatapoints.newBuilder().setDetails(true)).setTxTime(tx).build();
		int points = pd.getDataPointsCount();
		streamer.send(pd);
		return points;
	}
	
	public int size() {
		return batch.get().getDataPointsCount();
	}

}
