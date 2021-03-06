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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientCall.Listener;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;
import net.opentsdb.grpc.PutDatapoints;
import net.opentsdb.grpc.PutDatapointsResponse;
import net.opentsdb.grpc.TXTime;
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
	public static final String ENVOY_SVC_TIME =  "x-envoy-upstream-service-time";
	public static  final Key<String> ENVOY_SVC_TIME_KEY =  Key.of(ENVOY_SVC_TIME, Metadata.ASCII_STRING_MARSHALLER);	

	protected final Logger LOG = LoggerFactory.getLogger(getClass());
	protected final HeliosSigar sigar = HeliosSigar.getInstance();
	protected final OpenTSDBClient client;
	protected final AtomicBoolean connected = new AtomicBoolean(false);
	protected final AtomicBoolean streamerOpen = new AtomicBoolean(false);
	protected final AtomicBoolean collecting = new AtomicBoolean(false);
	protected BidiStreamer<PutDatapoints,PutDatapointsResponse> streamer = null;
	protected DatapointBatcher batcher;
	protected final PushPopMap<String, String> tags = new PushPopMap<>();
	protected final LongAdder pending = new LongAdder();
	protected final Map<String, String> processQueries = new HashMap<>();
	protected final String host;
	protected final int port;
	protected final AtomicReference<Metadata> headersCapture = new AtomicReference<>(); 
	protected final AtomicReference<Metadata> trailersCapture = new AtomicReference<>();

	protected Thread collectionThread = null;

	/**
	 * Creates a new OSMonitor
	 * @param host The grpc server host
	 * @param port The grpc server port
	 */
	public OSMonitor(String host, int port) {
		this.host = host;
		this.port = port;
		LOG.info("OpenTSDBClient({}, {})", host, port);
		client = OpenTSDBClient.newInstance(host, port);
		LOG.info("Ready");
		String hostName = ManagementFactory.getRuntimeMXBean().getName().split("@")[1];
		String app = "os";
		processQueries.put("java", "State.Name.ew=java");
		processQueries.put("envoy", "State.Name.ew=envoy");
		startConnect();
	}
	
	private final class MonitoredClientCallListener<RespT> extends SimpleForwardingClientCallListener<RespT> {

		protected MonitoredClientCallListener(Listener<RespT> delegate) {
			super(delegate);
		}
		
		@Override
		public void onHeaders(Metadata headers) {
			String svcTime = headers.get(ENVOY_SVC_TIME_KEY);
			if(svcTime != null) LOG.info("SVC_TIME: {}", svcTime);
			super.onHeaders(headers);
		}
		
		@Override
		public void onMessage(RespT message) {
			// TODO Auto-generated method stub
			super.onMessage(message);
		}
		
		
		
	}
	
	private class MonitoredClientCall<ReqT, RespT> extends SimpleForwardingClientCall<ReqT, RespT> {

		protected MonitoredClientCall(ClientCall<ReqT, RespT> delegate) {
			super(delegate);
		}
		
		@Override
		public void start(Listener<RespT> responseListener, Metadata headers) {
			
			super.start(new MonitoredClientCallListener<>(responseListener), headers);
		}

		
		
	}
	
	protected class Monitor implements ClientInterceptor {

		@Override
		public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
				CallOptions callOptions, Channel next) {
			LOG.info("Calling ----> {}", method.getFullMethodName());
			return new MonitoredClientCall<>(next.newCall(method, callOptions));
		}
		
	}
	
	protected void startConnect() {
		if(!connected.get()) {
			new Thread("OSMonitorConnectThread") {
				public void run() {
					while(!connected.get()) {
						openClient();
						if(!connected.get()) {
							try { Thread.sleep(1000); } catch (Exception x) {}
						}
					}
					LOG.info("OSMonitorConnectThread Finished");
				}
			}.start();
		}
	}
	
	protected void startStreamerConnect() {
		LOG.info("Starting OSMonitorStreamerConnectThread");
		new Thread("OSMonitorStreamerConnectThread") {
			public void run() {
				while(!streamerOpen.get()) {
					startStreamer();
					if(!streamerOpen.get()) {
						try { Thread.sleep(1000); } catch (Exception x) {}
					}
				}
				LOG.info("OSMonitorStreamerConnectThread Finished");
			}
		}.start();
	}
	
	

	protected void openClient() {
		if(connected.compareAndSet(false, true)) {
			try {
				LOG.info("Opening Client...");
				//client.open(MetadataUtils.newCaptureMetadataInterceptor(headersCapture, trailersCapture) );
				client.open(new Monitor());
				LOG.info("Client opened");
				startStreamer();
			} catch (Exception ex) {
				connected.set(false);
			}
		}
	}
	
	protected void closeClient() {
		if(connected.compareAndSet(true, false)) {
			try {
				LOG.info("Closing Client...");
				stopStreamer();
				try { client.close(); } catch (Exception x) {}
				LOG.info("Client closed");				
			} catch (Exception ex) {
				
			}
		}
	}
	

	
	protected void startStreamer() {
		if(connected.get() && streamerOpen.compareAndSet(false, true)) {
			try {
				LOG.info("Starting streamer...");
				streamer = client.putDatapoints(this::onResponse).start();
				streamer.onErrorAction((t,s) -> {
					LOG.error("Streamer Error Callback: {}", t.toString());
					stopStreamer();
					if(!client.isConnected()) {
						closeClient();
						startConnect();
					} else {						
						startStreamer();
						if(!streamerOpen.get()) {
							stopStreamer();
							startStreamerConnect();
						}
					}
				});
				batcher = new DatapointBatcher(streamer);
				LOG.info("Streamer started");
			} catch (Exception ex) {
				streamerOpen.set(false);
				streamer = null;
				batcher = null;
			}
		}
	}
	
	protected void stopStreamer() {
		if(streamerOpen.compareAndSet(true, false)) {
			LOG.info("Stopping streamer...");
			try {
				streamer.close();
			} catch (Exception ex) {
				LOG.warn("Error closing streamer: {}", ex);
			} finally {
				streamer = null;
				batcher = null;
				LOG.info("Streamer stopped");
			}
		}
	}
	
	protected boolean ping() {
		try {
			client.ping();
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
	
	public void onResponse(PutDatapointsResponse response) {
		long now = System.currentTimeMillis();
		long total = response.getFailed() + response.getSuccess();
		pending.add(-total);
		TXTime tx = response.getTxTime();
		if(tx != null && tx.getTxtime() != 0L) {
			
			long elapsed = now - tx.getTxtime();
			long sendTime = tx.getStime();
			long returnTime = now - tx.getRtime();
			long processingTime = tx.getPtime();
			
			long variance = sendTime + returnTime + processingTime - elapsed;
			
			trace("grpc.putdatapoints", sendTime, Collections.singletonMap("phase", "send"));
			trace("grpc.putdatapoints", returnTime, Collections.singletonMap("phase", "return"));
			trace("grpc.putdatapoints", processingTime, Collections.singletonMap("phase", "process"));
			trace("grpc.putdatapoints", elapsed, Collections.singletonMap("phase", "total"));
			trace("grpc.putdatapoints", pending.longValue(), Collections.singletonMap("phase", "pending"));
			
			// total=1072, elapsed=931, send=930, return=1, proc=926, now-pending=0
			
			LOG.info("Received Response. count={}, elapsed={}, send={}, return={}, proc={}, pending={}, var={}", 
					total, elapsed, sendTime, returnTime, processingTime, pending.longValue(), variance);
		} else {
			LOG.info("Received Response. count={}, now-pending={}", total, pending.longValue());
		}
		if(response.getFailed() > 0) {
			System.err.println("Failed points: " + response.getFailed());
		}
	}
	
//	int64 txtime = 1;  // Inbound: send time, Outbound: same
//	int64 stime = 2; 	// Outbound: send elapased
//	int64 rtime = 3;  // Outbound: return elapsed
//	int64 ptime = 4;	// Outbound: processing elapsed
	
	
	private static double per(double time, double count) {
		if(time==0D || count==0D) return 0D;
		return time/count;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OSMonitor os = new OSMonitor("localhost", 10000);
		final boolean flush = false;
		try { 
			os.startCollection(flush, false); 
			System.in.read();
			os.stopCollection();
		} catch (Exception ex) {}
		os.LOG.info("Done");
		try { os.client.close(); } catch (Exception x) {}
		
	}
	
	public void stopCollection() {
		if(collecting.compareAndSet(true, false)) {
			collectionThread.interrupt();
			collectionThread = null;
		}
	}
	
	public void startCollection(final boolean flush, final boolean closeStream) {
		if(collecting.compareAndSet(false, true)) {
			collectionThread = new Thread("CollectionThread") {
				public void run() {
					while(collecting.get()) {
						try {
							if(closeStream) {
								startStreamer();
								try {
									collect(flush);
									Thread.currentThread().join(5000);
								} finally {
									stopStreamer();
								}
							} else {
								if(streamerOpen.get()) {
									collect(flush);
									Thread.currentThread().join(5000);
								}
							}
							
							
						} catch (InterruptedException iex) {
							if(Thread.interrupted()) Thread.interrupted();
						} catch (Exception ex) {
							LOG.error("Collection Error", ex);
						}
					}
				}
			};
			collectionThread.setDaemon(true);
			collectionThread.start();
		}
	}
	
	protected void collect(boolean flush) throws Exception {
		collectCpu(flush);
		tags.clear();
		collectFs(flush);
		tags.clear();
		collectIfaces(flush);
		tags.clear();
		collectNetstats(flush);
		tags.clear();
		collectSystemMem(flush);
		tags.clear();
		collectSwap(flush);
		tags.clear();
		collectServerSockets(flush);
		tags.clear();
		collectClientSockets(flush);
		tags.clear();
		processQueries(flush);
		tags.clear();
		if(!flush) {
			flush();
		}		
	}
	
	public void trace(String metric, double value, Map<String, String> tagPairs) {
		if(batcher!=null) {
			batcher.record(metric, value, tagPairs);
		}
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
