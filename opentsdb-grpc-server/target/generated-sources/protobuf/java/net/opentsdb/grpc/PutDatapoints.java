// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: opentsdb.proto

package net.opentsdb.grpc;

/**
 * Protobuf type {@code opentsdb.PutDatapoints}
 */
public  final class PutDatapoints extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:opentsdb.PutDatapoints)
    PutDatapointsOrBuilder {
private static final long serialVersionUID = 0L;
  // Use PutDatapoints.newBuilder() to construct.
  private PutDatapoints(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private PutDatapoints() {
    dataPoints_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private PutDatapoints(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    int mutable_bitField0_ = 0;
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          default: {
            if (!parseUnknownFieldProto3(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
          case 10: {
            net.opentsdb.grpc.PutOptions.Builder subBuilder = null;
            if (options_ != null) {
              subBuilder = options_.toBuilder();
            }
            options_ = input.readMessage(net.opentsdb.grpc.PutOptions.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(options_);
              options_ = subBuilder.buildPartial();
            }

            break;
          }
          case 18: {
            if (!((mutable_bitField0_ & 0x00000002) == 0x00000002)) {
              dataPoints_ = new java.util.ArrayList<net.opentsdb.grpc.DataPoint>();
              mutable_bitField0_ |= 0x00000002;
            }
            dataPoints_.add(
                input.readMessage(net.opentsdb.grpc.DataPoint.parser(), extensionRegistry));
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      if (((mutable_bitField0_ & 0x00000002) == 0x00000002)) {
        dataPoints_ = java.util.Collections.unmodifiableList(dataPoints_);
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_PutDatapoints_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_PutDatapoints_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            net.opentsdb.grpc.PutDatapoints.class, net.opentsdb.grpc.PutDatapoints.Builder.class);
  }

  private int bitField0_;
  public static final int OPTIONS_FIELD_NUMBER = 1;
  private net.opentsdb.grpc.PutOptions options_;
  /**
   * <code>.opentsdb.PutOptions options = 1;</code>
   */
  public boolean hasOptions() {
    return options_ != null;
  }
  /**
   * <code>.opentsdb.PutOptions options = 1;</code>
   */
  public net.opentsdb.grpc.PutOptions getOptions() {
    return options_ == null ? net.opentsdb.grpc.PutOptions.getDefaultInstance() : options_;
  }
  /**
   * <code>.opentsdb.PutOptions options = 1;</code>
   */
  public net.opentsdb.grpc.PutOptionsOrBuilder getOptionsOrBuilder() {
    return getOptions();
  }

  public static final int DATAPOINTS_FIELD_NUMBER = 2;
  private java.util.List<net.opentsdb.grpc.DataPoint> dataPoints_;
  /**
   * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
   */
  public java.util.List<net.opentsdb.grpc.DataPoint> getDataPointsList() {
    return dataPoints_;
  }
  /**
   * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
   */
  public java.util.List<? extends net.opentsdb.grpc.DataPointOrBuilder> 
      getDataPointsOrBuilderList() {
    return dataPoints_;
  }
  /**
   * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
   */
  public int getDataPointsCount() {
    return dataPoints_.size();
  }
  /**
   * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
   */
  public net.opentsdb.grpc.DataPoint getDataPoints(int index) {
    return dataPoints_.get(index);
  }
  /**
   * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
   */
  public net.opentsdb.grpc.DataPointOrBuilder getDataPointsOrBuilder(
      int index) {
    return dataPoints_.get(index);
  }

  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (options_ != null) {
      output.writeMessage(1, getOptions());
    }
    for (int i = 0; i < dataPoints_.size(); i++) {
      output.writeMessage(2, dataPoints_.get(i));
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (options_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getOptions());
    }
    for (int i = 0; i < dataPoints_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(2, dataPoints_.get(i));
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof net.opentsdb.grpc.PutDatapoints)) {
      return super.equals(obj);
    }
    net.opentsdb.grpc.PutDatapoints other = (net.opentsdb.grpc.PutDatapoints) obj;

    boolean result = true;
    result = result && (hasOptions() == other.hasOptions());
    if (hasOptions()) {
      result = result && getOptions()
          .equals(other.getOptions());
    }
    result = result && getDataPointsList()
        .equals(other.getDataPointsList());
    result = result && unknownFields.equals(other.unknownFields);
    return result;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    if (hasOptions()) {
      hash = (37 * hash) + OPTIONS_FIELD_NUMBER;
      hash = (53 * hash) + getOptions().hashCode();
    }
    if (getDataPointsCount() > 0) {
      hash = (37 * hash) + DATAPOINTS_FIELD_NUMBER;
      hash = (53 * hash) + getDataPointsList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static net.opentsdb.grpc.PutDatapoints parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static net.opentsdb.grpc.PutDatapoints parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static net.opentsdb.grpc.PutDatapoints parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static net.opentsdb.grpc.PutDatapoints parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static net.opentsdb.grpc.PutDatapoints parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static net.opentsdb.grpc.PutDatapoints parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static net.opentsdb.grpc.PutDatapoints parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static net.opentsdb.grpc.PutDatapoints parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static net.opentsdb.grpc.PutDatapoints parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static net.opentsdb.grpc.PutDatapoints parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static net.opentsdb.grpc.PutDatapoints parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static net.opentsdb.grpc.PutDatapoints parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(net.opentsdb.grpc.PutDatapoints prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code opentsdb.PutDatapoints}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:opentsdb.PutDatapoints)
      net.opentsdb.grpc.PutDatapointsOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_PutDatapoints_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_PutDatapoints_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              net.opentsdb.grpc.PutDatapoints.class, net.opentsdb.grpc.PutDatapoints.Builder.class);
    }

    // Construct using net.opentsdb.grpc.PutDatapoints.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
        getDataPointsFieldBuilder();
      }
    }
    public Builder clear() {
      super.clear();
      if (optionsBuilder_ == null) {
        options_ = null;
      } else {
        options_ = null;
        optionsBuilder_ = null;
      }
      if (dataPointsBuilder_ == null) {
        dataPoints_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000002);
      } else {
        dataPointsBuilder_.clear();
      }
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_PutDatapoints_descriptor;
    }

    public net.opentsdb.grpc.PutDatapoints getDefaultInstanceForType() {
      return net.opentsdb.grpc.PutDatapoints.getDefaultInstance();
    }

    public net.opentsdb.grpc.PutDatapoints build() {
      net.opentsdb.grpc.PutDatapoints result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public net.opentsdb.grpc.PutDatapoints buildPartial() {
      net.opentsdb.grpc.PutDatapoints result = new net.opentsdb.grpc.PutDatapoints(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (optionsBuilder_ == null) {
        result.options_ = options_;
      } else {
        result.options_ = optionsBuilder_.build();
      }
      if (dataPointsBuilder_ == null) {
        if (((bitField0_ & 0x00000002) == 0x00000002)) {
          dataPoints_ = java.util.Collections.unmodifiableList(dataPoints_);
          bitField0_ = (bitField0_ & ~0x00000002);
        }
        result.dataPoints_ = dataPoints_;
      } else {
        result.dataPoints_ = dataPointsBuilder_.build();
      }
      result.bitField0_ = to_bitField0_;
      onBuilt();
      return result;
    }

    public Builder clone() {
      return (Builder) super.clone();
    }
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return (Builder) super.setField(field, value);
    }
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return (Builder) super.clearField(field);
    }
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return (Builder) super.clearOneof(oneof);
    }
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return (Builder) super.setRepeatedField(field, index, value);
    }
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return (Builder) super.addRepeatedField(field, value);
    }
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof net.opentsdb.grpc.PutDatapoints) {
        return mergeFrom((net.opentsdb.grpc.PutDatapoints)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(net.opentsdb.grpc.PutDatapoints other) {
      if (other == net.opentsdb.grpc.PutDatapoints.getDefaultInstance()) return this;
      if (other.hasOptions()) {
        mergeOptions(other.getOptions());
      }
      if (dataPointsBuilder_ == null) {
        if (!other.dataPoints_.isEmpty()) {
          if (dataPoints_.isEmpty()) {
            dataPoints_ = other.dataPoints_;
            bitField0_ = (bitField0_ & ~0x00000002);
          } else {
            ensureDataPointsIsMutable();
            dataPoints_.addAll(other.dataPoints_);
          }
          onChanged();
        }
      } else {
        if (!other.dataPoints_.isEmpty()) {
          if (dataPointsBuilder_.isEmpty()) {
            dataPointsBuilder_.dispose();
            dataPointsBuilder_ = null;
            dataPoints_ = other.dataPoints_;
            bitField0_ = (bitField0_ & ~0x00000002);
            dataPointsBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getDataPointsFieldBuilder() : null;
          } else {
            dataPointsBuilder_.addAllMessages(other.dataPoints_);
          }
        }
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    public final boolean isInitialized() {
      return true;
    }

    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      net.opentsdb.grpc.PutDatapoints parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (net.opentsdb.grpc.PutDatapoints) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private net.opentsdb.grpc.PutOptions options_ = null;
    private com.google.protobuf.SingleFieldBuilderV3<
        net.opentsdb.grpc.PutOptions, net.opentsdb.grpc.PutOptions.Builder, net.opentsdb.grpc.PutOptionsOrBuilder> optionsBuilder_;
    /**
     * <code>.opentsdb.PutOptions options = 1;</code>
     */
    public boolean hasOptions() {
      return optionsBuilder_ != null || options_ != null;
    }
    /**
     * <code>.opentsdb.PutOptions options = 1;</code>
     */
    public net.opentsdb.grpc.PutOptions getOptions() {
      if (optionsBuilder_ == null) {
        return options_ == null ? net.opentsdb.grpc.PutOptions.getDefaultInstance() : options_;
      } else {
        return optionsBuilder_.getMessage();
      }
    }
    /**
     * <code>.opentsdb.PutOptions options = 1;</code>
     */
    public Builder setOptions(net.opentsdb.grpc.PutOptions value) {
      if (optionsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        options_ = value;
        onChanged();
      } else {
        optionsBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.opentsdb.PutOptions options = 1;</code>
     */
    public Builder setOptions(
        net.opentsdb.grpc.PutOptions.Builder builderForValue) {
      if (optionsBuilder_ == null) {
        options_ = builderForValue.build();
        onChanged();
      } else {
        optionsBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.opentsdb.PutOptions options = 1;</code>
     */
    public Builder mergeOptions(net.opentsdb.grpc.PutOptions value) {
      if (optionsBuilder_ == null) {
        if (options_ != null) {
          options_ =
            net.opentsdb.grpc.PutOptions.newBuilder(options_).mergeFrom(value).buildPartial();
        } else {
          options_ = value;
        }
        onChanged();
      } else {
        optionsBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.opentsdb.PutOptions options = 1;</code>
     */
    public Builder clearOptions() {
      if (optionsBuilder_ == null) {
        options_ = null;
        onChanged();
      } else {
        options_ = null;
        optionsBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.opentsdb.PutOptions options = 1;</code>
     */
    public net.opentsdb.grpc.PutOptions.Builder getOptionsBuilder() {
      
      onChanged();
      return getOptionsFieldBuilder().getBuilder();
    }
    /**
     * <code>.opentsdb.PutOptions options = 1;</code>
     */
    public net.opentsdb.grpc.PutOptionsOrBuilder getOptionsOrBuilder() {
      if (optionsBuilder_ != null) {
        return optionsBuilder_.getMessageOrBuilder();
      } else {
        return options_ == null ?
            net.opentsdb.grpc.PutOptions.getDefaultInstance() : options_;
      }
    }
    /**
     * <code>.opentsdb.PutOptions options = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        net.opentsdb.grpc.PutOptions, net.opentsdb.grpc.PutOptions.Builder, net.opentsdb.grpc.PutOptionsOrBuilder> 
        getOptionsFieldBuilder() {
      if (optionsBuilder_ == null) {
        optionsBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            net.opentsdb.grpc.PutOptions, net.opentsdb.grpc.PutOptions.Builder, net.opentsdb.grpc.PutOptionsOrBuilder>(
                getOptions(),
                getParentForChildren(),
                isClean());
        options_ = null;
      }
      return optionsBuilder_;
    }

    private java.util.List<net.opentsdb.grpc.DataPoint> dataPoints_ =
      java.util.Collections.emptyList();
    private void ensureDataPointsIsMutable() {
      if (!((bitField0_ & 0x00000002) == 0x00000002)) {
        dataPoints_ = new java.util.ArrayList<net.opentsdb.grpc.DataPoint>(dataPoints_);
        bitField0_ |= 0x00000002;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        net.opentsdb.grpc.DataPoint, net.opentsdb.grpc.DataPoint.Builder, net.opentsdb.grpc.DataPointOrBuilder> dataPointsBuilder_;

    /**
     * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
     */
    public java.util.List<net.opentsdb.grpc.DataPoint> getDataPointsList() {
      if (dataPointsBuilder_ == null) {
        return java.util.Collections.unmodifiableList(dataPoints_);
      } else {
        return dataPointsBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
     */
    public int getDataPointsCount() {
      if (dataPointsBuilder_ == null) {
        return dataPoints_.size();
      } else {
        return dataPointsBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
     */
    public net.opentsdb.grpc.DataPoint getDataPoints(int index) {
      if (dataPointsBuilder_ == null) {
        return dataPoints_.get(index);
      } else {
        return dataPointsBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
     */
    public Builder setDataPoints(
        int index, net.opentsdb.grpc.DataPoint value) {
      if (dataPointsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureDataPointsIsMutable();
        dataPoints_.set(index, value);
        onChanged();
      } else {
        dataPointsBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
     */
    public Builder setDataPoints(
        int index, net.opentsdb.grpc.DataPoint.Builder builderForValue) {
      if (dataPointsBuilder_ == null) {
        ensureDataPointsIsMutable();
        dataPoints_.set(index, builderForValue.build());
        onChanged();
      } else {
        dataPointsBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
     */
    public Builder addDataPoints(net.opentsdb.grpc.DataPoint value) {
      if (dataPointsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureDataPointsIsMutable();
        dataPoints_.add(value);
        onChanged();
      } else {
        dataPointsBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
     */
    public Builder addDataPoints(
        int index, net.opentsdb.grpc.DataPoint value) {
      if (dataPointsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureDataPointsIsMutable();
        dataPoints_.add(index, value);
        onChanged();
      } else {
        dataPointsBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
     */
    public Builder addDataPoints(
        net.opentsdb.grpc.DataPoint.Builder builderForValue) {
      if (dataPointsBuilder_ == null) {
        ensureDataPointsIsMutable();
        dataPoints_.add(builderForValue.build());
        onChanged();
      } else {
        dataPointsBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
     */
    public Builder addDataPoints(
        int index, net.opentsdb.grpc.DataPoint.Builder builderForValue) {
      if (dataPointsBuilder_ == null) {
        ensureDataPointsIsMutable();
        dataPoints_.add(index, builderForValue.build());
        onChanged();
      } else {
        dataPointsBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
     */
    public Builder addAllDataPoints(
        java.lang.Iterable<? extends net.opentsdb.grpc.DataPoint> values) {
      if (dataPointsBuilder_ == null) {
        ensureDataPointsIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, dataPoints_);
        onChanged();
      } else {
        dataPointsBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
     */
    public Builder clearDataPoints() {
      if (dataPointsBuilder_ == null) {
        dataPoints_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000002);
        onChanged();
      } else {
        dataPointsBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
     */
    public Builder removeDataPoints(int index) {
      if (dataPointsBuilder_ == null) {
        ensureDataPointsIsMutable();
        dataPoints_.remove(index);
        onChanged();
      } else {
        dataPointsBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
     */
    public net.opentsdb.grpc.DataPoint.Builder getDataPointsBuilder(
        int index) {
      return getDataPointsFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
     */
    public net.opentsdb.grpc.DataPointOrBuilder getDataPointsOrBuilder(
        int index) {
      if (dataPointsBuilder_ == null) {
        return dataPoints_.get(index);  } else {
        return dataPointsBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
     */
    public java.util.List<? extends net.opentsdb.grpc.DataPointOrBuilder> 
         getDataPointsOrBuilderList() {
      if (dataPointsBuilder_ != null) {
        return dataPointsBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(dataPoints_);
      }
    }
    /**
     * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
     */
    public net.opentsdb.grpc.DataPoint.Builder addDataPointsBuilder() {
      return getDataPointsFieldBuilder().addBuilder(
          net.opentsdb.grpc.DataPoint.getDefaultInstance());
    }
    /**
     * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
     */
    public net.opentsdb.grpc.DataPoint.Builder addDataPointsBuilder(
        int index) {
      return getDataPointsFieldBuilder().addBuilder(
          index, net.opentsdb.grpc.DataPoint.getDefaultInstance());
    }
    /**
     * <code>repeated .opentsdb.DataPoint dataPoints = 2;</code>
     */
    public java.util.List<net.opentsdb.grpc.DataPoint.Builder> 
         getDataPointsBuilderList() {
      return getDataPointsFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        net.opentsdb.grpc.DataPoint, net.opentsdb.grpc.DataPoint.Builder, net.opentsdb.grpc.DataPointOrBuilder> 
        getDataPointsFieldBuilder() {
      if (dataPointsBuilder_ == null) {
        dataPointsBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            net.opentsdb.grpc.DataPoint, net.opentsdb.grpc.DataPoint.Builder, net.opentsdb.grpc.DataPointOrBuilder>(
                dataPoints_,
                ((bitField0_ & 0x00000002) == 0x00000002),
                getParentForChildren(),
                isClean());
        dataPoints_ = null;
      }
      return dataPointsBuilder_;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:opentsdb.PutDatapoints)
  }

  // @@protoc_insertion_point(class_scope:opentsdb.PutDatapoints)
  private static final net.opentsdb.grpc.PutDatapoints DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new net.opentsdb.grpc.PutDatapoints();
  }

  public static net.opentsdb.grpc.PutDatapoints getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<PutDatapoints>
      PARSER = new com.google.protobuf.AbstractParser<PutDatapoints>() {
    public PutDatapoints parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new PutDatapoints(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<PutDatapoints> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<PutDatapoints> getParserForType() {
    return PARSER;
  }

  public net.opentsdb.grpc.PutDatapoints getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

