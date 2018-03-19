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

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.internal.GrpcUtil;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
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
	
	public static final String CONSUL_SERVER = "consul.server";
	public static final String DEFAULT_CONSUL_SERVER = "localhost";
	
	public static final String CONSUL_PORT = "consul.server.port";
	public static final int DEFAULT_CONSUL_PORT = 8500;
	
	public static final String DISABLE_EPOLL = "epoll.disable";
	
	public static final String AVAILABLE_PROCESSORS = "io.netty.availableProcessors";
	
	public static final int ACTUAL_CORES = Runtime.getRuntime().availableProcessors();

	
	
	
	
	public static void main(String[] args) {
		System.out.println("v: " + DEFAULT_GRPC_USER_AGENT);
	}
	
	private final Properties configProps;
	private final NettyChannelBuilder builder;
	private boolean enableFullStreamDecompression = false;
	private boolean keepAliveWithoutCalls = false;
	
	private boolean epoll = true;
	
	private RelTime idleTimeout = null;
	private RelTime keepAlive = null;
	private RelTime keepAliveTimeout = null;
	
	private int maxInboundMessageSize = DEFAULT_GRPC_MAX_MESSAGE_SIZE;
	private int flowControlWindow = DEFAULT_GRPC_FLOW_CONTROL;
	private int maxHeaderListSize = DEFAULT_GRPC_MAX_HEADERLIST_SIZE;
	
	
	private String authority = null;
	private String userAgent = null;
	
	private Class<? extends Channel> channelType = null;
	
	private String host = null;
	private int port = -1;
	
	
	public Map<String, String> activeConfig() {
		return Collections.unmodifiableMap(CONFIGS);
	}
	
//	private static ManagedChannelBuilder<NettyChannelBuilder> builder(String host, int port) {
//		NettyChannelBuilder mcb = NettyChannelBuilder.forAddress(host, port);
//		configure(mcb);
//		return mcb;
//	}
//	
//	private static ManagedChannelBuilder<NettyChannelBuilder> builder(URI uri) {
//		NettyChannelBuilder mcb = NettyChannelBuilder.forAddress(uri.getHost(), uri.getPort());
//		configure(mcb);
//		return mcb;
//	}
//	
//	private static ManagedChannelBuilder<NettyChannelBuilder> builderFromConsul() {
//		return builder(new EnvoyFinder().getEnvoyServiceURI());
//	}
//	
//	private static ManagedChannelBuilder<NettyChannelBuilder> builderFromConsul(String host, int port) {
//		return builder(new EnvoyFinder(host, port).getEnvoyServiceURI());
//	}
//	
//	private static ManagedChannelBuilder<NettyChannelBuilder> builderFromConsul(URI uri) {
//		return builder(new EnvoyFinder(uri.getHost(), uri.getPort()).getEnvoyServiceURI());
//	}
//	
//	private static ManagedChannelBuilder<NettyChannelBuilder> builder(SocketAddress socketAddress) {
//		NettyChannelBuilder mcb = NettyChannelBuilder.forAddress(socketAddress);
//		configure(mcb);
//		return mcb;
//	}
	
	/**
	 * Determines if epoll is available.
	 * If we're on Linux, usually it is, but
	 * it can be disabled by setting {@link #GRPC_NO_EPOLL}
	 * to true.
	 * @return
	 */
	public boolean isEpoll() {
		if(config(DISABLE_EPOLL, false)) {
			return false;
		}
		return Epoll.isAvailable();
	}
	
	/**
	 * Returns the number of available cores
	 * which by default is reported from {@link Runtime#availableProcessors()}
	 * but can be overriden by setting {@link #AVAILABLE_PROCESSORS}.
	 * @return the number of available cores
	 */
	public int availableCores() {
		return config(AVAILABLE_PROCESSORS, ACTUAL_CORES);
	}

	private NettyChannelBuilder configure(NettyChannelBuilder mcb) {
		mcb.usePlaintext(true).directExecutor();
//		mcb.executor(Executors.newCachedThreadPool());
		enableFullStreamDecompression = config(GRPC_DECOMPRESS_INCOMING, DEFAULT_GRPC_DECOMPRESS_INCOMING);
		if(enableFullStreamDecompression) {
			mcb.enableFullStreamDecompression();
		}
		idleTimeout = config(GRPC_MAX_CONN_IDLE, DEFAULT_GRPC_MAX_CONN_IDLE);
		keepAlive = config(GRPC_KEEP_ALIVE, DEFAULT_GRPC_KEEP_ALIVE);
		keepAliveTimeout = config(GRPC_KEEP_ALIVE_TIMEOUT, DEFAULT_GRPC_KEEP_ALIVE_TIMEOUT);
		mcb.idleTimeout(idleTimeout.unitTime(), idleTimeout.timeUnit());
		mcb.keepAliveTime(keepAlive.unitTime(), keepAlive.timeUnit());
		mcb.keepAliveTimeout(keepAliveTimeout.unitTime(), keepAliveTimeout.timeUnit());
//		
		keepAliveWithoutCalls = config(GRPC_KEEP_ALIVE_NOCALLS, DEFAULT_GRPC_KEEP_ALIVE_NOCALLS); 
		mcb.keepAliveWithoutCalls(keepAliveWithoutCalls);
		
		maxInboundMessageSize = config(GRPC_MAX_MESSAGE_SIZE, DEFAULT_GRPC_MAX_MESSAGE_SIZE);
		mcb.maxInboundMessageSize(maxInboundMessageSize);
//		
		authority = config(GRPC_AUTHORITY, (String)null);
		if(authority!=null) {
			mcb.overrideAuthority(authority);
		}
//		
		userAgent = config(GRPC_USER_AGENT, DEFAULT_GRPC_USER_AGENT);
		mcb.userAgent(userAgent);
//		
//		epoll = isEpoll();
//		channelType = epoll ? EpollSocketChannel.class : NioSocketChannel.class;
//		mcb.channelType(channelType);
//		if(epoll) {
//			mcb.eventLoopGroup(new EpollEventLoopGroup(availableCores() * 2, new ThreadFactory() {
//				final AtomicInteger serial = new AtomicInteger();
//				@Override
//				public Thread newThread(Runnable r) {
//					Thread t = new Thread("EpollEventLoopGroupThread#" + serial.incrementAndGet());
//					t.setDaemon(true);
//					return t;
//				}
//			}));
//			mcb.withOption(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
//		} else {
//			mcb.eventLoopGroup(new NioEventLoopGroup(availableCores() * 2, new ThreadFactory() {
//				final AtomicInteger serial = new AtomicInteger();
//				@Override
//				public Thread newThread(Runnable r) {
//					Thread t = new Thread("NioEventLoopGroupThread#" + serial.incrementAndGet());
//					t.setDaemon(true);
//					return t;
//				}
//			}));
//
//		}
//		
		flowControlWindow = config(GRPC_FLOW_CONTROL, DEFAULT_GRPC_FLOW_CONTROL);
		mcb.flowControlWindow(flowControlWindow);
		
		maxHeaderListSize = config(GRPC_MAX_HEADERLIST_SIZE, DEFAULT_GRPC_MAX_HEADERLIST_SIZE);
		mcb.maxHeaderListSize(maxHeaderListSize);
		
		mcb.withOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
//		
		return mcb;
	}
	
	public ManagedChannelBuilder<NettyChannelBuilder> configure(String host, int port) {
		NettyChannelBuilder mcb = NettyChannelBuilder.forAddress(
				config(GRPC_SERVER, DEFAULT_GRPC_SERVER), 
				config(GRPC_PORT, DEFAULT_GRPC_PORT)
		);
		return mcb;
		
	}
	
	public ManagedChannel build() {
		return builder.build();
	}
	
	
	/**
	 * Acquires a configuration value. Order of lookup is: <ol>
	 * 	<li>The passed properties</li>
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
	public static String config(String key, String defaultValue, Properties props) {
		String v = null;
		Object o = null;
		if(props!=null) {
			o = props.get(key);
			if(o != null) {
				v = o.toString();
			}
		}
		if(v==null) {
			v = System.getProperty(key);
			if(v==null) {
				v = System.getenv(key.toUpperCase().replace('.', '_'));
				if(v==null) {
					v = defaultValue;
				}
			}
		}
		if(v!=null) {
			CONFIGS.put(key, v);
		}
		return v;
	}
	
	public String config(String key, String defaultValue) {
		return config(key, defaultValue, configProps);
	}
	
	/**
	 * Acquires a configuration value via {@link #config(String, String)}
	 * @param key The configuration key
	 * @param defaultValue The default value
	 * @return The configured value or the default if not defined
	 */
	public static int config(String key, int defaultValue, Properties props) {
		String v = config(key, (String)null, props);
		int i;
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
	
	public int config(String key, int defaultValue) {
		return config(key, defaultValue, configProps);
	}
	
	
	/**
	 * Acquires a configuration value via {@link #config(String, String)}
	 * @param key The configuration key
	 * @param defaultValue The default value
	 * @return The configured value or the default if not defined
	 */
	public static long config(String key, long defaultValue, Properties props) {
		String v = config(key, (String)null, props);
		long l;
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
	
	public long config(String key, long defaultValue) {
		return config(key, defaultValue, configProps);
	}
	
	
	/**
	 * Acquires a configuration value via {@link #config( String, String)}
	 * @param cfg The OpenTSDB configuration repo
	 * @param key The configuration key
	 * @param defaultValue The default value
	 * @return The configured value or the default if not defined
	 */
	public static RelTime config(String key, RelTime defaultValue, Properties props) {
		String v = config(key, (String)null, props);
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
	
	public RelTime config(String key, RelTime defaultValue) {
		return config(key, defaultValue, configProps);
	}
	
	/**
	 * Acquires a configuration value via {@link #config(String, String)}
	 * @param cfg The OpenTSDB configuration repo
	 * @param key The configuration key
	 * @param defaultValue The default value
	 * @return The configured value or the default if not defined
	 */
	public static boolean config(String key, boolean defaultValue, Properties props) {
		String v = config(key, (String)null, props);
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
	
	public boolean config(String key, boolean defaultValue) {
		return config(key, defaultValue, configProps);
	}
	
	private static URI fromConsul(String host, int port) {
		EnvoyFinder ef = new EnvoyFinder(host, port);
		return ef.getEnvoyServiceURI();
	}
	
	private static URI fromConsul() {
		return new EnvoyFinder().getEnvoyServiceURI();
	}
	
	private static URI fromConsul(Properties props) {
		return fromConsul(
			config(CONSUL_SERVER, DEFAULT_CONSUL_SERVER, props),
			config(CONSUL_PORT, DEFAULT_CONSUL_PORT, props)
		);
	}

	public static ClientConfiguration clientConfiguration() {
		return new ClientConfiguration(new Properties());
	}
	
	public static ClientConfiguration clientConfiguration(Properties props) {
		return new ClientConfiguration(props);
	}
	
	public static ClientConfiguration clientConfiguration(String resource) {
		return clientConfiguration(URLHelper.fromURL(resource));
	}
	
	public static ClientConfiguration clientConfiguration(String host, int port) {
		return clientConfiguration(new Properties(), host, port);
	}
	
	public static ClientConfiguration clientConfiguration(Properties configProps, String host, int port) {
		configProps.put(GRPC_SERVER, host);
		configProps.put(GRPC_PORT, port);
		return clientConfiguration(configProps);
	}
	
	public static ClientConfiguration clientConfiguration(String resource, String host, int port) {
		Properties configProps = URLHelper.fromURL(resource);
		configProps.put(GRPC_SERVER, host);
		configProps.put(GRPC_PORT, port);
		return clientConfiguration(configProps);
	}

	private ClientConfiguration(Properties configProps) {
		this.configProps = configProps;
		host = config(GRPC_SERVER, DEFAULT_GRPC_SERVER);
		port = config(GRPC_PORT, DEFAULT_GRPC_PORT);
		builder = NettyChannelBuilder.forAddress(host, port);
		configure(builder);
	}

	public boolean isEnableFullStreamDecompression() {
		return enableFullStreamDecompression;
	}

	public boolean isKeepAliveWithoutCalls() {
		return keepAliveWithoutCalls;
	}

	public RelTime getIdleTimeout() {
		return idleTimeout;
	}

	public RelTime getKeepAlive() {
		return keepAlive;
	}

	public RelTime getKeepAliveTimeout() {
		return keepAliveTimeout;
	}

	public int getMaxInboundMessageSize() {
		return maxInboundMessageSize;
	}

	public int getFlowControlWindow() {
		return flowControlWindow;
	}

	public int getMaxHeaderListSize() {
		return maxHeaderListSize;
	}

	public String getAuthority() {
		return authority;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public Class<? extends Channel> getChannelType() {
		return channelType;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

}
