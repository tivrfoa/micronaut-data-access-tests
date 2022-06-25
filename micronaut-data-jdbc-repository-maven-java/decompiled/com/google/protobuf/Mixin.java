package com.google.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class Mixin extends GeneratedMessageV3 implements MixinOrBuilder {
   private static final long serialVersionUID = 0L;
   public static final int NAME_FIELD_NUMBER = 1;
   private volatile Object name_;
   public static final int ROOT_FIELD_NUMBER = 2;
   private volatile Object root_;
   private byte memoizedIsInitialized = -1;
   private static final Mixin DEFAULT_INSTANCE = new Mixin();
   private static final Parser<Mixin> PARSER = new AbstractParser<Mixin>() {
      public Mixin parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return new Mixin(input, extensionRegistry);
      }
   };

   private Mixin(GeneratedMessageV3.Builder<?> builder) {
      super(builder);
   }

   private Mixin() {
      this.name_ = "";
      this.root_ = "";
   }

   @Override
   protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
      return new Mixin();
   }

   @Override
   public final UnknownFieldSet getUnknownFields() {
      return this.unknownFields;
   }

   private Mixin(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                  case 10: {
                     String s = input.readStringRequireUtf8();
                     this.name_ = s;
                     break;
                  }
                  case 18: {
                     String s = input.readStringRequireUtf8();
                     this.root_ = s;
                     break;
                  }
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
      return ApiProto.internal_static_google_protobuf_Mixin_descriptor;
   }

   @Override
   protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
      return ApiProto.internal_static_google_protobuf_Mixin_fieldAccessorTable.ensureFieldAccessorsInitialized(Mixin.class, Mixin.Builder.class);
   }

   @Override
   public String getName() {
      Object ref = this.name_;
      if (ref instanceof String) {
         return (String)ref;
      } else {
         ByteString bs = (ByteString)ref;
         String s = bs.toStringUtf8();
         this.name_ = s;
         return s;
      }
   }

   @Override
   public ByteString getNameBytes() {
      Object ref = this.name_;
      if (ref instanceof String) {
         ByteString b = ByteString.copyFromUtf8((String)ref);
         this.name_ = b;
         return b;
      } else {
         return (ByteString)ref;
      }
   }

   @Override
   public String getRoot() {
      Object ref = this.root_;
      if (ref instanceof String) {
         return (String)ref;
      } else {
         ByteString bs = (ByteString)ref;
         String s = bs.toStringUtf8();
         this.root_ = s;
         return s;
      }
   }

   @Override
   public ByteString getRootBytes() {
      Object ref = this.root_;
      if (ref instanceof String) {
         ByteString b = ByteString.copyFromUtf8((String)ref);
         this.root_ = b;
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
      if (!GeneratedMessageV3.isStringEmpty(this.name_)) {
         GeneratedMessageV3.writeString(output, 1, this.name_);
      }

      if (!GeneratedMessageV3.isStringEmpty(this.root_)) {
         GeneratedMessageV3.writeString(output, 2, this.root_);
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
         if (!GeneratedMessageV3.isStringEmpty(this.name_)) {
            size += GeneratedMessageV3.computeStringSize(1, this.name_);
         }

         if (!GeneratedMessageV3.isStringEmpty(this.root_)) {
            size += GeneratedMessageV3.computeStringSize(2, this.root_);
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
      } else if (!(obj instanceof Mixin)) {
         return super.equals(obj);
      } else {
         Mixin other = (Mixin)obj;
         if (!this.getName().equals(other.getName())) {
            return false;
         } else if (!this.getRoot().equals(other.getRoot())) {
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
         hash = 53 * hash + this.getName().hashCode();
         hash = 37 * hash + 2;
         hash = 53 * hash + this.getRoot().hashCode();
         hash = 29 * hash + this.unknownFields.hashCode();
         this.memoizedHashCode = hash;
         return hash;
      }
   }

   public static Mixin parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static Mixin parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static Mixin parseFrom(ByteString data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static Mixin parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static Mixin parseFrom(byte[] data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static Mixin parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static Mixin parseFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static Mixin parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public static Mixin parseDelimitedFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
   }

   public static Mixin parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
   }

   public static Mixin parseFrom(CodedInputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static Mixin parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public Mixin.Builder newBuilderForType() {
      return newBuilder();
   }

   public static Mixin.Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
   }

   public static Mixin.Builder newBuilder(Mixin prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
   }

   public Mixin.Builder toBuilder() {
      return this == DEFAULT_INSTANCE ? new Mixin.Builder() : new Mixin.Builder().mergeFrom(this);
   }

   protected Mixin.Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
      return new Mixin.Builder(parent);
   }

   public static Mixin getDefaultInstance() {
      return DEFAULT_INSTANCE;
   }

   public static Parser<Mixin> parser() {
      return PARSER;
   }

   @Override
   public Parser<Mixin> getParserForType() {
      return PARSER;
   }

   public Mixin getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
   }

   public static final class Builder extends GeneratedMessageV3.Builder<Mixin.Builder> implements MixinOrBuilder {
      private Object name_ = "";
      private Object root_ = "";

      public static final Descriptors.Descriptor getDescriptor() {
         return ApiProto.internal_static_google_protobuf_Mixin_descriptor;
      }

      @Override
      protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
         return ApiProto.internal_static_google_protobuf_Mixin_fieldAccessorTable.ensureFieldAccessorsInitialized(Mixin.class, Mixin.Builder.class);
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

      public Mixin.Builder clear() {
         super.clear();
         this.name_ = "";
         this.root_ = "";
         return this;
      }

      @Override
      public Descriptors.Descriptor getDescriptorForType() {
         return ApiProto.internal_static_google_protobuf_Mixin_descriptor;
      }

      public Mixin getDefaultInstanceForType() {
         return Mixin.getDefaultInstance();
      }

      public Mixin build() {
         Mixin result = this.buildPartial();
         if (!result.isInitialized()) {
            throw newUninitializedMessageException(result);
         } else {
            return result;
         }
      }

      public Mixin buildPartial() {
         Mixin result = new Mixin(this);
         result.name_ = this.name_;
         result.root_ = this.root_;
         this.onBuilt();
         return result;
      }

      public Mixin.Builder clone() {
         return (Mixin.Builder)super.clone();
      }

      public Mixin.Builder setField(Descriptors.FieldDescriptor field, Object value) {
         return (Mixin.Builder)super.setField(field, value);
      }

      public Mixin.Builder clearField(Descriptors.FieldDescriptor field) {
         return (Mixin.Builder)super.clearField(field);
      }

      public Mixin.Builder clearOneof(Descriptors.OneofDescriptor oneof) {
         return (Mixin.Builder)super.clearOneof(oneof);
      }

      public Mixin.Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
         return (Mixin.Builder)super.setRepeatedField(field, index, value);
      }

      public Mixin.Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
         return (Mixin.Builder)super.addRepeatedField(field, value);
      }

      public Mixin.Builder mergeFrom(Message other) {
         if (other instanceof Mixin) {
            return this.mergeFrom((Mixin)other);
         } else {
            super.mergeFrom(other);
            return this;
         }
      }

      public Mixin.Builder mergeFrom(Mixin other) {
         if (other == Mixin.getDefaultInstance()) {
            return this;
         } else {
            if (!other.getName().isEmpty()) {
               this.name_ = other.name_;
               this.onChanged();
            }

            if (!other.getRoot().isEmpty()) {
               this.root_ = other.root_;
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

      public Mixin.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         Mixin parsedMessage = null;

         try {
            parsedMessage = Mixin.PARSER.parsePartialFrom(input, extensionRegistry);
         } catch (InvalidProtocolBufferException var8) {
            parsedMessage = (Mixin)var8.getUnfinishedMessage();
            throw var8.unwrapIOException();
         } finally {
            if (parsedMessage != null) {
               this.mergeFrom(parsedMessage);
            }

         }

         return this;
      }

      @Override
      public String getName() {
         Object ref = this.name_;
         if (!(ref instanceof String)) {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            this.name_ = s;
            return s;
         } else {
            return (String)ref;
         }
      }

      @Override
      public ByteString getNameBytes() {
         Object ref = this.name_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.name_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      public Mixin.Builder setName(String value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            this.name_ = value;
            this.onChanged();
            return this;
         }
      }

      public Mixin.Builder clearName() {
         this.name_ = Mixin.getDefaultInstance().getName();
         this.onChanged();
         return this;
      }

      public Mixin.Builder setNameBytes(ByteString value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            AbstractMessageLite.checkByteStringIsUtf8(value);
            this.name_ = value;
            this.onChanged();
            return this;
         }
      }

      @Override
      public String getRoot() {
         Object ref = this.root_;
         if (!(ref instanceof String)) {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            this.root_ = s;
            return s;
         } else {
            return (String)ref;
         }
      }

      @Override
      public ByteString getRootBytes() {
         Object ref = this.root_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.root_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      public Mixin.Builder setRoot(String value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            this.root_ = value;
            this.onChanged();
            return this;
         }
      }

      public Mixin.Builder clearRoot() {
         this.root_ = Mixin.getDefaultInstance().getRoot();
         this.onChanged();
         return this;
      }

      public Mixin.Builder setRootBytes(ByteString value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            AbstractMessageLite.checkByteStringIsUtf8(value);
            this.root_ = value;
            this.onChanged();
            return this;
         }
      }

      public final Mixin.Builder setUnknownFields(UnknownFieldSet unknownFields) {
         return (Mixin.Builder)super.setUnknownFields(unknownFields);
      }

      public final Mixin.Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
         return (Mixin.Builder)super.mergeUnknownFields(unknownFields);
      }
   }
}
