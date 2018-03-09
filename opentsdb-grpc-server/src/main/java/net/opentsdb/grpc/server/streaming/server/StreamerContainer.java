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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.MethodType;
import io.grpc.stub.StreamObserver;

/**
 * <p>Title: StreamerContainer</p>
 * <p>Description: </p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.server.streaming.server.StreamerContainer</code></p>
 */

public class StreamerContainer<T, R> implements StreamerContainerMXBean {
	private static final Pattern METHOD_NAME_PARSER = Pattern.compile("(.*)?\\.(.*)?/(.*)");
	private static final String OBJECT_NAME_FMT = "net.opentsdb.grpc.server.streamer:package=%s,class=%s,method=%s";
	protected final Logger LOG;
	protected final ObjectName objectName;
	protected final MethodDescriptor<T,R> md;   // e.g. opentsdb.OpenTSDBService/Puts
	protected final StreamerBuilder<T,R> builder;
	
	protected final ServerStats ss = new ServerStats();
	
	protected final MethodType methodType;
	
	
	/**
	 * Creates a new StreamerContainer
	 */
	public StreamerContainer(StreamerBuilder<T,R> builder) {
		this.builder = builder;
		md = builder.methodDescriptor();
		methodType = md.getType();
		LOG = LoggerFactory.getLogger(md.getFullMethodName() + "." + getClass().getSimpleName());
		objectName = objectName();
	}
	
	public BidiServerStreamer<T,R> newBidiStreamer(StreamObserver<T> responseObserver) {
		if(methodType != MethodType.BIDI_STREAMING) {
			throw new IllegalArgumentException("The method [" + md.getFullMethodName() + "] is not of type BIDI_STREAMING");
		}
		return new BidiServerStreamer<T,R>(builder, ss, responseObserver);
	}
	
	public ServerStreamer<T,R> newServerStreamer(StreamObserver<T> responseObserver) {
		if(methodType != MethodType.SERVER_STREAMING) {
			throw new IllegalArgumentException("The method [" + md.getFullMethodName() + "] is not of type SERVER_STREAMING");
		}
		return new ServerStreamer<T,R>(builder, ss, responseObserver);
	}
	
	
	protected ObjectName objectName() {
		Matcher m = METHOD_NAME_PARSER.matcher(md.getFullMethodName());
		if(!m.matches()) {
			LOG.warn("Failed to parse MethodDescriptor name: {}", md.getFullMethodName());
			return null;
		}
		try {
			return new ObjectName(String.format(OBJECT_NAME_FMT, 
				m.group(1),
				m.group(2),
				m.group(3)
			));
		} catch (Exception ex) {
			LOG.warn("Failed to build ObjectName for MethodDescriptor name: {}", md.getFullMethodName(), ex);
			return null;
		}
	}
	
	protected void register() {
		if(objectName != null) {
			try {
				ManagementFactory.getPlatformMBeanServer().registerMBean(this, objectName);
			} catch (Exception ex) {
				LOG.warn("Failed to create JMX MBean for {} with ObjectName {}", md.getFullMethodName(), objectName, ex);
			}
		} else {
			LOG.warn("Cannot create JMX MBean for {} since ObjectName was not created", md.getFullMethodName());
		}
	}
	
	
//	public final AtomicLong activeStreams = new AtomicLong();
//	public final LongAdder receivedMessages = new LongAdder();
//	public final LongAdder sentMessages = new LongAdder();
//	public final LongAdder processedItems = new LongAdder();
//	public final LongAdder failedItems = new LongAdder();
	
	public long getActiveStreams() {
		return ss.activeStreams.get();
	}
	
	public long getReceivedMessages() {
		return ss.receivedMessages.longValue();
	}
	
	public long getSentMessages() {
		return ss.sentMessages.longValue();
	}
	
	public long getProcessedItems() {
		return ss.processedItems.longValue();
	}

	public long getFailedItems() {
		return ss.failedItems.longValue();
	}

}
