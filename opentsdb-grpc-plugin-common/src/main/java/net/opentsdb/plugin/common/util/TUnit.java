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

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Title: TUnit</p>
 * <p>Description: Decodes and functions for OpenTSDB's relative time format. </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.util.TUnit</code></p>
 */

public enum TUnit {
	ms(1, TimeUnit.MILLISECONDS),
	s(1000, TimeUnit.SECONDS),
	m(1000 * 60, TimeUnit.MINUTES),
	h(1000 * 60 * 60, TimeUnit.HOURS),
	d(1000 * 60 * 60 * 24, TimeUnit.DAYS),
	w(1000 * 60 * 60 * 24 * 7, null),
	n(1000 * 60 * 60 * 24 * 7 * 30, null),
	y(1000 * 60 * 60 * 24 * 7 * 30 * 365, null);
	
	private TUnit(long inMs, TimeUnit timeUnit) {
		this.inMs = inMs;
		this.timeUnit = timeUnit;
	}
	
	private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)(ms|s|m|h|d|w|n|y)", Pattern.CASE_INSENSITIVE);
	private static final Map<TimeUnit, TUnit> TIMEUNIT_TO_TUNIT;
	
	static {
		EnumMap<TimeUnit, TUnit> tmp = new EnumMap<>(TimeUnit.class);
		for(TUnit t: values()) {
			if(t.timeUnit!=null) {
				tmp.put(t.timeUnit, t);
			}
		}
		TIMEUNIT_TO_TUNIT = Collections.unmodifiableMap(tmp);
	}
	
	/** The value of this TUnit in ms. */
	public final long inMs;
	/** The TimeUnit equivalent or null if not implemented */
	public final TimeUnit timeUnit;
	
	public long convert(TUnit unit, long time) {
		int me = ordinal();
		int you = unit.ordinal();
		if(me==you) return time;
		return (time * unit.inMs) / inMs;
		
	}
	
	public static void main(String[] args) {
		System.out.println("18h to days:" + TUnit.d.convert(TUnit.h, 18));
		System.out.println("18d to hours:" + TUnit.h.convert(TUnit.d, 18));
		System.out.println("18d to days:" + TUnit.d.convert(TUnit.d, 18));
	}
	
	public static final TUnit MAX_WITH_TIME_UNIT = d;
	
	public TUnit forTimeUnit(TimeUnit unit) {
		return TIMEUNIT_TO_TUNIT.get(unit);
	}
	
	/**
	 * Decodes the passed string to a TUnit, lowercasing and trimming the value
	 * @param value The value to decode
	 * @return The decoded tunit
	 */
	public static TUnit fromString(String value) {
		if(value==null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("The passed value was null or empty");
		}
		try {
			return valueOf(value.trim().toLowerCase());
		} catch (Exception x) {
			throw new IllegalArgumentException("The value [" + value + "] is not a valid TUnit");
		}
	}
	
	/**
	 * Decodes the passed string to a RelativeTime
	 * e.g. <b>100s</b> or <b>2d</b>
	 * @param value The value to decode
	 * @return the decoded RelativeTime
	 */
	public static RelTime decode(String value) {
		if(value==null || value.trim().isEmpty()) {
			throw new IllegalArgumentException("The passed value was null or empty");
		}
		String v = value.replace(" ", "").toLowerCase();
		Matcher m = TIME_PATTERN.matcher(v);
		if(!m.matches()) {
			throw new IllegalArgumentException("Cannot decode the value [" + value + "] to a relative time");
		}
		return new RelTime(fromString(m.group(2)), Long.parseLong(m.group(1)));
	}
}
