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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stumbleupon.async.Deferred;

import net.opentsdb.core.TSDB;
import net.opentsdb.stats.StatsCollector;
import net.opentsdb.tsd.RpcPlugin;

/**
 * <p>Title: MultiBootRpcPlugin</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.plugin.common.isolation.MultiBootRpcPlugin</code></p>
 */

public class MultiBootRpcPlugin extends RpcPlugin {
	protected static final Logger LOG = LoggerFactory.getLogger(MultiBootRpcPlugin.class);
	
	protected final Set<RpcPlugin> delegates = new LinkedHashSet<>();
	protected final String[] delegateClassNames;
	protected IsolatedClassLoader2 icl;
	protected String version = getClass().getPackage().getImplementationVersion();
	
	/**
	 * Creates a new MultiBootRpcPlugin
	 * @param rpcClassNames The class names of the delegates to load
	 */	
	public MultiBootRpcPlugin(String...rpcClassNames) {
		delegateClassNames = rpcClassNames;
		if(version==null) {
			version = "Unassigned";
		}
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tsd.RpcPlugin#initialize(net.opentsdb.core.TSDB)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void initialize(TSDB tsdb) {
		LOG.info("Creating Delegates: {}", Arrays.toString(delegateClassNames));
		icl = IsolatedClassLoader2.classLoader(true, getClass().getProtectionDomain().getCodeSource().getLocation());
		for(String className : delegateClassNames) {
			try {
				Class<? extends RpcPlugin> clazz = (Class<? extends RpcPlugin>) Class.forName(className, true, icl);
				RpcPlugin delegate = clazz.newInstance();
				delegates.add(delegate);
				LOG.info("Created RpcPlugin Delegate: {}", className);
			} catch (Exception ex) {
				LOG.error("Failed to create delegate RpcPlugin: {}", className, ex);
				throw new IllegalArgumentException("Failed to create delegate RpcPlugin: " + className, ex);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tsd.RpcPlugin#shutdown()
	 */
	@Override
	public Deferred<Object> shutdown() {
		Deferred<Object> def = new Deferred<>();
		Thread t = new Thread("MultiBootRpcPluginShutdown") {
			public void run() {
				final List<Deferred<Object>> defs = new ArrayList<>(delegates.size());
					for(RpcPlugin delegate : delegates) {
						String className = delegate.getClass().getName();
						try {
							LOG.info("Stopping Delegate RpcPlugin: {}", className);
							Deferred<Object> d = delegate.shutdown();
							defs.add(d);
						} catch (Exception ex) {
							LOG.error("Failed to stop Delegate RpcPlugin: {}, err={}", className, ex);
						}
					}
					if(!defs.isEmpty()) {
						try {
							Deferred.group(defs).join(5000);
							LOG.info("{} delegate plugins shutdown", defs.size());
						} catch (Exception ex) {
							LOG.warn("Timed out waiting for delegates to shut down");
						} finally {
							
							def.callback(null);
						}
					}
			}
		};
		t.setDaemon(true);
		t.start();
		return def;
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tsd.RpcPlugin#version()
	 */
	@Override
	public String version() {
		return version;
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tsd.RpcPlugin#collectStats(net.opentsdb.stats.StatsCollector)
	 */
	@Override
	public void collectStats(StatsCollector collector) {
		for(RpcPlugin delegate : delegates) {
			collector.addExtraTag("delegate", delegate.getClass().getSimpleName());
			try {
				delegate.collectStats(collector);
			} catch (Exception ex) {
				LOG.warn("Failed to collect stats from RpcPlugin delegate: plugin={}, err={}",
						delegate.getClass().getName(), ex
				);
			} finally {
				collector.clearExtraTag("delegate");
			}
		}
	}

}
