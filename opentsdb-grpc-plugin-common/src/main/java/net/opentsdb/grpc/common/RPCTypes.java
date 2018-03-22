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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
import net.opentsdb.grpc.TXTime;



/**
 * <p>Title: RPCTypes</p>
 * <p>Description: A cache of details about the send and return types of gRPC RPCs</p> 
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>net.opentsdb.grpc.common.RPCTypes</code></p>
 */

public class RPCTypes<T, R> {

	private static final Cache<MethodDescriptor<?, ?>, RPCTypes<?,?>> TYPE_CACHE = 
			CacheBuilder.newBuilder()
			.concurrencyLevel(Runtime.getRuntime().availableProcessors())
			.initialCapacity(128)
			.maximumSize(1024)
			.weakKeys()
			.recordStats()
			.build();

	private static Logger LOG = LoggerFactory.getLogger(RPCTypes.class);
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final RPCTypes DEFAULT_TYPE = new RPCTypes(Object.class, Object.class, false, false, null, null, null, null); 
	
	public final Class<T> requestType;
	public final Class<R> responseType;
	public final boolean providesFinal;
	public final boolean hasTXTime;
	private final Method getTxMethodT;
	private final Method getTxMethodR;
	private final Method setTxMethodT;
	private final Method setTxMethodR;
	
	
	
	public RPCTypes(Class<T> requestType, Class<R> responseType, boolean providesFinal, boolean hasTXTime, Method getTxMethodT, Method getTxMethodR,  Method setTxMethodT, Method setTxMethodR) {
		this.requestType = requestType;
		this.responseType = responseType;
		this.providesFinal = providesFinal;
		this.hasTXTime = hasTXTime;
		this.getTxMethodT = getTxMethodT;
		this.getTxMethodR = getTxMethodR;
		this.setTxMethodT = setTxMethodT;
		this.setTxMethodR = setTxMethodR;		
		
	}


	@SuppressWarnings("unchecked")
	RPCTypes(MethodDescriptor<T,R> md) {
		Marshaller<T> rqm = md.getRequestMarshaller();
		boolean bothReflectable = true;
		if(rqm instanceof ReflectableMarshaller) {
			requestType = ((ReflectableMarshaller<T>)rqm).getMessageClass();
		} else {
			requestType = (Class<T>) Object.class;
			bothReflectable = false;
		}
		Marshaller<R> rsm = md.getResponseMarshaller();
		if(rsm instanceof ReflectableMarshaller) {
			responseType = ((ReflectableMarshaller<R>)rsm).getMessageClass();
		} else {
			responseType = (Class<R>) Object.class;
			bothReflectable = false;
		}	
		if(!bothReflectable) {
			providesFinal = false;
			hasTXTime = false;
			getTxMethodT = null;
			getTxMethodR = null;
			setTxMethodT = null;
			setTxMethodR = null;						
		} else {
			providesFinal = getMethod(responseType, boolean.class, "getFinalResponse") != null;
			getTxMethodT = getMethod(requestType, TXTime.class, "getTxTime");
			getTxMethodR = getMethod(responseType, TXTime.class, "getTxTime");
			setTxMethodT = getSetMethod(requestType, TXTime.class, "setTxTime");
			setTxMethodR = getSetMethod(responseType, TXTime.class, "setTxTime");
			
			hasTXTime = getTxMethodT != null && getTxMethodR != null;
		}
		LOG.info("Cached RPCTypes: {}", toString());
	}
	
	
	public TXTime getTXTimeT(T t) {
		try {
			return (TXTime)getTxMethodT.invoke(t);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to get TXTime from instance of " + t.getClass().getName(), ex);
		}
	}
	
	public void setTXTimeT(Object builder, TXTime txTime) {
		try {
			setTxMethodT.invoke(builder, txTime);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to set TXTime on instance of " + builder.getClass().getName(), ex);
		}
	}
	
	public TXTime getTXTimeR(R r) {
		try {
			return (TXTime)getTxMethodT.invoke(r);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to get TXTime from instance of " + r.getClass().getName(), ex);
		}
	}

	public void setTXTimeR(Object builder, TXTime txTime) {
		try {
			setTxMethodT.invoke(builder, txTime);
		} catch (Exception ex) {
			throw new RuntimeException("Failed to set TXTime on instance of " + builder.getClass().getName(), ex);
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
	}
	
	@SuppressWarnings("unchecked")
	public static <T,R> RPCTypes<T,R> getRpcTypesFor(MethodDescriptor<T,R> md) {
		try {
			return (RPCTypes<T, R>) TYPE_CACHE.get(md, new Callable<RPCTypes<T,R>>(){
				public RPCTypes<T,R> call() {
					return new RPCTypes<T,R>(md);
				}
			});
		} catch (ExecutionException e) {
			return DEFAULT_TYPE;
		}
	}
	
	private static Method getMethod(Class<?> clazz, Class<?> returnType, String name) {
		try {
			Method m = clazz.getDeclaredMethod(name);
			if(m.getReturnType()==returnType && Modifier.isPublic(m.getModifiers())) {
				return m;
			}
			return null;
		} catch (Exception ex) {
			return null;
		}
	}
	
	private static Method getSetMethod(Class<?> clazz, Class<?> paramType, String name) {
		try {
			final String builderClassName = clazz.getName() + "$Builder";
			Class<?> builderClass = null;
			for(Class<?> subClass : clazz.getClasses()) {
				if(builderClassName.equals(subClass.getName())) {
					builderClass = subClass;
					break;
				}
			}
			if(builderClass==null) {
				return null;
			}
			Method m = builderClass.getDeclaredMethod(name);
			if(m.getParameterTypes()[0]==paramType && Modifier.isPublic(m.getModifiers())) {
				return m;
			}
			return null;
		} catch (Exception ex) {
			return null;
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
