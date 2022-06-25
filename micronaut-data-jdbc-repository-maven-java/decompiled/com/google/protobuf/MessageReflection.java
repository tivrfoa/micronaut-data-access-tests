package com.google.protobuf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

class MessageReflection {
   static void writeMessageTo(Message message, Map<Descriptors.FieldDescriptor, Object> fields, CodedOutputStream output, boolean alwaysWriteRequiredFields) throws IOException {
      boolean isMessageSet = message.getDescriptorForType().getOptions().getMessageSetWireFormat();
      if (alwaysWriteRequiredFields) {
         fields = new TreeMap(fields);

         for(Descriptors.FieldDescriptor field : message.getDescriptorForType().getFields()) {
            if (field.isRequired() && !fields.containsKey(field)) {
               fields.put(field, message.getField(field));
            }
         }
      }

      for(Entry<Descriptors.FieldDescriptor, Object> entry : fields.entrySet()) {
         Descriptors.FieldDescriptor field = (Descriptors.FieldDescriptor)entry.getKey();
         Object value = entry.getValue();
         if (isMessageSet && field.isExtension() && field.getType() == Descriptors.FieldDescriptor.Type.MESSAGE && !field.isRepeated()) {
            output.writeMessageSetExtension(field.getNumber(), (Message)value);
         } else {
            FieldSet.writeField(field, value, output);
         }
      }

      UnknownFieldSet unknownFields = message.getUnknownFields();
      if (isMessageSet) {
         unknownFields.writeAsMessageSetTo(output);
      } else {
         unknownFields.writeTo(output);
      }

   }

   static int getSerializedSize(Message message, Map<Descriptors.FieldDescriptor, Object> fields) {
      int size = 0;
      boolean isMessageSet = message.getDescriptorForType().getOptions().getMessageSetWireFormat();

      for(Entry<Descriptors.FieldDescriptor, Object> entry : fields.entrySet()) {
         Descriptors.FieldDescriptor field = (Descriptors.FieldDescriptor)entry.getKey();
         Object value = entry.getValue();
         if (isMessageSet && field.isExtension() && field.getType() == Descriptors.FieldDescriptor.Type.MESSAGE && !field.isRepeated()) {
            size += CodedOutputStream.computeMessageSetExtensionSize(field.getNumber(), (Message)value);
         } else {
            size += FieldSet.computeFieldSize(field, value);
         }
      }

      UnknownFieldSet unknownFields = message.getUnknownFields();
      if (isMessageSet) {
         size += unknownFields.getSerializedSizeAsMessageSet();
      } else {
         size += unknownFields.getSerializedSize();
      }

      return size;
   }

   static String delimitWithCommas(List<String> parts) {
      StringBuilder result = new StringBuilder();

      for(String part : parts) {
         if (result.length() > 0) {
            result.append(", ");
         }

         result.append(part);
      }

      return result.toString();
   }

   static boolean isInitialized(MessageOrBuilder message) {
      for(Descriptors.FieldDescriptor field : message.getDescriptorForType().getFields()) {
         if (field.isRequired() && !message.hasField(field)) {
            return false;
         }
      }

      for(Entry<Descriptors.FieldDescriptor, Object> entry : message.getAllFields().entrySet()) {
         Descriptors.FieldDescriptor field = (Descriptors.FieldDescriptor)entry.getKey();
         if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
            if (field.isRepeated()) {
               for(Message element : (List)entry.getValue()) {
                  if (!element.isInitialized()) {
                     return false;
                  }
               }
            } else if (!((Message)entry.getValue()).isInitialized()) {
               return false;
            }
         }
      }

      return true;
   }

   private static String subMessagePrefix(String prefix, Descriptors.FieldDescriptor field, int index) {
      StringBuilder result = new StringBuilder(prefix);
      if (field.isExtension()) {
         result.append('(').append(field.getFullName()).append(')');
      } else {
         result.append(field.getName());
      }

      if (index != -1) {
         result.append('[').append(index).append(']');
      }

      result.append('.');
      return result.toString();
   }

   private static void findMissingFields(MessageOrBuilder message, String prefix, List<String> results) {
      for(Descriptors.FieldDescriptor field : message.getDescriptorForType().getFields()) {
         if (field.isRequired() && !message.hasField(field)) {
            results.add(prefix + field.getName());
         }
      }

      for(Entry<Descriptors.FieldDescriptor, Object> entry : message.getAllFields().entrySet()) {
         Descriptors.FieldDescriptor field = (Descriptors.FieldDescriptor)entry.getKey();
         Object value = entry.getValue();
         if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
            if (field.isRepeated()) {
               int i = 0;

               for(Object element : (List)value) {
                  findMissingFields((MessageOrBuilder)element, subMessagePrefix(prefix, field, i++), results);
               }
            } else if (message.hasField(field)) {
               findMissingFields((MessageOrBuilder)value, subMessagePrefix(prefix, field, -1), results);
            }
         }
      }

   }

   static List<String> findMissingFields(MessageOrBuilder message) {
      List<String> results = new ArrayList();
      findMissingFields(message, "", results);
      return results;
   }

   static boolean mergeFieldFrom(
      CodedInputStream input,
      UnknownFieldSet.Builder unknownFields,
      ExtensionRegistryLite extensionRegistry,
      Descriptors.Descriptor type,
      MessageReflection.MergeTarget target,
      int tag
   ) throws IOException {
      if (type.getOptions().getMessageSetWireFormat() && tag == WireFormat.MESSAGE_SET_ITEM_TAG) {
         mergeMessageSetExtensionFromCodedStream(input, unknownFields, extensionRegistry, type, target);
         return true;
      } else {
         int wireType = WireFormat.getTagWireType(tag);
         int fieldNumber = WireFormat.getTagFieldNumber(tag);
         Message defaultInstance = null;
         Descriptors.FieldDescriptor field;
         if (type.isExtensionNumber(fieldNumber)) {
            if (extensionRegistry instanceof ExtensionRegistry) {
               ExtensionRegistry.ExtensionInfo extension = target.findExtensionByNumber((ExtensionRegistry)extensionRegistry, type, fieldNumber);
               if (extension == null) {
                  field = null;
               } else {
                  field = extension.descriptor;
                  defaultInstance = extension.defaultInstance;
                  if (defaultInstance == null && field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                     throw new IllegalStateException("Message-typed extension lacked default instance: " + field.getFullName());
                  }
               }
            } else {
               field = null;
            }
         } else if (target.getContainerType() == MessageReflection.MergeTarget.ContainerType.MESSAGE) {
            field = type.findFieldByNumber(fieldNumber);
         } else {
            field = null;
         }

         boolean unknown = false;
         boolean packed = false;
         if (field == null) {
            unknown = true;
         } else if (wireType == FieldSet.getWireFormatForFieldType(field.getLiteType(), false)) {
            packed = false;
         } else if (field.isPackable() && wireType == FieldSet.getWireFormatForFieldType(field.getLiteType(), true)) {
            packed = true;
         } else {
            unknown = true;
         }

         if (unknown) {
            return unknownFields != null ? unknownFields.mergeFieldFrom(tag, input) : input.skipField(tag);
         } else {
            if (packed) {
               int length = input.readRawVarint32();
               int limit = input.pushLimit(length);
               if (field.getLiteType() == WireFormat.FieldType.ENUM) {
                  while(input.getBytesUntilLimit() > 0) {
                     int rawValue = input.readEnum();
                     if (field.getFile().supportsUnknownEnumValue()) {
                        target.addRepeatedField(field, field.getEnumType().findValueByNumberCreatingIfUnknown(rawValue));
                     } else {
                        Object value = field.getEnumType().findValueByNumber(rawValue);
                        if (value == null) {
                           if (unknownFields != null) {
                              unknownFields.mergeVarintField(fieldNumber, rawValue);
                           }
                        } else {
                           target.addRepeatedField(field, value);
                        }
                     }
                  }
               } else {
                  while(input.getBytesUntilLimit() > 0) {
                     Object value = WireFormat.readPrimitiveField(input, field.getLiteType(), target.getUtf8Validation(field));
                     target.addRepeatedField(field, value);
                  }
               }

               input.popLimit(limit);
            } else {
               Object value;
               switch(field.getType()) {
                  case GROUP:
                     value = target.parseGroup(input, extensionRegistry, field, defaultInstance);
                     break;
                  case MESSAGE:
                     value = target.parseMessage(input, extensionRegistry, field, defaultInstance);
                     break;
                  case ENUM:
                     int rawValue = input.readEnum();
                     if (field.getFile().supportsUnknownEnumValue()) {
                        value = field.getEnumType().findValueByNumberCreatingIfUnknown(rawValue);
                     } else {
                        value = field.getEnumType().findValueByNumber(rawValue);
                        if (value == null) {
                           if (unknownFields != null) {
                              unknownFields.mergeVarintField(fieldNumber, rawValue);
                           }

                           return true;
                        }
                     }
                     break;
                  default:
                     value = WireFormat.readPrimitiveField(input, field.getLiteType(), target.getUtf8Validation(field));
               }

               if (field.isRepeated()) {
                  target.addRepeatedField(field, value);
               } else {
                  target.setField(field, value);
               }
            }

            return true;
         }
      }
   }

   private static void mergeMessageSetExtensionFromCodedStream(
      CodedInputStream input,
      UnknownFieldSet.Builder unknownFields,
      ExtensionRegistryLite extensionRegistry,
      Descriptors.Descriptor type,
      MessageReflection.MergeTarget target
   ) throws IOException {
      int typeId = 0;
      ByteString rawBytes = null;
      ExtensionRegistry.ExtensionInfo extension = null;

      while(true) {
         int tag = input.readTag();
         if (tag == 0) {
            break;
         }

         if (tag == WireFormat.MESSAGE_SET_TYPE_ID_TAG) {
            typeId = input.readUInt32();
            if (typeId != 0 && extensionRegistry instanceof ExtensionRegistry) {
               extension = target.findExtensionByNumber((ExtensionRegistry)extensionRegistry, type, typeId);
            }
         } else if (tag == WireFormat.MESSAGE_SET_MESSAGE_TAG) {
            if (typeId != 0 && extension != null && ExtensionRegistryLite.isEagerlyParseMessageSets()) {
               eagerlyMergeMessageSetExtension(input, extension, extensionRegistry, target);
               rawBytes = null;
            } else {
               rawBytes = input.readBytes();
            }
         } else if (!input.skipField(tag)) {
            break;
         }
      }

      input.checkLastTagWas(WireFormat.MESSAGE_SET_ITEM_END_TAG);
      if (rawBytes != null && typeId != 0) {
         if (extension != null) {
            mergeMessageSetExtensionFromBytes(rawBytes, extension, extensionRegistry, target);
         } else if (rawBytes != null && unknownFields != null) {
            unknownFields.mergeField(typeId, UnknownFieldSet.Field.newBuilder().addLengthDelimited(rawBytes).build());
         }
      }

   }

   private static void mergeMessageSetExtensionFromBytes(
      ByteString rawBytes, ExtensionRegistry.ExtensionInfo extension, ExtensionRegistryLite extensionRegistry, MessageReflection.MergeTarget target
   ) throws IOException {
      Descriptors.FieldDescriptor field = extension.descriptor;
      boolean hasOriginalValue = target.hasField(field);
      if (!hasOriginalValue && !ExtensionRegistryLite.isEagerlyParseMessageSets()) {
         LazyField lazyField = new LazyField(extension.defaultInstance, extensionRegistry, rawBytes);
         target.setField(field, lazyField);
      } else {
         Object value = target.parseMessageFromBytes(rawBytes, extensionRegistry, field, extension.defaultInstance);
         target.setField(field, value);
      }

   }

   private static void eagerlyMergeMessageSetExtension(
      CodedInputStream input, ExtensionRegistry.ExtensionInfo extension, ExtensionRegistryLite extensionRegistry, MessageReflection.MergeTarget target
   ) throws IOException {
      Descriptors.FieldDescriptor field = extension.descriptor;
      Object value = target.parseMessage(input, extensionRegistry, field, extension.defaultInstance);
      target.setField(field, value);
   }

   static class BuilderAdapter implements MessageReflection.MergeTarget {
      private final Message.Builder builder;

      @Override
      public Descriptors.Descriptor getDescriptorForType() {
         return this.builder.getDescriptorForType();
      }

      public BuilderAdapter(Message.Builder builder) {
         this.builder = builder;
      }

      @Override
      public Object getField(Descriptors.FieldDescriptor field) {
         return this.builder.getField(field);
      }

      @Override
      public boolean hasField(Descriptors.FieldDescriptor field) {
         return this.builder.hasField(field);
      }

      @Override
      public MessageReflection.MergeTarget setField(Descriptors.FieldDescriptor field, Object value) {
         this.builder.setField(field, value);
         return this;
      }

      @Override
      public MessageReflection.MergeTarget clearField(Descriptors.FieldDescriptor field) {
         this.builder.clearField(field);
         return this;
      }

      @Override
      public MessageReflection.MergeTarget setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
         this.builder.setRepeatedField(field, index, value);
         return this;
      }

      @Override
      public MessageReflection.MergeTarget addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
         this.builder.addRepeatedField(field, value);
         return this;
      }

      @Override
      public boolean hasOneof(Descriptors.OneofDescriptor oneof) {
         return this.builder.hasOneof(oneof);
      }

      @Override
      public MessageReflection.MergeTarget clearOneof(Descriptors.OneofDescriptor oneof) {
         this.builder.clearOneof(oneof);
         return this;
      }

      @Override
      public Descriptors.FieldDescriptor getOneofFieldDescriptor(Descriptors.OneofDescriptor oneof) {
         return this.builder.getOneofFieldDescriptor(oneof);
      }

      @Override
      public MessageReflection.MergeTarget.ContainerType getContainerType() {
         return MessageReflection.MergeTarget.ContainerType.MESSAGE;
      }

      @Override
      public ExtensionRegistry.ExtensionInfo findExtensionByName(ExtensionRegistry registry, String name) {
         return registry.findImmutableExtensionByName(name);
      }

      @Override
      public ExtensionRegistry.ExtensionInfo findExtensionByNumber(ExtensionRegistry registry, Descriptors.Descriptor containingType, int fieldNumber) {
         return registry.findImmutableExtensionByNumber(containingType, fieldNumber);
      }

      @Override
      public Object parseGroup(CodedInputStream input, ExtensionRegistryLite extensionRegistry, Descriptors.FieldDescriptor field, Message defaultInstance) throws IOException {
         Message.Builder subBuilder;
         if (defaultInstance != null) {
            subBuilder = defaultInstance.newBuilderForType();
         } else {
            subBuilder = this.builder.newBuilderForField(field);
         }

         if (!field.isRepeated()) {
            Message originalMessage = (Message)this.getField(field);
            if (originalMessage != null) {
               subBuilder.mergeFrom(originalMessage);
            }
         }

         input.readGroup(field.getNumber(), subBuilder, extensionRegistry);
         return subBuilder.buildPartial();
      }

      @Override
      public Object parseMessage(CodedInputStream input, ExtensionRegistryLite extensionRegistry, Descriptors.FieldDescriptor field, Message defaultInstance) throws IOException {
         Message.Builder subBuilder;
         if (defaultInstance != null) {
            subBuilder = defaultInstance.newBuilderForType();
         } else {
            subBuilder = this.builder.newBuilderForField(field);
         }

         if (!field.isRepeated()) {
            Message originalMessage = (Message)this.getField(field);
            if (originalMessage != null) {
               subBuilder.mergeFrom(originalMessage);
            }
         }

         input.readMessage(subBuilder, extensionRegistry);
         return subBuilder.buildPartial();
      }

      @Override
      public Object parseMessageFromBytes(ByteString bytes, ExtensionRegistryLite extensionRegistry, Descriptors.FieldDescriptor field, Message defaultInstance) throws IOException {
         Message.Builder subBuilder;
         if (defaultInstance != null) {
            subBuilder = defaultInstance.newBuilderForType();
         } else {
            subBuilder = this.builder.newBuilderForField(field);
         }

         if (!field.isRepeated()) {
            Message originalMessage = (Message)this.getField(field);
            if (originalMessage != null) {
               subBuilder.mergeFrom(originalMessage);
            }
         }

         subBuilder.mergeFrom(bytes, extensionRegistry);
         return subBuilder.buildPartial();
      }

      @Override
      public MessageReflection.MergeTarget newMergeTargetForField(Descriptors.FieldDescriptor field, Message defaultInstance) {
         Message.Builder subBuilder;
         if (defaultInstance != null) {
            subBuilder = defaultInstance.newBuilderForType();
         } else {
            subBuilder = this.builder.newBuilderForField(field);
         }

         if (!field.isRepeated()) {
            Message originalMessage = (Message)this.getField(field);
            if (originalMessage != null) {
               subBuilder.mergeFrom(originalMessage);
            }
         }

         return new MessageReflection.BuilderAdapter(subBuilder);
      }

      @Override
      public MessageReflection.MergeTarget newEmptyTargetForField(Descriptors.FieldDescriptor field, Message defaultInstance) {
         Message.Builder subBuilder;
         if (defaultInstance != null) {
            subBuilder = defaultInstance.newBuilderForType();
         } else {
            subBuilder = this.builder.newBuilderForField(field);
         }

         return new MessageReflection.BuilderAdapter(subBuilder);
      }

      @Override
      public WireFormat.Utf8Validation getUtf8Validation(Descriptors.FieldDescriptor descriptor) {
         if (descriptor.needsUtf8Check()) {
            return WireFormat.Utf8Validation.STRICT;
         } else {
            return !descriptor.isRepeated() && this.builder instanceof GeneratedMessage.Builder
               ? WireFormat.Utf8Validation.LAZY
               : WireFormat.Utf8Validation.LOOSE;
         }
      }

      @Override
      public Object finish() {
         return this.builder.buildPartial();
      }
   }

   static class ExtensionAdapter implements MessageReflection.MergeTarget {
      private final FieldSet<Descriptors.FieldDescriptor> extensions;

      ExtensionAdapter(FieldSet<Descriptors.FieldDescriptor> extensions) {
         this.extensions = extensions;
      }

      @Override
      public Descriptors.Descriptor getDescriptorForType() {
         throw new UnsupportedOperationException("getDescriptorForType() called on FieldSet object");
      }

      @Override
      public Object getField(Descriptors.FieldDescriptor field) {
         return this.extensions.getField(field);
      }

      @Override
      public boolean hasField(Descriptors.FieldDescriptor field) {
         return this.extensions.hasField(field);
      }

      @Override
      public MessageReflection.MergeTarget setField(Descriptors.FieldDescriptor field, Object value) {
         this.extensions.setField(field, value);
         return this;
      }

      @Override
      public MessageReflection.MergeTarget clearField(Descriptors.FieldDescriptor field) {
         this.extensions.clearField(field);
         return this;
      }

      @Override
      public MessageReflection.MergeTarget setRepeatedField(Descriptors.FieldDescriptor field, int index, Object value) {
         this.extensions.setRepeatedField(field, index, value);
         return this;
      }

      @Override
      public MessageReflection.MergeTarget addRepeatedField(Descriptors.FieldDescriptor field, Object value) {
         this.extensions.addRepeatedField(field, value);
         return this;
      }

      @Override
      public boolean hasOneof(Descriptors.OneofDescriptor oneof) {
         return false;
      }

      @Override
      public MessageReflection.MergeTarget clearOneof(Descriptors.OneofDescriptor oneof) {
         return this;
      }

      @Override
      public Descriptors.FieldDescriptor getOneofFieldDescriptor(Descriptors.OneofDescriptor oneof) {
         return null;
      }

      @Override
      public MessageReflection.MergeTarget.ContainerType getContainerType() {
         return MessageReflection.MergeTarget.ContainerType.EXTENSION_SET;
      }

      @Override
      public ExtensionRegistry.ExtensionInfo findExtensionByName(ExtensionRegistry registry, String name) {
         return registry.findImmutableExtensionByName(name);
      }

      @Override
      public ExtensionRegistry.ExtensionInfo findExtensionByNumber(ExtensionRegistry registry, Descriptors.Descriptor containingType, int fieldNumber) {
         return registry.findImmutableExtensionByNumber(containingType, fieldNumber);
      }

      @Override
      public Object parseGroup(CodedInputStream input, ExtensionRegistryLite registry, Descriptors.FieldDescriptor field, Message defaultInstance) throws IOException {
         Message.Builder subBuilder = defaultInstance.newBuilderForType();
         if (!field.isRepeated()) {
            Message originalMessage = (Message)this.getField(field);
            if (originalMessage != null) {
               subBuilder.mergeFrom(originalMessage);
            }
         }

         input.readGroup(field.getNumber(), subBuilder, registry);
         return subBuilder.buildPartial();
      }

      @Override
      public Object parseMessage(CodedInputStream input, ExtensionRegistryLite registry, Descriptors.FieldDescriptor field, Message defaultInstance) throws IOException {
         Message.Builder subBuilder = defaultInstance.newBuilderForType();
         if (!field.isRepeated()) {
            Message originalMessage = (Message)this.getField(field);
            if (originalMessage != null) {
               subBuilder.mergeFrom(originalMessage);
            }
         }

         input.readMessage(subBuilder, registry);
         return subBuilder.buildPartial();
      }

      @Override
      public Object parseMessageFromBytes(ByteString bytes, ExtensionRegistryLite registry, Descriptors.FieldDescriptor field, Message defaultInstance) throws IOException {
         Message.Builder subBuilder = defaultInstance.newBuilderForType();
         if (!field.isRepeated()) {
            Message originalMessage = (Message)this.getField(field);
            if (originalMessage != null) {
               subBuilder.mergeFrom(originalMessage);
            }
         }

         subBuilder.mergeFrom(bytes, registry);
         return subBuilder.buildPartial();
      }

      @Override
      public MessageReflection.MergeTarget newMergeTargetForField(Descriptors.FieldDescriptor descriptor, Message defaultInstance) {
         throw new UnsupportedOperationException("newMergeTargetForField() called on FieldSet object");
      }

      @Override
      public MessageReflection.MergeTarget newEmptyTargetForField(Descriptors.FieldDescriptor descriptor, Message defaultInstance) {
         throw new UnsupportedOperationException("newEmptyTargetForField() called on FieldSet object");
      }

      @Override
      public WireFormat.Utf8Validation getUtf8Validation(Descriptors.FieldDescriptor descriptor) {
         return descriptor.needsUtf8Check() ? WireFormat.Utf8Validation.STRICT : WireFormat.Utf8Validation.LOOSE;
      }

      @Override
      public Object finish() {
         throw new UnsupportedOperationException("finish() called on FieldSet object");
      }
   }

   interface MergeTarget {
      Descriptors.Descriptor getDescriptorForType();

      MessageReflection.MergeTarget.ContainerType getContainerType();

      ExtensionRegistry.ExtensionInfo findExtensionByName(ExtensionRegistry var1, String var2);

      ExtensionRegistry.ExtensionInfo findExtensionByNumber(ExtensionRegistry var1, Descriptors.Descriptor var2, int var3);

      Object getField(Descriptors.FieldDescriptor var1);

      boolean hasField(Descriptors.FieldDescriptor var1);

      MessageReflection.MergeTarget setField(Descriptors.FieldDescriptor var1, Object var2);

      MessageReflection.MergeTarget clearField(Descriptors.FieldDescriptor var1);

      MessageReflection.MergeTarget setRepeatedField(Descriptors.FieldDescriptor var1, int var2, Object var3);

      MessageReflection.MergeTarget addRepeatedField(Descriptors.FieldDescriptor var1, Object var2);

      boolean hasOneof(Descriptors.OneofDescriptor var1);

      MessageReflection.MergeTarget clearOneof(Descriptors.OneofDescriptor var1);

      Descriptors.FieldDescriptor getOneofFieldDescriptor(Descriptors.OneofDescriptor var1);

      Object parseGroup(CodedInputStream var1, ExtensionRegistryLite var2, Descriptors.FieldDescriptor var3, Message var4) throws IOException;

      Object parseMessage(CodedInputStream var1, ExtensionRegistryLite var2, Descriptors.FieldDescriptor var3, Message var4) throws IOException;

      Object parseMessageFromBytes(ByteString var1, ExtensionRegistryLite var2, Descriptors.FieldDescriptor var3, Message var4) throws IOException;

      WireFormat.Utf8Validation getUtf8Validation(Descriptors.FieldDescriptor var1);

      MessageReflection.MergeTarget newMergeTargetForField(Descriptors.FieldDescriptor var1, Message var2);

      MessageReflection.MergeTarget newEmptyTargetForField(Descriptors.FieldDescriptor var1, Message var2);

      Object finish();

      public static enum ContainerType {
         MESSAGE,
         EXTENSION_SET;
      }
   }
}
