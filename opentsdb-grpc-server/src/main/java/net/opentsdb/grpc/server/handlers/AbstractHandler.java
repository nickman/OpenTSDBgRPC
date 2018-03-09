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

import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.opentsdb.core.TSDB;
import net.opentsdb.grpc.server.Configuration;
import net.opentsdb.stats.StatsCollector;

/**
 * <p>Title: AbstractHandler</p>
 * <p>Description: Base class for gRPC server handlers</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.handlers.AbstractHandler</code></p>
 */

public abstract class AbstractHandler {
	/** The JMX ObjectName prefix */
	protected static final String OBJECT_NAME_PREFIX = "net.opentsdb.grpc:type=GRPCHandler,name=";
	/** The platform MBeanServer */
	protected static final MBeanServer MBEAN_SERVER = ManagementFactory.getPlatformMBeanServer();
	/** Instance logger */
	protected final Logger LOG = LoggerFactory.getLogger(getClass());
	/** The parent TSDB instance */
	protected final TSDB tsdb;
	/** The extended configuration instance */
	protected final Configuration cfg;
	/** The JMX ObjectName for this handler */
	protected final ObjectName objectName;
	/** The handler name */
	protected final String name = getClass().getSimpleName().replace("Handler", "");
	
	/** The number of currently active streams for this handler */
	protected final AtomicInteger activeStreams = new AtomicInteger();
	/** The total number of created streams for this handler */
	protected final LongAdder totalStreams = new LongAdder();
	
	protected final AtomicLong lastComm = new AtomicLong(0);	

	
	
	
	/**
	 * Creates a new Handler
	 * @param tsdb The parent TSDB instance
	 * @param cfg The extended configuration instance
	 */
	public AbstractHandler(TSDB tsdb, Configuration cfg) {	
		this.tsdb = tsdb;
		this.cfg = cfg;
		objectName = register();
	}
	
	public int getActiveStreams() {
		return activeStreams.get();
	}
	
	public long getTotalStreams() {
		return totalStreams.longValue();
	}
	
	
	/**
	 * Collects stats for this handler
	 * @param collector The stats collector
	 */
	public void collectStats(StatsCollector collector) {
		GrpcStatsCollector gcollector = new GrpcStatsCollector("grpc", collector);
		try {
			gcollector.addExtraTag("grpchandler", name);
			doStats(gcollector);			
		} finally {
			gcollector.clearExtraTag("grpchandler");
		}
		gcollector.recordGrpc("activestreams", activeStreams.get());
		gcollector.recordGrpc("totalstreams", totalStreams.longValue());
		
	}
	
	public long getLastComm() {
		return lastComm.get();
	}
	
	public Date getLastCommDate() {
		return new Date(lastComm.get());
	}	
	
	/**
	 * Collects handler stats
	 * @param collector A grpc specific stats collector
	 */
	protected abstract void doStats(GrpcStatsCollector collector);
	

	/**
	 * Creates the JMX ObjectName and registers this handler
	 * @return the created ObjectName
	 */
	protected ObjectName register() {
		
		try {
			ObjectName on = new ObjectName(OBJECT_NAME_PREFIX + name);
			MBEAN_SERVER.registerMBean(this, on);
			return on;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to register JMX mbean", ex);
		}
	}
	
}
