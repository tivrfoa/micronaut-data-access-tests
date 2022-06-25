package com.google.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class SourceContext extends GeneratedMessageV3 implements SourceContextOrBuilder {
   private static final long serialVersionUID = 0L;
   public static final int FILE_NAME_FIELD_NUMBER = 1;
   private volatile Object fileName_;
   private byte memoizedIsInitialized = -1;
   private static final SourceContext DEFAULT_INSTANCE = new SourceContext();
   private static final Parser<SourceContext> PARSER = new AbstractParser<SourceContext>() {
      public SourceContext parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return new SourceContext(input, extensionRegistry);
      }
   };

   private SourceContext(GeneratedMessageV3.Builder<?> builder) {
      super(builder);
   }

   private SourceContext() {
      this.fileName_ = "";
   }

   @Override
   protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
      return new SourceContext();
   }

   @Override
   public final UnknownFieldSet getUnknownFields() {
      return this.unknownFields;
   }

   private SourceContext(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     this.fileName_ = s;
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
      return SourceContextProto.internal_static_google_protobuf_SourceContext_descriptor;
   }

   @Override
   protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
      return SourceContextProto.internal_static_google_protobuf_SourceContext_fieldAccessorTable
         .ensureFieldAccessorsInitialized(SourceContext.class, SourceContext.Builder.class);
   }

   @Override
   public String getFileName() {
      Object ref = this.fileName_;
      if (ref instanceof String) {
         return (String)ref;
      } else {
         ByteString bs = (ByteString)ref;
         String s = bs.toStringUtf8();
         this.fileName_ = s;
         return s;
      }
   }

   @Override
   public ByteString getFileNameBytes() {
      Object ref = this.fileName_;
      if (ref instanceof String) {
         ByteString b = ByteString.copyFromUtf8((String)ref);
         this.fileName_ = b;
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
      if (!GeneratedMessageV3.isStringEmpty(this.fileName_)) {
         GeneratedMessageV3.writeString(output, 1, this.fileName_);
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
         if (!GeneratedMessageV3.isStringEmpty(this.fileName_)) {
            size += GeneratedMessageV3.computeStringSize(1, this.fileName_);
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
      } else if (!(obj instanceof SourceContext)) {
         return super.equals(obj);
      } else {
         SourceContext other = (SourceContext)obj;
         if (!this.getFileName().equals(other.getFileName())) {
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
         hash = 53 * hash + this.getFileName().hashCode();
         hash = 29 * hash + this.unknownFields.hashCode();
         this.memoizedHashCode = hash;
         return hash;
      }
   }

   public static SourceContext parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static SourceContext parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static SourceContext parseFrom(ByteString data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static SourceContext parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static SourceContext parseFrom(byte[] data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static SourceContext parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static SourceContext parseFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static SourceContext parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public static SourceContext parseDelimitedFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
   }

   public static SourceContext parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
   }

   public static SourceContext parseFrom(CodedInputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static SourceContext parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public SourceContext.Builder newBuilderForType() {
      return newBuilder();
   }

   public static SourceContext.Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
   }

   public static SourceContext.Builder newBuilder(SourceContext prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
   }

   public SourceContext.Builder toBuilder() {
      return this == DEFAULT_INSTANCE ? new SourceContext.Builder() : new SourceContext.Builder().mergeFrom(this);
   }

   protected SourceContext.Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
      return new SourceContext.Builder(parent);
   }

   public static SourceContext getDefaultInstance() {
      return DEFAULT_INSTANCE;
   }

   public static Parser<SourceContext> parser() {
      return PARSER;
   }

   @Override
   public Parser<SourceContext> getParserForType() {
      return PARSER;
   }

   public SourceContext getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
   }

   public static final class Builder extends GeneratedMessageV3.Builder<SourceContext.Builder> implements SourceContextOrBuilder {
      private Object fileName_ = "";

      public static final Descriptors.Descriptor getDescriptor() {
         return SourceContextProto.internal_static_google_protobuf_SourceContext_descriptor;
      }

      @Override
      protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
         return SourceContextProto.internal_static_google_protobuf_SourceContext_fieldAccessorTable
            .ensureFieldAccessorsInitialized(SourceContext.class, SourceContext.Builder.class);
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

      public SourceContext.Builder clear() {
         super.clear();
         this.fileName_ = "";
         return this;
      }

      @Override
      public Descriptors.Descriptor getDescriptorForType() {
         return SourceContextProto.internal_static_google_protobuf_SourceContext_descriptor;
      }

      public SourceContext getDefaultInstanceForType() {
         return SourceContext.getDefaultInstance();
      }

      public SourceContext build() {
         SourceContext result = this.buildPartial();
         if (!result.isInitialized()) {
            throw newUninitializedMessageException(result);
         } else {
            return result;
         }
      }

      public SourceContext buildPartial() {
         SourceContext result = new SourceContext(this);
         result.fileName_ = this.fileName_;
         this.onBuilt();
         return result;
      }

      public SourceContext.Builder clone() {
         return (SourceContext.Builder)super.clone();
      }

      public SourceContext.Builder setField(Descriptors.FieldDescriptor field, Object value) {
         return (SourceContext.Builder)super.setField(field, value);
      }

      public SourceContext.Builder clearField(Descriptors.FieldDescriptor field) {
         return (SourceContext.Builder)super.clearField(field);
      }

      public SourceContext.Builder clearOneof(Descriptors.OneofDescriptor oneof) {
         return (SourceContext.Builder)super.clearOneof(oneof);
      }

      public SourceContext.Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
         return (SourceContext.Builder)super.setRepeatedField(field, index, value);
      }

      public SourceContext.Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
         return (SourceContext.Builder)super.addRepeatedField(field, value);
      }

      public SourceContext.Builder mergeFrom(Message other) {
         if (other instanceof SourceContext) {
            return this.mergeFrom((SourceContext)other);
         } else {
            super.mergeFrom(other);
            return this;
         }
      }

      public SourceContext.Builder mergeFrom(SourceContext other) {
         if (other == SourceContext.getDefaultInstance()) {
            return this;
         } else {
            if (!other.getFileName().isEmpty()) {
               this.fileName_ = other.fileName_;
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

      public SourceContext.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         SourceContext parsedMessage = null;

         try {
            parsedMessage = SourceContext.PARSER.parsePartialFrom(input, extensionRegistry);
         } catch (InvalidProtocolBufferException var8) {
            parsedMessage = (SourceContext)var8.getUnfinishedMessage();
            throw var8.unwrapIOException();
         } finally {
            if (parsedMessage != null) {
               this.mergeFrom(parsedMessage);
            }

         }

         return this;
      }

      @Override
      public String getFileName() {
         Object ref = this.fileName_;
         if (!(ref instanceof String)) {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            this.fileName_ = s;
            return s;
         } else {
            return (String)ref;
         }
      }

      @Override
      public ByteString getFileNameBytes() {
         Object ref = this.fileName_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.fileName_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      public SourceContext.Builder setFileName(String value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            this.fileName_ = value;
            this.onChanged();
            return this;
         }
      }

      public SourceContext.Builder clearFileName() {
         this.fileName_ = SourceContext.getDefaultInstance().getFileName();
         this.onChanged();
         return this;
      }

      public SourceContext.Builder setFileNameBytes(ByteString value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            AbstractMessageLite.checkByteStringIsUtf8(value);
            this.fileName_ = value;
            this.onChanged();
            return this;
         }
      }

      public final SourceContext.Builder setUnknownFields(UnknownFieldSet unknownFields) {
         return (SourceContext.Builder)super.setUnknownFields(unknownFields);
      }

      public final SourceContext.Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
         return (SourceContext.Builder)super.mergeUnknownFields(unknownFields);
      }
   }
}
