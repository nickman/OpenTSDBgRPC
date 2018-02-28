// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: opentsdb.proto

package net.opentsdb.grpc;

/**
 * Protobuf type {@code opentsdb.Aggregation}
 */
public  final class Aggregation extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:opentsdb.Aggregation)
    AggregationOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Aggregation.newBuilder() to construct.
  private Aggregation(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Aggregation() {
    aggregator_ = 0;
    groupBy_ = 0;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private Aggregation(
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
            net.opentsdb.grpc.RelativeTime.Builder subBuilder = null;
            if (interval_ != null) {
              subBuilder = interval_.toBuilder();
            }
            interval_ = input.readMessage(net.opentsdb.grpc.RelativeTime.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(interval_);
              interval_ = subBuilder.buildPartial();
            }

            break;
          }
          case 16: {
            int rawValue = input.readEnum();

            aggregator_ = rawValue;
            break;
          }
          case 24: {
            int rawValue = input.readEnum();

            groupBy_ = rawValue;
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
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_Aggregation_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_Aggregation_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            net.opentsdb.grpc.Aggregation.class, net.opentsdb.grpc.Aggregation.Builder.class);
  }

  public static final int INTERVAL_FIELD_NUMBER = 1;
  private net.opentsdb.grpc.RelativeTime interval_;
  /**
   * <code>.opentsdb.RelativeTime interval = 1;</code>
   */
  public boolean hasInterval() {
    return interval_ != null;
  }
  /**
   * <code>.opentsdb.RelativeTime interval = 1;</code>
   */
  public net.opentsdb.grpc.RelativeTime getInterval() {
    return interval_ == null ? net.opentsdb.grpc.RelativeTime.getDefaultInstance() : interval_;
  }
  /**
   * <code>.opentsdb.RelativeTime interval = 1;</code>
   */
  public net.opentsdb.grpc.RelativeTimeOrBuilder getIntervalOrBuilder() {
    return getInterval();
  }

  public static final int AGGREGATOR_FIELD_NUMBER = 2;
  private int aggregator_;
  /**
   * <code>.opentsdb.Aggregator aggregator = 2;</code>
   */
  public int getAggregatorValue() {
    return aggregator_;
  }
  /**
   * <code>.opentsdb.Aggregator aggregator = 2;</code>
   */
  public net.opentsdb.grpc.Aggregator getAggregator() {
    net.opentsdb.grpc.Aggregator result = net.opentsdb.grpc.Aggregator.valueOf(aggregator_);
    return result == null ? net.opentsdb.grpc.Aggregator.UNRECOGNIZED : result;
  }

  public static final int GROUPBY_FIELD_NUMBER = 3;
  private int groupBy_;
  /**
   * <code>.opentsdb.Aggregator groupBy = 3;</code>
   */
  public int getGroupByValue() {
    return groupBy_;
  }
  /**
   * <code>.opentsdb.Aggregator groupBy = 3;</code>
   */
  public net.opentsdb.grpc.Aggregator getGroupBy() {
    net.opentsdb.grpc.Aggregator result = net.opentsdb.grpc.Aggregator.valueOf(groupBy_);
    return result == null ? net.opentsdb.grpc.Aggregator.UNRECOGNIZED : result;
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
    if (interval_ != null) {
      output.writeMessage(1, getInterval());
    }
    if (aggregator_ != net.opentsdb.grpc.Aggregator.SUM.getNumber()) {
      output.writeEnum(2, aggregator_);
    }
    if (groupBy_ != net.opentsdb.grpc.Aggregator.SUM.getNumber()) {
      output.writeEnum(3, groupBy_);
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (interval_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, getInterval());
    }
    if (aggregator_ != net.opentsdb.grpc.Aggregator.SUM.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(2, aggregator_);
    }
    if (groupBy_ != net.opentsdb.grpc.Aggregator.SUM.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(3, groupBy_);
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
    if (!(obj instanceof net.opentsdb.grpc.Aggregation)) {
      return super.equals(obj);
    }
    net.opentsdb.grpc.Aggregation other = (net.opentsdb.grpc.Aggregation) obj;

    boolean result = true;
    result = result && (hasInterval() == other.hasInterval());
    if (hasInterval()) {
      result = result && getInterval()
          .equals(other.getInterval());
    }
    result = result && aggregator_ == other.aggregator_;
    result = result && groupBy_ == other.groupBy_;
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
    if (hasInterval()) {
      hash = (37 * hash) + INTERVAL_FIELD_NUMBER;
      hash = (53 * hash) + getInterval().hashCode();
    }
    hash = (37 * hash) + AGGREGATOR_FIELD_NUMBER;
    hash = (53 * hash) + aggregator_;
    hash = (37 * hash) + GROUPBY_FIELD_NUMBER;
    hash = (53 * hash) + groupBy_;
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static net.opentsdb.grpc.Aggregation parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static net.opentsdb.grpc.Aggregation parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static net.opentsdb.grpc.Aggregation parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static net.opentsdb.grpc.Aggregation parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static net.opentsdb.grpc.Aggregation parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static net.opentsdb.grpc.Aggregation parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static net.opentsdb.grpc.Aggregation parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static net.opentsdb.grpc.Aggregation parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static net.opentsdb.grpc.Aggregation parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static net.opentsdb.grpc.Aggregation parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static net.opentsdb.grpc.Aggregation parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static net.opentsdb.grpc.Aggregation parseFrom(
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
  public static Builder newBuilder(net.opentsdb.grpc.Aggregation prototype) {
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
   * Protobuf type {@code opentsdb.Aggregation}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:opentsdb.Aggregation)
      net.opentsdb.grpc.AggregationOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_Aggregation_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_Aggregation_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              net.opentsdb.grpc.Aggregation.class, net.opentsdb.grpc.Aggregation.Builder.class);
    }

    // Construct using net.opentsdb.grpc.Aggregation.newBuilder()
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
      }
    }
    public Builder clear() {
      super.clear();
      if (intervalBuilder_ == null) {
        interval_ = null;
      } else {
        interval_ = null;
        intervalBuilder_ = null;
      }
      aggregator_ = 0;

      groupBy_ = 0;

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_Aggregation_descriptor;
    }

    public net.opentsdb.grpc.Aggregation getDefaultInstanceForType() {
      return net.opentsdb.grpc.Aggregation.getDefaultInstance();
    }

    public net.opentsdb.grpc.Aggregation build() {
      net.opentsdb.grpc.Aggregation result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public net.opentsdb.grpc.Aggregation buildPartial() {
      net.opentsdb.grpc.Aggregation result = new net.opentsdb.grpc.Aggregation(this);
      if (intervalBuilder_ == null) {
        result.interval_ = interval_;
      } else {
        result.interval_ = intervalBuilder_.build();
      }
      result.aggregator_ = aggregator_;
      result.groupBy_ = groupBy_;
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
      if (other instanceof net.opentsdb.grpc.Aggregation) {
        return mergeFrom((net.opentsdb.grpc.Aggregation)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(net.opentsdb.grpc.Aggregation other) {
      if (other == net.opentsdb.grpc.Aggregation.getDefaultInstance()) return this;
      if (other.hasInterval()) {
        mergeInterval(other.getInterval());
      }
      if (other.aggregator_ != 0) {
        setAggregatorValue(other.getAggregatorValue());
      }
      if (other.groupBy_ != 0) {
        setGroupByValue(other.getGroupByValue());
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
      net.opentsdb.grpc.Aggregation parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (net.opentsdb.grpc.Aggregation) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private net.opentsdb.grpc.RelativeTime interval_ = null;
    private com.google.protobuf.SingleFieldBuilderV3<
        net.opentsdb.grpc.RelativeTime, net.opentsdb.grpc.RelativeTime.Builder, net.opentsdb.grpc.RelativeTimeOrBuilder> intervalBuilder_;
    /**
     * <code>.opentsdb.RelativeTime interval = 1;</code>
     */
    public boolean hasInterval() {
      return intervalBuilder_ != null || interval_ != null;
    }
    /**
     * <code>.opentsdb.RelativeTime interval = 1;</code>
     */
    public net.opentsdb.grpc.RelativeTime getInterval() {
      if (intervalBuilder_ == null) {
        return interval_ == null ? net.opentsdb.grpc.RelativeTime.getDefaultInstance() : interval_;
      } else {
        return intervalBuilder_.getMessage();
      }
    }
    /**
     * <code>.opentsdb.RelativeTime interval = 1;</code>
     */
    public Builder setInterval(net.opentsdb.grpc.RelativeTime value) {
      if (intervalBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        interval_ = value;
        onChanged();
      } else {
        intervalBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.opentsdb.RelativeTime interval = 1;</code>
     */
    public Builder setInterval(
        net.opentsdb.grpc.RelativeTime.Builder builderForValue) {
      if (intervalBuilder_ == null) {
        interval_ = builderForValue.build();
        onChanged();
      } else {
        intervalBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.opentsdb.RelativeTime interval = 1;</code>
     */
    public Builder mergeInterval(net.opentsdb.grpc.RelativeTime value) {
      if (intervalBuilder_ == null) {
        if (interval_ != null) {
          interval_ =
            net.opentsdb.grpc.RelativeTime.newBuilder(interval_).mergeFrom(value).buildPartial();
        } else {
          interval_ = value;
        }
        onChanged();
      } else {
        intervalBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.opentsdb.RelativeTime interval = 1;</code>
     */
    public Builder clearInterval() {
      if (intervalBuilder_ == null) {
        interval_ = null;
        onChanged();
      } else {
        interval_ = null;
        intervalBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.opentsdb.RelativeTime interval = 1;</code>
     */
    public net.opentsdb.grpc.RelativeTime.Builder getIntervalBuilder() {
      
      onChanged();
      return getIntervalFieldBuilder().getBuilder();
    }
    /**
     * <code>.opentsdb.RelativeTime interval = 1;</code>
     */
    public net.opentsdb.grpc.RelativeTimeOrBuilder getIntervalOrBuilder() {
      if (intervalBuilder_ != null) {
        return intervalBuilder_.getMessageOrBuilder();
      } else {
        return interval_ == null ?
            net.opentsdb.grpc.RelativeTime.getDefaultInstance() : interval_;
      }
    }
    /**
     * <code>.opentsdb.RelativeTime interval = 1;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        net.opentsdb.grpc.RelativeTime, net.opentsdb.grpc.RelativeTime.Builder, net.opentsdb.grpc.RelativeTimeOrBuilder> 
        getIntervalFieldBuilder() {
      if (intervalBuilder_ == null) {
        intervalBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            net.opentsdb.grpc.RelativeTime, net.opentsdb.grpc.RelativeTime.Builder, net.opentsdb.grpc.RelativeTimeOrBuilder>(
                getInterval(),
                getParentForChildren(),
                isClean());
        interval_ = null;
      }
      return intervalBuilder_;
    }

    private int aggregator_ = 0;
    /**
     * <code>.opentsdb.Aggregator aggregator = 2;</code>
     */
    public int getAggregatorValue() {
      return aggregator_;
    }
    /**
     * <code>.opentsdb.Aggregator aggregator = 2;</code>
     */
    public Builder setAggregatorValue(int value) {
      aggregator_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>.opentsdb.Aggregator aggregator = 2;</code>
     */
    public net.opentsdb.grpc.Aggregator getAggregator() {
      net.opentsdb.grpc.Aggregator result = net.opentsdb.grpc.Aggregator.valueOf(aggregator_);
      return result == null ? net.opentsdb.grpc.Aggregator.UNRECOGNIZED : result;
    }
    /**
     * <code>.opentsdb.Aggregator aggregator = 2;</code>
     */
    public Builder setAggregator(net.opentsdb.grpc.Aggregator value) {
      if (value == null) {
        throw new NullPointerException();
      }
      
      aggregator_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <code>.opentsdb.Aggregator aggregator = 2;</code>
     */
    public Builder clearAggregator() {
      
      aggregator_ = 0;
      onChanged();
      return this;
    }

    private int groupBy_ = 0;
    /**
     * <code>.opentsdb.Aggregator groupBy = 3;</code>
     */
    public int getGroupByValue() {
      return groupBy_;
    }
    /**
     * <code>.opentsdb.Aggregator groupBy = 3;</code>
     */
    public Builder setGroupByValue(int value) {
      groupBy_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>.opentsdb.Aggregator groupBy = 3;</code>
     */
    public net.opentsdb.grpc.Aggregator getGroupBy() {
      net.opentsdb.grpc.Aggregator result = net.opentsdb.grpc.Aggregator.valueOf(groupBy_);
      return result == null ? net.opentsdb.grpc.Aggregator.UNRECOGNIZED : result;
    }
    /**
     * <code>.opentsdb.Aggregator groupBy = 3;</code>
     */
    public Builder setGroupBy(net.opentsdb.grpc.Aggregator value) {
      if (value == null) {
        throw new NullPointerException();
      }
      
      groupBy_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <code>.opentsdb.Aggregator groupBy = 3;</code>
     */
    public Builder clearGroupBy() {
      
      groupBy_ = 0;
      onChanged();
      return this;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:opentsdb.Aggregation)
  }

  // @@protoc_insertion_point(class_scope:opentsdb.Aggregation)
  private static final net.opentsdb.grpc.Aggregation DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new net.opentsdb.grpc.Aggregation();
  }

  public static net.opentsdb.grpc.Aggregation getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Aggregation>
      PARSER = new com.google.protobuf.AbstractParser<Aggregation>() {
    public Aggregation parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new Aggregation(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<Aggregation> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Aggregation> getParserForType() {
    return PARSER;
  }

  public net.opentsdb.grpc.Aggregation getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

