// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: opentsdb.proto

package net.opentsdb.grpc;

public interface DateTimeOrBuilder extends
    // @@protoc_insertion_point(interface_extends:opentsdb.DateTime)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.opentsdb.RelativeTime relTime = 1;</code>
   */
  boolean hasRelTime();
  /**
   * <code>.opentsdb.RelativeTime relTime = 1;</code>
   */
  net.opentsdb.grpc.RelativeTime getRelTime();
  /**
   * <code>.opentsdb.RelativeTime relTime = 1;</code>
   */
  net.opentsdb.grpc.RelativeTimeOrBuilder getRelTimeOrBuilder();

  /**
   * <code>int64 unitTime = 2;</code>
   */
  long getUnitTime();

  /**
   * <code>string absTime = 3;</code>
   */
  java.lang.String getAbsTime();
  /**
   * <code>string absTime = 3;</code>
   */
  com.google.protobuf.ByteString
      getAbsTimeBytes();

  public net.opentsdb.grpc.DateTime.TimeRangeCase getTimeRangeCase();
}
