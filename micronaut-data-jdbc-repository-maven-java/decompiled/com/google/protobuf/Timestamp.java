package com.google.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class Timestamp extends GeneratedMessageV3 implements TimestampOrBuilder {
   private static final long serialVersionUID = 0L;
   public static final int SECONDS_FIELD_NUMBER = 1;
   private long seconds_;
   public static final int NANOS_FIELD_NUMBER = 2;
   private int nanos_;
   private byte memoizedIsInitialized = -1;
   private static final Timestamp DEFAULT_INSTANCE = new Timestamp();
   private static final Parser<Timestamp> PARSER = new AbstractParser<Timestamp>() {
      public Timestamp parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return new Timestamp(input, extensionRegistry);
      }
   };

   private Timestamp(GeneratedMessageV3.Builder<?> builder) {
      super(builder);
   }

   private Timestamp() {
   }

   @Override
   protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
      return new Timestamp();
   }

   @Override
   public final UnknownFieldSet getUnknownFields() {
      return this.unknownFields;
   }

   private Timestamp(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     this.seconds_ = input.readInt64();
                     break;
                  case 16:
                     this.nanos_ = input.readInt32();
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
      return TimestampProto.internal_static_google_protobuf_Timestamp_descriptor;
   }

   @Override
   protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
      return TimestampProto.internal_static_google_protobuf_Timestamp_fieldAccessorTable
         .ensureFieldAccessorsInitialized(Timestamp.class, Timestamp.Builder.class);
   }

   @Override
   public long getSeconds() {
      return this.seconds_;
   }

   @Override
   public int getNanos() {
      return this.nanos_;
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
      if (this.seconds_ != 0L) {
         output.writeInt64(1, this.seconds_);
      }

      if (this.nanos_ != 0) {
         output.writeInt32(2, this.nanos_);
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
         if (this.seconds_ != 0L) {
            size += CodedOutputStream.computeInt64Size(1, this.seconds_);
         }

         if (this.nanos_ != 0) {
            size += CodedOutputStream.computeInt32Size(2, this.nanos_);
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
      } else if (!(obj instanceof Timestamp)) {
         return super.equals(obj);
      } else {
         Timestamp other = (Timestamp)obj;
         if (this.getSeconds() != other.getSeconds()) {
            return false;
         } else if (this.getNanos() != other.getNanos()) {
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
         hash = 53 * hash + Internal.hashLong(this.getSeconds());
         hash = 37 * hash + 2;
         hash = 53 * hash + this.getNanos();
         hash = 29 * hash + this.unknownFields.hashCode();
         this.memoizedHashCode = hash;
         return hash;
      }
   }

   public static Timestamp parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static Timestamp parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static Timestamp parseFrom(ByteString data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static Timestamp parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static Timestamp parseFrom(byte[] data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static Timestamp parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static Timestamp parseFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static Timestamp parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public static Timestamp parseDelimitedFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
   }

   public static Timestamp parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
   }

   public static Timestamp parseFrom(CodedInputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static Timestamp parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public Timestamp.Builder newBuilderForType() {
      return newBuilder();
   }

   public static Timestamp.Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
   }

   public static Timestamp.Builder newBuilder(Timestamp prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
   }

   public Timestamp.Builder toBuilder() {
      return this == DEFAULT_INSTANCE ? new Timestamp.Builder() : new Timestamp.Builder().mergeFrom(this);
   }

   protected Timestamp.Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
      return new Timestamp.Builder(parent);
   }

   public static Timestamp getDefaultInstance() {
      return DEFAULT_INSTANCE;
   }

   public static Parser<Timestamp> parser() {
      return PARSER;
   }

   @Override
   public Parser<Timestamp> getParserForType() {
      return PARSER;
   }

   public Timestamp getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
   }

   public static final class Builder extends GeneratedMessageV3.Builder<Timestamp.Builder> implements TimestampOrBuilder {
      private long seconds_;
      private int nanos_;

      public static final Descriptors.Descriptor getDescriptor() {
         return TimestampProto.internal_static_google_protobuf_Timestamp_descriptor;
      }

      @Override
      protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
         return TimestampProto.internal_static_google_protobuf_Timestamp_fieldAccessorTable
            .ensureFieldAccessorsInitialized(Timestamp.class, Timestamp.Builder.class);
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

      public Timestamp.Builder clear() {
         super.clear();
         this.seconds_ = 0L;
         this.nanos_ = 0;
         return this;
      }

      @Override
      public Descriptors.Descriptor getDescriptorForType() {
         return TimestampProto.internal_static_google_protobuf_Timestamp_descriptor;
      }

      public Timestamp getDefaultInstanceForType() {
         return Timestamp.getDefaultInstance();
      }

      public Timestamp build() {
         Timestamp result = this.buildPartial();
         if (!result.isInitialized()) {
            throw newUninitializedMessageException(result);
         } else {
            return result;
         }
      }

      public Timestamp buildPartial() {
         Timestamp result = new Timestamp(this);
         result.seconds_ = this.seconds_;
         result.nanos_ = this.nanos_;
         this.onBuilt();
         return result;
      }

      public Timestamp.Builder clone() {
         return (Timestamp.Builder)super.clone();
      }

      public Timestamp.Builder setField(Descriptors.FieldDescriptor field, Object value) {
         return (Timestamp.Builder)super.setField(field, value);
      }

      public Timestamp.Builder clearField(Descriptors.FieldDescriptor field) {
         return (Timestamp.Builder)super.clearField(field);
      }

      public Timestamp.Builder clearOneof(Descriptors.OneofDescriptor oneof) {
         return (Timestamp.Builder)super.clearOneof(oneof);
      }

      public Timestamp.Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
         return (Timestamp.Builder)super.setRepeatedField(field, index, value);
      }

      public Timestamp.Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
         return (Timestamp.Builder)super.addRepeatedField(field, value);
      }

      public Timestamp.Builder mergeFrom(Message other) {
         if (other instanceof Timestamp) {
            return this.mergeFrom((Timestamp)other);
         } else {
            super.mergeFrom(other);
            return this;
         }
      }

      public Timestamp.Builder mergeFrom(Timestamp other) {
         if (other == Timestamp.getDefaultInstance()) {
            return this;
         } else {
            if (other.getSeconds() != 0L) {
               this.setSeconds(other.getSeconds());
            }

            if (other.getNanos() != 0) {
               this.setNanos(other.getNanos());
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

      public Timestamp.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         Timestamp parsedMessage = null;

         try {
            parsedMessage = Timestamp.PARSER.parsePartialFrom(input, extensionRegistry);
         } catch (InvalidProtocolBufferException var8) {
            parsedMessage = (Timestamp)var8.getUnfinishedMessage();
            throw var8.unwrapIOException();
         } finally {
            if (parsedMessage != null) {
               this.mergeFrom(parsedMessage);
            }

         }

         return this;
      }

      @Override
      public long getSeconds() {
         return this.seconds_;
      }

      public Timestamp.Builder setSeconds(long value) {
         this.seconds_ = value;
         this.onChanged();
         return this;
      }

      public Timestamp.Builder clearSeconds() {
         this.seconds_ = 0L;
         this.onChanged();
         return this;
      }

      @Override
      public int getNanos() {
         return this.nanos_;
      }

      public Timestamp.Builder setNanos(int value) {
         this.nanos_ = value;
         this.onChanged();
         return this;
      }

      public Timestamp.Builder clearNanos() {
         this.nanos_ = 0;
         this.onChanged();
         return this;
      }

      public final Timestamp.Builder setUnknownFields(UnknownFieldSet unknownFields) {
         return (Timestamp.Builder)super.setUnknownFields(unknownFields);
      }

      public final Timestamp.Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
         return (Timestamp.Builder)super.mergeUnknownFields(unknownFields);
      }
   }
}
