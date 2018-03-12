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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import net.opentsdb.plugin.common.util.AccumulatingLongAdder;

/**
 * <p>Title: StreamerContext</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.streaming.server.StreamerContext</code></p>
 */

public class StreamerContext {
	
	private final AtomicLong activeStreams;
	private final LongAdder receivedMessages;
	private final LongAdder sentMessages;
	private final LongAdder processedItems;
	private final LongAdder failedItems;
	private final AtomicBoolean open = new AtomicBoolean();
	

	public String toString() {
		return new StringBuilder("StreamerContext [")
			.append("\n\tOpen:").append(open.get())
			.append("\n\tActiveStreams:").append(activeStreams.get())
			.append("\n\tReceived:").append(receivedMessages.longValue())
			.append("\n\tSent:").append(sentMessages.longValue())
			.append("\n\tProcessed:").append(processedItems.longValue())
			.append("\n\tFailed:").append(failedItems.longValue())
			.append("\n]")
			.toString();
	}
	
	/**
	 * Creates a new StreamerContext
	 */
	public StreamerContext(RollupServerStats rss) {
		activeStreams = rss.activeStreams;
		receivedMessages = rss.accReceivedMessages();
		sentMessages = rss.accSentMessages();
		processedItems = rss.accProcessedItems();
		failedItems = rss.accFailedItems();		
	}
	
	public LongAdder accProcessedItems() {
		return new AccumulatingLongAdder(processedItems);
	}
	
	public LongAdder accFailedItems() {
		return new AccumulatingLongAdder(failedItems);
	}
	
	
	AtomicBoolean openFlag() {
		return open;
	}
	
	public boolean isOpen() {
		return open.get();
	}
	
	public long getReceivedMessages() {
		return receivedMessages.longValue();
	}

	public long getSentMessages() {
		return sentMessages.longValue();
	}

	public long getProcessedItems() {
		return processedItems.longValue();
	}

	public long getFailedItems() {
		return failedItems.longValue();
	}

	public long incrementStreams() {
		return activeStreams.incrementAndGet();
	}
	
	public long decrementStreams() {
		return activeStreams.decrementAndGet();
	}

	public void received(int received) {
		receivedMessages.add(received);
	}
	
	public void received() {
		receivedMessages.increment();
	}
	
	
	public void sent(int sent) {
		sentMessages.add(sent);
	}
	
	public void sent() {
		sentMessages.increment();
	}
	
	
	public void processed(int processed) {
		processedItems.add(processed);
	}
	
	public void processed() {
		processedItems.increment();
	}
	
	
	public void failed(int failed) {
		failedItems.add(failed);
	}
	
	public void failed() {
		failedItems.increment();
	}
	

}
