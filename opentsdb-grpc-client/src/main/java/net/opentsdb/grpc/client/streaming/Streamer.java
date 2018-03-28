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
package net.opentsdb.grpc.client.streaming;

import java.util.concurrent.TimeUnit;

/**
 * <p>Title: Streamer</p>
 * <p>Description: Defines an auto streamer</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.Streamer</code></p>
 * @param <T> The request type
 * @param <R> The response type
 */

public interface Streamer<T, R> {
	/**
	 * Returns a formatted string presenting the streamer stats
	 * @return the streamer stats
	 */
	public String printStats();
	
	public AbstractStreamer<T,R> start();
	
	public boolean send(T t);
	
	public boolean waitForCompletion(long time, TimeUnit unit);
	
	public void waitForCompletion();
	
	public void clientComplete();
	
	public long getRequestsSent();

	public long getResponsesReceived();

	public long getRequestsDropped();

	public long getInFlight();

	public long getOnReadyEvents();
	
}
