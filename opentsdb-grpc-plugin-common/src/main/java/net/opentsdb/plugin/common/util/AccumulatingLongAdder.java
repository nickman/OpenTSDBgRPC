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
package net.opentsdb.plugin.common.util;

import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

/**
 * <p>Title: AccumulatingLongAdder</p>
 * <p>Description: {@link LongAdder} that accumulates up to another LongAdder.</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.util.AccumulatingLongAdder</code></p>
 */

public class AccumulatingLongAdder extends LongAdder {
	/**  */
	private static final long serialVersionUID = -7886684573073032262L;
	/** The parent accumulated up to */
	protected final LongAdder parent;
	
	/**
	 * Creates a new AccumulatingLongAdder
	 * @param parent The parent to accumulate up to
	 */
	public AccumulatingLongAdder(final LongAdder parent) {
		this.parent = Objects.requireNonNull(parent, "The passed LongAdder parent was null");
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.util.concurrent.atomic.LongAdder#add(long)
	 */
	@Override
	public void add(final long x) {		
		parent.add(x);
		super.add(x);		
	}
	

}
