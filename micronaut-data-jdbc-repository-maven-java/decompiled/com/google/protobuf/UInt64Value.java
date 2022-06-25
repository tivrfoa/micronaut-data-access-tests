package com.google.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class UInt64Value extends GeneratedMessageV3 implements UInt64ValueOrBuilder {
   private static final long serialVersionUID = 0L;
   public static final int VALUE_FIELD_NUMBER = 1;
   private long value_;
   private byte memoizedIsInitialized = -1;
   private static final UInt64Value DEFAULT_INSTANCE = new UInt64Value();
   private static final Parser<UInt64Value> PARSER = new AbstractParser<UInt64Value>() {
      public UInt64Value parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return new UInt64Value(input, extensionRegistry);
      }
   };

   private UInt64Value(GeneratedMessageV3.Builder<?> builder) {
      super(builder);
   }

   private UInt64Value() {
   }

   @Override
   protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
      return new UInt64Value();
   }

   @Override
   public final UnknownFieldSet getUnknownFields() {
      return this.unknownFields;
   }

   private UInt64Value(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                  case 8:
                     this.value_ = input.readUInt64();
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
      return WrappersProto.internal_static_google_protobuf_UInt64Value_descriptor;
   }

   @Override
   protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
      return WrappersProto.internal_static_google_protobuf_UInt64Value_fieldAccessorTable
         .ensureFieldAccessorsInitialized(UInt64Value.class, UInt64Value.Builder.class);
   }

   @Override
   public long getValue() {
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
      if (this.value_ != 0L) {
         output.writeUInt64(1, this.value_);
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
         if (this.value_ != 0L) {
            size += CodedOutputStream.computeUInt64Size(1, this.value_);
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
      } else if (!(obj instanceof UInt64Value)) {
         return super.equals(obj);
      } else {
         UInt64Value other = (UInt64Value)obj;
         if (this.getValue() != other.getValue()) {
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
         hash = 53 * hash + Internal.hashLong(this.getValue());
         hash = 29 * hash + this.unknownFields.hashCode();
         this.memoizedHashCode = hash;
         return hash;
      }
   }

   public static UInt64Value parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static UInt64Value parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static UInt64Value parseFrom(ByteString data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static UInt64Value parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static UInt64Value parseFrom(byte[] data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static UInt64Value parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static UInt64Value parseFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static UInt64Value parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public static UInt64Value parseDelimitedFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
   }

   public static UInt64Value parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
   }

   public static UInt64Value parseFrom(CodedInputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static UInt64Value parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public UInt64Value.Builder newBuilderForType() {
      return newBuilder();
   }

   public static UInt64Value.Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
   }

   public static UInt64Value.Builder newBuilder(UInt64Value prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
   }

   public UInt64Value.Builder toBuilder() {
      return this == DEFAULT_INSTANCE ? new UInt64Value.Builder() : new UInt64Value.Builder().mergeFrom(this);
   }

   protected UInt64Value.Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
      return new UInt64Value.Builder(parent);
   }

   public static UInt64Value getDefaultInstance() {
      return DEFAULT_INSTANCE;
   }

   public static UInt64Value of(long value) {
      return newBuilder().setValue(value).build();
   }

   public static Parser<UInt64Value> parser() {
      return PARSER;
   }

   @Override
   public Parser<UInt64Value> getParserForType() {
      return PARSER;
   }

   public UInt64Value getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
   }

   public static final class Builder extends GeneratedMessageV3.Builder<UInt64Value.Builder> implements UInt64ValueOrBuilder {
      private long value_;

      public static final Descriptors.Descriptor getDescriptor() {
         return WrappersProto.internal_static_google_protobuf_UInt64Value_descriptor;
      }

      @Override
      protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
         return WrappersProto.internal_static_google_protobuf_UInt64Value_fieldAccessorTable
            .ensureFieldAccessorsInitialized(UInt64Value.class, UInt64Value.Builder.class);
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

      public UInt64Value.Builder clear() {
         super.clear();
         this.value_ = 0L;
         return this;
      }

      @Override
      public Descriptors.Descriptor getDescriptorForType() {
         return WrappersProto.internal_static_google_protobuf_UInt64Value_descriptor;
      }

      public UInt64Value getDefaultInstanceForType() {
         return UInt64Value.getDefaultInstance();
      }

      public UInt64Value build() {
         UInt64Value result = this.buildPartial();
         if (!result.isInitialized()) {
            throw newUninitializedMessageException(result);
         } else {
            return result;
         }
      }

      public UInt64Value buildPartial() {
         UInt64Value result = new UInt64Value(this);
         result.value_ = this.value_;
         this.onBuilt();
         return result;
      }

      public UInt64Value.Builder clone() {
         return (UInt64Value.Builder)super.clone();
      }

      public UInt64Value.Builder setField(Descriptors.FieldDescriptor field, Object value) {
         return (UInt64Value.Builder)super.setField(field, value);
      }

      public UInt64Value.Builder clearField(Descriptors.FieldDescriptor field) {
         return (UInt64Value.Builder)super.clearField(field);
      }

      public UInt64Value.Builder clearOneof(Descriptors.OneofDescriptor oneof) {
         return (UInt64Value.Builder)super.clearOneof(oneof);
      }

      public UInt64Value.Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
         return (UInt64Value.Builder)super.setRepeatedField(field, index, value);
      }

      public UInt64Value.Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
         return (UInt64Value.Builder)super.addRepeatedField(field, value);
      }

      public UInt64Value.Builder mergeFrom(Message other) {
         if (other instanceof UInt64Value) {
            return this.mergeFrom((UInt64Value)other);
         } else {
            super.mergeFrom(other);
            return this;
         }
      }

      public UInt64Value.Builder mergeFrom(UInt64Value other) {
         if (other == UInt64Value.getDefaultInstance()) {
            return this;
         } else {
            if (other.getValue() != 0L) {
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

      public UInt64Value.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         UInt64Value parsedMessage = null;

         try {
            parsedMessage = UInt64Value.PARSER.parsePartialFrom(input, extensionRegistry);
         } catch (InvalidProtocolBufferException var8) {
            parsedMessage = (UInt64Value)var8.getUnfinishedMessage();
            throw var8.unwrapIOException();
         } finally {
            if (parsedMessage != null) {
               this.mergeFrom(parsedMessage);
            }

         }

         return this;
      }

      @Override
      public long getValue() {
         return this.value_;
      }

      public UInt64Value.Builder setValue(long value) {
         this.value_ = value;
         this.onChanged();
         return this;
      }

      public UInt64Value.Builder clearValue() {
         this.value_ = 0L;
         this.onChanged();
         return this;
      }

      public final UInt64Value.Builder setUnknownFields(UnknownFieldSet unknownFields) {
         return (UInt64Value.Builder)super.setUnknownFields(unknownFields);
      }

      public final UInt64Value.Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
         return (UInt64Value.Builder)super.mergeUnknownFields(unknownFields);
      }
   }
}
