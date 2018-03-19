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
package net.opentsdb.grpc.client.monitor;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.helios.nativex.sigar.HeliosSigar;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.MultiProcCpu;
import org.hyperic.sigar.NetConnection;
import org.hyperic.sigar.NetFlags;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.NetStat;
import org.hyperic.sigar.ProcMem;
import org.hyperic.sigar.Swap;
import org.hyperic.sigar.Tcp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.opentsdb.grpc.PutDatapoints;
import net.opentsdb.grpc.PutDatapointsResponse;
import net.opentsdb.grpc.client.DatapointBatcher;
import net.opentsdb.grpc.client.OpenTSDBClient;
import net.opentsdb.grpc.client.streaming.BidiStreamer;



/**
 * <p>Title: OSMonitor</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.client.monitor.OSMonitor</code></p>
 */

public class OSMonitor {
	protected final Logger LOG = LoggerFactory.getLogger(getClass());
	protected final HeliosSigar sigar = HeliosSigar.getInstance();
	protected final OpenTSDBClient client;
	protected BidiStreamer<PutDatapoints,PutDatapointsResponse> streamer = null;
	protected final DatapointBatcher batcher;
	protected final PushPopMap<String, String> tags = new PushPopMap<>();
	protected final LongAdder pending = new LongAdder();
	protected final Map<String, String> processQueries = new HashMap<>();
	/**
	 * Creates a new OSMonitor
	 */
	public OSMonitor(String host, int port) {
		LOG.info("Starting OpenTSDBClient({}, {})", host, port);
		client = OpenTSDBClient.newInstance(host, port).open();
		streamer = client.putDatapoints(this::onResponse).start();
		batcher = new DatapointBatcher(streamer);
		LOG.info("Ready");
		tags.push("host", ManagementFactory.getRuntimeMXBean().getName().split("@")[1])
			.push("app", "os");
		processQueries.put("java", "State.Name.ew=java");
		processQueries.put("envoy", "State.Name.ew=envoy");
	}
	
	public void onResponse(PutDatapointsResponse response) {		
		long total = response.getFailed() + response.getSuccess();
		pending.add(-total);
		LOG.info("Received Response. total={}, now-pending={}", total, pending.longValue());
		if(response.getFailed() > 0) {
			System.err.println("Failed points: " + response.getFailed());
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OSMonitor os = new OSMonitor("localhost", 10000);
		final boolean flush = false;
		Thread t = new Thread() {
			public void run() {
				while(true) {
					try {
						os.collectCpu(flush);
						os.collectFs(flush);
						os.collectIfaces(flush);
						os.collectNetstats(flush);
						os.collectSystemMem(flush);
						os.collectSwap(flush);
						os.collectServerSockets(flush);
						os.collectClientSockets(flush);
						os.processQueries(flush);
						if(!flush) {
							os.flush();
						}
						Thread.sleep(5000);
					} catch (Exception ex) {
						os.LOG.error("Collection Error", ex);
					}
				}
			}
		};
		t.setDaemon(true);
		t.start();
		try { 
			System.in.read();
			t.interrupt();
		} catch (Exception ex) {}
		os.LOG.info("Done");
		try { os.client.close(); } catch (Exception x) {}
		
	}
	
	public void trace(String metric, double value, Map<String, String> tagPairs) {
		batcher.record(metric, value, tagPairs);
	}
	
	public void bytesTrace(String metric, double value, Map<String, String> tagPairs) {
		Map<String, String> tags = new HashMap<>(tagPairs);
		batcher.record(metric, value, tags);
		tags.put("unit", "bytesKb");
		double v = div(value,1024);
		batcher.record(metric, v, tags);
		tags.put("unit", "bytesMb");
		v = div(v,1024);
		batcher.record(metric, v, tags);		
	}
	
	private double div(double v, double d) {
		return v/d;
	}
	
	public void ptrace(String metric, double value, Map<String, String> tagPairs) {
		LOG.info("{}{}: {}", metric, tagPairs, value);
		trace(metric, value, tagPairs);
	}
	
	
	public void flush() {
		int points = batcher.flush();
		LOG.info("Batch sent: total={}", points);
		pending.add(points);		
	}
	
	protected void collectServerSockets(boolean flush) throws Exception {
		collectSockets("server", NetFlags.CONN_SERVER | NetFlags.CONN_PROTOCOLS, n -> {
			try {
				return n.getLocalAddress();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
		if(flush) flush();
	}
	
	protected void collectClientSockets(boolean flush) throws Exception {
		collectSockets("client", NetFlags.CONN_CLIENT | NetFlags.CONN_PROTOCOLS, n -> {
			try {
				return n.getRemoteAddress();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		});
		if(flush) flush();
	}
	
	
	protected void collectSockets(String type, int connFlags, Function<NetConnection, String> fx) throws Exception {
		boolean client = type.equals("client");
		TreeMap<String, TreeMap<String, TreeMap<String, AtomicInteger>>> connMap = new TreeMap<>();
        NetConnection[] conns = sigar.getNetConnectionList(connFlags);
        final long[] receiveQueue = new long[1];
        final long[] sendQueue = new long[1];
        Arrays.stream(conns).forEach(it -> {
        	try {
        		receiveQueue[0] += it.getReceiveQueue();
        		sendQueue[0] += it.getSendQueue();
	        	String addr = InetAddress.getByName(fx.apply(it)).getHostAddress();
	            String sock = addr + ":" + (client ? "" : it.getLocalPort());
	            String state = it.getStateString();
	            String protocol = it.getTypeString();
	            TreeMap<String, TreeMap<String, AtomicInteger>> stateMap = connMap.computeIfAbsent(sock, s -> new TreeMap<String, TreeMap<String, AtomicInteger>>());
	            TreeMap<String, AtomicInteger> protocolMap = stateMap.computeIfAbsent(state, s -> new TreeMap<String, AtomicInteger>());
	            AtomicInteger counter = protocolMap.computeIfAbsent(protocol, p -> new AtomicInteger());
	            counter.incrementAndGet();
        	} catch (Exception ex) {}
        });
        trace("sys.net." + type, receiveQueue[0], p("queue", "receive"));        
        trace("sys.net." + type, sendQueue[0], p("queue", "send"));
        tags.pop();
        for(Map.Entry<String, TreeMap<String, TreeMap<String, AtomicInteger>>> connEntry : connMap.entrySet()) {
        	String sock = connEntry.getKey();
        	for(Map.Entry<String, TreeMap<String, AtomicInteger>> stateEntry : connEntry.getValue().entrySet()) {
        		String state = stateEntry.getKey();
        		for(Map.Entry<String, AtomicInteger> protocolEntry : stateEntry.getValue().entrySet()) {
        			String protocol = protocolEntry.getKey();
        			AtomicInteger counter = protocolEntry.getValue();
        			String[] sockParts = sock.split(":");
        			if(client) {
            			//LOG.info("{}: addr={}, state={}, protocol={}, count={}", type, sockParts[0], state, protocol, counter.get());
            			trace("sys.net." + type, counter.get(), p("protocol",protocol, "state",state.toLowerCase(), "bind",sockParts[0]));
        			} else {
            			//LOG.info("{}: addr={}, port={}, state={}, protocol={}, count={}", type, sockParts[0], sockParts[1], state, protocol, counter.get());
            			trace("sys.net." + type, counter.get(), p("protocol",protocol, "state",state.toLowerCase(), "port",sockParts[1], "bind",sockParts[0]));        				
        			}
        		}
        	}
        }
        tags.clear();
	}

	protected void processQueries(boolean flush) throws Exception {
		for(Map.Entry<String, String> entry : processQueries.entrySet()) {
			String name = entry.getKey();
			String pql = entry.getValue();
			MultiProcCpu mcpu = sigar.getMultiProcCpu(pql);
            trace("procs", mcpu.getPercent() * 100, p("exe",name, "unit","percentcpu"));
            trace("procs", mcpu.getProcesses(), p("exe",name, "unit","count"));            
            ProcMem mmem = sigar.getMultiProcMem(pql);

            trace("procs", mmem.getMajorFaults(), p("exe", name, "unit","majorfaults"));
            trace("procs", mmem.getMinorFaults(), p("exe", name, "unit","minorfaults"));            
            trace("procs", mmem.getPageFaults(), p("exe", name, "unit","pagefaults"));            
            trace("procs", mmem.getResident(), p("exe", name, "unit","resident"));            
            trace("procs", mmem.getShare(), p("exe", name, "unit","share"));            
            trace("procs", mmem.getSize(), p("exe", name, "unit","size"));            
            trace("procs", mmem.getSize(), p("exe", name, "unit","size"));            
		}
		tags.pop(2);
		if(flush) flush();
    }

	
	protected void collectCpu(boolean flush) throws Exception {
		CpuPerc[] cpus = sigar.getCpuPercList();
		IntStream.range(0, cpus.length).forEach(index -> {
			CpuPerc cpu = cpus[index];
	        trace("sys.cpu", cpu.getCombined()*100, p("cpu",index, "type","combined"));
	        trace("sys.cpu", cpu.getIdle()*100, p("cpu", index, "type", "idle"));
	        trace("sys.cpu", cpu.getIrq()*100, p("cpu", index, "type", "irq"));
	        trace("sys.cpu", cpu.getNice()*100, p("cpu", index, "type", "nice"));
	        trace("sys.cpu", cpu.getSoftIrq()*100, p("cpu", index, "type", "softirq"));
	        trace("sys.cpu", cpu.getStolen()*100, p("cpu", index, "type", "stolen"));
	        trace("sys.cpu", cpu.getSys()*100, p("cpu", index, "type", "sys"));
	        trace("sys.cpu", cpu.getUser()*100, p("cpu", index, "type", "user"));
	        trace("sys.cpu", cpu.getWait()*100, p("cpu", index, "type", "wait"));            
		});
		tags.pop(2);
		if(flush) flush();
    }
	
	protected void collectFs(boolean flush) throws Exception {
		Arrays.stream(sigar.getFileSystemList()).forEach(fs -> {
			try {
				FileSystemUsage fsu = sigar.getFileSystemUsage(fs.getDirName());
				trace("sys.fs.avail", fsu.getAvail(), p("name",fs.getDirName(), "type",fs.getSysTypeName()));
				trace("sys.fs.queue", fsu.getDiskQueue(), p("name",fs.getDirName(), "type",fs.getSysTypeName()));
				trace("sys.fs.files", fsu.getFiles(), p("name",fs.getDirName(), "type",fs.getSysTypeName()));
				trace("sys.fs.free", fsu.getFree(), p("name",fs.getDirName(), "type",fs.getSysTypeName()));
				trace("sys.fs.freefiles", fsu.getFreeFiles(), p("name",fs.getDirName(), "type",fs.getSysTypeName()));
				trace("sys.fs.total", fsu.getTotal(), p("name",fs.getDirName(), "type",fs.getSysTypeName()));
				trace("sys.fs.used", fsu.getUsed(), p("name",fs.getDirName(), "type",fs.getSysTypeName()));
				trace("sys.fs.usedperc", fsu.getUsePercent(), p("name",fs.getDirName(), "type",fs.getSysTypeName()));
	
				trace("sys.fs.bytes", fsu.getDiskReadBytes(), p("name",fs.getDirName(), "type",fs.getSysTypeName(), "dir","reads"));
				trace("sys.fs.bytes", fsu.getDiskWriteBytes(), p("name",fs.getDirName(), "type",fs.getSysTypeName(), "dir","writes"));
	
				trace("sys.fs.ios", fsu.getDiskReads(), p("name",fs.getDirName(), "type",fs.getSysTypeName(), "dir","reads"));
				trace("sys.fs.ios", fsu.getDiskWrites(), p("name",fs.getDirName(), "type",fs.getSysTypeName(), "dir","writes"));
			} catch (Exception ex) {
				
			}			
		});
		tags.pop(3);
		if(flush) flush();
	}
	
	protected void collectIfaces(boolean flush) throws Exception {
		Arrays.stream(sigar.getNetInterfaceList()).forEach(iface -> {
			NetInterfaceStat ifs = sigar.getNetInterfaceStat(iface);
			
			bytesTrace("sys.net.iface", ifs.getRxBytes(), p("name",iface, "dir","rx", "unit","bytes"));
            trace("sys.net.iface", ifs.getRxPackets(), p("name",iface, "dir","rx", "unit","packets"));
            trace("sys.net.iface", ifs.getRxDropped(), p("name",iface, "dir","rx", "unit","dropped"));
            trace("sys.net.iface", ifs.getRxErrors(), p("name",iface, "dir","rx", "unit","errors"));
            trace("sys.net.iface", ifs.getRxOverruns(), p("name",iface, "dir","rx", "unit","overruns"));
            trace("sys.net.iface", ifs.getRxFrame(), p("name",iface, "dir","rx", "unit","frame"));
            
            bytesTrace("sys.net.iface", ifs.getTxBytes(), p("name",iface, "dir","tx", "unit","bytes"));
            trace("sys.net.iface", ifs.getTxPackets(), p("name",iface, "dir","tx", "unit","packets"));
            trace("sys.net.iface", ifs.getTxDropped(), p("name",iface, "dir","tx", "unit","dropped"));
            trace("sys.net.iface", ifs.getTxErrors(), p("name",iface, "dir","tx", "unit","errors"));
            trace("sys.net.iface", ifs.getTxOverruns(), p("name",iface, "dir","tx", "unit","overruns"));
		});
		tags.pop(3);
		if(flush) flush();
	}
	
	protected void collectSystemMem(boolean flush) throws Exception {
		Mem mem = sigar.getMem();
        trace("sys.mem", mem.getUsed(), p("unit","used"));       
        trace("sys.mem", mem.getFree(), p("unit","used"));       
        trace("sys.mem.actual", mem.getActualFree(), p("unit","free"));       
        trace("sys.mem.actual", mem.getActualUsed(), p("unit","used"));       
        trace("sys.mem.total", mem.getTotal(), p("unit","bytes"));       
        trace("sys.mem.total", mem.getRam(), p("unit","MB"));       
        trace("sys.mem.percent", mem.getFreePercent(), p("unit","free"));       
        trace("sys.mem.percent", mem.getUsedPercent(), p("unit","used"));       
		tags.pop();
		if(flush) flush();
	}
	
	protected void collectSwap(boolean flush) throws Exception {
		Swap swap = sigar.getSwap();
        long swapFree = swap.getFree();
        long swapUsed = swap.getUsed();
        long swapTotal = swap.getTotal();

        trace("sys.swap", swapFree, p("type", "free"));
        trace("sys.swap", swapUsed, p("type", "used"));
        trace("sys.swap", swapTotal, p("type", "total"));
        trace("sys.swap.percent", swapUsed/swapTotal*100, p("type", "used"));
        trace("sys.swap.percent", swapFree/swapTotal*100, p("type", "free"));
        
        trace("sys.swap.page", swap.getPageIn(), p("dir", "in"));
        trace("sys.swap.page", swap.getPageOut(), p("dir", "out"));

		tags.pop(2);
		if(flush) flush();
	}
	
	protected void collectNetstats(boolean flush) throws Exception {
		Tcp tcp = sigar.getTcp();
        trace("sys.net.tcp", tcp.getRetransSegs(), p("type","RetransSegs"));
        trace("sys.net.tcp", tcp.getPassiveOpens(), p("type","PassiveOpens"));
        trace("sys.net.tcp", tcp.getCurrEstab(), p("type","CurrEstab"));
        trace("sys.net.tcp", tcp.getEstabResets(), p("type","EstabResets"));
        trace("sys.net.tcp", tcp.getAttemptFails(), p("type","AttemptFails"));
        trace("sys.net.tcp", tcp.getInSegs(), p("type","InSegs"));
        trace("sys.net.tcp", tcp.getActiveOpens(), p("type","ActiveOpens"));
        trace("sys.net.tcp", tcp.getInErrs(), p("type","InErrs"));        
        trace("sys.net.tcp", tcp.getOutRsts(), p("type","OutRsts"));        
        trace("sys.net.tcp", tcp.getOutSegs(), p("type","OutSegs"));        
		tags.pop();
		
		NetStat netstat = sigar.getNetStat();
        //===================================================================================================================
        //        INBOUND
        //===================================================================================================================
        trace("sys.net.socket", netstat.getAllInboundTotal(), p("dir","inbound", "protocol","all", "state","all"));
        trace("sys.net.socket", netstat.getTcpInboundTotal(), p("dir","inbound", "protocol","tcp", "state","all"));       
        trace("sys.net.socket", netstat.getTcpBound(), p("dir","inbound", "protocol","tcp", "state","bound"));
        trace("sys.net.socket", netstat.getTcpListen(), p("dir","inbound", "protocol","tcp", "state","lastack"));        
        trace("sys.net.socket", netstat.getTcpLastAck(), p("dir","inbound", "protocol","tcp", "state","lastack"));        
        trace("sys.net.socket", netstat.getTcpCloseWait(), p("dir","inbound", "protocol","tcp", "state","closewait"));
        
        //===================================================================================================================
        //        OUTBOUND
        //===================================================================================================================
        trace("sys.net.socket", netstat.getAllOutboundTotal(), p("dir","outbound", "protocol","all", "state","all"));
        trace("sys.net.socket", netstat.getTcpOutboundTotal(), p("dir","outbound", "protocol","tcp", "state","all"));        
        trace("sys.net.socket", netstat.getTcpSynRecv(), p("dir","outbound", "protocol","tcp", "state","synrecv"));        
        trace("sys.net.socket", netstat.getTcpSynSent(), p("dir","outbound", "protocol","tcp", "state","synsent"));        
        trace("sys.net.socket", netstat.getTcpEstablished(), p("dir","outbound", "protocol","tcp", "state","established"));
        trace("sys.net.socket", netstat.getTcpClose(), p("dir","outbound", "protocol","tcp", "state","close"));
        trace("sys.net.socket", netstat.getTcpClosing(), p("dir","outbound", "protocol","tcp", "state","closing"));
        trace("sys.net.socket", netstat.getTcpFinWait1(), p("dir","outbound", "protocol","tcp", "state","finwait1"));
        trace("sys.net.socket", netstat.getTcpFinWait2(), p("dir","outbound", "protocol","tcp", "state","finwait2"));
        trace("sys.net.socket", netstat.getTcpIdle(), p("dir","outbound", "protocol","tcp", "state","idle"));
        trace("sys.net.socket", netstat.getTcpTimeWait(), p("dir","outbound", "protocol","tcp", "state","timewait"));        
        tags.pop(3);
        
		if(flush) flush();
	}
	

    protected Map<String, String> p(Object...keyVals) {
    	int len = keyVals.length;
    	if(len > 0) {
    		if(len%2 != 0) {
    			throw new IllegalArgumentException("Odd number of key vals:" + Arrays.toString(keyVals));
    		}
    		for(int i = 0; i < len; i++) {
    			String key = keyVals[i].toString().toLowerCase().replace(',',  '_');
    			i++;
    			String value = keyVals[i].toString().toLowerCase().replace(',',  '_');
    			tags.push(key, value);
    		}
    	}
    	return tags.map();
    }
	
	public static class PushPopMap<K,V> {
		private final Stack<K> stack = new Stack<>();
		private final Set<K> set = new HashSet<>();
		private final Map<K,V> map = new HashMap<>();
		private final Map<K,V> umap = Collections.unmodifiableMap(map);
		
		public Map<K,V> map() {
			return umap;
		}
		
		public PushPopMap<K,V> push(K key, V value) {
			if(set.add(key)) {
				stack.push(key);
			}
			map.put(key, value);
			return this;
		}
		
		public PushPopMap<K,V> pop() {
			K k = stack.pop();
			map.remove(k);
			set.remove(k);
			return this;
		}
		
		public PushPopMap<K,V> pop(int pops) {
			for(int i = 0; i < pops; i++) {
				if(stack.isEmpty()) break;
				K k = stack.pop();
				map.remove(k);
				set.remove(k);
			}
			return this;
		}
		
		
		public PushPopMap<K,V> clear() {
			stack.clear();
			map.clear();
			set.clear();
			return this;
		}
	}

}
