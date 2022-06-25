package com.google.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class FloatValue extends GeneratedMessageV3 implements FloatValueOrBuilder {
   private static final long serialVersionUID = 0L;
   public static final int VALUE_FIELD_NUMBER = 1;
   private float value_;
   private byte memoizedIsInitialized = -1;
   private static final FloatValue DEFAULT_INSTANCE = new FloatValue();
   private static final Parser<FloatValue> PARSER = new AbstractParser<FloatValue>() {
      public FloatValue parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return new FloatValue(input, extensionRegistry);
      }
   };

   private FloatValue(GeneratedMessageV3.Builder<?> builder) {
      super(builder);
   }

   private FloatValue() {
   }

   @Override
   protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
      return new FloatValue();
   }

   @Override
   public final UnknownFieldSet getUnknownFields() {
      return this.unknownFields;
   }

   private FloatValue(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                  case 13:
                     this.value_ = input.readFloat();
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
      return WrappersProto.internal_static_google_protobuf_FloatValue_descriptor;
   }

   @Override
   protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
      return WrappersProto.internal_static_google_protobuf_FloatValue_fieldAccessorTable
         .ensureFieldAccessorsInitialized(FloatValue.class, FloatValue.Builder.class);
   }

   @Override
   public float getValue() {
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
      if (Float.floatToRawIntBits(this.value_) != 0) {
         output.writeFloat(1, this.value_);
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
         if (Float.floatToRawIntBits(this.value_) != 0) {
            size += CodedOutputStream.computeFloatSize(1, this.value_);
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
      } else if (!(obj instanceof FloatValue)) {
         return super.equals(obj);
      } else {
         FloatValue other = (FloatValue)obj;
         if (Float.floatToIntBits(this.getValue()) != Float.floatToIntBits(other.getValue())) {
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
         hash = 53 * hash + Float.floatToIntBits(this.getValue());
         hash = 29 * hash + this.unknownFields.hashCode();
         this.memoizedHashCode = hash;
         return hash;
      }
   }

   public static FloatValue parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static FloatValue parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static FloatValue parseFrom(ByteString data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static FloatValue parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static FloatValue parseFrom(byte[] data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static FloatValue parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static FloatValue parseFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static FloatValue parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public static FloatValue parseDelimitedFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
   }

   public static FloatValue parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
   }

   public static FloatValue parseFrom(CodedInputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static FloatValue parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public FloatValue.Builder newBuilderForType() {
      return newBuilder();
   }

   public static FloatValue.Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
   }

   public static FloatValue.Builder newBuilder(FloatValue prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
   }

   public FloatValue.Builder toBuilder() {
      return this == DEFAULT_INSTANCE ? new FloatValue.Builder() : new FloatValue.Builder().mergeFrom(this);
   }

   protected FloatValue.Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
      return new FloatValue.Builder(parent);
   }

   public static FloatValue getDefaultInstance() {
      return DEFAULT_INSTANCE;
   }

   public static FloatValue of(float value) {
      return newBuilder().setValue(value).build();
   }

   public static Parser<FloatValue> parser() {
      return PARSER;
   }

   @Override
   public Parser<FloatValue> getParserForType() {
      return PARSER;
   }

   public FloatValue getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
   }

   public static final class Builder extends GeneratedMessageV3.Builder<FloatValue.Builder> implements FloatValueOrBuilder {
      private float value_;

      public static final Descriptors.Descriptor getDescriptor() {
         return WrappersProto.internal_static_google_protobuf_FloatValue_descriptor;
      }

      @Override
      protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
         return WrappersProto.internal_static_google_protobuf_FloatValue_fieldAccessorTable
            .ensureFieldAccessorsInitialized(FloatValue.class, FloatValue.Builder.class);
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

      public FloatValue.Builder clear() {
         super.clear();
         this.value_ = 0.0F;
         return this;
      }

      @Override
      public Descriptors.Descriptor getDescriptorForType() {
         return WrappersProto.internal_static_google_protobuf_FloatValue_descriptor;
      }

      public FloatValue getDefaultInstanceForType() {
         return FloatValue.getDefaultInstance();
      }

      public FloatValue build() {
         FloatValue result = this.buildPartial();
         if (!result.isInitialized()) {
            throw newUninitializedMessageException(result);
         } else {
            return result;
         }
      }

      public FloatValue buildPartial() {
         FloatValue result = new FloatValue(this);
         result.value_ = this.value_;
         this.onBuilt();
         return result;
      }

      public FloatValue.Builder clone() {
         return (FloatValue.Builder)super.clone();
      }

      public FloatValue.Builder setField(Descriptors.FieldDescriptor field, Object value) {
         return (FloatValue.Builder)super.setField(field, value);
      }

      public FloatValue.Builder clearField(Descriptors.FieldDescriptor field) {
         return (FloatValue.Builder)super.clearField(field);
      }

      public FloatValue.Builder clearOneof(Descriptors.OneofDescriptor oneof) {
         return (FloatValue.Builder)super.clearOneof(oneof);
      }

      public FloatValue.Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
         return (FloatValue.Builder)super.setRepeatedField(field, index, value);
      }

      public FloatValue.Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
         return (FloatValue.Builder)super.addRepeatedField(field, value);
      }

      public FloatValue.Builder mergeFrom(Message other) {
         if (other instanceof FloatValue) {
            return this.mergeFrom((FloatValue)other);
         } else {
            super.mergeFrom(other);
            return this;
         }
      }

      public FloatValue.Builder mergeFrom(FloatValue other) {
         if (other == FloatValue.getDefaultInstance()) {
            return this;
         } else {
            if (other.getValue() != 0.0F) {
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

      public FloatValue.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         FloatValue parsedMessage = null;

         try {
            parsedMessage = FloatValue.PARSER.parsePartialFrom(input, extensionRegistry);
         } catch (InvalidProtocolBufferException var8) {
            parsedMessage = (FloatValue)var8.getUnfinishedMessage();
            throw var8.unwrapIOException();
         } finally {
            if (parsedMessage != null) {
               this.mergeFrom(parsedMessage);
            }

         }

         return this;
      }

      @Override
      public float getValue() {
         return this.value_;
      }

      public FloatValue.Builder setValue(float value) {
         this.value_ = value;
         this.onChanged();
         return this;
      }

      public FloatValue.Builder clearValue() {
         this.value_ = 0.0F;
         this.onChanged();
         return this;
      }

      public final FloatValue.Builder setUnknownFields(UnknownFieldSet unknownFields) {
         return (FloatValue.Builder)super.setUnknownFields(unknownFields);
      }

      public final FloatValue.Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
         return (FloatValue.Builder)super.mergeUnknownFields(unknownFields);
      }
   }
}
