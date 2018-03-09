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

import net.opentsdb.grpc.server.util.AccumulatingLongAdder;

/**
 * <p>Title: ServerStats</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.streaming.server.ServerStats</code></p>
 */

public class ServerStats {

	public final AtomicLong activeStreams = new AtomicLong();
	public final LongAdder receivedMessages = new LongAdder();
	public final LongAdder sentMessages = new LongAdder();
	public final LongAdder processedItems = new LongAdder();
	public final LongAdder failedItems = new LongAdder();

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
	
	
	

}
