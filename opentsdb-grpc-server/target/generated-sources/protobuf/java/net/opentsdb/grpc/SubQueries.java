// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: opentsdb.proto

package net.opentsdb.grpc;

/**
 * Protobuf type {@code opentsdb.SubQueries}
 */
public  final class SubQueries extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:opentsdb.SubQueries)
    SubQueriesOrBuilder {
private static final long serialVersionUID = 0L;
  // Use SubQueries.newBuilder() to construct.
  private SubQueries(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private SubQueries() {
    queries_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private SubQueries(
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
            if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
              queries_ = new java.util.ArrayList<net.opentsdb.grpc.SubQuery>();
              mutable_bitField0_ |= 0x00000001;
            }
            queries_.add(
                input.readMessage(net.opentsdb.grpc.SubQuery.parser(), extensionRegistry));
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
        queries_ = java.util.Collections.unmodifiableList(queries_);
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_SubQueries_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_SubQueries_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            net.opentsdb.grpc.SubQueries.class, net.opentsdb.grpc.SubQueries.Builder.class);
  }

  public static final int QUERIES_FIELD_NUMBER = 1;
  private java.util.List<net.opentsdb.grpc.SubQuery> queries_;
  /**
   * <code>repeated .opentsdb.SubQuery queries = 1;</code>
   */
  public java.util.List<net.opentsdb.grpc.SubQuery> getQueriesList() {
    return queries_;
  }
  /**
   * <code>repeated .opentsdb.SubQuery queries = 1;</code>
   */
  public java.util.List<? extends net.opentsdb.grpc.SubQueryOrBuilder> 
      getQueriesOrBuilderList() {
    return queries_;
  }
  /**
   * <code>repeated .opentsdb.SubQuery queries = 1;</code>
   */
  public int getQueriesCount() {
    return queries_.size();
  }
  /**
   * <code>repeated .opentsdb.SubQuery queries = 1;</code>
   */
  public net.opentsdb.grpc.SubQuery getQueries(int index) {
    return queries_.get(index);
  }
  /**
   * <code>repeated .opentsdb.SubQuery queries = 1;</code>
   */
  public net.opentsdb.grpc.SubQueryOrBuilder getQueriesOrBuilder(
      int index) {
    return queries_.get(index);
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
    for (int i = 0; i < queries_.size(); i++) {
      output.writeMessage(1, queries_.get(i));
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < queries_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, queries_.get(i));
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
    if (!(obj instanceof net.opentsdb.grpc.SubQueries)) {
      return super.equals(obj);
    }
    net.opentsdb.grpc.SubQueries other = (net.opentsdb.grpc.SubQueries) obj;

    boolean result = true;
    result = result && getQueriesList()
        .equals(other.getQueriesList());
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
    if (getQueriesCount() > 0) {
      hash = (37 * hash) + QUERIES_FIELD_NUMBER;
      hash = (53 * hash) + getQueriesList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static net.opentsdb.grpc.SubQueries parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static net.opentsdb.grpc.SubQueries parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static net.opentsdb.grpc.SubQueries parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static net.opentsdb.grpc.SubQueries parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static net.opentsdb.grpc.SubQueries parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static net.opentsdb.grpc.SubQueries parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static net.opentsdb.grpc.SubQueries parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static net.opentsdb.grpc.SubQueries parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static net.opentsdb.grpc.SubQueries parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static net.opentsdb.grpc.SubQueries parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static net.opentsdb.grpc.SubQueries parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static net.opentsdb.grpc.SubQueries parseFrom(
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
  public static Builder newBuilder(net.opentsdb.grpc.SubQueries prototype) {
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
   * Protobuf type {@code opentsdb.SubQueries}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:opentsdb.SubQueries)
      net.opentsdb.grpc.SubQueriesOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_SubQueries_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_SubQueries_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              net.opentsdb.grpc.SubQueries.class, net.opentsdb.grpc.SubQueries.Builder.class);
    }

    // Construct using net.opentsdb.grpc.SubQueries.newBuilder()
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
        getQueriesFieldBuilder();
      }
    }
    public Builder clear() {
      super.clear();
      if (queriesBuilder_ == null) {
        queries_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
      } else {
        queriesBuilder_.clear();
      }
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_SubQueries_descriptor;
    }

    public net.opentsdb.grpc.SubQueries getDefaultInstanceForType() {
      return net.opentsdb.grpc.SubQueries.getDefaultInstance();
    }

    public net.opentsdb.grpc.SubQueries build() {
      net.opentsdb.grpc.SubQueries result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public net.opentsdb.grpc.SubQueries buildPartial() {
      net.opentsdb.grpc.SubQueries result = new net.opentsdb.grpc.SubQueries(this);
      int from_bitField0_ = bitField0_;
      if (queriesBuilder_ == null) {
        if (((bitField0_ & 0x00000001) == 0x00000001)) {
          queries_ = java.util.Collections.unmodifiableList(queries_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.queries_ = queries_;
      } else {
        result.queries_ = queriesBuilder_.build();
      }
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
      if (other instanceof net.opentsdb.grpc.SubQueries) {
        return mergeFrom((net.opentsdb.grpc.SubQueries)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(net.opentsdb.grpc.SubQueries other) {
      if (other == net.opentsdb.grpc.SubQueries.getDefaultInstance()) return this;
      if (queriesBuilder_ == null) {
        if (!other.queries_.isEmpty()) {
          if (queries_.isEmpty()) {
            queries_ = other.queries_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureQueriesIsMutable();
            queries_.addAll(other.queries_);
          }
          onChanged();
        }
      } else {
        if (!other.queries_.isEmpty()) {
          if (queriesBuilder_.isEmpty()) {
            queriesBuilder_.dispose();
            queriesBuilder_ = null;
            queries_ = other.queries_;
            bitField0_ = (bitField0_ & ~0x00000001);
            queriesBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getQueriesFieldBuilder() : null;
          } else {
            queriesBuilder_.addAllMessages(other.queries_);
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
      net.opentsdb.grpc.SubQueries parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (net.opentsdb.grpc.SubQueries) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private java.util.List<net.opentsdb.grpc.SubQuery> queries_ =
      java.util.Collections.emptyList();
    private void ensureQueriesIsMutable() {
      if (!((bitField0_ & 0x00000001) == 0x00000001)) {
        queries_ = new java.util.ArrayList<net.opentsdb.grpc.SubQuery>(queries_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        net.opentsdb.grpc.SubQuery, net.opentsdb.grpc.SubQuery.Builder, net.opentsdb.grpc.SubQueryOrBuilder> queriesBuilder_;

    /**
     * <code>repeated .opentsdb.SubQuery queries = 1;</code>
     */
    public java.util.List<net.opentsdb.grpc.SubQuery> getQueriesList() {
      if (queriesBuilder_ == null) {
        return java.util.Collections.unmodifiableList(queries_);
      } else {
        return queriesBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .opentsdb.SubQuery queries = 1;</code>
     */
    public int getQueriesCount() {
      if (queriesBuilder_ == null) {
        return queries_.size();
      } else {
        return queriesBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .opentsdb.SubQuery queries = 1;</code>
     */
    public net.opentsdb.grpc.SubQuery getQueries(int index) {
      if (queriesBuilder_ == null) {
        return queries_.get(index);
      } else {
        return queriesBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .opentsdb.SubQuery queries = 1;</code>
     */
    public Builder setQueries(
        int index, net.opentsdb.grpc.SubQuery value) {
      if (queriesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureQueriesIsMutable();
        queries_.set(index, value);
        onChanged();
      } else {
        queriesBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.SubQuery queries = 1;</code>
     */
    public Builder setQueries(
        int index, net.opentsdb.grpc.SubQuery.Builder builderForValue) {
      if (queriesBuilder_ == null) {
        ensureQueriesIsMutable();
        queries_.set(index, builderForValue.build());
        onChanged();
      } else {
        queriesBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.SubQuery queries = 1;</code>
     */
    public Builder addQueries(net.opentsdb.grpc.SubQuery value) {
      if (queriesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureQueriesIsMutable();
        queries_.add(value);
        onChanged();
      } else {
        queriesBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.SubQuery queries = 1;</code>
     */
    public Builder addQueries(
        int index, net.opentsdb.grpc.SubQuery value) {
      if (queriesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureQueriesIsMutable();
        queries_.add(index, value);
        onChanged();
      } else {
        queriesBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.SubQuery queries = 1;</code>
     */
    public Builder addQueries(
        net.opentsdb.grpc.SubQuery.Builder builderForValue) {
      if (queriesBuilder_ == null) {
        ensureQueriesIsMutable();
        queries_.add(builderForValue.build());
        onChanged();
      } else {
        queriesBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.SubQuery queries = 1;</code>
     */
    public Builder addQueries(
        int index, net.opentsdb.grpc.SubQuery.Builder builderForValue) {
      if (queriesBuilder_ == null) {
        ensureQueriesIsMutable();
        queries_.add(index, builderForValue.build());
        onChanged();
      } else {
        queriesBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.SubQuery queries = 1;</code>
     */
    public Builder addAllQueries(
        java.lang.Iterable<? extends net.opentsdb.grpc.SubQuery> values) {
      if (queriesBuilder_ == null) {
        ensureQueriesIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, queries_);
        onChanged();
      } else {
        queriesBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.SubQuery queries = 1;</code>
     */
    public Builder clearQueries() {
      if (queriesBuilder_ == null) {
        queries_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        queriesBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.SubQuery queries = 1;</code>
     */
    public Builder removeQueries(int index) {
      if (queriesBuilder_ == null) {
        ensureQueriesIsMutable();
        queries_.remove(index);
        onChanged();
      } else {
        queriesBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.SubQuery queries = 1;</code>
     */
    public net.opentsdb.grpc.SubQuery.Builder getQueriesBuilder(
        int index) {
      return getQueriesFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .opentsdb.SubQuery queries = 1;</code>
     */
    public net.opentsdb.grpc.SubQueryOrBuilder getQueriesOrBuilder(
        int index) {
      if (queriesBuilder_ == null) {
        return queries_.get(index);  } else {
        return queriesBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .opentsdb.SubQuery queries = 1;</code>
     */
    public java.util.List<? extends net.opentsdb.grpc.SubQueryOrBuilder> 
         getQueriesOrBuilderList() {
      if (queriesBuilder_ != null) {
        return queriesBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(queries_);
      }
    }
    /**
     * <code>repeated .opentsdb.SubQuery queries = 1;</code>
     */
    public net.opentsdb.grpc.SubQuery.Builder addQueriesBuilder() {
      return getQueriesFieldBuilder().addBuilder(
          net.opentsdb.grpc.SubQuery.getDefaultInstance());
    }
    /**
     * <code>repeated .opentsdb.SubQuery queries = 1;</code>
     */
    public net.opentsdb.grpc.SubQuery.Builder addQueriesBuilder(
        int index) {
      return getQueriesFieldBuilder().addBuilder(
          index, net.opentsdb.grpc.SubQuery.getDefaultInstance());
    }
    /**
     * <code>repeated .opentsdb.SubQuery queries = 1;</code>
     */
    public java.util.List<net.opentsdb.grpc.SubQuery.Builder> 
         getQueriesBuilderList() {
      return getQueriesFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        net.opentsdb.grpc.SubQuery, net.opentsdb.grpc.SubQuery.Builder, net.opentsdb.grpc.SubQueryOrBuilder> 
        getQueriesFieldBuilder() {
      if (queriesBuilder_ == null) {
        queriesBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            net.opentsdb.grpc.SubQuery, net.opentsdb.grpc.SubQuery.Builder, net.opentsdb.grpc.SubQueryOrBuilder>(
                queries_,
                ((bitField0_ & 0x00000001) == 0x00000001),
                getParentForChildren(),
                isClean());
        queries_ = null;
      }
      return queriesBuilder_;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:opentsdb.SubQueries)
  }

  // @@protoc_insertion_point(class_scope:opentsdb.SubQueries)
  private static final net.opentsdb.grpc.SubQueries DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new net.opentsdb.grpc.SubQueries();
  }

  public static net.opentsdb.grpc.SubQueries getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<SubQueries>
      PARSER = new com.google.protobuf.AbstractParser<SubQueries>() {
    public SubQueries parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new SubQueries(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<SubQueries> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<SubQueries> getParserForType() {
    return PARSER;
  }

  public net.opentsdb.grpc.SubQueries getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

