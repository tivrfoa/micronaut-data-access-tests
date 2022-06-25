package com.fasterxml.jackson.datatype.jdk8;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.ReferenceType;
import java.io.Serializable;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public class Jdk8Deserializers extends Deserializers.Base implements Serializable {
   private static final long serialVersionUID = 1L;

   @Override
   public JsonDeserializer<?> findReferenceDeserializer(
      ReferenceType refType,
      DeserializationConfig config,
      BeanDescription beanDesc,
      TypeDeserializer contentTypeDeserializer,
      JsonDeserializer<?> contentDeserializer
   ) {
      if (refType.hasRawClass(Optional.class)) {
         return new OptionalDeserializer(refType, null, contentTypeDeserializer, contentDeserializer);
      } else if (refType.hasRawClass(OptionalInt.class)) {
         return OptionalIntDeserializer.INSTANCE;
      } else if (refType.hasRawClass(OptionalLong.class)) {
         return OptionalLongDeserializer.INSTANCE;
      } else {
         return refType.hasRawClass(OptionalDouble.class) ? OptionalDoubleDeserializer.INSTANCE : null;
      }
   }
}
