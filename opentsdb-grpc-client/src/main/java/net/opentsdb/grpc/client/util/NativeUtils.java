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
package net.opentsdb.grpc.client.util;

import io.netty.channel.epoll.Epoll;

/**
 * <p>Title: NativeUtils</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.client.util.NativeUtils</code></p>
 */

public class NativeUtils {
	
	public static final String DISABLE_EPOLL = "epoll.disable";
	
	public static final String AVAILABLE_PROCESSORS = "io.netty.availableProcessors";
	
	public static final int ACTUAL_CORES = Runtime.getRuntime().availableProcessors();
	
	private NativeUtils() {}
	
	
	/**
	 * Determines if epoll is available.
	 * If we're on Linux, usually it is, but
	 * it can be disabled by setting {@link #GRPC_NO_EPOLL}
	 * to true.
	 * @return
	 */
	public static boolean isEpoll() {
		if(ClientConfiguration.config(DISABLE_EPOLL, false)) {
			return false;
		}
		return Epoll.isAvailable();
	}
	
	/**
	 * Returns the number of available cores
	 * which by default is reported from {@link Runtime#availableProcessors()}
	 * but can be overriden by setting {@link #AVAILABLE_PROCESSORS}.
	 * @return the number of available cores
	 */
	public static int availableCores() {
		return ClientConfiguration.config(AVAILABLE_PROCESSORS, ACTUAL_CORES);
	}
	
	

}
