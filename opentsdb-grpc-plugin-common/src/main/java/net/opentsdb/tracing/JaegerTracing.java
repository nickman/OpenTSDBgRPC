/**
 * 
 */
package net.opentsdb.tracing;

import com.uber.jaeger.Tracer;
import com.uber.jaeger.metrics.Metrics;
import com.uber.jaeger.metrics.NullStatsReporter;
import com.uber.jaeger.metrics.StatsFactory;
import com.uber.jaeger.metrics.StatsFactoryImpl;
import com.uber.jaeger.reporters.RemoteReporter;
import com.uber.jaeger.reporters.Reporter;
import com.uber.jaeger.samplers.ConstSampler;
import com.uber.jaeger.samplers.Sampler;
import com.uber.jaeger.senders.HttpSender;
import com.uber.jaeger.senders.Sender;

import io.opentracing.contrib.ClientTracingInterceptor;
import io.opentracing.contrib.ServerTracingInterceptor;



/**
 *
 */
public class JaegerTracing {
	private static volatile JaegerTracing instance =  null;
	private static final Object lock = new Object();
	
	private final Reporter reporter;
	private final Sender sender;
	private final Metrics metrics;
	private final StatsFactory statsFactory;
	private final Tracer tracer;
	private final ServerTracingInterceptor serverTracingInterceptor;
	private final ClientTracingInterceptor clientTracingInterceptor;

	
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
		statsFactory =  new StatsFactoryImpl(new NullStatsReporter());
		metrics =  new Metrics(statsFactory);
		sender = new HttpSender("http://localhost:10000/api/traces");
		reporter =  new RemoteReporter(sender, 3000, 1024, metrics);
		Sampler sampler = new ConstSampler(true);
		tracer = new Tracer.Builder("OpenTSDB", reporter, sampler).build();
		serverTracingInterceptor = new ServerTracingInterceptor.Builder(this.tracer)
				.withStreaming()
				.build()
				;
		clientTracingInterceptor = new ClientTracingInterceptor.Builder(this.tracer)
				.withStreaming()
				.build()
				;
	}
	
	public Tracer tracer() {
		return tracer;
	}

	/**
	 * @return the serverTracingInterceptor
	 */
	public ServerTracingInterceptor getServerTracingInterceptor() {
		return serverTracingInterceptor;
	}

	/**
	 * @return the clientTracingInterceptor
	 */
	public ClientTracingInterceptor getClientTracingInterceptor() {
		return clientTracingInterceptor;
	}
}

