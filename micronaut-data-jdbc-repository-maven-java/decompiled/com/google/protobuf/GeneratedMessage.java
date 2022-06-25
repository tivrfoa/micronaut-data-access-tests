package com.google.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

public abstract class GeneratedMessage extends AbstractMessage implements Serializable {
   private static final long serialVersionUID = 1L;
   protected static boolean alwaysUseFieldBuilders = false;
   protected UnknownFieldSet unknownFields;

   protected GeneratedMessage() {
      this.unknownFields = UnknownFieldSet.getDefaultInstance();
   }

   protected GeneratedMessage(GeneratedMessage.Builder<?> builder) {
      this.unknownFields = builder.getUnknownFields();
   }

   @Override
   public Parser<? extends GeneratedMessage> getParserForType() {
      throw new UnsupportedOperationException("This is supposed to be overridden by subclasses.");
   }

   static void enableAlwaysUseFieldBuildersForTesting() {
      alwaysUseFieldBuilders = true;
   }

   protected abstract GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable();

   @Override
   public Descriptors.Descriptor getDescriptorForType() {
      return this.internalGetFieldAccessorTable().descriptor;
   }

   private Map<Descriptors.FieldDescriptor, Object> getAllFieldsMutable(boolean getBytesForString) {
      TreeMap<Descriptors.FieldDescriptor, Object> result = new TreeMap();
      Descriptors.Descriptor descriptor = this.internalGetFieldAccessorTable().descriptor;
      List<Descriptors.FieldDescriptor> fields = descriptor.getFields();

      for(int i = 0; i < fields.size(); ++i) {
         Descriptors.FieldDescriptor field = (Descriptors.FieldDescriptor)fields.get(i);
         Descriptors.OneofDescriptor oneofDescriptor = field.getContainingOneof();
         if (oneofDescriptor != null) {
            i += oneofDescriptor.getFieldCount() - 1;
            if (!this.hasOneof(oneofDescriptor)) {
               continue;
            }

            field = this.getOneofFieldDescriptor(oneofDescriptor);
         } else {
            if (field.isRepeated()) {
               List<?> value = (List)this.getField(field);
               if (!value.isEmpty()) {
                  result.put(field, value);
               }
               continue;
            }

            if (!this.hasField(field)) {
               continue;
            }
         }

         if (getBytesForString && field.getJavaType() == Descriptors.FieldDescriptor.JavaType.STRING) {
            result.put(field, this.getFieldRaw(field));
         } else {
            result.put(field, this.getField(field));
         }
      }

      return result;
   }

   @Override
   public boolean isInitialized() {
      for(Descriptors.FieldDescriptor field : this.getDescriptorForType().getFields()) {
         if (field.isRequired() && !this.hasField(field)) {
            return false;
         }

         if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
            if (field.isRepeated()) {
               for(Message element : (List)this.getField(field)) {
                  if (!element.isInitialized()) {
                     return false;
                  }
               }
            } else if (this.hasField(field) && !((Message)this.getField(field)).isInitialized()) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   public Map<Descriptors.FieldDescriptor, Object> getAllFields() {
      return Collections.unmodifiableMap(this.getAllFieldsMutable(false));
   }

   Map<Descriptors.FieldDescriptor, Object> getAllFieldsRaw() {
      return Collections.unmodifiableMap(this.getAllFieldsMutable(true));
   }

   @Override
   public boolean hasOneof(Descriptors.OneofDescriptor oneof) {
      return this.internalGetFieldAccessorTable().getOneof(oneof).has(this);
   }

   @Override
   public Descriptors.FieldDescriptor getOneofFieldDescriptor(Descriptors.OneofDescriptor oneof) {
      return this.internalGetFieldAccessorTable().getOneof(oneof).get(this);
   }

   @Override
   public boolean hasField(Descriptors.FieldDescriptor field) {
      return this.internalGetFieldAccessorTable().getField(field).has(this);
   }

   @Override
   public Object getField(Descriptors.FieldDescriptor field) {
      return this.internalGetFieldAccessorTable().getField(field).get(this);
   }

   Object getFieldRaw(Descriptors.FieldDescriptor field) {
      return this.internalGetFieldAccessorTable().getField(field).getRaw(this);
   }

   @Override
   public int getRepeatedFieldCount(Descriptors.FieldDescriptor field) {
      return this.internalGetFieldAccessorTable().getField(field).getRepeatedCount(this);
   }

   @Override
   public Object getRepeatedField(Descriptors.FieldDescriptor field, int index) {
      return this.internalGetFieldAccessorTable().getField(field).getRepeated(this, index);
   }

   @Override
   public UnknownFieldSet getUnknownFields() {
      throw new UnsupportedOperationException("This is supposed to be overridden by subclasses.");
   }

   protected boolean parseUnknownField(CodedInputStream input, UnknownFieldSet.Builder unknownFields, ExtensionRegistryLite extensionRegistry, int tag) throws IOException {
      return unknownFields.mergeFieldFrom(tag, input);
   }

   protected static <M extends Message> M parseWithIOException(Parser<M> parser, InputStream input) throws IOException {
      try {
         return parser.parseFrom(input);
      } catch (InvalidProtocolBufferException var3) {
         throw var3.unwrapIOException();
      }
   }

   protected static <M extends Message> M parseWithIOException(Parser<M> parser, InputStream input, ExtensionRegistryLite extensions) throws IOException {
      try {
         return parser.parseFrom(input, extensions);
      } catch (InvalidProtocolBufferException var4) {
         throw var4.unwrapIOException();
      }
   }

   protected static <M extends Message> M parseWithIOException(Parser<M> parser, CodedInputStream input) throws IOException {
      try {
         return parser.parseFrom(input);
      } catch (InvalidProtocolBufferException var3) {
         throw var3.unwrapIOException();
      }
   }

   protected static <M extends Message> M parseWithIOException(Parser<M> parser, CodedInputStream input, ExtensionRegistryLite extensions) throws IOException {
      try {
         return parser.parseFrom(input, extensions);
      } catch (InvalidProtocolBufferException var4) {
         throw var4.unwrapIOException();
      }
   }

   protected static <M extends Message> M parseDelimitedWithIOException(Parser<M> parser, InputStream input) throws IOException {
      try {
         return parser.parseDelimitedFrom(input);
      } catch (InvalidProtocolBufferException var3) {
         throw var3.unwrapIOException();
      }
   }

   protected static <M extends Message> M parseDelimitedWithIOException(Parser<M> parser, InputStream input, ExtensionRegistryLite extensions) throws IOException {
      try {
         return parser.parseDelimitedFrom(input, extensions);
      } catch (InvalidProtocolBufferException var4) {
         throw var4.unwrapIOException();
      }
   }

   @Override
   public void writeTo(CodedOutputStream output) throws IOException {
      MessageReflection.writeMessageTo(this, this.getAllFieldsRaw(), output, false);
   }

   @Override
   public int getSerializedSize() {
      int size = this.memoizedSize;
      if (size != -1) {
         return size;
      } else {
         this.memoizedSize = MessageReflection.getSerializedSize(this, this.getAllFieldsRaw());
         return this.memoizedSize;
      }
   }

   protected void makeExtensionsImmutable() {
   }

   protected abstract Message.Builder newBuilderForType(GeneratedMessage.BuilderParent var1);

   @Override
   protected Message.Builder newBuilderForType(final AbstractMessage.BuilderParent parent) {
      return this.newBuilderForType(new GeneratedMessage.BuilderParent() {
         @Override
         public void markDirty() {
            parent.markDirty();
         }
      });
   }

   public static <ContainingType extends Message, Type> GeneratedMessage.GeneratedExtension<ContainingType, Type> newMessageScopedGeneratedExtension(
      final Message scope, final int descriptorIndex, Class singularType, Message defaultInstance
   ) {
      return new GeneratedMessage.GeneratedExtension<>(new GeneratedMessage.CachedDescriptorRetriever() {
         @Override
         public Descriptors.FieldDescriptor loadDescriptor() {
            return (Descriptors.FieldDescriptor)scope.getDescriptorForType().getExtensions().get(descriptorIndex);
         }
      }, singularType, defaultInstance, Extension.ExtensionType.IMMUTABLE);
   }

   public static <ContainingType extends Message, Type> GeneratedMessage.GeneratedExtension<ContainingType, Type> newFileScopedGeneratedExtension(
      Class singularType, Message defaultInstance
   ) {
      return new GeneratedMessage.GeneratedExtension<>(null, singularType, defaultInstance, Extension.ExtensionType.IMMUTABLE);
   }

   public static <ContainingType extends Message, Type> GeneratedMessage.GeneratedExtension<ContainingType, Type> newMessageScopedGeneratedExtension(
      final Message scope, final String name, Class singularType, Message defaultInstance
   ) {
      return new GeneratedMessage.GeneratedExtension<>(new GeneratedMessage.CachedDescriptorRetriever() {
         @Override
         protected Descriptors.FieldDescriptor loadDescriptor() {
            return scope.getDescriptorForType().findFieldByName(name);
         }
      }, singularType, defaultInstance, Extension.ExtensionType.MUTABLE);
   }

   public static <ContainingType extends Message, Type> GeneratedMessage.GeneratedExtension<ContainingType, Type> newFileScopedGeneratedExtension(
      final Class singularType, Message defaultInstance, final String descriptorOuterClass, final String extensionName
   ) {
      return new GeneratedMessage.GeneratedExtension<>(new GeneratedMessage.CachedDescriptorRetriever() {
         @Override
         protected Descriptors.FieldDescriptor loadDescriptor() {
            try {
               Class clazz = singularType.getClassLoader().loadClass(descriptorOuterClass);
               Descriptors.FileDescriptor file = (Descriptors.FileDescriptor)clazz.getField("descriptor").get(null);
               return file.findExtensionByName(extensionName);
            } catch (Exception var3) {
               throw new RuntimeException("Cannot load descriptors: " + descriptorOuterClass + " is not a valid descriptor class name", var3);
            }
         }
      }, singularType, defaultInstance, Extension.ExtensionType.MUTABLE);
   }

   private static java.lang.reflect.Method getMethodOrDie(Class clazz, String name, Class... params) {
      try {
         return clazz.getMethod(name, params);
      } catch (NoSuchMethodException var4) {
         throw new RuntimeException("Generated message class \"" + clazz.getName() + "\" missing method \"" + name + "\".", var4);
      }
   }

   private static Object invokeOrDie(java.lang.reflect.Method method, Object object, Object... params) {
      try {
         return method.invoke(object, params);
      } catch (IllegalAccessException var5) {
         throw new RuntimeException("Couldn't use Java reflection to implement protocol message reflection.", var5);
      } catch (InvocationTargetException var6) {
         Throwable cause = var6.getCause();
         if (cause instanceof RuntimeException) {
            throw (RuntimeException)cause;
         } else if (cause instanceof Error) {
            throw (Error)cause;
         } else {
            throw new RuntimeException("Unexpected exception thrown by generated accessor method.", cause);
         }
      }
   }

   protected MapField internalGetMapField(int fieldNumber) {
      throw new RuntimeException("No map fields found in " + this.getClass().getName());
   }

   protected Object writeReplace() throws ObjectStreamException {
      return new GeneratedMessageLite.SerializedForm(this);
   }

   private static <MessageType extends GeneratedMessage.ExtendableMessage<MessageType>, T> Extension<MessageType, T> checkNotLite(
      ExtensionLite<MessageType, T> extension
   ) {
      if (extension.isLite()) {
         throw new IllegalArgumentException("Expected non-lite extension.");
      } else {
         return (Extension<MessageType, T>)extension;
      }
   }

   protected static int computeStringSize(int fieldNumber, Object value) {
      return value instanceof String
         ? CodedOutputStream.computeStringSize(fieldNumber, (String)value)
         : CodedOutputStream.computeBytesSize(fieldNumber, (ByteString)value);
   }

   protected static int computeStringSizeNoTag(Object value) {
      return value instanceof String ? CodedOutputStream.computeStringSizeNoTag((String)value) : CodedOutputStream.computeBytesSizeNoTag((ByteString)value);
   }

   protected static void writeString(CodedOutputStream output, int fieldNumber, Object value) throws IOException {
      if (value instanceof String) {
         output.writeString(fieldNumber, (String)value);
      } else {
         output.writeBytes(fieldNumber, (ByteString)value);
      }

   }

   protected static void writeStringNoTag(CodedOutputStream output, Object value) throws IOException {
      if (value instanceof String) {
         output.writeStringNoTag((String)value);
      } else {
         output.writeBytesNoTag((ByteString)value);
      }

   }

   public abstract static class Builder<BuilderType extends GeneratedMessage.Builder<BuilderType>> extends AbstractMessage.Builder<BuilderType> {
      private GeneratedMessage.BuilderParent builderParent;
      private GeneratedMessage.Builder<BuilderType>.BuilderParentImpl meAsParent;
      private boolean isClean;
      private UnknownFieldSet unknownFields = UnknownFieldSet.getDefaultInstance();

      protected Builder() {
         this(null);
      }

      protected Builder(GeneratedMessage.BuilderParent builderParent) {
         this.builderParent = builderParent;
      }

      @Override
      void dispose() {
         this.builderParent = null;
      }

      protected void onBuilt() {
         if (this.builderParent != null) {
            this.markClean();
         }

      }

      @Override
      protected void markClean() {
         this.isClean = true;
      }

      protected boolean isClean() {
         return this.isClean;
      }

      public BuilderType clone() {
         BuilderType builder = (BuilderType)this.getDefaultInstanceForType().newBuilderForType();
         builder.mergeFrom(this.buildPartial());
         return builder;
      }

      public BuilderType clear() {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
         this.onChanged();
         return (BuilderType)this;
      }

      protected abstract GeneratedMessage.FieldAccessorTable internalGetFieldAccessorTable();

      @Override
      public Descriptors.Descriptor getDescriptorForType() {
         return this.internalGetFieldAccessorTable().descriptor;
      }

      @Override
      public Map<Descriptors.FieldDescriptor, Object> getAllFields() {
         return Collections.unmodifiableMap(this.getAllFieldsMutable());
      }

      private Map<Descriptors.FieldDescriptor, Object> getAllFieldsMutable() {
         TreeMap<Descriptors.FieldDescriptor, Object> result = new TreeMap();
         Descriptors.Descriptor descriptor = this.internalGetFieldAccessorTable().descriptor;
         List<Descriptors.FieldDescriptor> fields = descriptor.getFields();

         for(int i = 0; i < fields.size(); ++i) {
            Descriptors.FieldDescriptor field = (Descriptors.FieldDescriptor)fields.get(i);
            Descriptors.OneofDescriptor oneofDescriptor = field.getContainingOneof();
            if (oneofDescriptor != null) {
               i += oneofDescriptor.getFieldCount() - 1;
               if (!this.hasOneof(oneofDescriptor)) {
                  continue;
               }

               field = this.getOneofFieldDescriptor(oneofDescriptor);
            } else {
               if (field.isRepeated()) {
                  List<?> value = (List)this.getField(field);
                  if (!value.isEmpty()) {
                     result.put(field, value);
                  }
                  continue;
               }

               if (!this.hasField(field)) {
                  continue;
               }
            }

            result.put(field, this.getField(field));
         }

         return result;
      }

      @Override
      public Message.Builder newBuilderForField(Descriptors.FieldDescriptor field) {
         return this.internalGetFieldAccessorTable().getField(field).newBuilder();
      }

      @Override
      public Message.Builder getFieldBuilder(Descriptors.FieldDescriptor field) {
         return this.internalGetFieldAccessorTable().getField(field).getBuilder(this);
      }

      @Override
      public Message.Builder getRepeatedFieldBuilder(Descriptors.FieldDescriptor field, int index) {
         return this.internalGetFieldAccessorTable().getField(field).getRepeatedBuilder(this, index);
      }

      @Override
      public boolean hasOneof(Descriptors.OneofDescriptor oneof) {
         return this.internalGetFieldAccessorTable().getOneof(oneof).has(this);
      }

      @Override
      public Descriptors.FieldDescriptor getOneofFieldDescriptor(Descriptors.OneofDescriptor oneof) {
         return this.internalGetFieldAccessorTable().getOneof(oneof).get(this);
      }

      @Override
      public boolean hasField(Descriptors.FieldDescriptor field) {
         return this.internalGetFieldAccessorTable().getField(field).has(this);
      }

      @Override
      public Object getField(Descriptors.FieldDescriptor field) {
         Object object = this.internalGetFieldAccessorTable().getField(field).get(this);
         return field.isRepeated() ? Collections.unmodifiableList((List)object) : object;
      }

      public BuilderType setField(Descriptors.FieldDescriptor field, Object value) {
         this.internalGetFieldAccessorTable().getField(field).set(this, value);
         return (BuilderType)this;
      }

      public BuilderType clearField(Descriptors.FieldDescriptor field) {
         this.internalGetFieldAccessorTable().getField(field).clear(this);
         return (BuilderType)this;
      }

      public BuilderType clearOneof(Descriptors.OneofDescriptor oneof) {
         this.internalGetFieldAccessorTable().getOneof(oneof).clear(this);
         return (BuilderType)this;
      }

      @Override
      public int getRepeatedFieldCount(Descriptors.FieldDescriptor field) {
         return this.internalGetFieldAccessorTable().getField(field).getRepeatedCount(this);
      }

      @Override
      public Object getRepeatedField(Descriptors.FieldDescriptor field, int index) {
         return this.internalGetFieldAccessorTable().getField(field).getRepeated(this, index);
      }

      public BuilderType setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
         this.internalGetFieldAccessorTable().getField(field).setRepeated(this, index, value);
         return (BuilderType)this;
      }

      public BuilderType addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
         this.internalGetFieldAccessorTable().getField(field).addRepeated(this, value);
         return (BuilderType)this;
      }

      public BuilderType setUnknownFields(UnknownFieldSet unknownFields) {
         this.unknownFields = unknownFields;
         this.onChanged();
         return (BuilderType)this;
      }

      public BuilderType mergeUnknownFields(UnknownFieldSet unknownFields) {
         this.unknownFields = UnknownFieldSet.newBuilder(this.unknownFields).mergeFrom(unknownFields).build();
         this.onChanged();
         return (BuilderType)this;
      }

      @Override
      public boolean isInitialized() {
         for(Descriptors.FieldDescriptor field : this.getDescriptorForType().getFields()) {
            if (field.isRequired() && !this.hasField(field)) {
               return false;
            }

            if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
               if (field.isRepeated()) {
                  for(Message element : (List)this.getField(field)) {
                     if (!element.isInitialized()) {
                        return false;
                     }
                  }
               } else if (this.hasField(field) && !((Message)this.getField(field)).isInitialized()) {
                  return false;
               }
            }
         }

         return true;
      }

      @Override
      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      protected boolean parseUnknownField(CodedInputStream input, UnknownFieldSet.Builder unknownFields, ExtensionRegistryLite extensionRegistry, int tag) throws IOException {
         return unknownFields.mergeFieldFrom(tag, input);
      }

      protected GeneratedMessage.BuilderParent getParentForChildren() {
         if (this.meAsParent == null) {
            this.meAsParent = new GeneratedMessage.Builder.BuilderParentImpl();
         }

         return this.meAsParent;
      }

      protected final void onChanged() {
         if (this.isClean && this.builderParent != null) {
            this.builderParent.markDirty();
            this.isClean = false;
         }

      }

      protected MapField internalGetMapField(int fieldNumber) {
         throw new RuntimeException("No map fields found in " + this.getClass().getName());
      }

      protected MapField internalGetMutableMapField(int fieldNumber) {
         throw new RuntimeException("No map fields found in " + this.getClass().getName());
      }

      private class BuilderParentImpl implements GeneratedMessage.BuilderParent {
         private BuilderParentImpl() {
         }

         @Override
         public void markDirty() {
            Builder.this.onChanged();
         }
      }
   }

   protected interface BuilderParent extends AbstractMessage.BuilderParent {
   }

   private abstract static class CachedDescriptorRetriever implements GeneratedMessage.ExtensionDescriptorRetriever {
      private volatile Descriptors.FieldDescriptor descriptor;

      private CachedDescriptorRetriever() {
      }

      protected abstract Descriptors.FieldDescriptor loadDescriptor();

      @Override
      public Descriptors.FieldDescriptor getDescriptor() {
         if (this.descriptor == null) {
            synchronized(this) {
               if (this.descriptor == null) {
                  this.descriptor = this.loadDescriptor();
               }
            }
         }

         return this.descriptor;
      }
   }

   public abstract static class ExtendableBuilder<MessageType extends GeneratedMessage.ExtendableMessage, BuilderType extends GeneratedMessage.ExtendableBuilder<MessageType, BuilderType>>
      extends GeneratedMessage.Builder<BuilderType>
      implements GeneratedMessage.ExtendableMessageOrBuilder<MessageType> {
      private FieldSet<Descriptors.FieldDescriptor> extensions = FieldSet.emptySet();

      protected ExtendableBuilder() {
      }

      protected ExtendableBuilder(GeneratedMessage.BuilderParent parent) {
         super(parent);
      }

      void internalSetExtensionSet(FieldSet<Descriptors.FieldDescriptor> extensions) {
         this.extensions = extensions;
      }

      public BuilderType clear() {
         this.extensions = FieldSet.emptySet();
         return super.clear();
      }

      public BuilderType clone() {
         return super.clone();
      }

      private void ensureExtensionsIsMutable() {
         if (this.extensions.isImmutable()) {
            this.extensions = this.extensions.clone();
         }

      }

      private void verifyExtensionContainingType(Extension<MessageType, ?> extension) {
         if (extension.getDescriptor().getContainingType() != this.getDescriptorForType()) {
            throw new IllegalArgumentException(
               "Extension is for type \""
                  + extension.getDescriptor().getContainingType().getFullName()
                  + "\" which does not match message type \""
                  + this.getDescriptorForType().getFullName()
                  + "\"."
            );
         }
      }

      @Override
      public final <Type> boolean hasExtension(ExtensionLite<MessageType, Type> extensionLite) {
         Extension<MessageType, Type> extension = GeneratedMessage.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         return this.extensions.hasField(extension.getDescriptor());
      }

      @Override
      public final <Type> int getExtensionCount(ExtensionLite<MessageType, List<Type>> extensionLite) {
         Extension<MessageType, List<Type>> extension = GeneratedMessage.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
         return this.extensions.getRepeatedFieldCount(descriptor);
      }

      @Override
      public final <Type> Type getExtension(ExtensionLite<MessageType, Type> extensionLite) {
         Extension<MessageType, Type> extension = GeneratedMessage.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
         Object value = this.extensions.getField(descriptor);
         if (value == null) {
            if (descriptor.isRepeated()) {
               return (Type)Collections.emptyList();
            } else {
               return (Type)(descriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE
                  ? extension.getMessageDefaultInstance()
                  : extension.fromReflectionType(descriptor.getDefaultValue()));
            }
         } else {
            return (Type)extension.fromReflectionType(value);
         }
      }

      @Override
      public final <Type> Type getExtension(ExtensionLite<MessageType, List<Type>> extensionLite, int index) {
         Extension<MessageType, List<Type>> extension = GeneratedMessage.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
         return (Type)extension.singularFromReflectionType(this.extensions.getRepeatedField(descriptor, index));
      }

      public final <Type> BuilderType setExtension(ExtensionLite<MessageType, Type> extensionLite, Type value) {
         Extension<MessageType, Type> extension = GeneratedMessage.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         this.ensureExtensionsIsMutable();
         Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
         this.extensions.setField(descriptor, extension.toReflectionType(value));
         this.onChanged();
         return (BuilderType)this;
      }

      public final <Type> BuilderType setExtension(ExtensionLite<MessageType, List<Type>> extensionLite, int index, Type value) {
         Extension<MessageType, List<Type>> extension = GeneratedMessage.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         this.ensureExtensionsIsMutable();
         Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
         this.extensions.setRepeatedField(descriptor, index, extension.singularToReflectionType(value));
         this.onChanged();
         return (BuilderType)this;
      }

      public final <Type> BuilderType addExtension(ExtensionLite<MessageType, List<Type>> extensionLite, Type value) {
         Extension<MessageType, List<Type>> extension = GeneratedMessage.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         this.ensureExtensionsIsMutable();
         Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
         this.extensions.addRepeatedField(descriptor, extension.singularToReflectionType(value));
         this.onChanged();
         return (BuilderType)this;
      }

      public final <Type> BuilderType clearExtension(ExtensionLite<MessageType, ?> extensionLite) {
         Extension<MessageType, ?> extension = GeneratedMessage.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         this.ensureExtensionsIsMutable();
         this.extensions.clearField(extension.getDescriptor());
         this.onChanged();
         return (BuilderType)this;
      }

      @Override
      public final <Type> boolean hasExtension(Extension<MessageType, Type> extension) {
         return this.hasExtension(extension);
      }

      @Override
      public final <Type> boolean hasExtension(GeneratedMessage.GeneratedExtension<MessageType, Type> extension) {
         return this.hasExtension(extension);
      }

      @Override
      public final <Type> int getExtensionCount(Extension<MessageType, List<Type>> extension) {
         return this.getExtensionCount(extension);
      }

      @Override
      public final <Type> int getExtensionCount(GeneratedMessage.GeneratedExtension<MessageType, List<Type>> extension) {
         return this.getExtensionCount(extension);
      }

      @Override
      public final <Type> Type getExtension(Extension<MessageType, Type> extension) {
         return this.getExtension(extension);
      }

      @Override
      public final <Type> Type getExtension(GeneratedMessage.GeneratedExtension<MessageType, Type> extension) {
         return this.getExtension(extension);
      }

      @Override
      public final <Type> Type getExtension(Extension<MessageType, List<Type>> extension, int index) {
         return this.getExtension(extension, index);
      }

      @Override
      public final <Type> Type getExtension(GeneratedMessage.GeneratedExtension<MessageType, List<Type>> extension, int index) {
         return this.getExtension(extension, index);
      }

      public final <Type> BuilderType setExtension(Extension<MessageType, Type> extension, Type value) {
         return this.setExtension(extension, value);
      }

      public <Type> BuilderType setExtension(GeneratedMessage.GeneratedExtension<MessageType, Type> extension, Type value) {
         return this.setExtension(extension, value);
      }

      public final <Type> BuilderType setExtension(Extension<MessageType, List<Type>> extension, int index, Type value) {
         return this.setExtension(extension, index, value);
      }

      public <Type> BuilderType setExtension(GeneratedMessage.GeneratedExtension<MessageType, List<Type>> extension, int index, Type value) {
         return this.setExtension(extension, index, value);
      }

      public final <Type> BuilderType addExtension(Extension<MessageType, List<Type>> extension, Type value) {
         return this.addExtension(extension, value);
      }

      public <Type> BuilderType addExtension(GeneratedMessage.GeneratedExtension<MessageType, List<Type>> extension, Type value) {
         return this.addExtension(extension, value);
      }

      public final <Type> BuilderType clearExtension(Extension<MessageType, ?> extension) {
         return this.clearExtension(extension);
      }

      public <Type> BuilderType clearExtension(GeneratedMessage.GeneratedExtension<MessageType, ?> extension) {
         return this.clearExtension(extension);
      }

      protected boolean extensionsAreInitialized() {
         return this.extensions.isInitialized();
      }

      private FieldSet<Descriptors.FieldDescriptor> buildExtensions() {
         this.extensions.makeImmutable();
         return this.extensions;
      }

      @Override
      public boolean isInitialized() {
         return super.isInitialized() && this.extensionsAreInitialized();
      }

      @Override
      protected boolean parseUnknownField(CodedInputStream input, UnknownFieldSet.Builder unknownFields, ExtensionRegistryLite extensionRegistry, int tag) throws IOException {
         return MessageReflection.mergeFieldFrom(
            input, unknownFields, extensionRegistry, this.getDescriptorForType(), new MessageReflection.BuilderAdapter(this), tag
         );
      }

      @Override
      public Map<Descriptors.FieldDescriptor, Object> getAllFields() {
         Map<Descriptors.FieldDescriptor, Object> result = super.getAllFieldsMutable();
         result.putAll(this.extensions.getAllFields());
         return Collections.unmodifiableMap(result);
      }

      @Override
      public Object getField(Descriptors.FieldDescriptor field) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            Object value = this.extensions.getField(field);
            if (value == null) {
               return field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE
                  ? DynamicMessage.getDefaultInstance(field.getMessageType())
                  : field.getDefaultValue();
            } else {
               return value;
            }
         } else {
            return super.getField(field);
         }
      }

      @Override
      public int getRepeatedFieldCount(Descriptors.FieldDescriptor field) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            return this.extensions.getRepeatedFieldCount(field);
         } else {
            return super.getRepeatedFieldCount(field);
         }
      }

      @Override
      public Object getRepeatedField(Descriptors.FieldDescriptor field, int index) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            return this.extensions.getRepeatedField(field, index);
         } else {
            return super.getRepeatedField(field, index);
         }
      }

      @Override
      public boolean hasField(Descriptors.FieldDescriptor field) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            return this.extensions.hasField(field);
         } else {
            return super.hasField(field);
         }
      }

      public BuilderType setField(Descriptors.FieldDescriptor field, Object value) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            this.ensureExtensionsIsMutable();
            this.extensions.setField(field, value);
            this.onChanged();
            return (BuilderType)this;
         } else {
            return super.setField(field, value);
         }
      }

      public BuilderType clearField(Descriptors.FieldDescriptor field) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            this.ensureExtensionsIsMutable();
            this.extensions.clearField(field);
            this.onChanged();
            return (BuilderType)this;
         } else {
            return super.clearField(field);
         }
      }

      public BuilderType setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            this.ensureExtensionsIsMutable();
            this.extensions.setRepeatedField(field, index, value);
            this.onChanged();
            return (BuilderType)this;
         } else {
            return super.setRepeatedField(field, index, value);
         }
      }

      public BuilderType addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            this.ensureExtensionsIsMutable();
            this.extensions.addRepeatedField(field, value);
            this.onChanged();
            return (BuilderType)this;
         } else {
            return super.addRepeatedField(field, value);
         }
      }

      protected final void mergeExtensionFields(GeneratedMessage.ExtendableMessage other) {
         this.ensureExtensionsIsMutable();
         this.extensions.mergeFrom(other.extensions);
         this.onChanged();
      }

      private void verifyContainingType(Descriptors.FieldDescriptor field) {
         if (field.getContainingType() != this.getDescriptorForType()) {
            throw new IllegalArgumentException("FieldDescriptor does not match message type.");
         }
      }
   }

   public abstract static class ExtendableMessage<MessageType extends GeneratedMessage.ExtendableMessage>
      extends GeneratedMessage
      implements GeneratedMessage.ExtendableMessageOrBuilder<MessageType> {
      private static final long serialVersionUID = 1L;
      private final FieldSet<Descriptors.FieldDescriptor> extensions;

      protected ExtendableMessage() {
         this.extensions = FieldSet.newFieldSet();
      }

      protected ExtendableMessage(GeneratedMessage.ExtendableBuilder<MessageType, ?> builder) {
         super(builder);
         this.extensions = builder.buildExtensions();
      }

      private void verifyExtensionContainingType(Extension<MessageType, ?> extension) {
         if (extension.getDescriptor().getContainingType() != this.getDescriptorForType()) {
            throw new IllegalArgumentException(
               "Extension is for type \""
                  + extension.getDescriptor().getContainingType().getFullName()
                  + "\" which does not match message type \""
                  + this.getDescriptorForType().getFullName()
                  + "\"."
            );
         }
      }

      @Override
      public final <Type> boolean hasExtension(ExtensionLite<MessageType, Type> extensionLite) {
         Extension<MessageType, Type> extension = GeneratedMessage.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         return this.extensions.hasField(extension.getDescriptor());
      }

      @Override
      public final <Type> int getExtensionCount(ExtensionLite<MessageType, List<Type>> extensionLite) {
         Extension<MessageType, List<Type>> extension = GeneratedMessage.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
         return this.extensions.getRepeatedFieldCount(descriptor);
      }

      @Override
      public final <Type> Type getExtension(ExtensionLite<MessageType, Type> extensionLite) {
         Extension<MessageType, Type> extension = GeneratedMessage.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
         Object value = this.extensions.getField(descriptor);
         if (value == null) {
            if (descriptor.isRepeated()) {
               return (Type)Collections.emptyList();
            } else {
               return (Type)(descriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE
                  ? extension.getMessageDefaultInstance()
                  : extension.fromReflectionType(descriptor.getDefaultValue()));
            }
         } else {
            return (Type)extension.fromReflectionType(value);
         }
      }

      @Override
      public final <Type> Type getExtension(ExtensionLite<MessageType, List<Type>> extensionLite, int index) {
         Extension<MessageType, List<Type>> extension = GeneratedMessage.checkNotLite(extensionLite);
         this.verifyExtensionContainingType(extension);
         Descriptors.FieldDescriptor descriptor = extension.getDescriptor();
         return (Type)extension.singularFromReflectionType(this.extensions.getRepeatedField(descriptor, index));
      }

      @Override
      public final <Type> boolean hasExtension(Extension<MessageType, Type> extension) {
         return this.hasExtension(extension);
      }

      @Override
      public final <Type> boolean hasExtension(GeneratedMessage.GeneratedExtension<MessageType, Type> extension) {
         return this.hasExtension(extension);
      }

      @Override
      public final <Type> int getExtensionCount(Extension<MessageType, List<Type>> extension) {
         return this.getExtensionCount(extension);
      }

      @Override
      public final <Type> int getExtensionCount(GeneratedMessage.GeneratedExtension<MessageType, List<Type>> extension) {
         return this.getExtensionCount(extension);
      }

      @Override
      public final <Type> Type getExtension(Extension<MessageType, Type> extension) {
         return this.getExtension(extension);
      }

      @Override
      public final <Type> Type getExtension(GeneratedMessage.GeneratedExtension<MessageType, Type> extension) {
         return this.getExtension(extension);
      }

      @Override
      public final <Type> Type getExtension(Extension<MessageType, List<Type>> extension, int index) {
         return this.getExtension(extension, index);
      }

      @Override
      public final <Type> Type getExtension(GeneratedMessage.GeneratedExtension<MessageType, List<Type>> extension, int index) {
         return this.getExtension(extension, index);
      }

      protected boolean extensionsAreInitialized() {
         return this.extensions.isInitialized();
      }

      @Override
      public boolean isInitialized() {
         return super.isInitialized() && this.extensionsAreInitialized();
      }

      @Override
      protected boolean parseUnknownField(CodedInputStream input, UnknownFieldSet.Builder unknownFields, ExtensionRegistryLite extensionRegistry, int tag) throws IOException {
         return MessageReflection.mergeFieldFrom(
            input, unknownFields, extensionRegistry, this.getDescriptorForType(), new MessageReflection.ExtensionAdapter(this.extensions), tag
         );
      }

      @Override
      protected void makeExtensionsImmutable() {
         this.extensions.makeImmutable();
      }

      protected GeneratedMessage.ExtendableMessage<MessageType>.ExtensionWriter newExtensionWriter() {
         return new GeneratedMessage.ExtendableMessage.ExtensionWriter(false);
      }

      protected GeneratedMessage.ExtendableMessage<MessageType>.ExtensionWriter newMessageSetExtensionWriter() {
         return new GeneratedMessage.ExtendableMessage.ExtensionWriter(true);
      }

      protected int extensionsSerializedSize() {
         return this.extensions.getSerializedSize();
      }

      protected int extensionsSerializedSizeAsMessageSet() {
         return this.extensions.getMessageSetSerializedSize();
      }

      protected Map<Descriptors.FieldDescriptor, Object> getExtensionFields() {
         return this.extensions.getAllFields();
      }

      @Override
      public Map<Descriptors.FieldDescriptor, Object> getAllFields() {
         Map<Descriptors.FieldDescriptor, Object> result = super.getAllFieldsMutable(false);
         result.putAll(this.getExtensionFields());
         return Collections.unmodifiableMap(result);
      }

      @Override
      public Map<Descriptors.FieldDescriptor, Object> getAllFieldsRaw() {
         Map<Descriptors.FieldDescriptor, Object> result = super.getAllFieldsMutable(false);
         result.putAll(this.getExtensionFields());
         return Collections.unmodifiableMap(result);
      }

      @Override
      public boolean hasField(Descriptors.FieldDescriptor field) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            return this.extensions.hasField(field);
         } else {
            return super.hasField(field);
         }
      }

      @Override
      public Object getField(Descriptors.FieldDescriptor field) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            Object value = this.extensions.getField(field);
            if (value == null) {
               if (field.isRepeated()) {
                  return Collections.emptyList();
               } else {
                  return field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE
                     ? DynamicMessage.getDefaultInstance(field.getMessageType())
                     : field.getDefaultValue();
               }
            } else {
               return value;
            }
         } else {
            return super.getField(field);
         }
      }

      @Override
      public int getRepeatedFieldCount(Descriptors.FieldDescriptor field) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            return this.extensions.getRepeatedFieldCount(field);
         } else {
            return super.getRepeatedFieldCount(field);
         }
      }

      @Override
      public Object getRepeatedField(Descriptors.FieldDescriptor field, int index) {
         if (field.isExtension()) {
            this.verifyContainingType(field);
            return this.extensions.getRepeatedField(field, index);
         } else {
            return super.getRepeatedField(field, index);
         }
      }

      private void verifyContainingType(Descriptors.FieldDescriptor field) {
         if (field.getContainingType() != this.getDescriptorForType()) {
            throw new IllegalArgumentException("FieldDescriptor does not match message type.");
         }
      }

      protected class ExtensionWriter {
         private final Iterator<Entry<Descriptors.FieldDescriptor, Object>> iter = ExtendableMessage.this.extensions.iterator();
         private Entry<Descriptors.FieldDescriptor, Object> next;
         private final boolean messageSetWireFormat;

         private ExtensionWriter(boolean messageSetWireFormat) {
            if (this.iter.hasNext()) {
               this.next = (Entry)this.iter.next();
            }

            this.messageSetWireFormat = messageSetWireFormat;
         }

         public void writeUntil(int end, CodedOutputStream output) throws IOException {
            while(this.next != null && ((Descriptors.FieldDescriptor)this.next.getKey()).getNumber() < end) {
               Descriptors.FieldDescriptor descriptor = (Descriptors.FieldDescriptor)this.next.getKey();
               if (!this.messageSetWireFormat || descriptor.getLiteJavaType() != WireFormat.JavaType.MESSAGE || descriptor.isRepeated()) {
                  FieldSet.writeField(descriptor, this.next.getValue(), output);
               } else if (this.next instanceof LazyField.LazyEntry) {
                  output.writeRawMessageSetExtension(descriptor.getNumber(), ((LazyField.LazyEntry)this.next).getField().toByteString());
               } else {
                  output.writeMessageSetExtension(descriptor.getNumber(), (Message)this.next.getValue());
               }

               if (this.iter.hasNext()) {
                  this.next = (Entry)this.iter.next();
               } else {
                  this.next = null;
               }
            }

         }
      }
   }

   public interface ExtendableMessageOrBuilder<MessageType extends GeneratedMessage.ExtendableMessage> extends MessageOrBuilder {
      @Override
      Message getDefaultInstanceForType();

      <Type> boolean hasExtension(ExtensionLite<MessageType, Type> var1);

      <Type> int getExtensionCount(ExtensionLite<MessageType, List<Type>> var1);

      <Type> Type getExtension(ExtensionLite<MessageType, Type> var1);

      <Type> Type getExtension(ExtensionLite<MessageType, List<Type>> var1, int var2);

      <Type> boolean hasExtension(Extension<MessageType, Type> var1);

      <Type> boolean hasExtension(GeneratedMessage.GeneratedExtension<MessageType, Type> var1);

      <Type> int getExtensionCount(Extension<MessageType, List<Type>> var1);

      <Type> int getExtensionCount(GeneratedMessage.GeneratedExtension<MessageType, List<Type>> var1);

      <Type> Type getExtension(Extension<MessageType, Type> var1);

      <Type> Type getExtension(GeneratedMessage.GeneratedExtension<MessageType, Type> var1);

      <Type> Type getExtension(Extension<MessageType, List<Type>> var1, int var2);

      <Type> Type getExtension(GeneratedMessage.GeneratedExtension<MessageType, List<Type>> var1, int var2);
   }

   interface ExtensionDescriptorRetriever {
      Descriptors.FieldDescriptor getDescriptor();
   }

   public static final class FieldAccessorTable {
      private final Descriptors.Descriptor descriptor;
      private final GeneratedMessage.FieldAccessorTable.FieldAccessor[] fields;
      private String[] camelCaseNames;
      private final GeneratedMessage.FieldAccessorTable.OneofAccessor[] oneofs;
      private volatile boolean initialized;

      public FieldAccessorTable(
         Descriptors.Descriptor descriptor,
         String[] camelCaseNames,
         Class<? extends GeneratedMessage> messageClass,
         Class<? extends GeneratedMessage.Builder> builderClass
      ) {
         this(descriptor, camelCaseNames);
         this.ensureFieldAccessorsInitialized(messageClass, builderClass);
      }

      public FieldAccessorTable(Descriptors.Descriptor descriptor, String[] camelCaseNames) {
         this.descriptor = descriptor;
         this.camelCaseNames = camelCaseNames;
         this.fields = new GeneratedMessage.FieldAccessorTable.FieldAccessor[descriptor.getFields().size()];
         this.oneofs = new GeneratedMessage.FieldAccessorTable.OneofAccessor[descriptor.getOneofs().size()];
         this.initialized = false;
      }

      private boolean isMapFieldEnabled(Descriptors.FieldDescriptor field) {
         return true;
      }

      public GeneratedMessage.FieldAccessorTable ensureFieldAccessorsInitialized(
         Class<? extends GeneratedMessage> messageClass, Class<? extends GeneratedMessage.Builder> builderClass
      ) {
         if (this.initialized) {
            return this;
         } else {
            synchronized(this) {
               if (this.initialized) {
                  return this;
               } else {
                  int fieldsSize = this.fields.length;

                  for(int i = 0; i < fieldsSize; ++i) {
                     Descriptors.FieldDescriptor field = (Descriptors.FieldDescriptor)this.descriptor.getFields().get(i);
                     String containingOneofCamelCaseName = null;
                     if (field.getContainingOneof() != null) {
                        containingOneofCamelCaseName = this.camelCaseNames[fieldsSize + field.getContainingOneof().getIndex()];
                     }

                     if (field.isRepeated()) {
                        if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                           if (field.isMapField() && this.isMapFieldEnabled(field)) {
                              this.fields[i] = new GeneratedMessage.FieldAccessorTable.MapFieldAccessor(
                                 field, this.camelCaseNames[i], messageClass, builderClass
                              );
                           } else {
                              this.fields[i] = new GeneratedMessage.FieldAccessorTable.RepeatedMessageFieldAccessor(
                                 field, this.camelCaseNames[i], messageClass, builderClass
                              );
                           }
                        } else if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.ENUM) {
                           this.fields[i] = new GeneratedMessage.FieldAccessorTable.RepeatedEnumFieldAccessor(
                              field, this.camelCaseNames[i], messageClass, builderClass
                           );
                        } else {
                           this.fields[i] = new GeneratedMessage.FieldAccessorTable.RepeatedFieldAccessor(
                              field, this.camelCaseNames[i], messageClass, builderClass
                           );
                        }
                     } else if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                        this.fields[i] = new GeneratedMessage.FieldAccessorTable.SingularMessageFieldAccessor(
                           field, this.camelCaseNames[i], messageClass, builderClass, containingOneofCamelCaseName
                        );
                     } else if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.ENUM) {
                        this.fields[i] = new GeneratedMessage.FieldAccessorTable.SingularEnumFieldAccessor(
                           field, this.camelCaseNames[i], messageClass, builderClass, containingOneofCamelCaseName
                        );
                     } else if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.STRING) {
                        this.fields[i] = new GeneratedMessage.FieldAccessorTable.SingularStringFieldAccessor(
                           field, this.camelCaseNames[i], messageClass, builderClass, containingOneofCamelCaseName
                        );
                     } else {
                        this.fields[i] = new GeneratedMessage.FieldAccessorTable.SingularFieldAccessor(
                           field, this.camelCaseNames[i], messageClass, builderClass, containingOneofCamelCaseName
                        );
                     }
                  }

                  int oneofsSize = this.oneofs.length;

                  for(int i = 0; i < oneofsSize; ++i) {
                     this.oneofs[i] = new GeneratedMessage.FieldAccessorTable.OneofAccessor(
                        this.descriptor, this.camelCaseNames[i + fieldsSize], messageClass, builderClass
                     );
                  }

                  this.initialized = true;
                  this.camelCaseNames = null;
                  return this;
               }
            }
         }
      }

      private GeneratedMessage.FieldAccessorTable.FieldAccessor getField(Descriptors.FieldDescriptor field) {
         if (field.getContainingType() != this.descriptor) {
            throw new IllegalArgumentException("FieldDescriptor does not match message type.");
         } else if (field.isExtension()) {
            throw new IllegalArgumentException("This type does not have extensions.");
         } else {
            return this.fields[field.getIndex()];
         }
      }

      private GeneratedMessage.FieldAccessorTable.OneofAccessor getOneof(Descriptors.OneofDescriptor oneof) {
         if (oneof.getContainingType() != this.descriptor) {
            throw new IllegalArgumentException("OneofDescriptor does not match message type.");
         } else {
            return this.oneofs[oneof.getIndex()];
         }
      }

      private static boolean supportFieldPresence(Descriptors.FileDescriptor file) {
         return file.getSyntax() == Descriptors.FileDescriptor.Syntax.PROTO2;
      }

      private interface FieldAccessor {
         Object get(GeneratedMessage var1);

         Object get(GeneratedMessage.Builder var1);

         Object getRaw(GeneratedMessage var1);

         Object getRaw(GeneratedMessage.Builder var1);

         void set(GeneratedMessage.Builder var1, Object var2);

         Object getRepeated(GeneratedMessage var1, int var2);

         Object getRepeated(GeneratedMessage.Builder var1, int var2);

         Object getRepeatedRaw(GeneratedMessage var1, int var2);

         Object getRepeatedRaw(GeneratedMessage.Builder var1, int var2);

         void setRepeated(GeneratedMessage.Builder var1, int var2, Object var3);

         void addRepeated(GeneratedMessage.Builder var1, Object var2);

         boolean has(GeneratedMessage var1);

         boolean has(GeneratedMessage.Builder var1);

         int getRepeatedCount(GeneratedMessage var1);

         int getRepeatedCount(GeneratedMessage.Builder var1);

         void clear(GeneratedMessage.Builder var1);

         Message.Builder newBuilder();

         Message.Builder getBuilder(GeneratedMessage.Builder var1);

         Message.Builder getRepeatedBuilder(GeneratedMessage.Builder var1, int var2);
      }

      private static class MapFieldAccessor implements GeneratedMessage.FieldAccessorTable.FieldAccessor {
         private final Descriptors.FieldDescriptor field;
         private final Message mapEntryMessageDefaultInstance;

         MapFieldAccessor(
            Descriptors.FieldDescriptor descriptor,
            String camelCaseName,
            Class<? extends GeneratedMessage> messageClass,
            Class<? extends GeneratedMessage.Builder> builderClass
         ) {
            this.field = descriptor;
            java.lang.reflect.Method getDefaultInstanceMethod = GeneratedMessage.getMethodOrDie(messageClass, "getDefaultInstance");
            MapField defaultMapField = this.getMapField((GeneratedMessage)GeneratedMessage.invokeOrDie(getDefaultInstanceMethod, null));
            this.mapEntryMessageDefaultInstance = defaultMapField.getMapEntryMessageDefaultInstance();
         }

         private MapField<?, ?> getMapField(GeneratedMessage message) {
            return message.internalGetMapField(this.field.getNumber());
         }

         private MapField<?, ?> getMapField(GeneratedMessage.Builder builder) {
            return builder.internalGetMapField(this.field.getNumber());
         }

         private MapField<?, ?> getMutableMapField(GeneratedMessage.Builder builder) {
            return builder.internalGetMutableMapField(this.field.getNumber());
         }

         @Override
         public Object get(GeneratedMessage message) {
            List result = new ArrayList();

            for(int i = 0; i < this.getRepeatedCount(message); ++i) {
               result.add(this.getRepeated(message, i));
            }

            return Collections.unmodifiableList(result);
         }

         @Override
         public Object get(GeneratedMessage.Builder builder) {
            List result = new ArrayList();

            for(int i = 0; i < this.getRepeatedCount(builder); ++i) {
               result.add(this.getRepeated(builder, i));
            }

            return Collections.unmodifiableList(result);
         }

         @Override
         public Object getRaw(GeneratedMessage message) {
            return this.get(message);
         }

         @Override
         public Object getRaw(GeneratedMessage.Builder builder) {
            return this.get(builder);
         }

         @Override
         public void set(GeneratedMessage.Builder builder, Object value) {
            this.clear(builder);

            for(Object entry : (List)value) {
               this.addRepeated(builder, entry);
            }

         }

         @Override
         public Object getRepeated(GeneratedMessage message, int index) {
            return this.getMapField(message).getList().get(index);
         }

         @Override
         public Object getRepeated(GeneratedMessage.Builder builder, int index) {
            return this.getMapField(builder).getList().get(index);
         }

         @Override
         public Object getRepeatedRaw(GeneratedMessage message, int index) {
            return this.getRepeated(message, index);
         }

         @Override
         public Object getRepeatedRaw(GeneratedMessage.Builder builder, int index) {
            return this.getRepeated(builder, index);
         }

         @Override
         public void setRepeated(GeneratedMessage.Builder builder, int index, Object value) {
            this.getMutableMapField(builder).getMutableList().set(index, (Message)value);
         }

         @Override
         public void addRepeated(GeneratedMessage.Builder builder, Object value) {
            this.getMutableMapField(builder).getMutableList().add((Message)value);
         }

         @Override
         public boolean has(GeneratedMessage message) {
            throw new UnsupportedOperationException("hasField() is not supported for repeated fields.");
         }

         @Override
         public boolean has(GeneratedMessage.Builder builder) {
            throw new UnsupportedOperationException("hasField() is not supported for repeated fields.");
         }

         @Override
         public int getRepeatedCount(GeneratedMessage message) {
            return this.getMapField(message).getList().size();
         }

         @Override
         public int getRepeatedCount(GeneratedMessage.Builder builder) {
            return this.getMapField(builder).getList().size();
         }

         @Override
         public void clear(GeneratedMessage.Builder builder) {
            this.getMutableMapField(builder).getMutableList().clear();
         }

         @Override
         public Message.Builder newBuilder() {
            return this.mapEntryMessageDefaultInstance.newBuilderForType();
         }

         @Override
         public Message.Builder getBuilder(GeneratedMessage.Builder builder) {
            throw new UnsupportedOperationException("Nested builder not supported for map fields.");
         }

         @Override
         public Message.Builder getRepeatedBuilder(GeneratedMessage.Builder builder, int index) {
            throw new UnsupportedOperationException("Nested builder not supported for map fields.");
         }
      }

      private static class OneofAccessor {
         private final Descriptors.Descriptor descriptor;
         private final java.lang.reflect.Method caseMethod;
         private final java.lang.reflect.Method caseMethodBuilder;
         private final java.lang.reflect.Method clearMethod;

         OneofAccessor(
            Descriptors.Descriptor descriptor,
            String camelCaseName,
            Class<? extends GeneratedMessage> messageClass,
            Class<? extends GeneratedMessage.Builder> builderClass
         ) {
            this.descriptor = descriptor;
            this.caseMethod = GeneratedMessage.getMethodOrDie(messageClass, "get" + camelCaseName + "Case");
            this.caseMethodBuilder = GeneratedMessage.getMethodOrDie(builderClass, "get" + camelCaseName + "Case");
            this.clearMethod = GeneratedMessage.getMethodOrDie(builderClass, "clear" + camelCaseName);
         }

         public boolean has(GeneratedMessage message) {
            return ((Internal.EnumLite)GeneratedMessage.invokeOrDie(this.caseMethod, message)).getNumber() != 0;
         }

         public boolean has(GeneratedMessage.Builder builder) {
            return ((Internal.EnumLite)GeneratedMessage.invokeOrDie(this.caseMethodBuilder, builder)).getNumber() != 0;
         }

         public Descriptors.FieldDescriptor get(GeneratedMessage message) {
            int fieldNumber = ((Internal.EnumLite)GeneratedMessage.invokeOrDie(this.caseMethod, message)).getNumber();
            return fieldNumber > 0 ? this.descriptor.findFieldByNumber(fieldNumber) : null;
         }

         public Descriptors.FieldDescriptor get(GeneratedMessage.Builder builder) {
            int fieldNumber = ((Internal.EnumLite)GeneratedMessage.invokeOrDie(this.caseMethodBuilder, builder)).getNumber();
            return fieldNumber > 0 ? this.descriptor.findFieldByNumber(fieldNumber) : null;
         }

         public void clear(GeneratedMessage.Builder builder) {
            GeneratedMessage.invokeOrDie(this.clearMethod, builder);
         }
      }

      private static final class RepeatedEnumFieldAccessor extends GeneratedMessage.FieldAccessorTable.RepeatedFieldAccessor {
         private Descriptors.EnumDescriptor enumDescriptor;
         private final java.lang.reflect.Method valueOfMethod;
         private final java.lang.reflect.Method getValueDescriptorMethod;
         private boolean supportUnknownEnumValue;
         private java.lang.reflect.Method getRepeatedValueMethod;
         private java.lang.reflect.Method getRepeatedValueMethodBuilder;
         private java.lang.reflect.Method setRepeatedValueMethod;
         private java.lang.reflect.Method addRepeatedValueMethod;

         RepeatedEnumFieldAccessor(
            Descriptors.FieldDescriptor descriptor,
            String camelCaseName,
            Class<? extends GeneratedMessage> messageClass,
            Class<? extends GeneratedMessage.Builder> builderClass
         ) {
            super(descriptor, camelCaseName, messageClass, builderClass);
            this.enumDescriptor = descriptor.getEnumType();
            this.valueOfMethod = GeneratedMessage.getMethodOrDie(this.type, "valueOf", Descriptors.EnumValueDescriptor.class);
            this.getValueDescriptorMethod = GeneratedMessage.getMethodOrDie(this.type, "getValueDescriptor");
            this.supportUnknownEnumValue = descriptor.getFile().supportsUnknownEnumValue();
            if (this.supportUnknownEnumValue) {
               this.getRepeatedValueMethod = GeneratedMessage.getMethodOrDie(messageClass, "get" + camelCaseName + "Value", Integer.TYPE);
               this.getRepeatedValueMethodBuilder = GeneratedMessage.getMethodOrDie(builderClass, "get" + camelCaseName + "Value", Integer.TYPE);
               this.setRepeatedValueMethod = GeneratedMessage.getMethodOrDie(builderClass, "set" + camelCaseName + "Value", Integer.TYPE, Integer.TYPE);
               this.addRepeatedValueMethod = GeneratedMessage.getMethodOrDie(builderClass, "add" + camelCaseName + "Value", Integer.TYPE);
            }

         }

         @Override
         public Object get(GeneratedMessage message) {
            List newList = new ArrayList();
            int size = this.getRepeatedCount(message);

            for(int i = 0; i < size; ++i) {
               newList.add(this.getRepeated(message, i));
            }

            return Collections.unmodifiableList(newList);
         }

         @Override
         public Object get(GeneratedMessage.Builder builder) {
            List newList = new ArrayList();
            int size = this.getRepeatedCount(builder);

            for(int i = 0; i < size; ++i) {
               newList.add(this.getRepeated(builder, i));
            }

            return Collections.unmodifiableList(newList);
         }

         @Override
         public Object getRepeated(GeneratedMessage message, int index) {
            if (this.supportUnknownEnumValue) {
               int value = GeneratedMessage.invokeOrDie(this.getRepeatedValueMethod, message, index);
               return this.enumDescriptor.findValueByNumberCreatingIfUnknown(value);
            } else {
               return GeneratedMessage.invokeOrDie(this.getValueDescriptorMethod, super.getRepeated(message, index));
            }
         }

         @Override
         public Object getRepeated(GeneratedMessage.Builder builder, int index) {
            if (this.supportUnknownEnumValue) {
               int value = GeneratedMessage.invokeOrDie(this.getRepeatedValueMethodBuilder, builder, index);
               return this.enumDescriptor.findValueByNumberCreatingIfUnknown(value);
            } else {
               return GeneratedMessage.invokeOrDie(this.getValueDescriptorMethod, super.getRepeated(builder, index));
            }
         }

         @Override
         public void setRepeated(GeneratedMessage.Builder builder, int index, Object value) {
            if (this.supportUnknownEnumValue) {
               GeneratedMessage.invokeOrDie(this.setRepeatedValueMethod, builder, index, ((Descriptors.EnumValueDescriptor)value).getNumber());
            } else {
               super.setRepeated(builder, index, GeneratedMessage.invokeOrDie(this.valueOfMethod, null, value));
            }
         }

         @Override
         public void addRepeated(GeneratedMessage.Builder builder, Object value) {
            if (this.supportUnknownEnumValue) {
               GeneratedMessage.invokeOrDie(this.addRepeatedValueMethod, builder, ((Descriptors.EnumValueDescriptor)value).getNumber());
            } else {
               super.addRepeated(builder, GeneratedMessage.invokeOrDie(this.valueOfMethod, null, value));
            }
         }
      }

      private static class RepeatedFieldAccessor implements GeneratedMessage.FieldAccessorTable.FieldAccessor {
         protected final Class type;
         protected final java.lang.reflect.Method getMethod;
         protected final java.lang.reflect.Method getMethodBuilder;
         protected final java.lang.reflect.Method getRepeatedMethod;
         protected final java.lang.reflect.Method getRepeatedMethodBuilder;
         protected final java.lang.reflect.Method setRepeatedMethod;
         protected final java.lang.reflect.Method addRepeatedMethod;
         protected final java.lang.reflect.Method getCountMethod;
         protected final java.lang.reflect.Method getCountMethodBuilder;
         protected final java.lang.reflect.Method clearMethod;

         RepeatedFieldAccessor(
            Descriptors.FieldDescriptor descriptor,
            String camelCaseName,
            Class<? extends GeneratedMessage> messageClass,
            Class<? extends GeneratedMessage.Builder> builderClass
         ) {
            this.getMethod = GeneratedMessage.getMethodOrDie(messageClass, "get" + camelCaseName + "List");
            this.getMethodBuilder = GeneratedMessage.getMethodOrDie(builderClass, "get" + camelCaseName + "List");
            this.getRepeatedMethod = GeneratedMessage.getMethodOrDie(messageClass, "get" + camelCaseName, Integer.TYPE);
            this.getRepeatedMethodBuilder = GeneratedMessage.getMethodOrDie(builderClass, "get" + camelCaseName, Integer.TYPE);
            this.type = this.getRepeatedMethod.getReturnType();
            this.setRepeatedMethod = GeneratedMessage.getMethodOrDie(builderClass, "set" + camelCaseName, Integer.TYPE, this.type);
            this.addRepeatedMethod = GeneratedMessage.getMethodOrDie(builderClass, "add" + camelCaseName, this.type);
            this.getCountMethod = GeneratedMessage.getMethodOrDie(messageClass, "get" + camelCaseName + "Count");
            this.getCountMethodBuilder = GeneratedMessage.getMethodOrDie(builderClass, "get" + camelCaseName + "Count");
            this.clearMethod = GeneratedMessage.getMethodOrDie(builderClass, "clear" + camelCaseName);
         }

         @Override
         public Object get(GeneratedMessage message) {
            return GeneratedMessage.invokeOrDie(this.getMethod, message);
         }

         @Override
         public Object get(GeneratedMessage.Builder builder) {
            return GeneratedMessage.invokeOrDie(this.getMethodBuilder, builder);
         }

         @Override
         public Object getRaw(GeneratedMessage message) {
            return this.get(message);
         }

         @Override
         public Object getRaw(GeneratedMessage.Builder builder) {
            return this.get(builder);
         }

         @Override
         public void set(GeneratedMessage.Builder builder, Object value) {
            this.clear(builder);

            for(Object element : (List)value) {
               this.addRepeated(builder, element);
            }

         }

         @Override
         public Object getRepeated(GeneratedMessage message, int index) {
            return GeneratedMessage.invokeOrDie(this.getRepeatedMethod, message, index);
         }

         @Override
         public Object getRepeated(GeneratedMessage.Builder builder, int index) {
            return GeneratedMessage.invokeOrDie(this.getRepeatedMethodBuilder, builder, index);
         }

         @Override
         public Object getRepeatedRaw(GeneratedMessage message, int index) {
            return this.getRepeated(message, index);
         }

         @Override
         public Object getRepeatedRaw(GeneratedMessage.Builder builder, int index) {
            return this.getRepeated(builder, index);
         }

         @Override
         public void setRepeated(GeneratedMessage.Builder builder, int index, Object value) {
            GeneratedMessage.invokeOrDie(this.setRepeatedMethod, builder, index, value);
         }

         @Override
         public void addRepeated(GeneratedMessage.Builder builder, Object value) {
            GeneratedMessage.invokeOrDie(this.addRepeatedMethod, builder, value);
         }

         @Override
         public boolean has(GeneratedMessage message) {
            throw new UnsupportedOperationException("hasField() called on a repeated field.");
         }

         @Override
         public boolean has(GeneratedMessage.Builder builder) {
            throw new UnsupportedOperationException("hasField() called on a repeated field.");
         }

         @Override
         public int getRepeatedCount(GeneratedMessage message) {
            return GeneratedMessage.invokeOrDie(this.getCountMethod, message);
         }

         @Override
         public int getRepeatedCount(GeneratedMessage.Builder builder) {
            return GeneratedMessage.invokeOrDie(this.getCountMethodBuilder, builder);
         }

         @Override
         public void clear(GeneratedMessage.Builder builder) {
            GeneratedMessage.invokeOrDie(this.clearMethod, builder);
         }

         @Override
         public Message.Builder newBuilder() {
            throw new UnsupportedOperationException("newBuilderForField() called on a non-Message type.");
         }

         @Override
         public Message.Builder getBuilder(GeneratedMessage.Builder builder) {
            throw new UnsupportedOperationException("getFieldBuilder() called on a non-Message type.");
         }

         @Override
         public Message.Builder getRepeatedBuilder(GeneratedMessage.Builder builder, int index) {
            throw new UnsupportedOperationException("getRepeatedFieldBuilder() called on a non-Message type.");
         }
      }

      private static final class RepeatedMessageFieldAccessor extends GeneratedMessage.FieldAccessorTable.RepeatedFieldAccessor {
         private final java.lang.reflect.Method newBuilderMethod = GeneratedMessage.getMethodOrDie(this.type, "newBuilder");
         private final java.lang.reflect.Method getBuilderMethodBuilder;

         RepeatedMessageFieldAccessor(
            Descriptors.FieldDescriptor descriptor,
            String camelCaseName,
            Class<? extends GeneratedMessage> messageClass,
            Class<? extends GeneratedMessage.Builder> builderClass
         ) {
            super(descriptor, camelCaseName, messageClass, builderClass);
            this.getBuilderMethodBuilder = GeneratedMessage.getMethodOrDie(builderClass, "get" + camelCaseName + "Builder", Integer.TYPE);
         }

         private Object coerceType(Object value) {
            return this.type.isInstance(value)
               ? value
               : ((Message.Builder)GeneratedMessage.invokeOrDie(this.newBuilderMethod, null)).mergeFrom((Message)value).build();
         }

         @Override
         public void setRepeated(GeneratedMessage.Builder builder, int index, Object value) {
            super.setRepeated(builder, index, this.coerceType(value));
         }

         @Override
         public void addRepeated(GeneratedMessage.Builder builder, Object value) {
            super.addRepeated(builder, this.coerceType(value));
         }

         @Override
         public Message.Builder newBuilder() {
            return (Message.Builder)GeneratedMessage.invokeOrDie(this.newBuilderMethod, null);
         }

         @Override
         public Message.Builder getRepeatedBuilder(GeneratedMessage.Builder builder, int index) {
            return (Message.Builder)GeneratedMessage.invokeOrDie(this.getBuilderMethodBuilder, builder, index);
         }
      }

      private static final class SingularEnumFieldAccessor extends GeneratedMessage.FieldAccessorTable.SingularFieldAccessor {
         private Descriptors.EnumDescriptor enumDescriptor;
         private java.lang.reflect.Method valueOfMethod;
         private java.lang.reflect.Method getValueDescriptorMethod;
         private boolean supportUnknownEnumValue;
         private java.lang.reflect.Method getValueMethod;
         private java.lang.reflect.Method getValueMethodBuilder;
         private java.lang.reflect.Method setValueMethod;

         SingularEnumFieldAccessor(
            Descriptors.FieldDescriptor descriptor,
            String camelCaseName,
            Class<? extends GeneratedMessage> messageClass,
            Class<? extends GeneratedMessage.Builder> builderClass,
            String containingOneofCamelCaseName
         ) {
            super(descriptor, camelCaseName, messageClass, builderClass, containingOneofCamelCaseName);
            this.enumDescriptor = descriptor.getEnumType();
            this.valueOfMethod = GeneratedMessage.getMethodOrDie(this.type, "valueOf", Descriptors.EnumValueDescriptor.class);
            this.getValueDescriptorMethod = GeneratedMessage.getMethodOrDie(this.type, "getValueDescriptor");
            this.supportUnknownEnumValue = descriptor.getFile().supportsUnknownEnumValue();
            if (this.supportUnknownEnumValue) {
               this.getValueMethod = GeneratedMessage.getMethodOrDie(messageClass, "get" + camelCaseName + "Value");
               this.getValueMethodBuilder = GeneratedMessage.getMethodOrDie(builderClass, "get" + camelCaseName + "Value");
               this.setValueMethod = GeneratedMessage.getMethodOrDie(builderClass, "set" + camelCaseName + "Value", Integer.TYPE);
            }

         }

         @Override
         public Object get(GeneratedMessage message) {
            if (this.supportUnknownEnumValue) {
               int value = GeneratedMessage.invokeOrDie(this.getValueMethod, message);
               return this.enumDescriptor.findValueByNumberCreatingIfUnknown(value);
            } else {
               return GeneratedMessage.invokeOrDie(this.getValueDescriptorMethod, super.get(message));
            }
         }

         @Override
         public Object get(GeneratedMessage.Builder builder) {
            if (this.supportUnknownEnumValue) {
               int value = GeneratedMessage.invokeOrDie(this.getValueMethodBuilder, builder);
               return this.enumDescriptor.findValueByNumberCreatingIfUnknown(value);
            } else {
               return GeneratedMessage.invokeOrDie(this.getValueDescriptorMethod, super.get(builder));
            }
         }

         @Override
         public void set(GeneratedMessage.Builder builder, Object value) {
            if (this.supportUnknownEnumValue) {
               GeneratedMessage.invokeOrDie(this.setValueMethod, builder, ((Descriptors.EnumValueDescriptor)value).getNumber());
            } else {
               super.set(builder, GeneratedMessage.invokeOrDie(this.valueOfMethod, null, value));
            }
         }
      }

      private static class SingularFieldAccessor implements GeneratedMessage.FieldAccessorTable.FieldAccessor {
         protected final Class<?> type;
         protected final java.lang.reflect.Method getMethod;
         protected final java.lang.reflect.Method getMethodBuilder;
         protected final java.lang.reflect.Method setMethod;
         protected final java.lang.reflect.Method hasMethod;
         protected final java.lang.reflect.Method hasMethodBuilder;
         protected final java.lang.reflect.Method clearMethod;
         protected final java.lang.reflect.Method caseMethod;
         protected final java.lang.reflect.Method caseMethodBuilder;
         protected final Descriptors.FieldDescriptor field;
         protected final boolean isOneofField;
         protected final boolean hasHasMethod;

         SingularFieldAccessor(
            Descriptors.FieldDescriptor descriptor,
            String camelCaseName,
            Class<? extends GeneratedMessage> messageClass,
            Class<? extends GeneratedMessage.Builder> builderClass,
            String containingOneofCamelCaseName
         ) {
            this.field = descriptor;
            this.isOneofField = descriptor.getContainingOneof() != null;
            this.hasHasMethod = GeneratedMessage.FieldAccessorTable.supportFieldPresence(descriptor.getFile())
               || !this.isOneofField && descriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE;
            this.getMethod = GeneratedMessage.getMethodOrDie(messageClass, "get" + camelCaseName);
            this.getMethodBuilder = GeneratedMessage.getMethodOrDie(builderClass, "get" + camelCaseName);
            this.type = this.getMethod.getReturnType();
            this.setMethod = GeneratedMessage.getMethodOrDie(builderClass, "set" + camelCaseName, this.type);
            this.hasMethod = this.hasHasMethod ? GeneratedMessage.getMethodOrDie(messageClass, "has" + camelCaseName) : null;
            this.hasMethodBuilder = this.hasHasMethod ? GeneratedMessage.getMethodOrDie(builderClass, "has" + camelCaseName) : null;
            this.clearMethod = GeneratedMessage.getMethodOrDie(builderClass, "clear" + camelCaseName);
            this.caseMethod = this.isOneofField ? GeneratedMessage.getMethodOrDie(messageClass, "get" + containingOneofCamelCaseName + "Case") : null;
            this.caseMethodBuilder = this.isOneofField ? GeneratedMessage.getMethodOrDie(builderClass, "get" + containingOneofCamelCaseName + "Case") : null;
         }

         private int getOneofFieldNumber(GeneratedMessage message) {
            return ((Internal.EnumLite)GeneratedMessage.invokeOrDie(this.caseMethod, message)).getNumber();
         }

         private int getOneofFieldNumber(GeneratedMessage.Builder builder) {
            return ((Internal.EnumLite)GeneratedMessage.invokeOrDie(this.caseMethodBuilder, builder)).getNumber();
         }

         @Override
         public Object get(GeneratedMessage message) {
            return GeneratedMessage.invokeOrDie(this.getMethod, message);
         }

         @Override
         public Object get(GeneratedMessage.Builder builder) {
            return GeneratedMessage.invokeOrDie(this.getMethodBuilder, builder);
         }

         @Override
         public Object getRaw(GeneratedMessage message) {
            return this.get(message);
         }

         @Override
         public Object getRaw(GeneratedMessage.Builder builder) {
            return this.get(builder);
         }

         @Override
         public void set(GeneratedMessage.Builder builder, Object value) {
            GeneratedMessage.invokeOrDie(this.setMethod, builder, value);
         }

         @Override
         public Object getRepeated(GeneratedMessage message, int index) {
            throw new UnsupportedOperationException("getRepeatedField() called on a singular field.");
         }

         @Override
         public Object getRepeatedRaw(GeneratedMessage message, int index) {
            throw new UnsupportedOperationException("getRepeatedFieldRaw() called on a singular field.");
         }

         @Override
         public Object getRepeated(GeneratedMessage.Builder builder, int index) {
            throw new UnsupportedOperationException("getRepeatedField() called on a singular field.");
         }

         @Override
         public Object getRepeatedRaw(GeneratedMessage.Builder builder, int index) {
            throw new UnsupportedOperationException("getRepeatedFieldRaw() called on a singular field.");
         }

         @Override
         public void setRepeated(GeneratedMessage.Builder builder, int index, Object value) {
            throw new UnsupportedOperationException("setRepeatedField() called on a singular field.");
         }

         @Override
         public void addRepeated(GeneratedMessage.Builder builder, Object value) {
            throw new UnsupportedOperationException("addRepeatedField() called on a singular field.");
         }

         @Override
         public boolean has(GeneratedMessage message) {
            if (!this.hasHasMethod) {
               if (this.isOneofField) {
                  return this.getOneofFieldNumber(message) == this.field.getNumber();
               } else {
                  return !this.get(message).equals(this.field.getDefaultValue());
               }
            } else {
               return GeneratedMessage.invokeOrDie(this.hasMethod, message);
            }
         }

         @Override
         public boolean has(GeneratedMessage.Builder builder) {
            if (!this.hasHasMethod) {
               if (this.isOneofField) {
                  return this.getOneofFieldNumber(builder) == this.field.getNumber();
               } else {
                  return !this.get(builder).equals(this.field.getDefaultValue());
               }
            } else {
               return GeneratedMessage.invokeOrDie(this.hasMethodBuilder, builder);
            }
         }

         @Override
         public int getRepeatedCount(GeneratedMessage message) {
            throw new UnsupportedOperationException("getRepeatedFieldSize() called on a singular field.");
         }

         @Override
         public int getRepeatedCount(GeneratedMessage.Builder builder) {
            throw new UnsupportedOperationException("getRepeatedFieldSize() called on a singular field.");
         }

         @Override
         public void clear(GeneratedMessage.Builder builder) {
            GeneratedMessage.invokeOrDie(this.clearMethod, builder);
         }

         @Override
         public Message.Builder newBuilder() {
            throw new UnsupportedOperationException("newBuilderForField() called on a non-Message type.");
         }

         @Override
         public Message.Builder getBuilder(GeneratedMessage.Builder builder) {
            throw new UnsupportedOperationException("getFieldBuilder() called on a non-Message type.");
         }

         @Override
         public Message.Builder getRepeatedBuilder(GeneratedMessage.Builder builder, int index) {
            throw new UnsupportedOperationException("getRepeatedFieldBuilder() called on a non-Message type.");
         }
      }

      private static final class SingularMessageFieldAccessor extends GeneratedMessage.FieldAccessorTable.SingularFieldAccessor {
         private final java.lang.reflect.Method newBuilderMethod = GeneratedMessage.getMethodOrDie(this.type, "newBuilder");
         private final java.lang.reflect.Method getBuilderMethodBuilder;

         SingularMessageFieldAccessor(
            Descriptors.FieldDescriptor descriptor,
            String camelCaseName,
            Class<? extends GeneratedMessage> messageClass,
            Class<? extends GeneratedMessage.Builder> builderClass,
            String containingOneofCamelCaseName
         ) {
            super(descriptor, camelCaseName, messageClass, builderClass, containingOneofCamelCaseName);
            this.getBuilderMethodBuilder = GeneratedMessage.getMethodOrDie(builderClass, "get" + camelCaseName + "Builder");
         }

         private Object coerceType(Object value) {
            return this.type.isInstance(value)
               ? value
               : ((Message.Builder)GeneratedMessage.invokeOrDie(this.newBuilderMethod, null)).mergeFrom((Message)value).buildPartial();
         }

         @Override
         public void set(GeneratedMessage.Builder builder, Object value) {
            super.set(builder, this.coerceType(value));
         }

         @Override
         public Message.Builder newBuilder() {
            return (Message.Builder)GeneratedMessage.invokeOrDie(this.newBuilderMethod, null);
         }

         @Override
         public Message.Builder getBuilder(GeneratedMessage.Builder builder) {
            return (Message.Builder)GeneratedMessage.invokeOrDie(this.getBuilderMethodBuilder, builder);
         }
      }

      private static final class SingularStringFieldAccessor extends GeneratedMessage.FieldAccessorTable.SingularFieldAccessor {
         private final java.lang.reflect.Method getBytesMethod;
         private final java.lang.reflect.Method getBytesMethodBuilder;
         private final java.lang.reflect.Method setBytesMethodBuilder;

         SingularStringFieldAccessor(
            Descriptors.FieldDescriptor descriptor,
            String camelCaseName,
            Class<? extends GeneratedMessage> messageClass,
            Class<? extends GeneratedMessage.Builder> builderClass,
            String containingOneofCamelCaseName
         ) {
            super(descriptor, camelCaseName, messageClass, builderClass, containingOneofCamelCaseName);
            this.getBytesMethod = GeneratedMessage.getMethodOrDie(messageClass, "get" + camelCaseName + "Bytes");
            this.getBytesMethodBuilder = GeneratedMessage.getMethodOrDie(builderClass, "get" + camelCaseName + "Bytes");
            this.setBytesMethodBuilder = GeneratedMessage.getMethodOrDie(builderClass, "set" + camelCaseName + "Bytes", ByteString.class);
         }

         @Override
         public Object getRaw(GeneratedMessage message) {
            return GeneratedMessage.invokeOrDie(this.getBytesMethod, message);
         }

         @Override
         public Object getRaw(GeneratedMessage.Builder builder) {
            return GeneratedMessage.invokeOrDie(this.getBytesMethodBuilder, builder);
         }

         @Override
         public void set(GeneratedMessage.Builder builder, Object value) {
            if (value instanceof ByteString) {
               GeneratedMessage.invokeOrDie(this.setBytesMethodBuilder, builder, value);
            } else {
               super.set(builder, value);
            }

         }
      }
   }

   public static class GeneratedExtension<ContainingType extends Message, Type> extends Extension<ContainingType, Type> {
      private GeneratedMessage.ExtensionDescriptorRetriever descriptorRetriever;
      private final Class singularType;
      private final Message messageDefaultInstance;
      private final java.lang.reflect.Method enumValueOf;
      private final java.lang.reflect.Method enumGetValueDescriptor;
      private final Extension.ExtensionType extensionType;

      GeneratedExtension(
         GeneratedMessage.ExtensionDescriptorRetriever descriptorRetriever,
         Class singularType,
         Message messageDefaultInstance,
         Extension.ExtensionType extensionType
      ) {
         if (Message.class.isAssignableFrom(singularType) && !singularType.isInstance(messageDefaultInstance)) {
            throw new IllegalArgumentException("Bad messageDefaultInstance for " + singularType.getName());
         } else {
            this.descriptorRetriever = descriptorRetriever;
            this.singularType = singularType;
            this.messageDefaultInstance = messageDefaultInstance;
            if (ProtocolMessageEnum.class.isAssignableFrom(singularType)) {
               this.enumValueOf = GeneratedMessage.getMethodOrDie(singularType, "valueOf", Descriptors.EnumValueDescriptor.class);
               this.enumGetValueDescriptor = GeneratedMessage.getMethodOrDie(singularType, "getValueDescriptor");
            } else {
               this.enumValueOf = null;
               this.enumGetValueDescriptor = null;
            }

            this.extensionType = extensionType;
         }
      }

      public void internalInit(final Descriptors.FieldDescriptor descriptor) {
         if (this.descriptorRetriever != null) {
            throw new IllegalStateException("Already initialized.");
         } else {
            this.descriptorRetriever = new GeneratedMessage.ExtensionDescriptorRetriever() {
               @Override
               public Descriptors.FieldDescriptor getDescriptor() {
                  return descriptor;
               }
            };
         }
      }

      @Override
      public Descriptors.FieldDescriptor getDescriptor() {
         if (this.descriptorRetriever == null) {
            throw new IllegalStateException("getDescriptor() called before internalInit()");
         } else {
            return this.descriptorRetriever.getDescriptor();
         }
      }

      @Override
      public Message getMessageDefaultInstance() {
         return this.messageDefaultInstance;
      }

      @Override
      protected Extension.ExtensionType getExtensionType() {
         return this.extensionType;
      }

      @Override
      protected Object fromReflectionType(Object value) {
         Descriptors.FieldDescriptor descriptor = this.getDescriptor();
         if (!descriptor.isRepeated()) {
            return this.singularFromReflectionType(value);
         } else if (descriptor.getJavaType() != Descriptors.FieldDescriptor.JavaType.MESSAGE
            && descriptor.getJavaType() != Descriptors.FieldDescriptor.JavaType.ENUM) {
            return value;
         } else {
            List result = new ArrayList();

            for(Object element : (List)value) {
               result.add(this.singularFromReflectionType(element));
            }

            return result;
         }
      }

      @Override
      protected Object singularFromReflectionType(Object value) {
         Descriptors.FieldDescriptor descriptor = this.getDescriptor();
         switch(descriptor.getJavaType()) {
            case MESSAGE:
               if (this.singularType.isInstance(value)) {
                  return value;
               }

               return this.messageDefaultInstance.newBuilderForType().mergeFrom((Message)value).build();
            case ENUM:
               return GeneratedMessage.invokeOrDie(this.enumValueOf, null, (Descriptors.EnumValueDescriptor)value);
            default:
               return value;
         }
      }

      @Override
      protected Object toReflectionType(Object value) {
         Descriptors.FieldDescriptor descriptor = this.getDescriptor();
         if (!descriptor.isRepeated()) {
            return this.singularToReflectionType(value);
         } else if (descriptor.getJavaType() != Descriptors.FieldDescriptor.JavaType.ENUM) {
            return value;
         } else {
            List result = new ArrayList();

            for(Object element : (List)value) {
               result.add(this.singularToReflectionType(element));
            }

            return result;
         }
      }

      @Override
      protected Object singularToReflectionType(Object value) {
         Descriptors.FieldDescriptor descriptor = this.getDescriptor();
         switch(descriptor.getJavaType()) {
            case ENUM:
               return GeneratedMessage.invokeOrDie(this.enumGetValueDescriptor, value);
            default:
               return value;
         }
      }

      @Override
      public int getNumber() {
         return this.getDescriptor().getNumber();
      }

      @Override
      public WireFormat.FieldType getLiteType() {
         return this.getDescriptor().getLiteType();
      }

      @Override
      public boolean isRepeated() {
         return this.getDescriptor().isRepeated();
      }

      @Override
      public Type getDefaultValue() {
         if (this.isRepeated()) {
            return (Type)Collections.emptyList();
         } else {
            return (Type)(this.getDescriptor().getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE
               ? this.messageDefaultInstance
               : this.singularFromReflectionType(this.getDescriptor().getDefaultValue()));
         }
      }
   }
}
