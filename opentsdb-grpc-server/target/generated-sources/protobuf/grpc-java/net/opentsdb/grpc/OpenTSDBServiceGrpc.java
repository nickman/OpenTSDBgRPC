package net.opentsdb.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.9.0)",
    comments = "Source: opentsdb.proto")
public final class OpenTSDBServiceGrpc {

  private OpenTSDBServiceGrpc() {}

  public static final String SERVICE_NAME = "opentsdb.OpenTSDBService";

  // Static method descriptors that strictly reflect the proto.
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getSMethod()} instead. 
  public static final io.grpc.MethodDescriptor<net.opentsdb.grpc.ContentName,
      net.opentsdb.grpc.Content> METHOD_S = getSMethod();

  private static volatile io.grpc.MethodDescriptor<net.opentsdb.grpc.ContentName,
      net.opentsdb.grpc.Content> getSMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<net.opentsdb.grpc.ContentName,
      net.opentsdb.grpc.Content> getSMethod() {
    io.grpc.MethodDescriptor<net.opentsdb.grpc.ContentName, net.opentsdb.grpc.Content> getSMethod;
    if ((getSMethod = OpenTSDBServiceGrpc.getSMethod) == null) {
      synchronized (OpenTSDBServiceGrpc.class) {
        if ((getSMethod = OpenTSDBServiceGrpc.getSMethod) == null) {
          OpenTSDBServiceGrpc.getSMethod = getSMethod = 
              io.grpc.MethodDescriptor.<net.opentsdb.grpc.ContentName, net.opentsdb.grpc.Content>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "opentsdb.OpenTSDBService", "S"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.ContentName.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.Content.getDefaultInstance()))
                  .setSchemaDescriptor(new OpenTSDBServiceMethodDescriptorSupplier("S"))
                  .build();
          }
        }
     }
     return getSMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getGetAggregatorsMethod()} instead. 
  public static final io.grpc.MethodDescriptor<net.opentsdb.grpc.Empty,
      net.opentsdb.grpc.AggregatorNames> METHOD_GET_AGGREGATORS = getGetAggregatorsMethod();

  private static volatile io.grpc.MethodDescriptor<net.opentsdb.grpc.Empty,
      net.opentsdb.grpc.AggregatorNames> getGetAggregatorsMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<net.opentsdb.grpc.Empty,
      net.opentsdb.grpc.AggregatorNames> getGetAggregatorsMethod() {
    io.grpc.MethodDescriptor<net.opentsdb.grpc.Empty, net.opentsdb.grpc.AggregatorNames> getGetAggregatorsMethod;
    if ((getGetAggregatorsMethod = OpenTSDBServiceGrpc.getGetAggregatorsMethod) == null) {
      synchronized (OpenTSDBServiceGrpc.class) {
        if ((getGetAggregatorsMethod = OpenTSDBServiceGrpc.getGetAggregatorsMethod) == null) {
          OpenTSDBServiceGrpc.getGetAggregatorsMethod = getGetAggregatorsMethod = 
              io.grpc.MethodDescriptor.<net.opentsdb.grpc.Empty, net.opentsdb.grpc.AggregatorNames>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "opentsdb.OpenTSDBService", "GetAggregators"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.AggregatorNames.getDefaultInstance()))
                  .setSchemaDescriptor(new OpenTSDBServiceMethodDescriptorSupplier("GetAggregators"))
                  .build();
          }
        }
     }
     return getGetAggregatorsMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getGetAnnotationMethod()} instead. 
  public static final io.grpc.MethodDescriptor<net.opentsdb.grpc.AnnotationRequest,
      net.opentsdb.grpc.TSDBAnnotation> METHOD_GET_ANNOTATION = getGetAnnotationMethod();

  private static volatile io.grpc.MethodDescriptor<net.opentsdb.grpc.AnnotationRequest,
      net.opentsdb.grpc.TSDBAnnotation> getGetAnnotationMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<net.opentsdb.grpc.AnnotationRequest,
      net.opentsdb.grpc.TSDBAnnotation> getGetAnnotationMethod() {
    io.grpc.MethodDescriptor<net.opentsdb.grpc.AnnotationRequest, net.opentsdb.grpc.TSDBAnnotation> getGetAnnotationMethod;
    if ((getGetAnnotationMethod = OpenTSDBServiceGrpc.getGetAnnotationMethod) == null) {
      synchronized (OpenTSDBServiceGrpc.class) {
        if ((getGetAnnotationMethod = OpenTSDBServiceGrpc.getGetAnnotationMethod) == null) {
          OpenTSDBServiceGrpc.getGetAnnotationMethod = getGetAnnotationMethod = 
              io.grpc.MethodDescriptor.<net.opentsdb.grpc.AnnotationRequest, net.opentsdb.grpc.TSDBAnnotation>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "opentsdb.OpenTSDBService", "GetAnnotation"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.AnnotationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.TSDBAnnotation.getDefaultInstance()))
                  .setSchemaDescriptor(new OpenTSDBServiceMethodDescriptorSupplier("GetAnnotation"))
                  .build();
          }
        }
     }
     return getGetAnnotationMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getCreateAnnotationsMethod()} instead. 
  public static final io.grpc.MethodDescriptor<net.opentsdb.grpc.TSDBAnnotation,
      net.opentsdb.grpc.TSDBAnnotation> METHOD_CREATE_ANNOTATIONS = getCreateAnnotationsMethod();

  private static volatile io.grpc.MethodDescriptor<net.opentsdb.grpc.TSDBAnnotation,
      net.opentsdb.grpc.TSDBAnnotation> getCreateAnnotationsMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<net.opentsdb.grpc.TSDBAnnotation,
      net.opentsdb.grpc.TSDBAnnotation> getCreateAnnotationsMethod() {
    io.grpc.MethodDescriptor<net.opentsdb.grpc.TSDBAnnotation, net.opentsdb.grpc.TSDBAnnotation> getCreateAnnotationsMethod;
    if ((getCreateAnnotationsMethod = OpenTSDBServiceGrpc.getCreateAnnotationsMethod) == null) {
      synchronized (OpenTSDBServiceGrpc.class) {
        if ((getCreateAnnotationsMethod = OpenTSDBServiceGrpc.getCreateAnnotationsMethod) == null) {
          OpenTSDBServiceGrpc.getCreateAnnotationsMethod = getCreateAnnotationsMethod = 
              io.grpc.MethodDescriptor.<net.opentsdb.grpc.TSDBAnnotation, net.opentsdb.grpc.TSDBAnnotation>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "opentsdb.OpenTSDBService", "CreateAnnotations"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.TSDBAnnotation.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.TSDBAnnotation.getDefaultInstance()))
                  .setSchemaDescriptor(new OpenTSDBServiceMethodDescriptorSupplier("CreateAnnotations"))
                  .build();
          }
        }
     }
     return getCreateAnnotationsMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getUpdateAnnotationsMethod()} instead. 
  public static final io.grpc.MethodDescriptor<net.opentsdb.grpc.TSDBAnnotation,
      net.opentsdb.grpc.TSDBAnnotation> METHOD_UPDATE_ANNOTATIONS = getUpdateAnnotationsMethod();

  private static volatile io.grpc.MethodDescriptor<net.opentsdb.grpc.TSDBAnnotation,
      net.opentsdb.grpc.TSDBAnnotation> getUpdateAnnotationsMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<net.opentsdb.grpc.TSDBAnnotation,
      net.opentsdb.grpc.TSDBAnnotation> getUpdateAnnotationsMethod() {
    io.grpc.MethodDescriptor<net.opentsdb.grpc.TSDBAnnotation, net.opentsdb.grpc.TSDBAnnotation> getUpdateAnnotationsMethod;
    if ((getUpdateAnnotationsMethod = OpenTSDBServiceGrpc.getUpdateAnnotationsMethod) == null) {
      synchronized (OpenTSDBServiceGrpc.class) {
        if ((getUpdateAnnotationsMethod = OpenTSDBServiceGrpc.getUpdateAnnotationsMethod) == null) {
          OpenTSDBServiceGrpc.getUpdateAnnotationsMethod = getUpdateAnnotationsMethod = 
              io.grpc.MethodDescriptor.<net.opentsdb.grpc.TSDBAnnotation, net.opentsdb.grpc.TSDBAnnotation>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "opentsdb.OpenTSDBService", "UpdateAnnotations"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.TSDBAnnotation.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.TSDBAnnotation.getDefaultInstance()))
                  .setSchemaDescriptor(new OpenTSDBServiceMethodDescriptorSupplier("UpdateAnnotations"))
                  .build();
          }
        }
     }
     return getUpdateAnnotationsMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getDeleteAnnotationsMethod()} instead. 
  public static final io.grpc.MethodDescriptor<net.opentsdb.grpc.TSDBAnnotation,
      net.opentsdb.grpc.TSDBAnnotation> METHOD_DELETE_ANNOTATIONS = getDeleteAnnotationsMethod();

  private static volatile io.grpc.MethodDescriptor<net.opentsdb.grpc.TSDBAnnotation,
      net.opentsdb.grpc.TSDBAnnotation> getDeleteAnnotationsMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<net.opentsdb.grpc.TSDBAnnotation,
      net.opentsdb.grpc.TSDBAnnotation> getDeleteAnnotationsMethod() {
    io.grpc.MethodDescriptor<net.opentsdb.grpc.TSDBAnnotation, net.opentsdb.grpc.TSDBAnnotation> getDeleteAnnotationsMethod;
    if ((getDeleteAnnotationsMethod = OpenTSDBServiceGrpc.getDeleteAnnotationsMethod) == null) {
      synchronized (OpenTSDBServiceGrpc.class) {
        if ((getDeleteAnnotationsMethod = OpenTSDBServiceGrpc.getDeleteAnnotationsMethod) == null) {
          OpenTSDBServiceGrpc.getDeleteAnnotationsMethod = getDeleteAnnotationsMethod = 
              io.grpc.MethodDescriptor.<net.opentsdb.grpc.TSDBAnnotation, net.opentsdb.grpc.TSDBAnnotation>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "opentsdb.OpenTSDBService", "DeleteAnnotations"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.TSDBAnnotation.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.TSDBAnnotation.getDefaultInstance()))
                  .setSchemaDescriptor(new OpenTSDBServiceMethodDescriptorSupplier("DeleteAnnotations"))
                  .build();
          }
        }
     }
     return getDeleteAnnotationsMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getBulkDeleteAnnotationsMethod()} instead. 
  public static final io.grpc.MethodDescriptor<net.opentsdb.grpc.BulkAnnotationRequest,
      net.opentsdb.grpc.BulkAnnotationResponse> METHOD_BULK_DELETE_ANNOTATIONS = getBulkDeleteAnnotationsMethod();

  private static volatile io.grpc.MethodDescriptor<net.opentsdb.grpc.BulkAnnotationRequest,
      net.opentsdb.grpc.BulkAnnotationResponse> getBulkDeleteAnnotationsMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<net.opentsdb.grpc.BulkAnnotationRequest,
      net.opentsdb.grpc.BulkAnnotationResponse> getBulkDeleteAnnotationsMethod() {
    io.grpc.MethodDescriptor<net.opentsdb.grpc.BulkAnnotationRequest, net.opentsdb.grpc.BulkAnnotationResponse> getBulkDeleteAnnotationsMethod;
    if ((getBulkDeleteAnnotationsMethod = OpenTSDBServiceGrpc.getBulkDeleteAnnotationsMethod) == null) {
      synchronized (OpenTSDBServiceGrpc.class) {
        if ((getBulkDeleteAnnotationsMethod = OpenTSDBServiceGrpc.getBulkDeleteAnnotationsMethod) == null) {
          OpenTSDBServiceGrpc.getBulkDeleteAnnotationsMethod = getBulkDeleteAnnotationsMethod = 
              io.grpc.MethodDescriptor.<net.opentsdb.grpc.BulkAnnotationRequest, net.opentsdb.grpc.BulkAnnotationResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "opentsdb.OpenTSDBService", "BulkDeleteAnnotations"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.BulkAnnotationRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.BulkAnnotationResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new OpenTSDBServiceMethodDescriptorSupplier("BulkDeleteAnnotations"))
                  .build();
          }
        }
     }
     return getBulkDeleteAnnotationsMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getGetConfigurationMethod()} instead. 
  public static final io.grpc.MethodDescriptor<net.opentsdb.grpc.Empty,
      net.opentsdb.grpc.KeyValues> METHOD_GET_CONFIGURATION = getGetConfigurationMethod();

  private static volatile io.grpc.MethodDescriptor<net.opentsdb.grpc.Empty,
      net.opentsdb.grpc.KeyValues> getGetConfigurationMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<net.opentsdb.grpc.Empty,
      net.opentsdb.grpc.KeyValues> getGetConfigurationMethod() {
    io.grpc.MethodDescriptor<net.opentsdb.grpc.Empty, net.opentsdb.grpc.KeyValues> getGetConfigurationMethod;
    if ((getGetConfigurationMethod = OpenTSDBServiceGrpc.getGetConfigurationMethod) == null) {
      synchronized (OpenTSDBServiceGrpc.class) {
        if ((getGetConfigurationMethod = OpenTSDBServiceGrpc.getGetConfigurationMethod) == null) {
          OpenTSDBServiceGrpc.getGetConfigurationMethod = getGetConfigurationMethod = 
              io.grpc.MethodDescriptor.<net.opentsdb.grpc.Empty, net.opentsdb.grpc.KeyValues>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "opentsdb.OpenTSDBService", "GetConfiguration"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.KeyValues.getDefaultInstance()))
                  .setSchemaDescriptor(new OpenTSDBServiceMethodDescriptorSupplier("GetConfiguration"))
                  .build();
          }
        }
     }
     return getGetConfigurationMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getGetFilterMetasMethod()} instead. 
  public static final io.grpc.MethodDescriptor<net.opentsdb.grpc.Empty,
      net.opentsdb.grpc.FilterMetas> METHOD_GET_FILTER_METAS = getGetFilterMetasMethod();

  private static volatile io.grpc.MethodDescriptor<net.opentsdb.grpc.Empty,
      net.opentsdb.grpc.FilterMetas> getGetFilterMetasMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<net.opentsdb.grpc.Empty,
      net.opentsdb.grpc.FilterMetas> getGetFilterMetasMethod() {
    io.grpc.MethodDescriptor<net.opentsdb.grpc.Empty, net.opentsdb.grpc.FilterMetas> getGetFilterMetasMethod;
    if ((getGetFilterMetasMethod = OpenTSDBServiceGrpc.getGetFilterMetasMethod) == null) {
      synchronized (OpenTSDBServiceGrpc.class) {
        if ((getGetFilterMetasMethod = OpenTSDBServiceGrpc.getGetFilterMetasMethod) == null) {
          OpenTSDBServiceGrpc.getGetFilterMetasMethod = getGetFilterMetasMethod = 
              io.grpc.MethodDescriptor.<net.opentsdb.grpc.Empty, net.opentsdb.grpc.FilterMetas>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "opentsdb.OpenTSDBService", "GetFilterMetas"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.FilterMetas.getDefaultInstance()))
                  .setSchemaDescriptor(new OpenTSDBServiceMethodDescriptorSupplier("GetFilterMetas"))
                  .build();
          }
        }
     }
     return getGetFilterMetasMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getDropCachesMethod()} instead. 
  public static final io.grpc.MethodDescriptor<net.opentsdb.grpc.Empty,
      net.opentsdb.grpc.Empty> METHOD_DROP_CACHES = getDropCachesMethod();

  private static volatile io.grpc.MethodDescriptor<net.opentsdb.grpc.Empty,
      net.opentsdb.grpc.Empty> getDropCachesMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<net.opentsdb.grpc.Empty,
      net.opentsdb.grpc.Empty> getDropCachesMethod() {
    io.grpc.MethodDescriptor<net.opentsdb.grpc.Empty, net.opentsdb.grpc.Empty> getDropCachesMethod;
    if ((getDropCachesMethod = OpenTSDBServiceGrpc.getDropCachesMethod) == null) {
      synchronized (OpenTSDBServiceGrpc.class) {
        if ((getDropCachesMethod = OpenTSDBServiceGrpc.getDropCachesMethod) == null) {
          OpenTSDBServiceGrpc.getDropCachesMethod = getDropCachesMethod = 
              io.grpc.MethodDescriptor.<net.opentsdb.grpc.Empty, net.opentsdb.grpc.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "opentsdb.OpenTSDBService", "DropCaches"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.Empty.getDefaultInstance()))
                  .setSchemaDescriptor(new OpenTSDBServiceMethodDescriptorSupplier("DropCaches"))
                  .build();
          }
        }
     }
     return getDropCachesMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getPutMethod()} instead. 
  public static final io.grpc.MethodDescriptor<net.opentsdb.grpc.PutDatapoints,
      net.opentsdb.grpc.PutDatapointsResponse> METHOD_PUT = getPutMethod();

  private static volatile io.grpc.MethodDescriptor<net.opentsdb.grpc.PutDatapoints,
      net.opentsdb.grpc.PutDatapointsResponse> getPutMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<net.opentsdb.grpc.PutDatapoints,
      net.opentsdb.grpc.PutDatapointsResponse> getPutMethod() {
    io.grpc.MethodDescriptor<net.opentsdb.grpc.PutDatapoints, net.opentsdb.grpc.PutDatapointsResponse> getPutMethod;
    if ((getPutMethod = OpenTSDBServiceGrpc.getPutMethod) == null) {
      synchronized (OpenTSDBServiceGrpc.class) {
        if ((getPutMethod = OpenTSDBServiceGrpc.getPutMethod) == null) {
          OpenTSDBServiceGrpc.getPutMethod = getPutMethod = 
              io.grpc.MethodDescriptor.<net.opentsdb.grpc.PutDatapoints, net.opentsdb.grpc.PutDatapointsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "opentsdb.OpenTSDBService", "Put"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.PutDatapoints.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.PutDatapointsResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new OpenTSDBServiceMethodDescriptorSupplier("Put"))
                  .build();
          }
        }
     }
     return getPutMethod;
  }
  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  @java.lang.Deprecated // Use {@link #getPutsMethod()} instead. 
  public static final io.grpc.MethodDescriptor<net.opentsdb.grpc.DataPoint,
      net.opentsdb.grpc.PutDatapointsResponse> METHOD_PUTS = getPutsMethod();

  private static volatile io.grpc.MethodDescriptor<net.opentsdb.grpc.DataPoint,
      net.opentsdb.grpc.PutDatapointsResponse> getPutsMethod;

  @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
  public static io.grpc.MethodDescriptor<net.opentsdb.grpc.DataPoint,
      net.opentsdb.grpc.PutDatapointsResponse> getPutsMethod() {
    io.grpc.MethodDescriptor<net.opentsdb.grpc.DataPoint, net.opentsdb.grpc.PutDatapointsResponse> getPutsMethod;
    if ((getPutsMethod = OpenTSDBServiceGrpc.getPutsMethod) == null) {
      synchronized (OpenTSDBServiceGrpc.class) {
        if ((getPutsMethod = OpenTSDBServiceGrpc.getPutsMethod) == null) {
          OpenTSDBServiceGrpc.getPutsMethod = getPutsMethod = 
              io.grpc.MethodDescriptor.<net.opentsdb.grpc.DataPoint, net.opentsdb.grpc.PutDatapointsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "opentsdb.OpenTSDBService", "Puts"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.DataPoint.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  net.opentsdb.grpc.PutDatapointsResponse.getDefaultInstance()))
                  .setSchemaDescriptor(new OpenTSDBServiceMethodDescriptorSupplier("Puts"))
                  .build();
          }
        }
     }
     return getPutsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static OpenTSDBServiceStub newStub(io.grpc.Channel channel) {
    return new OpenTSDBServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static OpenTSDBServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new OpenTSDBServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static OpenTSDBServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new OpenTSDBServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class OpenTSDBServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void s(net.opentsdb.grpc.ContentName request,
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.Content> responseObserver) {
      asyncUnimplementedUnaryCall(getSMethod(), responseObserver);
    }

    /**
     */
    public void getAggregators(net.opentsdb.grpc.Empty request,
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.AggregatorNames> responseObserver) {
      asyncUnimplementedUnaryCall(getGetAggregatorsMethod(), responseObserver);
    }

    /**
     */
    public void getAnnotation(net.opentsdb.grpc.AnnotationRequest request,
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.TSDBAnnotation> responseObserver) {
      asyncUnimplementedUnaryCall(getGetAnnotationMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<net.opentsdb.grpc.TSDBAnnotation> createAnnotations(
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.TSDBAnnotation> responseObserver) {
      return asyncUnimplementedStreamingCall(getCreateAnnotationsMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<net.opentsdb.grpc.TSDBAnnotation> updateAnnotations(
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.TSDBAnnotation> responseObserver) {
      return asyncUnimplementedStreamingCall(getUpdateAnnotationsMethod(), responseObserver);
    }

    /**
     */
    public void deleteAnnotations(net.opentsdb.grpc.TSDBAnnotation request,
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.TSDBAnnotation> responseObserver) {
      asyncUnimplementedUnaryCall(getDeleteAnnotationsMethod(), responseObserver);
    }

    /**
     */
    public void bulkDeleteAnnotations(net.opentsdb.grpc.BulkAnnotationRequest request,
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.BulkAnnotationResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getBulkDeleteAnnotationsMethod(), responseObserver);
    }

    /**
     */
    public void getConfiguration(net.opentsdb.grpc.Empty request,
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.KeyValues> responseObserver) {
      asyncUnimplementedUnaryCall(getGetConfigurationMethod(), responseObserver);
    }

    /**
     */
    public void getFilterMetas(net.opentsdb.grpc.Empty request,
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.FilterMetas> responseObserver) {
      asyncUnimplementedUnaryCall(getGetFilterMetasMethod(), responseObserver);
    }

    /**
     */
    public void dropCaches(net.opentsdb.grpc.Empty request,
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.Empty> responseObserver) {
      asyncUnimplementedUnaryCall(getDropCachesMethod(), responseObserver);
    }

    /**
     */
    public void put(net.opentsdb.grpc.PutDatapoints request,
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.PutDatapointsResponse> responseObserver) {
      asyncUnimplementedUnaryCall(getPutMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<net.opentsdb.grpc.DataPoint> puts(
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.PutDatapointsResponse> responseObserver) {
      return asyncUnimplementedStreamingCall(getPutsMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getSMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                net.opentsdb.grpc.ContentName,
                net.opentsdb.grpc.Content>(
                  this, METHODID_S)))
          .addMethod(
            getGetAggregatorsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                net.opentsdb.grpc.Empty,
                net.opentsdb.grpc.AggregatorNames>(
                  this, METHODID_GET_AGGREGATORS)))
          .addMethod(
            getGetAnnotationMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                net.opentsdb.grpc.AnnotationRequest,
                net.opentsdb.grpc.TSDBAnnotation>(
                  this, METHODID_GET_ANNOTATION)))
          .addMethod(
            getCreateAnnotationsMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                net.opentsdb.grpc.TSDBAnnotation,
                net.opentsdb.grpc.TSDBAnnotation>(
                  this, METHODID_CREATE_ANNOTATIONS)))
          .addMethod(
            getUpdateAnnotationsMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                net.opentsdb.grpc.TSDBAnnotation,
                net.opentsdb.grpc.TSDBAnnotation>(
                  this, METHODID_UPDATE_ANNOTATIONS)))
          .addMethod(
            getDeleteAnnotationsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                net.opentsdb.grpc.TSDBAnnotation,
                net.opentsdb.grpc.TSDBAnnotation>(
                  this, METHODID_DELETE_ANNOTATIONS)))
          .addMethod(
            getBulkDeleteAnnotationsMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                net.opentsdb.grpc.BulkAnnotationRequest,
                net.opentsdb.grpc.BulkAnnotationResponse>(
                  this, METHODID_BULK_DELETE_ANNOTATIONS)))
          .addMethod(
            getGetConfigurationMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                net.opentsdb.grpc.Empty,
                net.opentsdb.grpc.KeyValues>(
                  this, METHODID_GET_CONFIGURATION)))
          .addMethod(
            getGetFilterMetasMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                net.opentsdb.grpc.Empty,
                net.opentsdb.grpc.FilterMetas>(
                  this, METHODID_GET_FILTER_METAS)))
          .addMethod(
            getDropCachesMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                net.opentsdb.grpc.Empty,
                net.opentsdb.grpc.Empty>(
                  this, METHODID_DROP_CACHES)))
          .addMethod(
            getPutMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                net.opentsdb.grpc.PutDatapoints,
                net.opentsdb.grpc.PutDatapointsResponse>(
                  this, METHODID_PUT)))
          .addMethod(
            getPutsMethod(),
            asyncBidiStreamingCall(
              new MethodHandlers<
                net.opentsdb.grpc.DataPoint,
                net.opentsdb.grpc.PutDatapointsResponse>(
                  this, METHODID_PUTS)))
          .build();
    }
  }

  /**
   */
  public static final class OpenTSDBServiceStub extends io.grpc.stub.AbstractStub<OpenTSDBServiceStub> {
    private OpenTSDBServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private OpenTSDBServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OpenTSDBServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new OpenTSDBServiceStub(channel, callOptions);
    }

    /**
     */
    public void s(net.opentsdb.grpc.ContentName request,
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.Content> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getSMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getAggregators(net.opentsdb.grpc.Empty request,
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.AggregatorNames> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetAggregatorsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getAnnotation(net.opentsdb.grpc.AnnotationRequest request,
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.TSDBAnnotation> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetAnnotationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<net.opentsdb.grpc.TSDBAnnotation> createAnnotations(
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.TSDBAnnotation> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getCreateAnnotationsMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<net.opentsdb.grpc.TSDBAnnotation> updateAnnotations(
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.TSDBAnnotation> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getUpdateAnnotationsMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public void deleteAnnotations(net.opentsdb.grpc.TSDBAnnotation request,
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.TSDBAnnotation> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDeleteAnnotationsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void bulkDeleteAnnotations(net.opentsdb.grpc.BulkAnnotationRequest request,
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.BulkAnnotationResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getBulkDeleteAnnotationsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getConfiguration(net.opentsdb.grpc.Empty request,
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.KeyValues> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetConfigurationMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getFilterMetas(net.opentsdb.grpc.Empty request,
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.FilterMetas> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getGetFilterMetasMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void dropCaches(net.opentsdb.grpc.Empty request,
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.Empty> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getDropCachesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void put(net.opentsdb.grpc.PutDatapoints request,
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.PutDatapointsResponse> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getPutMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<net.opentsdb.grpc.DataPoint> puts(
        io.grpc.stub.StreamObserver<net.opentsdb.grpc.PutDatapointsResponse> responseObserver) {
      return asyncBidiStreamingCall(
          getChannel().newCall(getPutsMethod(), getCallOptions()), responseObserver);
    }
  }

  /**
   */
  public static final class OpenTSDBServiceBlockingStub extends io.grpc.stub.AbstractStub<OpenTSDBServiceBlockingStub> {
    private OpenTSDBServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private OpenTSDBServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OpenTSDBServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new OpenTSDBServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public net.opentsdb.grpc.Content s(net.opentsdb.grpc.ContentName request) {
      return blockingUnaryCall(
          getChannel(), getSMethod(), getCallOptions(), request);
    }

    /**
     */
    public net.opentsdb.grpc.AggregatorNames getAggregators(net.opentsdb.grpc.Empty request) {
      return blockingUnaryCall(
          getChannel(), getGetAggregatorsMethod(), getCallOptions(), request);
    }

    /**
     */
    public net.opentsdb.grpc.TSDBAnnotation getAnnotation(net.opentsdb.grpc.AnnotationRequest request) {
      return blockingUnaryCall(
          getChannel(), getGetAnnotationMethod(), getCallOptions(), request);
    }

    /**
     */
    public net.opentsdb.grpc.TSDBAnnotation deleteAnnotations(net.opentsdb.grpc.TSDBAnnotation request) {
      return blockingUnaryCall(
          getChannel(), getDeleteAnnotationsMethod(), getCallOptions(), request);
    }

    /**
     */
    public net.opentsdb.grpc.BulkAnnotationResponse bulkDeleteAnnotations(net.opentsdb.grpc.BulkAnnotationRequest request) {
      return blockingUnaryCall(
          getChannel(), getBulkDeleteAnnotationsMethod(), getCallOptions(), request);
    }

    /**
     */
    public net.opentsdb.grpc.KeyValues getConfiguration(net.opentsdb.grpc.Empty request) {
      return blockingUnaryCall(
          getChannel(), getGetConfigurationMethod(), getCallOptions(), request);
    }

    /**
     */
    public net.opentsdb.grpc.FilterMetas getFilterMetas(net.opentsdb.grpc.Empty request) {
      return blockingUnaryCall(
          getChannel(), getGetFilterMetasMethod(), getCallOptions(), request);
    }

    /**
     */
    public net.opentsdb.grpc.Empty dropCaches(net.opentsdb.grpc.Empty request) {
      return blockingUnaryCall(
          getChannel(), getDropCachesMethod(), getCallOptions(), request);
    }

    /**
     */
    public net.opentsdb.grpc.PutDatapointsResponse put(net.opentsdb.grpc.PutDatapoints request) {
      return blockingUnaryCall(
          getChannel(), getPutMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class OpenTSDBServiceFutureStub extends io.grpc.stub.AbstractStub<OpenTSDBServiceFutureStub> {
    private OpenTSDBServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private OpenTSDBServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected OpenTSDBServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new OpenTSDBServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<net.opentsdb.grpc.Content> s(
        net.opentsdb.grpc.ContentName request) {
      return futureUnaryCall(
          getChannel().newCall(getSMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<net.opentsdb.grpc.AggregatorNames> getAggregators(
        net.opentsdb.grpc.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getGetAggregatorsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<net.opentsdb.grpc.TSDBAnnotation> getAnnotation(
        net.opentsdb.grpc.AnnotationRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getGetAnnotationMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<net.opentsdb.grpc.TSDBAnnotation> deleteAnnotations(
        net.opentsdb.grpc.TSDBAnnotation request) {
      return futureUnaryCall(
          getChannel().newCall(getDeleteAnnotationsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<net.opentsdb.grpc.BulkAnnotationResponse> bulkDeleteAnnotations(
        net.opentsdb.grpc.BulkAnnotationRequest request) {
      return futureUnaryCall(
          getChannel().newCall(getBulkDeleteAnnotationsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<net.opentsdb.grpc.KeyValues> getConfiguration(
        net.opentsdb.grpc.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getGetConfigurationMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<net.opentsdb.grpc.FilterMetas> getFilterMetas(
        net.opentsdb.grpc.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getGetFilterMetasMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<net.opentsdb.grpc.Empty> dropCaches(
        net.opentsdb.grpc.Empty request) {
      return futureUnaryCall(
          getChannel().newCall(getDropCachesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<net.opentsdb.grpc.PutDatapointsResponse> put(
        net.opentsdb.grpc.PutDatapoints request) {
      return futureUnaryCall(
          getChannel().newCall(getPutMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_S = 0;
  private static final int METHODID_GET_AGGREGATORS = 1;
  private static final int METHODID_GET_ANNOTATION = 2;
  private static final int METHODID_DELETE_ANNOTATIONS = 3;
  private static final int METHODID_BULK_DELETE_ANNOTATIONS = 4;
  private static final int METHODID_GET_CONFIGURATION = 5;
  private static final int METHODID_GET_FILTER_METAS = 6;
  private static final int METHODID_DROP_CACHES = 7;
  private static final int METHODID_PUT = 8;
  private static final int METHODID_CREATE_ANNOTATIONS = 9;
  private static final int METHODID_UPDATE_ANNOTATIONS = 10;
  private static final int METHODID_PUTS = 11;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final OpenTSDBServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(OpenTSDBServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_S:
          serviceImpl.s((net.opentsdb.grpc.ContentName) request,
              (io.grpc.stub.StreamObserver<net.opentsdb.grpc.Content>) responseObserver);
          break;
        case METHODID_GET_AGGREGATORS:
          serviceImpl.getAggregators((net.opentsdb.grpc.Empty) request,
              (io.grpc.stub.StreamObserver<net.opentsdb.grpc.AggregatorNames>) responseObserver);
          break;
        case METHODID_GET_ANNOTATION:
          serviceImpl.getAnnotation((net.opentsdb.grpc.AnnotationRequest) request,
              (io.grpc.stub.StreamObserver<net.opentsdb.grpc.TSDBAnnotation>) responseObserver);
          break;
        case METHODID_DELETE_ANNOTATIONS:
          serviceImpl.deleteAnnotations((net.opentsdb.grpc.TSDBAnnotation) request,
              (io.grpc.stub.StreamObserver<net.opentsdb.grpc.TSDBAnnotation>) responseObserver);
          break;
        case METHODID_BULK_DELETE_ANNOTATIONS:
          serviceImpl.bulkDeleteAnnotations((net.opentsdb.grpc.BulkAnnotationRequest) request,
              (io.grpc.stub.StreamObserver<net.opentsdb.grpc.BulkAnnotationResponse>) responseObserver);
          break;
        case METHODID_GET_CONFIGURATION:
          serviceImpl.getConfiguration((net.opentsdb.grpc.Empty) request,
              (io.grpc.stub.StreamObserver<net.opentsdb.grpc.KeyValues>) responseObserver);
          break;
        case METHODID_GET_FILTER_METAS:
          serviceImpl.getFilterMetas((net.opentsdb.grpc.Empty) request,
              (io.grpc.stub.StreamObserver<net.opentsdb.grpc.FilterMetas>) responseObserver);
          break;
        case METHODID_DROP_CACHES:
          serviceImpl.dropCaches((net.opentsdb.grpc.Empty) request,
              (io.grpc.stub.StreamObserver<net.opentsdb.grpc.Empty>) responseObserver);
          break;
        case METHODID_PUT:
          serviceImpl.put((net.opentsdb.grpc.PutDatapoints) request,
              (io.grpc.stub.StreamObserver<net.opentsdb.grpc.PutDatapointsResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CREATE_ANNOTATIONS:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.createAnnotations(
              (io.grpc.stub.StreamObserver<net.opentsdb.grpc.TSDBAnnotation>) responseObserver);
        case METHODID_UPDATE_ANNOTATIONS:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.updateAnnotations(
              (io.grpc.stub.StreamObserver<net.opentsdb.grpc.TSDBAnnotation>) responseObserver);
        case METHODID_PUTS:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.puts(
              (io.grpc.stub.StreamObserver<net.opentsdb.grpc.PutDatapointsResponse>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class OpenTSDBServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    OpenTSDBServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return net.opentsdb.grpc.Opentsdb.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("OpenTSDBService");
    }
  }

  private static final class OpenTSDBServiceFileDescriptorSupplier
      extends OpenTSDBServiceBaseDescriptorSupplier {
    OpenTSDBServiceFileDescriptorSupplier() {}
  }

  private static final class OpenTSDBServiceMethodDescriptorSupplier
      extends OpenTSDBServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    OpenTSDBServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (OpenTSDBServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new OpenTSDBServiceFileDescriptorSupplier())
              .addMethod(getSMethod())
              .addMethod(getGetAggregatorsMethod())
              .addMethod(getGetAnnotationMethod())
              .addMethod(getCreateAnnotationsMethod())
              .addMethod(getUpdateAnnotationsMethod())
              .addMethod(getDeleteAnnotationsMethod())
              .addMethod(getBulkDeleteAnnotationsMethod())
              .addMethod(getGetConfigurationMethod())
              .addMethod(getGetFilterMetasMethod())
              .addMethod(getDropCachesMethod())
              .addMethod(getPutMethod())
              .addMethod(getPutsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
