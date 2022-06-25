package com.google.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class BytesValue extends GeneratedMessageV3 implements BytesValueOrBuilder {
   private static final long serialVersionUID = 0L;
   public static final int VALUE_FIELD_NUMBER = 1;
   private ByteString value_;
   private byte memoizedIsInitialized = -1;
   private static final BytesValue DEFAULT_INSTANCE = new BytesValue();
   private static final Parser<BytesValue> PARSER = new AbstractParser<BytesValue>() {
      public BytesValue parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return new BytesValue(input, extensionRegistry);
      }
   };

   private BytesValue(GeneratedMessageV3.Builder<?> builder) {
      super(builder);
   }

   private BytesValue() {
      this.value_ = ByteString.EMPTY;
   }

   @Override
   protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
      return new BytesValue();
   }

   @Override
   public final UnknownFieldSet getUnknownFields() {
      return this.unknownFields;
   }

   private BytesValue(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     this.value_ = input.readBytes();
                     break;
                  default:
                     if (!this.parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                        done = true;
                     }
               }
            }
         } catch (InvalidProtocolBufferException var11) {
            throw var11.setUnfinishedMessage(this);
         } catch (UninitializedMessageException var12) {
            throw var12.asInvalidProtocolBufferException().setUnfinishedMessage(this);
         } catch (IOException var13) {
            throw new InvalidProtocolBufferException(var13).setUnfinishedMessage(this);
         } finally {
            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }

      }
   }

   public static final Descriptors.Descriptor getDescriptor() {
      return WrappersProto.internal_static_google_protobuf_BytesValue_descriptor;
   }

   @Override
   protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
      return WrappersProto.internal_static_google_protobuf_BytesValue_fieldAccessorTable
         .ensureFieldAccessorsInitialized(BytesValue.class, BytesValue.Builder.class);
   }

   @Override
   public ByteString getValue() {
      return this.value_;
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
      if (!this.value_.isEmpty()) {
         output.writeBytes(1, this.value_);
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
         if (!this.value_.isEmpty()) {
            size += CodedOutputStream.computeBytesSize(1, this.value_);
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
      } else if (!(obj instanceof BytesValue)) {
         return super.equals(obj);
      } else {
         BytesValue other = (BytesValue)obj;
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

   public static BytesValue parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static BytesValue parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static BytesValue parseFrom(ByteString data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static BytesValue parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static BytesValue parseFrom(byte[] data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static BytesValue parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static BytesValue parseFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static BytesValue parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public static BytesValue parseDelimitedFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
   }

   public static BytesValue parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
   }

   public static BytesValue parseFrom(CodedInputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static BytesValue parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public BytesValue.Builder newBuilderForType() {
      return newBuilder();
   }

   public static BytesValue.Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
   }

   public static BytesValue.Builder newBuilder(BytesValue prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
   }

   public BytesValue.Builder toBuilder() {
      return this == DEFAULT_INSTANCE ? new BytesValue.Builder() : new BytesValue.Builder().mergeFrom(this);
   }

   protected BytesValue.Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
      return new BytesValue.Builder(parent);
   }

   public static BytesValue getDefaultInstance() {
      return DEFAULT_INSTANCE;
   }

   public static BytesValue of(ByteString value) {
      return newBuilder().setValue(value).build();
   }

   public static Parser<BytesValue> parser() {
      return PARSER;
   }

   @Override
   public Parser<BytesValue> getParserForType() {
      return PARSER;
   }

   public BytesValue getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
   }

   public static final class Builder extends GeneratedMessageV3.Builder<BytesValue.Builder> implements BytesValueOrBuilder {
      private ByteString value_ = ByteString.EMPTY;

      public static final Descriptors.Descriptor getDescriptor() {
         return WrappersProto.internal_static_google_protobuf_BytesValue_descriptor;
      }

      @Override
      protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
         return WrappersProto.internal_static_google_protobuf_BytesValue_fieldAccessorTable
            .ensureFieldAccessorsInitialized(BytesValue.class, BytesValue.Builder.class);
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

      public BytesValue.Builder clear() {
         super.clear();
         this.value_ = ByteString.EMPTY;
         return this;
      }

      @Override
      public Descriptors.Descriptor getDescriptorForType() {
         return WrappersProto.internal_static_google_protobuf_BytesValue_descriptor;
      }

      public BytesValue getDefaultInstanceForType() {
         return BytesValue.getDefaultInstance();
      }

      public BytesValue build() {
         BytesValue result = this.buildPartial();
         if (!result.isInitialized()) {
            throw newUninitializedMessageException(result);
         } else {
            return result;
         }
      }

      public BytesValue buildPartial() {
         BytesValue result = new BytesValue(this);
         result.value_ = this.value_;
         this.onBuilt();
         return result;
      }

      public BytesValue.Builder clone() {
         return (BytesValue.Builder)super.clone();
      }

      public BytesValue.Builder setField(Descriptors.FieldDescriptor field, Object value) {
         return (BytesValue.Builder)super.setField(field, value);
      }

      public BytesValue.Builder clearField(Descriptors.FieldDescriptor field) {
         return (BytesValue.Builder)super.clearField(field);
      }

      public BytesValue.Builder clearOneof(Descriptors.OneofDescriptor oneof) {
         return (BytesValue.Builder)super.clearOneof(oneof);
      }

      public BytesValue.Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
         return (BytesValue.Builder)super.setRepeatedField(field, index, value);
      }

      public BytesValue.Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
         return (BytesValue.Builder)super.addRepeatedField(field, value);
      }

      public BytesValue.Builder mergeFrom(Message other) {
         if (other instanceof BytesValue) {
            return this.mergeFrom((BytesValue)other);
         } else {
            super.mergeFrom(other);
            return this;
         }
      }

      public BytesValue.Builder mergeFrom(BytesValue other) {
         if (other == BytesValue.getDefaultInstance()) {
            return this;
         } else {
            if (other.getValue() != ByteString.EMPTY) {
               this.setValue(other.getValue());
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

      public BytesValue.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         BytesValue parsedMessage = null;

         try {
            parsedMessage = BytesValue.PARSER.parsePartialFrom(input, extensionRegistry);
         } catch (InvalidProtocolBufferException var8) {
            parsedMessage = (BytesValue)var8.getUnfinishedMessage();
            throw var8.unwrapIOException();
         } finally {
            if (parsedMessage != null) {
               this.mergeFrom(parsedMessage);
            }

         }

         return this;
      }

      @Override
      public ByteString getValue() {
         return this.value_;
      }

      public BytesValue.Builder setValue(ByteString value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            this.value_ = value;
            this.onChanged();
            return this;
         }
      }

      public BytesValue.Builder clearValue() {
         this.value_ = BytesValue.getDefaultInstance().getValue();
         this.onChanged();
         return this;
      }

      public final BytesValue.Builder setUnknownFields(UnknownFieldSet unknownFields) {
         return (BytesValue.Builder)super.setUnknownFields(unknownFields);
      }

      public final BytesValue.Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
         return (BytesValue.Builder)super.mergeUnknownFields(unknownFields);
      }
   }
}
