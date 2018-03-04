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
package net.opentsdb.grpc.server.util;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import net.opentsdb.grpc.RelativeTime;

/**
 * <p>Title: RelTime</p>
 * <p>Description: Represents a time unit and a value</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.util.RelTime</code></p>
 */

public class RelTime implements Comparable<RelTime> {
	/** The tunit of this relative time */
	public final TUnit unit;
	/** The time value of this relative time */
	public final long time;
	
	/**
	 * Creates a new RelativeTime
	 * @param unit The tunit of this relative time
	 * @param time The time value of this relative time
	 */
	public RelTime(TUnit unit, long time) {		
		this.unit = Objects.requireNonNull(unit, "The unit was null");
		if(time < 0) {
			throw new IllegalArgumentException("The time value cannot be negative. Was [" + time + "]");
		}
		this.time = time;
	}
	
	/**
	 * Creates a RelativeTime from a string e.g. <b>100s</b> or <b>2d</b>
	 * @param value The string to build from
	 * @return The RelativeTime
	 */
	public static RelTime from(String value) {
		return TUnit.decode(value);
	}
	
	public static RelTime from(RelativeTime rt) {
		return new RelTime(TUnit.fromString(rt.getUnit()), rt.getTime());
	}
	
	public TimeUnit timeUnit() {
		if(unit.timeUnit!=null) return unit.timeUnit;
		return TUnit.MAX_WITH_TIME_UNIT.timeUnit;
	}
	
	public long unitTime() {
		if(unit.timeUnit!=null) return time;
		return unit.convert(TUnit.MAX_WITH_TIME_UNIT, time);
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return time + unit.name();
	}
	
	public String debugString() {
		return time + unit.name() + "[" + unitTime() + ":" + timeUnit().name() + "]";
	}
	
	/**
	 * Converts this relative time to a value in the specified unit
	 * @param unit The unit to convert to
	 * @return The converted time
	 */
	public long convertTo(TimeUnit unit) {
		return unit.convert(time * this.unit.inMs, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RelTime o) {
		long me = time * this.unit.inMs;
		long you = o.convertTo(TimeUnit.MILLISECONDS);
		return me==you ? 0 : me < you ? -1 : 1;
	}
	

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (time ^ (time >>> 32));
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RelTime other = (RelTime) obj;
		if (time != other.time)
			return false;
		if (unit != other.unit)
			return false;
		return true;
	}
	
}
