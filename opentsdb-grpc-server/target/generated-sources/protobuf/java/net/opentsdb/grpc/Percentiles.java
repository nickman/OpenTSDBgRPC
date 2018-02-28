// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: opentsdb.proto

package net.opentsdb.grpc;

/**
 * Protobuf type {@code opentsdb.Percentiles}
 */
public  final class Percentiles extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:opentsdb.Percentiles)
    PercentilesOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Percentiles.newBuilder() to construct.
  private Percentiles(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Percentiles() {
    percentile_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private Percentiles(
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
          case 9: {
            if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
              percentile_ = new java.util.ArrayList<java.lang.Double>();
              mutable_bitField0_ |= 0x00000001;
            }
            percentile_.add(input.readDouble());
            break;
          }
          case 10: {
            int length = input.readRawVarint32();
            int limit = input.pushLimit(length);
            if (!((mutable_bitField0_ & 0x00000001) == 0x00000001) && input.getBytesUntilLimit() > 0) {
              percentile_ = new java.util.ArrayList<java.lang.Double>();
              mutable_bitField0_ |= 0x00000001;
            }
            while (input.getBytesUntilLimit() > 0) {
              percentile_.add(input.readDouble());
            }
            input.popLimit(limit);
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
      if (((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
        percentile_ = java.util.Collections.unmodifiableList(percentile_);
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_Percentiles_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_Percentiles_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            net.opentsdb.grpc.Percentiles.class, net.opentsdb.grpc.Percentiles.Builder.class);
  }

  public static final int PERCENTILE_FIELD_NUMBER = 1;
  private java.util.List<java.lang.Double> percentile_;
  /**
   * <code>repeated double percentile = 1;</code>
   */
  public java.util.List<java.lang.Double>
      getPercentileList() {
    return percentile_;
  }
  /**
   * <code>repeated double percentile = 1;</code>
   */
  public int getPercentileCount() {
    return percentile_.size();
  }
  /**
   * <code>repeated double percentile = 1;</code>
   */
  public double getPercentile(int index) {
    return percentile_.get(index);
  }
  private int percentileMemoizedSerializedSize = -1;

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
    getSerializedSize();
    if (getPercentileList().size() > 0) {
      output.writeUInt32NoTag(10);
      output.writeUInt32NoTag(percentileMemoizedSerializedSize);
    }
    for (int i = 0; i < percentile_.size(); i++) {
      output.writeDoubleNoTag(percentile_.get(i));
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    {
      int dataSize = 0;
      dataSize = 8 * getPercentileList().size();
      size += dataSize;
      if (!getPercentileList().isEmpty()) {
        size += 1;
        size += com.google.protobuf.CodedOutputStream
            .computeInt32SizeNoTag(dataSize);
      }
      percentileMemoizedSerializedSize = dataSize;
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
    if (!(obj instanceof net.opentsdb.grpc.Percentiles)) {
      return super.equals(obj);
    }
    net.opentsdb.grpc.Percentiles other = (net.opentsdb.grpc.Percentiles) obj;

    boolean result = true;
    result = result && getPercentileList()
        .equals(other.getPercentileList());
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
    if (getPercentileCount() > 0) {
      hash = (37 * hash) + PERCENTILE_FIELD_NUMBER;
      hash = (53 * hash) + getPercentileList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static net.opentsdb.grpc.Percentiles parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static net.opentsdb.grpc.Percentiles parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static net.opentsdb.grpc.Percentiles parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static net.opentsdb.grpc.Percentiles parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static net.opentsdb.grpc.Percentiles parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static net.opentsdb.grpc.Percentiles parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static net.opentsdb.grpc.Percentiles parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static net.opentsdb.grpc.Percentiles parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static net.opentsdb.grpc.Percentiles parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static net.opentsdb.grpc.Percentiles parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static net.opentsdb.grpc.Percentiles parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static net.opentsdb.grpc.Percentiles parseFrom(
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
  public static Builder newBuilder(net.opentsdb.grpc.Percentiles prototype) {
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
   * Protobuf type {@code opentsdb.Percentiles}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:opentsdb.Percentiles)
      net.opentsdb.grpc.PercentilesOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_Percentiles_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_Percentiles_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              net.opentsdb.grpc.Percentiles.class, net.opentsdb.grpc.Percentiles.Builder.class);
    }

    // Construct using net.opentsdb.grpc.Percentiles.newBuilder()
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
      percentile_ = java.util.Collections.emptyList();
      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_Percentiles_descriptor;
    }

    public net.opentsdb.grpc.Percentiles getDefaultInstanceForType() {
      return net.opentsdb.grpc.Percentiles.getDefaultInstance();
    }

    public net.opentsdb.grpc.Percentiles build() {
      net.opentsdb.grpc.Percentiles result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public net.opentsdb.grpc.Percentiles buildPartial() {
      net.opentsdb.grpc.Percentiles result = new net.opentsdb.grpc.Percentiles(this);
      int from_bitField0_ = bitField0_;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        percentile_ = java.util.Collections.unmodifiableList(percentile_);
        bitField0_ = (bitField0_ & ~0x00000001);
      }
      result.percentile_ = percentile_;
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
      if (other instanceof net.opentsdb.grpc.Percentiles) {
        return mergeFrom((net.opentsdb.grpc.Percentiles)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(net.opentsdb.grpc.Percentiles other) {
      if (other == net.opentsdb.grpc.Percentiles.getDefaultInstance()) return this;
      if (!other.percentile_.isEmpty()) {
        if (percentile_.isEmpty()) {
          percentile_ = other.percentile_;
          bitField0_ = (bitField0_ & ~0x00000001);
        } else {
          ensurePercentileIsMutable();
          percentile_.addAll(other.percentile_);
        }
        onChanged();
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
      net.opentsdb.grpc.Percentiles parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (net.opentsdb.grpc.Percentiles) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private java.util.List<java.lang.Double> percentile_ = java.util.Collections.emptyList();
    private void ensurePercentileIsMutable() {
      if (!((bitField0_ & 0x00000001) == 0x00000001)) {
        percentile_ = new java.util.ArrayList<java.lang.Double>(percentile_);
        bitField0_ |= 0x00000001;
       }
    }
    /**
     * <code>repeated double percentile = 1;</code>
     */
    public java.util.List<java.lang.Double>
        getPercentileList() {
      return java.util.Collections.unmodifiableList(percentile_);
    }
    /**
     * <code>repeated double percentile = 1;</code>
     */
    public int getPercentileCount() {
      return percentile_.size();
    }
    /**
     * <code>repeated double percentile = 1;</code>
     */
    public double getPercentile(int index) {
      return percentile_.get(index);
    }
    /**
     * <code>repeated double percentile = 1;</code>
     */
    public Builder setPercentile(
        int index, double value) {
      ensurePercentileIsMutable();
      percentile_.set(index, value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated double percentile = 1;</code>
     */
    public Builder addPercentile(double value) {
      ensurePercentileIsMutable();
      percentile_.add(value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated double percentile = 1;</code>
     */
    public Builder addAllPercentile(
        java.lang.Iterable<? extends java.lang.Double> values) {
      ensurePercentileIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(
          values, percentile_);
      onChanged();
      return this;
    }
    /**
     * <code>repeated double percentile = 1;</code>
     */
    public Builder clearPercentile() {
      percentile_ = java.util.Collections.emptyList();
      bitField0_ = (bitField0_ & ~0x00000001);
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


    // @@protoc_insertion_point(builder_scope:opentsdb.Percentiles)
  }

  // @@protoc_insertion_point(class_scope:opentsdb.Percentiles)
  private static final net.opentsdb.grpc.Percentiles DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new net.opentsdb.grpc.Percentiles();
  }

  public static net.opentsdb.grpc.Percentiles getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Percentiles>
      PARSER = new com.google.protobuf.AbstractParser<Percentiles>() {
    public Percentiles parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new Percentiles(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<Percentiles> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Percentiles> getParserForType() {
    return PARSER;
  }

  public net.opentsdb.grpc.Percentiles getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

