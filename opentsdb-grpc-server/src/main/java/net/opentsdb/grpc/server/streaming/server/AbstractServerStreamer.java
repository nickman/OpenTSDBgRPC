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
package net.opentsdb.grpc.server.streaming.server;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.MethodDescriptor;
import io.grpc.stub.StreamObserver;
import net.opentsdb.grpc.PutDatapoints;
import net.opentsdb.grpc.server.util.AccumulatingLongAdder;

/**
 * <p>Title: BidiServerStreamer</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.streaming.server.AbstractServerStreamer</code></p>
 */

public abstract class AbstractServerStreamer<T, R> implements StreamObserver<T> {
	
	protected final AtomicBoolean open = new AtomicBoolean(false);
	protected long startTime = -1;
	
	protected final AtomicLong activeStreams;
	protected final LongAdder receivedMessages;
	protected final LongAdder sentMessages;
	protected final LongAdder processedItems;
	protected final LongAdder failedItems;
	protected final StreamObserver<T> responseObserver;
	
	protected final MethodDescriptor<T,R> md;   // e.g. opentsdb.OpenTSDBService/Puts
	
	protected final Logger LOG;
	
	
	
//	activeStreams.incrementAndGet();
//	totalStreams.increment();
//	
//	return new StreamObserver<PutDatapoints>() {			
//		final long startTime = System.currentTimeMillis();
//		final LongAdder _receivedDataPoints = new AccumulatingLongAdder(receivedDataPoints);
//		final LongAdder _okDataPoints = new AccumulatingLongAdder(okDataPoints);
//		final LongAdder _failedDataPoints = new AccumulatingLongAdder(failedDataPoints);
	
	
	/**
	 * Creates a new BidiServerStreamer
	 */
	public AbstractServerStreamer(StreamerBuilder<T,R> builder, ServerStats ss, StreamObserver<T> responseObserver) {
		md = builder.methodDescriptor();
		LOG = LoggerFactory.getLogger(md.getFullMethodName() + "." + getClass().getSimpleName());
		activeStreams = ss.activeStreams;
		receivedMessages = ss.accReceivedMessages();
		sentMessages = ss.accSentMessages();
		processedItems = ss.accProcessedItems();
		failedItems = ss.accFailedItems();
		this.responseObserver = responseObserver;
	}
	
	public void start() {
		startTime = System.currentTimeMillis();
		open.set(true);
		activeStreams.incrementAndGet();
	}
	
	 

	@Override
	public void onNext(T value) {
		receivedMessages.increment();
		
	}

	@Override
	public void onError(Throwable t) {
		if(open.compareAndSet(true, false)) {
			activeStreams.decrementAndGet();
		}
		
	}

	@Override
	public void onCompleted() {
		if(open.compareAndSet(true, false)) {
			activeStreams.decrementAndGet();
		}
	}
	

}
