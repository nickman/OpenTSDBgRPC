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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessageV3;

import io.grpc.MethodDescriptor;
import io.grpc.MethodDescriptor.Marshaller;
import io.grpc.MethodDescriptor.ReflectableMarshaller;
import net.opentsdb.grpc.OpenTSDBServiceGrpc;
import net.opentsdb.grpc.PutDatapoints;
import net.opentsdb.grpc.PutDatapointsResponse;
import net.opentsdb.grpc.TXTime;



/**
 * <p>Title: RPCTypes</p>
 * <p>Description: A cache of details about the send and return types of gRPC RPCs</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.common.RPCTypes</code></p>
 * @param <T> The request type
 * @param <R> The response type
 */

public class RPCTypes<T, R> {

	private static final Cache<MethodDescriptor<?, ?>, RPCTypes<?,?>> MD_CACHE = 
			CacheBuilder.newBuilder()
			.concurrencyLevel(Runtime.getRuntime().availableProcessors())
			.initialCapacity(128)
			.maximumSize(1024)
			.weakKeys()
			.recordStats()
			.build();
	
	private static Logger LOG = LoggerFactory.getLogger(RPCTypes.class);
	
	public static final String TX_TIME_FIELD_NAME = "txTime";
	public static final String IS_FINAL_FIELD_NAME = "finalResponse";
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final RPCTypes DEFAULT_TYPE = new RPCTypes(Object.class, Object.class, false, false, null, null, null, null, null); 
	
	public final Class<T> requestType;
	public final Class<R> responseType;
	public final boolean providesFinal;
	public final boolean hasTXTime;
	public final Descriptor descriptorT;
	public final Descriptor descriptorR;
	public final FieldDescriptor finalResponseFieldDescriptor;
	public final FieldDescriptor txTimeFieldDescriptorT;
	public final FieldDescriptor txTimeFieldDescriptorR;
	
	
	
	public RPCTypes(Class<T> requestType, Class<R> responseType, boolean providesFinal, boolean hasTXTime, 
			Descriptor descriptorT, Descriptor descriptorR, 
			FieldDescriptor txTimeFieldDescriptorT, FieldDescriptor txTimeFieldDescriptorR, FieldDescriptor finalResponseFieldDescriptor) {
		this.requestType = requestType;
		this.responseType = responseType;
		this.providesFinal = providesFinal;
		this.hasTXTime = hasTXTime;
		this.descriptorT =  descriptorT;
		this.descriptorR = descriptorR;
		this.finalResponseFieldDescriptor = finalResponseFieldDescriptor;
		this.txTimeFieldDescriptorT = txTimeFieldDescriptorT;
		this.txTimeFieldDescriptorR = txTimeFieldDescriptorR;
		
	}


	@SuppressWarnings("unchecked")
	RPCTypes(MethodDescriptor<T,R> md) {
		if(areBothGeneratedMessages(md)) {
			requestType = getGeneratedMessage(md.getRequestMarshaller());
			responseType = getGeneratedMessage(md.getResponseMarshaller());
			descriptorT = getDescriptor(requestType);
			descriptorR = getDescriptor(responseType);
			finalResponseFieldDescriptor = descriptorR.findFieldByName(IS_FINAL_FIELD_NAME);
			providesFinal = finalResponseFieldDescriptor != null;
			txTimeFieldDescriptorT = descriptorT.findFieldByName(TX_TIME_FIELD_NAME);
			txTimeFieldDescriptorR = descriptorR.findFieldByName(TX_TIME_FIELD_NAME);
			hasTXTime = txTimeFieldDescriptorT != null && txTimeFieldDescriptorR != null;
		} else {
			requestType = (Class<T>) Object.class;
			responseType = (Class<R>) Object.class;
			providesFinal = false;
			hasTXTime = false;
			descriptorT = null;
			descriptorR = null; 
			finalResponseFieldDescriptor = null;
			txTimeFieldDescriptorT = null;
			txTimeFieldDescriptorR = null;
			
		}
		LOG.info("Cached RPCTypes: {}", toString());
	}
	
	
	private boolean areBothGeneratedMessages(MethodDescriptor<T,R> md) {
		return getGeneratedMessage(md.getRequestMarshaller()) != null &&
				getGeneratedMessage(md.getResponseMarshaller()) != null;
	}
	
	private static <E> Class<E> getGeneratedMessage(Marshaller<E> marshaller) {
		if(marshaller instanceof ReflectableMarshaller) {
			Class<E> type = ((ReflectableMarshaller<E>)marshaller).getMessageClass();
			if(GeneratedMessageV3.class.isAssignableFrom(type)) {
				return type;
			}
		}
		return null;
	}
	
	private static Descriptor getDescriptor(Class<?> messageType) {
		try {
			return (Descriptor) messageType.getDeclaredMethod("getDescriptor").invoke(null);
		} catch (Exception ex) {
			return null;
		}
	}
	
	
	public TXTime getTXTimeT(T t) {
		try {
			if(txTimeFieldDescriptorT==null) {
				return null;
			}
			return (TXTime)((GeneratedMessageV3)t).getField(txTimeFieldDescriptorT);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to get TXTime from instance of " + t.getClass().getName(), ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public T setTXTimeT(T t, TXTime txTime) {
		try {
			if(txTimeFieldDescriptorT!=null) {
				GeneratedMessageV3 msg = (GeneratedMessageV3)t;
				return (T)msg.newBuilderForType().mergeFrom(msg).setField(txTimeFieldDescriptorT, txTime).build();
			}
			return t;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to set TXTime on instance of " + t.getClass().getName(), ex);
		}
	}
	
	public TXTime getTXTimeR(R r) {
		try {
			if(txTimeFieldDescriptorR==null) {
				return null;
			}
			return (TXTime)((GeneratedMessageV3)r).getField(txTimeFieldDescriptorR);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to get TXTime from instance of " + r.getClass().getName(), ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public R setTXTimeR(R r, TXTime txTime) {
		try {
			if(txTimeFieldDescriptorR!=null) {
				GeneratedMessageV3 msg = (GeneratedMessageV3)r;
				return (R)msg.newBuilderForType().mergeFrom(msg).setField(txTimeFieldDescriptorR, txTime).build();
			}
			return r;
		} catch (Exception ex) {
			throw new RuntimeException("Failed to set TXTime on instance of " + r.getClass().getName(), ex);
		}
	}

	
	
	public static void main(String[] args) {
		RPCTypes.getRpcTypesFor(OpenTSDBServiceGrpc.getPutMethod());
		GeneratedMessageV3 gm = PutDatapoints.newBuilder().build();
		Descriptor d = gm.getDescriptorForType();
		for(FieldDescriptor fd : d.getFields()) {
			LOG.info("Field: {} / {}", fd.getName(), System.identityHashCode(fd));
			FieldDescriptor bfd = PutDatapoints.newBuilder().getDescriptorForType().findFieldByName(fd.getName());
			LOG.info("Builder Field: {} / {}", bfd.getName(), System.identityHashCode(bfd));
		}
		System.out.println("===============================================================================================");
		gm = PutDatapointsResponse.newBuilder().build();
		d = gm.getDescriptorForType();
		for(FieldDescriptor fd : d.getFields()) {
			LOG.info("Field: {} / {}", fd.getName(), System.identityHashCode(fd));
			FieldDescriptor bfd = PutDatapointsResponse.newBuilder().getDescriptorForType().findFieldByName(fd.getName());
			LOG.info("Builder Field: {} / {}", bfd.getName(), System.identityHashCode(bfd));
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public static <T,R> RPCTypes<T,R> getRpcTypesFor(MethodDescriptor<T,R> md) {
		try {
			return (RPCTypes<T, R>) MD_CACHE.get(md, new Callable<RPCTypes<T,R>>(){
				public RPCTypes<T,R> call() {
					return new RPCTypes<T,R>(md);
				}
			});
		} catch (ExecutionException e) {
			return DEFAULT_TYPE;
		}
	}
	
	
	public String toString() {
		return new StringBuilder("RPCType: rq=")
			.append(requestType.getName())
			.append(", resp=").append(responseType.getName())
			.append(", pf=").append(providesFinal)
			.append(", tx=").append(hasTXTime)
			.toString();
	}

}
