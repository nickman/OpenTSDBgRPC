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

import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecwid.consul.v1.agent.AgentConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.stumbleupon.async.Deferred;

import io.netty.channel.unix.DomainSocketAddress;
import net.opentsdb.consul.utils.NetUtils;
import net.opentsdb.core.TSDB;
import net.opentsdb.plugin.common.Configuration;
import net.opentsdb.stats.StatsCollector;
import net.opentsdb.tools.StartupPlugin;
import net.opentsdb.utils.Config;

/**
 * <p>Title: ConsulPlugin</p>
 * <p>Description: OpenTSDB {@link StartupPlugin} to register TSDs in Consul</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.consul.ConsulPlugin</code></p>
 */

public class ConsulPlugin extends StartupPlugin {
	private final Logger LOG = LoggerFactory.getLogger(getClass());
	
	private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)(ns|us|ms|s|m|h)");
	private static final String HOSTNAME = ManagementFactory.getRuntimeMXBean().getName().split("@")[1];
	private static final String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

	protected int tsdPort = -1;
	protected int grpcPort = -1;
	protected String listenerAddress = null;
	protected String grpcListenerAddress = null;
	protected URI[] consuls = NetUtils.uris("localhost:8500");
	protected boolean read = false;
	protected boolean write = false;
	protected String checkInterval = "15s";
	protected String deregisterAfter = "2m";
	
	protected String httpServiceId = null;
	protected String grpcServiceId = null;
	
	protected String[] tsdAliases = new String[0];
	protected String[] grpcAliases = new String[0];
	
	protected Configuration cfg = null;
	
	protected final CloseableHttpClient httpclient = HttpClients.createDefault();
	
	protected final Set<String> tsdAliasIds = new HashSet<>();
	protected final Set<String> grpcAliasIds = new HashSet<>();
	
	
	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tools.StartupPlugin#initialize(net.opentsdb.utils.Config)
	 */
	@Override
	public Config initialize(Config config) {
		cfg = new Configuration(config);
		return config;
	}
	
	protected void lateInit(Config config) {
		tsdPort = config.getInt("tsd.network.port");
		consuls = NetUtils.uris(cfg.config("consul.endpoints", "localhost:8500"));
		
		listenerAddress = cfg.config("tsd.network.bind", (String)null);
		if(listenerAddress==null || NetUtils.isWildcard(listenerAddress)) {
			listenerAddress = NetUtils.getListenAddressOrLoopback();			
		}
		
		SocketAddress grpcSocketAddress = cfg.getGrpcSocketAddress();
		if(grpcSocketAddress instanceof DomainSocketAddress) {
			grpcPort = 0;
			//grpcListenerAddress = "unix://" + ((DomainSocketAddress)grpcSocketAddress).path();
			grpcListenerAddress = ((DomainSocketAddress)grpcSocketAddress).path();
		} else {
			InetSocketAddress isa = (InetSocketAddress) grpcSocketAddress;
			grpcPort = isa.getPort();
			grpcListenerAddress = listenerAddress;			
		}
		
		
		tsdAliases = cfg.config("tsd.consul.aliases", tsdAliases);
		grpcAliases = cfg.config("grpc.consul.aliases", grpcAliases);
		
		String modes = cfg.config("tsd.mode", "rw");
		read = modes.contains("r");
		write = modes.contains("w");
		
		checkInterval = cfg.config("consul.check.interval", "15s").trim();
		if(!DURATION_PATTERN.matcher(checkInterval).matches()) {
			throw new IllegalArgumentException("Invalid consul.check.interval: [" + checkInterval + "]");
		}
		
		deregisterAfter = cfg.config("consul.check.deregister", "2m").trim();
		if(!DURATION_PATTERN.matcher(deregisterAfter).matches()) {
			throw new IllegalArgumentException("Invalid consul.check.deregister: [" + deregisterAfter + "]");
		}
		LOG.info("TSD Port={}, gRPC Port={}, TSDAddress={}, gRPCAddress={}, Consuls={}", tsdPort, grpcPort, listenerAddress, grpcListenerAddress, Arrays.toString(consuls));
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tools.StartupPlugin#setReady(net.opentsdb.core.TSDB)
	 */
	@Override
	public void setReady(TSDB tsdb) {
		lateInit(tsdb.getConfig());
		register();
	}
	
	protected void register() {
		LOG.info("Registering Services in Consul....");
		try {
			httpServiceId = "http-opentsdb@" + HOSTNAME + "/" + listenerAddress + ":" + tsdPort;
			List<String> tags = new ArrayList<>();
			tags.add(HOSTNAME);
			tags.add("opentsdb");
			tags.add("http");
			tags.add("pid=" + PID);
			if(read) {
				tags.add("read");
			}
			if(write) {
				tags.add("write");
			}
			NewService.Check check = new NewService.Check();
			check.setHttp("http://" + listenerAddress + ":" + tsdPort + "/version");
			check.setInterval(checkInterval);
			check.setDeregisterCriticalServiceAfter(deregisterAfter);

			AgentConsulClient client = consul();
			NewService ns = new NewService();
			ns.setAddress(listenerAddress);
			ns.setPort(tsdPort);
			ns.setName("OpenTSDB");
			ns.setId(httpServiceId);
			ns.setTags(tags);
			ns.setEnableTagOverride(false);
			
			
			ns.setCheck(check);
			client.agentServiceRegister(ns);
			for(String alias : tsdAliases) {
				if(!"OpenTSDB".equals(alias)) {					
					String aliasId = httpServiceId + "-" + alias; 
					tsdAliasIds.add(aliasId);
					client.agentServiceRegister(alias(ns, alias, aliasId));
				}
			}
			LOG.info("Registered OpenTSDB HTTP Service: checkInterval={}, dereg={}, aliases={}", checkInterval, deregisterAfter, Arrays.toString(tsdAliases));
			
			if(grpcPort != -1) {
				grpcServiceId = ("grpc-opentsdb@" + HOSTNAME + "/" + grpcListenerAddress + ":" + grpcPort).replace("//", "/");
				ns = new NewService();
				if(grpcListenerAddress.startsWith("/")) {
					ns.setAddress(grpcListenerAddress);
				} else {
					ns.setAddress(grpcListenerAddress);
					ns.setPort(grpcPort);
				}					
				ns.setName("OpenTSDBGRPC");
				ns.setId(grpcServiceId);
				tags.add("grpc");
				tags.remove("http");
				ns.setTags(tags);
				ns.setEnableTagOverride(false);
				
				ns.setCheck(check);
				// TODO: Figure out how to register a grpc health check
				client.agentServiceRegister(ns);
				for(String alias : grpcAliases) {
					if(!"OpenTSDBGRPC".equals(alias)) {
						String aliasId = grpcServiceId + "-" + alias;
						grpcAliasIds.add(aliasId);
						client.agentServiceRegister(alias(ns, alias, aliasId));
					}
				}
				
				LOG.info("Registered OpenTSDB gRPC Service: checkInterval={}, dereg={}, aliases={}", checkInterval, deregisterAfter, Arrays.toString(grpcAliases));
			}
		} catch (Exception ex) {
			LOG.error("Failed to register", ex);
			throw new IllegalArgumentException("Failed to register", ex);
		}
	}
	
	protected NewService alias(NewService svc, String name, String id) {
		NewService ns = new NewService();
		ns.setName(name);
		ns.setId(id);
		ns.setAddress(svc.getAddress());
		ns.setPort(svc.getPort());		
		ns.setTags(svc.getTags());
		ns.setEnableTagOverride(false);
		ns.setCheck(svc.getCheck());		
		return ns;
	}
	
	protected AgentConsulClient consul() throws Exception {
		for(URI uri: consuls) {
			try {
				AgentConsulClient client = new AgentConsulClient(uri.getHost(), uri.getPort());
				client.getAgentSelf();
				return client;
			} catch (Exception x) {}
		}
		throw new Exception("Failed to connect to Consul at any of [" + Arrays.toString(consuls) + "]");
	}
	

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tools.StartupPlugin#shutdown()
	 */
	@Override
	public Deferred<Object> shutdown() {
		final Deferred<Object> def = new Deferred<>();
		Thread t = new Thread("ConsulDeregistration") {
			public void run() {
				LOG.info("Unregistering Consul Services");
				try {
					AgentConsulClient client = consul();
					if(httpServiceId != null) {
						deregister(client, httpServiceId);
						for(String aliasId : tsdAliasIds) {
							deregister(client, aliasId);
						}						
					}
					if(grpcServiceId != null) {
						deregister(client, grpcServiceId);
						for(String aliasId : grpcAliasIds) {
							deregister(client, aliasId);
						}
					}
					LOG.info("Consul Services Unregistered");
				} catch (Exception ex) {
					LOG.warn("Failed to deregister services", ex);
				} finally {
					try { httpclient.close(); } catch (Exception x) {} 
					def.callback(null);
				}
			}
		};
		t.setDaemon(true);
		t.start();
		return def;
	}
	
	protected void deregister(AgentConsulClient client, String id) {
		try {
			client.agentServiceDeregister(id);
			LOG.info("Deregistered OpenTSDB Service: id={}", id);	
		} catch (Exception ex) {
			LOG.warn("Failed to deregister service: id={}, err={}", id,  ex);
		}
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tools.StartupPlugin#version()
	 */
	@Override
	public String version() {
		return "1.0";		
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tools.StartupPlugin#getType()
	 */
	@Override
	public String getType() {
		return "2.4.0";
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tools.StartupPlugin#collectStats(net.opentsdb.stats.StatsCollector)
	 */
	@Override
	public void collectStats(StatsCollector collector) {

	}
	
	final String template = "    {" + 
			"      \"ID\": \"%s\"," + 
			"      \"Name\": \"%s\"," + 
			"      \"Tags\": [" + 
			"        \"primary\"," + 
			"        \"v2\"," + 
			"        \"envoy\"," + 
			"        \"pid=%s\"" + 
			"      ]," + 
			"      \"Address\": \"%s\"," + 
			"      \"Port\": %s," + 
			"      \"Check\": {" + 
			"        \"Name\" : \"Envoy Up Check\"," + 
			"        \"DeregisterCriticalServiceAfter\": \"1m\"," + 
			"        \"HTTP\": \"http://%s:%s/server_info\"," + 
			"        \"Method\": \"GET\"," + 
			"        \"Interval\": \"3s\"," + 
			"        \"TLSSkipVerify\": true" + 
			"      }" + 
			"    }";
	
//    {
//        "ID": "%s",
//        "Name": "envoy",
//        "Tags": [
//          "primary",
//          "v2",
//          "envoy",
//          "pid=%s"
//        ],
//        "Address": "%s",
//        "Port": %s,
//        "Check": {
//          "Name" : "Envoy Up Check",
//          "DeregisterCriticalServiceAfter": "1m",
//          "HTTP": "http://%s:%s/server_info",
//          "Method": "GET",
//          "Interval": "3s",
//          "TLSSkipVerify": true
//        }
//      }
	
	
	private void putCheck(String payload, String description) {
		for(URI uri : consuls) {
			
		}
		throw new IllegalArgumentException("Failed to register in Consul:" + description);
	}
	
	private void putCheck(URI uri, String payload) throws Exception {
		HttpPut put = new HttpPut(uri);
		put.setEntity(new StringEntity(payload));
		CloseableHttpResponse response = null;
		try {
			response = httpclient.execute(put);
			int code = response.getStatusLine().getStatusCode();
			if(code < 200 || code > 299) {
				throw new Exception("Failed to put payload:" + response.getStatusLine());
			}			
		} finally {
			try { response.close(); } catch (Exception x) {}
		}
	}

}
