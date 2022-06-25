package com.google.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class StringValue extends GeneratedMessageV3 implements StringValueOrBuilder {
   private static final long serialVersionUID = 0L;
   public static final int VALUE_FIELD_NUMBER = 1;
   private volatile Object value_;
   private byte memoizedIsInitialized = -1;
   private static final StringValue DEFAULT_INSTANCE = new StringValue();
   private static final Parser<StringValue> PARSER = new AbstractParser<StringValue>() {
      public StringValue parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return new StringValue(input, extensionRegistry);
      }
   };

   private StringValue(GeneratedMessageV3.Builder<?> builder) {
      super(builder);
   }

   private StringValue() {
      this.value_ = "";
   }

   @Override
   protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
      return new StringValue();
   }

   @Override
   public final UnknownFieldSet getUnknownFields() {
      return this.unknownFields;
   }

   private StringValue(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
         throw new NullPointerException();
      } else {
         UnknownFieldSet.Builder unknownFields = UnknownFieldSet.newBuilder();

         try {
            boolean done = false;

            while(!done) {
               int tag = input.readTag();
               switch(tag) {
                  case 0:
                     done = true;
                     break;
                  case 10:
                     String s = input.readStringRequireUtf8();
                     this.value_ = s;
                     break;
                  default:
                     if (!this.parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                        done = true;
                     }
               }
            }
         } catch (InvalidProtocolBufferException var12) {
            throw var12.setUnfinishedMessage(this);
         } catch (UninitializedMessageException var13) {
            throw var13.asInvalidProtocolBufferException().setUnfinishedMessage(this);
         } catch (IOException var14) {
            throw new InvalidProtocolBufferException(var14).setUnfinishedMessage(this);
         } finally {
            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }

      }
   }

   public static final Descriptors.Descriptor getDescriptor() {
      return WrappersProto.internal_static_google_protobuf_StringValue_descriptor;
   }

   @Override
   protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
      return WrappersProto.internal_static_google_protobuf_StringValue_fieldAccessorTable
         .ensureFieldAccessorsInitialized(StringValue.class, StringValue.Builder.class);
   }

   @Override
   public String getValue() {
      Object ref = this.value_;
      if (ref instanceof String) {
         return (String)ref;
      } else {
         ByteString bs = (ByteString)ref;
         String s = bs.toStringUtf8();
         this.value_ = s;
         return s;
      }
   }

   @Override
   public ByteString getValueBytes() {
      Object ref = this.value_;
      if (ref instanceof String) {
         ByteString b = ByteString.copyFromUtf8((String)ref);
         this.value_ = b;
         return b;
      } else {
         return (ByteString)ref;
      }
   }

   @Override
   public final boolean isInitialized() {
      byte isInitialized = this.memoizedIsInitialized;
      if (isInitialized == 1) {
         return true;
      } else if (isInitialized == 0) {
         return false;
      } else {
         this.memoizedIsInitialized = 1;
         return true;
      }
   }

   @Override
   public void writeTo(CodedOutputStream output) throws IOException {
      if (!GeneratedMessageV3.isStringEmpty(this.value_)) {
         GeneratedMessageV3.writeString(output, 1, this.value_);
      }

      this.unknownFields.writeTo(output);
   }

   @Override
   public int getSerializedSize() {
      int size = this.memoizedSize;
      if (size != -1) {
         return size;
      } else {
         size = 0;
         if (!GeneratedMessageV3.isStringEmpty(this.value_)) {
            size += GeneratedMessageV3.computeStringSize(1, this.value_);
         }

         size += this.unknownFields.getSerializedSize();
         this.memoizedSize = size;
         return size;
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof StringValue)) {
         return super.equals(obj);
      } else {
         StringValue other = (StringValue)obj;
         if (!this.getValue().equals(other.getValue())) {
            return false;
         } else {
            return this.unknownFields.equals(other.unknownFields);
         }
      }
   }

   @Override
   public int hashCode() {
      if (this.memoizedHashCode != 0) {
         return this.memoizedHashCode;
      } else {
         int hash = 41;
         hash = 19 * hash + getDescriptor().hashCode();
         hash = 37 * hash + 1;
         hash = 53 * hash + this.getValue().hashCode();
         hash = 29 * hash + this.unknownFields.hashCode();
         this.memoizedHashCode = hash;
         return hash;
      }
   }

   public static StringValue parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static StringValue parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static StringValue parseFrom(ByteString data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static StringValue parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static StringValue parseFrom(byte[] data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static StringValue parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static StringValue parseFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static StringValue parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public static StringValue parseDelimitedFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
   }

   public static StringValue parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
   }

   public static StringValue parseFrom(CodedInputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static StringValue parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public StringValue.Builder newBuilderForType() {
      return newBuilder();
   }

   public static StringValue.Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
   }

   public static StringValue.Builder newBuilder(StringValue prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
   }

   public StringValue.Builder toBuilder() {
      return this == DEFAULT_INSTANCE ? new StringValue.Builder() : new StringValue.Builder().mergeFrom(this);
   }

   protected StringValue.Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
      return new StringValue.Builder(parent);
   }

   public static StringValue getDefaultInstance() {
      return DEFAULT_INSTANCE;
   }

   public static StringValue of(String value) {
      return newBuilder().setValue(value).build();
   }

   public static Parser<StringValue> parser() {
      return PARSER;
   }

   @Override
   public Parser<StringValue> getParserForType() {
      return PARSER;
   }

   public StringValue getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
   }

   public static final class Builder extends GeneratedMessageV3.Builder<StringValue.Builder> implements StringValueOrBuilder {
      private Object value_ = "";

      public static final Descriptors.Descriptor getDescriptor() {
         return WrappersProto.internal_static_google_protobuf_StringValue_descriptor;
      }

      @Override
      protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
         return WrappersProto.internal_static_google_protobuf_StringValue_fieldAccessorTable
            .ensureFieldAccessorsInitialized(StringValue.class, StringValue.Builder.class);
      }

      private Builder() {
         this.maybeForceBuilderInitialization();
      }

      private Builder(GeneratedMessageV3.BuilderParent parent) {
         super(parent);
         this.maybeForceBuilderInitialization();
      }

      private void maybeForceBuilderInitialization() {
         if (GeneratedMessageV3.alwaysUseFieldBuilders) {
         }

      }

      public StringValue.Builder clear() {
         super.clear();
         this.value_ = "";
         return this;
      }

      @Override
      public Descriptors.Descriptor getDescriptorForType() {
         return WrappersProto.internal_static_google_protobuf_StringValue_descriptor;
      }

      public StringValue getDefaultInstanceForType() {
         return StringValue.getDefaultInstance();
      }

      public StringValue build() {
         StringValue result = this.buildPartial();
         if (!result.isInitialized()) {
            throw newUninitializedMessageException(result);
         } else {
            return result;
         }
      }

      public StringValue buildPartial() {
         StringValue result = new StringValue(this);
         result.value_ = this.value_;
         this.onBuilt();
         return result;
      }

      public StringValue.Builder clone() {
         return (StringValue.Builder)super.clone();
      }

      public StringValue.Builder setField(Descriptors.FieldDescriptor field, Object value) {
         return (StringValue.Builder)super.setField(field, value);
      }

      public StringValue.Builder clearField(Descriptors.FieldDescriptor field) {
         return (StringValue.Builder)super.clearField(field);
      }

      public StringValue.Builder clearOneof(Descriptors.OneofDescriptor oneof) {
         return (StringValue.Builder)super.clearOneof(oneof);
      }

      public StringValue.Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
         return (StringValue.Builder)super.setRepeatedField(field, index, value);
      }

      public StringValue.Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
         return (StringValue.Builder)super.addRepeatedField(field, value);
      }

      public StringValue.Builder mergeFrom(Message other) {
         if (other instanceof StringValue) {
            return this.mergeFrom((StringValue)other);
         } else {
            super.mergeFrom(other);
            return this;
         }
      }

      public StringValue.Builder mergeFrom(StringValue other) {
         if (other == StringValue.getDefaultInstance()) {
            return this;
         } else {
            if (!other.getValue().isEmpty()) {
               this.value_ = other.value_;
               this.onChanged();
            }

            this.mergeUnknownFields(other.unknownFields);
            this.onChanged();
            return this;
         }
      }

      @Override
      public final boolean isInitialized() {
         return true;
      }

      public StringValue.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         StringValue parsedMessage = null;

         try {
            parsedMessage = StringValue.PARSER.parsePartialFrom(input, extensionRegistry);
         } catch (InvalidProtocolBufferException var8) {
            parsedMessage = (StringValue)var8.getUnfinishedMessage();
            throw var8.unwrapIOException();
         } finally {
            if (parsedMessage != null) {
               this.mergeFrom(parsedMessage);
            }

         }

         return this;
      }

      @Override
      public String getValue() {
         Object ref = this.value_;
         if (!(ref instanceof String)) {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            this.value_ = s;
            return s;
         } else {
            return (String)ref;
         }
      }

      @Override
      public ByteString getValueBytes() {
         Object ref = this.value_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.value_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      public StringValue.Builder setValue(String value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            this.value_ = value;
            this.onChanged();
            return this;
         }
      }

      public StringValue.Builder clearValue() {
         this.value_ = StringValue.getDefaultInstance().getValue();
         this.onChanged();
         return this;
      }

      public StringValue.Builder setValueBytes(ByteString value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            AbstractMessageLite.checkByteStringIsUtf8(value);
            this.value_ = value;
            this.onChanged();
            return this;
         }
      }

      public final StringValue.Builder setUnknownFields(UnknownFieldSet unknownFields) {
         return (StringValue.Builder)super.setUnknownFields(unknownFields);
      }

      public final StringValue.Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
         return (StringValue.Builder)super.mergeUnknownFields(unknownFields);
      }
   }
}
