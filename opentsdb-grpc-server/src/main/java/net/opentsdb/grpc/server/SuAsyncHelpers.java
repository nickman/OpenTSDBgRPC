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
package net.opentsdb.grpc.server;

import java.util.function.Consumer;

import com.stumbleupon.async.Callback;
import com.stumbleupon.async.Deferred;

/**
 * <p>Title: SuAsyncHelpers</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.SuAsyncHelpers</code></p>
 */

public class SuAsyncHelpers {
	public static <T> void singleTCallback(Deferred<T> def, Consumer<T> ok, Consumer<Throwable> err) {
		def.addCallbacks(
				new Callback<Void, T>() {
					@Override
					public Void call(T t) throws Exception {
						ok.accept(t);
						return null;
					}
					
				},
				new Callback<Void, Throwable>() {
					@Override
					public Void call(Throwable t) throws Exception {
						err.accept(t);
						return null;
					}					
				}
		);
	}
}
