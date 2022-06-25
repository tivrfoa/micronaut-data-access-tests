package com.google.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ListValue extends GeneratedMessageV3 implements ListValueOrBuilder {
   private static final long serialVersionUID = 0L;
   public static final int VALUES_FIELD_NUMBER = 1;
   private List<Value> values_;
   private byte memoizedIsInitialized = -1;
   private static final ListValue DEFAULT_INSTANCE = new ListValue();
   private static final Parser<ListValue> PARSER = new AbstractParser<ListValue>() {
      public ListValue parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return new ListValue(input, extensionRegistry);
      }
   };

   private ListValue(GeneratedMessageV3.Builder<?> builder) {
      super(builder);
   }

   private ListValue() {
      this.values_ = Collections.emptyList();
   }

   @Override
   protected Object newInstance(GeneratedMessageV3.UnusedPrivateParameter unused) {
      return new ListValue();
   }

   @Override
   public final UnknownFieldSet getUnknownFields() {
      return this.unknownFields;
   }

   private ListValue(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     if ((mutable_bitField0_ & 1) == 0) {
                        this.values_ = new ArrayList();
                        mutable_bitField0_ |= 1;
                     }

                     this.values_.add(input.readMessage(Value.parser(), extensionRegistry));
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
            if ((mutable_bitField0_ & 1) != 0) {
               this.values_ = Collections.unmodifiableList(this.values_);
            }

            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }

      }
   }

   public static final Descriptors.Descriptor getDescriptor() {
      return StructProto.internal_static_google_protobuf_ListValue_descriptor;
   }

   @Override
   protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
      return StructProto.internal_static_google_protobuf_ListValue_fieldAccessorTable.ensureFieldAccessorsInitialized(ListValue.class, ListValue.Builder.class);
   }

   @Override
   public List<Value> getValuesList() {
      return this.values_;
   }

   @Override
   public List<? extends ValueOrBuilder> getValuesOrBuilderList() {
      return this.values_;
   }

   @Override
   public int getValuesCount() {
      return this.values_.size();
   }

   @Override
   public Value getValues(int index) {
      return (Value)this.values_.get(index);
   }

   @Override
   public ValueOrBuilder getValuesOrBuilder(int index) {
      return (ValueOrBuilder)this.values_.get(index);
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
      for(int i = 0; i < this.values_.size(); ++i) {
         output.writeMessage(1, (MessageLite)this.values_.get(i));
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

         for(int i = 0; i < this.values_.size(); ++i) {
            size += CodedOutputStream.computeMessageSize(1, (MessageLite)this.values_.get(i));
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
      } else if (!(obj instanceof ListValue)) {
         return super.equals(obj);
      } else {
         ListValue other = (ListValue)obj;
         if (!this.getValuesList().equals(other.getValuesList())) {
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
         if (this.getValuesCount() > 0) {
            hash = 37 * hash + 1;
            hash = 53 * hash + this.getValuesList().hashCode();
         }

         hash = 29 * hash + this.unknownFields.hashCode();
         this.memoizedHashCode = hash;
         return hash;
      }
   }

   public static ListValue parseFrom(ByteBuffer data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static ListValue parseFrom(ByteBuffer data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static ListValue parseFrom(ByteString data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static ListValue parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static ListValue parseFrom(byte[] data) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
   }

   public static ListValue parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
   }

   public static ListValue parseFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static ListValue parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public static ListValue parseDelimitedFrom(InputStream input) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input);
   }

   public static ListValue parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseDelimitedWithIOException(PARSER, input, extensionRegistry);
   }

   public static ListValue parseFrom(CodedInputStream input) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input);
   }

   public static ListValue parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
      return GeneratedMessageV3.parseWithIOException(PARSER, input, extensionRegistry);
   }

   public ListValue.Builder newBuilderForType() {
      return newBuilder();
   }

   public static ListValue.Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
   }

   public static ListValue.Builder newBuilder(ListValue prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
   }

   public ListValue.Builder toBuilder() {
      return this == DEFAULT_INSTANCE ? new ListValue.Builder() : new ListValue.Builder().mergeFrom(this);
   }

   protected ListValue.Builder newBuilderForType(GeneratedMessageV3.BuilderParent parent) {
      return new ListValue.Builder(parent);
   }

   public static ListValue getDefaultInstance() {
      return DEFAULT_INSTANCE;
   }

   public static Parser<ListValue> parser() {
      return PARSER;
   }

   @Override
   public Parser<ListValue> getParserForType() {
      return PARSER;
   }

   public ListValue getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
   }

   public static final class Builder extends GeneratedMessageV3.Builder<ListValue.Builder> implements ListValueOrBuilder {
      private int bitField0_;
      private List<Value> values_ = Collections.emptyList();
      private RepeatedFieldBuilderV3<Value, Value.Builder, ValueOrBuilder> valuesBuilder_;

      public static final Descriptors.Descriptor getDescriptor() {
         return StructProto.internal_static_google_protobuf_ListValue_descriptor;
      }

      @Override
      protected GeneratedMessageV3.FieldAccessorTable internalGetFieldAccessorTable() {
         return StructProto.internal_static_google_protobuf_ListValue_fieldAccessorTable
            .ensureFieldAccessorsInitialized(ListValue.class, ListValue.Builder.class);
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
            this.getValuesFieldBuilder();
         }

      }

      public ListValue.Builder clear() {
         super.clear();
         if (this.valuesBuilder_ == null) {
            this.values_ = Collections.emptyList();
            this.bitField0_ &= -2;
         } else {
            this.valuesBuilder_.clear();
         }

         return this;
      }

      @Override
      public Descriptors.Descriptor getDescriptorForType() {
         return StructProto.internal_static_google_protobuf_ListValue_descriptor;
      }

      public ListValue getDefaultInstanceForType() {
         return ListValue.getDefaultInstance();
      }

      public ListValue build() {
         ListValue result = this.buildPartial();
         if (!result.isInitialized()) {
            throw newUninitializedMessageException(result);
         } else {
            return result;
         }
      }

      public ListValue buildPartial() {
         ListValue result = new ListValue(this);
         int from_bitField0_ = this.bitField0_;
         if (this.valuesBuilder_ == null) {
            if ((this.bitField0_ & 1) != 0) {
               this.values_ = Collections.unmodifiableList(this.values_);
               this.bitField0_ &= -2;
            }

            result.values_ = this.values_;
         } else {
            result.values_ = this.valuesBuilder_.build();
         }

         this.onBuilt();
         return result;
      }

      public ListValue.Builder clone() {
         return (ListValue.Builder)super.clone();
      }

      public ListValue.Builder setField(Descriptors.FieldDescriptor field, Object value) {
         return (ListValue.Builder)super.setField(field, value);
      }

      public ListValue.Builder clearField(Descriptors.FieldDescriptor field) {
         return (ListValue.Builder)super.clearField(field);
      }

      public ListValue.Builder clearOneof(Descriptors.OneofDescriptor oneof) {
         return (ListValue.Builder)super.clearOneof(oneof);
      }

      public ListValue.Builder setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
         return (ListValue.Builder)super.setRepeatedField(field, index, value);
      }

      public ListValue.Builder addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
         return (ListValue.Builder)super.addRepeatedField(field, value);
      }

      public ListValue.Builder mergeFrom(Message other) {
         if (other instanceof ListValue) {
            return this.mergeFrom((ListValue)other);
         } else {
            super.mergeFrom(other);
            return this;
         }
      }

      public ListValue.Builder mergeFrom(ListValue other) {
         if (other == ListValue.getDefaultInstance()) {
            return this;
         } else {
            if (this.valuesBuilder_ == null) {
               if (!other.values_.isEmpty()) {
                  if (this.values_.isEmpty()) {
                     this.values_ = other.values_;
                     this.bitField0_ &= -2;
                  } else {
                     this.ensureValuesIsMutable();
                     this.values_.addAll(other.values_);
                  }

                  this.onChanged();
               }
            } else if (!other.values_.isEmpty()) {
               if (this.valuesBuilder_.isEmpty()) {
                  this.valuesBuilder_.dispose();
                  this.valuesBuilder_ = null;
                  this.values_ = other.values_;
                  this.bitField0_ &= -2;
                  this.valuesBuilder_ = GeneratedMessageV3.alwaysUseFieldBuilders ? this.getValuesFieldBuilder() : null;
               } else {
                  this.valuesBuilder_.addAllMessages(other.values_);
               }
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

      public ListValue.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         ListValue parsedMessage = null;

         try {
            parsedMessage = ListValue.PARSER.parsePartialFrom(input, extensionRegistry);
         } catch (InvalidProtocolBufferException var8) {
            parsedMessage = (ListValue)var8.getUnfinishedMessage();
            throw var8.unwrapIOException();
         } finally {
            if (parsedMessage != null) {
               this.mergeFrom(parsedMessage);
            }

         }

         return this;
      }

      private void ensureValuesIsMutable() {
         if ((this.bitField0_ & 1) == 0) {
            this.values_ = new ArrayList(this.values_);
            this.bitField0_ |= 1;
         }

      }

      @Override
      public List<Value> getValuesList() {
         return this.valuesBuilder_ == null ? Collections.unmodifiableList(this.values_) : this.valuesBuilder_.getMessageList();
      }

      @Override
      public int getValuesCount() {
         return this.valuesBuilder_ == null ? this.values_.size() : this.valuesBuilder_.getCount();
      }

      @Override
      public Value getValues(int index) {
         return this.valuesBuilder_ == null ? (Value)this.values_.get(index) : this.valuesBuilder_.getMessage(index);
      }

      public ListValue.Builder setValues(int index, Value value) {
         if (this.valuesBuilder_ == null) {
            if (value == null) {
               throw new NullPointerException();
            }

            this.ensureValuesIsMutable();
            this.values_.set(index, value);
            this.onChanged();
         } else {
            this.valuesBuilder_.setMessage(index, value);
         }

         return this;
      }

      public ListValue.Builder setValues(int index, Value.Builder builderForValue) {
         if (this.valuesBuilder_ == null) {
            this.ensureValuesIsMutable();
            this.values_.set(index, builderForValue.build());
            this.onChanged();
         } else {
            this.valuesBuilder_.setMessage(index, builderForValue.build());
         }

         return this;
      }

      public ListValue.Builder addValues(Value value) {
         if (this.valuesBuilder_ == null) {
            if (value == null) {
               throw new NullPointerException();
            }

            this.ensureValuesIsMutable();
            this.values_.add(value);
            this.onChanged();
         } else {
            this.valuesBuilder_.addMessage(value);
         }

         return this;
      }

      public ListValue.Builder addValues(int index, Value value) {
         if (this.valuesBuilder_ == null) {
            if (value == null) {
               throw new NullPointerException();
            }

            this.ensureValuesIsMutable();
            this.values_.add(index, value);
            this.onChanged();
         } else {
            this.valuesBuilder_.addMessage(index, value);
         }

         return this;
      }

      public ListValue.Builder addValues(Value.Builder builderForValue) {
         if (this.valuesBuilder_ == null) {
            this.ensureValuesIsMutable();
            this.values_.add(builderForValue.build());
            this.onChanged();
         } else {
            this.valuesBuilder_.addMessage(builderForValue.build());
         }

         return this;
      }

      public ListValue.Builder addValues(int index, Value.Builder builderForValue) {
         if (this.valuesBuilder_ == null) {
            this.ensureValuesIsMutable();
            this.values_.add(index, builderForValue.build());
            this.onChanged();
         } else {
            this.valuesBuilder_.addMessage(index, builderForValue.build());
         }

         return this;
      }

      public ListValue.Builder addAllValues(Iterable<? extends Value> values) {
         if (this.valuesBuilder_ == null) {
            this.ensureValuesIsMutable();
            AbstractMessageLite.Builder.addAll(values, this.values_);
            this.onChanged();
         } else {
            this.valuesBuilder_.addAllMessages(values);
         }

         return this;
      }

      public ListValue.Builder clearValues() {
         if (this.valuesBuilder_ == null) {
            this.values_ = Collections.emptyList();
            this.bitField0_ &= -2;
            this.onChanged();
         } else {
            this.valuesBuilder_.clear();
         }

         return this;
      }

      public ListValue.Builder removeValues(int index) {
         if (this.valuesBuilder_ == null) {
            this.ensureValuesIsMutable();
            this.values_.remove(index);
            this.onChanged();
         } else {
            this.valuesBuilder_.remove(index);
         }

         return this;
      }

      public Value.Builder getValuesBuilder(int index) {
         return this.getValuesFieldBuilder().getBuilder(index);
      }

      @Override
      public ValueOrBuilder getValuesOrBuilder(int index) {
         return this.valuesBuilder_ == null ? (ValueOrBuilder)this.values_.get(index) : this.valuesBuilder_.getMessageOrBuilder(index);
      }

      @Override
      public List<? extends ValueOrBuilder> getValuesOrBuilderList() {
         return this.valuesBuilder_ != null ? this.valuesBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.values_);
      }

      public Value.Builder addValuesBuilder() {
         return this.getValuesFieldBuilder().addBuilder(Value.getDefaultInstance());
      }

      public Value.Builder addValuesBuilder(int index) {
         return this.getValuesFieldBuilder().addBuilder(index, Value.getDefaultInstance());
      }

      public List<Value.Builder> getValuesBuilderList() {
         return this.getValuesFieldBuilder().getBuilderList();
      }

      private RepeatedFieldBuilderV3<Value, Value.Builder, ValueOrBuilder> getValuesFieldBuilder() {
         if (this.valuesBuilder_ == null) {
            this.valuesBuilder_ = new RepeatedFieldBuilderV3<>(this.values_, (this.bitField0_ & 1) != 0, this.getParentForChildren(), this.isClean());
            this.values_ = null;
         }

         return this.valuesBuilder_;
      }

      public final ListValue.Builder setUnknownFields(UnknownFieldSet unknownFields) {
         return (ListValue.Builder)super.setUnknownFields(unknownFields);
      }

      public final ListValue.Builder mergeUnknownFields(UnknownFieldSet unknownFields) {
         return (ListValue.Builder)super.mergeUnknownFields(unknownFields);
      }
   }
}
