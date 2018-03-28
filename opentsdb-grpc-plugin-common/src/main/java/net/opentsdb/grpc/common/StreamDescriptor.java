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
package net.opentsdb.grpc.common;

import io.grpc.CallOptions.Key;
import net.opentsdb.grpc.CreateAnnotationResponse;
import net.opentsdb.grpc.PutDatapoints;
import net.opentsdb.grpc.PutDatapointsResponse;
import net.opentsdb.grpc.TSDBAnnotations;
import net.opentsdb.grpc.TXTime;

/**
 * <p>Title: StreamDescriptor</p>
 * <p>Description: Generic internal descriptors for streamed types</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.client.StreamDescriptor</code></p>
 */

public interface StreamDescriptor<T, R> {
	
	/**
	 * Returns the number of sub items in an inbound argument
	 * @param t The inbound argument to count sub items for
	 * @return the number of sub items
	 */
	default long subItemsIn(T t) { return 1L; }
	
	/**
	 * Returns the number of sub items in an return value
	 * @param r The return value to count sub items for
	 * @return the number of sub items
	 */
	default long subItemsOut(R r) { return 1L; }
	
	/**
	 * Determines if the passed return value is a final response
	 * @param r The return value to test
	 * @return true if a final response, false otherwise
	 */
	default boolean finalResponse(R r) { return false; }
	
	/**
	 * Retreives and returns the TXTime from the passed input
	 * @param t The input
	 * @return the TXTime or null if the input does not support
	 */
	default TXTime getTxTimeT(T t) {
		return null;
	}

	/**
	 * Retreives and returns the TXTime from the passed output
	 * @param r The output
	 * @return the TXTime or null if the output does not support
	 */
	default TXTime getTxTimeR(R r) {
		return null;
	}
	
	default void setTxTimeT(T t, TXTime tx) {
		
	}
	
	default void setTxTimeR(R r, TXTime tx) {
		
	}
	
	
	/**
	 * <p>Title: DatapointsDescriptor</p>
	 * <p>Description: A StreamDescriptor for Datapoints</p> 
	 * @author Whitehead (nwhitehead AT heliosdev DOT org)
	 * <p><code>net.opentsdb.grpc.client.StreamDescriptor.DatapointsDescriptor</code></p>
	 */
	public static class DatapointsDescriptor implements StreamDescriptor<PutDatapoints, PutDatapointsResponse> {
		private DatapointsDescriptor() {}
		public static final DatapointsDescriptor INSTANCE = new DatapointsDescriptor();
		public static final Key<Integer> QUEUE_SIZE = Key.of("QueueSize", 0);
		@Override
		public long subItemsIn(PutDatapoints t) { return t.getDataPointsCount(); }
		@Override
		public long subItemsOut(PutDatapointsResponse r) { return r.getFailed() + r.getSuccess(); }
		@Override
		public boolean finalResponse(PutDatapointsResponse r) { return r.getFinalResponse(); }		
	}
	
	public static class AnnotationsDescriptor implements StreamDescriptor<TSDBAnnotations, CreateAnnotationResponse> {
		private AnnotationsDescriptor() {}
		public static final AnnotationsDescriptor INSTANCE = new AnnotationsDescriptor();
		public static final Key<Integer> QUEUE_SIZE = Key.of("QueueSize", 0);
		@Override
		public long subItemsIn(TSDBAnnotations t) { return t.getAnnotationsCount(); }
		@Override
		public long subItemsOut(CreateAnnotationResponse r) { return r.getFailed() + r.getSuccess(); }
		@Override
		public boolean finalResponse(CreateAnnotationResponse r) { return r.getFinalResponse(); }		
	}
	
}
