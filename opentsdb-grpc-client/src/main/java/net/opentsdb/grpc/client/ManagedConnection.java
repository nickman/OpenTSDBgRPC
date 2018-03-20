/**
 * 
 */
package net.opentsdb.grpc.client;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ClientInterceptors;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.health.v1.HealthGrpc.HealthBlockingStub;
import io.grpc.stub.MetadataUtils;
import net.opentsdb.grpc.client.util.ClientConfiguration;
import net.opentsdb.grpc.client.util.ThreadFactories;

/**
 * @author nwhitehead
 *
 *  HEADERS: [date, server, x-envoy-upstream-service-time, grpc-accept-encoding, content-type, x-envoy-decorator-operation, grpc-encoding]
 */
public class ManagedConnection {
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, ThreadFactories.threadFactory("ManagedConnectionScheduler", true));
	private static final ExecutorService executor = Executors.newFixedThreadPool(2, ThreadFactories.threadFactory("ManagedConnectionExecutor", true));
	
	public static final String ENVOY_SVC_TIME =  "x-envoy-upstream-service-time";
	public static  final Key<String> ENVOY_SVC_TIME_KEY =  Key.of(ENVOY_SVC_TIME, Metadata.ASCII_STRING_MARSHALLER);
	
	protected final Logger LOG;
	protected final String host;
	protected final int port;
	protected final ClientConfiguration clientConfig;
	protected ManagedChannel channel = null;
	protected final AtomicReference<ConnectivityState> connState = new AtomicReference<>(ConnectivityState.IDLE);
	protected final AtomicBoolean connected = new AtomicBoolean(false);
	protected final Map<ConnectivityState, Set<BiConsumer<ConnectivityState, ManagedConnection>>> stateListeners = stateListeners();
	protected HealthBlockingStub healthStub = null;
	protected final AtomicReference<Metadata> headersCapture = new AtomicReference<>(); 
	protected final AtomicReference<Metadata> trailersCapture = new AtomicReference<>();
	
	
	
	/**
	 * Creates a new Managed Connection
	 * @param host The host to connect to
	 * @param port The port to connect to
	 */
	public ManagedConnection(String host, int port) {
		this(ClientConfiguration.clientConfiguration(host, port));
	}
	
	public ManagedConnection() {
		this(ClientConfiguration.clientConfiguration());
	}
	
	public ManagedConnection(ClientConfiguration clientConfig) {
		this.clientConfig = clientConfig;
		this.host = clientConfig.getHost();
		this.port = clientConfig.getPort();		
		LOG = LoggerFactory.getLogger(getClass().getSimpleName() + "[" + host + ":" + port + "]");
	}
	
	
	public static void main(String[] args) {
		ManagedConnection mc = new ManagedConnection("localhost", 10000).open();
//		ManagedConnection mc = new ManagedConnection("localhost", 4249).open();
		mc.LOG.info("HEALTH: {}", mc.healthCheck());
		for(int i = 0; i < 100; i++) {
			try { Thread.sleep(3000); } catch (Exception ex) {}
			mc.LOG.info("HEALTH: {}", mc.healthCheck());
		}
	}
	
	private class ManagedClientInterceptor implements ClientInterceptor {

		/**
		 * {@inheritDoc}
		 * @see io.grpc.ClientInterceptor#interceptCall(io.grpc.MethodDescriptor, io.grpc.CallOptions, io.grpc.Channel)
		 */
		@Override
		public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
				CallOptions callOptions, Channel next) {
			LOG.info("HEADERS: {}", Arrays.toString(headersCapture.get().keys().toArray(new String[0])));
			LOG.info("TRAILERS: {}", Arrays.toString(trailersCapture.get().keys().toArray(new String[0])));
			return next.newCall(method, callOptions);
		}
		
	}
	
	public ManagedConnection open() {
		if(connected.compareAndSet(false, true)) {
			channel = clientConfig.build(
					MetadataUtils.newCaptureMetadataInterceptor(headersCapture, trailersCapture) 
//					new ManagedClientInterceptor()
			);			
			initStateChange();
			healthStub = HealthGrpc.newBlockingStub(channel);			
		}
		return this;
	}
	
	
	protected void check() {
		if(!connected.get()) {
			throw new IllegalStateException(String.format("ManagedConnection-%s-%s is not connected", host, port));
		}
	}
	
	public boolean healthCheck() {
		if(!connected.get()) {
			return false;
		}
		try {
			healthStub.check(HealthCheckRequest.newBuilder().build());
			if(headersCapture.get().containsKey(ENVOY_SVC_TIME_KEY)) {
				String svcTime = headersCapture.get().get(ENVOY_SVC_TIME_KEY);
				LOG.info("SVC_TIME: {}", svcTime);
			}
//			LOG.info("HEADERS: {}", Arrays.toString(headersCapture.get().keys().toArray(new String[0])));
//			LOG.info("TRAILERS: {}", Arrays.toString(trailersCapture.get().keys().toArray(new String[0])));
			
			return true;
		} catch (Exception ex) {
			return false;
		}
		
	}
	
	
	protected void initStateChange() {
		if(channel!=null) {
			onStateChange();
		}
	}
	
	protected void onStateChange() {
		ConnectivityState cs = channel.getState(true);
		ConnectivityState prior = connState.getAndSet(cs);
		if(cs != prior) {
			LOG.info("Connectivity State Change: {} --> {}", prior, cs);
			stateListeners.get(cs).stream().forEach(x -> {
				executor.execute(() -> {
					x.accept(cs, ManagedConnection.this);
				});
			});
		}
		channel.notifyWhenStateChanged(cs, () -> {
			onStateChange();
		});
	}

	public boolean isConnected() {
		return connState.get()==ConnectivityState.READY;
	}
	
	public ConnectivityState connectivityState() {
		return connState.get();
	}

	
	
	public ManagedConnection addConnectivityListener(ConnectivityState state, BiConsumer<ConnectivityState, ManagedConnection> listener) {
		stateListeners.get(Objects.requireNonNull(state, "The passed ConnectivityState was null"))
			.add(Objects.requireNonNull(listener, "The passed ConnectivityState listener was null"));
		return this;
		
	}
	
	public ManagedConnection addConnectivityListener(BiConsumer<ConnectivityState, ManagedConnection> listener) {
		for(ConnectivityState cs : ConnectivityState.values()) {
			addConnectivityListener(cs, listener);
		}
		return this;
	}
	
	private static Map<ConnectivityState, Set<BiConsumer<ConnectivityState, ManagedConnection>>> stateListeners() {
		EnumMap<ConnectivityState, Set<BiConsumer<ConnectivityState, ManagedConnection>>> map = new EnumMap<>(ConnectivityState.class);
		for(ConnectivityState cs : ConnectivityState.values()) {
			map.put(cs, new CopyOnWriteArraySet<>());
		}
		return map;
	}
	
	
}
