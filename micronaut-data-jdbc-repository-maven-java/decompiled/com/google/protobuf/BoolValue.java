package com.google.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class BoolValue extends GeneratedMessageV3 implements BoolValueOrBuilder {
   private static final long serialVersionUID = 0L;
   public static final int VALUE_FIELD_NUMBER = 1;
   private boolean value_;
   private byte memoizedIsInitialized = -1;
   private static final BoolValue DEFAULT_INSTANCE = new BoolValue();
   private static final Parser<BoolValue> PARSER = new AbstractParser<BoolValue>() {
      public BoolValue parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return new BoolValue(input, extensionRegistry);
      }
   };

   private BoolValue(GeneratedMessageV3.Builder<?> builder) {
      super(builder);
   }

   private BoolValue() {
   }

   @Override
   protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
      return new BoolValue();
   }

   @Override
   public final UnknownFieldSet getUnknownFields() {
      return this.unknownFields;
   }

   private BoolValue(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     this.value_ = input.readBool();
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
      return WrappersProto.internal_static_google_protobuf_BoolValue_descriptor;
   }

   @Override
   protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
      return WrappersProto.internal_static_google_protobuf_BoolValue_fieldAccessorTable
         .ensureFieldAccessorsInitialized(BoolValue.class, BoolValue.Builder.class);
   }

   @Override
   public boolean getValue() {
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
      if (this.value_) {
         output.writeBool(1, this.value_);
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
         if (this.value_) {
            size += CodedOutputStream.computeBoolSize(1, this.value_);
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
      } else if (!(obj instanceof BoolValue)) {
         return super.equals(obj);
      } else {
         BoolValue other = (BoolValue)obj;
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
         hash = 53 * hash + Internal.hashBoolean(this.getValue());
         hash = 29 * hash + this.unknownFields.hashCode();
         this.memoizedHashCode = hash;
         return hash;
      }
   }

   public static BoolValue parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static BoolValue parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static BoolValue parseFrom(ByteString data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static BoolValue parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static BoolValue parseFrom(byte[] data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static BoolValue parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static BoolValue parseFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static BoolValue parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public static BoolValue parseDelimitedFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
   }

   public static BoolValue parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
   }

   public static BoolValue parseFrom(CodedInputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static BoolValue parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public BoolValue.Builder newBuilderForType() {
      return newBuilder();
   }

   public static BoolValue.Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
   }

   public static BoolValue.Builder newBuilder(BoolValue prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
   }

   public BoolValue.Builder toBuilder() {
      return this == DEFAULT_INSTANCE ? new BoolValue.Builder() : new BoolValue.Builder().mergeFrom(this);
   }

   protected BoolValue.Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
      return new BoolValue.Builder(parent);
   }

   public static BoolValue getDefaultInstance() {
      return DEFAULT_INSTANCE;
   }

   public static BoolValue of(boolean value) {
      return newBuilder().setValue(value).build();
   }

   public static Parser<BoolValue> parser() {
      return PARSER;
   }

   @Override
   public Parser<BoolValue> getParserForType() {
      return PARSER;
   }

   public BoolValue getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
   }

   public static final class Builder extends GeneratedMessageV3.Builder<BoolValue.Builder> implements BoolValueOrBuilder {
      private boolean value_;

      public static final Descriptors.Descriptor getDescriptor() {
         return WrappersProto.internal_static_google_protobuf_BoolValue_descriptor;
      }

      @Override
      protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
         return WrappersProto.internal_static_google_protobuf_BoolValue_fieldAccessorTable
            .ensureFieldAccessorsInitialized(BoolValue.class, BoolValue.Builder.class);
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

      public BoolValue.Builder clear() {
         super.clear();
         this.value_ = false;
         return this;
      }

      @Override
      public Descriptors.Descriptor getDescriptorForType() {
         return WrappersProto.internal_static_google_protobuf_BoolValue_descriptor;
      }

      public BoolValue getDefaultInstanceForType() {
         return BoolValue.getDefaultInstance();
      }

      public BoolValue build() {
         BoolValue result = this.buildPartial();
         if (!result.isInitialized()) {
            throw newUninitializedMessageException(result);
         } else {
            return result;
         }
      }

      public BoolValue buildPartial() {
         BoolValue result = new BoolValue(this);
         result.value_ = this.value_;
         this.onBuilt();
         return result;
      }

      public BoolValue.Builder clone() {
         return (BoolValue.Builder)super.clone();
      }

      public BoolValue.Builder setField(Descriptors.FieldDescriptor field, Object value) {
         return (BoolValue.Builder)super.setField(field, value);
      }

      public BoolValue.Builder clearField(Descriptors.FieldDescriptor field) {
         return (BoolValue.Builder)super.clearField(field);
      }

      public BoolValue.Builder clearOneof(Descriptors.OneofDescriptor oneof) {
         return (BoolValue.Builder)super.clearOneof(oneof);
      }

      public BoolValue.Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
         return (BoolValue.Builder)super.setRepeatedField(field, index, value);
      }

      public BoolValue.Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
         return (BoolValue.Builder)super.addRepeatedField(field, value);
      }

      public BoolValue.Builder mergeFrom(Message other) {
         if (other instanceof BoolValue) {
            return this.mergeFrom((BoolValue)other);
         } else {
            super.mergeFrom(other);
            return this;
         }
      }

      public BoolValue.Builder mergeFrom(BoolValue other) {
         if (other == BoolValue.getDefaultInstance()) {
            return this;
         } else {
            if (other.getValue()) {
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

      public BoolValue.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         BoolValue parsedMessage = null;

         try {
            parsedMessage = BoolValue.PARSER.parsePartialFrom(input, extensionRegistry);
         } catch (InvalidProtocolBufferException var8) {
            parsedMessage = (BoolValue)var8.getUnfinishedMessage();
            throw var8.unwrapIOException();
         } finally {
            if (parsedMessage != null) {
               this.mergeFrom(parsedMessage);
            }

         }

         return this;
      }

      @Override
      public boolean getValue() {
         return this.value_;
      }

      public BoolValue.Builder setValue(boolean value) {
         this.value_ = value;
         this.onChanged();
         return this;
      }

      public BoolValue.Builder clearValue() {
         this.value_ = false;
         this.onChanged();
         return this;
      }

      public final BoolValue.Builder setUnknownFields(UnknownFieldSet unknownFields) {
         return (BoolValue.Builder)super.setUnknownFields(unknownFields);
      }

      public final BoolValue.Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
         return (BoolValue.Builder)super.mergeUnknownFields(unknownFields);
      }
   }
}
