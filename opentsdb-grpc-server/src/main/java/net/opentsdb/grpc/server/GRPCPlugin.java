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
package net.opentsdb.grpc.server;

import static net.opentsdb.grpc.server.Configuration.DEFAULT_GRPC_FLOW_CONTROL;
import static net.opentsdb.grpc.server.Configuration.DEFAULT_GRPC_KEEP_ALIVE;
import static net.opentsdb.grpc.server.Configuration.DEFAULT_GRPC_KEEP_ALIVE_TIMEOUT;
import static net.opentsdb.grpc.server.Configuration.DEFAULT_GRPC_MAX_CALLS_PER_CONN;
import static net.opentsdb.grpc.server.Configuration.DEFAULT_GRPC_MAX_CONN_AGE;
import static net.opentsdb.grpc.server.Configuration.DEFAULT_GRPC_MAX_CONN_AGE_GRACE;
import static net.opentsdb.grpc.server.Configuration.DEFAULT_GRPC_MAX_CONN_IDLE;
import static net.opentsdb.grpc.server.Configuration.DEFAULT_GRPC_MAX_HEADERLIST_SIZE;
import static net.opentsdb.grpc.server.Configuration.DEFAULT_GRPC_MAX_MESSAGE_SIZE;
import static net.opentsdb.grpc.server.Configuration.DEFAULT_GRPC_PERMIT_KEEP_ALIVE;
import static net.opentsdb.grpc.server.Configuration.DEFAULT_GRPC_PERMIT_KEEP_ALIVE_TIME;
import static net.opentsdb.grpc.server.Configuration.DEFAULT_IFACE;
import static net.opentsdb.grpc.server.Configuration.DEFAULT_PORT;
import static net.opentsdb.grpc.server.Configuration.GRPC_FLOW_CONTROL;
import static net.opentsdb.grpc.server.Configuration.GRPC_IFACE;
import static net.opentsdb.grpc.server.Configuration.GRPC_KEEP_ALIVE;
import static net.opentsdb.grpc.server.Configuration.GRPC_KEEP_ALIVE_TIMEOUT;
import static net.opentsdb.grpc.server.Configuration.GRPC_MAX_CALLS_PER_CONN;
import static net.opentsdb.grpc.server.Configuration.GRPC_MAX_CONN_AGE;
import static net.opentsdb.grpc.server.Configuration.GRPC_MAX_CONN_AGE_GRACE;
import static net.opentsdb.grpc.server.Configuration.GRPC_MAX_CONN_IDLE;
import static net.opentsdb.grpc.server.Configuration.GRPC_MAX_HEADERLIST_SIZE;
import static net.opentsdb.grpc.server.Configuration.GRPC_MAX_MESSAGE_SIZE;
import static net.opentsdb.grpc.server.Configuration.GRPC_PERMIT_KEEP_ALIVE;
import static net.opentsdb.grpc.server.Configuration.GRPC_PERMIT_KEEP_ALIVE_TIME;
import static net.opentsdb.grpc.server.Configuration.GRPC_PORT;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stumbleupon.async.Deferred;

import io.grpc.Server;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollMode;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.opentsdb.core.TSDB;
import net.opentsdb.grpc.server.netty.EventLoopThreadFactory;
import net.opentsdb.grpc.server.util.RelTime;
import net.opentsdb.stats.StatsCollector;
import net.opentsdb.tsd.RpcPlugin;
import net.opentsdb.utils.Config;

/**
 * <p>Title: GRPCPlugin</p>
 * <p>Description: The OpenTSDB plugin that initializes the gRPC server</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.GRPCPlugin</code></p>
 */

public class GRPCPlugin extends RpcPlugin {
	private static Logger LOG = LoggerFactory.getLogger(GRPCPlugin.class);
	private TSDB tsdb;
	private Config config;
	private OpenTSDBServer server;
	private Configuration cfg;
	private boolean epoll = true;
	
	private EventLoopThreadFactory bossThreadFactory;
	private EventLoopThreadFactory workerThreadFactory;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	
	private SocketAddress sa;
	
	private Server nettyServer;
	
	private final AtomicBoolean started = new AtomicBoolean(false);
	
	
	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tsd.RpcPlugin#initialize(net.opentsdb.core.TSDB)
	 */
	@Override
	public void initialize(TSDB tsdb) {
		LOG.info("Initializing OpenTSDB gRPC Server....");
		this.tsdb = tsdb;
		config = tsdb.getConfig();
		cfg = new Configuration(config);
		epoll = cfg.isEpoll();
		server = new OpenTSDBServer(tsdb, cfg);
		try {
			initServer();
			start();
			logConfig();
		} catch (Exception ex) {
			throw new IllegalArgumentException("Failed to start GRPCPlugin", ex);
		}
	}
	
	private void logConfig() {
		if(LOG.isDebugEnabled()) {
			StringBuilder b = new StringBuilder("\n\t=====================\n\tConfiguration\n\t=====================\n");
			TreeMap<String, String> tmap = new TreeMap<>(config.getMap());
			for(Map.Entry<String, String> entry: tmap.entrySet()) {
				b.append("\n\t").append(entry.getKey()).append(":").append(entry.getValue());
			}
			LOG.debug(b.toString());
		}		
	}
	
	public static void main(String[] args) {
		try {
			
			GRPCPlugin plugin = new GRPCPlugin();
			Config config = new Config("./src/test/resources/config/test.conf");
			plugin.initialize(new TSDB(config));
			Thread.sleep(5000);
			plugin.tsdb.shutdown();
			plugin.shutdown().joinUninterruptibly();
			
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}
	}
	
	private void initServer() {
		LOG.info("Creating Netty gRPC Server....");
		String iface = cfg.config(GRPC_IFACE, DEFAULT_IFACE);
		int port = cfg.config(GRPC_PORT, DEFAULT_PORT);
		sa = new InetSocketAddress(iface, port);
		eventLoopGroups();
		RelTime keepAlive = cfg.config(GRPC_KEEP_ALIVE, DEFAULT_GRPC_KEEP_ALIVE);
		RelTime keepAliveTimeout = cfg.config(GRPC_KEEP_ALIVE_TIMEOUT, DEFAULT_GRPC_KEEP_ALIVE_TIMEOUT);
		RelTime keepAlivePermit = cfg.config(GRPC_PERMIT_KEEP_ALIVE_TIME, DEFAULT_GRPC_PERMIT_KEEP_ALIVE_TIME);
		RelTime maxConnAge = cfg.config(GRPC_MAX_CONN_AGE, DEFAULT_GRPC_MAX_CONN_AGE);
		RelTime maxConnAgeGrace = cfg.config(GRPC_MAX_CONN_AGE_GRACE, DEFAULT_GRPC_MAX_CONN_AGE_GRACE);
		RelTime maxConnIdle = cfg.config(GRPC_MAX_CONN_IDLE, DEFAULT_GRPC_MAX_CONN_IDLE);		
		NettyServerBuilder serverBuilder = NettyServerBuilder.forAddress(sa);
		serverBuilder
			.addService(server)
			.addService(ProtoReflectionService.newInstance())
			.bossEventLoopGroup(bossGroup)
			.workerEventLoopGroup(workerGroup)
			.channelType(cfg.isEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
			.directExecutor()
			.keepAliveTime(keepAlive.unitTime(), keepAlive.timeUnit())
			.keepAliveTimeout(keepAliveTimeout.unitTime(), keepAliveTimeout.timeUnit())
			.permitKeepAliveTime(keepAlivePermit.unitTime(), keepAlivePermit.timeUnit())
			.maxConnectionAge(maxConnAge.unitTime(), maxConnAge.timeUnit())
			.maxConnectionAgeGrace(maxConnAgeGrace.unitTime(), maxConnAgeGrace.timeUnit())
			.maxConnectionIdle(maxConnIdle.unitTime(), maxConnIdle.timeUnit())
			.flowControlWindow(cfg.config(GRPC_FLOW_CONTROL, DEFAULT_GRPC_FLOW_CONTROL))
			.maxHeaderListSize(cfg.config(GRPC_MAX_HEADERLIST_SIZE, DEFAULT_GRPC_MAX_HEADERLIST_SIZE))
			.maxMessageSize(cfg.config(GRPC_MAX_MESSAGE_SIZE, DEFAULT_GRPC_MAX_MESSAGE_SIZE))
			.permitKeepAliveWithoutCalls(cfg.config(GRPC_PERMIT_KEEP_ALIVE, DEFAULT_GRPC_PERMIT_KEEP_ALIVE))
			.maxConcurrentCallsPerConnection(cfg.config(GRPC_MAX_CALLS_PER_CONN, DEFAULT_GRPC_MAX_CALLS_PER_CONN));
		if(epoll) {
			serverBuilder.withChildOption(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
		}
		nettyServer = serverBuilder.build();
		LOG.info("Netty gRPC Server Created");			
	}
	
	/**
	 * Starts the netty grpc server
	 */
	public void start() {
		if(started.compareAndSet(false, true)) {
			try {
				LOG.info("Starting Netty gRPC Server....");
				nettyServer.start();
				LOG.info("Netty gRPC Server Started on {}", sa);
			} catch (Exception ex) {
				LOG.error("Failed to start gRPC Server", ex);
				try { nettyServer.shutdownNow(); } catch (Exception x) {}
				forceCloseEventLoopGroups();
				throw new IllegalArgumentException("Failed to start gRPC Server", ex);
			}
		}
	}
	
	
	/**
	 * Initializes the netty server event loop groups
	 */
	private void eventLoopGroups() {
		bossThreadFactory = new EventLoopThreadFactory("boss");
		workerThreadFactory = new EventLoopThreadFactory("worker");
		bossGroup = cfg.isEpoll() ?
				new EpollEventLoopGroup(cfg.bossThreads(), bossThreadFactory) :
				new NioEventLoopGroup(cfg.bossThreads(), bossThreadFactory);
		workerGroup = cfg.isEpoll() ?
				new EpollEventLoopGroup(cfg.workerThreads(), workerThreadFactory) :
				new NioEventLoopGroup(cfg.workerThreads(), workerThreadFactory);
		
	}
	
	/**
	 * Forces shutdown on the event loop groups
	 */
	private void forceCloseEventLoopGroups() {
		try { 
			bossGroup.shutdownGracefully(0L, 0L, TimeUnit.NANOSECONDS).get(); 
		} catch (Exception x) {}
		try { 
			workerGroup.shutdownGracefully(0L, 0L, TimeUnit.NANOSECONDS).get(); 
		} catch (Exception x) {}		
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tsd.RpcPlugin#shutdown()
	 */
	@Override
	public Deferred<Object> shutdown() {
		final Deferred<Object> def = new Deferred<Object>();
		if(started.compareAndSet(true, false)) {
			LOG.info("Stopping Netty gRPC Server ....");
			nettyServer.shutdown();
			new Thread("NettygRPCShutdownThread") {
				public void run() {
					try {
						nettyServer.awaitTermination(15, TimeUnit.SECONDS);
					} catch (InterruptedException iex) {
						LOG.warn("Shutdown thread interrupted while awaiting termination");
					} finally {
						if(!nettyServer.isTerminated()) {
							LOG.warn("Forcing Netty gRPC Server Shutdown");
							nettyServer.shutdownNow();							
						} else {
							LOG.info("Netty gRPC Server Shutdown was clean");
						}
						forceCloseEventLoopGroups();
						LOG.info("Netty gRPC Server Shutdown Complete");
						def.callback(null);
					}
				}
			}.start();
		}
		return def;
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tsd.RpcPlugin#version()
	 */
	@Override
	public String version() {
		return "2.4.0";
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tsd.RpcPlugin#collectStats(net.opentsdb.stats.StatsCollector)
	 */
	@Override
	public void collectStats(StatsCollector collector) {
		try {			
			server.collectStats(collector);
		} finally {
			
		}
	}

}
