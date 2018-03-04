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
package net.opentsdb.grpc.server.netty;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import org.jctools.maps.NonBlockingHashMapLong;

/**
 * <p>Title: EventLoopThreadFactory</p>
 * <p>Description: An instrumented thread factory for event loop groups</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.netty.EventLoopThreadFactory</code></p>
 */

public class EventLoopThreadFactory implements ThreadFactory {
	private final String name;
	private final AtomicLong serial = new AtomicLong();
	private final AtomicLong destroyed = new AtomicLong();
	private final NonBlockingHashMapLong<Thread> threads = new NonBlockingHashMapLong<>(64, true); 
	
	
	public EventLoopThreadFactory(String name) {		
		this.name = name;
	}

	@Override
	public Thread newThread(final Runnable r) {
		Thread t = new Thread(new Runnable(){
			public void run() {
				try {
					r.run();
				} finally {
					destroyed.incrementAndGet();
					threads.remove(Thread.currentThread().getId());
				}
			}
		}, name + "Thread#" + serial.incrementAndGet());
		threads.put(t.getId(), t);
		return t;
	}
}
