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
package net.opentsdb.plugin.common;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import io.netty.channel.epoll.Epoll;
import net.opentsdb.plugin.common.util.RelTime;
import net.opentsdb.plugin.common.util.TUnit;
import net.opentsdb.utils.Config;

/**
 * <p>Title: Configuration</p>
 * <p>Description: Configuration constants and utils</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.Configuration</code></p>
 */

public class Configuration {
	public static final String GRPC_IFACE = "grpc.server.iface";
	public static final String DEFAULT_IFACE = "0.0.0.0";
	
	public static final String GRPC_PORT = "grpc.server.port";
	public static final int DEFAULT_PORT = 4249;
	
	public static final String GRPC_FLOW_CONTROL = "grpc.server.flowcontrol";
	public static final int DEFAULT_GRPC_FLOW_CONTROL = 1048576;
	
	public static final String GRPC_MAX_HEADERLIST_SIZE = "grpc.server.maxheaders";
	public static final int DEFAULT_GRPC_MAX_HEADERLIST_SIZE = 8192;

	public static final String GRPC_MAX_MESSAGE_SIZE = "grpc.server.maxmessagesize";
	public static final int DEFAULT_GRPC_MAX_MESSAGE_SIZE = 4 * 1024 * 1024;
	
	public static final String GRPC_KEEP_ALIVE = "grpc.server.keepalive";
	public static final RelTime DEFAULT_GRPC_KEEP_ALIVE = new RelTime(TUnit.s, 10);
	
	public static final String GRPC_KEEP_ALIVE_TIMEOUT = "grpc.server.keepalive.timeout";
	public static final RelTime DEFAULT_GRPC_KEEP_ALIVE_TIMEOUT = new RelTime(TUnit.m, 10);
	
	public static final String GRPC_PERMIT_KEEP_ALIVE_TIME = "grpc.server.keepalive.permit.time";
	public static final RelTime DEFAULT_GRPC_PERMIT_KEEP_ALIVE_TIME = new RelTime(TUnit.m, 5);
	
	public static final String GRPC_PERMIT_KEEP_ALIVE = "grpc.server.keepalive.permit.time";
	public static final boolean DEFAULT_GRPC_PERMIT_KEEP_ALIVE = true;
	
	public static final String GRPC_MAX_CALLS_PER_CONN = "grpc.server.conn.maxcalls";
	public static final int DEFAULT_GRPC_MAX_CALLS_PER_CONN = Integer.MAX_VALUE;
	
	public static final String GRPC_MAX_CONN_AGE = "grpc.server.conn.maxage";
	public static final RelTime DEFAULT_GRPC_MAX_CONN_AGE = new RelTime(TUnit.s, Long.MAX_VALUE);
	
	public static final String GRPC_MAX_CONN_AGE_GRACE = "grpc.server.conn.maxage.grace";
	public static final RelTime DEFAULT_GRPC_MAX_CONN_AGE_GRACE = new RelTime(TUnit.s, Long.MAX_VALUE);
	
	public static final String GRPC_MAX_CONN_IDLE = "grpc.server.conn.maxidle";
	public static final RelTime DEFAULT_GRPC_MAX_CONN_IDLE = new RelTime(TUnit.s, Long.MAX_VALUE);
	
	public static final String GRPC_NO_EPOLL = "grpc.epoll.disable";
	public static final boolean DEFAULT_GRPC_NO_EPOLL = false;
	
	public static final String GRPC_BOSS_THREADS = "grpc.netty.boss.threads";
	public static final String GRPC_WORKER_THREADS = "grpc.netty.worker.threads";
	
	public static final int CORES = Runtime.getRuntime().availableProcessors();
	public static final String CORES_OVERRIDE = "cores.override";
	
	
	private final Config config;
	
	
	
	public Configuration(Config config) {
		this.config = config;
	}

	/**
	 * Acquires a configuration value. Order of lookup is: <ol>
	 *  <li>System properties</li>
	 *  <li>Environmental variables</li>
	 *  <li>The TSDB configuration</li>
	 * </ol>
	 * When looking up an environmental variable, they key is uppercased
	 * and dots are replaced with underscores. e.g. 
	 * The key <b><code>grpc.server.iface</code></b> is renamed to 
	 * <b><code>GRPC_SERVER_IFACE</code></b>.
	 * @param key The configuration key
	 * @param defaultValue The default value
	 * @return The configured value or the default if not defined
	 */
	public String config(String key, String defaultValue) {
		boolean updateConfig = true;
		String v = System.getProperty(key);
		if(v==null) {
			v = System.getenv(key.toUpperCase().replace('.', '_'));
			if(v==null) {
				try {
					v = config.getString(key);
					// Thought this would throw an NPE, but it doesn't
					// so we'll do this in case the spec changes
					if(v==null) {
						throw new Exception();
					}
					updateConfig = false;
				} catch (Exception x1) {
					v = defaultValue;
				}
			}
		}
		if(v != null && updateConfig) {
			config.overrideConfig(key, v);
		}
		return v;
	}
	
	/**
	 * Acquires a configuration value via {@link #config(String, String)}
	 * @param key The configuration key
	 * @param defaultValue The default value
	 * @return The configured value or the default if not defined
	 */

	public int config(String key, int defaultValue) {
		String v = config(key, (String)null);
		int i = Integer.MIN_VALUE;
		if(v==null || v.trim().isEmpty()) {
			i = defaultValue;
		} else {
			try {
				i = Integer.parseInt(v.trim());
			} catch (Exception x) {
				i = defaultValue;
			}
		}
		config.overrideConfig(key, Integer.toString(i));
		return i;
	}
	
	/**
	 * Acquires a configuration value via {@link #config(String, String)}
	 * @param key The configuration key
	 * @param defaultValue The default value
	 * @return The configured value or the default if not defined
	 */
	public RelTime config(String key, RelTime defaultValue) {
		String v = config(key, (String)null);
		RelTime r = null;
		if(v==null || v.trim().isEmpty()) {
			r = defaultValue;
		} else {
			try {
				r = RelTime.from(v.trim());
			} catch (Exception x) {
				r = defaultValue;
			}
		}
		config.overrideConfig(key, r.toString());
		return r;
	}
	
	
	/**
	 * Acquires a configuration value via {@link #config(String, String)}
	 * @param key The configuration key
	 * @param defaultValue The default value
	 * @return The configured value or the default if not defined
	 */
	public boolean config(String key, boolean defaultValue) {
		String v = config(key, (String)null);
		boolean b = false;
		if(v==null || v.trim().isEmpty()) {
			b = defaultValue;
		} else {
			try {
				b = Boolean.parseBoolean(v.trim());
			} catch (Exception x) {
				b = defaultValue;
			}
		}
		config.overrideConfig(key, "" + b);
		return b;
	}
	
	private static final Pattern COMMA_SPLITTER = Pattern.compile(",");
	
	/**
	 * Acquires a configuration value via {@link #config(String, String)}
	 * @param key The configuration key
	 * @param defaultValue The default value
	 * @return The configured value or the default if not defined
	 */
	public String[] config(String key, String...defaultValue) {
		String v = config(key, (String)null);
		List<String> arr = new ArrayList<>();
		if(v==null || v.trim().isEmpty()) {
			Collections.addAll(arr, defaultValue);
		} else {
			try {
				String[] as = COMMA_SPLITTER.split(v);
				for(String a : as) {
					if(a==null || a.trim().isEmpty()) continue;
					arr.add(a.trim());
				}
			} catch (Exception x) {
				arr.clear();
				Collections.addAll(arr, defaultValue);
			}
		}
		String[] result = arr.toArray(new String[0]);
		config.overrideConfig(key, String.join(",", result));
		return result;
	}
	
	
	
	/**
	 * Determines if epoll is available.
	 * If we're on Linux, usually it is, but
	 * it can be disabled by setting {@link #GRPC_NO_EPOLL}
	 * to true.
	 * @return
	 */
	public boolean isEpoll() {
		if(config(GRPC_NO_EPOLL, false)) {
			return false;
		}
		return Epoll.isAvailable();
	}
	
	/**
	 * Returns the number of available cores
	 * which by default is reported from {@link Runtime#availableProcessors()}
	 * but can be overriden by setting {@link #CORES_OVERRIDE}.
	 * @return the number of available cores
	 */
	public int availableCores() {
		return config(CORES_OVERRIDE, CORES);
	}
	
	/**
	 * Returns the number of netty boss threads to 
	 * run in the gRPC server.
	 * @return the number of netty boss threads
	 */
	public int bossThreads() {
		return config(GRPC_BOSS_THREADS, 1);
	}
	
	/**
	 * Returns the number of netty worker threads to 
	 * run in the gRPC server.
	 * @return the number of netty worker threads
	 */
	public int workerThreads() {
		return config(GRPC_WORKER_THREADS, availableCores() * 2);
	}
	
}
