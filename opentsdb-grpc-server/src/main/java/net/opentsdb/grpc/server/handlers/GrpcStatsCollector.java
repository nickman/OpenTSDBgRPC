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

import java.lang.reflect.Field;

import net.opentsdb.stats.StatsCollector;

/**
 * <p>Title: GrpcStatsCollector</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.handlers.GrpcStatsCollector</code></p>
 */

public class GrpcStatsCollector extends StatsCollector {
	protected static final Field prefixField;
	protected final StatsCollector delegate;
	protected final String grpcPrefix;
	
	static {
		try {
			prefixField = StatsCollector.class.getDeclaredField("prefix");
			prefixField.setAccessible(true);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to get StatsCollector prefix field", ex);
		}
	}
	
	/**
	 * Creates a new GrpcStatsCollector
	 * @param grpcPrefix The grpc specific metric prefix
	 * @param delegate The wrapped stats collector delegate
	 */
	public GrpcStatsCollector(String grpcPrefix, StatsCollector delegate) {
		super(getPrefix(delegate));
		this.delegate = delegate;
		this.grpcPrefix = grpcPrefix;
	}
	
	public void recordGrpc(final String name, final long value) {
		record(grpcPrefix + "." + name, value);
	}

	
	protected static String getPrefix(StatsCollector delegate) {
		try {
			Object pref = prefixField.get(delegate);
			return pref==null ? "" : pref.toString();
		} catch (Exception ex) {
			throw new RuntimeException("Failed to get StatsCollector prefix", ex);
		}
	}
}
