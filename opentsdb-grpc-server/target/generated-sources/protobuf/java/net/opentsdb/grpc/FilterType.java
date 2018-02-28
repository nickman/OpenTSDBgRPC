// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: opentsdb.proto

package net.opentsdb.grpc;

/**
 * Protobuf enum {@code opentsdb.FilterType}
 */
public enum FilterType
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>iliteral_or = 0;</code>
   */
  iliteral_or(0),
  /**
   * <code>wildcard = 1;</code>
   */
  wildcard(1),
  /**
   * <code>not_literal_or = 2;</code>
   */
  not_literal_or(2),
  /**
   * <code>not_iliteral_or = 3;</code>
   */
  not_iliteral_or(3),
  /**
   * <code>not_key = 4;</code>
   */
  not_key(4),
  /**
   * <code>iwildcard = 5;</code>
   */
  iwildcard(5),
  /**
   * <code>literal_or = 6;</code>
   */
  literal_or(6),
  /**
   * <code>regexp = 8;</code>
   */
  regexp(8),
  UNRECOGNIZED(-1),
  ;

  /**
   * <code>iliteral_or = 0;</code>
   */
  public static final int iliteral_or_VALUE = 0;
  /**
   * <code>wildcard = 1;</code>
   */
  public static final int wildcard_VALUE = 1;
  /**
   * <code>not_literal_or = 2;</code>
   */
  public static final int not_literal_or_VALUE = 2;
  /**
   * <code>not_iliteral_or = 3;</code>
   */
  public static final int not_iliteral_or_VALUE = 3;
  /**
   * <code>not_key = 4;</code>
   */
  public static final int not_key_VALUE = 4;
  /**
   * <code>iwildcard = 5;</code>
   */
  public static final int iwildcard_VALUE = 5;
  /**
   * <code>literal_or = 6;</code>
   */
  public static final int literal_or_VALUE = 6;
  /**
   * <code>regexp = 8;</code>
   */
  public static final int regexp_VALUE = 8;


  public final int getNumber() {
    if (this == UNRECOGNIZED) {
      throw new java.lang.IllegalArgumentException(
          "Can't get the number of an unknown enum value.");
    }
    return value;
  }

  /**
   * @deprecated Use {@link #forNumber(int)} instead.
   */
  @java.lang.Deprecated
  public static FilterType valueOf(int value) {
    return forNumber(value);
  }

  public static FilterType forNumber(int value) {
    switch (value) {
      case 0: return iliteral_or;
      case 1: return wildcard;
      case 2: return not_literal_or;
      case 3: return not_iliteral_or;
      case 4: return not_key;
      case 5: return iwildcard;
      case 6: return literal_or;
      case 8: return regexp;
      default: return null;
    }
  }

  public static com.google.protobuf.Internal.EnumLiteMap<FilterType>
      internalGetValueMap() {
    return internalValueMap;
  }
  private static final com.google.protobuf.Internal.EnumLiteMap<
      FilterType> internalValueMap =
        new com.google.protobuf.Internal.EnumLiteMap<FilterType>() {
          public FilterType findValueByNumber(int number) {
            return FilterType.forNumber(number);
          }
        };

  public final com.google.protobuf.Descriptors.EnumValueDescriptor
      getValueDescriptor() {
    return getDescriptor().getValues().get(ordinal());
  }
  public final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptorForType() {
    return getDescriptor();
  }
  public static final com.google.protobuf.Descriptors.EnumDescriptor
      getDescriptor() {
    return net.opentsdb.grpc.Opentsdb.getDescriptor().getEnumTypes().get(2);
  }

  private static final FilterType[] VALUES = values();

  public static FilterType valueOf(
      com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
    if (desc.getType() != getDescriptor()) {
      throw new java.lang.IllegalArgumentException(
        "EnumValueDescriptor is not for this type.");
    }
    if (desc.getIndex() == -1) {
      return UNRECOGNIZED;
    }
    return VALUES[desc.getIndex()];
  }

  private final int value;

  private FilterType(int value) {
    this.value = value;
  }

  // @@protoc_insertion_point(enum_scope:opentsdb.FilterType)
}

