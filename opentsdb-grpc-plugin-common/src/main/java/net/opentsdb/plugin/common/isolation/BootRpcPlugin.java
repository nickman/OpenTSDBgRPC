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
package net.opentsdb.plugin.common.isolation;

import java.net.URL;

import com.stumbleupon.async.Deferred;

import net.opentsdb.core.TSDB;
import net.opentsdb.stats.StatsCollector;
import net.opentsdb.tsd.RpcPlugin;

/**
 * <p>Title: BootRpcPlugin</p>
 * <p>Description: Boots the {@link GRPCPlugin} instance from an isolated class loader.
 * This is required because of conflicts between OpenTSDB's protobuf implementation
 * and the more contemporary protobufs used by gRPC.</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.BootRpcPlugin</code></p>
 */

public abstract class BootRpcPlugin extends RpcPlugin {
	protected RpcPlugin delegate = null;
	protected final String delegatePluginClassName;
	
	public BootRpcPlugin(String delegatePluginClassName) {
		this.delegatePluginClassName = delegatePluginClassName;
	}
	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tsd.RpcPlugin#initialize(net.opentsdb.core.TSDB)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(TSDB tsdb) {
		// IsolatedClassLoader2 icl = IsolatedClassLoader2.classLoader(true, getClass().getProtectionDomain().getCodeSource().getLocation());
		IsolatedClassLoader icl = new IsolatedClassLoader(new URL[] {getClass().getProtectionDomain().getCodeSource().getLocation()});
		try {
			Class<? extends RpcPlugin> clazz = (Class<? extends RpcPlugin>) Class.forName(delegatePluginClassName, true, icl);
			delegate = clazz.newInstance();
			delegate.initialize(tsdb);
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tsd.RpcPlugin#shutdown()
	 */
	@Override
	public Deferred<Object> shutdown() {
		return delegate.shutdown();
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tsd.RpcPlugin#version()
	 */
	@Override
	public String version() {
		return delegate.version();
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tsd.RpcPlugin#collectStats(net.opentsdb.stats.StatsCollector)
	 */
	@Override
	public void collectStats(StatsCollector collector) {
		delegate.collectStats(collector);
	}

}
