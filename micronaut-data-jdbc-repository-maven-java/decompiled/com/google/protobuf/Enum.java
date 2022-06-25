package com.google.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Enum extends GeneratedMessageV3 implements EnumOrBuilder {
   private static final long serialVersionUID = 0L;
   public static final int NAME_FIELD_NUMBER = 1;
   private volatile Object name_;
   public static final int ENUMVALUE_FIELD_NUMBER = 2;
   private List<EnumValue> enumvalue_;
   public static final int OPTIONS_FIELD_NUMBER = 3;
   private List<Option> options_;
   public static final int SOURCE_CONTEXT_FIELD_NUMBER = 4;
   private SourceContext sourceContext_;
   public static final int SYNTAX_FIELD_NUMBER = 5;
   private int syntax_;
   private byte memoizedIsInitialized = -1;
   private static final Enum DEFAULT_INSTANCE = new Enum();
   private static final Parser<Enum> PARSER = new AbstractParser<Enum>() {
      public Enum parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return new Enum(input, extensionRegistry);
      }
   };

   private Enum(GeneratedMessageV3.Builder<?> builder) {
      super(builder);
   }

   private Enum() {
      this.name_ = "";
      this.enumvalue_ = Collections.emptyList();
      this.options_ = Collections.emptyList();
      this.syntax_ = 0;
   }

   @Override
   protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
      return new Enum();
   }

   @Override
   public final UnknownFieldSet getUnknownFields() {
      return this.unknownFields;
   }

   private Enum(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     this.name_ = s;
                     break;
                  case 18:
                     if ((mutable_bitField0_ & 1) == 0) {
                        this.enumvalue_ = new ArrayList();
                        mutable_bitField0_ |= 1;
                     }

                     this.enumvalue_.add(input.readMessage(EnumValue.parser(), extensionRegistry));
                     break;
                  case 26:
                     if ((mutable_bitField0_ & 2) == 0) {
                        this.options_ = new ArrayList();
                        mutable_bitField0_ |= 2;
                     }

                     this.options_.add(input.readMessage(Option.parser(), extensionRegistry));
                     break;
                  case 34:
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
                  case 40:
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
               this.enumvalue_ = Collections.unmodifiableList(this.enumvalue_);
            }

            if ((mutable_bitField0_ & 2) != 0) {
               this.options_ = Collections.unmodifiableList(this.options_);
            }

            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }

      }
   }

   public static final Descriptors.Descriptor getDescriptor() {
      return TypeProto.internal_static_google_protobuf_Enum_descriptor;
   }

   @Override
   protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
      return TypeProto.internal_static_google_protobuf_Enum_fieldAccessorTable.ensureFieldAccessorsInitialized(Enum.class, Enum.Builder.class);
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
   public List<EnumValue> getEnumvalueList() {
      return this.enumvalue_;
   }

   @Override
   public List<? extends EnumValueOrBuilder> getEnumvalueOrBuilderList() {
      return this.enumvalue_;
   }

   @Override
   public int getEnumvalueCount() {
      return this.enumvalue_.size();
   }

   @Override
   public EnumValue getEnumvalue(int index) {
      return (EnumValue)this.enumvalue_.get(index);
   }

   @Override
   public EnumValueOrBuilder getEnumvalueOrBuilder(int index) {
      return (EnumValueOrBuilder)this.enumvalue_.get(index);
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

      for(int i = 0; i < this.enumvalue_.size(); ++i) {
         output.writeMessage(2, (MessageLite)this.enumvalue_.get(i));
      }

      for(int i = 0; i < this.options_.size(); ++i) {
         output.writeMessage(3, (MessageLite)this.options_.get(i));
      }

      if (this.sourceContext_ != null) {
         output.writeMessage(4, this.getSourceContext());
      }

      if (this.syntax_ != Syntax.SYNTAX_PROTO2.getNumber()) {
         output.writeEnum(5, this.syntax_);
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

         for(int i = 0; i < this.enumvalue_.size(); ++i) {
            size += CodedOutputStream.computeMessageSize(2, (MessageLite)this.enumvalue_.get(i));
         }

         for(int i = 0; i < this.options_.size(); ++i) {
            size += CodedOutputStream.computeMessageSize(3, (MessageLite)this.options_.get(i));
         }

         if (this.sourceContext_ != null) {
            size += CodedOutputStream.computeMessageSize(4, this.getSourceContext());
         }

         if (this.syntax_ != Syntax.SYNTAX_PROTO2.getNumber()) {
            size += CodedOutputStream.computeEnumSize(5, this.syntax_);
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
      } else if (!(obj instanceof Enum)) {
         return super.equals(obj);
      } else {
         Enum other = (Enum)obj;
         if (!this.getName().equals(other.getName())) {
            return false;
         } else if (!this.getEnumvalueList().equals(other.getEnumvalueList())) {
            return false;
         } else if (!this.getOptionsList().equals(other.getOptionsList())) {
            return false;
         } else if (this.hasSourceContext() != other.hasSourceContext()) {
            return false;
         } else if (this.hasSourceContext() && !this.getSourceContext().equals(other.getSourceContext())) {
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
         if (this.getEnumvalueCount() > 0) {
            hash = 37 * hash + 2;
            hash = 53 * hash + this.getEnumvalueList().hashCode();
         }

         if (this.getOptionsCount() > 0) {
            hash = 37 * hash + 3;
            hash = 53 * hash + this.getOptionsList().hashCode();
         }

         if (this.hasSourceContext()) {
            hash = 37 * hash + 4;
            hash = 53 * hash + this.getSourceContext().hashCode();
         }

         hash = 37 * hash + 5;
         hash = 53 * hash + this.syntax_;
         hash = 29 * hash + this.unknownFields.hashCode();
         this.memoizedHashCode = hash;
         return hash;
      }
   }

   public static Enum parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static Enum parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static Enum parseFrom(ByteString data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static Enum parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static Enum parseFrom(byte[] data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static Enum parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static Enum parseFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static Enum parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public static Enum parseDelimitedFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
   }

   public static Enum parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
   }

   public static Enum parseFrom(CodedInputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static Enum parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public Enum.Builder newBuilderForType() {
      return newBuilder();
   }

   public static Enum.Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
   }

   public static Enum.Builder newBuilder(Enum prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
   }

   public Enum.Builder toBuilder() {
      return this == DEFAULT_INSTANCE ? new Enum.Builder() : new Enum.Builder().mergeFrom(this);
   }

   protected Enum.Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
      return new Enum.Builder(parent);
   }

   public static Enum getDefaultInstance() {
      return DEFAULT_INSTANCE;
   }

   public static Parser<Enum> parser() {
      return PARSER;
   }

   @Override
   public Parser<Enum> getParserForType() {
      return PARSER;
   }

   public Enum getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
   }

   public static final class Builder extends GeneratedMessageV3.Builder<Enum.Builder> implements EnumOrBuilder {
      private int bitField0_;
      private Object name_ = "";
      private List<EnumValue> enumvalue_ = Collections.emptyList();
      private RepeatedFieldBuilderV3<EnumValue, EnumValue.Builder, EnumValueOrBuilder> enumvalueBuilder_;
      private List<Option> options_ = Collections.emptyList();
      private RepeatedFieldBuilderV3<Option, Option.Builder, OptionOrBuilder> optionsBuilder_;
      private SourceContext sourceContext_;
      private SingleFieldBuilderV3<SourceContext, SourceContext.Builder, SourceContextOrBuilder> sourceContextBuilder_;
      private int syntax_ = 0;

      public static final Descriptors.Descriptor getDescriptor() {
         return TypeProto.internal_static_google_protobuf_Enum_descriptor;
      }

      @Override
      protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
         return TypeProto.internal_static_google_protobuf_Enum_fieldAccessorTable.ensureFieldAccessorsInitialized(Enum.class, Enum.Builder.class);
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
            this.getEnumvalueFieldBuilder();
            this.getOptionsFieldBuilder();
         }

      }

      public Enum.Builder clear() {
         super.clear();
         this.name_ = "";
         if (this.enumvalueBuilder_ == null) {
            this.enumvalue_ = Collections.emptyList();
            this.bitField0_ &= -2;
         } else {
            this.enumvalueBuilder_.clear();
         }

         if (this.optionsBuilder_ == null) {
            this.options_ = Collections.emptyList();
            this.bitField0_ &= -3;
         } else {
            this.optionsBuilder_.clear();
         }

         if (this.sourceContextBuilder_ == null) {
            this.sourceContext_ = null;
         } else {
            this.sourceContext_ = null;
            this.sourceContextBuilder_ = null;
         }

         this.syntax_ = 0;
         return this;
      }

      @Override
      public Descriptors.Descriptor getDescriptorForType() {
         return TypeProto.internal_static_google_protobuf_Enum_descriptor;
      }

      public Enum getDefaultInstanceForType() {
         return Enum.getDefaultInstance();
      }

      public Enum build() {
         Enum result = this.buildPartial();
         if (!result.isInitialized()) {
            throw newUninitializedMessageException(result);
         } else {
            return result;
         }
      }

      public Enum buildPartial() {
         Enum result = new Enum(this);
         int from_bitField0_ = this.bitField0_;
         result.name_ = this.name_;
         if (this.enumvalueBuilder_ == null) {
            if ((this.bitField0_ & 1) != 0) {
               this.enumvalue_ = Collections.unmodifiableList(this.enumvalue_);
               this.bitField0_ &= -2;
            }

            result.enumvalue_ = this.enumvalue_;
         } else {
            result.enumvalue_ = this.enumvalueBuilder_.build();
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

         if (this.sourceContextBuilder_ == null) {
            result.sourceContext_ = this.sourceContext_;
         } else {
            result.sourceContext_ = this.sourceContextBuilder_.build();
         }

         result.syntax_ = this.syntax_;
         this.onBuilt();
         return result;
      }

      public Enum.Builder clone() {
         return (Enum.Builder)super.clone();
      }

      public Enum.Builder setField(Descriptors.FieldDescriptor field, Object value) {
         return (Enum.Builder)super.setField(field, value);
      }

      public Enum.Builder clearField(Descriptors.FieldDescriptor field) {
         return (Enum.Builder)super.clearField(field);
      }

      public Enum.Builder clearOneof(Descriptors.OneofDescriptor oneof) {
         return (Enum.Builder)super.clearOneof(oneof);
      }

      public Enum.Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
         return (Enum.Builder)super.setRepeatedField(field, index, value);
      }

      public Enum.Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
         return (Enum.Builder)super.addRepeatedField(field, value);
      }

      public Enum.Builder mergeFrom(Message other) {
         if (other instanceof Enum) {
            return this.mergeFrom((Enum)other);
         } else {
            super.mergeFrom(other);
            return this;
         }
      }

      public Enum.Builder mergeFrom(Enum other) {
         if (other == Enum.getDefaultInstance()) {
            return this;
         } else {
            if (!other.getName().isEmpty()) {
               this.name_ = other.name_;
               this.onChanged();
            }

            if (this.enumvalueBuilder_ == null) {
               if (!other.enumvalue_.isEmpty()) {
                  if (this.enumvalue_.isEmpty()) {
                     this.enumvalue_ = other.enumvalue_;
                     this.bitField0_ &= -2;
                  } else {
                     this.ensureEnumvalueIsMutable();
                     this.enumvalue_.addAll(other.enumvalue_);
                  }

                  this.onChanged();
               }
            } else if (!other.enumvalue_.isEmpty()) {
               if (this.enumvalueBuilder_.isEmpty()) {
                  this.enumvalueBuilder_.dispose();
                  this.enumvalueBuilder_ = null;
                  this.enumvalue_ = other.enumvalue_;
                  this.bitField0_ &= -2;
                  this.enumvalueBuilder_ = GeneratedMessageV3.alwaysUseFieldBuilders ? this.getEnumvalueFieldBuilder() : null;
               } else {
                  this.enumvalueBuilder_.addAllMessages(other.enumvalue_);
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

            if (other.hasSourceContext()) {
               this.mergeSourceContext(other.getSourceContext());
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

      public Enum.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         Enum parsedMessage = null;

         try {
            parsedMessage = Enum.PARSER.parsePartialFrom(input, extensionRegistry);
         } catch (InvalidProtocolBufferException var8) {
            parsedMessage = (Enum)var8.getUnfinishedMessage();
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

      public Enum.Builder setName(String value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            this.name_ = value;
            this.onChanged();
            return this;
         }
      }

      public Enum.Builder clearName() {
         this.name_ = Enum.getDefaultInstance().getName();
         this.onChanged();
         return this;
      }

      public Enum.Builder setNameBytes(ByteString value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            AbstractMessageLite.checkByteStringIsUtf8(value);
            this.name_ = value;
            this.onChanged();
            return this;
         }
      }

      private void ensureEnumvalueIsMutable() {
         if ((this.bitField0_ & 1) == 0) {
            this.enumvalue_ = new ArrayList(this.enumvalue_);
            this.bitField0_ |= 1;
         }

      }

      @Override
      public List<EnumValue> getEnumvalueList() {
         return this.enumvalueBuilder_ == null ? Collections.unmodifiableList(this.enumvalue_) : this.enumvalueBuilder_.getMessageList();
      }

      @Override
      public int getEnumvalueCount() {
         return this.enumvalueBuilder_ == null ? this.enumvalue_.size() : this.enumvalueBuilder_.getCount();
      }

      @Override
      public EnumValue getEnumvalue(int index) {
         return this.enumvalueBuilder_ == null ? (EnumValue)this.enumvalue_.get(index) : this.enumvalueBuilder_.getMessage(index);
      }

      public Enum.Builder setEnumvalue(int index, EnumValue value) {
         if (this.enumvalueBuilder_ == null) {
            if (value == null) {
               throw new NullPointerException();
            }

            this.ensureEnumvalueIsMutable();
            this.enumvalue_.set(index, value);
            this.onChanged();
         } else {
            this.enumvalueBuilder_.setMessage(index, value);
         }

         return this;
      }

      public Enum.Builder setEnumvalue(int index, EnumValue.Builder builderForValue) {
         if (this.enumvalueBuilder_ == null) {
            this.ensureEnumvalueIsMutable();
            this.enumvalue_.set(index, builderForValue.build());
            this.onChanged();
         } else {
            this.enumvalueBuilder_.setMessage(index, builderForValue.build());
         }

         return this;
      }

      public Enum.Builder addEnumvalue(EnumValue value) {
         if (this.enumvalueBuilder_ == null) {
            if (value == null) {
               throw new NullPointerException();
            }

            this.ensureEnumvalueIsMutable();
            this.enumvalue_.add(value);
            this.onChanged();
         } else {
            this.enumvalueBuilder_.addMessage(value);
         }

         return this;
      }

      public Enum.Builder addEnumvalue(int index, EnumValue value) {
         if (this.enumvalueBuilder_ == null) {
            if (value == null) {
               throw new NullPointerException();
            }

            this.ensureEnumvalueIsMutable();
            this.enumvalue_.add(index, value);
            this.onChanged();
         } else {
            this.enumvalueBuilder_.addMessage(index, value);
         }

         return this;
      }

      public Enum.Builder addEnumvalue(EnumValue.Builder builderForValue) {
         if (this.enumvalueBuilder_ == null) {
            this.ensureEnumvalueIsMutable();
            this.enumvalue_.add(builderForValue.build());
            this.onChanged();
         } else {
            this.enumvalueBuilder_.addMessage(builderForValue.build());
         }

         return this;
      }

      public Enum.Builder addEnumvalue(int index, EnumValue.Builder builderForValue) {
         if (this.enumvalueBuilder_ == null) {
            this.ensureEnumvalueIsMutable();
            this.enumvalue_.add(index, builderForValue.build());
            this.onChanged();
         } else {
            this.enumvalueBuilder_.addMessage(index, builderForValue.build());
         }

         return this;
      }

      public Enum.Builder addAllEnumvalue(Iterable<? extends EnumValue> values) {
         if (this.enumvalueBuilder_ == null) {
            this.ensureEnumvalueIsMutable();
            AbstractMessageLite.Builder.addAll(values, this.enumvalue_);
            this.onChanged();
         } else {
            this.enumvalueBuilder_.addAllMessages(values);
         }

         return this;
      }

      public Enum.Builder clearEnumvalue() {
         if (this.enumvalueBuilder_ == null) {
            this.enumvalue_ = Collections.emptyList();
            this.bitField0_ &= -2;
            this.onChanged();
         } else {
            this.enumvalueBuilder_.clear();
         }

         return this;
      }

      public Enum.Builder removeEnumvalue(int index) {
         if (this.enumvalueBuilder_ == null) {
            this.ensureEnumvalueIsMutable();
            this.enumvalue_.remove(index);
            this.onChanged();
         } else {
            this.enumvalueBuilder_.remove(index);
         }

         return this;
      }

      public EnumValue.Builder getEnumvalueBuilder(int index) {
         return this.getEnumvalueFieldBuilder().getBuilder(index);
      }

      @Override
      public EnumValueOrBuilder getEnumvalueOrBuilder(int index) {
         return this.enumvalueBuilder_ == null ? (EnumValueOrBuilder)this.enumvalue_.get(index) : this.enumvalueBuilder_.getMessageOrBuilder(index);
      }

      @Override
      public List<? extends EnumValueOrBuilder> getEnumvalueOrBuilderList() {
         return this.enumvalueBuilder_ != null ? this.enumvalueBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.enumvalue_);
      }

      public EnumValue.Builder addEnumvalueBuilder() {
         return this.getEnumvalueFieldBuilder().addBuilder(EnumValue.getDefaultInstance());
      }

      public EnumValue.Builder addEnumvalueBuilder(int index) {
         return this.getEnumvalueFieldBuilder().addBuilder(index, EnumValue.getDefaultInstance());
      }

      public List<EnumValue.Builder> getEnumvalueBuilderList() {
         return this.getEnumvalueFieldBuilder().getBuilderList();
      }

      private RepeatedFieldBuilderV3<EnumValue, EnumValue.Builder, EnumValueOrBuilder> getEnumvalueFieldBuilder() {
         if (this.enumvalueBuilder_ == null) {
            this.enumvalueBuilder_ = new RepeatedFieldBuilderV3<>(this.enumvalue_, (this.bitField0_ & 1) != 0, this.getParentForChildren(), this.isClean());
            this.enumvalue_ = null;
         }

         return this.enumvalueBuilder_;
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

      public Enum.Builder setOptions(int index, Option value) {
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

      public Enum.Builder setOptions(int index, Option.Builder builderForValue) {
         if (this.optionsBuilder_ == null) {
            this.ensureOptionsIsMutable();
            this.options_.set(index, builderForValue.build());
            this.onChanged();
         } else {
            this.optionsBuilder_.setMessage(index, builderForValue.build());
         }

         return this;
      }

      public Enum.Builder addOptions(Option value) {
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

      public Enum.Builder addOptions(int index, Option value) {
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

      public Enum.Builder addOptions(Option.Builder builderForValue) {
         if (this.optionsBuilder_ == null) {
            this.ensureOptionsIsMutable();
            this.options_.add(builderForValue.build());
            this.onChanged();
         } else {
            this.optionsBuilder_.addMessage(builderForValue.build());
         }

         return this;
      }

      public Enum.Builder addOptions(int index, Option.Builder builderForValue) {
         if (this.optionsBuilder_ == null) {
            this.ensureOptionsIsMutable();
            this.options_.add(index, builderForValue.build());
            this.onChanged();
         } else {
            this.optionsBuilder_.addMessage(index, builderForValue.build());
         }

         return this;
      }

      public Enum.Builder addAllOptions(Iterable<? extends Option> values) {
         if (this.optionsBuilder_ == null) {
            this.ensureOptionsIsMutable();
            AbstractMessageLite.Builder.addAll(values, this.options_);
            this.onChanged();
         } else {
            this.optionsBuilder_.addAllMessages(values);
         }

         return this;
      }

      public Enum.Builder clearOptions() {
         if (this.optionsBuilder_ == null) {
            this.options_ = Collections.emptyList();
            this.bitField0_ &= -3;
            this.onChanged();
         } else {
            this.optionsBuilder_.clear();
         }

         return this;
      }

      public Enum.Builder removeOptions(int index) {
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

      public Enum.Builder setSourceContext(SourceContext value) {
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

      public Enum.Builder setSourceContext(SourceContext.Builder builderForValue) {
         if (this.sourceContextBuilder_ == null) {
            this.sourceContext_ = builderForValue.build();
            this.onChanged();
         } else {
            this.sourceContextBuilder_.setMessage(builderForValue.build());
         }

         return this;
      }

      public Enum.Builder mergeSourceContext(SourceContext value) {
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

      public Enum.Builder clearSourceContext() {
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

      @Override
      public int getSyntaxValue() {
         return this.syntax_;
      }

      public Enum.Builder setSyntaxValue(int value) {
         this.syntax_ = value;
         this.onChanged();
         return this;
      }

      @Override
      public Syntax getSyntax() {
         Syntax result = Syntax.valueOf(this.syntax_);
         return result == null ? Syntax.UNRECOGNIZED : result;
      }

      public Enum.Builder setSyntax(Syntax value) {
         if (value == null) {
            throw new NullPointerException();
         } else {
            this.syntax_ = value.getNumber();
            this.onChanged();
            return this;
         }
      }

      public Enum.Builder clearSyntax() {
         this.syntax_ = 0;
         this.onChanged();
         return this;
      }

      public final Enum.Builder setUnknownFields(UnknownFieldSet unknownFields) {
         return (Enum.Builder)super.setUnknownFields(unknownFields);
      }

      public final Enum.Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
         return (Enum.Builder)super.mergeUnknownFields(unknownFields);
      }
   }
}
