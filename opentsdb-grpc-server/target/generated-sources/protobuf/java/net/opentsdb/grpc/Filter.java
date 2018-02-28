// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: opentsdb.proto

package net.opentsdb.grpc;

/**
 * Protobuf type {@code opentsdb.Filter}
 */
public  final class Filter extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:opentsdb.Filter)
    FilterOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Filter.newBuilder() to construct.
  private Filter(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Filter() {
    filterType_ = 0;
    tagk_ = "";
    filterExpression_ = "";
    groupBy_ = false;
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private Filter(
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
          case 8: {
            int rawValue = input.readEnum();

            filterType_ = rawValue;
            break;
          }
          case 18: {
            java.lang.String s = input.readStringRequireUtf8();

            tagk_ = s;
            break;
          }
          case 26: {
            java.lang.String s = input.readStringRequireUtf8();

            filterExpression_ = s;
            break;
          }
          case 32: {

            groupBy_ = input.readBool();
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
    return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_Filter_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_Filter_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            net.opentsdb.grpc.Filter.class, net.opentsdb.grpc.Filter.Builder.class);
  }

  public static final int FILTERTYPE_FIELD_NUMBER = 1;
  private int filterType_;
  /**
   * <code>.opentsdb.FilterType filterType = 1;</code>
   */
  public int getFilterTypeValue() {
    return filterType_;
  }
  /**
   * <code>.opentsdb.FilterType filterType = 1;</code>
   */
  public net.opentsdb.grpc.FilterType getFilterType() {
    net.opentsdb.grpc.FilterType result = net.opentsdb.grpc.FilterType.valueOf(filterType_);
    return result == null ? net.opentsdb.grpc.FilterType.UNRECOGNIZED : result;
  }

  public static final int TAGK_FIELD_NUMBER = 2;
  private volatile java.lang.Object tagk_;
  /**
   * <code>string tagk = 2;</code>
   */
  public java.lang.String getTagk() {
    java.lang.Object ref = tagk_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      tagk_ = s;
      return s;
    }
  }
  /**
   * <code>string tagk = 2;</code>
   */
  public com.google.protobuf.ByteString
      getTagkBytes() {
    java.lang.Object ref = tagk_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      tagk_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int FILTEREXPRESSION_FIELD_NUMBER = 3;
  private volatile java.lang.Object filterExpression_;
  /**
   * <code>string filterExpression = 3;</code>
   */
  public java.lang.String getFilterExpression() {
    java.lang.Object ref = filterExpression_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      filterExpression_ = s;
      return s;
    }
  }
  /**
   * <code>string filterExpression = 3;</code>
   */
  public com.google.protobuf.ByteString
      getFilterExpressionBytes() {
    java.lang.Object ref = filterExpression_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      filterExpression_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int GROUPBY_FIELD_NUMBER = 4;
  private boolean groupBy_;
  /**
   * <code>bool groupBy = 4;</code>
   */
  public boolean getGroupBy() {
    return groupBy_;
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
    if (filterType_ != net.opentsdb.grpc.FilterType.iliteral_or.getNumber()) {
      output.writeEnum(1, filterType_);
    }
    if (!getTagkBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, tagk_);
    }
    if (!getFilterExpressionBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, filterExpression_);
    }
    if (groupBy_ != false) {
      output.writeBool(4, groupBy_);
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (filterType_ != net.opentsdb.grpc.FilterType.iliteral_or.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(1, filterType_);
    }
    if (!getTagkBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, tagk_);
    }
    if (!getFilterExpressionBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, filterExpression_);
    }
    if (groupBy_ != false) {
      size += com.google.protobuf.CodedOutputStream
        .computeBoolSize(4, groupBy_);
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
    if (!(obj instanceof net.opentsdb.grpc.Filter)) {
      return super.equals(obj);
    }
    net.opentsdb.grpc.Filter other = (net.opentsdb.grpc.Filter) obj;

    boolean result = true;
    result = result && filterType_ == other.filterType_;
    result = result && getTagk()
        .equals(other.getTagk());
    result = result && getFilterExpression()
        .equals(other.getFilterExpression());
    result = result && (getGroupBy()
        == other.getGroupBy());
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
    hash = (37 * hash) + FILTERTYPE_FIELD_NUMBER;
    hash = (53 * hash) + filterType_;
    hash = (37 * hash) + TAGK_FIELD_NUMBER;
    hash = (53 * hash) + getTagk().hashCode();
    hash = (37 * hash) + FILTEREXPRESSION_FIELD_NUMBER;
    hash = (53 * hash) + getFilterExpression().hashCode();
    hash = (37 * hash) + GROUPBY_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
        getGroupBy());
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static net.opentsdb.grpc.Filter parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static net.opentsdb.grpc.Filter parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static net.opentsdb.grpc.Filter parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static net.opentsdb.grpc.Filter parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static net.opentsdb.grpc.Filter parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static net.opentsdb.grpc.Filter parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static net.opentsdb.grpc.Filter parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static net.opentsdb.grpc.Filter parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static net.opentsdb.grpc.Filter parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static net.opentsdb.grpc.Filter parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static net.opentsdb.grpc.Filter parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static net.opentsdb.grpc.Filter parseFrom(
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
  public static Builder newBuilder(net.opentsdb.grpc.Filter prototype) {
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
   * Protobuf type {@code opentsdb.Filter}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:opentsdb.Filter)
      net.opentsdb.grpc.FilterOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_Filter_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_Filter_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              net.opentsdb.grpc.Filter.class, net.opentsdb.grpc.Filter.Builder.class);
    }

    // Construct using net.opentsdb.grpc.Filter.newBuilder()
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
      filterType_ = 0;

      tagk_ = "";

      filterExpression_ = "";

      groupBy_ = false;

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_Filter_descriptor;
    }

    public net.opentsdb.grpc.Filter getDefaultInstanceForType() {
      return net.opentsdb.grpc.Filter.getDefaultInstance();
    }

    public net.opentsdb.grpc.Filter build() {
      net.opentsdb.grpc.Filter result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public net.opentsdb.grpc.Filter buildPartial() {
      net.opentsdb.grpc.Filter result = new net.opentsdb.grpc.Filter(this);
      result.filterType_ = filterType_;
      result.tagk_ = tagk_;
      result.filterExpression_ = filterExpression_;
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
      if (other instanceof net.opentsdb.grpc.Filter) {
        return mergeFrom((net.opentsdb.grpc.Filter)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(net.opentsdb.grpc.Filter other) {
      if (other == net.opentsdb.grpc.Filter.getDefaultInstance()) return this;
      if (other.filterType_ != 0) {
        setFilterTypeValue(other.getFilterTypeValue());
      }
      if (!other.getTagk().isEmpty()) {
        tagk_ = other.tagk_;
        onChanged();
      }
      if (!other.getFilterExpression().isEmpty()) {
        filterExpression_ = other.filterExpression_;
        onChanged();
      }
      if (other.getGroupBy() != false) {
        setGroupBy(other.getGroupBy());
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
      net.opentsdb.grpc.Filter parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (net.opentsdb.grpc.Filter) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private int filterType_ = 0;
    /**
     * <code>.opentsdb.FilterType filterType = 1;</code>
     */
    public int getFilterTypeValue() {
      return filterType_;
    }
    /**
     * <code>.opentsdb.FilterType filterType = 1;</code>
     */
    public Builder setFilterTypeValue(int value) {
      filterType_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>.opentsdb.FilterType filterType = 1;</code>
     */
    public net.opentsdb.grpc.FilterType getFilterType() {
      net.opentsdb.grpc.FilterType result = net.opentsdb.grpc.FilterType.valueOf(filterType_);
      return result == null ? net.opentsdb.grpc.FilterType.UNRECOGNIZED : result;
    }
    /**
     * <code>.opentsdb.FilterType filterType = 1;</code>
     */
    public Builder setFilterType(net.opentsdb.grpc.FilterType value) {
      if (value == null) {
        throw new NullPointerException();
      }
      
      filterType_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <code>.opentsdb.FilterType filterType = 1;</code>
     */
    public Builder clearFilterType() {
      
      filterType_ = 0;
      onChanged();
      return this;
    }

    private java.lang.Object tagk_ = "";
    /**
     * <code>string tagk = 2;</code>
     */
    public java.lang.String getTagk() {
      java.lang.Object ref = tagk_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        tagk_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string tagk = 2;</code>
     */
    public com.google.protobuf.ByteString
        getTagkBytes() {
      java.lang.Object ref = tagk_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        tagk_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string tagk = 2;</code>
     */
    public Builder setTagk(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      tagk_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string tagk = 2;</code>
     */
    public Builder clearTagk() {
      
      tagk_ = getDefaultInstance().getTagk();
      onChanged();
      return this;
    }
    /**
     * <code>string tagk = 2;</code>
     */
    public Builder setTagkBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      tagk_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object filterExpression_ = "";
    /**
     * <code>string filterExpression = 3;</code>
     */
    public java.lang.String getFilterExpression() {
      java.lang.Object ref = filterExpression_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        filterExpression_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string filterExpression = 3;</code>
     */
    public com.google.protobuf.ByteString
        getFilterExpressionBytes() {
      java.lang.Object ref = filterExpression_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        filterExpression_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string filterExpression = 3;</code>
     */
    public Builder setFilterExpression(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      filterExpression_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string filterExpression = 3;</code>
     */
    public Builder clearFilterExpression() {
      
      filterExpression_ = getDefaultInstance().getFilterExpression();
      onChanged();
      return this;
    }
    /**
     * <code>string filterExpression = 3;</code>
     */
    public Builder setFilterExpressionBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      filterExpression_ = value;
      onChanged();
      return this;
    }

    private boolean groupBy_ ;
    /**
     * <code>bool groupBy = 4;</code>
     */
    public boolean getGroupBy() {
      return groupBy_;
    }
    /**
     * <code>bool groupBy = 4;</code>
     */
    public Builder setGroupBy(boolean value) {
      
      groupBy_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>bool groupBy = 4;</code>
     */
    public Builder clearGroupBy() {
      
      groupBy_ = false;
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


    // @@protoc_insertion_point(builder_scope:opentsdb.Filter)
  }

  // @@protoc_insertion_point(class_scope:opentsdb.Filter)
  private static final net.opentsdb.grpc.Filter DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new net.opentsdb.grpc.Filter();
  }

  public static net.opentsdb.grpc.Filter getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Filter>
      PARSER = new com.google.protobuf.AbstractParser<Filter>() {
    public Filter parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new Filter(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<Filter> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Filter> getParserForType() {
    return PARSER;
  }

  public net.opentsdb.grpc.Filter getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

