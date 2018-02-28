// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: opentsdb.proto

package net.opentsdb.grpc;

/**
 * Protobuf type {@code opentsdb.PutDatapointsResponse}
 */
public  final class PutDatapointsResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:opentsdb.PutDatapointsResponse)
    PutDatapointsResponseOrBuilder {
private static final long serialVersionUID = 0L;
  // Use PutDatapointsResponse.newBuilder() to construct.
  private PutDatapointsResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private PutDatapointsResponse() {
    success_ = 0;
    failed_ = 0;
    errors_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private PutDatapointsResponse(
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

            success_ = input.readInt32();
            break;
          }
          case 16: {

            failed_ = input.readInt32();
            break;
          }
          case 26: {
            if (!((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
              errors_ = new java.util.ArrayList<net.opentsdb.grpc.PutDatapointError>();
              mutable_bitField0_ |= 0x00000004;
            }
            errors_.add(
                input.readMessage(net.opentsdb.grpc.PutDatapointError.parser(), extensionRegistry));
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
      if (((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
        errors_ = java.util.Collections.unmodifiableList(errors_);
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_PutDatapointsResponse_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_PutDatapointsResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            net.opentsdb.grpc.PutDatapointsResponse.class, net.opentsdb.grpc.PutDatapointsResponse.Builder.class);
  }

  private int bitField0_;
  public static final int SUCCESS_FIELD_NUMBER = 1;
  private int success_;
  /**
   * <code>int32 success = 1;</code>
   */
  public int getSuccess() {
    return success_;
  }

  public static final int FAILED_FIELD_NUMBER = 2;
  private int failed_;
  /**
   * <code>int32 failed = 2;</code>
   */
  public int getFailed() {
    return failed_;
  }

  public static final int ERRORS_FIELD_NUMBER = 3;
  private java.util.List<net.opentsdb.grpc.PutDatapointError> errors_;
  /**
   * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
   */
  public java.util.List<net.opentsdb.grpc.PutDatapointError> getErrorsList() {
    return errors_;
  }
  /**
   * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
   */
  public java.util.List<? extends net.opentsdb.grpc.PutDatapointErrorOrBuilder> 
      getErrorsOrBuilderList() {
    return errors_;
  }
  /**
   * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
   */
  public int getErrorsCount() {
    return errors_.size();
  }
  /**
   * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
   */
  public net.opentsdb.grpc.PutDatapointError getErrors(int index) {
    return errors_.get(index);
  }
  /**
   * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
   */
  public net.opentsdb.grpc.PutDatapointErrorOrBuilder getErrorsOrBuilder(
      int index) {
    return errors_.get(index);
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
    if (success_ != 0) {
      output.writeInt32(1, success_);
    }
    if (failed_ != 0) {
      output.writeInt32(2, failed_);
    }
    for (int i = 0; i < errors_.size(); i++) {
      output.writeMessage(3, errors_.get(i));
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (success_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(1, success_);
    }
    if (failed_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(2, failed_);
    }
    for (int i = 0; i < errors_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(3, errors_.get(i));
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
    if (!(obj instanceof net.opentsdb.grpc.PutDatapointsResponse)) {
      return super.equals(obj);
    }
    net.opentsdb.grpc.PutDatapointsResponse other = (net.opentsdb.grpc.PutDatapointsResponse) obj;

    boolean result = true;
    result = result && (getSuccess()
        == other.getSuccess());
    result = result && (getFailed()
        == other.getFailed());
    result = result && getErrorsList()
        .equals(other.getErrorsList());
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
    hash = (37 * hash) + SUCCESS_FIELD_NUMBER;
    hash = (53 * hash) + getSuccess();
    hash = (37 * hash) + FAILED_FIELD_NUMBER;
    hash = (53 * hash) + getFailed();
    if (getErrorsCount() > 0) {
      hash = (37 * hash) + ERRORS_FIELD_NUMBER;
      hash = (53 * hash) + getErrorsList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static net.opentsdb.grpc.PutDatapointsResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static net.opentsdb.grpc.PutDatapointsResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static net.opentsdb.grpc.PutDatapointsResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static net.opentsdb.grpc.PutDatapointsResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static net.opentsdb.grpc.PutDatapointsResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static net.opentsdb.grpc.PutDatapointsResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static net.opentsdb.grpc.PutDatapointsResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static net.opentsdb.grpc.PutDatapointsResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static net.opentsdb.grpc.PutDatapointsResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static net.opentsdb.grpc.PutDatapointsResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static net.opentsdb.grpc.PutDatapointsResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static net.opentsdb.grpc.PutDatapointsResponse parseFrom(
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
  public static Builder newBuilder(net.opentsdb.grpc.PutDatapointsResponse prototype) {
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
   * Protobuf type {@code opentsdb.PutDatapointsResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:opentsdb.PutDatapointsResponse)
      net.opentsdb.grpc.PutDatapointsResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_PutDatapointsResponse_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_PutDatapointsResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              net.opentsdb.grpc.PutDatapointsResponse.class, net.opentsdb.grpc.PutDatapointsResponse.Builder.class);
    }

    // Construct using net.opentsdb.grpc.PutDatapointsResponse.newBuilder()
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
        getErrorsFieldBuilder();
      }
    }
    public Builder clear() {
      super.clear();
      success_ = 0;

      failed_ = 0;

      if (errorsBuilder_ == null) {
        errors_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000004);
      } else {
        errorsBuilder_.clear();
      }
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return net.opentsdb.grpc.Opentsdb.internal_static_opentsdb_PutDatapointsResponse_descriptor;
    }

    public net.opentsdb.grpc.PutDatapointsResponse getDefaultInstanceForType() {
      return net.opentsdb.grpc.PutDatapointsResponse.getDefaultInstance();
    }

    public net.opentsdb.grpc.PutDatapointsResponse build() {
      net.opentsdb.grpc.PutDatapointsResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public net.opentsdb.grpc.PutDatapointsResponse buildPartial() {
      net.opentsdb.grpc.PutDatapointsResponse result = new net.opentsdb.grpc.PutDatapointsResponse(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      result.success_ = success_;
      result.failed_ = failed_;
      if (errorsBuilder_ == null) {
        if (((bitField0_ & 0x00000004) == 0x00000004)) {
          errors_ = java.util.Collections.unmodifiableList(errors_);
          bitField0_ = (bitField0_ & ~0x00000004);
        }
        result.errors_ = errors_;
      } else {
        result.errors_ = errorsBuilder_.build();
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
      if (other instanceof net.opentsdb.grpc.PutDatapointsResponse) {
        return mergeFrom((net.opentsdb.grpc.PutDatapointsResponse)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(net.opentsdb.grpc.PutDatapointsResponse other) {
      if (other == net.opentsdb.grpc.PutDatapointsResponse.getDefaultInstance()) return this;
      if (other.getSuccess() != 0) {
        setSuccess(other.getSuccess());
      }
      if (other.getFailed() != 0) {
        setFailed(other.getFailed());
      }
      if (errorsBuilder_ == null) {
        if (!other.errors_.isEmpty()) {
          if (errors_.isEmpty()) {
            errors_ = other.errors_;
            bitField0_ = (bitField0_ & ~0x00000004);
          } else {
            ensureErrorsIsMutable();
            errors_.addAll(other.errors_);
          }
          onChanged();
        }
      } else {
        if (!other.errors_.isEmpty()) {
          if (errorsBuilder_.isEmpty()) {
            errorsBuilder_.dispose();
            errorsBuilder_ = null;
            errors_ = other.errors_;
            bitField0_ = (bitField0_ & ~0x00000004);
            errorsBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getErrorsFieldBuilder() : null;
          } else {
            errorsBuilder_.addAllMessages(other.errors_);
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
      net.opentsdb.grpc.PutDatapointsResponse parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (net.opentsdb.grpc.PutDatapointsResponse) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private int success_ ;
    /**
     * <code>int32 success = 1;</code>
     */
    public int getSuccess() {
      return success_;
    }
    /**
     * <code>int32 success = 1;</code>
     */
    public Builder setSuccess(int value) {
      
      success_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 success = 1;</code>
     */
    public Builder clearSuccess() {
      
      success_ = 0;
      onChanged();
      return this;
    }

    private int failed_ ;
    /**
     * <code>int32 failed = 2;</code>
     */
    public int getFailed() {
      return failed_;
    }
    /**
     * <code>int32 failed = 2;</code>
     */
    public Builder setFailed(int value) {
      
      failed_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 failed = 2;</code>
     */
    public Builder clearFailed() {
      
      failed_ = 0;
      onChanged();
      return this;
    }

    private java.util.List<net.opentsdb.grpc.PutDatapointError> errors_ =
      java.util.Collections.emptyList();
    private void ensureErrorsIsMutable() {
      if (!((bitField0_ & 0x00000004) == 0x00000004)) {
        errors_ = new java.util.ArrayList<net.opentsdb.grpc.PutDatapointError>(errors_);
        bitField0_ |= 0x00000004;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        net.opentsdb.grpc.PutDatapointError, net.opentsdb.grpc.PutDatapointError.Builder, net.opentsdb.grpc.PutDatapointErrorOrBuilder> errorsBuilder_;

    /**
     * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
     */
    public java.util.List<net.opentsdb.grpc.PutDatapointError> getErrorsList() {
      if (errorsBuilder_ == null) {
        return java.util.Collections.unmodifiableList(errors_);
      } else {
        return errorsBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
     */
    public int getErrorsCount() {
      if (errorsBuilder_ == null) {
        return errors_.size();
      } else {
        return errorsBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
     */
    public net.opentsdb.grpc.PutDatapointError getErrors(int index) {
      if (errorsBuilder_ == null) {
        return errors_.get(index);
      } else {
        return errorsBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
     */
    public Builder setErrors(
        int index, net.opentsdb.grpc.PutDatapointError value) {
      if (errorsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureErrorsIsMutable();
        errors_.set(index, value);
        onChanged();
      } else {
        errorsBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
     */
    public Builder setErrors(
        int index, net.opentsdb.grpc.PutDatapointError.Builder builderForValue) {
      if (errorsBuilder_ == null) {
        ensureErrorsIsMutable();
        errors_.set(index, builderForValue.build());
        onChanged();
      } else {
        errorsBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
     */
    public Builder addErrors(net.opentsdb.grpc.PutDatapointError value) {
      if (errorsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureErrorsIsMutable();
        errors_.add(value);
        onChanged();
      } else {
        errorsBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
     */
    public Builder addErrors(
        int index, net.opentsdb.grpc.PutDatapointError value) {
      if (errorsBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureErrorsIsMutable();
        errors_.add(index, value);
        onChanged();
      } else {
        errorsBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
     */
    public Builder addErrors(
        net.opentsdb.grpc.PutDatapointError.Builder builderForValue) {
      if (errorsBuilder_ == null) {
        ensureErrorsIsMutable();
        errors_.add(builderForValue.build());
        onChanged();
      } else {
        errorsBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
     */
    public Builder addErrors(
        int index, net.opentsdb.grpc.PutDatapointError.Builder builderForValue) {
      if (errorsBuilder_ == null) {
        ensureErrorsIsMutable();
        errors_.add(index, builderForValue.build());
        onChanged();
      } else {
        errorsBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
     */
    public Builder addAllErrors(
        java.lang.Iterable<? extends net.opentsdb.grpc.PutDatapointError> values) {
      if (errorsBuilder_ == null) {
        ensureErrorsIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, errors_);
        onChanged();
      } else {
        errorsBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
     */
    public Builder clearErrors() {
      if (errorsBuilder_ == null) {
        errors_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000004);
        onChanged();
      } else {
        errorsBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
     */
    public Builder removeErrors(int index) {
      if (errorsBuilder_ == null) {
        ensureErrorsIsMutable();
        errors_.remove(index);
        onChanged();
      } else {
        errorsBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
     */
    public net.opentsdb.grpc.PutDatapointError.Builder getErrorsBuilder(
        int index) {
      return getErrorsFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
     */
    public net.opentsdb.grpc.PutDatapointErrorOrBuilder getErrorsOrBuilder(
        int index) {
      if (errorsBuilder_ == null) {
        return errors_.get(index);  } else {
        return errorsBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
     */
    public java.util.List<? extends net.opentsdb.grpc.PutDatapointErrorOrBuilder> 
         getErrorsOrBuilderList() {
      if (errorsBuilder_ != null) {
        return errorsBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(errors_);
      }
    }
    /**
     * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
     */
    public net.opentsdb.grpc.PutDatapointError.Builder addErrorsBuilder() {
      return getErrorsFieldBuilder().addBuilder(
          net.opentsdb.grpc.PutDatapointError.getDefaultInstance());
    }
    /**
     * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
     */
    public net.opentsdb.grpc.PutDatapointError.Builder addErrorsBuilder(
        int index) {
      return getErrorsFieldBuilder().addBuilder(
          index, net.opentsdb.grpc.PutDatapointError.getDefaultInstance());
    }
    /**
     * <code>repeated .opentsdb.PutDatapointError errors = 3;</code>
     */
    public java.util.List<net.opentsdb.grpc.PutDatapointError.Builder> 
         getErrorsBuilderList() {
      return getErrorsFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        net.opentsdb.grpc.PutDatapointError, net.opentsdb.grpc.PutDatapointError.Builder, net.opentsdb.grpc.PutDatapointErrorOrBuilder> 
        getErrorsFieldBuilder() {
      if (errorsBuilder_ == null) {
        errorsBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            net.opentsdb.grpc.PutDatapointError, net.opentsdb.grpc.PutDatapointError.Builder, net.opentsdb.grpc.PutDatapointErrorOrBuilder>(
                errors_,
                ((bitField0_ & 0x00000004) == 0x00000004),
                getParentForChildren(),
                isClean());
        errors_ = null;
      }
      return errorsBuilder_;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:opentsdb.PutDatapointsResponse)
  }

  // @@protoc_insertion_point(class_scope:opentsdb.PutDatapointsResponse)
  private static final net.opentsdb.grpc.PutDatapointsResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new net.opentsdb.grpc.PutDatapointsResponse();
  }

  public static net.opentsdb.grpc.PutDatapointsResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<PutDatapointsResponse>
      PARSER = new com.google.protobuf.AbstractParser<PutDatapointsResponse>() {
    public PutDatapointsResponse parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new PutDatapointsResponse(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<PutDatapointsResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<PutDatapointsResponse> getParserForType() {
    return PARSER;
  }

  public net.opentsdb.grpc.PutDatapointsResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

