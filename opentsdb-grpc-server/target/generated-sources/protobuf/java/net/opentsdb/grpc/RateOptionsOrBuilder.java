// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: opentsdb.proto

package net.opentsdb.grpc;

public interface RateOptionsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:opentsdb.RateOptions)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>bool rate = 1;</code>
   */
  boolean getRate();

  /**
   * <code>bool counter = 2;</code>
   */
  boolean getCounter();

  /**
   * <code>int32 counterMax = 3;</code>
   */
  int getCounterMax();

  /**
   * <code>int32 resetValue = 4;</code>
   */
  int getResetValue();

  /**
   * <code>bool dropResets = 5;</code>
   */
  boolean getDropResets();
}
