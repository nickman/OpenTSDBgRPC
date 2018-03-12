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
package net.opentsdb.grpc.client.envoy;

import java.net.URI;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ecwid.consul.v1.agent.AgentConsulClient;
import com.ecwid.consul.v1.agent.model.Service;

/**
 * <p>Title: EnvoyFinder</p>
 * <p>Description: Finds envoy instances in Consul</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.client.envoy.EnvoyFinder</code></p>
 */

public class EnvoyFinder {
	private static final Logger LOG = LoggerFactory.getLogger(EnvoyFinder.class);
	private final String host;
	private final int port;
	private final AgentConsulClient client;
	
	/**
	 * Creates a new EnvoyFinder
	 * @param host The consul host
	 * @param port The consul port
	 */
	public EnvoyFinder(String host, int port) {
		this.host = host;
		this.port = port;
		LOG.info("EnvoyFinder: host={}, port={}", host, port);
		client = new AgentConsulClient(host, port);
	}
	
	/**
	 * Creates a new EnvoyFinder for Consul at <b>localhost:8500</b>
	 */
	public EnvoyFinder() {
		this("localhost", 8500);
	}
	
	/**
	 * Retrieves all registered services from the connected Consul instance
	 * @return A map of services keyed by the service id
	 */
	public Map<String, Service> getServices() {
		LOG.info("Fetching services from consul//{}:{}", host, port);
		Map<String, Service> map = client.getAgentServices().getValue();
		LOG.info("Retrieved {} services from consul//{}:{}", map.size(), host, port);
		return map;
	}
	
	/**
	 * Retrieves all registered services from the connected Consul instance
	 * with serice names equal to the passed name
	 * @param name The name of services to retrieve
	 * @return A map of services keyed by the service id
	 */
	public Map<String, Service> getServices(String name) {
		LOG.info("Fetching services from consul//{}:{} with the name: [{}]", host, port, name);
		return client.getAgentServices().getValue().entrySet().stream()
			.filter(entry -> entry.getValue().getService().equals(name))
			.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
	}
	
	/**
	 * Retrieves all registered services from the connected Consul instance
	 * filtering with the passed Service predicate
	 * @param predicate A predicate to filter services
	 * @return A map of services keyed by the service id
	 */
	public Map<String, Service> getServices(Predicate<Service> pred) {
		LOG.info("Fetching filtered services from consul//{}:{} ", host, port);
		return client.getAgentServices().getValue().entrySet().stream()
			.filter(entry -> pred.test(entry.getValue()))
			.collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));
	}
	
	
	public Map<String, Service> getEnvoyServices() {
		LOG.info("Fetching Evoy services from consul//{}:{}", host, port);
		return getServices("envoy");
	}
	
	/**
	 * Returns the first located envoy service
	 * @return the first located envoy service
	 */
	public Service getEnvoyService() {
		LOG.info("Fetching Evoy services from consul//{}:{}", host, port);
		Map<String, Service> map = getServices("envoy");
		if(map.isEmpty()) {
			throw new IllegalStateException("No envoy services found");
		}
		return map.values().iterator().next();
	}
	
	/**
	 * Returns the first located envoy service as a URI
	 * @return the first located envoy service
	 */
	public URI getEnvoyServiceURI() {
		LOG.info("Fetching Evoy URI from consul//{}:{}", host, port);
		Map<String, Service> map = getServices("envoy");
		if(map.isEmpty()) {
			throw new IllegalStateException("No envoy services found");
		}
		Service service = map.values().iterator().next();
		return toURI(service);
	}

	
	public static URI toURI(Service service) {
		String host = service.getAddress();
		int port = service.getPort();
		if(port==0 && host.startsWith("tcp://")) {
			try {
				return toURI(host);
			} catch (Exception ex) {}
		}
		return toURI("tcp://" + host + ":" + port);
	}
	
	private static URI toURI(String str) {
		try {
			return new URI(str.trim());
		} catch (Exception ex) {
			throw new IllegalArgumentException("Invalid URI: [" + str + "]", ex);
		}
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LOG.info("Consul Test");
		EnvoyFinder finder = new EnvoyFinder();
		Map<String, Service> services = finder.getServices();
		StringBuilder b = new StringBuilder("\nServices:");
		for(Map.Entry<String, Service> entry : services.entrySet()) {
			Service svc = entry.getValue();
			b.append("\n\t").append(entry.getKey()).append(" : ").append(svc.getService());
			b.append("\n\t\tAddress: ").append(svc.getAddress()).append(":").append(svc.getPort());
			b.append("\n\t\tTags: ");
			for(String tag : svc.getTags()) {
				b.append("\n\t\t\t").append(tag);
			}
		}
		LOG.info(b.toString());
		LOG.info("First Envoy Service: {}", finder.getEnvoyServiceURI());

	}

}
