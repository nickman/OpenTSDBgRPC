// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: opentsdb.proto

package net.opentsdb.grpc;

public interface DownsampleOrBuilder extends
    // @@protoc_insertion_point(interface_extends:opentsdb.Downsample)
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
   * <code>.opentsdb.Aggregator aggregator = 2;</code>
   */
  int getAggregatorValue();
  /**
   * <code>.opentsdb.Aggregator aggregator = 2;</code>
   */
  net.opentsdb.grpc.Aggregator getAggregator();

  /**
   * <code>.opentsdb.FillPolicy fillPolicy = 3;</code>
   */
  int getFillPolicyValue();
  /**
   * <code>.opentsdb.FillPolicy fillPolicy = 3;</code>
   */
  net.opentsdb.grpc.FillPolicy getFillPolicy();
}
