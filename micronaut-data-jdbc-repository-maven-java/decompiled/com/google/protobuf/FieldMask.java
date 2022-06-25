package com.google.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public final class FieldMask extends GeneratedMessageV3 implements FieldMaskOrBuilder {
   private static final long serialVersionUID = 0L;
   public static final int PATHS_FIELD_NUMBER = 1;
   private LazyStringList paths_;
   private byte memoizedIsInitialized = -1;
   private static final FieldMask DEFAULT_INSTANCE = new FieldMask();
   private static final Parser<FieldMask> PARSER = new AbstractParser<FieldMask>() {
      public FieldMask parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return new FieldMask(input, extensionRegistry);
      }
   };

   private FieldMask(GeneratedMessageV3.Builder<?> builder) {
      super(builder);
   }

   private FieldMask() {
      this.paths_ = LazyStringArrayList.EMPTY;
   }

   @Override
   protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
      return new FieldMask();
   }

   @Override
   public final UnknownFieldSet getUnknownFields() {
      return this.unknownFields;
   }

   private FieldMask(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
         throw new NullPointerException();
      } else {
         int mutable_bitField0_ = 0;
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
                     if ((mutable_bitField0_ & 1) == 0) {
                        this.paths_ = new LazyStringArrayList();
                        mutable_bitField0_ |= 1;
                     }

                     this.paths_.add(s);
                     break;
                  default:
                     if (!this.parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                        done = true;
                     }
               }
            }
         } catch (InvalidProtocolBufferException var13) {
            throw var13.setUnfinishedMessage(this);
         } catch (UninitializedMessageException var14) {
            throw var14.asInvalidProtocolBufferException().setUnfinishedMessage(this);
         } catch (IOException var15) {
            throw new InvalidProtocolBufferException(var15).setUnfinishedMessage(this);
         } finally {
            if ((mutable_bitField0_ & 1) != 0) {
               this.paths_ = this.paths_.getUnmodifiableView();
            }

            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }

      }
   }

   public static final Descriptors.Descriptor getDescriptor() {
      return FieldMaskProto.internal_static_google_protobuf_FieldMask_descriptor;
   }

   @Override
   protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
      return FieldMaskProto.internal_static_google_protobuf_FieldMask_fieldAccessorTable
         .ensureFieldAccessorsInitialized(FieldMask.class, FieldMask.Builder.class);
   }

   public ProtocolStringList getPathsList() {
      return this.paths_;
   }

   @Override
   public int getPathsCount() {
      return this.paths_.size();
   }

   @Override
   public String getPaths(int index) {
      return (String)this.paths_.get(index);
   }

   @Override
   public ByteString getPathsBytes(int index) {
      return this.paths_.getByteString(index);
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
      for(int i = 0; i < this.paths_.size(); ++i) {
         GeneratedMessageV3.writeString(output, 1, this.paths_.getRaw(i));
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
         int dataSize = 0;

         for(int i = 0; i < this.paths_.size(); ++i) {
            dataSize += computeStringSizeNoTag(this.paths_.getRaw(i));
         }

         size += dataSize;
         size += 1 * this.getPathsList().size();
         size += this.unknownFields.getSerializedSize();
         this.memoizedSize = size;
         return size;
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof FieldMask)) {
         return super.equals(obj);
      } else {
         FieldMask other = (FieldMask)obj;
         if (!this.getPathsList().equals(other.getPathsList())) {
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
         if (this.getPathsCount() > 0) {
            hash = 37 * hash + 1;
            hash = 53 * hash + this.getPathsList().hashCode();
         }

         hash = 29 * hash + this.unknownFields.hashCode();
         this.memoizedHashCode = hash;
         return hash;
      }
   }

   public static FieldMask parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static FieldMask parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static FieldMask parseFrom(ByteString data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static FieldMask parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static FieldMask parseFrom(byte[] data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static FieldMask parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static FieldMask parseFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static FieldMask parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public static FieldMask parseDelimitedFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
   }

   public static FieldMask parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
   }

   public static FieldMask parseFrom(CodedInputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static FieldMask parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public FieldMask.Builder newBuilderForType() {
      return newBuilder();
   }

   public static FieldMask.Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
   }

   public static FieldMask.Builder newBuilder(FieldMask prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
   }

   public FieldMask.Builder toBuilder() {
      return this == DEFAULT_INSTANCE ? new FieldMask.Builder() : new FieldMask.Builder().mergeFrom(this);
   }

   protected FieldMask.Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
      return new FieldMask.Builder(parent);
   }

   public static FieldMask getDefaultInstance() {
      return DEFAULT_INSTANCE;
   }

   public static Parser<FieldMask> parser() {
      return PARSER;
   }

   @Override
   public Parser<FieldMask> getParserForType() {
      return PARSER;
   }

   public FieldMask getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
   }

   public static final class Builder extends GeneratedMessageV3.Builder<FieldMask.Builder> implements FieldMaskOrBuilder {
      private int bitField0_;
      private LazyStringList paths_ = LazyStringArrayList.EMPTY;

      public static final Descriptors.Descriptor getDescriptor() {
         return FieldMaskProto.internal_static_google_protobuf_FieldMask_descriptor;
      }

      @Override
      protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
         return FieldMaskProto.internal_static_google_protobuf_FieldMask_fieldAccessorTable
            .ensureFieldAccessorsInitialized(FieldMask.class, FieldMask.Builder.class);
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

      public FieldMask.Builder clear() {
         super.clear();
         this.paths_ = LazyStringArrayList.EMPTY;
         this.bitField0_ &= -2;
         return this;
      }

      @Override
      public Descriptors.Descriptor getDescriptorForType() {
         return FieldMaskProto.internal_static_google_protobuf_FieldMask_descriptor;
      }

      public FieldMask getDefaultInstanceForType() {
         return FieldMask.getDefaultInstance();
      }

      public FieldMask build() {
         FieldMask result = this.buildPartial();
         if (!result.isInitialized()) {
            throw newUninitializedMessageException(result);
         } else {
            return result;
         }
      }

      public FieldMask buildPartial() {
         FieldMask result = new FieldMask(this);
         int from_bitField0_ = this.bitField0_;
         if ((this.bitField0_ & 1) != 0) {
            this.paths_ = this.paths_.getUnmodifiableView();
            this.bitField0_ &= -2;
         }

         result.paths_ = this.paths_;
         this.onBuilt();
         return result;
      }

      public FieldMask.Builder clone() {
         return (FieldMask.Builder)super.clone();
      }

      public FieldMask.Builder setField(Descriptors.FieldDescriptor field, Object value) {
         return (FieldMask.Builder)super.setField(field, value);
      }

      public FieldMask.Builder clearField(Descriptors.FieldDescriptor field) {
         return (FieldMask.Builder)super.clearField(field);
      }

      public FieldMask.Builder clearOneof(Descriptors.OneofDescriptor oneof) {
         return (FieldMask.Builder)super.clearOneof(oneof);
      }

      public FieldMask.Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
         return (FieldMask.Builder)super.setRepeatedField(field, index, value);
      }

      public FieldMask.Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
         return (FieldMask.Builder)super.addRepeatedField(field, value);
      }

      public FieldMask.Builder mergeFrom(Message other) {
         if (other instanceof FieldMask) {
            return this.mergeFrom((FieldMask)other);
         } else {
            super.mergeFrom(other);
            return this;
         }
      }

      public FieldMask.Builder mergeFrom(FieldMask other) {
         if (other == FieldMask.getDefaultInstance()) {
            return this;
         } else {
            if (!other.paths_.isEmpty()) {
               if (this.paths_.isEmpty()) {
                  this.paths_ = other.paths_;
                  this.bitField0_ &= -2;
               } else {
                  this.ensurePathsIsMutable();
                  this.paths_.addAll(other.paths_);
               }

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

      public FieldMask.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         FieldMask parsedMessage = null;

         try {
            parsedMessage = FieldMask.PARSER.parsePartialFrom(input, extensionRegistry);
         } catch (InvalidProtocolBufferException var8) {
            parsedMessage = (FieldMask)var8.getUnfinishedMessage();
            throw var8.unwrapIOException();
         } finally {
            if (parsedMessage != null) {
               this.mergeFrom(parsedMessage);
            }

         }

         return this;
      }

      private void ensurePathsIsMutable() {
         if ((this.bitField0_ & 1) == 0) {
            this.paths_ = new LazyStringArrayList(this.paths_);
            this.bitField0_ |= 1;
         }

      }

      public ProtocolStringList getPathsList() {
         return this.paths_.getUnmodifiableView();
      }

      @Override
      public int getPathsCount() {
         return this.paths_.size();
      }

      @Override
      public String getPaths(int index) {
         return (String)this.paths_.get(index);
      }

      @Override
      public ByteString getPathsBytes(int index) {
         return this.paths_.getByteString(index);
      }

      public FieldMask.Builder setPaths(int index, String value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            this.ensurePathsIsMutable();
            this.paths_.set(index, value);
            this.onChanged();
            return this;
         }
      }

      public FieldMask.Builder addPaths(String value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            this.ensurePathsIsMutable();
            this.paths_.add(value);
            this.onChanged();
            return this;
         }
      }

      public FieldMask.Builder addAllPaths(Iterable<String> values) {
         this.ensurePathsIsMutable();
         AbstractMessageLite.Builder.addAll(values, this.paths_);
         this.onChanged();
         return this;
      }

      public FieldMask.Builder clearPaths() {
         this.paths_ = LazyStringArrayList.EMPTY;
         this.bitField0_ &= -2;
         this.onChanged();
         return this;
      }

      public FieldMask.Builder addPathsBytes(ByteString value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            AbstractMessageLite.checkByteStringIsUtf8(value);
            this.ensurePathsIsMutable();
            this.paths_.add(value);
            this.onChanged();
            return this;
         }
      }

      public final FieldMask.Builder setUnknownFields(UnknownFieldSet unknownFields) {
         return (FieldMask.Builder)super.setUnknownFields(unknownFields);
      }

      public final FieldMask.Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
         return (FieldMask.Builder)super.mergeUnknownFields(unknownFields);
      }
   }
}
