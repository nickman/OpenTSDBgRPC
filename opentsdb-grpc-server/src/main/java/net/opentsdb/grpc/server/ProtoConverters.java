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

import java.util.Map;
import java.util.Objects;

import com.google.protobuf.ByteString;

import net.opentsdb.grpc.TSDBAnnotation;
import net.opentsdb.grpc.Tsuid;
import net.opentsdb.meta.Annotation;
import net.opentsdb.uid.UniqueId;

/**
 * <p>Title: ProtoConverters</p>
 * <p>Description: Converters between the gRPC types and native OpenTSDB types</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.ProtoConverters</code></p>
 */

public class ProtoConverters {
	
	/**
	 * Builds a TSDBAnnotation gRPC object from an OpenTSDB annotation
	 * @param a The OpenTSDB annotation
	 * @return The TSDBAnnotation gRPC object
	 */
	public static TSDBAnnotation from(Annotation a) {
		Objects.requireNonNull(a, "The passed Annotation was null");
		TSDBAnnotation.Builder b = TSDBAnnotation.newBuilder();
		if(a.getDescription() != null) {
			b.setDescription(a.getDescription());
		}
		b.setEndTime(a.getEndTime());
		if(a.getNotes() != null) {
			b.setNotes(a.getNotes());
		}
		b.setStartTime(a.getStartTime());
		String tsuid = a.getTSUID();
		if(tsuid != null) {
			b.setTsuid(Tsuid.newBuilder()
					.setTsuidName(a.getTSUID())
					.setTsuidBytes(ByteString.copyFrom(UniqueId.stringToUid(a.getTSUID())))
					.build()
			);
		}
		Map<String, String> c = a.getCustom();
		if(c != null && !c.isEmpty()) {
			b.putAllCustom(c);
		}		
		return b.build();
	}
	
	public static Annotation from(TSDBAnnotation a) {
		Objects.requireNonNull(a, "The passed TSDBAnnotation was null");
		Annotation an = new Annotation();
		Map<String, String> c = a.getCustomMap();
		if(c != null && !c.isEmpty()) {
			an.setCustom(c);
		}
		an.setDescription(a.getDescription());		
		an.setEndTime(a.getEndTime());
		an.setNotes(a.getNotes());
		an.setStartTime(a.getStartTime());
		Tsuid tsuid = a.getTsuid();
		ByteString bs = tsuid.getTsuidBytes();
		String name = tsuid.getTsuidName();
		String ts = null;
		if(name==null) {
			if(bs!=null) {
				ts = UniqueId.uidToString(bs.toByteArray());
			}
		} else {
			ts = name;
		}
		an.setTSUID(ts);
		return an;
	}
	
}
