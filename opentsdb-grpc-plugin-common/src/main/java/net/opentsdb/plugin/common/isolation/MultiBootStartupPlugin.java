/**
 * 
 */
package net.opentsdb.plugin.common.isolation;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stumbleupon.async.Deferred;

import net.opentsdb.core.TSDB;
import net.opentsdb.stats.StatsCollector;
import net.opentsdb.tools.StartupPlugin;
import net.opentsdb.tsd.RpcPlugin;
import net.opentsdb.utils.Config;

/**
 * @author nwhitehead
 *
 */
public class MultiBootStartupPlugin extends StartupPlugin {
	protected static final Logger LOG = LoggerFactory.getLogger(MultiBootStartupPlugin.class);
	
	protected final Set<StartupPlugin> delegates = new LinkedHashSet<>();
	protected final String[] delegateClassNames;
	protected IsolatedClassLoader2 icl;
	protected String version = getClass().getPackage().getImplementationVersion();

	
	/**
	 * Creates a new MultiBootStartupPlugin
	 * @param classNames The class names of the delegates to load
	 */	
	public MultiBootStartupPlugin(String...classNames) {
		delegateClassNames = classNames;
		if(version==null) {
			version = "Unassigned";
		}
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tools.StartupPlugin#initialize(net.opentsdb.utils.Config)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Config initialize(Config config) {
		LOG.info("Initializing Delegates: {}", Arrays.toString(delegateClassNames));
		icl = IsolatedClassLoader2.classLoader(true, getClass().getProtectionDomain().getCodeSource().getLocation());
		for(String className : delegateClassNames) {
			try {
				Class<? extends StartupPlugin> clazz = (Class<? extends StartupPlugin>) Class.forName(className, true, icl);
				StartupPlugin delegate = clazz.newInstance();
				delegates.add(delegate);
				LOG.info("Created StartupPlugin Delegate: {}", className);
			} catch (Exception ex) {
				LOG.error("Failed to create delegate StartupPlugin: {}", className, ex);
				throw new IllegalArgumentException("Failed to create delegate StartupPlugin: " + className, ex);
			}
		}
		return config;
	}
 
	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tools.StartupPlugin#setReady(net.opentsdb.core.TSDB)
	 */
	@Override
	public void setReady(TSDB tsdb) {
		LOG.info("Setting {} Delegates Ready", delegates.size());
		for(StartupPlugin delegate: delegates) {
			try {
				delegate.setReady(tsdb);
			} catch (Exception ex) {
				LOG.error("Failed to set delegate StartupPlugin {} ready", delegate.getClass().getName(), ex);
				throw new IllegalArgumentException("Failed to set delegate StartupPlugin " + delegate.getClass().getName() + " ready", ex);
			}
		}
		LOG.info("All Delegates Ready");

	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tools.StartupPlugin#shutdown()
	 */
	@Override
	public Deferred<Object> shutdown() {
		Deferred<Object> def = new Deferred<>();
		Thread t = new Thread("MultiBootStartupPluginShutdown") {
			public void run() {
				final List<Deferred<Object>> defs = new ArrayList<>(delegates.size());
					for(StartupPlugin delegate : delegates) {
						String className = delegate.getClass().getName();
						try {
							LOG.info("Stopping Delegate StartupPlugin: {}", className);
							Deferred<Object> d = delegate.shutdown();
							defs.add(d);
						} catch (Exception ex) {
							LOG.error("Failed to stop Delegate StartupPlugin: {}, err={}", className, ex);
						}
					}
					if(!defs.isEmpty()) {
						try {
							Deferred.group(defs).join(5000);
							LOG.info("{} delegate plugins shutdown", defs.size());
						} catch (Exception ex) {
							LOG.warn("Timed out waiting for delegates to shut down");
						} finally {
							
							def.callback(null);
						}
					}
			}
		};
		t.setDaemon(true);
		t.start();
		return def;
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tools.StartupPlugin#version()
	 */
	@Override
	public String version() {
		return version;
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tools.StartupPlugin#getType()
	 */
	@Override
	public String getType() {
		return "Startup";
	}

	/**
	 * {@inheritDoc}
	 * @see net.opentsdb.tools.StartupPlugin#collectStats(net.opentsdb.stats.StatsCollector)
	 */
	@Override
	public void collectStats(StatsCollector collector) {
		for(StartupPlugin delegate : delegates) {
			collector.addExtraTag("delegate", delegate.getClass().getSimpleName());
			try {
				delegate.collectStats(collector);
			} catch (Exception ex) {
				LOG.warn("Failed to collect stats from StartupPlugin delegate: plugin={}, err={}",
						delegate.getClass().getName(), ex
				);
			} finally {
				collector.clearExtraTag("delegate");
			}
		}
	}

}
