package com.google.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Api extends GeneratedMessageV3 implements ApiOrBuilder {
   private static final long serialVersionUID = 0L;
   public static final int NAME_FIELD_NUMBER = 1;
   private volatile Object name_;
   public static final int METHODS_FIELD_NUMBER = 2;
   private List<Method> methods_;
   public static final int OPTIONS_FIELD_NUMBER = 3;
   private List<Option> options_;
   public static final int VERSION_FIELD_NUMBER = 4;
   private volatile Object version_;
   public static final int SOURCE_CONTEXT_FIELD_NUMBER = 5;
   private SourceContext sourceContext_;
   public static final int MIXINS_FIELD_NUMBER = 6;
   private List<Mixin> mixins_;
   public static final int SYNTAX_FIELD_NUMBER = 7;
   private int syntax_;
   private byte memoizedIsInitialized = -1;
   private static final Api DEFAULT_INSTANCE = new Api();
   private static final Parser<Api> PARSER = new AbstractParser<Api>() {
      public Api parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return new Api(input, extensionRegistry);
      }
   };

   private Api(GeneratedMessageV3.Builder<?> builder) {
      super(builder);
   }

   private Api() {
      this.name_ = "";
      this.methods_ = Collections.emptyList();
      this.options_ = Collections.emptyList();
      this.version_ = "";
      this.mixins_ = Collections.emptyList();
      this.syntax_ = 0;
   }

   @Override
   protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
      return new Api();
   }

   @Override
   public final UnknownFieldSet getUnknownFields() {
      return this.unknownFields;
   }

   private Api(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                  case 10: {
                     String s = input.readStringRequireUtf8();
                     this.name_ = s;
                     break;
                  }
                  case 18:
                     if ((mutable_bitField0_ & 1) == 0) {
                        this.methods_ = new ArrayList();
                        mutable_bitField0_ |= 1;
                     }

                     this.methods_.add(input.readMessage(Method.parser(), extensionRegistry));
                     break;
                  case 26:
                     if ((mutable_bitField0_ & 2) == 0) {
                        this.options_ = new ArrayList();
                        mutable_bitField0_ |= 2;
                     }

                     this.options_.add(input.readMessage(Option.parser(), extensionRegistry));
                     break;
                  case 34: {
                     String s = input.readStringRequireUtf8();
                     this.version_ = s;
                     break;
                  }
                  case 42:
                     SourceContext.Builder subBuilder = null;
                     if (this.sourceContext_ != null) {
                        subBuilder = this.sourceContext_.toBuilder();
                     }

                     this.sourceContext_ = input.readMessage(SourceContext.parser(), extensionRegistry);
                     if (subBuilder != null) {
                        subBuilder.mergeFrom(this.sourceContext_);
                        this.sourceContext_ = subBuilder.buildPartial();
                     }
                     break;
                  case 50:
                     if ((mutable_bitField0_ & 4) == 0) {
                        this.mixins_ = new ArrayList();
                        mutable_bitField0_ |= 4;
                     }

                     this.mixins_.add(input.readMessage(Mixin.parser(), extensionRegistry));
                     break;
                  case 56:
                     int rawValue = input.readEnum();
                     this.syntax_ = rawValue;
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
               this.methods_ = Collections.unmodifiableList(this.methods_);
            }

            if ((mutable_bitField0_ & 2) != 0) {
               this.options_ = Collections.unmodifiableList(this.options_);
            }

            if ((mutable_bitField0_ & 4) != 0) {
               this.mixins_ = Collections.unmodifiableList(this.mixins_);
            }

            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }

      }
   }

   public static final Descriptors.Descriptor getDescriptor() {
      return ApiProto.internal_static_google_protobuf_Api_descriptor;
   }

   @Override
   protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
      return ApiProto.internal_static_google_protobuf_Api_fieldAccessorTable.ensureFieldAccessorsInitialized(Api.class, Api.Builder.class);
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
   public List<Method> getMethodsList() {
      return this.methods_;
   }

   @Override
   public List<? extends MethodOrBuilder> getMethodsOrBuilderList() {
      return this.methods_;
   }

   @Override
   public int getMethodsCount() {
      return this.methods_.size();
   }

   @Override
   public Method getMethods(int index) {
      return (Method)this.methods_.get(index);
   }

   @Override
   public MethodOrBuilder getMethodsOrBuilder(int index) {
      return (MethodOrBuilder)this.methods_.get(index);
   }

   @Override
   public List<Option> getOptionsList() {
      return this.options_;
   }

   @Override
   public List<? extends OptionOrBuilder> getOptionsOrBuilderList() {
      return this.options_;
   }

   @Override
   public int getOptionsCount() {
      return this.options_.size();
   }

   @Override
   public Option getOptions(int index) {
      return (Option)this.options_.get(index);
   }

   @Override
   public OptionOrBuilder getOptionsOrBuilder(int index) {
      return (OptionOrBuilder)this.options_.get(index);
   }

   @Override
   public String getVersion() {
      Object ref = this.version_;
      if (ref instanceof String) {
         return (String)ref;
      } else {
         ByteString bs = (ByteString)ref;
         String s = bs.toStringUtf8();
         this.version_ = s;
         return s;
      }
   }

   @Override
   public ByteString getVersionBytes() {
      Object ref = this.version_;
      if (ref instanceof String) {
         ByteString b = ByteString.copyFromUtf8((String)ref);
         this.version_ = b;
         return b;
      } else {
         return (ByteString)ref;
      }
   }

   @Override
   public boolean hasSourceContext() {
      return this.sourceContext_ != null;
   }

   @Override
   public SourceContext getSourceContext() {
      return this.sourceContext_ == null ? SourceContext.getDefaultInstance() : this.sourceContext_;
   }

   @Override
   public SourceContextOrBuilder getSourceContextOrBuilder() {
      return this.getSourceContext();
   }

   @Override
   public List<Mixin> getMixinsList() {
      return this.mixins_;
   }

   @Override
   public List<? extends MixinOrBuilder> getMixinsOrBuilderList() {
      return this.mixins_;
   }

   @Override
   public int getMixinsCount() {
      return this.mixins_.size();
   }

   @Override
   public Mixin getMixins(int index) {
      return (Mixin)this.mixins_.get(index);
   }

   @Override
   public MixinOrBuilder getMixinsOrBuilder(int index) {
      return (MixinOrBuilder)this.mixins_.get(index);
   }

   @Override
   public int getSyntaxValue() {
      return this.syntax_;
   }

   @Override
   public Syntax getSyntax() {
      Syntax result = Syntax.valueOf(this.syntax_);
      return result == null ? Syntax.UNRECOGNIZED : result;
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

      for(int i = 0; i < this.methods_.size(); ++i) {
         output.writeMessage(2, (MessageLite)this.methods_.get(i));
      }

      for(int i = 0; i < this.options_.size(); ++i) {
         output.writeMessage(3, (MessageLite)this.options_.get(i));
      }

      if (!GeneratedMessageV3.isStringEmpty(this.version_)) {
         GeneratedMessageV3.writeString(output, 4, this.version_);
      }

      if (this.sourceContext_ != null) {
         output.writeMessage(5, this.getSourceContext());
      }

      for(int i = 0; i < this.mixins_.size(); ++i) {
         output.writeMessage(6, (MessageLite)this.mixins_.get(i));
      }

      if (this.syntax_ != Syntax.SYNTAX_PROTO2.getNumber()) {
         output.writeEnum(7, this.syntax_);
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

         for(int i = 0; i < this.methods_.size(); ++i) {
            size += CodedOutputStream.computeMessageSize(2, (MessageLite)this.methods_.get(i));
         }

         for(int i = 0; i < this.options_.size(); ++i) {
            size += CodedOutputStream.computeMessageSize(3, (MessageLite)this.options_.get(i));
         }

         if (!GeneratedMessageV3.isStringEmpty(this.version_)) {
            size += GeneratedMessageV3.computeStringSize(4, this.version_);
         }

         if (this.sourceContext_ != null) {
            size += CodedOutputStream.computeMessageSize(5, this.getSourceContext());
         }

         for(int i = 0; i < this.mixins_.size(); ++i) {
            size += CodedOutputStream.computeMessageSize(6, (MessageLite)this.mixins_.get(i));
         }

         if (this.syntax_ != Syntax.SYNTAX_PROTO2.getNumber()) {
            size += CodedOutputStream.computeEnumSize(7, this.syntax_);
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
      } else if (!(obj instanceof Api)) {
         return super.equals(obj);
      } else {
         Api other = (Api)obj;
         if (!this.getName().equals(other.getName())) {
            return false;
         } else if (!this.getMethodsList().equals(other.getMethodsList())) {
            return false;
         } else if (!this.getOptionsList().equals(other.getOptionsList())) {
            return false;
         } else if (!this.getVersion().equals(other.getVersion())) {
            return false;
         } else if (this.hasSourceContext() != other.hasSourceContext()) {
            return false;
         } else if (this.hasSourceContext() && !this.getSourceContext().equals(other.getSourceContext())) {
            return false;
         } else if (!this.getMixinsList().equals(other.getMixinsList())) {
            return false;
         } else if (this.syntax_ != other.syntax_) {
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
         if (this.getMethodsCount() > 0) {
            hash = 37 * hash + 2;
            hash = 53 * hash + this.getMethodsList().hashCode();
         }

         if (this.getOptionsCount() > 0) {
            hash = 37 * hash + 3;
            hash = 53 * hash + this.getOptionsList().hashCode();
         }

         hash = 37 * hash + 4;
         hash = 53 * hash + this.getVersion().hashCode();
         if (this.hasSourceContext()) {
            hash = 37 * hash + 5;
            hash = 53 * hash + this.getSourceContext().hashCode();
         }

         if (this.getMixinsCount() > 0) {
            hash = 37 * hash + 6;
            hash = 53 * hash + this.getMixinsList().hashCode();
         }

         hash = 37 * hash + 7;
         hash = 53 * hash + this.syntax_;
         hash = 29 * hash + this.unknownFields.hashCode();
         this.memoizedHashCode = hash;
         return hash;
      }
   }

   public static Api parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static Api parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static Api parseFrom(ByteString data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static Api parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static Api parseFrom(byte[] data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static Api parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static Api parseFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static Api parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public static Api parseDelimitedFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
   }

   public static Api parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
   }

   public static Api parseFrom(CodedInputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static Api parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public Api.Builder newBuilderForType() {
      return newBuilder();
   }

   public static Api.Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
   }

   public static Api.Builder newBuilder(Api prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
   }

   public Api.Builder toBuilder() {
      return this == DEFAULT_INSTANCE ? new Api.Builder() : new Api.Builder().mergeFrom(this);
   }

   protected Api.Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
      return new Api.Builder(parent);
   }

   public static Api getDefaultInstance() {
      return DEFAULT_INSTANCE;
   }

   public static Parser<Api> parser() {
      return PARSER;
   }

   @Override
   public Parser<Api> getParserForType() {
      return PARSER;
   }

   public Api getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
   }

   public static final class Builder extends GeneratedMessageV3.Builder<Api.Builder> implements ApiOrBuilder {
      private int bitField0_;
      private Object name_ = "";
      private List<Method> methods_ = Collections.emptyList();
      private RepeatedFieldBuilderV3<Method, Method.Builder, MethodOrBuilder> methodsBuilder_;
      private List<Option> options_ = Collections.emptyList();
      private RepeatedFieldBuilderV3<Option, Option.Builder, OptionOrBuilder> optionsBuilder_;
      private Object version_ = "";
      private SourceContext sourceContext_;
      private SingleFieldBuilderV3<SourceContext, SourceContext.Builder, SourceContextOrBuilder> sourceContextBuilder_;
      private List<Mixin> mixins_ = Collections.emptyList();
      private RepeatedFieldBuilderV3<Mixin, Mixin.Builder, MixinOrBuilder> mixinsBuilder_;
      private int syntax_ = 0;

      public static final Descriptors.Descriptor getDescriptor() {
         return ApiProto.internal_static_google_protobuf_Api_descriptor;
      }

      @Override
      protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
         return ApiProto.internal_static_google_protobuf_Api_fieldAccessorTable.ensureFieldAccessorsInitialized(Api.class, Api.Builder.class);
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
            this.getMethodsFieldBuilder();
            this.getOptionsFieldBuilder();
            this.getMixinsFieldBuilder();
         }

      }

      public Api.Builder clear() {
         super.clear();
         this.name_ = "";
         if (this.methodsBuilder_ == null) {
            this.methods_ = Collections.emptyList();
            this.bitField0_ &= -2;
         } else {
            this.methodsBuilder_.clear();
         }

         if (this.optionsBuilder_ == null) {
            this.options_ = Collections.emptyList();
            this.bitField0_ &= -3;
         } else {
            this.optionsBuilder_.clear();
         }

         this.version_ = "";
         if (this.sourceContextBuilder_ == null) {
            this.sourceContext_ = null;
         } else {
            this.sourceContext_ = null;
            this.sourceContextBuilder_ = null;
         }

         if (this.mixinsBuilder_ == null) {
            this.mixins_ = Collections.emptyList();
            this.bitField0_ &= -5;
         } else {
            this.mixinsBuilder_.clear();
         }

         this.syntax_ = 0;
         return this;
      }

      @Override
      public Descriptors.Descriptor getDescriptorForType() {
         return ApiProto.internal_static_google_protobuf_Api_descriptor;
      }

      public Api getDefaultInstanceForType() {
         return Api.getDefaultInstance();
      }

      public Api build() {
         Api result = this.buildPartial();
         if (!result.isInitialized()) {
            throw newUninitializedMessageException(result);
         } else {
            return result;
         }
      }

      public Api buildPartial() {
         Api result = new Api(this);
         int from_bitField0_ = this.bitField0_;
         result.name_ = this.name_;
         if (this.methodsBuilder_ == null) {
            if ((this.bitField0_ & 1) != 0) {
               this.methods_ = Collections.unmodifiableList(this.methods_);
               this.bitField0_ &= -2;
            }

            result.methods_ = this.methods_;
         } else {
            result.methods_ = this.methodsBuilder_.build();
         }

         if (this.optionsBuilder_ == null) {
            if ((this.bitField0_ & 2) != 0) {
               this.options_ = Collections.unmodifiableList(this.options_);
               this.bitField0_ &= -3;
            }

            result.options_ = this.options_;
         } else {
            result.options_ = this.optionsBuilder_.build();
         }

         result.version_ = this.version_;
         if (this.sourceContextBuilder_ == null) {
            result.sourceContext_ = this.sourceContext_;
         } else {
            result.sourceContext_ = this.sourceContextBuilder_.build();
         }

         if (this.mixinsBuilder_ == null) {
            if ((this.bitField0_ & 4) != 0) {
               this.mixins_ = Collections.unmodifiableList(this.mixins_);
               this.bitField0_ &= -5;
            }

            result.mixins_ = this.mixins_;
         } else {
            result.mixins_ = this.mixinsBuilder_.build();
         }

         result.syntax_ = this.syntax_;
         this.onBuilt();
         return result;
      }

      public Api.Builder clone() {
         return (Api.Builder)super.clone();
      }

      public Api.Builder setField(Descriptors.FieldDescriptor field, Object value) {
         return (Api.Builder)super.setField(field, value);
      }

      public Api.Builder clearField(Descriptors.FieldDescriptor field) {
         return (Api.Builder)super.clearField(field);
      }

      public Api.Builder clearOneof(Descriptors.OneofDescriptor oneof) {
         return (Api.Builder)super.clearOneof(oneof);
      }

      public Api.Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
         return (Api.Builder)super.setRepeatedField(field, index, value);
      }

      public Api.Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
         return (Api.Builder)super.addRepeatedField(field, value);
      }

      public Api.Builder mergeFrom(Message other) {
         if (other instanceof Api) {
            return this.mergeFrom((Api)other);
         } else {
            super.mergeFrom(other);
            return this;
         }
      }

      public Api.Builder mergeFrom(Api other) {
         if (other == Api.getDefaultInstance()) {
            return this;
         } else {
            if (!other.getName().isEmpty()) {
               this.name_ = other.name_;
               this.onChanged();
            }

            if (this.methodsBuilder_ == null) {
               if (!other.methods_.isEmpty()) {
                  if (this.methods_.isEmpty()) {
                     this.methods_ = other.methods_;
                     this.bitField0_ &= -2;
                  } else {
                     this.ensureMethodsIsMutable();
                     this.methods_.addAll(other.methods_);
                  }

                  this.onChanged();
               }
            } else if (!other.methods_.isEmpty()) {
               if (this.methodsBuilder_.isEmpty()) {
                  this.methodsBuilder_.dispose();
                  this.methodsBuilder_ = null;
                  this.methods_ = other.methods_;
                  this.bitField0_ &= -2;
                  this.methodsBuilder_ = GeneratedMessageV3.alwaysUseFieldBuilders ? this.getMethodsFieldBuilder() : null;
               } else {
                  this.methodsBuilder_.addAllMessages(other.methods_);
               }
            }

            if (this.optionsBuilder_ == null) {
               if (!other.options_.isEmpty()) {
                  if (this.options_.isEmpty()) {
                     this.options_ = other.options_;
                     this.bitField0_ &= -3;
                  } else {
                     this.ensureOptionsIsMutable();
                     this.options_.addAll(other.options_);
                  }

                  this.onChanged();
               }
            } else if (!other.options_.isEmpty()) {
               if (this.optionsBuilder_.isEmpty()) {
                  this.optionsBuilder_.dispose();
                  this.optionsBuilder_ = null;
                  this.options_ = other.options_;
                  this.bitField0_ &= -3;
                  this.optionsBuilder_ = GeneratedMessageV3.alwaysUseFieldBuilders ? this.getOptionsFieldBuilder() : null;
               } else {
                  this.optionsBuilder_.addAllMessages(other.options_);
               }
            }

            if (!other.getVersion().isEmpty()) {
               this.version_ = other.version_;
               this.onChanged();
            }

            if (other.hasSourceContext()) {
               this.mergeSourceContext(other.getSourceContext());
            }

            if (this.mixinsBuilder_ == null) {
               if (!other.mixins_.isEmpty()) {
                  if (this.mixins_.isEmpty()) {
                     this.mixins_ = other.mixins_;
                     this.bitField0_ &= -5;
                  } else {
                     this.ensureMixinsIsMutable();
                     this.mixins_.addAll(other.mixins_);
                  }

                  this.onChanged();
               }
            } else if (!other.mixins_.isEmpty()) {
               if (this.mixinsBuilder_.isEmpty()) {
                  this.mixinsBuilder_.dispose();
                  this.mixinsBuilder_ = null;
                  this.mixins_ = other.mixins_;
                  this.bitField0_ &= -5;
                  this.mixinsBuilder_ = GeneratedMessageV3.alwaysUseFieldBuilders ? this.getMixinsFieldBuilder() : null;
               } else {
                  this.mixinsBuilder_.addAllMessages(other.mixins_);
               }
            }

            if (other.syntax_ != 0) {
               this.setSyntaxValue(other.getSyntaxValue());
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

      public Api.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         Api parsedMessage = null;

         try {
            parsedMessage = Api.PARSER.parsePartialFrom(input, extensionRegistry);
         } catch (InvalidProtocolBufferException var8) {
            parsedMessage = (Api)var8.getUnfinishedMessage();
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

      public Api.Builder setName(String value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            this.name_ = value;
            this.onChanged();
            return this;
         }
      }

      public Api.Builder clearName() {
         this.name_ = Api.getDefaultInstance().getName();
         this.onChanged();
         return this;
      }

      public Api.Builder setNameBytes(ByteString value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            AbstractMessageLite.checkByteStringIsUtf8(value);
            this.name_ = value;
            this.onChanged();
            return this;
         }
      }

      private void ensureMethodsIsMutable() {
         if ((this.bitField0_ & 1) == 0) {
            this.methods_ = new ArrayList(this.methods_);
            this.bitField0_ |= 1;
         }

      }

      @Override
      public List<Method> getMethodsList() {
         return this.methodsBuilder_ == null ? Collections.unmodifiableList(this.methods_) : this.methodsBuilder_.getMessageList();
      }

      @Override
      public int getMethodsCount() {
         return this.methodsBuilder_ == null ? this.methods_.size() : this.methodsBuilder_.getCount();
      }

      @Override
      public Method getMethods(int index) {
         return this.methodsBuilder_ == null ? (Method)this.methods_.get(index) : this.methodsBuilder_.getMessage(index);
      }

      public Api.Builder setMethods(int index, Method value) {
         if (this.methodsBuilder_ == null) {
            if (value == null) {
               throw new NullPointerException();
            }

            this.ensureMethodsIsMutable();
            this.methods_.set(index, value);
            this.onChanged();
         } else {
            this.methodsBuilder_.setMessage(index, value);
         }

         return this;
      }

      public Api.Builder setMethods(int index, Method.Builder builderForValue) {
         if (this.methodsBuilder_ == null) {
            this.ensureMethodsIsMutable();
            this.methods_.set(index, builderForValue.build());
            this.onChanged();
         } else {
            this.methodsBuilder_.setMessage(index, builderForValue.build());
         }

         return this;
      }

      public Api.Builder addMethods(Method value) {
         if (this.methodsBuilder_ == null) {
            if (value == null) {
               throw new NullPointerException();
            }

            this.ensureMethodsIsMutable();
            this.methods_.add(value);
            this.onChanged();
         } else {
            this.methodsBuilder_.addMessage(value);
         }

         return this;
      }

      public Api.Builder addMethods(int index, Method value) {
         if (this.methodsBuilder_ == null) {
            if (value == null) {
               throw new NullPointerException();
            }

            this.ensureMethodsIsMutable();
            this.methods_.add(index, value);
            this.onChanged();
         } else {
            this.methodsBuilder_.addMessage(index, value);
         }

         return this;
      }

      public Api.Builder addMethods(Method.Builder builderForValue) {
         if (this.methodsBuilder_ == null) {
            this.ensureMethodsIsMutable();
            this.methods_.add(builderForValue.build());
            this.onChanged();
         } else {
            this.methodsBuilder_.addMessage(builderForValue.build());
         }

         return this;
      }

      public Api.Builder addMethods(int index, Method.Builder builderForValue) {
         if (this.methodsBuilder_ == null) {
            this.ensureMethodsIsMutable();
            this.methods_.add(index, builderForValue.build());
            this.onChanged();
         } else {
            this.methodsBuilder_.addMessage(index, builderForValue.build());
         }

         return this;
      }

      public Api.Builder addAllMethods(Iterable<? extends Method> values) {
         if (this.methodsBuilder_ == null) {
            this.ensureMethodsIsMutable();
            AbstractMessageLite.Builder.addAll(values, this.methods_);
            this.onChanged();
         } else {
            this.methodsBuilder_.addAllMessages(values);
         }

         return this;
      }

      public Api.Builder clearMethods() {
         if (this.methodsBuilder_ == null) {
            this.methods_ = Collections.emptyList();
            this.bitField0_ &= -2;
            this.onChanged();
         } else {
            this.methodsBuilder_.clear();
         }

         return this;
      }

      public Api.Builder removeMethods(int index) {
         if (this.methodsBuilder_ == null) {
            this.ensureMethodsIsMutable();
            this.methods_.remove(index);
            this.onChanged();
         } else {
            this.methodsBuilder_.remove(index);
         }

         return this;
      }

      public Method.Builder getMethodsBuilder(int index) {
         return this.getMethodsFieldBuilder().getBuilder(index);
      }

      @Override
      public MethodOrBuilder getMethodsOrBuilder(int index) {
         return this.methodsBuilder_ == null ? (MethodOrBuilder)this.methods_.get(index) : this.methodsBuilder_.getMessageOrBuilder(index);
      }

      @Override
      public List<? extends MethodOrBuilder> getMethodsOrBuilderList() {
         return this.methodsBuilder_ != null ? this.methodsBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.methods_);
      }

      public Method.Builder addMethodsBuilder() {
         return this.getMethodsFieldBuilder().addBuilder(Method.getDefaultInstance());
      }

      public Method.Builder addMethodsBuilder(int index) {
         return this.getMethodsFieldBuilder().addBuilder(index, Method.getDefaultInstance());
      }

      public List<Method.Builder> getMethodsBuilderList() {
         return this.getMethodsFieldBuilder().getBuilderList();
      }

      private RepeatedFieldBuilderV3<Method, Method.Builder, MethodOrBuilder> getMethodsFieldBuilder() {
         if (this.methodsBuilder_ == null) {
            this.methodsBuilder_ = new RepeatedFieldBuilderV3<>(this.methods_, (this.bitField0_ & 1) != 0, this.getParentForChildren(), this.isClean());
            this.methods_ = null;
         }

         return this.methodsBuilder_;
      }

      private void ensureOptionsIsMutable() {
         if ((this.bitField0_ & 2) == 0) {
            this.options_ = new ArrayList(this.options_);
            this.bitField0_ |= 2;
         }

      }

      @Override
      public List<Option> getOptionsList() {
         return this.optionsBuilder_ == null ? Collections.unmodifiableList(this.options_) : this.optionsBuilder_.getMessageList();
      }

      @Override
      public int getOptionsCount() {
         return this.optionsBuilder_ == null ? this.options_.size() : this.optionsBuilder_.getCount();
      }

      @Override
      public Option getOptions(int index) {
         return this.optionsBuilder_ == null ? (Option)this.options_.get(index) : this.optionsBuilder_.getMessage(index);
      }

      public Api.Builder setOptions(int index, Option value) {
         if (this.optionsBuilder_ == null) {
            if (value == null) {
               throw new NullPointerException();
            }

            this.ensureOptionsIsMutable();
            this.options_.set(index, value);
            this.onChanged();
         } else {
            this.optionsBuilder_.setMessage(index, value);
         }

         return this;
      }

      public Api.Builder setOptions(int index, Option.Builder builderForValue) {
         if (this.optionsBuilder_ == null) {
            this.ensureOptionsIsMutable();
            this.options_.set(index, builderForValue.build());
            this.onChanged();
         } else {
            this.optionsBuilder_.setMessage(index, builderForValue.build());
         }

         return this;
      }

      public Api.Builder addOptions(Option value) {
         if (this.optionsBuilder_ == null) {
            if (value == null) {
               throw new NullPointerException();
            }

            this.ensureOptionsIsMutable();
            this.options_.add(value);
            this.onChanged();
         } else {
            this.optionsBuilder_.addMessage(value);
         }

         return this;
      }

      public Api.Builder addOptions(int index, Option value) {
         if (this.optionsBuilder_ == null) {
            if (value == null) {
               throw new NullPointerException();
            }

            this.ensureOptionsIsMutable();
            this.options_.add(index, value);
            this.onChanged();
         } else {
            this.optionsBuilder_.addMessage(index, value);
         }

         return this;
      }

      public Api.Builder addOptions(Option.Builder builderForValue) {
         if (this.optionsBuilder_ == null) {
            this.ensureOptionsIsMutable();
            this.options_.add(builderForValue.build());
            this.onChanged();
         } else {
            this.optionsBuilder_.addMessage(builderForValue.build());
         }

         return this;
      }

      public Api.Builder addOptions(int index, Option.Builder builderForValue) {
         if (this.optionsBuilder_ == null) {
            this.ensureOptionsIsMutable();
            this.options_.add(index, builderForValue.build());
            this.onChanged();
         } else {
            this.optionsBuilder_.addMessage(index, builderForValue.build());
         }

         return this;
      }

      public Api.Builder addAllOptions(Iterable<? extends Option> values) {
         if (this.optionsBuilder_ == null) {
            this.ensureOptionsIsMutable();
            AbstractMessageLite.Builder.addAll(values, this.options_);
            this.onChanged();
         } else {
            this.optionsBuilder_.addAllMessages(values);
         }

         return this;
      }

      public Api.Builder clearOptions() {
         if (this.optionsBuilder_ == null) {
            this.options_ = Collections.emptyList();
            this.bitField0_ &= -3;
            this.onChanged();
         } else {
            this.optionsBuilder_.clear();
         }

         return this;
      }

      public Api.Builder removeOptions(int index) {
         if (this.optionsBuilder_ == null) {
            this.ensureOptionsIsMutable();
            this.options_.remove(index);
            this.onChanged();
         } else {
            this.optionsBuilder_.remove(index);
         }

         return this;
      }

      public Option.Builder getOptionsBuilder(int index) {
         return this.getOptionsFieldBuilder().getBuilder(index);
      }

      @Override
      public OptionOrBuilder getOptionsOrBuilder(int index) {
         return this.optionsBuilder_ == null ? (OptionOrBuilder)this.options_.get(index) : this.optionsBuilder_.getMessageOrBuilder(index);
      }

      @Override
      public List<? extends OptionOrBuilder> getOptionsOrBuilderList() {
         return this.optionsBuilder_ != null ? this.optionsBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.options_);
      }

      public Option.Builder addOptionsBuilder() {
         return this.getOptionsFieldBuilder().addBuilder(Option.getDefaultInstance());
      }

      public Option.Builder addOptionsBuilder(int index) {
         return this.getOptionsFieldBuilder().addBuilder(index, Option.getDefaultInstance());
      }

      public List<Option.Builder> getOptionsBuilderList() {
         return this.getOptionsFieldBuilder().getBuilderList();
      }

      private RepeatedFieldBuilderV3<Option, Option.Builder, OptionOrBuilder> getOptionsFieldBuilder() {
         if (this.optionsBuilder_ == null) {
            this.optionsBuilder_ = new RepeatedFieldBuilderV3<>(this.options_, (this.bitField0_ & 2) != 0, this.getParentForChildren(), this.isClean());
            this.options_ = null;
         }

         return this.optionsBuilder_;
      }

      @Override
      public String getVersion() {
         Object ref = this.version_;
         if (!(ref instanceof String)) {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            this.version_ = s;
            return s;
         } else {
            return (String)ref;
         }
      }

      @Override
      public ByteString getVersionBytes() {
         Object ref = this.version_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.version_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      public Api.Builder setVersion(String value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            this.version_ = value;
            this.onChanged();
            return this;
         }
      }

      public Api.Builder clearVersion() {
         this.version_ = Api.getDefaultInstance().getVersion();
         this.onChanged();
         return this;
      }

      public Api.Builder setVersionBytes(ByteString value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            AbstractMessageLite.checkByteStringIsUtf8(value);
            this.version_ = value;
            this.onChanged();
            return this;
         }
      }

      @Override
      public boolean hasSourceContext() {
         return this.sourceContextBuilder_ != null || this.sourceContext_ != null;
      }

      @Override
      public SourceContext getSourceContext() {
         if (this.sourceContextBuilder_ == null) {
            return this.sourceContext_ == null ? SourceContext.getDefaultInstance() : this.sourceContext_;
         } else {
            return this.sourceContextBuilder_.getMessage();
         }
      }

      public Api.Builder setSourceContext(SourceContext value) {
         if (this.sourceContextBuilder_ == null) {
            if (value == null) {
               throw new NullPointerException();
            }

            this.sourceContext_ = value;
            this.onChanged();
         } else {
            this.sourceContextBuilder_.setMessage(value);
         }

         return this;
      }

      public Api.Builder setSourceContext(SourceContext.Builder builderForValue) {
         if (this.sourceContextBuilder_ == null) {
            this.sourceContext_ = builderForValue.build();
            this.onChanged();
         } else {
            this.sourceContextBuilder_.setMessage(builderForValue.build());
         }

         return this;
      }

      public Api.Builder mergeSourceContext(SourceContext value) {
         if (this.sourceContextBuilder_ == null) {
            if (this.sourceContext_ != null) {
               this.sourceContext_ = SourceContext.newBuilder(this.sourceContext_).mergeFrom(value).buildPartial();
            } else {
               this.sourceContext_ = value;
            }

            this.onChanged();
         } else {
            this.sourceContextBuilder_.mergeFrom(value);
         }

         return this;
      }

      public Api.Builder clearSourceContext() {
         if (this.sourceContextBuilder_ == null) {
            this.sourceContext_ = null;
            this.onChanged();
         } else {
            this.sourceContext_ = null;
            this.sourceContextBuilder_ = null;
         }

         return this;
      }

      public SourceContext.Builder getSourceContextBuilder() {
         this.onChanged();
         return this.getSourceContextFieldBuilder().getBuilder();
      }

      @Override
      public SourceContextOrBuilder getSourceContextOrBuilder() {
         if (this.sourceContextBuilder_ != null) {
            return this.sourceContextBuilder_.getMessageOrBuilder();
         } else {
            return this.sourceContext_ == null ? SourceContext.getDefaultInstance() : this.sourceContext_;
         }
      }

      private SingleFieldBuilderV3<SourceContext, SourceContext.Builder, SourceContextOrBuilder> getSourceContextFieldBuilder() {
         if (this.sourceContextBuilder_ == null) {
            this.sourceContextBuilder_ = new SingleFieldBuilderV3<>(this.getSourceContext(), this.getParentForChildren(), this.isClean());
            this.sourceContext_ = null;
         }

         return this.sourceContextBuilder_;
      }

      private void ensureMixinsIsMutable() {
         if ((this.bitField0_ & 4) == 0) {
            this.mixins_ = new ArrayList(this.mixins_);
            this.bitField0_ |= 4;
         }

      }

      @Override
      public List<Mixin> getMixinsList() {
         return this.mixinsBuilder_ == null ? Collections.unmodifiableList(this.mixins_) : this.mixinsBuilder_.getMessageList();
      }

      @Override
      public int getMixinsCount() {
         return this.mixinsBuilder_ == null ? this.mixins_.size() : this.mixinsBuilder_.getCount();
      }

      @Override
      public Mixin getMixins(int index) {
         return this.mixinsBuilder_ == null ? (Mixin)this.mixins_.get(index) : this.mixinsBuilder_.getMessage(index);
      }

      public Api.Builder setMixins(int index, Mixin value) {
         if (this.mixinsBuilder_ == null) {
            if (value == null) {
               throw new NullPointerException();
            }

            this.ensureMixinsIsMutable();
            this.mixins_.set(index, value);
            this.onChanged();
         } else {
            this.mixinsBuilder_.setMessage(index, value);
         }

         return this;
      }

      public Api.Builder setMixins(int index, Mixin.Builder builderForValue) {
         if (this.mixinsBuilder_ == null) {
            this.ensureMixinsIsMutable();
            this.mixins_.set(index, builderForValue.build());
            this.onChanged();
         } else {
            this.mixinsBuilder_.setMessage(index, builderForValue.build());
         }

         return this;
      }

      public Api.Builder addMixins(Mixin value) {
         if (this.mixinsBuilder_ == null) {
            if (value == null) {
               throw new NullPointerException();
            }

            this.ensureMixinsIsMutable();
            this.mixins_.add(value);
            this.onChanged();
         } else {
            this.mixinsBuilder_.addMessage(value);
         }

         return this;
      }

      public Api.Builder addMixins(int index, Mixin value) {
         if (this.mixinsBuilder_ == null) {
            if (value == null) {
               throw new NullPointerException();
            }

            this.ensureMixinsIsMutable();
            this.mixins_.add(index, value);
            this.onChanged();
         } else {
            this.mixinsBuilder_.addMessage(index, value);
         }

         return this;
      }

      public Api.Builder addMixins(Mixin.Builder builderForValue) {
         if (this.mixinsBuilder_ == null) {
            this.ensureMixinsIsMutable();
            this.mixins_.add(builderForValue.build());
            this.onChanged();
         } else {
            this.mixinsBuilder_.addMessage(builderForValue.build());
         }

         return this;
      }

      public Api.Builder addMixins(int index, Mixin.Builder builderForValue) {
         if (this.mixinsBuilder_ == null) {
            this.ensureMixinsIsMutable();
            this.mixins_.add(index, builderForValue.build());
            this.onChanged();
         } else {
            this.mixinsBuilder_.addMessage(index, builderForValue.build());
         }

         return this;
      }

      public Api.Builder addAllMixins(Iterable<? extends Mixin> values) {
         if (this.mixinsBuilder_ == null) {
            this.ensureMixinsIsMutable();
            AbstractMessageLite.Builder.addAll(values, this.mixins_);
            this.onChanged();
         } else {
            this.mixinsBuilder_.addAllMessages(values);
         }

         return this;
      }

      public Api.Builder clearMixins() {
         if (this.mixinsBuilder_ == null) {
            this.mixins_ = Collections.emptyList();
            this.bitField0_ &= -5;
            this.onChanged();
         } else {
            this.mixinsBuilder_.clear();
         }

         return this;
      }

      public Api.Builder removeMixins(int index) {
         if (this.mixinsBuilder_ == null) {
            this.ensureMixinsIsMutable();
            this.mixins_.remove(index);
            this.onChanged();
         } else {
            this.mixinsBuilder_.remove(index);
         }

         return this;
      }

      public Mixin.Builder getMixinsBuilder(int index) {
         return this.getMixinsFieldBuilder().getBuilder(index);
      }

      @Override
      public MixinOrBuilder getMixinsOrBuilder(int index) {
         return this.mixinsBuilder_ == null ? (MixinOrBuilder)this.mixins_.get(index) : this.mixinsBuilder_.getMessageOrBuilder(index);
      }

      @Override
      public List<? extends MixinOrBuilder> getMixinsOrBuilderList() {
         return this.mixinsBuilder_ != null ? this.mixinsBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.mixins_);
      }

      public Mixin.Builder addMixinsBuilder() {
         return this.getMixinsFieldBuilder().addBuilder(Mixin.getDefaultInstance());
      }

      public Mixin.Builder addMixinsBuilder(int index) {
         return this.getMixinsFieldBuilder().addBuilder(index, Mixin.getDefaultInstance());
      }

      public List<Mixin.Builder> getMixinsBuilderList() {
         return this.getMixinsFieldBuilder().getBuilderList();
      }

      private RepeatedFieldBuilderV3<Mixin, Mixin.Builder, MixinOrBuilder> getMixinsFieldBuilder() {
         if (this.mixinsBuilder_ == null) {
            this.mixinsBuilder_ = new RepeatedFieldBuilderV3<>(this.mixins_, (this.bitField0_ & 4) != 0, this.getParentForChildren(), this.isClean());
            this.mixins_ = null;
         }

         return this.mixinsBuilder_;
      }

      @Override
      public int getSyntaxValue() {
         return this.syntax_;
      }

      public Api.Builder setSyntaxValue(int value) {
         this.syntax_ = value;
         this.onChanged();
         return this;
      }

      @Override
      public Syntax getSyntax() {
         Syntax result = Syntax.valueOf(this.syntax_);
         return result == null ? Syntax.UNRECOGNIZED : result;
      }

      public Api.Builder setSyntax(Syntax value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            this.syntax_ = value.getNumber();
            this.onChanged();
            return this;
         }
      }

      public Api.Builder clearSyntax() {
         this.syntax_ = 0;
         this.onChanged();
         return this;
      }

      public final Api.Builder setUnknownFields(UnknownFieldSet unknownFields) {
         return (Api.Builder)super.setUnknownFields(unknownFields);
      }

      public final Api.Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
         return (Api.Builder)super.mergeUnknownFields(unknownFields);
      }
   }
}
