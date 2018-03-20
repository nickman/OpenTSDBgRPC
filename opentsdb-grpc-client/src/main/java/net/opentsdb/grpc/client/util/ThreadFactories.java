/**
 * 
 */
package net.opentsdb.grpc.client.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author nwhitehead
 *
 */
public class ThreadFactories {

	public static ThreadFactory threadFactory(String name, boolean daemon, int priority) {
		return new ThreadFactory() {
			final AtomicInteger serial = new AtomicInteger();
			final ThreadGroup threadGroup = new ThreadGroup(name + "ThreadGroup");
			/**
			 * {@inheritDoc}
			 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
			 */
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(threadGroup, r, name + "Thread#" + serial.incrementAndGet());
				t.setDaemon(daemon);
				t.setPriority(priority);
				return t;
			}
		};
	}
	
	public static ThreadFactory threadFactory(String name, boolean daemon) {
		return threadFactory(name, daemon, Thread.NORM_PRIORITY);
	}
	
	private ThreadFactories() {}
}
