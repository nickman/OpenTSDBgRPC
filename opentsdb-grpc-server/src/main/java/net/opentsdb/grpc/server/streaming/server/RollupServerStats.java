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
package net.opentsdb.grpc.server.streaming.server;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import net.opentsdb.plugin.common.util.AccumulatingLongAdder;
import net.opentsdb.stats.StatsCollector;

/**
 * <p>Title: ServerStats</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.streaming.server.ServerStats</code></p>
 */

public class RollupServerStats {

	public final AtomicLong activeStreams = new AtomicLong();
	public final LongAdder receivedMessages = new LongAdder();
	public final LongAdder sentMessages = new LongAdder();
	public final LongAdder processedItems = new LongAdder();
	public final LongAdder failedItems = new LongAdder();
	public final LongAdder cancellations = new LongAdder();
	
	protected void doStats(StatsCollector collector) {
		collector.record("activestreams", activeStreams.get());
		collector.record("receivedmsgs", receivedMessages.longValue());
		collector.record("sentmsgs", sentMessages.longValue());
		collector.record("processed", processedItems.longValue());
		collector.record("failed", failedItems.longValue());
		collector.record("cancellations", cancellations.longValue());
	}

	public LongAdder accReceivedMessages() {
		return new AccumulatingLongAdder(receivedMessages);
	}
	
	public LongAdder accSentMessages() {
		return new AccumulatingLongAdder(sentMessages);
	}
	
	public LongAdder accProcessedItems() {
		return new AccumulatingLongAdder(processedItems);
	}

	public LongAdder accFailedItems() {
		return new AccumulatingLongAdder(failedItems);
	}
	
	public LongAdder accCancellations() {
		return new AccumulatingLongAdder(cancellations);
	}
	
	
	public ServerStreamStats serverStats() {
		return new ServerStreamStats();
	}
	
	public void recordCancellation() {
		cancellations.increment();
	}
	
	
	public class ServerStreamStats {
		
		private ServerStreamStats() {}
		
		public final LongAdder receivedMessages = accReceivedMessages();
		public final LongAdder sentMessages = accSentMessages();
		public final LongAdder processedItems = accProcessedItems();
		public final LongAdder failedItems = accFailedItems();	
		public final LongAdder cancellations = accCancellations();
		
		public long incrementStreams() {
			return activeStreams.incrementAndGet();
		}
		
		public long decrementStreams() {
			return activeStreams.decrementAndGet();
		}
		
	}
	
	

}
