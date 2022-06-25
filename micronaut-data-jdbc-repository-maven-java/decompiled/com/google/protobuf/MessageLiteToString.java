package com.google.protobuf;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

final class MessageLiteToString {
   private static final String LIST_SUFFIX = "List";
   private static final String BUILDER_LIST_SUFFIX = "OrBuilderList";
   private static final String MAP_SUFFIX = "Map";
   private static final String BYTES_SUFFIX = "Bytes";

   private MessageLiteToString() {
   }

   static String toString(MessageLite messageLite, String commentString) {
      StringBuilder buffer = new StringBuilder();
      buffer.append("# ").append(commentString);
      reflectivePrintWithIndent(messageLite, buffer, 0);
      return buffer.toString();
   }

   private static void reflectivePrintWithIndent(MessageLite messageLite, StringBuilder buffer, int indent) {
      Map<String, java.lang.reflect.Method> nameToNoArgMethod = new HashMap();
      Map<String, java.lang.reflect.Method> nameToMethod = new HashMap();
      Set<String> getters = new TreeSet();

      for(java.lang.reflect.Method method : messageLite.getClass().getDeclaredMethods()) {
         nameToMethod.put(method.getName(), method);
         if (method.getParameterTypes().length == 0) {
            nameToNoArgMethod.put(method.getName(), method);
            if (method.getName().startsWith("get")) {
               getters.add(method.getName());
            }
         }
      }

      for(String getter : getters) {
         String suffix = getter.startsWith("get") ? getter.substring(3) : getter;
         if (suffix.endsWith("List") && !suffix.endsWith("OrBuilderList") && !suffix.equals("List")) {
            String camelCase = suffix.substring(0, 1).toLowerCase() + suffix.substring(1, suffix.length() - "List".length());
            java.lang.reflect.Method listMethod = (java.lang.reflect.Method)nameToNoArgMethod.get(getter);
            if (listMethod != null && listMethod.getReturnType().equals(List.class)) {
               printField(buffer, indent, camelCaseToSnakeCase(camelCase), GeneratedMessageLite.invokeOrDie(listMethod, messageLite));
               continue;
            }
         }

         if (suffix.endsWith("Map") && !suffix.equals("Map")) {
            String camelCase = suffix.substring(0, 1).toLowerCase() + suffix.substring(1, suffix.length() - "Map".length());
            java.lang.reflect.Method mapMethod = (java.lang.reflect.Method)nameToNoArgMethod.get(getter);
            if (mapMethod != null
               && mapMethod.getReturnType().equals(Map.class)
               && !mapMethod.isAnnotationPresent(Deprecated.class)
               && Modifier.isPublic(mapMethod.getModifiers())) {
               printField(buffer, indent, camelCaseToSnakeCase(camelCase), GeneratedMessageLite.invokeOrDie(mapMethod, messageLite));
               continue;
            }
         }

         java.lang.reflect.Method setter = (java.lang.reflect.Method)nameToMethod.get("set" + suffix);
         if (setter != null && (!suffix.endsWith("Bytes") || !nameToNoArgMethod.containsKey("get" + suffix.substring(0, suffix.length() - "Bytes".length())))) {
            String camelCase = suffix.substring(0, 1).toLowerCase() + suffix.substring(1);
            java.lang.reflect.Method getMethod = (java.lang.reflect.Method)nameToNoArgMethod.get("get" + suffix);
            java.lang.reflect.Method hasMethod = (java.lang.reflect.Method)nameToNoArgMethod.get("has" + suffix);
            if (getMethod != null) {
               Object value = GeneratedMessageLite.invokeOrDie(getMethod, messageLite);
               boolean hasValue = hasMethod == null ? !isDefaultValue(value) : GeneratedMessageLite.invokeOrDie(hasMethod, messageLite);
               if (hasValue) {
                  printField(buffer, indent, camelCaseToSnakeCase(camelCase), value);
               }
            }
         }
      }

      if (messageLite instanceof GeneratedMessageLite.ExtendableMessage) {
         for(Entry<GeneratedMessageLite.ExtensionDescriptor, Object> entry : ((GeneratedMessageLite.ExtendableMessage)messageLite).extensions) {
            printField(buffer, indent, "[" + ((GeneratedMessageLite.ExtensionDescriptor)entry.getKey()).getNumber() + "]", entry.getValue());
         }
      }

      if (((GeneratedMessageLite)messageLite).unknownFields != null) {
         ((GeneratedMessageLite)messageLite).unknownFields.printWithIndent(buffer, indent);
      }

   }

   private static boolean isDefaultValue(Object o) {
      if (o instanceof Boolean) {
         return !(Boolean)o;
      } else if (o instanceof Integer) {
         return (Integer)o == 0;
      } else if (o instanceof Float) {
         return Float.floatToRawIntBits((Float)o) == 0;
      } else if (o instanceof Double) {
         return Double.doubleToRawLongBits((Double)o) == 0L;
      } else if (o instanceof String) {
         return o.equals("");
      } else if (o instanceof ByteString) {
         return o.equals(ByteString.EMPTY);
      } else if (o instanceof MessageLite) {
         return o == ((MessageLite)o).getDefaultInstanceForType();
      } else if (o instanceof java.lang.Enum) {
         return ((java.lang.Enum)o).ordinal() == 0;
      } else {
         return false;
      }
   }

   static final void printField(StringBuilder buffer, int indent, String name, Object object) {
      if (object instanceof List) {
         for(Object entry : (List)object) {
            printField(buffer, indent, name, entry);
         }

      } else if (object instanceof Map) {
         Map<?, ?> map = (Map)object;

         for(Entry<?, ?> entry : map.entrySet()) {
            printField(buffer, indent, name, entry);
         }

      } else {
         buffer.append('\n');

         for(int i = 0; i < indent; ++i) {
            buffer.append(' ');
         }

         buffer.append(name);
         if (object instanceof String) {
            buffer.append(": \"").append(TextFormatEscaper.escapeText((String)object)).append('"');
         } else if (object instanceof ByteString) {
            buffer.append(": \"").append(TextFormatEscaper.escapeBytes((ByteString)object)).append('"');
         } else if (object instanceof GeneratedMessageLite) {
            buffer.append(" {");
            reflectivePrintWithIndent((GeneratedMessageLite)object, buffer, indent + 2);
            buffer.append("\n");

            for(int i = 0; i < indent; ++i) {
               buffer.append(' ');
            }

            buffer.append("}");
         } else if (object instanceof Entry) {
            buffer.append(" {");
            Entry<?, ?> entry = (Entry)object;
            printField(buffer, indent + 2, "key", entry.getKey());
            printField(buffer, indent + 2, "value", entry.getValue());
            buffer.append("\n");

            for(int i = 0; i < indent; ++i) {
               buffer.append(' ');
            }

            buffer.append("}");
         } else {
            buffer.append(": ").append(object);
         }

      }
   }

   private static final String camelCaseToSnakeCase(String camelCase) {
      StringBuilder builder = new StringBuilder();

      for(int i = 0; i < camelCase.length(); ++i) {
         char ch = camelCase.charAt(i);
         if (Character.isUpperCase(ch)) {
            builder.append("_");
         }

         builder.append(Character.toLowerCase(ch));
      }

      return builder.toString();
   }
}
