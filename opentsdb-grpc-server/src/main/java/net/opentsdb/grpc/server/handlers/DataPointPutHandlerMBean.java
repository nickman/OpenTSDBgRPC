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

import java.util.Date;

/**
 * <p>Title: DataPointPutHandlerMBean</p>
 * <p>Description: JMX interface for {@link DataPointPutHandler}</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.handlers.DataPointPutHandlerMBean</code></p>
 */

public interface DataPointPutHandlerMBean {

	/**
	 * Returns the number of active datapoint streams
	 * @return the number of active datapoint streams
	 */
	public int getActiveStreams();
	
	/**
	 * Returns the cummulative number of started datapoint streams
	 * @return the total number of streams
	 */
	public long getTotalStreams();
	
	/**
	 * Returns the total number of received datapoints
	 * @return the total number of received datapoints
	 */
	public long getReceivedDataPoints();
	
	/**
	 * Returns the total number of successfully processed datapoints
	 * @return the total number of successfully processed datapoints
	 */
	public long getProcessedDataPoints();
	
	/**
	 * Returns the total number of failed datapoints
	 * @return the total number of failed datapoints
	 */
	public long getFailedDataPoints();
	
	/**
	 * Returns the timestamp of the last comm to this handler
	 * @return a long UTC timestamp
	 */
	public long getLastComm();
	
	/**
	 * Returns the date of the last comm to this handler
	 * @return a date
	 */
	public Date getLastCommDate();
	

}
