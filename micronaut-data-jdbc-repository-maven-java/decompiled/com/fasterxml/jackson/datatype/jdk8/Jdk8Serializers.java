package com.fasterxml.jackson.datatype.jdk8;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.Serializable;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class Jdk8Serializers extends Serializers.Base implements Serializable {
   private static final long serialVersionUID = 1L;

   @Override
   public JsonSerializer<?> findReferenceSerializer(
      SerializationConfig config,
      ReferenceType refType,
      BeanDescription beanDesc,
      TypeSerializer contentTypeSerializer,
      JsonSerializer<Object> contentValueSerializer
   ) {
      Class<?> raw = refType.getRawClass();
      if (!Optional.class.isAssignableFrom(raw)) {
         if (OptionalInt.class.isAssignableFrom(raw)) {
            return OptionalIntSerializer.INSTANCE;
         } else if (OptionalLong.class.isAssignableFrom(raw)) {
            return OptionalLongSerializer.INSTANCE;
         } else {
            return OptionalDouble.class.isAssignableFrom(raw) ? OptionalDoubleSerializer.INSTANCE : null;
         }
      } else {
         boolean staticTyping = contentTypeSerializer == null && config.isEnabled(MapperFeature.USE_STATIC_TYPING);
         return new OptionalSerializer(refType, staticTyping, contentTypeSerializer, contentValueSerializer);
      }
   }

   @Override
   public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
      Class<?> raw = type.getRawClass();
      if (LongStream.class.isAssignableFrom(raw)) {
         return LongStreamSerializer.INSTANCE;
      } else if (IntStream.class.isAssignableFrom(raw)) {
         return IntStreamSerializer.INSTANCE;
      } else if (DoubleStream.class.isAssignableFrom(raw)) {
         return DoubleStreamSerializer.INSTANCE;
      } else if (!Stream.class.isAssignableFrom(raw)) {
         return null;
      } else {
         JavaType[] params = config.getTypeFactory().findTypeParameters(type, Stream.class);
         JavaType vt = params != null && params.length == 1 ? params[0] : TypeFactory.unknownType();
         return new StreamSerializer(config.getTypeFactory().constructParametricType(Stream.class, vt), vt);
      }
   }
}
