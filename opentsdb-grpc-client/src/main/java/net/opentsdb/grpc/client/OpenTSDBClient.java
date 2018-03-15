/**
 * 
 */
package net.opentsdb.grpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.MethodDescriptor;

/**
 * @author nwhitehead
 *
 */
public class OpenTSDBClient {
	protected static final Logger LOG = LoggerFactory.getLogger(OpenTSDBClient.class);
	
	
	
	
	
	
	
	protected class OpenTSDBClientInterceptor implements ClientInterceptor {

		/**
		 * {@inheritDoc}
		 * @see io.grpc.ClientInterceptor#interceptCall(io.grpc.MethodDescriptor, io.grpc.CallOptions, io.grpc.Channel)
		 */
		@Override
		public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
				CallOptions callOptions, Channel next) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
}
