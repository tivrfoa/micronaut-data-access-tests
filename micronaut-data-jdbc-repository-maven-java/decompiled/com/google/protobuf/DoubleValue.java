package com.google.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class DoubleValue extends GeneratedMessageV3 implements DoubleValueOrBuilder {
   private static final long serialVersionUID = 0L;
   public static final int VALUE_FIELD_NUMBER = 1;
   private double value_;
   private byte memoizedIsInitialized = -1;
   private static final DoubleValue DEFAULT_INSTANCE = new DoubleValue();
   private static final Parser<DoubleValue> PARSER = new AbstractParser<DoubleValue>() {
      public DoubleValue parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return new DoubleValue(input, extensionRegistry);
      }
   };

   private DoubleValue(GeneratedMessageV3.Builder<?> builder) {
      super(builder);
   }

   private DoubleValue() {
   }

   @Override
   protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
      return new DoubleValue();
   }

   @Override
   public final UnknownFieldSet getUnknownFields() {
      return this.unknownFields;
   }

   private DoubleValue(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                  case 9:
                     this.value_ = input.readDouble();
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
      return WrappersProto.internal_static_google_protobuf_DoubleValue_descriptor;
   }

   @Override
   protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
      return WrappersProto.internal_static_google_protobuf_DoubleValue_fieldAccessorTable
         .ensureFieldAccessorsInitialized(DoubleValue.class, DoubleValue.Builder.class);
   }

   @Override
   public double getValue() {
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
      if (Double.doubleToRawLongBits(this.value_) != 0L) {
         output.writeDouble(1, this.value_);
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
         if (Double.doubleToRawLongBits(this.value_) != 0L) {
            size += CodedOutputStream.computeDoubleSize(1, this.value_);
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
      } else if (!(obj instanceof DoubleValue)) {
         return super.equals(obj);
      } else {
         DoubleValue other = (DoubleValue)obj;
         if (Double.doubleToLongBits(this.getValue()) != Double.doubleToLongBits(other.getValue())) {
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
         hash = 53 * hash + Internal.hashLong(Double.doubleToLongBits(this.getValue()));
         hash = 29 * hash + this.unknownFields.hashCode();
         this.memoizedHashCode = hash;
         return hash;
      }
   }

   public static DoubleValue parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static DoubleValue parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static DoubleValue parseFrom(ByteString data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static DoubleValue parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static DoubleValue parseFrom(byte[] data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static DoubleValue parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static DoubleValue parseFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static DoubleValue parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public static DoubleValue parseDelimitedFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
   }

   public static DoubleValue parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
   }

   public static DoubleValue parseFrom(CodedInputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static DoubleValue parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public DoubleValue.Builder newBuilderForType() {
      return newBuilder();
   }

   public static DoubleValue.Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
   }

   public static DoubleValue.Builder newBuilder(DoubleValue prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
   }

   public DoubleValue.Builder toBuilder() {
      return this == DEFAULT_INSTANCE ? new DoubleValue.Builder() : new DoubleValue.Builder().mergeFrom(this);
   }

   protected DoubleValue.Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
      return new DoubleValue.Builder(parent);
   }

   public static DoubleValue getDefaultInstance() {
      return DEFAULT_INSTANCE;
   }

   public static DoubleValue of(double value) {
      return newBuilder().setValue(value).build();
   }

   public static Parser<DoubleValue> parser() {
      return PARSER;
   }

   @Override
   public Parser<DoubleValue> getParserForType() {
      return PARSER;
   }

   public DoubleValue getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
   }

   public static final class Builder extends GeneratedMessageV3.Builder<DoubleValue.Builder> implements DoubleValueOrBuilder {
      private double value_;

      public static final Descriptors.Descriptor getDescriptor() {
         return WrappersProto.internal_static_google_protobuf_DoubleValue_descriptor;
      }

      @Override
      protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
         return WrappersProto.internal_static_google_protobuf_DoubleValue_fieldAccessorTable
            .ensureFieldAccessorsInitialized(DoubleValue.class, DoubleValue.Builder.class);
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

      public DoubleValue.Builder clear() {
         super.clear();
         this.value_ = 0.0;
         return this;
      }

      @Override
      public Descriptors.Descriptor getDescriptorForType() {
         return WrappersProto.internal_static_google_protobuf_DoubleValue_descriptor;
      }

      public DoubleValue getDefaultInstanceForType() {
         return DoubleValue.getDefaultInstance();
      }

      public DoubleValue build() {
         DoubleValue result = this.buildPartial();
         if (!result.isInitialized()) {
            throw newUninitializedMessageException(result);
         } else {
            return result;
         }
      }

      public DoubleValue buildPartial() {
         DoubleValue result = new DoubleValue(this);
         result.value_ = this.value_;
         this.onBuilt();
         return result;
      }

      public DoubleValue.Builder clone() {
         return (DoubleValue.Builder)super.clone();
      }

      public DoubleValue.Builder setField(Descriptors.FieldDescriptor field, Object value) {
         return (DoubleValue.Builder)super.setField(field, value);
      }

      public DoubleValue.Builder clearField(Descriptors.FieldDescriptor field) {
         return (DoubleValue.Builder)super.clearField(field);
      }

      public DoubleValue.Builder clearOneof(Descriptors.OneofDescriptor oneof) {
         return (DoubleValue.Builder)super.clearOneof(oneof);
      }

      public DoubleValue.Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
         return (DoubleValue.Builder)super.setRepeatedField(field, index, value);
      }

      public DoubleValue.Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
         return (DoubleValue.Builder)super.addRepeatedField(field, value);
      }

      public DoubleValue.Builder mergeFrom(Message other) {
         if (other instanceof DoubleValue) {
            return this.mergeFrom((DoubleValue)other);
         } else {
            super.mergeFrom(other);
            return this;
         }
      }

      public DoubleValue.Builder mergeFrom(DoubleValue other) {
         if (other == DoubleValue.getDefaultInstance()) {
            return this;
         } else {
            if (other.getValue() != 0.0) {
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

      public DoubleValue.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         DoubleValue parsedMessage = null;

         try {
            parsedMessage = DoubleValue.PARSER.parsePartialFrom(input, extensionRegistry);
         } catch (InvalidProtocolBufferException var8) {
            parsedMessage = (DoubleValue)var8.getUnfinishedMessage();
            throw var8.unwrapIOException();
         } finally {
            if (parsedMessage != null) {
               this.mergeFrom(parsedMessage);
            }

         }

         return this;
      }

      @Override
      public double getValue() {
         return this.value_;
      }

      public DoubleValue.Builder setValue(double value) {
         this.value_ = value;
         this.onChanged();
         return this;
      }

      public DoubleValue.Builder clearValue() {
         this.value_ = 0.0;
         this.onChanged();
         return this;
      }

      public final DoubleValue.Builder setUnknownFields(UnknownFieldSet unknownFields) {
         return (DoubleValue.Builder)super.setUnknownFields(unknownFields);
      }

      public final DoubleValue.Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
         return (DoubleValue.Builder)super.mergeUnknownFields(unknownFields);
      }
   }
}
