package com.google.protobuf;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import sun.misc.Unsafe;

@CheckReturnValue
final class MessageSchema<T> implements Schema<T> {
   private static final int INTS_PER_FIELD = 3;
   private static final int OFFSET_BITS = 20;
   private static final int OFFSET_MASK = 1048575;
   private static final int FIELD_TYPE_MASK = 267386880;
   private static final int REQUIRED_MASK = 268435456;
   private static final int ENFORCE_UTF8_MASK = 536870912;
   private static final int NO_PRESENCE_SENTINEL = 1048575;
   private static final int[] EMPTY_INT_ARRAY = new int[0];
   static final int ONEOF_TYPE_OFFSET = 51;
   private static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();
   private final int[] buffer;
   private final Object[] objects;
   private final int minFieldNumber;
   private final int maxFieldNumber;
   private final MessageLite defaultInstance;
   private final boolean hasExtensions;
   private final boolean lite;
   private final boolean proto3;
   private final boolean useCachedSizeField;
   private final int[] intArray;
   private final int checkInitializedCount;
   private final int repeatedFieldOffsetStart;
   private final NewInstanceSchema newInstanceSchema;
   private final ListFieldSchema listFieldSchema;
   private final UnknownFieldSchema<?, ?> unknownFieldSchema;
   private final ExtensionSchema<?> extensionSchema;
   private final MapFieldSchema mapFieldSchema;

   private MessageSchema(
      int[] buffer,
      Object[] objects,
      int minFieldNumber,
      int maxFieldNumber,
      MessageLite defaultInstance,
      boolean proto3,
      boolean useCachedSizeField,
      int[] intArray,
      int checkInitialized,
      int mapFieldPositions,
      NewInstanceSchema newInstanceSchema,
      ListFieldSchema listFieldSchema,
      UnknownFieldSchema<?, ?> unknownFieldSchema,
      ExtensionSchema<?> extensionSchema,
      MapFieldSchema mapFieldSchema
   ) {
      this.buffer = buffer;
      this.objects = objects;
      this.minFieldNumber = minFieldNumber;
      this.maxFieldNumber = maxFieldNumber;
      this.lite = defaultInstance instanceof GeneratedMessageLite;
      this.proto3 = proto3;
      this.hasExtensions = extensionSchema != null && extensionSchema.hasExtensions(defaultInstance);
      this.useCachedSizeField = useCachedSizeField;
      this.intArray = intArray;
      this.checkInitializedCount = checkInitialized;
      this.repeatedFieldOffsetStart = mapFieldPositions;
      this.newInstanceSchema = newInstanceSchema;
      this.listFieldSchema = listFieldSchema;
      this.unknownFieldSchema = unknownFieldSchema;
      this.extensionSchema = extensionSchema;
      this.defaultInstance = defaultInstance;
      this.mapFieldSchema = mapFieldSchema;
   }

   static <T> MessageSchema<T> newSchema(
      Class<T> messageClass,
      MessageInfo messageInfo,
      NewInstanceSchema newInstanceSchema,
      ListFieldSchema listFieldSchema,
      UnknownFieldSchema<?, ?> unknownFieldSchema,
      ExtensionSchema<?> extensionSchema,
      MapFieldSchema mapFieldSchema
   ) {
      return messageInfo instanceof RawMessageInfo
         ? newSchemaForRawMessageInfo((RawMessageInfo)messageInfo, newInstanceSchema, listFieldSchema, unknownFieldSchema, extensionSchema, mapFieldSchema)
         : newSchemaForMessageInfo((StructuralMessageInfo)messageInfo, newInstanceSchema, listFieldSchema, unknownFieldSchema, extensionSchema, mapFieldSchema);
   }

   static <T> MessageSchema<T> newSchemaForRawMessageInfo(
      RawMessageInfo messageInfo,
      NewInstanceSchema newInstanceSchema,
      ListFieldSchema listFieldSchema,
      UnknownFieldSchema<?, ?> unknownFieldSchema,
      ExtensionSchema<?> extensionSchema,
      MapFieldSchema mapFieldSchema
   ) {
      boolean isProto3 = messageInfo.getSyntax() == ProtoSyntax.PROTO3;
      String info = messageInfo.getStringInfo();
      int length = info.length();
      int i = 0;
      int next = info.charAt(i++);
      if (next >= 55296) {
         int result = next & 8191;

         int shift;
         for(shift = 13; (next = info.charAt(i++)) >= 55296; shift += 13) {
            result |= (next & 8191) << shift;
         }

         next = result | next << shift;
      }

      next = info.charAt(i++);
      if (next >= 55296) {
         int result = next & 8191;

         int shift;
         for(shift = 13; (next = info.charAt(i++)) >= 55296; shift += 13) {
            result |= (next & 8191) << shift;
         }

         next = result | next << shift;
      }

      int minFieldNumber;
      int maxFieldNumber;
      int numEntries;
      int mapFieldCount;
      int checkInitialized;
      int[] intArray;
      int objectsPosition;
      int oneofCount;
      if (next == 0) {
         oneofCount = 0;
         int hasBitsCount = 0;
         minFieldNumber = 0;
         maxFieldNumber = 0;
         numEntries = 0;
         mapFieldCount = 0;
         int repeatedFieldCount = 0;
         checkInitialized = 0;
         intArray = EMPTY_INT_ARRAY;
         objectsPosition = 0;
      } else {
         next = info.charAt(i++);
         if (next >= 55296) {
            int result = next & 8191;

            int shift;
            for(shift = 13; (next = info.charAt(i++)) >= 55296; shift += 13) {
               result |= (next & 8191) << shift;
            }

            next = result | next << shift;
         }

         oneofCount = next;
         next = info.charAt(i++);
         if (next >= 55296) {
            int result = next & 8191;

            int shift;
            for(shift = 13; (next = info.charAt(i++)) >= 55296; shift += 13) {
               result |= (next & 8191) << shift;
            }

            next = result | next << shift;
         }

         next = info.charAt(i++);
         if (next >= 55296) {
            int result = next & 8191;

            int shift;
            for(shift = 13; (next = info.charAt(i++)) >= 55296; shift += 13) {
               result |= (next & 8191) << shift;
            }

            next = result | next << shift;
         }

         minFieldNumber = next;
         next = info.charAt(i++);
         if (next >= 55296) {
            int result = next & 8191;

            int shift;
            for(shift = 13; (next = info.charAt(i++)) >= 55296; shift += 13) {
               result |= (next & 8191) << shift;
            }

            next = result | next << shift;
         }

         maxFieldNumber = next;
         next = info.charAt(i++);
         if (next >= 55296) {
            int result = next & 8191;

            int shift;
            for(shift = 13; (next = info.charAt(i++)) >= 55296; shift += 13) {
               result |= (next & 8191) << shift;
            }

            next = result | next << shift;
         }

         numEntries = next;
         next = info.charAt(i++);
         if (next >= 55296) {
            int result = next & 8191;

            int shift;
            for(shift = 13; (next = info.charAt(i++)) >= 55296; shift += 13) {
               result |= (next & 8191) << shift;
            }

            next = result | next << shift;
         }

         mapFieldCount = next;
         next = info.charAt(i++);
         if (next >= 55296) {
            int result = next & 8191;

            int shift;
            for(shift = 13; (next = info.charAt(i++)) >= 55296; shift += 13) {
               result |= (next & 8191) << shift;
            }

            next = result | next << shift;
         }

         next = info.charAt(i++);
         if (next >= 55296) {
            int result = next & 8191;

            int shift;
            for(shift = 13; (next = info.charAt(i++)) >= 55296; shift += 13) {
               result |= (next & 8191) << shift;
            }

            next = result | next << shift;
         }

         checkInitialized = next;
         intArray = new int[next + next + next];
         objectsPosition = next * 2 + next;
      }

      Unsafe unsafe = UNSAFE;
      Object[] messageInfoObjects = messageInfo.getObjects();
      int checkInitializedPosition = 0;
      Class<?> messageClass = messageInfo.getDefaultInstance().getClass();
      int[] buffer = new int[numEntries * 3];
      Object[] objects = new Object[numEntries * 2];
      int mapFieldIndex = checkInitialized;
      int repeatedFieldIndex = checkInitialized + mapFieldCount;

      int presenceFieldOffset;
      int presenceMaskShift;
      for(int bufferIndex = 0; i < length; buffer[bufferIndex++] = presenceMaskShift << 20 | presenceFieldOffset) {
         next = info.charAt(i++);
         if (next >= 55296) {
            int result = next & 8191;

            for(presenceMaskShift = 13; (next = info.charAt(i++)) >= 55296; presenceMaskShift += 13) {
               result |= (next & 8191) << presenceMaskShift;
            }

            next = result | next << presenceMaskShift;
         }

         next = info.charAt(i++);
         if (next >= 55296) {
            int result = next & 8191;

            for(presenceMaskShift = 13; (next = info.charAt(i++)) >= 55296; presenceMaskShift += 13) {
               result |= (next & 8191) << presenceMaskShift;
            }

            next = result | next << presenceMaskShift;
         }

         int fieldType = next & 0xFF;
         if ((next & 1024) != 0) {
            intArray[checkInitializedPosition++] = bufferIndex;
         }

         int fieldOffset;
         if (fieldType >= 51) {
            next = info.charAt(i++);
            if (next >= 55296) {
               int result = next & 8191;

               int shift;
               for(shift = 13; (next = info.charAt(i++)) >= 55296; shift += 13) {
                  result |= (next & 8191) << shift;
               }

               next = result | next << shift;
            }

            int oneofFieldType = fieldType - 51;
            if (oneofFieldType == 9 || oneofFieldType == 17) {
               objects[bufferIndex / 3 * 2 + 1] = messageInfoObjects[objectsPosition++];
            } else if (oneofFieldType == 12 && !isProto3) {
               objects[bufferIndex / 3 * 2 + 1] = messageInfoObjects[objectsPosition++];
            }

            int index = next * 2;
            Object o = messageInfoObjects[index];
            java.lang.reflect.Field oneofField;
            if (o instanceof java.lang.reflect.Field) {
               oneofField = (java.lang.reflect.Field)o;
            } else {
               oneofField = reflectField(messageClass, (String)o);
               messageInfoObjects[index] = oneofField;
            }

            fieldOffset = (int)unsafe.objectFieldOffset(oneofField);
            o = messageInfoObjects[++index];
            java.lang.reflect.Field oneofCaseField;
            if (o instanceof java.lang.reflect.Field) {
               oneofCaseField = (java.lang.reflect.Field)o;
            } else {
               oneofCaseField = reflectField(messageClass, (String)o);
               messageInfoObjects[index] = oneofCaseField;
            }

            presenceFieldOffset = (int)unsafe.objectFieldOffset(oneofCaseField);
            presenceMaskShift = 0;
         } else {
            java.lang.reflect.Field field = reflectField(messageClass, (String)messageInfoObjects[objectsPosition++]);
            if (fieldType == 9 || fieldType == 17) {
               objects[bufferIndex / 3 * 2 + 1] = field.getType();
            } else if (fieldType == 27 || fieldType == 49) {
               objects[bufferIndex / 3 * 2 + 1] = messageInfoObjects[objectsPosition++];
            } else if (fieldType != 12 && fieldType != 30 && fieldType != 44) {
               if (fieldType == 50) {
                  intArray[mapFieldIndex++] = bufferIndex;
                  objects[bufferIndex / 3 * 2] = messageInfoObjects[objectsPosition++];
                  if ((next & 2048) != 0) {
                     objects[bufferIndex / 3 * 2 + 1] = messageInfoObjects[objectsPosition++];
                  }
               }
            } else if (!isProto3) {
               objects[bufferIndex / 3 * 2 + 1] = messageInfoObjects[objectsPosition++];
            }

            fieldOffset = (int)unsafe.objectFieldOffset(field);
            boolean hasHasBit = (next & 4096) == 4096;
            if (hasHasBit && fieldType <= 17) {
               next = info.charAt(i++);
               if (next >= 55296) {
                  int result = next & 8191;

                  int shift;
                  for(shift = 13; (next = info.charAt(i++)) >= 55296; shift += 13) {
                     result |= (next & 8191) << shift;
                  }

                  next = result | next << shift;
               }

               int index = oneofCount * 2 + next / 32;
               Object o = messageInfoObjects[index];
               java.lang.reflect.Field hasBitsField;
               if (o instanceof java.lang.reflect.Field) {
                  hasBitsField = (java.lang.reflect.Field)o;
               } else {
                  hasBitsField = reflectField(messageClass, (String)o);
                  messageInfoObjects[index] = hasBitsField;
               }

               presenceFieldOffset = (int)unsafe.objectFieldOffset(hasBitsField);
               presenceMaskShift = next % 32;
            } else {
               presenceFieldOffset = 1048575;
               presenceMaskShift = 0;
            }

            if (fieldType >= 18 && fieldType <= 49) {
               intArray[repeatedFieldIndex++] = fieldOffset;
            }
         }

         buffer[bufferIndex++] = next;
         buffer[bufferIndex++] = ((next & 512) != 0 ? 536870912 : 0) | ((next & 256) != 0 ? 268435456 : 0) | fieldType << 20 | fieldOffset;
      }

      return new MessageSchema<>(
         buffer,
         objects,
         minFieldNumber,
         maxFieldNumber,
         messageInfo.getDefaultInstance(),
         isProto3,
         false,
         intArray,
         checkInitialized,
         checkInitialized + mapFieldCount,
         newInstanceSchema,
         listFieldSchema,
         unknownFieldSchema,
         extensionSchema,
         mapFieldSchema
      );
   }

   private static java.lang.reflect.Field reflectField(Class<?> messageClass, String fieldName) {
      try {
         return messageClass.getDeclaredField(fieldName);
      } catch (NoSuchFieldException var8) {
         java.lang.reflect.Field[] fields = messageClass.getDeclaredFields();

         for(java.lang.reflect.Field field : fields) {
            if (fieldName.equals(field.getName())) {
               return field;
            }
         }

         throw new RuntimeException("Field " + fieldName + " for " + messageClass.getName() + " not found. Known fields are " + Arrays.toString(fields));
      }
   }

   static <T> MessageSchema<T> newSchemaForMessageInfo(
      StructuralMessageInfo messageInfo,
      NewInstanceSchema newInstanceSchema,
      ListFieldSchema listFieldSchema,
      UnknownFieldSchema<?, ?> unknownFieldSchema,
      ExtensionSchema<?> extensionSchema,
      MapFieldSchema mapFieldSchema
   ) {
      boolean isProto3 = messageInfo.getSyntax() == ProtoSyntax.PROTO3;
      FieldInfo[] fis = messageInfo.getFields();
      int minFieldNumber;
      int maxFieldNumber;
      if (fis.length == 0) {
         minFieldNumber = 0;
         maxFieldNumber = 0;
      } else {
         minFieldNumber = fis[0].getFieldNumber();
         maxFieldNumber = fis[fis.length - 1].getFieldNumber();
      }

      int numEntries = fis.length;
      int[] buffer = new int[numEntries * 3];
      Object[] objects = new Object[numEntries * 2];
      int mapFieldCount = 0;
      int repeatedFieldCount = 0;

      for(FieldInfo fi : fis) {
         if (fi.getType() == FieldType.MAP) {
            ++mapFieldCount;
         } else if (fi.getType().id() >= 18 && fi.getType().id() <= 49) {
            ++repeatedFieldCount;
         }
      }

      int[] mapFieldPositions = mapFieldCount > 0 ? new int[mapFieldCount] : null;
      int[] repeatedFieldOffsets = repeatedFieldCount > 0 ? new int[repeatedFieldCount] : null;
      mapFieldCount = 0;
      repeatedFieldCount = 0;
      int[] checkInitialized = messageInfo.getCheckInitialized();
      if (checkInitialized == null) {
         checkInitialized = EMPTY_INT_ARRAY;
      }

      int checkInitializedIndex = 0;
      int fieldIndex = 0;

      for(int bufferIndex = 0; fieldIndex < fis.length; bufferIndex += 3) {
         FieldInfo fi = fis[fieldIndex];
         int fieldNumber = fi.getFieldNumber();
         storeFieldData(fi, buffer, bufferIndex, objects);
         if (checkInitializedIndex < checkInitialized.length && checkInitialized[checkInitializedIndex] == fieldNumber) {
            checkInitialized[checkInitializedIndex++] = bufferIndex;
         }

         if (fi.getType() == FieldType.MAP) {
            mapFieldPositions[mapFieldCount++] = bufferIndex;
         } else if (fi.getType().id() >= 18 && fi.getType().id() <= 49) {
            repeatedFieldOffsets[repeatedFieldCount++] = (int)UnsafeUtil.objectFieldOffset(fi.getField());
         }

         ++fieldIndex;
      }

      if (mapFieldPositions == null) {
         mapFieldPositions = EMPTY_INT_ARRAY;
      }

      if (repeatedFieldOffsets == null) {
         repeatedFieldOffsets = EMPTY_INT_ARRAY;
      }

      int[] combined = new int[checkInitialized.length + mapFieldPositions.length + repeatedFieldOffsets.length];
      System.arraycopy(checkInitialized, 0, combined, 0, checkInitialized.length);
      System.arraycopy(mapFieldPositions, 0, combined, checkInitialized.length, mapFieldPositions.length);
      System.arraycopy(repeatedFieldOffsets, 0, combined, checkInitialized.length + mapFieldPositions.length, repeatedFieldOffsets.length);
      return new MessageSchema<>(
         buffer,
         objects,
         minFieldNumber,
         maxFieldNumber,
         messageInfo.getDefaultInstance(),
         isProto3,
         true,
         combined,
         checkInitialized.length,
         checkInitialized.length + mapFieldPositions.length,
         newInstanceSchema,
         listFieldSchema,
         unknownFieldSchema,
         extensionSchema,
         mapFieldSchema
      );
   }

   private static void storeFieldData(FieldInfo fi, int[] buffer, int bufferIndex, Object[] objects) {
      OneofInfo oneof = fi.getOneof();
      int fieldOffset;
      int typeId;
      int presenceMaskShift;
      int presenceFieldOffset;
      if (oneof != null) {
         typeId = fi.getType().id() + 51;
         fieldOffset = (int)UnsafeUtil.objectFieldOffset(oneof.getValueField());
         presenceFieldOffset = (int)UnsafeUtil.objectFieldOffset(oneof.getCaseField());
         presenceMaskShift = 0;
      } else {
         FieldType type = fi.getType();
         fieldOffset = (int)UnsafeUtil.objectFieldOffset(fi.getField());
         typeId = type.id();
         if (!type.isList() && !type.isMap()) {
            java.lang.reflect.Field presenceField = fi.getPresenceField();
            if (presenceField == null) {
               presenceFieldOffset = 1048575;
            } else {
               presenceFieldOffset = (int)UnsafeUtil.objectFieldOffset(presenceField);
            }

            presenceMaskShift = Integer.numberOfTrailingZeros(fi.getPresenceMask());
         } else if (fi.getCachedSizeField() == null) {
            presenceFieldOffset = 0;
            presenceMaskShift = 0;
         } else {
            presenceFieldOffset = (int)UnsafeUtil.objectFieldOffset(fi.getCachedSizeField());
            presenceMaskShift = 0;
         }
      }

      buffer[bufferIndex] = fi.getFieldNumber();
      buffer[bufferIndex + 1] = (fi.isEnforceUtf8() ? 536870912 : 0) | (fi.isRequired() ? 268435456 : 0) | typeId << 20 | fieldOffset;
      buffer[bufferIndex + 2] = presenceMaskShift << 20 | presenceFieldOffset;
      Object messageFieldClass = fi.getMessageFieldClass();
      if (fi.getMapDefaultEntry() != null) {
         objects[bufferIndex / 3 * 2] = fi.getMapDefaultEntry();
         if (messageFieldClass != null) {
            objects[bufferIndex / 3 * 2 + 1] = messageFieldClass;
         } else if (fi.getEnumVerifier() != null) {
            objects[bufferIndex / 3 * 2 + 1] = fi.getEnumVerifier();
         }
      } else if (messageFieldClass != null) {
         objects[bufferIndex / 3 * 2 + 1] = messageFieldClass;
      } else if (fi.getEnumVerifier() != null) {
         objects[bufferIndex / 3 * 2 + 1] = fi.getEnumVerifier();
      }

   }

   @Override
   public T newInstance() {
      return (T)this.newInstanceSchema.newInstance(this.defaultInstance);
   }

   @Override
   public boolean equals(T message, T other) {
      int bufferLength = this.buffer.length;

      for(int pos = 0; pos < bufferLength; pos += 3) {
         if (!this.equals(message, other, pos)) {
            return false;
         }
      }

      Object messageUnknown = this.unknownFieldSchema.getFromMessage(message);
      Object otherUnknown = this.unknownFieldSchema.getFromMessage(other);
      if (!messageUnknown.equals(otherUnknown)) {
         return false;
      } else if (this.hasExtensions) {
         FieldSet<?> messageExtensions = this.extensionSchema.getExtensions(message);
         FieldSet<?> otherExtensions = this.extensionSchema.getExtensions(other);
         return messageExtensions.equals(otherExtensions);
      } else {
         return true;
      }
   }

   private boolean equals(T message, T other, int pos) {
      int typeAndOffset = this.typeAndOffsetAt(pos);
      long offset = offset(typeAndOffset);
      switch(type(typeAndOffset)) {
         case 0:
            return this.arePresentForEquals(message, other, pos)
               && Double.doubleToLongBits(UnsafeUtil.getDouble(message, offset)) == Double.doubleToLongBits(UnsafeUtil.getDouble(other, offset));
         case 1:
            return this.arePresentForEquals(message, other, pos)
               && Float.floatToIntBits(UnsafeUtil.getFloat(message, offset)) == Float.floatToIntBits(UnsafeUtil.getFloat(other, offset));
         case 2:
            return this.arePresentForEquals(message, other, pos) && UnsafeUtil.getLong(message, offset) == UnsafeUtil.getLong(other, offset);
         case 3:
            return this.arePresentForEquals(message, other, pos) && UnsafeUtil.getLong(message, offset) == UnsafeUtil.getLong(other, offset);
         case 4:
            return this.arePresentForEquals(message, other, pos) && UnsafeUtil.getInt(message, offset) == UnsafeUtil.getInt(other, offset);
         case 5:
            return this.arePresentForEquals(message, other, pos) && UnsafeUtil.getLong(message, offset) == UnsafeUtil.getLong(other, offset);
         case 6:
            return this.arePresentForEquals(message, other, pos) && UnsafeUtil.getInt(message, offset) == UnsafeUtil.getInt(other, offset);
         case 7:
            return this.arePresentForEquals(message, other, pos) && UnsafeUtil.getBoolean(message, offset) == UnsafeUtil.getBoolean(other, offset);
         case 8:
            return this.arePresentForEquals(message, other, pos)
               && SchemaUtil.safeEquals(UnsafeUtil.getObject(message, offset), UnsafeUtil.getObject(other, offset));
         case 9:
            return this.arePresentForEquals(message, other, pos)
               && SchemaUtil.safeEquals(UnsafeUtil.getObject(message, offset), UnsafeUtil.getObject(other, offset));
         case 10:
            return this.arePresentForEquals(message, other, pos)
               && SchemaUtil.safeEquals(UnsafeUtil.getObject(message, offset), UnsafeUtil.getObject(other, offset));
         case 11:
            return this.arePresentForEquals(message, other, pos) && UnsafeUtil.getInt(message, offset) == UnsafeUtil.getInt(other, offset);
         case 12:
            return this.arePresentForEquals(message, other, pos) && UnsafeUtil.getInt(message, offset) == UnsafeUtil.getInt(other, offset);
         case 13:
            return this.arePresentForEquals(message, other, pos) && UnsafeUtil.getInt(message, offset) == UnsafeUtil.getInt(other, offset);
         case 14:
            return this.arePresentForEquals(message, other, pos) && UnsafeUtil.getLong(message, offset) == UnsafeUtil.getLong(other, offset);
         case 15:
            return this.arePresentForEquals(message, other, pos) && UnsafeUtil.getInt(message, offset) == UnsafeUtil.getInt(other, offset);
         case 16:
            return this.arePresentForEquals(message, other, pos) && UnsafeUtil.getLong(message, offset) == UnsafeUtil.getLong(other, offset);
         case 17:
            return this.arePresentForEquals(message, other, pos)
               && SchemaUtil.safeEquals(UnsafeUtil.getObject(message, offset), UnsafeUtil.getObject(other, offset));
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 30:
         case 31:
         case 32:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         case 41:
         case 42:
         case 43:
         case 44:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
            return SchemaUtil.safeEquals(UnsafeUtil.getObject(message, offset), UnsafeUtil.getObject(other, offset));
         case 50:
            return SchemaUtil.safeEquals(UnsafeUtil.getObject(message, offset), UnsafeUtil.getObject(other, offset));
         case 51:
         case 52:
         case 53:
         case 54:
         case 55:
         case 56:
         case 57:
         case 58:
         case 59:
         case 60:
         case 61:
         case 62:
         case 63:
         case 64:
         case 65:
         case 66:
         case 67:
         case 68:
            return this.isOneofCaseEqual(message, other, pos)
               && SchemaUtil.safeEquals(UnsafeUtil.getObject(message, offset), UnsafeUtil.getObject(other, offset));
         default:
            return true;
      }
   }

   @Override
   public int hashCode(T message) {
      int hashCode = 0;
      int bufferLength = this.buffer.length;

      for(int pos = 0; pos < bufferLength; pos += 3) {
         int typeAndOffset = this.typeAndOffsetAt(pos);
         int entryNumber = this.numberAt(pos);
         long offset = offset(typeAndOffset);
         switch(type(typeAndOffset)) {
            case 0:
               hashCode = hashCode * 53 + Internal.hashLong(Double.doubleToLongBits(UnsafeUtil.getDouble(message, offset)));
               break;
            case 1:
               hashCode = hashCode * 53 + Float.floatToIntBits(UnsafeUtil.getFloat(message, offset));
               break;
            case 2:
               hashCode = hashCode * 53 + Internal.hashLong(UnsafeUtil.getLong(message, offset));
               break;
            case 3:
               hashCode = hashCode * 53 + Internal.hashLong(UnsafeUtil.getLong(message, offset));
               break;
            case 4:
               hashCode = hashCode * 53 + UnsafeUtil.getInt(message, offset);
               break;
            case 5:
               hashCode = hashCode * 53 + Internal.hashLong(UnsafeUtil.getLong(message, offset));
               break;
            case 6:
               hashCode = hashCode * 53 + UnsafeUtil.getInt(message, offset);
               break;
            case 7:
               hashCode = hashCode * 53 + Internal.hashBoolean(UnsafeUtil.getBoolean(message, offset));
               break;
            case 8:
               hashCode = hashCode * 53 + ((String)UnsafeUtil.getObject(message, offset)).hashCode();
               break;
            case 9:
               int protoHash = 37;
               Object submessage = UnsafeUtil.getObject(message, offset);
               if (submessage != null) {
                  protoHash = submessage.hashCode();
               }

               hashCode = 53 * hashCode + protoHash;
               break;
            case 10:
               hashCode = hashCode * 53 + UnsafeUtil.getObject(message, offset).hashCode();
               break;
            case 11:
               hashCode = hashCode * 53 + UnsafeUtil.getInt(message, offset);
               break;
            case 12:
               hashCode = hashCode * 53 + UnsafeUtil.getInt(message, offset);
               break;
            case 13:
               hashCode = hashCode * 53 + UnsafeUtil.getInt(message, offset);
               break;
            case 14:
               hashCode = hashCode * 53 + Internal.hashLong(UnsafeUtil.getLong(message, offset));
               break;
            case 15:
               hashCode = hashCode * 53 + UnsafeUtil.getInt(message, offset);
               break;
            case 16:
               hashCode = hashCode * 53 + Internal.hashLong(UnsafeUtil.getLong(message, offset));
               break;
            case 17:
               int protoHash = 37;
               Object submessage = UnsafeUtil.getObject(message, offset);
               if (submessage != null) {
                  protoHash = submessage.hashCode();
               }

               hashCode = 53 * hashCode + protoHash;
               break;
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            case 48:
            case 49:
               hashCode = hashCode * 53 + UnsafeUtil.getObject(message, offset).hashCode();
               break;
            case 50:
               hashCode = hashCode * 53 + UnsafeUtil.getObject(message, offset).hashCode();
               break;
            case 51:
               if (this.isOneofPresent(message, entryNumber, pos)) {
                  hashCode = hashCode * 53 + Internal.hashLong(Double.doubleToLongBits(oneofDoubleAt(message, offset)));
               }
               break;
            case 52:
               if (this.isOneofPresent(message, entryNumber, pos)) {
                  hashCode = hashCode * 53 + Float.floatToIntBits(oneofFloatAt(message, offset));
               }
               break;
            case 53:
               if (this.isOneofPresent(message, entryNumber, pos)) {
                  hashCode = hashCode * 53 + Internal.hashLong(oneofLongAt(message, offset));
               }
               break;
            case 54:
               if (this.isOneofPresent(message, entryNumber, pos)) {
                  hashCode = hashCode * 53 + Internal.hashLong(oneofLongAt(message, offset));
               }
               break;
            case 55:
               if (this.isOneofPresent(message, entryNumber, pos)) {
                  hashCode = hashCode * 53 + oneofIntAt(message, offset);
               }
               break;
            case 56:
               if (this.isOneofPresent(message, entryNumber, pos)) {
                  hashCode = hashCode * 53 + Internal.hashLong(oneofLongAt(message, offset));
               }
               break;
            case 57:
               if (this.isOneofPresent(message, entryNumber, pos)) {
                  hashCode = hashCode * 53 + oneofIntAt(message, offset);
               }
               break;
            case 58:
               if (this.isOneofPresent(message, entryNumber, pos)) {
                  hashCode = hashCode * 53 + Internal.hashBoolean(oneofBooleanAt(message, offset));
               }
               break;
            case 59:
               if (this.isOneofPresent(message, entryNumber, pos)) {
                  hashCode = hashCode * 53 + ((String)UnsafeUtil.getObject(message, offset)).hashCode();
               }
               break;
            case 60:
               if (this.isOneofPresent(message, entryNumber, pos)) {
                  Object submessage = UnsafeUtil.getObject(message, offset);
                  hashCode = 53 * hashCode + submessage.hashCode();
               }
               break;
            case 61:
               if (this.isOneofPresent(message, entryNumber, pos)) {
                  hashCode = hashCode * 53 + UnsafeUtil.getObject(message, offset).hashCode();
               }
               break;
            case 62:
               if (this.isOneofPresent(message, entryNumber, pos)) {
                  hashCode = hashCode * 53 + oneofIntAt(message, offset);
               }
               break;
            case 63:
               if (this.isOneofPresent(message, entryNumber, pos)) {
                  hashCode = hashCode * 53 + oneofIntAt(message, offset);
               }
               break;
            case 64:
               if (this.isOneofPresent(message, entryNumber, pos)) {
                  hashCode = hashCode * 53 + oneofIntAt(message, offset);
               }
               break;
            case 65:
               if (this.isOneofPresent(message, entryNumber, pos)) {
                  hashCode = hashCode * 53 + Internal.hashLong(oneofLongAt(message, offset));
               }
               break;
            case 66:
               if (this.isOneofPresent(message, entryNumber, pos)) {
                  hashCode = hashCode * 53 + oneofIntAt(message, offset);
               }
               break;
            case 67:
               if (this.isOneofPresent(message, entryNumber, pos)) {
                  hashCode = hashCode * 53 + Internal.hashLong(oneofLongAt(message, offset));
               }
               break;
            case 68:
               if (this.isOneofPresent(message, entryNumber, pos)) {
                  Object submessage = UnsafeUtil.getObject(message, offset);
                  hashCode = 53 * hashCode + submessage.hashCode();
               }
         }
      }

      hashCode = hashCode * 53 + this.unknownFieldSchema.getFromMessage(message).hashCode();
      if (this.hasExtensions) {
         hashCode = hashCode * 53 + this.extensionSchema.getExtensions(message).hashCode();
      }

      return hashCode;
   }

   @Override
   public void mergeFrom(T message, T other) {
      if (other == null) {
         throw new NullPointerException();
      } else {
         for(int i = 0; i < this.buffer.length; i += 3) {
            this.mergeSingleField(message, other, i);
         }

         SchemaUtil.mergeUnknownFields(this.unknownFieldSchema, message, other);
         if (this.hasExtensions) {
            SchemaUtil.mergeExtensions(this.extensionSchema, message, other);
         }

      }
   }

   private void mergeSingleField(T message, T other, int pos) {
      int typeAndOffset = this.typeAndOffsetAt(pos);
      long offset = offset(typeAndOffset);
      int number = this.numberAt(pos);
      switch(type(typeAndOffset)) {
         case 0:
            if (this.isFieldPresent(other, pos)) {
               UnsafeUtil.putDouble(message, offset, UnsafeUtil.getDouble(other, offset));
               this.setFieldPresent(message, pos);
            }
            break;
         case 1:
            if (this.isFieldPresent(other, pos)) {
               UnsafeUtil.putFloat(message, offset, UnsafeUtil.getFloat(other, offset));
               this.setFieldPresent(message, pos);
            }
            break;
         case 2:
            if (this.isFieldPresent(other, pos)) {
               UnsafeUtil.putLong(message, offset, UnsafeUtil.getLong(other, offset));
               this.setFieldPresent(message, pos);
            }
            break;
         case 3:
            if (this.isFieldPresent(other, pos)) {
               UnsafeUtil.putLong(message, offset, UnsafeUtil.getLong(other, offset));
               this.setFieldPresent(message, pos);
            }
            break;
         case 4:
            if (this.isFieldPresent(other, pos)) {
               UnsafeUtil.putInt(message, offset, UnsafeUtil.getInt(other, offset));
               this.setFieldPresent(message, pos);
            }
            break;
         case 5:
            if (this.isFieldPresent(other, pos)) {
               UnsafeUtil.putLong(message, offset, UnsafeUtil.getLong(other, offset));
               this.setFieldPresent(message, pos);
            }
            break;
         case 6:
            if (this.isFieldPresent(other, pos)) {
               UnsafeUtil.putInt(message, offset, UnsafeUtil.getInt(other, offset));
               this.setFieldPresent(message, pos);
            }
            break;
         case 7:
            if (this.isFieldPresent(other, pos)) {
               UnsafeUtil.putBoolean(message, offset, UnsafeUtil.getBoolean(other, offset));
               this.setFieldPresent(message, pos);
            }
            break;
         case 8:
            if (this.isFieldPresent(other, pos)) {
               UnsafeUtil.putObject(message, offset, UnsafeUtil.getObject(other, offset));
               this.setFieldPresent(message, pos);
            }
            break;
         case 9:
            this.mergeMessage(message, other, pos);
            break;
         case 10:
            if (this.isFieldPresent(other, pos)) {
               UnsafeUtil.putObject(message, offset, UnsafeUtil.getObject(other, offset));
               this.setFieldPresent(message, pos);
            }
            break;
         case 11:
            if (this.isFieldPresent(other, pos)) {
               UnsafeUtil.putInt(message, offset, UnsafeUtil.getInt(other, offset));
               this.setFieldPresent(message, pos);
            }
            break;
         case 12:
            if (this.isFieldPresent(other, pos)) {
               UnsafeUtil.putInt(message, offset, UnsafeUtil.getInt(other, offset));
               this.setFieldPresent(message, pos);
            }
            break;
         case 13:
            if (this.isFieldPresent(other, pos)) {
               UnsafeUtil.putInt(message, offset, UnsafeUtil.getInt(other, offset));
               this.setFieldPresent(message, pos);
            }
            break;
         case 14:
            if (this.isFieldPresent(other, pos)) {
               UnsafeUtil.putLong(message, offset, UnsafeUtil.getLong(other, offset));
               this.setFieldPresent(message, pos);
            }
            break;
         case 15:
            if (this.isFieldPresent(other, pos)) {
               UnsafeUtil.putInt(message, offset, UnsafeUtil.getInt(other, offset));
               this.setFieldPresent(message, pos);
            }
            break;
         case 16:
            if (this.isFieldPresent(other, pos)) {
               UnsafeUtil.putLong(message, offset, UnsafeUtil.getLong(other, offset));
               this.setFieldPresent(message, pos);
            }
            break;
         case 17:
            this.mergeMessage(message, other, pos);
            break;
         case 18:
         case 19:
         case 20:
         case 21:
         case 22:
         case 23:
         case 24:
         case 25:
         case 26:
         case 27:
         case 28:
         case 29:
         case 30:
         case 31:
         case 32:
         case 33:
         case 34:
         case 35:
         case 36:
         case 37:
         case 38:
         case 39:
         case 40:
         case 41:
         case 42:
         case 43:
         case 44:
         case 45:
         case 46:
         case 47:
         case 48:
         case 49:
            this.listFieldSchema.mergeListsAt(message, other, offset);
            break;
         case 50:
            SchemaUtil.mergeMap(this.mapFieldSchema, message, other, offset);
            break;
         case 51:
         case 52:
         case 53:
         case 54:
         case 55:
         case 56:
         case 57:
         case 58:
         case 59:
            if (this.isOneofPresent(other, number, pos)) {
               UnsafeUtil.putObject(message, offset, UnsafeUtil.getObject(other, offset));
               this.setOneofPresent(message, number, pos);
            }
            break;
         case 60:
            this.mergeOneofMessage(message, other, pos);
            break;
         case 61:
         case 62:
         case 63:
         case 64:
         case 65:
         case 66:
         case 67:
            if (this.isOneofPresent(other, number, pos)) {
               UnsafeUtil.putObject(message, offset, UnsafeUtil.getObject(other, offset));
               this.setOneofPresent(message, number, pos);
            }
            break;
         case 68:
            this.mergeOneofMessage(message, other, pos);
      }

   }

   private void mergeMessage(T message, T other, int pos) {
      int typeAndOffset = this.typeAndOffsetAt(pos);
      long offset = offset(typeAndOffset);
      if (this.isFieldPresent(other, pos)) {
         Object mine = UnsafeUtil.getObject(message, offset);
         Object theirs = UnsafeUtil.getObject(other, offset);
         if (mine != null && theirs != null) {
            Object merged = Internal.mergeMessage(mine, theirs);
            UnsafeUtil.putObject(message, offset, merged);
            this.setFieldPresent(message, pos);
         } else if (theirs != null) {
            UnsafeUtil.putObject(message, offset, theirs);
            this.setFieldPresent(message, pos);
         }

      }
   }

   private void mergeOneofMessage(T message, T other, int pos) {
      int typeAndOffset = this.typeAndOffsetAt(pos);
      int number = this.numberAt(pos);
      long offset = offset(typeAndOffset);
      if (this.isOneofPresent(other, number, pos)) {
         Object mine = null;
         if (this.isOneofPresent(message, number, pos)) {
            mine = UnsafeUtil.getObject(message, offset);
         }

         Object theirs = UnsafeUtil.getObject(other, offset);
         if (mine != null && theirs != null) {
            Object merged = Internal.mergeMessage(mine, theirs);
            UnsafeUtil.putObject(message, offset, merged);
            this.setOneofPresent(message, number, pos);
         } else if (theirs != null) {
            UnsafeUtil.putObject(message, offset, theirs);
            this.setOneofPresent(message, number, pos);
         }

      }
   }

   @Override
   public int getSerializedSize(T message) {
      return this.proto3 ? this.getSerializedSizeProto3(message) : this.getSerializedSizeProto2(message);
   }

   private int getSerializedSizeProto2(T message) {
      int size = 0;
      Unsafe unsafe = UNSAFE;
      int currentPresenceFieldOffset = 1048575;
      int currentPresenceField = 0;

      for(int i = 0; i < this.buffer.length; i += 3) {
         int typeAndOffset = this.typeAndOffsetAt(i);
         int number = this.numberAt(i);
         int fieldType = type(typeAndOffset);
         int presenceMaskAndOffset = 0;
         int presenceMask = 0;
         if (fieldType <= 17) {
            presenceMaskAndOffset = this.buffer[i + 2];
            int presenceFieldOffset = presenceMaskAndOffset & 1048575;
            presenceMask = 1 << (presenceMaskAndOffset >>> 20);
            if (presenceFieldOffset != currentPresenceFieldOffset) {
               currentPresenceFieldOffset = presenceFieldOffset;
               currentPresenceField = unsafe.getInt(message, (long)presenceFieldOffset);
            }
         } else if (this.useCachedSizeField && fieldType >= FieldType.DOUBLE_LIST_PACKED.id() && fieldType <= FieldType.SINT64_LIST_PACKED.id()) {
            presenceMaskAndOffset = this.buffer[i + 2] & 1048575;
         }

         long offset = offset(typeAndOffset);
         switch(fieldType) {
            case 0:
               if ((currentPresenceField & presenceMask) != 0) {
                  size += CodedOutputStream.computeDoubleSize(number, 0.0);
               }
               break;
            case 1:
               if ((currentPresenceField & presenceMask) != 0) {
                  size += CodedOutputStream.computeFloatSize(number, 0.0F);
               }
               break;
            case 2:
               if ((currentPresenceField & presenceMask) != 0) {
                  size += CodedOutputStream.computeInt64Size(number, unsafe.getLong(message, offset));
               }
               break;
            case 3:
               if ((currentPresenceField & presenceMask) != 0) {
                  size += CodedOutputStream.computeUInt64Size(number, unsafe.getLong(message, offset));
               }
               break;
            case 4:
               if ((currentPresenceField & presenceMask) != 0) {
                  size += CodedOutputStream.computeInt32Size(number, unsafe.getInt(message, offset));
               }
               break;
            case 5:
               if ((currentPresenceField & presenceMask) != 0) {
                  size += CodedOutputStream.computeFixed64Size(number, 0L);
               }
               break;
            case 6:
               if ((currentPresenceField & presenceMask) != 0) {
                  size += CodedOutputStream.computeFixed32Size(number, 0);
               }
               break;
            case 7:
               if ((currentPresenceField & presenceMask) != 0) {
                  size += CodedOutputStream.computeBoolSize(number, true);
               }
               break;
            case 8:
               if ((currentPresenceField & presenceMask) != 0) {
                  Object value = unsafe.getObject(message, offset);
                  if (value instanceof ByteString) {
                     size += CodedOutputStream.computeBytesSize(number, (ByteString)value);
                  } else {
                     size += CodedOutputStream.computeStringSize(number, (String)value);
                  }
               }
               break;
            case 9:
               if ((currentPresenceField & presenceMask) != 0) {
                  Object value = unsafe.getObject(message, offset);
                  size += SchemaUtil.computeSizeMessage(number, value, this.getMessageFieldSchema(i));
               }
               break;
            case 10:
               if ((currentPresenceField & presenceMask) != 0) {
                  ByteString value = (ByteString)unsafe.getObject(message, offset);
                  size += CodedOutputStream.computeBytesSize(number, value);
               }
               break;
            case 11:
               if ((currentPresenceField & presenceMask) != 0) {
                  size += CodedOutputStream.computeUInt32Size(number, unsafe.getInt(message, offset));
               }
               break;
            case 12:
               if ((currentPresenceField & presenceMask) != 0) {
                  size += CodedOutputStream.computeEnumSize(number, unsafe.getInt(message, offset));
               }
               break;
            case 13:
               if ((currentPresenceField & presenceMask) != 0) {
                  size += CodedOutputStream.computeSFixed32Size(number, 0);
               }
               break;
            case 14:
               if ((currentPresenceField & presenceMask) != 0) {
                  size += CodedOutputStream.computeSFixed64Size(number, 0L);
               }
               break;
            case 15:
               if ((currentPresenceField & presenceMask) != 0) {
                  size += CodedOutputStream.computeSInt32Size(number, unsafe.getInt(message, offset));
               }
               break;
            case 16:
               if ((currentPresenceField & presenceMask) != 0) {
                  size += CodedOutputStream.computeSInt64Size(number, unsafe.getLong(message, offset));
               }
               break;
            case 17:
               if ((currentPresenceField & presenceMask) != 0) {
                  size += CodedOutputStream.computeGroupSize(number, (MessageLite)unsafe.getObject(message, offset), this.getMessageFieldSchema(i));
               }
               break;
            case 18:
               size += SchemaUtil.computeSizeFixed64List(number, (List<?>)unsafe.getObject(message, offset), false);
               break;
            case 19:
               size += SchemaUtil.computeSizeFixed32List(number, (List<?>)unsafe.getObject(message, offset), false);
               break;
            case 20:
               size += SchemaUtil.computeSizeInt64List(number, (List<Long>)unsafe.getObject(message, offset), false);
               break;
            case 21:
               size += SchemaUtil.computeSizeUInt64List(number, (List<Long>)unsafe.getObject(message, offset), false);
               break;
            case 22:
               size += SchemaUtil.computeSizeInt32List(number, (List<Integer>)unsafe.getObject(message, offset), false);
               break;
            case 23:
               size += SchemaUtil.computeSizeFixed64List(number, (List<?>)unsafe.getObject(message, offset), false);
               break;
            case 24:
               size += SchemaUtil.computeSizeFixed32List(number, (List<?>)unsafe.getObject(message, offset), false);
               break;
            case 25:
               size += SchemaUtil.computeSizeBoolList(number, (List<?>)unsafe.getObject(message, offset), false);
               break;
            case 26:
               size += SchemaUtil.computeSizeStringList(number, (List<?>)unsafe.getObject(message, offset));
               break;
            case 27:
               size += SchemaUtil.computeSizeMessageList(number, (List<?>)unsafe.getObject(message, offset), this.getMessageFieldSchema(i));
               break;
            case 28:
               size += SchemaUtil.computeSizeByteStringList(number, (List<ByteString>)unsafe.getObject(message, offset));
               break;
            case 29:
               size += SchemaUtil.computeSizeUInt32List(number, (List<Integer>)unsafe.getObject(message, offset), false);
               break;
            case 30:
               size += SchemaUtil.computeSizeEnumList(number, (List<Integer>)unsafe.getObject(message, offset), false);
               break;
            case 31:
               size += SchemaUtil.computeSizeFixed32List(number, (List<?>)unsafe.getObject(message, offset), false);
               break;
            case 32:
               size += SchemaUtil.computeSizeFixed64List(number, (List<?>)unsafe.getObject(message, offset), false);
               break;
            case 33:
               size += SchemaUtil.computeSizeSInt32List(number, (List<Integer>)unsafe.getObject(message, offset), false);
               break;
            case 34:
               size += SchemaUtil.computeSizeSInt64List(number, (List<Long>)unsafe.getObject(message, offset), false);
               break;
            case 35:
               int fieldSize = SchemaUtil.computeSizeFixed64ListNoTag((List<?>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)presenceMaskAndOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 36:
               int fieldSize = SchemaUtil.computeSizeFixed32ListNoTag((List<?>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)presenceMaskAndOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 37:
               int fieldSize = SchemaUtil.computeSizeInt64ListNoTag((List<Long>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)presenceMaskAndOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 38:
               int fieldSize = SchemaUtil.computeSizeUInt64ListNoTag((List<Long>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)presenceMaskAndOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 39:
               int fieldSize = SchemaUtil.computeSizeInt32ListNoTag((List<Integer>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)presenceMaskAndOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 40:
               int fieldSize = SchemaUtil.computeSizeFixed64ListNoTag((List<?>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)presenceMaskAndOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 41:
               int fieldSize = SchemaUtil.computeSizeFixed32ListNoTag((List<?>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)presenceMaskAndOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 42:
               int fieldSize = SchemaUtil.computeSizeBoolListNoTag((List<?>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)presenceMaskAndOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 43:
               int fieldSize = SchemaUtil.computeSizeUInt32ListNoTag((List<Integer>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)presenceMaskAndOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 44:
               int fieldSize = SchemaUtil.computeSizeEnumListNoTag((List<Integer>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)presenceMaskAndOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 45:
               int fieldSize = SchemaUtil.computeSizeFixed32ListNoTag((List<?>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)presenceMaskAndOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 46:
               int fieldSize = SchemaUtil.computeSizeFixed64ListNoTag((List<?>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)presenceMaskAndOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 47:
               int fieldSize = SchemaUtil.computeSizeSInt32ListNoTag((List<Integer>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)presenceMaskAndOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 48:
               int fieldSize = SchemaUtil.computeSizeSInt64ListNoTag((List<Long>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)presenceMaskAndOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 49:
               size += SchemaUtil.computeSizeGroupList(number, (List<MessageLite>)unsafe.getObject(message, offset), this.getMessageFieldSchema(i));
               break;
            case 50:
               size += this.mapFieldSchema.getSerializedSize(number, unsafe.getObject(message, offset), this.getMapFieldDefaultEntry(i));
               break;
            case 51:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeDoubleSize(number, 0.0);
               }
               break;
            case 52:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeFloatSize(number, 0.0F);
               }
               break;
            case 53:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeInt64Size(number, oneofLongAt(message, offset));
               }
               break;
            case 54:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeUInt64Size(number, oneofLongAt(message, offset));
               }
               break;
            case 55:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeInt32Size(number, oneofIntAt(message, offset));
               }
               break;
            case 56:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeFixed64Size(number, 0L);
               }
               break;
            case 57:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeFixed32Size(number, 0);
               }
               break;
            case 58:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeBoolSize(number, true);
               }
               break;
            case 59:
               if (this.isOneofPresent(message, number, i)) {
                  Object value = unsafe.getObject(message, offset);
                  if (value instanceof ByteString) {
                     size += CodedOutputStream.computeBytesSize(number, (ByteString)value);
                  } else {
                     size += CodedOutputStream.computeStringSize(number, (String)value);
                  }
               }
               break;
            case 60:
               if (this.isOneofPresent(message, number, i)) {
                  Object value = unsafe.getObject(message, offset);
                  size += SchemaUtil.computeSizeMessage(number, value, this.getMessageFieldSchema(i));
               }
               break;
            case 61:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeBytesSize(number, (ByteString)unsafe.getObject(message, offset));
               }
               break;
            case 62:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeUInt32Size(number, oneofIntAt(message, offset));
               }
               break;
            case 63:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeEnumSize(number, oneofIntAt(message, offset));
               }
               break;
            case 64:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeSFixed32Size(number, 0);
               }
               break;
            case 65:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeSFixed64Size(number, 0L);
               }
               break;
            case 66:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeSInt32Size(number, oneofIntAt(message, offset));
               }
               break;
            case 67:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeSInt64Size(number, oneofLongAt(message, offset));
               }
               break;
            case 68:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeGroupSize(number, (MessageLite)unsafe.getObject(message, offset), this.getMessageFieldSchema(i));
               }
         }
      }

      size += this.getUnknownFieldsSerializedSize(this.unknownFieldSchema, message);
      if (this.hasExtensions) {
         size += this.extensionSchema.getExtensions(message).getSerializedSize();
      }

      return size;
   }

   private int getSerializedSizeProto3(T message) {
      Unsafe unsafe = UNSAFE;
      int size = 0;

      for(int i = 0; i < this.buffer.length; i += 3) {
         int typeAndOffset = this.typeAndOffsetAt(i);
         int fieldType = type(typeAndOffset);
         int number = this.numberAt(i);
         long offset = offset(typeAndOffset);
         int cachedSizeOffset = fieldType >= FieldType.DOUBLE_LIST_PACKED.id() && fieldType <= FieldType.SINT64_LIST_PACKED.id()
            ? this.buffer[i + 2] & 1048575
            : 0;
         switch(fieldType) {
            case 0:
               if (this.isFieldPresent(message, i)) {
                  size += CodedOutputStream.computeDoubleSize(number, 0.0);
               }
               break;
            case 1:
               if (this.isFieldPresent(message, i)) {
                  size += CodedOutputStream.computeFloatSize(number, 0.0F);
               }
               break;
            case 2:
               if (this.isFieldPresent(message, i)) {
                  size += CodedOutputStream.computeInt64Size(number, UnsafeUtil.getLong(message, offset));
               }
               break;
            case 3:
               if (this.isFieldPresent(message, i)) {
                  size += CodedOutputStream.computeUInt64Size(number, UnsafeUtil.getLong(message, offset));
               }
               break;
            case 4:
               if (this.isFieldPresent(message, i)) {
                  size += CodedOutputStream.computeInt32Size(number, UnsafeUtil.getInt(message, offset));
               }
               break;
            case 5:
               if (this.isFieldPresent(message, i)) {
                  size += CodedOutputStream.computeFixed64Size(number, 0L);
               }
               break;
            case 6:
               if (this.isFieldPresent(message, i)) {
                  size += CodedOutputStream.computeFixed32Size(number, 0);
               }
               break;
            case 7:
               if (this.isFieldPresent(message, i)) {
                  size += CodedOutputStream.computeBoolSize(number, true);
               }
               break;
            case 8:
               if (this.isFieldPresent(message, i)) {
                  Object value = UnsafeUtil.getObject(message, offset);
                  if (value instanceof ByteString) {
                     size += CodedOutputStream.computeBytesSize(number, (ByteString)value);
                  } else {
                     size += CodedOutputStream.computeStringSize(number, (String)value);
                  }
               }
               break;
            case 9:
               if (this.isFieldPresent(message, i)) {
                  Object value = UnsafeUtil.getObject(message, offset);
                  size += SchemaUtil.computeSizeMessage(number, value, this.getMessageFieldSchema(i));
               }
               break;
            case 10:
               if (this.isFieldPresent(message, i)) {
                  ByteString value = (ByteString)UnsafeUtil.getObject(message, offset);
                  size += CodedOutputStream.computeBytesSize(number, value);
               }
               break;
            case 11:
               if (this.isFieldPresent(message, i)) {
                  size += CodedOutputStream.computeUInt32Size(number, UnsafeUtil.getInt(message, offset));
               }
               break;
            case 12:
               if (this.isFieldPresent(message, i)) {
                  size += CodedOutputStream.computeEnumSize(number, UnsafeUtil.getInt(message, offset));
               }
               break;
            case 13:
               if (this.isFieldPresent(message, i)) {
                  size += CodedOutputStream.computeSFixed32Size(number, 0);
               }
               break;
            case 14:
               if (this.isFieldPresent(message, i)) {
                  size += CodedOutputStream.computeSFixed64Size(number, 0L);
               }
               break;
            case 15:
               if (this.isFieldPresent(message, i)) {
                  size += CodedOutputStream.computeSInt32Size(number, UnsafeUtil.getInt(message, offset));
               }
               break;
            case 16:
               if (this.isFieldPresent(message, i)) {
                  size += CodedOutputStream.computeSInt64Size(number, UnsafeUtil.getLong(message, offset));
               }
               break;
            case 17:
               if (this.isFieldPresent(message, i)) {
                  size += CodedOutputStream.computeGroupSize(number, (MessageLite)UnsafeUtil.getObject(message, offset), this.getMessageFieldSchema(i));
               }
               break;
            case 18:
               size += SchemaUtil.computeSizeFixed64List(number, listAt(message, offset), false);
               break;
            case 19:
               size += SchemaUtil.computeSizeFixed32List(number, listAt(message, offset), false);
               break;
            case 20:
               size += SchemaUtil.computeSizeInt64List(number, listAt(message, offset), false);
               break;
            case 21:
               size += SchemaUtil.computeSizeUInt64List(number, listAt(message, offset), false);
               break;
            case 22:
               size += SchemaUtil.computeSizeInt32List(number, listAt(message, offset), false);
               break;
            case 23:
               size += SchemaUtil.computeSizeFixed64List(number, listAt(message, offset), false);
               break;
            case 24:
               size += SchemaUtil.computeSizeFixed32List(number, listAt(message, offset), false);
               break;
            case 25:
               size += SchemaUtil.computeSizeBoolList(number, listAt(message, offset), false);
               break;
            case 26:
               size += SchemaUtil.computeSizeStringList(number, listAt(message, offset));
               break;
            case 27:
               size += SchemaUtil.computeSizeMessageList(number, listAt(message, offset), this.getMessageFieldSchema(i));
               break;
            case 28:
               size += SchemaUtil.computeSizeByteStringList(number, listAt(message, offset));
               break;
            case 29:
               size += SchemaUtil.computeSizeUInt32List(number, listAt(message, offset), false);
               break;
            case 30:
               size += SchemaUtil.computeSizeEnumList(number, listAt(message, offset), false);
               break;
            case 31:
               size += SchemaUtil.computeSizeFixed32List(number, listAt(message, offset), false);
               break;
            case 32:
               size += SchemaUtil.computeSizeFixed64List(number, listAt(message, offset), false);
               break;
            case 33:
               size += SchemaUtil.computeSizeSInt32List(number, listAt(message, offset), false);
               break;
            case 34:
               size += SchemaUtil.computeSizeSInt64List(number, listAt(message, offset), false);
               break;
            case 35:
               int fieldSize = SchemaUtil.computeSizeFixed64ListNoTag((List<?>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)cachedSizeOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 36:
               int fieldSize = SchemaUtil.computeSizeFixed32ListNoTag((List<?>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)cachedSizeOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 37:
               int fieldSize = SchemaUtil.computeSizeInt64ListNoTag((List<Long>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)cachedSizeOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 38:
               int fieldSize = SchemaUtil.computeSizeUInt64ListNoTag((List<Long>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)cachedSizeOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 39:
               int fieldSize = SchemaUtil.computeSizeInt32ListNoTag((List<Integer>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)cachedSizeOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 40:
               int fieldSize = SchemaUtil.computeSizeFixed64ListNoTag((List<?>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)cachedSizeOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 41:
               int fieldSize = SchemaUtil.computeSizeFixed32ListNoTag((List<?>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)cachedSizeOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 42:
               int fieldSize = SchemaUtil.computeSizeBoolListNoTag((List<?>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)cachedSizeOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 43:
               int fieldSize = SchemaUtil.computeSizeUInt32ListNoTag((List<Integer>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)cachedSizeOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 44:
               int fieldSize = SchemaUtil.computeSizeEnumListNoTag((List<Integer>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)cachedSizeOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 45:
               int fieldSize = SchemaUtil.computeSizeFixed32ListNoTag((List<?>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)cachedSizeOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 46:
               int fieldSize = SchemaUtil.computeSizeFixed64ListNoTag((List<?>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)cachedSizeOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 47:
               int fieldSize = SchemaUtil.computeSizeSInt32ListNoTag((List<Integer>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)cachedSizeOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 48:
               int fieldSize = SchemaUtil.computeSizeSInt64ListNoTag((List<Long>)unsafe.getObject(message, offset));
               if (fieldSize > 0) {
                  if (this.useCachedSizeField) {
                     unsafe.putInt(message, (long)cachedSizeOffset, fieldSize);
                  }

                  size += CodedOutputStream.computeTagSize(number) + CodedOutputStream.computeUInt32SizeNoTag(fieldSize) + fieldSize;
               }
               break;
            case 49:
               size += SchemaUtil.computeSizeGroupList(number, listAt(message, offset), this.getMessageFieldSchema(i));
               break;
            case 50:
               size += this.mapFieldSchema.getSerializedSize(number, UnsafeUtil.getObject(message, offset), this.getMapFieldDefaultEntry(i));
               break;
            case 51:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeDoubleSize(number, 0.0);
               }
               break;
            case 52:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeFloatSize(number, 0.0F);
               }
               break;
            case 53:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeInt64Size(number, oneofLongAt(message, offset));
               }
               break;
            case 54:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeUInt64Size(number, oneofLongAt(message, offset));
               }
               break;
            case 55:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeInt32Size(number, oneofIntAt(message, offset));
               }
               break;
            case 56:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeFixed64Size(number, 0L);
               }
               break;
            case 57:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeFixed32Size(number, 0);
               }
               break;
            case 58:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeBoolSize(number, true);
               }
               break;
            case 59:
               if (this.isOneofPresent(message, number, i)) {
                  Object value = UnsafeUtil.getObject(message, offset);
                  if (value instanceof ByteString) {
                     size += CodedOutputStream.computeBytesSize(number, (ByteString)value);
                  } else {
                     size += CodedOutputStream.computeStringSize(number, (String)value);
                  }
               }
               break;
            case 60:
               if (this.isOneofPresent(message, number, i)) {
                  Object value = UnsafeUtil.getObject(message, offset);
                  size += SchemaUtil.computeSizeMessage(number, value, this.getMessageFieldSchema(i));
               }
               break;
            case 61:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeBytesSize(number, (ByteString)UnsafeUtil.getObject(message, offset));
               }
               break;
            case 62:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeUInt32Size(number, oneofIntAt(message, offset));
               }
               break;
            case 63:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeEnumSize(number, oneofIntAt(message, offset));
               }
               break;
            case 64:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeSFixed32Size(number, 0);
               }
               break;
            case 65:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeSFixed64Size(number, 0L);
               }
               break;
            case 66:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeSInt32Size(number, oneofIntAt(message, offset));
               }
               break;
            case 67:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeSInt64Size(number, oneofLongAt(message, offset));
               }
               break;
            case 68:
               if (this.isOneofPresent(message, number, i)) {
                  size += CodedOutputStream.computeGroupSize(number, (MessageLite)UnsafeUtil.getObject(message, offset), this.getMessageFieldSchema(i));
               }
         }
      }

      return size + this.getUnknownFieldsSerializedSize(this.unknownFieldSchema, message);
   }

   private <UT, UB> int getUnknownFieldsSerializedSize(UnknownFieldSchema<UT, UB> schema, T message) {
      UT unknowns = schema.getFromMessage(message);
      return schema.getSerializedSize(unknowns);
   }

   private static List<?> listAt(Object message, long offset) {
      return (List<?>)UnsafeUtil.getObject(message, offset);
   }

   @Override
   public void writeTo(T message, Writer writer) throws IOException {
      if (writer.fieldOrder() == Writer.FieldOrder.DESCENDING) {
         this.writeFieldsInDescendingOrder(message, writer);
      } else if (this.proto3) {
         this.writeFieldsInAscendingOrderProto3(message, writer);
      } else {
         this.writeFieldsInAscendingOrderProto2(message, writer);
      }

   }

   private void writeFieldsInAscendingOrderProto2(T message, Writer writer) throws IOException {
      Iterator<? extends Entry<?, ?>> extensionIterator = null;
      Entry nextExtension = null;
      if (this.hasExtensions) {
         FieldSet<?> extensions = this.extensionSchema.getExtensions(message);
         if (!extensions.isEmpty()) {
            extensionIterator = extensions.iterator();
            nextExtension = (Entry)extensionIterator.next();
         }
      }

      int currentPresenceFieldOffset = 1048575;
      int currentPresenceField = 0;
      int bufferLength = this.buffer.length;
      Unsafe unsafe = UNSAFE;

      for(int pos = 0; pos < bufferLength; pos += 3) {
         int typeAndOffset = this.typeAndOffsetAt(pos);
         int number = this.numberAt(pos);
         int fieldType = type(typeAndOffset);
         int presenceMaskAndOffset = 0;
         int presenceMask = 0;
         if (fieldType <= 17) {
            presenceMaskAndOffset = this.buffer[pos + 2];
            int presenceFieldOffset = presenceMaskAndOffset & 1048575;
            if (presenceFieldOffset != currentPresenceFieldOffset) {
               currentPresenceFieldOffset = presenceFieldOffset;
               currentPresenceField = unsafe.getInt(message, (long)presenceFieldOffset);
            }

            presenceMask = 1 << (presenceMaskAndOffset >>> 20);
         }

         while(nextExtension != null && this.extensionSchema.extensionNumber(nextExtension) <= number) {
            this.extensionSchema.serializeExtension(writer, nextExtension);
            nextExtension = extensionIterator.hasNext() ? (Entry)extensionIterator.next() : null;
         }

         long offset = offset(typeAndOffset);
         switch(fieldType) {
            case 0:
               if ((currentPresenceField & presenceMask) != 0) {
                  writer.writeDouble(number, doubleAt(message, offset));
               }
               break;
            case 1:
               if ((currentPresenceField & presenceMask) != 0) {
                  writer.writeFloat(number, floatAt(message, offset));
               }
               break;
            case 2:
               if ((currentPresenceField & presenceMask) != 0) {
                  writer.writeInt64(number, unsafe.getLong(message, offset));
               }
               break;
            case 3:
               if ((currentPresenceField & presenceMask) != 0) {
                  writer.writeUInt64(number, unsafe.getLong(message, offset));
               }
               break;
            case 4:
               if ((currentPresenceField & presenceMask) != 0) {
                  writer.writeInt32(number, unsafe.getInt(message, offset));
               }
               break;
            case 5:
               if ((currentPresenceField & presenceMask) != 0) {
                  writer.writeFixed64(number, unsafe.getLong(message, offset));
               }
               break;
            case 6:
               if ((currentPresenceField & presenceMask) != 0) {
                  writer.writeFixed32(number, unsafe.getInt(message, offset));
               }
               break;
            case 7:
               if ((currentPresenceField & presenceMask) != 0) {
                  writer.writeBool(number, booleanAt(message, offset));
               }
               break;
            case 8:
               if ((currentPresenceField & presenceMask) != 0) {
                  this.writeString(number, unsafe.getObject(message, offset), writer);
               }
               break;
            case 9:
               if ((currentPresenceField & presenceMask) != 0) {
                  Object value = unsafe.getObject(message, offset);
                  writer.writeMessage(number, value, this.getMessageFieldSchema(pos));
               }
               break;
            case 10:
               if ((currentPresenceField & presenceMask) != 0) {
                  writer.writeBytes(number, (ByteString)unsafe.getObject(message, offset));
               }
               break;
            case 11:
               if ((currentPresenceField & presenceMask) != 0) {
                  writer.writeUInt32(number, unsafe.getInt(message, offset));
               }
               break;
            case 12:
               if ((currentPresenceField & presenceMask) != 0) {
                  writer.writeEnum(number, unsafe.getInt(message, offset));
               }
               break;
            case 13:
               if ((currentPresenceField & presenceMask) != 0) {
                  writer.writeSFixed32(number, unsafe.getInt(message, offset));
               }
               break;
            case 14:
               if ((currentPresenceField & presenceMask) != 0) {
                  writer.writeSFixed64(number, unsafe.getLong(message, offset));
               }
               break;
            case 15:
               if ((currentPresenceField & presenceMask) != 0) {
                  writer.writeSInt32(number, unsafe.getInt(message, offset));
               }
               break;
            case 16:
               if ((currentPresenceField & presenceMask) != 0) {
                  writer.writeSInt64(number, unsafe.getLong(message, offset));
               }
               break;
            case 17:
               if ((currentPresenceField & presenceMask) != 0) {
                  writer.writeGroup(number, unsafe.getObject(message, offset), this.getMessageFieldSchema(pos));
               }
               break;
            case 18:
               SchemaUtil.writeDoubleList(this.numberAt(pos), (List<Double>)unsafe.getObject(message, offset), writer, false);
               break;
            case 19:
               SchemaUtil.writeFloatList(this.numberAt(pos), (List<Float>)unsafe.getObject(message, offset), writer, false);
               break;
            case 20:
               SchemaUtil.writeInt64List(this.numberAt(pos), (List<Long>)unsafe.getObject(message, offset), writer, false);
               break;
            case 21:
               SchemaUtil.writeUInt64List(this.numberAt(pos), (List<Long>)unsafe.getObject(message, offset), writer, false);
               break;
            case 22:
               SchemaUtil.writeInt32List(this.numberAt(pos), (List<Integer>)unsafe.getObject(message, offset), writer, false);
               break;
            case 23:
               SchemaUtil.writeFixed64List(this.numberAt(pos), (List<Long>)unsafe.getObject(message, offset), writer, false);
               break;
            case 24:
               SchemaUtil.writeFixed32List(this.numberAt(pos), (List<Integer>)unsafe.getObject(message, offset), writer, false);
               break;
            case 25:
               SchemaUtil.writeBoolList(this.numberAt(pos), (List<Boolean>)unsafe.getObject(message, offset), writer, false);
               break;
            case 26:
               SchemaUtil.writeStringList(this.numberAt(pos), (List<String>)unsafe.getObject(message, offset), writer);
               break;
            case 27:
               SchemaUtil.writeMessageList(this.numberAt(pos), (List<?>)unsafe.getObject(message, offset), writer, this.getMessageFieldSchema(pos));
               break;
            case 28:
               SchemaUtil.writeBytesList(this.numberAt(pos), (List<ByteString>)unsafe.getObject(message, offset), writer);
               break;
            case 29:
               SchemaUtil.writeUInt32List(this.numberAt(pos), (List<Integer>)unsafe.getObject(message, offset), writer, false);
               break;
            case 30:
               SchemaUtil.writeEnumList(this.numberAt(pos), (List<Integer>)unsafe.getObject(message, offset), writer, false);
               break;
            case 31:
               SchemaUtil.writeSFixed32List(this.numberAt(pos), (List<Integer>)unsafe.getObject(message, offset), writer, false);
               break;
            case 32:
               SchemaUtil.writeSFixed64List(this.numberAt(pos), (List<Long>)unsafe.getObject(message, offset), writer, false);
               break;
            case 33:
               SchemaUtil.writeSInt32List(this.numberAt(pos), (List<Integer>)unsafe.getObject(message, offset), writer, false);
               break;
            case 34:
               SchemaUtil.writeSInt64List(this.numberAt(pos), (List<Long>)unsafe.getObject(message, offset), writer, false);
               break;
            case 35:
               SchemaUtil.writeDoubleList(this.numberAt(pos), (List<Double>)unsafe.getObject(message, offset), writer, true);
               break;
            case 36:
               SchemaUtil.writeFloatList(this.numberAt(pos), (List<Float>)unsafe.getObject(message, offset), writer, true);
               break;
            case 37:
               SchemaUtil.writeInt64List(this.numberAt(pos), (List<Long>)unsafe.getObject(message, offset), writer, true);
               break;
            case 38:
               SchemaUtil.writeUInt64List(this.numberAt(pos), (List<Long>)unsafe.getObject(message, offset), writer, true);
               break;
            case 39:
               SchemaUtil.writeInt32List(this.numberAt(pos), (List<Integer>)unsafe.getObject(message, offset), writer, true);
               break;
            case 40:
               SchemaUtil.writeFixed64List(this.numberAt(pos), (List<Long>)unsafe.getObject(message, offset), writer, true);
               break;
            case 41:
               SchemaUtil.writeFixed32List(this.numberAt(pos), (List<Integer>)unsafe.getObject(message, offset), writer, true);
               break;
            case 42:
               SchemaUtil.writeBoolList(this.numberAt(pos), (List<Boolean>)unsafe.getObject(message, offset), writer, true);
               break;
            case 43:
               SchemaUtil.writeUInt32List(this.numberAt(pos), (List<Integer>)unsafe.getObject(message, offset), writer, true);
               break;
            case 44:
               SchemaUtil.writeEnumList(this.numberAt(pos), (List<Integer>)unsafe.getObject(message, offset), writer, true);
               break;
            case 45:
               SchemaUtil.writeSFixed32List(this.numberAt(pos), (List<Integer>)unsafe.getObject(message, offset), writer, true);
               break;
            case 46:
               SchemaUtil.writeSFixed64List(this.numberAt(pos), (List<Long>)unsafe.getObject(message, offset), writer, true);
               break;
            case 47:
               SchemaUtil.writeSInt32List(this.numberAt(pos), (List<Integer>)unsafe.getObject(message, offset), writer, true);
               break;
            case 48:
               SchemaUtil.writeSInt64List(this.numberAt(pos), (List<Long>)unsafe.getObject(message, offset), writer, true);
               break;
            case 49:
               SchemaUtil.writeGroupList(this.numberAt(pos), (List<?>)unsafe.getObject(message, offset), writer, this.getMessageFieldSchema(pos));
               break;
            case 50:
               this.writeMapHelper(writer, number, unsafe.getObject(message, offset), pos);
               break;
            case 51:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeDouble(number, oneofDoubleAt(message, offset));
               }
               break;
            case 52:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeFloat(number, oneofFloatAt(message, offset));
               }
               break;
            case 53:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeInt64(number, oneofLongAt(message, offset));
               }
               break;
            case 54:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeUInt64(number, oneofLongAt(message, offset));
               }
               break;
            case 55:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeInt32(number, oneofIntAt(message, offset));
               }
               break;
            case 56:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeFixed64(number, oneofLongAt(message, offset));
               }
               break;
            case 57:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeFixed32(number, oneofIntAt(message, offset));
               }
               break;
            case 58:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeBool(number, oneofBooleanAt(message, offset));
               }
               break;
            case 59:
               if (this.isOneofPresent(message, number, pos)) {
                  this.writeString(number, unsafe.getObject(message, offset), writer);
               }
               break;
            case 60:
               if (this.isOneofPresent(message, number, pos)) {
                  Object value = unsafe.getObject(message, offset);
                  writer.writeMessage(number, value, this.getMessageFieldSchema(pos));
               }
               break;
            case 61:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeBytes(number, (ByteString)unsafe.getObject(message, offset));
               }
               break;
            case 62:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeUInt32(number, oneofIntAt(message, offset));
               }
               break;
            case 63:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeEnum(number, oneofIntAt(message, offset));
               }
               break;
            case 64:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeSFixed32(number, oneofIntAt(message, offset));
               }
               break;
            case 65:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeSFixed64(number, oneofLongAt(message, offset));
               }
               break;
            case 66:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeSInt32(number, oneofIntAt(message, offset));
               }
               break;
            case 67:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeSInt64(number, oneofLongAt(message, offset));
               }
               break;
            case 68:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeGroup(number, unsafe.getObject(message, offset), this.getMessageFieldSchema(pos));
               }
         }
      }

      while(nextExtension != null) {
         this.extensionSchema.serializeExtension(writer, nextExtension);
         nextExtension = extensionIterator.hasNext() ? (Entry)extensionIterator.next() : null;
      }

      this.writeUnknownInMessageTo(this.unknownFieldSchema, message, writer);
   }

   private void writeFieldsInAscendingOrderProto3(T message, Writer writer) throws IOException {
      Iterator<? extends Entry<?, ?>> extensionIterator = null;
      Entry nextExtension = null;
      if (this.hasExtensions) {
         FieldSet<?> extensions = this.extensionSchema.getExtensions(message);
         if (!extensions.isEmpty()) {
            extensionIterator = extensions.iterator();
            nextExtension = (Entry)extensionIterator.next();
         }
      }

      int bufferLength = this.buffer.length;

      for(int pos = 0; pos < bufferLength; pos += 3) {
         int typeAndOffset = this.typeAndOffsetAt(pos);

         int number;
         for(number = this.numberAt(pos);
            nextExtension != null && this.extensionSchema.extensionNumber(nextExtension) <= number;
            nextExtension = extensionIterator.hasNext() ? (Entry)extensionIterator.next() : null
         ) {
            this.extensionSchema.serializeExtension(writer, nextExtension);
         }

         switch(type(typeAndOffset)) {
            case 0:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeDouble(number, doubleAt(message, offset(typeAndOffset)));
               }
               break;
            case 1:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeFloat(number, floatAt(message, offset(typeAndOffset)));
               }
               break;
            case 2:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeInt64(number, longAt(message, offset(typeAndOffset)));
               }
               break;
            case 3:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeUInt64(number, longAt(message, offset(typeAndOffset)));
               }
               break;
            case 4:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeInt32(number, intAt(message, offset(typeAndOffset)));
               }
               break;
            case 5:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeFixed64(number, longAt(message, offset(typeAndOffset)));
               }
               break;
            case 6:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeFixed32(number, intAt(message, offset(typeAndOffset)));
               }
               break;
            case 7:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeBool(number, booleanAt(message, offset(typeAndOffset)));
               }
               break;
            case 8:
               if (this.isFieldPresent(message, pos)) {
                  this.writeString(number, UnsafeUtil.getObject(message, offset(typeAndOffset)), writer);
               }
               break;
            case 9:
               if (this.isFieldPresent(message, pos)) {
                  Object value = UnsafeUtil.getObject(message, offset(typeAndOffset));
                  writer.writeMessage(number, value, this.getMessageFieldSchema(pos));
               }
               break;
            case 10:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeBytes(number, (ByteString)UnsafeUtil.getObject(message, offset(typeAndOffset)));
               }
               break;
            case 11:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeUInt32(number, intAt(message, offset(typeAndOffset)));
               }
               break;
            case 12:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeEnum(number, intAt(message, offset(typeAndOffset)));
               }
               break;
            case 13:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeSFixed32(number, intAt(message, offset(typeAndOffset)));
               }
               break;
            case 14:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeSFixed64(number, longAt(message, offset(typeAndOffset)));
               }
               break;
            case 15:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeSInt32(number, intAt(message, offset(typeAndOffset)));
               }
               break;
            case 16:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeSInt64(number, longAt(message, offset(typeAndOffset)));
               }
               break;
            case 17:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeGroup(number, UnsafeUtil.getObject(message, offset(typeAndOffset)), this.getMessageFieldSchema(pos));
               }
               break;
            case 18:
               SchemaUtil.writeDoubleList(this.numberAt(pos), (List<Double>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 19:
               SchemaUtil.writeFloatList(this.numberAt(pos), (List<Float>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 20:
               SchemaUtil.writeInt64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 21:
               SchemaUtil.writeUInt64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 22:
               SchemaUtil.writeInt32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 23:
               SchemaUtil.writeFixed64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 24:
               SchemaUtil.writeFixed32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 25:
               SchemaUtil.writeBoolList(this.numberAt(pos), (List<Boolean>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 26:
               SchemaUtil.writeStringList(this.numberAt(pos), (List<String>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer);
               break;
            case 27:
               SchemaUtil.writeMessageList(
                  this.numberAt(pos), (List<?>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, this.getMessageFieldSchema(pos)
               );
               break;
            case 28:
               SchemaUtil.writeBytesList(this.numberAt(pos), (List<ByteString>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer);
               break;
            case 29:
               SchemaUtil.writeUInt32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 30:
               SchemaUtil.writeEnumList(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 31:
               SchemaUtil.writeSFixed32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 32:
               SchemaUtil.writeSFixed64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 33:
               SchemaUtil.writeSInt32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 34:
               SchemaUtil.writeSInt64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 35:
               SchemaUtil.writeDoubleList(this.numberAt(pos), (List<Double>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 36:
               SchemaUtil.writeFloatList(this.numberAt(pos), (List<Float>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 37:
               SchemaUtil.writeInt64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 38:
               SchemaUtil.writeUInt64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 39:
               SchemaUtil.writeInt32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 40:
               SchemaUtil.writeFixed64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 41:
               SchemaUtil.writeFixed32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 42:
               SchemaUtil.writeBoolList(this.numberAt(pos), (List<Boolean>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 43:
               SchemaUtil.writeUInt32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 44:
               SchemaUtil.writeEnumList(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 45:
               SchemaUtil.writeSFixed32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 46:
               SchemaUtil.writeSFixed64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 47:
               SchemaUtil.writeSInt32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 48:
               SchemaUtil.writeSInt64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 49:
               SchemaUtil.writeGroupList(
                  this.numberAt(pos), (List<?>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, this.getMessageFieldSchema(pos)
               );
               break;
            case 50:
               this.writeMapHelper(writer, number, UnsafeUtil.getObject(message, offset(typeAndOffset)), pos);
               break;
            case 51:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeDouble(number, oneofDoubleAt(message, offset(typeAndOffset)));
               }
               break;
            case 52:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeFloat(number, oneofFloatAt(message, offset(typeAndOffset)));
               }
               break;
            case 53:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeInt64(number, oneofLongAt(message, offset(typeAndOffset)));
               }
               break;
            case 54:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeUInt64(number, oneofLongAt(message, offset(typeAndOffset)));
               }
               break;
            case 55:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeInt32(number, oneofIntAt(message, offset(typeAndOffset)));
               }
               break;
            case 56:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeFixed64(number, oneofLongAt(message, offset(typeAndOffset)));
               }
               break;
            case 57:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeFixed32(number, oneofIntAt(message, offset(typeAndOffset)));
               }
               break;
            case 58:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeBool(number, oneofBooleanAt(message, offset(typeAndOffset)));
               }
               break;
            case 59:
               if (this.isOneofPresent(message, number, pos)) {
                  this.writeString(number, UnsafeUtil.getObject(message, offset(typeAndOffset)), writer);
               }
               break;
            case 60:
               if (this.isOneofPresent(message, number, pos)) {
                  Object value = UnsafeUtil.getObject(message, offset(typeAndOffset));
                  writer.writeMessage(number, value, this.getMessageFieldSchema(pos));
               }
               break;
            case 61:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeBytes(number, (ByteString)UnsafeUtil.getObject(message, offset(typeAndOffset)));
               }
               break;
            case 62:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeUInt32(number, oneofIntAt(message, offset(typeAndOffset)));
               }
               break;
            case 63:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeEnum(number, oneofIntAt(message, offset(typeAndOffset)));
               }
               break;
            case 64:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeSFixed32(number, oneofIntAt(message, offset(typeAndOffset)));
               }
               break;
            case 65:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeSFixed64(number, oneofLongAt(message, offset(typeAndOffset)));
               }
               break;
            case 66:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeSInt32(number, oneofIntAt(message, offset(typeAndOffset)));
               }
               break;
            case 67:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeSInt64(number, oneofLongAt(message, offset(typeAndOffset)));
               }
               break;
            case 68:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeGroup(number, UnsafeUtil.getObject(message, offset(typeAndOffset)), this.getMessageFieldSchema(pos));
               }
         }
      }

      while(nextExtension != null) {
         this.extensionSchema.serializeExtension(writer, nextExtension);
         nextExtension = extensionIterator.hasNext() ? (Entry)extensionIterator.next() : null;
      }

      this.writeUnknownInMessageTo(this.unknownFieldSchema, message, writer);
   }

   private void writeFieldsInDescendingOrder(T message, Writer writer) throws IOException {
      this.writeUnknownInMessageTo(this.unknownFieldSchema, message, writer);
      Iterator<? extends Entry<?, ?>> extensionIterator = null;
      Entry nextExtension = null;
      if (this.hasExtensions) {
         FieldSet<?> extensions = this.extensionSchema.getExtensions(message);
         if (!extensions.isEmpty()) {
            extensionIterator = extensions.descendingIterator();
            nextExtension = (Entry)extensionIterator.next();
         }
      }

      for(int pos = this.buffer.length - 3; pos >= 0; pos -= 3) {
         int typeAndOffset = this.typeAndOffsetAt(pos);

         int number;
         for(number = this.numberAt(pos);
            nextExtension != null && this.extensionSchema.extensionNumber(nextExtension) > number;
            nextExtension = extensionIterator.hasNext() ? (Entry)extensionIterator.next() : null
         ) {
            this.extensionSchema.serializeExtension(writer, nextExtension);
         }

         switch(type(typeAndOffset)) {
            case 0:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeDouble(number, doubleAt(message, offset(typeAndOffset)));
               }
               break;
            case 1:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeFloat(number, floatAt(message, offset(typeAndOffset)));
               }
               break;
            case 2:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeInt64(number, longAt(message, offset(typeAndOffset)));
               }
               break;
            case 3:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeUInt64(number, longAt(message, offset(typeAndOffset)));
               }
               break;
            case 4:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeInt32(number, intAt(message, offset(typeAndOffset)));
               }
               break;
            case 5:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeFixed64(number, longAt(message, offset(typeAndOffset)));
               }
               break;
            case 6:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeFixed32(number, intAt(message, offset(typeAndOffset)));
               }
               break;
            case 7:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeBool(number, booleanAt(message, offset(typeAndOffset)));
               }
               break;
            case 8:
               if (this.isFieldPresent(message, pos)) {
                  this.writeString(number, UnsafeUtil.getObject(message, offset(typeAndOffset)), writer);
               }
               break;
            case 9:
               if (this.isFieldPresent(message, pos)) {
                  Object value = UnsafeUtil.getObject(message, offset(typeAndOffset));
                  writer.writeMessage(number, value, this.getMessageFieldSchema(pos));
               }
               break;
            case 10:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeBytes(number, (ByteString)UnsafeUtil.getObject(message, offset(typeAndOffset)));
               }
               break;
            case 11:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeUInt32(number, intAt(message, offset(typeAndOffset)));
               }
               break;
            case 12:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeEnum(number, intAt(message, offset(typeAndOffset)));
               }
               break;
            case 13:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeSFixed32(number, intAt(message, offset(typeAndOffset)));
               }
               break;
            case 14:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeSFixed64(number, longAt(message, offset(typeAndOffset)));
               }
               break;
            case 15:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeSInt32(number, intAt(message, offset(typeAndOffset)));
               }
               break;
            case 16:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeSInt64(number, longAt(message, offset(typeAndOffset)));
               }
               break;
            case 17:
               if (this.isFieldPresent(message, pos)) {
                  writer.writeGroup(number, UnsafeUtil.getObject(message, offset(typeAndOffset)), this.getMessageFieldSchema(pos));
               }
               break;
            case 18:
               SchemaUtil.writeDoubleList(this.numberAt(pos), (List<Double>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 19:
               SchemaUtil.writeFloatList(this.numberAt(pos), (List<Float>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 20:
               SchemaUtil.writeInt64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 21:
               SchemaUtil.writeUInt64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 22:
               SchemaUtil.writeInt32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 23:
               SchemaUtil.writeFixed64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 24:
               SchemaUtil.writeFixed32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 25:
               SchemaUtil.writeBoolList(this.numberAt(pos), (List<Boolean>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 26:
               SchemaUtil.writeStringList(this.numberAt(pos), (List<String>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer);
               break;
            case 27:
               SchemaUtil.writeMessageList(
                  this.numberAt(pos), (List<?>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, this.getMessageFieldSchema(pos)
               );
               break;
            case 28:
               SchemaUtil.writeBytesList(this.numberAt(pos), (List<ByteString>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer);
               break;
            case 29:
               SchemaUtil.writeUInt32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 30:
               SchemaUtil.writeEnumList(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 31:
               SchemaUtil.writeSFixed32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 32:
               SchemaUtil.writeSFixed64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 33:
               SchemaUtil.writeSInt32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 34:
               SchemaUtil.writeSInt64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, false);
               break;
            case 35:
               SchemaUtil.writeDoubleList(this.numberAt(pos), (List<Double>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 36:
               SchemaUtil.writeFloatList(this.numberAt(pos), (List<Float>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 37:
               SchemaUtil.writeInt64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 38:
               SchemaUtil.writeUInt64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 39:
               SchemaUtil.writeInt32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 40:
               SchemaUtil.writeFixed64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 41:
               SchemaUtil.writeFixed32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 42:
               SchemaUtil.writeBoolList(this.numberAt(pos), (List<Boolean>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 43:
               SchemaUtil.writeUInt32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 44:
               SchemaUtil.writeEnumList(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 45:
               SchemaUtil.writeSFixed32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 46:
               SchemaUtil.writeSFixed64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 47:
               SchemaUtil.writeSInt32List(this.numberAt(pos), (List<Integer>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 48:
               SchemaUtil.writeSInt64List(this.numberAt(pos), (List<Long>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, true);
               break;
            case 49:
               SchemaUtil.writeGroupList(
                  this.numberAt(pos), (List<?>)UnsafeUtil.getObject(message, offset(typeAndOffset)), writer, this.getMessageFieldSchema(pos)
               );
               break;
            case 50:
               this.writeMapHelper(writer, number, UnsafeUtil.getObject(message, offset(typeAndOffset)), pos);
               break;
            case 51:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeDouble(number, oneofDoubleAt(message, offset(typeAndOffset)));
               }
               break;
            case 52:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeFloat(number, oneofFloatAt(message, offset(typeAndOffset)));
               }
               break;
            case 53:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeInt64(number, oneofLongAt(message, offset(typeAndOffset)));
               }
               break;
            case 54:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeUInt64(number, oneofLongAt(message, offset(typeAndOffset)));
               }
               break;
            case 55:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeInt32(number, oneofIntAt(message, offset(typeAndOffset)));
               }
               break;
            case 56:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeFixed64(number, oneofLongAt(message, offset(typeAndOffset)));
               }
               break;
            case 57:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeFixed32(number, oneofIntAt(message, offset(typeAndOffset)));
               }
               break;
            case 58:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeBool(number, oneofBooleanAt(message, offset(typeAndOffset)));
               }
               break;
            case 59:
               if (this.isOneofPresent(message, number, pos)) {
                  this.writeString(number, UnsafeUtil.getObject(message, offset(typeAndOffset)), writer);
               }
               break;
            case 60:
               if (this.isOneofPresent(message, number, pos)) {
                  Object value = UnsafeUtil.getObject(message, offset(typeAndOffset));
                  writer.writeMessage(number, value, this.getMessageFieldSchema(pos));
               }
               break;
            case 61:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeBytes(number, (ByteString)UnsafeUtil.getObject(message, offset(typeAndOffset)));
               }
               break;
            case 62:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeUInt32(number, oneofIntAt(message, offset(typeAndOffset)));
               }
               break;
            case 63:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeEnum(number, oneofIntAt(message, offset(typeAndOffset)));
               }
               break;
            case 64:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeSFixed32(number, oneofIntAt(message, offset(typeAndOffset)));
               }
               break;
            case 65:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeSFixed64(number, oneofLongAt(message, offset(typeAndOffset)));
               }
               break;
            case 66:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeSInt32(number, oneofIntAt(message, offset(typeAndOffset)));
               }
               break;
            case 67:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeSInt64(number, oneofLongAt(message, offset(typeAndOffset)));
               }
               break;
            case 68:
               if (this.isOneofPresent(message, number, pos)) {
                  writer.writeGroup(number, UnsafeUtil.getObject(message, offset(typeAndOffset)), this.getMessageFieldSchema(pos));
               }
         }
      }

      while(nextExtension != null) {
         this.extensionSchema.serializeExtension(writer, nextExtension);
         nextExtension = extensionIterator.hasNext() ? (Entry)extensionIterator.next() : null;
      }

   }

   private <K, V> void writeMapHelper(Writer writer, int number, Object mapField, int pos) throws IOException {
      if (mapField != null) {
         writer.writeMap(number, this.mapFieldSchema.forMapMetadata(this.getMapFieldDefaultEntry(pos)), this.mapFieldSchema.forMapData(mapField));
      }

   }

   private <UT, UB> void writeUnknownInMessageTo(UnknownFieldSchema<UT, UB> schema, T message, Writer writer) throws IOException {
      schema.writeTo(schema.getFromMessage(message), writer);
   }

   @Override
   public void mergeFrom(T message, Reader reader, ExtensionRegistryLite extensionRegistry) throws IOException {
      if (extensionRegistry == null) {
         throw new NullPointerException();
      } else {
         this.mergeFromHelper(this.unknownFieldSchema, this.extensionSchema, message, reader, extensionRegistry);
      }
   }

   // $FF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // Please report this to the Quiltflower issue tracker, at https://github.com/QuiltMC/quiltflower/issues with a copy of the class file (if you have the rights to distribute it!)
   private <UT, UB, ET extends FieldSet.FieldDescriptorLite<ET>> void mergeFromHelper(
      UnknownFieldSchema<UT, UB> unknownFieldSchema, ExtensionSchema<ET> extensionSchema, T message, Reader reader, ExtensionRegistryLite extensionRegistry
   ) throws IOException {
      UB unknownFields = null;
      FieldSet<ET> extensions = null;

      while(true) {
         boolean var17 = false;

         label534: {
            label535: {
               label536: {
                  label537: {
                     try {
                        var17 = true;
                        int number = reader.getFieldNumber();
                        int pos = this.positionForFieldNumber(number);
                        if (pos < 0) {
                           if (number == Integer.MAX_VALUE) {
                              var17 = false;
                              break label536;
                           }

                           Object extension = !this.hasExtensions
                              ? null
                              : extensionSchema.findExtensionByNumber(extensionRegistry, this.defaultInstance, number);
                           if (extension != null) {
                              if (extensions == null) {
                                 extensions = extensionSchema.getMutableExtensions(message);
                              }

                              unknownFields = extensionSchema.parseExtension(
                                 reader, extension, extensionRegistry, extensions, unknownFields, unknownFieldSchema
                              );
                              continue;
                           }

                           if (unknownFieldSchema.shouldDiscardUnknownFields(reader)) {
                              if (reader.skipField()) {
                                 continue;
                              }

                              var17 = false;
                           } else {
                              if (unknownFields == null) {
                                 unknownFields = unknownFieldSchema.getBuilderFromMessage(message);
                              }

                              if (unknownFieldSchema.mergeOneFieldFrom(unknownFields, reader)) {
                                 continue;
                              }

                              var17 = false;
                           }
                           break label537;
                        }

                        int typeAndOffset = this.typeAndOffsetAt(pos);

                        try {
                           switch(type(typeAndOffset)) {
                              case 0:
                                 UnsafeUtil.putDouble(message, offset(typeAndOffset), reader.readDouble());
                                 this.setFieldPresent(message, pos);
                                 continue;
                              case 1:
                                 UnsafeUtil.putFloat(message, offset(typeAndOffset), reader.readFloat());
                                 this.setFieldPresent(message, pos);
                                 continue;
                              case 2:
                                 UnsafeUtil.putLong(message, offset(typeAndOffset), reader.readInt64());
                                 this.setFieldPresent(message, pos);
                                 continue;
                              case 3:
                                 UnsafeUtil.putLong(message, offset(typeAndOffset), reader.readUInt64());
                                 this.setFieldPresent(message, pos);
                                 continue;
                              case 4:
                                 UnsafeUtil.putInt(message, offset(typeAndOffset), reader.readInt32());
                                 this.setFieldPresent(message, pos);
                                 continue;
                              case 5:
                                 UnsafeUtil.putLong(message, offset(typeAndOffset), reader.readFixed64());
                                 this.setFieldPresent(message, pos);
                                 continue;
                              case 6:
                                 UnsafeUtil.putInt(message, offset(typeAndOffset), reader.readFixed32());
                                 this.setFieldPresent(message, pos);
                                 continue;
                              case 7:
                                 UnsafeUtil.putBoolean(message, offset(typeAndOffset), reader.readBool());
                                 this.setFieldPresent(message, pos);
                                 continue;
                              case 8:
                                 this.readString(message, typeAndOffset, reader);
                                 this.setFieldPresent(message, pos);
                                 continue;
                              case 9:
                                 if (this.isFieldPresent(message, pos)) {
                                    Object mergedResult = Internal.mergeMessage(
                                       UnsafeUtil.getObject(message, offset(typeAndOffset)),
                                       reader.readMessageBySchemaWithCheck(this.getMessageFieldSchema(pos), extensionRegistry)
                                    );
                                    UnsafeUtil.putObject(message, offset(typeAndOffset), mergedResult);
                                 } else {
                                    UnsafeUtil.putObject(
                                       message, offset(typeAndOffset), reader.readMessageBySchemaWithCheck(this.getMessageFieldSchema(pos), extensionRegistry)
                                    );
                                    this.setFieldPresent(message, pos);
                                 }
                                 continue;
                              case 10:
                                 UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readBytes());
                                 this.setFieldPresent(message, pos);
                                 continue;
                              case 11:
                                 UnsafeUtil.putInt(message, offset(typeAndOffset), reader.readUInt32());
                                 this.setFieldPresent(message, pos);
                                 continue;
                              case 12:
                                 int enumValue = reader.readEnum();
                                 Internal.EnumVerifier enumVerifier = this.getEnumFieldVerifier(pos);
                                 if (enumVerifier != null && !enumVerifier.isInRange(enumValue)) {
                                    unknownFields = SchemaUtil.storeUnknownEnum(number, enumValue, unknownFields, unknownFieldSchema);
                                    continue;
                                 }

                                 UnsafeUtil.putInt(message, offset(typeAndOffset), enumValue);
                                 this.setFieldPresent(message, pos);
                                 continue;
                              case 13:
                                 UnsafeUtil.putInt(message, offset(typeAndOffset), reader.readSFixed32());
                                 this.setFieldPresent(message, pos);
                                 continue;
                              case 14:
                                 UnsafeUtil.putLong(message, offset(typeAndOffset), reader.readSFixed64());
                                 this.setFieldPresent(message, pos);
                                 continue;
                              case 15:
                                 UnsafeUtil.putInt(message, offset(typeAndOffset), reader.readSInt32());
                                 this.setFieldPresent(message, pos);
                                 continue;
                              case 16:
                                 UnsafeUtil.putLong(message, offset(typeAndOffset), reader.readSInt64());
                                 this.setFieldPresent(message, pos);
                                 continue;
                              case 17:
                                 if (this.isFieldPresent(message, pos)) {
                                    Object mergedResult = Internal.mergeMessage(
                                       UnsafeUtil.getObject(message, offset(typeAndOffset)),
                                       reader.readGroupBySchemaWithCheck(this.getMessageFieldSchema(pos), extensionRegistry)
                                    );
                                    UnsafeUtil.putObject(message, offset(typeAndOffset), mergedResult);
                                 } else {
                                    UnsafeUtil.putObject(
                                       message, offset(typeAndOffset), reader.readGroupBySchemaWithCheck(this.getMessageFieldSchema(pos), extensionRegistry)
                                    );
                                    this.setFieldPresent(message, pos);
                                 }
                                 continue;
                              case 18:
                                 reader.readDoubleList(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 19:
                                 reader.readFloatList(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 20:
                                 reader.readInt64List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 21:
                                 reader.readUInt64List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 22:
                                 reader.readInt32List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 23:
                                 reader.readFixed64List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 24:
                                 reader.readFixed32List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 25:
                                 reader.readBoolList(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 26:
                                 this.readStringList(message, typeAndOffset, reader);
                                 continue;
                              case 27:
                                 this.readMessageList(message, typeAndOffset, reader, this.getMessageFieldSchema(pos), extensionRegistry);
                                 continue;
                              case 28:
                                 reader.readBytesList(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 29:
                                 reader.readUInt32List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 30: {
                                 List<Integer> enumList = this.listFieldSchema.mutableListAt(message, offset(typeAndOffset));
                                 reader.readEnumList(enumList);
                                 unknownFields = SchemaUtil.filterUnknownEnumList(
                                    number, enumList, this.getEnumFieldVerifier(pos), unknownFields, unknownFieldSchema
                                 );
                                 continue;
                              }
                              case 31:
                                 reader.readSFixed32List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 32:
                                 reader.readSFixed64List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 33:
                                 reader.readSInt32List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 34:
                                 reader.readSInt64List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 35:
                                 reader.readDoubleList(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 36:
                                 reader.readFloatList(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 37:
                                 reader.readInt64List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 38:
                                 reader.readUInt64List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 39:
                                 reader.readInt32List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 40:
                                 reader.readFixed64List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 41:
                                 reader.readFixed32List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 42:
                                 reader.readBoolList(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 43:
                                 reader.readUInt32List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 44: {
                                 List<Integer> enumList = this.listFieldSchema.mutableListAt(message, offset(typeAndOffset));
                                 reader.readEnumList(enumList);
                                 unknownFields = SchemaUtil.filterUnknownEnumList(
                                    number, enumList, this.getEnumFieldVerifier(pos), unknownFields, unknownFieldSchema
                                 );
                                 continue;
                              }
                              case 45:
                                 reader.readSFixed32List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 46:
                                 reader.readSFixed64List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 47:
                                 reader.readSInt32List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 48:
                                 reader.readSInt64List(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
                                 continue;
                              case 49:
                                 this.readGroupList(message, offset(typeAndOffset), reader, this.getMessageFieldSchema(pos), extensionRegistry);
                                 continue;
                              case 50:
                                 this.mergeMap(message, pos, this.getMapFieldDefaultEntry(pos), extensionRegistry, reader);
                                 continue;
                              case 51:
                                 UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readDouble());
                                 this.setOneofPresent(message, number, pos);
                                 continue;
                              case 52:
                                 UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readFloat());
                                 this.setOneofPresent(message, number, pos);
                                 continue;
                              case 53:
                                 UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readInt64());
                                 this.setOneofPresent(message, number, pos);
                                 continue;
                              case 54:
                                 UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readUInt64());
                                 this.setOneofPresent(message, number, pos);
                                 continue;
                              case 55:
                                 UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readInt32());
                                 this.setOneofPresent(message, number, pos);
                                 continue;
                              case 56:
                                 UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readFixed64());
                                 this.setOneofPresent(message, number, pos);
                                 continue;
                              case 57:
                                 UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readFixed32());
                                 this.setOneofPresent(message, number, pos);
                                 continue;
                              case 58:
                                 UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readBool());
                                 this.setOneofPresent(message, number, pos);
                                 continue;
                              case 59:
                                 this.readString(message, typeAndOffset, reader);
                                 this.setOneofPresent(message, number, pos);
                                 continue;
                              case 60:
                                 if (this.isOneofPresent(message, number, pos)) {
                                    Object mergedResult = Internal.mergeMessage(
                                       UnsafeUtil.getObject(message, offset(typeAndOffset)),
                                       reader.readMessageBySchemaWithCheck(this.getMessageFieldSchema(pos), extensionRegistry)
                                    );
                                    UnsafeUtil.putObject(message, offset(typeAndOffset), mergedResult);
                                 } else {
                                    UnsafeUtil.putObject(
                                       message, offset(typeAndOffset), reader.readMessageBySchemaWithCheck(this.getMessageFieldSchema(pos), extensionRegistry)
                                    );
                                    this.setFieldPresent(message, pos);
                                 }

                                 this.setOneofPresent(message, number, pos);
                                 continue;
                              case 61:
                                 UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readBytes());
                                 this.setOneofPresent(message, number, pos);
                                 continue;
                              case 62:
                                 UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readUInt32());
                                 this.setOneofPresent(message, number, pos);
                                 continue;
                              case 63:
                                 int enumValue = reader.readEnum();
                                 Internal.EnumVerifier enumVerifier = this.getEnumFieldVerifier(pos);
                                 if (enumVerifier != null && !enumVerifier.isInRange(enumValue)) {
                                    unknownFields = SchemaUtil.storeUnknownEnum(number, enumValue, unknownFields, unknownFieldSchema);
                                    continue;
                                 }

                                 UnsafeUtil.putObject(message, offset(typeAndOffset), enumValue);
                                 this.setOneofPresent(message, number, pos);
                                 continue;
                              case 64:
                                 UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readSFixed32());
                                 this.setOneofPresent(message, number, pos);
                                 continue;
                              case 65:
                                 UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readSFixed64());
                                 this.setOneofPresent(message, number, pos);
                                 continue;
                              case 66:
                                 UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readSInt32());
                                 this.setOneofPresent(message, number, pos);
                                 continue;
                              case 67:
                                 UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readSInt64());
                                 this.setOneofPresent(message, number, pos);
                                 continue;
                              case 68:
                                 UnsafeUtil.putObject(
                                    message, offset(typeAndOffset), reader.readGroupBySchemaWithCheck(this.getMessageFieldSchema(pos), extensionRegistry)
                                 );
                                 this.setOneofPresent(message, number, pos);
                                 continue;
                              default:
                                 if (unknownFields == null) {
                                    unknownFields = unknownFieldSchema.newBuilder();
                                 }

                                 if (unknownFieldSchema.mergeOneFieldFrom(unknownFields, reader)) {
                                    continue;
                                 }

                                 var17 = false;
                           }
                        } catch (InvalidProtocolBufferException.InvalidWireTypeException var18) {
                           if (unknownFieldSchema.shouldDiscardUnknownFields(reader)) {
                              if (reader.skipField()) {
                                 continue;
                              }

                              var17 = false;
                              break label534;
                           }

                           if (unknownFields == null) {
                              unknownFields = unknownFieldSchema.getBuilderFromMessage(message);
                           }

                           if (unknownFieldSchema.mergeOneFieldFrom(unknownFields, reader)) {
                              continue;
                           }

                           var17 = false;
                           break label535;
                        }
                     } finally {
                        if (var17) {
                           for(int i = this.checkInitializedCount; i < this.repeatedFieldOffsetStart; ++i) {
                              unknownFields = this.filterMapUnknownEnumValues(message, this.intArray[i], unknownFields, unknownFieldSchema);
                           }

                           if (unknownFields != null) {
                              unknownFieldSchema.setBuilderToMessage(message, unknownFields);
                           }

                        }
                     }

                     for(int i = this.checkInitializedCount; i < this.repeatedFieldOffsetStart; ++i) {
                        unknownFields = this.filterMapUnknownEnumValues(message, this.intArray[i], unknownFields, unknownFieldSchema);
                     }

                     if (unknownFields != null) {
                        unknownFieldSchema.setBuilderToMessage(message, unknownFields);
                     }

                     return;
                  }

                  for(int i = this.checkInitializedCount; i < this.repeatedFieldOffsetStart; ++i) {
                     unknownFields = this.filterMapUnknownEnumValues(message, this.intArray[i], unknownFields, unknownFieldSchema);
                  }

                  if (unknownFields != null) {
                     unknownFieldSchema.setBuilderToMessage(message, unknownFields);
                  }

                  return;
               }

               for(int i = this.checkInitializedCount; i < this.repeatedFieldOffsetStart; ++i) {
                  unknownFields = this.filterMapUnknownEnumValues(message, this.intArray[i], unknownFields, unknownFieldSchema);
               }

               if (unknownFields != null) {
                  unknownFieldSchema.setBuilderToMessage(message, unknownFields);
               }

               return;
            }

            for(int i = this.checkInitializedCount; i < this.repeatedFieldOffsetStart; ++i) {
               unknownFields = this.filterMapUnknownEnumValues(message, this.intArray[i], unknownFields, unknownFieldSchema);
            }

            if (unknownFields != null) {
               unknownFieldSchema.setBuilderToMessage(message, unknownFields);
            }

            return;
         }

         for(int i = this.checkInitializedCount; i < this.repeatedFieldOffsetStart; ++i) {
            unknownFields = this.filterMapUnknownEnumValues(message, this.intArray[i], unknownFields, unknownFieldSchema);
         }

         if (unknownFields != null) {
            unknownFieldSchema.setBuilderToMessage(message, unknownFields);
         }

         return;
      }
   }

   static UnknownFieldSetLite getMutableUnknownFields(Object message) {
      UnknownFieldSetLite unknownFields = ((GeneratedMessageLite)message).unknownFields;
      if (unknownFields == UnknownFieldSetLite.getDefaultInstance()) {
         unknownFields = UnknownFieldSetLite.newInstance();
         ((GeneratedMessageLite)message).unknownFields = unknownFields;
      }

      return unknownFields;
   }

   private int decodeMapEntryValue(
      byte[] data, int position, int limit, WireFormat.FieldType fieldType, Class<?> messageType, ArrayDecoders.Registers registers
   ) throws IOException {
      switch(fieldType) {
         case BOOL:
            position = ArrayDecoders.decodeVarint64(data, position, registers);
            registers.object1 = registers.long1 != 0L;
            break;
         case BYTES:
            position = ArrayDecoders.decodeBytes(data, position, registers);
            break;
         case DOUBLE:
            registers.object1 = ArrayDecoders.decodeDouble(data, position);
            position += 8;
            break;
         case FIXED32:
         case SFIXED32:
            registers.object1 = ArrayDecoders.decodeFixed32(data, position);
            position += 4;
            break;
         case FIXED64:
         case SFIXED64:
            registers.object1 = ArrayDecoders.decodeFixed64(data, position);
            position += 8;
            break;
         case FLOAT:
            registers.object1 = ArrayDecoders.decodeFloat(data, position);
            position += 4;
            break;
         case ENUM:
         case INT32:
         case UINT32:
            position = ArrayDecoders.decodeVarint32(data, position, registers);
            registers.object1 = registers.int1;
            break;
         case INT64:
         case UINT64:
            position = ArrayDecoders.decodeVarint64(data, position, registers);
            registers.object1 = registers.long1;
            break;
         case MESSAGE:
            position = ArrayDecoders.decodeMessageField(Protobuf.getInstance().schemaFor(messageType), data, position, limit, registers);
            break;
         case SINT32:
            position = ArrayDecoders.decodeVarint32(data, position, registers);
            registers.object1 = CodedInputStream.decodeZigZag32(registers.int1);
            break;
         case SINT64:
            position = ArrayDecoders.decodeVarint64(data, position, registers);
            registers.object1 = CodedInputStream.decodeZigZag64(registers.long1);
            break;
         case STRING:
            position = ArrayDecoders.decodeStringRequireUtf8(data, position, registers);
            break;
         default:
            throw new RuntimeException("unsupported field type.");
      }

      return position;
   }

   private <K, V> int decodeMapEntry(
      byte[] data, int position, int limit, MapEntryLite.Metadata<K, V> metadata, Map<K, V> target, ArrayDecoders.Registers registers
   ) throws IOException {
      position = ArrayDecoders.decodeVarint32(data, position, registers);
      int length = registers.int1;
      if (length >= 0 && length <= limit - position) {
         int end = position + length;
         K key = metadata.defaultKey;
         V value = metadata.defaultValue;

         while(position < end) {
            int tag = data[position++];
            if (tag < 0) {
               position = ArrayDecoders.decodeVarint32(tag, data, position, registers);
               tag = registers.int1;
            }

            int fieldNumber = tag >>> 3;
            int wireType = tag & 7;
            switch(fieldNumber) {
               case 1:
                  if (wireType == metadata.keyType.getWireType()) {
                     position = this.decodeMapEntryValue(data, position, limit, metadata.keyType, null, registers);
                     key = (K)registers.object1;
                     continue;
                  }
                  break;
               case 2:
                  if (wireType == metadata.valueType.getWireType()) {
                     position = this.decodeMapEntryValue(data, position, limit, metadata.valueType, metadata.defaultValue.getClass(), registers);
                     value = (V)registers.object1;
                     continue;
                  }
            }

            position = ArrayDecoders.skipField(tag, data, position, limit, registers);
         }

         if (position != end) {
            throw InvalidProtocolBufferException.parseFailure();
         } else {
            target.put(key, value);
            return end;
         }
      } else {
         throw InvalidProtocolBufferException.truncatedMessage();
      }
   }

   private int parseRepeatedField(
      T message,
      byte[] data,
      int position,
      int limit,
      int tag,
      int number,
      int wireType,
      int bufferPosition,
      long typeAndOffset,
      int fieldType,
      long fieldOffset,
      ArrayDecoders.Registers registers
   ) throws IOException {
      Internal.ProtobufList<?> list = (Internal.ProtobufList)UNSAFE.getObject(message, fieldOffset);
      if (!list.isModifiable()) {
         int size = list.size();
         list = list.mutableCopyWithCapacity(size == 0 ? 10 : size * 2);
         UNSAFE.putObject(message, fieldOffset, list);
      }

      switch(fieldType) {
         case 18:
         case 35:
            if (wireType == 2) {
               position = ArrayDecoders.decodePackedDoubleList(data, position, list, registers);
            } else if (wireType == 1) {
               position = ArrayDecoders.decodeDoubleList(tag, data, position, limit, list, registers);
            }
            break;
         case 19:
         case 36:
            if (wireType == 2) {
               position = ArrayDecoders.decodePackedFloatList(data, position, list, registers);
            } else if (wireType == 5) {
               position = ArrayDecoders.decodeFloatList(tag, data, position, limit, list, registers);
            }
            break;
         case 20:
         case 21:
         case 37:
         case 38:
            if (wireType == 2) {
               position = ArrayDecoders.decodePackedVarint64List(data, position, list, registers);
            } else if (wireType == 0) {
               position = ArrayDecoders.decodeVarint64List(tag, data, position, limit, list, registers);
            }
            break;
         case 22:
         case 29:
         case 39:
         case 43:
            if (wireType == 2) {
               position = ArrayDecoders.decodePackedVarint32List(data, position, list, registers);
            } else if (wireType == 0) {
               position = ArrayDecoders.decodeVarint32List(tag, data, position, limit, list, registers);
            }
            break;
         case 23:
         case 32:
         case 40:
         case 46:
            if (wireType == 2) {
               position = ArrayDecoders.decodePackedFixed64List(data, position, list, registers);
            } else if (wireType == 1) {
               position = ArrayDecoders.decodeFixed64List(tag, data, position, limit, list, registers);
            }
            break;
         case 24:
         case 31:
         case 41:
         case 45:
            if (wireType == 2) {
               position = ArrayDecoders.decodePackedFixed32List(data, position, list, registers);
            } else if (wireType == 5) {
               position = ArrayDecoders.decodeFixed32List(tag, data, position, limit, list, registers);
            }
            break;
         case 25:
         case 42:
            if (wireType == 2) {
               position = ArrayDecoders.decodePackedBoolList(data, position, list, registers);
            } else if (wireType == 0) {
               position = ArrayDecoders.decodeBoolList(tag, data, position, limit, list, registers);
            }
            break;
         case 26:
            if (wireType == 2) {
               if ((typeAndOffset & 536870912L) == 0L) {
                  position = ArrayDecoders.decodeStringList(tag, data, position, limit, list, registers);
               } else {
                  position = ArrayDecoders.decodeStringListRequireUtf8(tag, data, position, limit, list, registers);
               }
            }
            break;
         case 27:
            if (wireType == 2) {
               position = ArrayDecoders.decodeMessageList(this.getMessageFieldSchema(bufferPosition), tag, data, position, limit, list, registers);
            }
            break;
         case 28:
            if (wireType == 2) {
               position = ArrayDecoders.decodeBytesList(tag, data, position, limit, list, registers);
            }
            break;
         case 30:
         case 44:
            if (wireType == 2) {
               position = ArrayDecoders.decodePackedVarint32List(data, position, list, registers);
            } else {
               if (wireType != 0) {
                  break;
               }

               position = ArrayDecoders.decodeVarint32List(tag, data, position, limit, list, registers);
            }

            UnknownFieldSetLite unknownFields = ((GeneratedMessageLite)message).unknownFields;
            if (unknownFields == UnknownFieldSetLite.getDefaultInstance()) {
               unknownFields = null;
            }

            unknownFields = SchemaUtil.filterUnknownEnumList(number, list, this.getEnumFieldVerifier(bufferPosition), unknownFields, this.unknownFieldSchema);
            if (unknownFields != null) {
               ((GeneratedMessageLite)message).unknownFields = unknownFields;
            }
            break;
         case 33:
         case 47:
            if (wireType == 2) {
               position = ArrayDecoders.decodePackedSInt32List(data, position, list, registers);
            } else if (wireType == 0) {
               position = ArrayDecoders.decodeSInt32List(tag, data, position, limit, list, registers);
            }
            break;
         case 34:
         case 48:
            if (wireType == 2) {
               position = ArrayDecoders.decodePackedSInt64List(data, position, list, registers);
            } else if (wireType == 0) {
               position = ArrayDecoders.decodeSInt64List(tag, data, position, limit, list, registers);
            }
            break;
         case 49:
            if (wireType == 3) {
               position = ArrayDecoders.decodeGroupList(this.getMessageFieldSchema(bufferPosition), tag, data, position, limit, list, registers);
            }
      }

      return position;
   }

   private <K, V> int parseMapField(T message, byte[] data, int position, int limit, int bufferPosition, long fieldOffset, ArrayDecoders.Registers registers) throws IOException {
      Unsafe unsafe = UNSAFE;
      Object mapDefaultEntry = this.getMapFieldDefaultEntry(bufferPosition);
      Object mapField = unsafe.getObject(message, fieldOffset);
      if (this.mapFieldSchema.isImmutable(mapField)) {
         Object oldMapField = mapField;
         mapField = this.mapFieldSchema.newMapField(mapDefaultEntry);
         this.mapFieldSchema.mergeFrom(mapField, oldMapField);
         unsafe.putObject(message, fieldOffset, mapField);
      }

      return this.decodeMapEntry(
         data, position, limit, this.mapFieldSchema.forMapMetadata(mapDefaultEntry), this.mapFieldSchema.forMutableMapData(mapField), registers
      );
   }

   private int parseOneofField(
      T message,
      byte[] data,
      int position,
      int limit,
      int tag,
      int number,
      int wireType,
      int typeAndOffset,
      int fieldType,
      long fieldOffset,
      int bufferPosition,
      ArrayDecoders.Registers registers
   ) throws IOException {
      Unsafe unsafe = UNSAFE;
      long oneofCaseOffset = (long)(this.buffer[bufferPosition + 2] & 1048575);
      switch(fieldType) {
         case 51:
            if (wireType == 1) {
               unsafe.putObject(message, fieldOffset, ArrayDecoders.decodeDouble(data, position));
               position += 8;
               unsafe.putInt(message, oneofCaseOffset, number);
            }
            break;
         case 52:
            if (wireType == 5) {
               unsafe.putObject(message, fieldOffset, ArrayDecoders.decodeFloat(data, position));
               position += 4;
               unsafe.putInt(message, oneofCaseOffset, number);
            }
            break;
         case 53:
         case 54:
            if (wireType == 0) {
               position = ArrayDecoders.decodeVarint64(data, position, registers);
               unsafe.putObject(message, fieldOffset, registers.long1);
               unsafe.putInt(message, oneofCaseOffset, number);
            }
            break;
         case 55:
         case 62:
            if (wireType == 0) {
               position = ArrayDecoders.decodeVarint32(data, position, registers);
               unsafe.putObject(message, fieldOffset, registers.int1);
               unsafe.putInt(message, oneofCaseOffset, number);
            }
            break;
         case 56:
         case 65:
            if (wireType == 1) {
               unsafe.putObject(message, fieldOffset, ArrayDecoders.decodeFixed64(data, position));
               position += 8;
               unsafe.putInt(message, oneofCaseOffset, number);
            }
            break;
         case 57:
         case 64:
            if (wireType == 5) {
               unsafe.putObject(message, fieldOffset, ArrayDecoders.decodeFixed32(data, position));
               position += 4;
               unsafe.putInt(message, oneofCaseOffset, number);
            }
            break;
         case 58:
            if (wireType == 0) {
               position = ArrayDecoders.decodeVarint64(data, position, registers);
               unsafe.putObject(message, fieldOffset, registers.long1 != 0L);
               unsafe.putInt(message, oneofCaseOffset, number);
            }
            break;
         case 59:
            if (wireType == 2) {
               position = ArrayDecoders.decodeVarint32(data, position, registers);
               int length = registers.int1;
               if (length == 0) {
                  unsafe.putObject(message, fieldOffset, "");
               } else {
                  if ((typeAndOffset & 536870912) != 0 && !Utf8.isValidUtf8(data, position, position + length)) {
                     throw InvalidProtocolBufferException.invalidUtf8();
                  }

                  String value = new String(data, position, length, Internal.UTF_8);
                  unsafe.putObject(message, fieldOffset, value);
                  position += length;
               }

               unsafe.putInt(message, oneofCaseOffset, number);
            }
            break;
         case 60:
            if (wireType == 2) {
               position = ArrayDecoders.decodeMessageField(this.getMessageFieldSchema(bufferPosition), data, position, limit, registers);
               Object oldValue = unsafe.getInt(message, oneofCaseOffset) == number ? unsafe.getObject(message, fieldOffset) : null;
               if (oldValue == null) {
                  unsafe.putObject(message, fieldOffset, registers.object1);
               } else {
                  unsafe.putObject(message, fieldOffset, Internal.mergeMessage(oldValue, registers.object1));
               }

               unsafe.putInt(message, oneofCaseOffset, number);
            }
            break;
         case 61:
            if (wireType == 2) {
               position = ArrayDecoders.decodeBytes(data, position, registers);
               unsafe.putObject(message, fieldOffset, registers.object1);
               unsafe.putInt(message, oneofCaseOffset, number);
            }
            break;
         case 63:
            if (wireType == 0) {
               position = ArrayDecoders.decodeVarint32(data, position, registers);
               int enumValue = registers.int1;
               Internal.EnumVerifier enumVerifier = this.getEnumFieldVerifier(bufferPosition);
               if (enumVerifier != null && !enumVerifier.isInRange(enumValue)) {
                  getMutableUnknownFields(message).storeField(tag, (long)enumValue);
               } else {
                  unsafe.putObject(message, fieldOffset, enumValue);
                  unsafe.putInt(message, oneofCaseOffset, number);
               }
            }
            break;
         case 66:
            if (wireType == 0) {
               position = ArrayDecoders.decodeVarint32(data, position, registers);
               unsafe.putObject(message, fieldOffset, CodedInputStream.decodeZigZag32(registers.int1));
               unsafe.putInt(message, oneofCaseOffset, number);
            }
            break;
         case 67:
            if (wireType == 0) {
               position = ArrayDecoders.decodeVarint64(data, position, registers);
               unsafe.putObject(message, fieldOffset, CodedInputStream.decodeZigZag64(registers.long1));
               unsafe.putInt(message, oneofCaseOffset, number);
            }
            break;
         case 68:
            if (wireType == 3) {
               int endTag = tag & -8 | 4;
               position = ArrayDecoders.decodeGroupField(this.getMessageFieldSchema(bufferPosition), data, position, limit, endTag, registers);
               Object oldValue = unsafe.getInt(message, oneofCaseOffset) == number ? unsafe.getObject(message, fieldOffset) : null;
               if (oldValue == null) {
                  unsafe.putObject(message, fieldOffset, registers.object1);
               } else {
                  unsafe.putObject(message, fieldOffset, Internal.mergeMessage(oldValue, registers.object1));
               }

               unsafe.putInt(message, oneofCaseOffset, number);
            }
      }

      return position;
   }

   private Schema getMessageFieldSchema(int pos) {
      int index = pos / 3 * 2;
      Schema schema = (Schema)this.objects[index];
      if (schema != null) {
         return schema;
      } else {
         schema = Protobuf.getInstance().schemaFor((Class<T>)this.objects[index + 1]);
         this.objects[index] = schema;
         return schema;
      }
   }

   private Object getMapFieldDefaultEntry(int pos) {
      return this.objects[pos / 3 * 2];
   }

   private Internal.EnumVerifier getEnumFieldVerifier(int pos) {
      return (Internal.EnumVerifier)this.objects[pos / 3 * 2 + 1];
   }

   @CanIgnoreReturnValue
   int parseProto2Message(T message, byte[] data, int position, int limit, int endGroup, ArrayDecoders.Registers registers) throws IOException {
      Unsafe unsafe = UNSAFE;
      int currentPresenceFieldOffset = 1048575;
      int currentPresenceField = 0;
      int tag = 0;
      int oldNumber = -1;
      int pos = 0;

      while(position < limit) {
         tag = data[position++];
         if (tag < 0) {
            position = ArrayDecoders.decodeVarint32(tag, data, position, registers);
            tag = registers.int1;
         }

         int number = tag >>> 3;
         int wireType = tag & 7;
         if (number > oldNumber) {
            pos = this.positionForFieldNumber(number, pos / 3);
         } else {
            pos = this.positionForFieldNumber(number);
         }

         oldNumber = number;
         if (pos == -1) {
            pos = 0;
         } else {
            int typeAndOffset = this.buffer[pos + 1];
            int fieldType = type(typeAndOffset);
            long fieldOffset = offset(typeAndOffset);
            if (fieldType <= 17) {
               int presenceMaskAndOffset = this.buffer[pos + 2];
               int presenceMask = 1 << (presenceMaskAndOffset >>> 20);
               int presenceFieldOffset = presenceMaskAndOffset & 1048575;
               if (presenceFieldOffset != currentPresenceFieldOffset) {
                  if (currentPresenceFieldOffset != 1048575) {
                     unsafe.putInt(message, (long)currentPresenceFieldOffset, currentPresenceField);
                  }

                  currentPresenceFieldOffset = presenceFieldOffset;
                  currentPresenceField = unsafe.getInt(message, (long)presenceFieldOffset);
               }

               switch(fieldType) {
                  case 0:
                     if (wireType == 1) {
                        UnsafeUtil.putDouble(message, fieldOffset, ArrayDecoders.decodeDouble(data, position));
                        position += 8;
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 1:
                     if (wireType == 5) {
                        UnsafeUtil.putFloat(message, fieldOffset, ArrayDecoders.decodeFloat(data, position));
                        position += 4;
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 2:
                  case 3:
                     if (wireType == 0) {
                        position = ArrayDecoders.decodeVarint64(data, position, registers);
                        unsafe.putLong(message, fieldOffset, registers.long1);
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 4:
                  case 11:
                     if (wireType == 0) {
                        position = ArrayDecoders.decodeVarint32(data, position, registers);
                        unsafe.putInt(message, fieldOffset, registers.int1);
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 5:
                  case 14:
                     if (wireType == 1) {
                        unsafe.putLong(message, fieldOffset, ArrayDecoders.decodeFixed64(data, position));
                        position += 8;
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 6:
                  case 13:
                     if (wireType == 5) {
                        unsafe.putInt(message, fieldOffset, ArrayDecoders.decodeFixed32(data, position));
                        position += 4;
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 7:
                     if (wireType == 0) {
                        position = ArrayDecoders.decodeVarint64(data, position, registers);
                        UnsafeUtil.putBoolean(message, fieldOffset, registers.long1 != 0L);
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 8:
                     if (wireType == 2) {
                        if ((typeAndOffset & 536870912) == 0) {
                           position = ArrayDecoders.decodeString(data, position, registers);
                        } else {
                           position = ArrayDecoders.decodeStringRequireUtf8(data, position, registers);
                        }

                        unsafe.putObject(message, fieldOffset, registers.object1);
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 9:
                     if (wireType == 2) {
                        position = ArrayDecoders.decodeMessageField(this.getMessageFieldSchema(pos), data, position, limit, registers);
                        if ((currentPresenceField & presenceMask) == 0) {
                           unsafe.putObject(message, fieldOffset, registers.object1);
                        } else {
                           unsafe.putObject(message, fieldOffset, Internal.mergeMessage(unsafe.getObject(message, fieldOffset), registers.object1));
                        }

                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 10:
                     if (wireType == 2) {
                        position = ArrayDecoders.decodeBytes(data, position, registers);
                        unsafe.putObject(message, fieldOffset, registers.object1);
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 12:
                     if (wireType == 0) {
                        position = ArrayDecoders.decodeVarint32(data, position, registers);
                        int enumValue = registers.int1;
                        Internal.EnumVerifier enumVerifier = this.getEnumFieldVerifier(pos);
                        if (enumVerifier != null && !enumVerifier.isInRange(enumValue)) {
                           getMutableUnknownFields(message).storeField(tag, (long)enumValue);
                           continue;
                        }

                        unsafe.putInt(message, fieldOffset, enumValue);
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 15:
                     if (wireType == 0) {
                        position = ArrayDecoders.decodeVarint32(data, position, registers);
                        unsafe.putInt(message, fieldOffset, CodedInputStream.decodeZigZag32(registers.int1));
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 16:
                     if (wireType == 0) {
                        position = ArrayDecoders.decodeVarint64(data, position, registers);
                        unsafe.putLong(message, fieldOffset, CodedInputStream.decodeZigZag64(registers.long1));
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 17:
                     if (wireType == 3) {
                        int endTag = number << 3 | 4;
                        position = ArrayDecoders.decodeGroupField(this.getMessageFieldSchema(pos), data, position, limit, endTag, registers);
                        if ((currentPresenceField & presenceMask) == 0) {
                           unsafe.putObject(message, fieldOffset, registers.object1);
                        } else {
                           unsafe.putObject(message, fieldOffset, Internal.mergeMessage(unsafe.getObject(message, fieldOffset), registers.object1));
                        }

                        currentPresenceField |= presenceMask;
                        continue;
                     }
               }
            } else if (fieldType == 27) {
               if (wireType == 2) {
                  Internal.ProtobufList<?> list = (Internal.ProtobufList)unsafe.getObject(message, fieldOffset);
                  if (!list.isModifiable()) {
                     int size = list.size();
                     list = list.mutableCopyWithCapacity(size == 0 ? 10 : size * 2);
                     unsafe.putObject(message, fieldOffset, list);
                  }

                  position = ArrayDecoders.decodeMessageList(this.getMessageFieldSchema(pos), tag, data, position, limit, list, registers);
                  continue;
               }
            } else if (fieldType <= 49) {
               int oldPosition = position;
               position = this.parseRepeatedField(
                  message, data, position, limit, tag, number, wireType, pos, (long)typeAndOffset, fieldType, fieldOffset, registers
               );
               if (position != oldPosition) {
                  continue;
               }
            } else if (fieldType == 50) {
               if (wireType == 2) {
                  int oldPosition = position;
                  position = this.parseMapField(message, data, position, limit, pos, fieldOffset, registers);
                  if (position != oldPosition) {
                     continue;
                  }
               }
            } else {
               int oldPosition = position;
               position = this.parseOneofField(message, data, position, limit, tag, number, wireType, typeAndOffset, fieldType, fieldOffset, pos, registers);
               if (position != oldPosition) {
                  continue;
               }
            }
         }

         if (tag == endGroup && endGroup != 0) {
            break;
         }

         if (this.hasExtensions && registers.extensionRegistry != ExtensionRegistryLite.getEmptyRegistry()) {
            position = ArrayDecoders.decodeExtensionOrUnknownField(
               tag, data, position, limit, message, this.defaultInstance, this.unknownFieldSchema, registers
            );
         } else {
            position = ArrayDecoders.decodeUnknownField(tag, data, position, limit, getMutableUnknownFields(message), registers);
         }
      }

      if (currentPresenceFieldOffset != 1048575) {
         unsafe.putInt(message, (long)currentPresenceFieldOffset, currentPresenceField);
      }

      UnknownFieldSetLite unknownFields = null;

      for(int i = this.checkInitializedCount; i < this.repeatedFieldOffsetStart; ++i) {
         unknownFields = this.filterMapUnknownEnumValues(message, this.intArray[i], unknownFields, this.unknownFieldSchema);
      }

      if (unknownFields != null) {
         this.unknownFieldSchema.setBuilderToMessage(message, unknownFields);
      }

      if (endGroup == 0) {
         if (position != limit) {
            throw InvalidProtocolBufferException.parseFailure();
         }
      } else if (position > limit || tag != endGroup) {
         throw InvalidProtocolBufferException.parseFailure();
      }

      return position;
   }

   @CanIgnoreReturnValue
   private int parseProto3Message(T message, byte[] data, int position, int limit, ArrayDecoders.Registers registers) throws IOException {
      Unsafe unsafe = UNSAFE;
      int currentPresenceFieldOffset = 1048575;
      int currentPresenceField = 0;
      int tag = 0;
      int oldNumber = -1;
      int pos = 0;

      while(position < limit) {
         tag = data[position++];
         if (tag < 0) {
            position = ArrayDecoders.decodeVarint32(tag, data, position, registers);
            tag = registers.int1;
         }

         int number = tag >>> 3;
         int wireType = tag & 7;
         if (number > oldNumber) {
            pos = this.positionForFieldNumber(number, pos / 3);
         } else {
            pos = this.positionForFieldNumber(number);
         }

         oldNumber = number;
         if (pos == -1) {
            pos = 0;
         } else {
            int typeAndOffset = this.buffer[pos + 1];
            int fieldType = type(typeAndOffset);
            long fieldOffset = offset(typeAndOffset);
            if (fieldType <= 17) {
               int presenceMaskAndOffset = this.buffer[pos + 2];
               int presenceMask = 1 << (presenceMaskAndOffset >>> 20);
               int presenceFieldOffset = presenceMaskAndOffset & 1048575;
               if (presenceFieldOffset != currentPresenceFieldOffset) {
                  if (currentPresenceFieldOffset != 1048575) {
                     unsafe.putInt(message, (long)currentPresenceFieldOffset, currentPresenceField);
                  }

                  if (presenceFieldOffset != 1048575) {
                     currentPresenceField = unsafe.getInt(message, (long)presenceFieldOffset);
                  }

                  currentPresenceFieldOffset = presenceFieldOffset;
               }

               switch(fieldType) {
                  case 0:
                     if (wireType == 1) {
                        UnsafeUtil.putDouble(message, fieldOffset, ArrayDecoders.decodeDouble(data, position));
                        position += 8;
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 1:
                     if (wireType == 5) {
                        UnsafeUtil.putFloat(message, fieldOffset, ArrayDecoders.decodeFloat(data, position));
                        position += 4;
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 2:
                  case 3:
                     if (wireType == 0) {
                        position = ArrayDecoders.decodeVarint64(data, position, registers);
                        unsafe.putLong(message, fieldOffset, registers.long1);
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 4:
                  case 11:
                     if (wireType == 0) {
                        position = ArrayDecoders.decodeVarint32(data, position, registers);
                        unsafe.putInt(message, fieldOffset, registers.int1);
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 5:
                  case 14:
                     if (wireType == 1) {
                        unsafe.putLong(message, fieldOffset, ArrayDecoders.decodeFixed64(data, position));
                        position += 8;
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 6:
                  case 13:
                     if (wireType == 5) {
                        unsafe.putInt(message, fieldOffset, ArrayDecoders.decodeFixed32(data, position));
                        position += 4;
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 7:
                     if (wireType == 0) {
                        position = ArrayDecoders.decodeVarint64(data, position, registers);
                        UnsafeUtil.putBoolean(message, fieldOffset, registers.long1 != 0L);
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 8:
                     if (wireType == 2) {
                        if ((typeAndOffset & 536870912) == 0) {
                           position = ArrayDecoders.decodeString(data, position, registers);
                        } else {
                           position = ArrayDecoders.decodeStringRequireUtf8(data, position, registers);
                        }

                        unsafe.putObject(message, fieldOffset, registers.object1);
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 9:
                     if (wireType == 2) {
                        position = ArrayDecoders.decodeMessageField(this.getMessageFieldSchema(pos), data, position, limit, registers);
                        Object oldValue = unsafe.getObject(message, fieldOffset);
                        if (oldValue == null) {
                           unsafe.putObject(message, fieldOffset, registers.object1);
                        } else {
                           unsafe.putObject(message, fieldOffset, Internal.mergeMessage(oldValue, registers.object1));
                        }

                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 10:
                     if (wireType == 2) {
                        position = ArrayDecoders.decodeBytes(data, position, registers);
                        unsafe.putObject(message, fieldOffset, registers.object1);
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 12:
                     if (wireType == 0) {
                        position = ArrayDecoders.decodeVarint32(data, position, registers);
                        unsafe.putInt(message, fieldOffset, registers.int1);
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 15:
                     if (wireType == 0) {
                        position = ArrayDecoders.decodeVarint32(data, position, registers);
                        unsafe.putInt(message, fieldOffset, CodedInputStream.decodeZigZag32(registers.int1));
                        currentPresenceField |= presenceMask;
                        continue;
                     }
                     break;
                  case 16:
                     if (wireType == 0) {
                        position = ArrayDecoders.decodeVarint64(data, position, registers);
                        unsafe.putLong(message, fieldOffset, CodedInputStream.decodeZigZag64(registers.long1));
                        currentPresenceField |= presenceMask;
                        continue;
                     }
               }
            } else if (fieldType == 27) {
               if (wireType == 2) {
                  Internal.ProtobufList<?> list = (Internal.ProtobufList)unsafe.getObject(message, fieldOffset);
                  if (!list.isModifiable()) {
                     int size = list.size();
                     list = list.mutableCopyWithCapacity(size == 0 ? 10 : size * 2);
                     unsafe.putObject(message, fieldOffset, list);
                  }

                  position = ArrayDecoders.decodeMessageList(this.getMessageFieldSchema(pos), tag, data, position, limit, list, registers);
                  continue;
               }
            } else if (fieldType <= 49) {
               int oldPosition = position;
               position = this.parseRepeatedField(
                  message, data, position, limit, tag, number, wireType, pos, (long)typeAndOffset, fieldType, fieldOffset, registers
               );
               if (position != oldPosition) {
                  continue;
               }
            } else if (fieldType == 50) {
               if (wireType == 2) {
                  int oldPosition = position;
                  position = this.parseMapField(message, data, position, limit, pos, fieldOffset, registers);
                  if (position != oldPosition) {
                     continue;
                  }
               }
            } else {
               int oldPosition = position;
               position = this.parseOneofField(message, data, position, limit, tag, number, wireType, typeAndOffset, fieldType, fieldOffset, pos, registers);
               if (position != oldPosition) {
                  continue;
               }
            }
         }

         position = ArrayDecoders.decodeUnknownField(tag, data, position, limit, getMutableUnknownFields(message), registers);
      }

      if (currentPresenceFieldOffset != 1048575) {
         unsafe.putInt(message, (long)currentPresenceFieldOffset, currentPresenceField);
      }

      if (position != limit) {
         throw InvalidProtocolBufferException.parseFailure();
      } else {
         return position;
      }
   }

   @Override
   public void mergeFrom(T message, byte[] data, int position, int limit, ArrayDecoders.Registers registers) throws IOException {
      if (this.proto3) {
         this.parseProto3Message(message, data, position, limit, registers);
      } else {
         this.parseProto2Message(message, data, position, limit, 0, registers);
      }

   }

   @Override
   public void makeImmutable(T message) {
      for(int i = this.checkInitializedCount; i < this.repeatedFieldOffsetStart; ++i) {
         long offset = offset(this.typeAndOffsetAt(this.intArray[i]));
         Object mapField = UnsafeUtil.getObject(message, offset);
         if (mapField != null) {
            UnsafeUtil.putObject(message, offset, this.mapFieldSchema.toImmutable(mapField));
         }
      }

      int length = this.intArray.length;

      for(int i = this.repeatedFieldOffsetStart; i < length; ++i) {
         this.listFieldSchema.makeImmutableListAt(message, (long)this.intArray[i]);
      }

      this.unknownFieldSchema.makeImmutable(message);
      if (this.hasExtensions) {
         this.extensionSchema.makeImmutable(message);
      }

   }

   private final <K, V> void mergeMap(Object message, int pos, Object mapDefaultEntry, ExtensionRegistryLite extensionRegistry, Reader reader) throws IOException {
      long offset = offset(this.typeAndOffsetAt(pos));
      Object mapField = UnsafeUtil.getObject(message, offset);
      if (mapField == null) {
         mapField = this.mapFieldSchema.newMapField(mapDefaultEntry);
         UnsafeUtil.putObject(message, offset, mapField);
      } else if (this.mapFieldSchema.isImmutable(mapField)) {
         Object oldMapField = mapField;
         mapField = this.mapFieldSchema.newMapField(mapDefaultEntry);
         this.mapFieldSchema.mergeFrom(mapField, oldMapField);
         UnsafeUtil.putObject(message, offset, mapField);
      }

      reader.readMap(this.mapFieldSchema.forMutableMapData(mapField), this.mapFieldSchema.forMapMetadata(mapDefaultEntry), extensionRegistry);
   }

   private final <UT, UB> UB filterMapUnknownEnumValues(Object message, int pos, UB unknownFields, UnknownFieldSchema<UT, UB> unknownFieldSchema) {
      int fieldNumber = this.numberAt(pos);
      long offset = offset(this.typeAndOffsetAt(pos));
      Object mapField = UnsafeUtil.getObject(message, offset);
      if (mapField == null) {
         return unknownFields;
      } else {
         Internal.EnumVerifier enumVerifier = this.getEnumFieldVerifier(pos);
         if (enumVerifier == null) {
            return unknownFields;
         } else {
            Map<?, ?> mapData = this.mapFieldSchema.forMutableMapData(mapField);
            return this.filterUnknownEnumMap(pos, fieldNumber, mapData, enumVerifier, unknownFields, unknownFieldSchema);
         }
      }
   }

   private final <K, V, UT, UB> UB filterUnknownEnumMap(
      int pos, int number, Map<K, V> mapData, Internal.EnumVerifier enumVerifier, UB unknownFields, UnknownFieldSchema<UT, UB> unknownFieldSchema
   ) {
      MapEntryLite.Metadata<K, V> metadata = this.mapFieldSchema.forMapMetadata(this.getMapFieldDefaultEntry(pos));
      Iterator<Entry<K, V>> it = mapData.entrySet().iterator();

      while(it.hasNext()) {
         Entry<K, V> entry = (Entry)it.next();
         if (!enumVerifier.isInRange(entry.getValue())) {
            if (unknownFields == null) {
               unknownFields = unknownFieldSchema.newBuilder();
            }

            int entrySize = MapEntryLite.computeSerializedSize(metadata, (K)entry.getKey(), (V)entry.getValue());
            ByteString.CodedBuilder codedBuilder = ByteString.newCodedBuilder(entrySize);
            CodedOutputStream codedOutput = codedBuilder.getCodedOutput();

            try {
               MapEntryLite.writeTo(codedOutput, metadata, (K)entry.getKey(), (V)entry.getValue());
            } catch (IOException var14) {
               throw new RuntimeException(var14);
            }

            unknownFieldSchema.addLengthDelimited(unknownFields, number, codedBuilder.build());
            it.remove();
         }
      }

      return unknownFields;
   }

   @Override
   public final boolean isInitialized(T message) {
      int currentPresenceFieldOffset = 1048575;
      int currentPresenceField = 0;

      for(int i = 0; i < this.checkInitializedCount; ++i) {
         int pos = this.intArray[i];
         int number = this.numberAt(pos);
         int typeAndOffset = this.typeAndOffsetAt(pos);
         int presenceMaskAndOffset = this.buffer[pos + 2];
         int presenceFieldOffset = presenceMaskAndOffset & 1048575;
         int presenceMask = 1 << (presenceMaskAndOffset >>> 20);
         if (presenceFieldOffset != currentPresenceFieldOffset) {
            currentPresenceFieldOffset = presenceFieldOffset;
            if (presenceFieldOffset != 1048575) {
               currentPresenceField = UNSAFE.getInt(message, (long)presenceFieldOffset);
            }
         }

         if (isRequired(typeAndOffset) && !this.isFieldPresent(message, pos, currentPresenceFieldOffset, currentPresenceField, presenceMask)) {
            return false;
         }

         switch(type(typeAndOffset)) {
            case 9:
            case 17:
               if (this.isFieldPresent(message, pos, currentPresenceFieldOffset, currentPresenceField, presenceMask)
                  && !isInitialized(message, typeAndOffset, this.getMessageFieldSchema(pos))) {
                  return false;
               }
               break;
            case 27:
            case 49:
               if (!this.isListInitialized(message, typeAndOffset, pos)) {
                  return false;
               }
               break;
            case 50:
               if (!this.isMapInitialized(message, typeAndOffset, pos)) {
                  return false;
               }
               break;
            case 60:
            case 68:
               if (this.isOneofPresent(message, number, pos) && !isInitialized(message, typeAndOffset, this.getMessageFieldSchema(pos))) {
                  return false;
               }
         }
      }

      return !this.hasExtensions || this.extensionSchema.getExtensions(message).isInitialized();
   }

   private static boolean isInitialized(Object message, int typeAndOffset, Schema schema) {
      Object nested = UnsafeUtil.getObject(message, offset(typeAndOffset));
      return schema.isInitialized(nested);
   }

   private <N> boolean isListInitialized(Object message, int typeAndOffset, int pos) {
      List<N> list = (List)UnsafeUtil.getObject(message, offset(typeAndOffset));
      if (list.isEmpty()) {
         return true;
      } else {
         Schema schema = this.getMessageFieldSchema(pos);

         for(int i = 0; i < list.size(); ++i) {
            N nested = (N)list.get(i);
            if (!schema.isInitialized(nested)) {
               return false;
            }
         }

         return true;
      }
   }

   private boolean isMapInitialized(T message, int typeAndOffset, int pos) {
      Map<?, ?> map = this.mapFieldSchema.forMapData(UnsafeUtil.getObject(message, offset(typeAndOffset)));
      if (map.isEmpty()) {
         return true;
      } else {
         Object mapDefaultEntry = this.getMapFieldDefaultEntry(pos);
         MapEntryLite.Metadata<?, ?> metadata = this.mapFieldSchema.forMapMetadata(mapDefaultEntry);
         if (metadata.valueType.getJavaType() != WireFormat.JavaType.MESSAGE) {
            return true;
         } else {
            Schema schema = null;

            for(Object nested : map.values()) {
               if (schema == null) {
                  schema = Protobuf.getInstance().schemaFor(nested.getClass());
               }

               if (!schema.isInitialized(nested)) {
                  return false;
               }
            }

            return true;
         }
      }
   }

   private void writeString(int fieldNumber, Object value, Writer writer) throws IOException {
      if (value instanceof String) {
         writer.writeString(fieldNumber, (String)value);
      } else {
         writer.writeBytes(fieldNumber, (ByteString)value);
      }

   }

   private void readString(Object message, int typeAndOffset, Reader reader) throws IOException {
      if (isEnforceUtf8(typeAndOffset)) {
         UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readStringRequireUtf8());
      } else if (this.lite) {
         UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readString());
      } else {
         UnsafeUtil.putObject(message, offset(typeAndOffset), reader.readBytes());
      }

   }

   private void readStringList(Object message, int typeAndOffset, Reader reader) throws IOException {
      if (isEnforceUtf8(typeAndOffset)) {
         reader.readStringListRequireUtf8(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
      } else {
         reader.readStringList(this.listFieldSchema.mutableListAt(message, offset(typeAndOffset)));
      }

   }

   private <E> void readMessageList(Object message, int typeAndOffset, Reader reader, Schema<E> schema, ExtensionRegistryLite extensionRegistry) throws IOException {
      long offset = offset(typeAndOffset);
      reader.readMessageList(this.listFieldSchema.mutableListAt(message, offset), schema, extensionRegistry);
   }

   private <E> void readGroupList(Object message, long offset, Reader reader, Schema<E> schema, ExtensionRegistryLite extensionRegistry) throws IOException {
      reader.readGroupList(this.listFieldSchema.mutableListAt(message, offset), schema, extensionRegistry);
   }

   private int numberAt(int pos) {
      return this.buffer[pos];
   }

   private int typeAndOffsetAt(int pos) {
      return this.buffer[pos + 1];
   }

   private int presenceMaskAndOffsetAt(int pos) {
      return this.buffer[pos + 2];
   }

   private static int type(int value) {
      return (value & 267386880) >>> 20;
   }

   private static boolean isRequired(int value) {
      return (value & 268435456) != 0;
   }

   private static boolean isEnforceUtf8(int value) {
      return (value & 536870912) != 0;
   }

   private static long offset(int value) {
      return (long)(value & 1048575);
   }

   private static <T> double doubleAt(T message, long offset) {
      return UnsafeUtil.getDouble(message, offset);
   }

   private static <T> float floatAt(T message, long offset) {
      return UnsafeUtil.getFloat(message, offset);
   }

   private static <T> int intAt(T message, long offset) {
      return UnsafeUtil.getInt(message, offset);
   }

   private static <T> long longAt(T message, long offset) {
      return UnsafeUtil.getLong(message, offset);
   }

   private static <T> boolean booleanAt(T message, long offset) {
      return UnsafeUtil.getBoolean(message, offset);
   }

   private static <T> double oneofDoubleAt(T message, long offset) {
      return UnsafeUtil.getObject(message, offset);
   }

   private static <T> float oneofFloatAt(T message, long offset) {
      return UnsafeUtil.getObject(message, offset);
   }

   private static <T> int oneofIntAt(T message, long offset) {
      return UnsafeUtil.getObject(message, offset);
   }

   private static <T> long oneofLongAt(T message, long offset) {
      return UnsafeUtil.getObject(message, offset);
   }

   private static <T> boolean oneofBooleanAt(T message, long offset) {
      return UnsafeUtil.getObject(message, offset);
   }

   private boolean arePresentForEquals(T message, T other, int pos) {
      return this.isFieldPresent(message, pos) == this.isFieldPresent(other, pos);
   }

   private boolean isFieldPresent(T message, int pos, int presenceFieldOffset, int presenceField, int presenceMask) {
      if (presenceFieldOffset == 1048575) {
         return this.isFieldPresent(message, pos);
      } else {
         return (presenceField & presenceMask) != 0;
      }
   }

   private boolean isFieldPresent(T message, int pos) {
      int presenceMaskAndOffset = this.presenceMaskAndOffsetAt(pos);
      long presenceFieldOffset = (long)(presenceMaskAndOffset & 1048575);
      if (presenceFieldOffset == 1048575L) {
         int typeAndOffset = this.typeAndOffsetAt(pos);
         long offset = offset(typeAndOffset);
         switch(type(typeAndOffset)) {
            case 0:
               return Double.doubleToRawLongBits(UnsafeUtil.getDouble(message, offset)) != 0L;
            case 1:
               return Float.floatToRawIntBits(UnsafeUtil.getFloat(message, offset)) != 0;
            case 2:
               return UnsafeUtil.getLong(message, offset) != 0L;
            case 3:
               return UnsafeUtil.getLong(message, offset) != 0L;
            case 4:
               return UnsafeUtil.getInt(message, offset) != 0;
            case 5:
               return UnsafeUtil.getLong(message, offset) != 0L;
            case 6:
               return UnsafeUtil.getInt(message, offset) != 0;
            case 7:
               return UnsafeUtil.getBoolean(message, offset);
            case 8:
               Object value = UnsafeUtil.getObject(message, offset);
               if (value instanceof String) {
                  return !((String)value).isEmpty();
               } else {
                  if (value instanceof ByteString) {
                     return !ByteString.EMPTY.equals(value);
                  }

                  throw new IllegalArgumentException();
               }
            case 9:
               return UnsafeUtil.getObject(message, offset) != null;
            case 10:
               return !ByteString.EMPTY.equals(UnsafeUtil.getObject(message, offset));
            case 11:
               return UnsafeUtil.getInt(message, offset) != 0;
            case 12:
               return UnsafeUtil.getInt(message, offset) != 0;
            case 13:
               return UnsafeUtil.getInt(message, offset) != 0;
            case 14:
               return UnsafeUtil.getLong(message, offset) != 0L;
            case 15:
               return UnsafeUtil.getInt(message, offset) != 0;
            case 16:
               return UnsafeUtil.getLong(message, offset) != 0L;
            case 17:
               return UnsafeUtil.getObject(message, offset) != null;
            default:
               throw new IllegalArgumentException();
         }
      } else {
         int presenceMask = 1 << (presenceMaskAndOffset >>> 20);
         return (UnsafeUtil.getInt(message, (long)(presenceMaskAndOffset & 1048575)) & presenceMask) != 0;
      }
   }

   private void setFieldPresent(T message, int pos) {
      int presenceMaskAndOffset = this.presenceMaskAndOffsetAt(pos);
      long presenceFieldOffset = (long)(presenceMaskAndOffset & 1048575);
      if (presenceFieldOffset != 1048575L) {
         int presenceMask = 1 << (presenceMaskAndOffset >>> 20);
         UnsafeUtil.putInt(message, presenceFieldOffset, UnsafeUtil.getInt(message, presenceFieldOffset) | presenceMask);
      }
   }

   private boolean isOneofPresent(T message, int fieldNumber, int pos) {
      int presenceMaskAndOffset = this.presenceMaskAndOffsetAt(pos);
      return UnsafeUtil.getInt(message, (long)(presenceMaskAndOffset & 1048575)) == fieldNumber;
   }

   private boolean isOneofCaseEqual(T message, T other, int pos) {
      int presenceMaskAndOffset = this.presenceMaskAndOffsetAt(pos);
      return UnsafeUtil.getInt(message, (long)(presenceMaskAndOffset & 1048575)) == UnsafeUtil.getInt(other, (long)(presenceMaskAndOffset & 1048575));
   }

   private void setOneofPresent(T message, int fieldNumber, int pos) {
      int presenceMaskAndOffset = this.presenceMaskAndOffsetAt(pos);
      UnsafeUtil.putInt(message, (long)(presenceMaskAndOffset & 1048575), fieldNumber);
   }

   private int positionForFieldNumber(int number) {
      return number >= this.minFieldNumber && number <= this.maxFieldNumber ? this.slowPositionForFieldNumber(number, 0) : -1;
   }

   private int positionForFieldNumber(int number, int min) {
      return number >= this.minFieldNumber && number <= this.maxFieldNumber ? this.slowPositionForFieldNumber(number, min) : -1;
   }

   private int slowPositionForFieldNumber(int number, int min) {
      int max = this.buffer.length / 3 - 1;

      while(min <= max) {
         int mid = max + min >>> 1;
         int pos = mid * 3;
         int midFieldNumber = this.numberAt(pos);
         if (number == midFieldNumber) {
            return pos;
         }

         if (number < midFieldNumber) {
            max = mid - 1;
         } else {
            min = mid + 1;
         }
      }

      return -1;
   }

   int getSchemaSize() {
      return this.buffer.length * 3;
   }
}
