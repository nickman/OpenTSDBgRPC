/**
 * 
 */
package net.opentsdb.grpc.client;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthCheckResponse.ServingStatus;
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
public class ManagedConnection implements Closeable {
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
	protected final AtomicBoolean healthOk = new AtomicBoolean(false);
	protected final Map<ConnectivityState, Set<BiConsumer<ConnectivityState, ManagedConnection>>> stateListeners = stateListeners();
	protected final Set<BiConsumer<Boolean, ConnectivityState>> availabilityListeners = new CopyOnWriteArraySet<>();
	protected final Set<Consumer<Metadata>> headerListeners = new CopyOnWriteArraySet<>();
	protected final Set<Consumer<Metadata>> tailerListeners = new CopyOnWriteArraySet<>();
	protected HealthBlockingStub healthStub = null;
	protected final AtomicReference<Metadata> headersCapture = new AtomicReference<>(); 
	protected final AtomicReference<Metadata> trailersCapture = new AtomicReference<>();
	protected ScheduledFuture<?> healthCheckHandle = null;
	
	protected final long healthCheckPeriodMs;
	protected final long shutdownGraceMs;
	
	
	
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
		healthCheckPeriodMs = clientConfig.getMcHealthCheckPeriod().convertTo(TimeUnit.MILLISECONDS);
		shutdownGraceMs = clientConfig.getMcShutdownGrace().convertTo(TimeUnit.MILLISECONDS);
		if(healthCheckPeriodMs < 1) {
			LOG.info("Scheduled HealthCheck Disabled");
			healthOk.set(true);
		} else {
			healthCheckHandle = scheduler.scheduleWithFixedDelay(healthCheckRunnable(), healthCheckPeriodMs, healthCheckPeriodMs, TimeUnit.MILLISECONDS);
		}
	}
	
	
	public static void main(String[] args) {
		ManagedConnection mc = new ManagedConnection("localhost", 10001).open();
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
			if(!tailerListeners.isEmpty()) {
				tailerListeners.stream()
					.forEach(l ->  
						executor.execute(() -> l.accept(trailersCapture.get()))
					);
			}
			if(!headerListeners.isEmpty()) {
				headerListeners.stream()
					.forEach(l ->  
						executor.execute(() -> l.accept(headersCapture.get()))
					);
			}
			return next.newCall(method, callOptions);
		}
	}
	
	protected Runnable healthCheckRunnable() {
		return () -> healthCheck();
	}
	
	@Override
	public void close() throws IOException {
		if(connected.compareAndSet(true, false)) {
			LOG.info("Closing ManagedConnection...");
			if(healthCheckHandle != null) {
				try { healthCheckHandle.cancel(true); } catch (Exception x) {}
			}
			if(healthOk.compareAndSet(true, false)) {
				healthOk.set(false);
			}
			availabilityChange(false);
			channel.shutdown();
			Thread shutdownThread = new Thread(String.format("Channel[%s:%s]Shutdown", host, port)) {
				public void run() {
					try {
						if(shutdownGraceMs < 1) {
							channel.shutdownNow();
						} else {
							if(channel.awaitTermination(shutdownGraceMs, TimeUnit.MILLISECONDS)) {
								LOG.info("ManagedConnection Shutdown");
							} else {
								LOG.warn("ManagedConnection Shutdown Timed Out after {} ms", shutdownGraceMs);
								channel.shutdownNow();
							}
						}
					} catch (Exception ex) {
						LOG.warn("Thread interrupted while awaiting shutdown", ex);
					}
				}
			};
			shutdownThread.setDaemon(true);
			shutdownThread.start();
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
		if(!healthOk.get()) {
			throw new IllegalStateException(String.format("ManagedConnection-%s-%s is not healthy", host, port));
		}		
	}
	
	protected boolean healthCheck() {
		boolean ok = false;
		if(connected.get()) {
			try {
				HealthCheckResponse response = healthStub.check(HealthCheckRequest.newBuilder().build());
				if(headersCapture.get().containsKey(ENVOY_SVC_TIME_KEY)) {
					String svcTime = headersCapture.get().get(ENVOY_SVC_TIME_KEY);
//					LOG.info("SVC_TIME: {}", svcTime);
				}
				ok = response.getStatus()==ServingStatus.SERVING;
			} catch (Exception ex) {
				ok = false;
			}			
		}
		if(healthOk.compareAndSet(!ok, ok)) {
			availabilityChange(ok);
		}
		return ok;
		
	}
	
	
	protected void initStateChange() {
		if(channel!=null) {
			onStateChange();
		}
	}
	
	protected void onStateChange() {
		ConnectivityState cs = channel.getState(true);
		ConnectivityState prior = connState.getAndSet(cs);
		if(cs != prior && cs != ConnectivityState.TRANSIENT_FAILURE && prior != ConnectivityState.TRANSIENT_FAILURE) {
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
	
	protected void availabilityChange(final boolean up) {
		final ConnectivityState cs = channel.getState(false);
		availabilityListeners.stream().forEach(x -> {
			executor.execute(() -> {
				x.accept(up, cs);
			});
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
	
	public ManagedConnection addAvailabilityListener(BiConsumer<Boolean, ConnectivityState> listener) {
		availabilityListeners.add(Objects.requireNonNull(listener, "The passed AvailabilityState listener was null"));
		return this;
	}
	
	public ManagedConnection addHeadersListener(Consumer<Metadata> listener) {
		headerListeners.add(Objects.requireNonNull(listener, "The passed Headers listener was null"));
		return this;
	}
	
	public ManagedConnection addTailersListener(Consumer<Metadata> listener) {
		tailerListeners.add(Objects.requireNonNull(listener, "The passed Tailers listener was null"));
		return this;
	}
	
	private static Map<ConnectivityState, Set<BiConsumer<ConnectivityState, ManagedConnection>>> stateListeners() {
		EnumMap<ConnectivityState, Set<BiConsumer<ConnectivityState, ManagedConnection>>> map = new EnumMap<>(ConnectivityState.class);
		for(ConnectivityState cs : ConnectivityState.values()) {
			map.put(cs, new CopyOnWriteArraySet<>());
		}
		return map;
	}

	/**
	 * Returns 
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * Returns 
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Returns 
	 * @return the channel
	 */
	public ManagedChannel getChannel() {
		return channel;
	}

	/**
	 * Returns 
	 * @return the connState
	 */
	public ConnectivityState getConnState() {
		return connState.get();
	}

	/**
	 * Returns 
	 * @return the healthOk
	 */
	public boolean isHealthy() {
		return healthOk.get();
	}

	/**
	 * Returns 
	 * @return the headersCapture
	 */
	public AtomicReference<Metadata> getHeadersCapture() {
		return headersCapture;
	}

	/**
	 * Returns 
	 * @return the trailersCapture
	 */
	public AtomicReference<Metadata> getTrailersCapture() {
		return trailersCapture;
	}
	
	
}
