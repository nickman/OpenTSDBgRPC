/**
 * 
 */
package net.opentsdb.tracing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

import com.uber.jaeger.Tracer;
import com.uber.jaeger.metrics.Metrics;
import com.uber.jaeger.metrics.MetricsFactory;
import com.uber.jaeger.reporters.RemoteReporter;
import com.uber.jaeger.reporters.Reporter;
import com.uber.jaeger.samplers.ConstSampler;
import com.uber.jaeger.samplers.Sampler;
import com.uber.jaeger.senders.HttpSender;
import com.uber.jaeger.senders.Sender;

import io.grpc.ClientInterceptor;
import io.grpc.ServerInterceptor;
import io.opentracing.Span;
import io.opentracing.contrib.ClientTracingInterceptor;
import io.opentracing.contrib.OpenTracingContextKey;
import io.opentracing.contrib.ServerTracingInterceptor;
import io.opentracing.noop.NoopSpan;
import io.opentracing.tag.Tags;
import io.opentracing.util.ThreadLocalScopeManager;
import net.opentsdb.grpc.DataPoint;



/**
 *
 */
public class JaegerTracing {
	private static volatile JaegerTracing instance =  null;
	private static final Object lock = new Object();
	
	
	
	private final Reporter reporter;
	private final Sender sender;
	private final MetricsFactory metricsFactory;
	private final io.opentracing.Tracer tracer;
	private final Metrics metrics;
	private final ThreadLocalScopeManager scopeManager = new ThreadLocalScopeManager();
	private final ServerTracingInterceptor serverTracingInterceptor;
	private final ClientTracingInterceptor clientTracingInterceptor;
	private final BlockingQueue<DataPoint> dpQueue;
	private final boolean tracingEnabled;
	private final boolean inServer;

	
	public static JaegerTracing getInstance() {
		if(instance==null) {
			synchronized(lock) {
				if(instance==null) {
					instance = new JaegerTracing();
				}
			}
		}
		return instance;
	}
	
	private JaegerTracing() {
		inServer = System.getProperties().containsKey("net.opentsdb.inserver");
		tracingEnabled = System.getProperties().containsKey("net.opentsdb.tracing.enabled");
		Sampler sampler = new ConstSampler(true);
		sender = new HttpSender("http://localhost:10000/api/traces");
		
		if(inServer) {
			dpQueue = null;
			metricsFactory = new OpenTSDBMetricsFactory("OpenTSDBGrpcInProcessServer", "OpenTSDB");
			metrics = new Metrics(metricsFactory);
			clientTracingInterceptor = null;
			reporter = tracingEnabled ? new RemoteReporter.Builder()
					.withSender(sender)
					.withFlushInterval(15000)
					.withMaxQueueSize(1024)
					.withMetrics(metrics)
					.build() : null;
			tracer = tracingEnabled ? new Tracer.Builder("OpenTSDB")
						.withReporter(reporter)
						.withSampler(sampler)
						.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER)
						.withScopeManager(scopeManager)
						.withMetricsFactory(metricsFactory)
						.build() : NoOpTracer.INSTANCE;	
			serverTracingInterceptor = new ServerTracingInterceptor.Builder(tracer)
					.withStreaming()
//					.withVerbosity()
					.build();
			
		} else {
			dpQueue = new ArrayBlockingQueue<>(1024, false);			
			metricsFactory = new OpenTSDBMetricsFactory("OpenTSDBGrpcInProcessServer", "OpenTSDBClient", dpQueue);
			metrics = new Metrics(metricsFactory);
			reporter = tracingEnabled ? new RemoteReporter.Builder()
					.withSender(sender)
					.withFlushInterval(15000)
					.withMaxQueueSize(1024)
					.withMetrics(metrics)
					.build() : null;
			tracer = tracingEnabled ? new Tracer.Builder("OpenTSDBClient")
					.withReporter(reporter)
					.withSampler(sampler)
					.withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_CLIENT)
					.withScopeManager(scopeManager)
					.withMetricsFactory(metricsFactory)
					.build() : NoOpTracer.INSTANCE;
			clientTracingInterceptor = new ClientTracingInterceptor.Builder(tracer)
					.withStreaming()
//					.withVerbosity()
					.build();
			serverTracingInterceptor = null;
		}
	}
	
	private void close() {
		if(reporter != null) {
			try { reporter.close(); } catch (Exception x) {/* No Op */}
		}
		
		instance = null;
	}
	
	public boolean isServer() {
		return inServer;
	}
	
	public io.opentracing.Tracer tracer() {		
		return tracingEnabled ? tracer : NoOpTracer.INSTANCE;
	}
	
	
	public Span newSpan(String opName) {
		Span parentSpan = OpenTracingContextKey.activeSpan();
		if(parentSpan!=null) {
			return
			tracer.buildSpan(opName)
				.asChildOf(parentSpan)
				.start();
		}
		return NoopSpan.INSTANCE;		
	}
	

	/**
	 * @return the serverTracingInterceptor
	 */
	public ServerInterceptor getServerTracingInterceptor() {
		return tracingEnabled ? serverTracingInterceptor : NoopServerInterceptor.INSTANCE;
	}

	/**
	 * @return the clientTracingInterceptor
	 */
	public ClientInterceptor getClientTracingInterceptor() {
		return tracingEnabled ? clientTracingInterceptor : NoopClientInterceptor.INSTANCE;
	}
	
	public Supplier<Collection<DataPoint>> getClientMetricQueue() {
		return new Supplier<Collection<DataPoint>>() {
			@Override
			public Collection<DataPoint> get() {
				try {
					DataPoint dp = dpQueue.take();
					int size = dpQueue.size();
					if(size > 0) {
						final List<DataPoint> c = new ArrayList<>(dpQueue.size() + 1);
						dpQueue.drainTo(c);
						c.add(dp);						
						return c;
					}
					return Collections.singletonList(dp);
				} catch (InterruptedException iex) {
					if(Thread.interrupted()) Thread.interrupted();
					return Collections.emptyList();
				}
			}
		};
	}

	/**
	 * Returns 
	 * @return the tracingEnabled
	 */
	public boolean isTracingEnabled() {
		return tracingEnabled;
	}
}

