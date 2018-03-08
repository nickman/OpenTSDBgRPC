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
package net.opentsdb.consul;

import java.net.URL;

import com.stumbleupon.async.Deferred;

import net.opentsdb.core.TSDB;
import net.opentsdb.stats.StatsCollector;
import net.opentsdb.tools.StartupPlugin;
import net.opentsdb.utils.Config;

/**
 * <p>Title: BootStartupPlugin</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.consul.BootStartupPlugin</code></p>
 */

public class BootStartupPlugin extends StartupPlugin {
	private StartupPlugin delegate = null;

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tools.StartupPlugin#initialize(net.opentsdb.utils.Config)
	 */
	@Override
	public Config initialize(Config config) {
		IsolatedClassLoader icl = new IsolatedClassLoader(new URL[] {getClass().getProtectionDomain().getCodeSource().getLocation()});
		try {
			Class<? extends StartupPlugin> clazz = (Class<? extends StartupPlugin>) Class.forName("net.opentsdb.consul.ConsulPlugin", true, icl);
			delegate = clazz.newInstance();
			delegate.initialize(config);
		} catch (Exception ex) {
			throw new IllegalArgumentException(ex);
		}
		return config;
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tools.StartupPlugin#setReady(net.opentsdb.core.TSDB)
	 */
	@Override
	public void setReady(TSDB tsdb) {
		delegate.setReady(tsdb);

	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tools.StartupPlugin#shutdown()
	 */
	@Override
	public Deferred<Object> shutdown() {
		return delegate.shutdown();
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tools.StartupPlugin#version()
	 */
	@Override
	public String version() {
		return delegate.version();
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tools.StartupPlugin#getType()
	 */
	@Override
	public String getType() {
		return delegate.getType();
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tools.StartupPlugin#collectStats(net.opentsdb.stats.StatsCollector)
	 */
	@Override
	public void collectStats(StatsCollector collector) {
		delegate.collectStats(collector);
	}

}
