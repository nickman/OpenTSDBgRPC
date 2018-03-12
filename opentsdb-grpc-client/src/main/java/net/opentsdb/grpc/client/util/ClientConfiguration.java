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
package net.opentsdb.grpc.client.util;

import java.net.SocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.grpc.ManagedChannelBuilder;
import io.grpc.internal.GrpcUtil;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.opentsdb.grpc.client.envoy.EnvoyFinder;
import net.opentsdb.plugin.common.util.RelTime;
import net.opentsdb.plugin.common.util.TUnit;

/**
 * <p>Title: ClientConfiguration</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.client.util.ClientConfiguration</code></p>
 */

public class ClientConfiguration {
	private static final Map<String, String> CONFIGS = new ConcurrentHashMap<>();
	
	public static final String GRPC_VERSION = GrpcUtil.class.getPackage().getImplementationVersion();
	
	public static final String GRPC_SERVER = "grpc.server";
	public static final String DEFAULT_GRPC_SERVER = "localhost";
	
	public static final String GRPC_PORT = "grpc.server.port";
	public static final int DEFAULT_GRPC_PORT = 10000;
	
	public static final String GRPC_MAX_CONN_IDLE = "grpc.client.conn.maxidle";
	public static final RelTime DEFAULT_GRPC_MAX_CONN_IDLE = new RelTime(TUnit.s, Long.MAX_VALUE);
	
	public static final String GRPC_KEEP_ALIVE = "grpc.client.keepalive";
	public static final RelTime DEFAULT_GRPC_KEEP_ALIVE = new RelTime(TUnit.s, 10);
	
	public static final String GRPC_KEEP_ALIVE_TIMEOUT = "grpc.client.keepalive.timeout";
	public static final RelTime DEFAULT_GRPC_KEEP_ALIVE_TIMEOUT = new RelTime(TUnit.m, 10);

	public static final String GRPC_KEEP_ALIVE_NOCALLS = "grpc.client.keepalive.nocalls";
	public static final boolean DEFAULT_GRPC_KEEP_ALIVE_NOCALLS = false;
	
	public static final String GRPC_DECOMPRESS_INCOMING = "grpc.client.in.decompress";
	public static final boolean DEFAULT_GRPC_DECOMPRESS_INCOMING = false;
		
	public static final String GRPC_MAX_MESSAGE_SIZE = "grpc.client.maxmessagesize";
	public static final int DEFAULT_GRPC_MAX_MESSAGE_SIZE = 4 * 1024 * 1024;
	
	public static final String GRPC_MAX_HEADERLIST_SIZE = "grpc.client.maxheaders";
	public static final int DEFAULT_GRPC_MAX_HEADERLIST_SIZE = 8192;
	
	
	public static final String GRPC_FLOW_CONTROL = "grpc.client.flowcontrol";
	public static final int DEFAULT_GRPC_FLOW_CONTROL = 1048576;
	
	
	public static final String GRPC_AUTHORITY = "grpc.authority";
	
	public static final String GRPC_USER_AGENT = "grpc.useragent";
	public static final String DEFAULT_GRPC_USER_AGENT = "OpenTSDBGRPCClient-" + GRPC_VERSION;
	
	public static void main(String[] args) {
		System.out.println("v: " + DEFAULT_GRPC_USER_AGENT);
	}

	public static ManagedChannelBuilder<NettyChannelBuilder> builder() {
		return builder(
			config(GRPC_SERVER, DEFAULT_GRPC_SERVER), 
			config(GRPC_PORT, DEFAULT_GRPC_PORT)
		);
	}
	
	public static ManagedChannelBuilder<NettyChannelBuilder> builder(String host, int port) {
		NettyChannelBuilder mcb = NettyChannelBuilder.forAddress(host, port);
		configure(mcb);
		return mcb;
	}
	
	public static ManagedChannelBuilder<NettyChannelBuilder> builder(URI uri) {
		NettyChannelBuilder mcb = NettyChannelBuilder.forAddress(uri.getHost(), uri.getPort());
		configure(mcb);
		return mcb;
	}
	
	public static ManagedChannelBuilder<NettyChannelBuilder> builderFromConsul() {
		return builder(new EnvoyFinder().getEnvoyServiceURI());
	}
	
	public static ManagedChannelBuilder<NettyChannelBuilder> builderFromConsul(String host, int port) {
		return builder(new EnvoyFinder(host, port).getEnvoyServiceURI());
	}
	
	public static ManagedChannelBuilder<NettyChannelBuilder> builderFromConsul(URI uri) {
		return builder(new EnvoyFinder(uri.getHost(), uri.getPort()).getEnvoyServiceURI());
	}
	
	public static ManagedChannelBuilder<NettyChannelBuilder> builder(SocketAddress socketAddress) {
		NettyChannelBuilder mcb = NettyChannelBuilder.forAddress(socketAddress);
		configure(mcb);
		return mcb;
	}
	
	
	private static void configure(NettyChannelBuilder mcb) {
		mcb.directExecutor().usePlaintext(true);
		if(config(GRPC_DECOMPRESS_INCOMING, DEFAULT_GRPC_DECOMPRESS_INCOMING)) {
			mcb.enableFullStreamDecompression();
		}
		RelTime idleTimeout = config(GRPC_MAX_CONN_IDLE, DEFAULT_GRPC_MAX_CONN_IDLE);
		RelTime keepAlive = config(GRPC_KEEP_ALIVE, DEFAULT_GRPC_KEEP_ALIVE);
		RelTime keepAliveTimeout = config(GRPC_KEEP_ALIVE_TIMEOUT, DEFAULT_GRPC_KEEP_ALIVE_TIMEOUT);
		mcb.idleTimeout(idleTimeout.unitTime(), idleTimeout.timeUnit());
		mcb.keepAliveTime(keepAlive.unitTime(), keepAlive.timeUnit());
		mcb.keepAliveTimeout(keepAliveTimeout.unitTime(), keepAliveTimeout.timeUnit());		
		mcb.keepAliveWithoutCalls(config(GRPC_KEEP_ALIVE_NOCALLS, DEFAULT_GRPC_KEEP_ALIVE_NOCALLS));
		mcb.maxInboundMessageSize(config(GRPC_MAX_MESSAGE_SIZE, DEFAULT_GRPC_MAX_MESSAGE_SIZE));
		
		String authority = config(GRPC_AUTHORITY, (String)null);
		if(authority!=null) {
			mcb.overrideAuthority(authority);
		}
		mcb.userAgent(config(GRPC_USER_AGENT, DEFAULT_GRPC_USER_AGENT));
		
		
		mcb.channelType(NativeUtils.isEpoll() ? EpollSocketChannel.class : NioSocketChannel.class);
		mcb.flowControlWindow(config(GRPC_FLOW_CONTROL, DEFAULT_GRPC_FLOW_CONTROL));		
		mcb.maxHeaderListSize(config(GRPC_MAX_HEADERLIST_SIZE, DEFAULT_GRPC_MAX_HEADERLIST_SIZE));
		if(NativeUtils.isEpoll()) {
			mcb.channelType(EpollSocketChannel.class);
			mcb.withOption(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
		} else {
			mcb.channelType(NioSocketChannel.class);
		}
		mcb.withOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
	}
	
	public ManagedChannelBuilder<NettyChannelBuilder> configure(String host, int port) {
		NettyChannelBuilder mcb = NettyChannelBuilder.forAddress(
				config(GRPC_SERVER, DEFAULT_GRPC_SERVER), 
				config(GRPC_PORT, DEFAULT_GRPC_PORT)
		);
		return mcb;
		
	}
	
	
	/**
	 * Acquires a configuration value. Order of lookup is: <ol>
	 *  <li>System properties</li>
	 *  <li>Environmental variables</li>
	 * </ol>
	 * When looking up an environmental variable, they key is uppercased
	 * and dots are replaced with underscores. e.g. 
	 * The key <b><code>grpc.server.iface</code></b> is renamed to 
	 * <b><code>GRPC_SERVER_IFACE</code></b>.
	 * @param key The configuration key
	 * @param defaultValue The default value
	 * @return The configured value or the default if not defined
	 */
	public static String config(String key, String defaultValue) {
		String v = System.getProperty(key);
		if(v==null) {
			v = System.getenv(key.toUpperCase().replace('.', '_'));
			if(v==null) {
				v = defaultValue;
			}
		}
		if(v!=null) {
			CONFIGS.put(key, v);
		}
		return v;
	}
	
	/**
	 * Acquires a configuration value via {@link #config(String, String)}
	 * @param key The configuration key
	 * @param defaultValue The default value
	 * @return The configured value or the default if not defined
	 */
	public static int config(String key, int defaultValue) {
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
		CONFIGS.put(key, "" + i);
		return i;
	}
	
	/**
	 * Acquires a configuration value via {@link #config(String, String)}
	 * @param key The configuration key
	 * @param defaultValue The default value
	 * @return The configured value or the default if not defined
	 */
	public static long config(String key, long defaultValue) {
		String v = config(key, (String)null);
		long l = Long.MIN_VALUE;
		if(v==null || v.trim().isEmpty()) {
			l = defaultValue;
		} else {
			try {
				l = Long.parseLong(v.trim());
			} catch (Exception x) {
				l = defaultValue;
			}
		}
		CONFIGS.put(key, "" + l);
		return l;
	}
	
	
	/**
	 * Acquires a configuration value via {@link #config( String, String)}
	 * @param cfg The OpenTSDB configuration repo
	 * @param key The configuration key
	 * @param defaultValue The default value
	 * @return The configured value or the default if not defined
	 */
	public static RelTime config(String key, RelTime defaultValue) {
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
		CONFIGS.put(key, r.toString());
		return r;
	}
	
	/**
	 * Acquires a configuration value via {@link #config(String, String)}
	 * @param cfg The OpenTSDB configuration repo
	 * @param key The configuration key
	 * @param defaultValue The default value
	 * @return The configured value or the default if not defined
	 */
	public static boolean config(String key, boolean defaultValue) {
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
		CONFIGS.put(key, "" + b);
		return b;
	}
	
	


	
	private ClientConfiguration() {}

}
