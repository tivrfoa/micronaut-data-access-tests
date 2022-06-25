package com.fasterxml.jackson.datatype.jdk8;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public class Jdk8BeanSerializerModifier extends BeanSerializerModifier {
   @Override
   public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
      for(int i = 0; i < beanProperties.size(); ++i) {
         BeanPropertyWriter writer = (BeanPropertyWriter)beanProperties.get(i);
         JavaType type = writer.getType();
         Object empty;
         if (type.isTypeOrSubTypeOf(Optional.class)) {
            empty = Optional.empty();
         } else if (type.hasRawClass(OptionalLong.class)) {
            empty = OptionalLong.empty();
         } else if (type.hasRawClass(OptionalInt.class)) {
            empty = OptionalInt.empty();
         } else {
            if (!type.hasRawClass(OptionalDouble.class)) {
               continue;
            }

            empty = OptionalDouble.empty();
         }

         beanProperties.set(i, new Jdk8OptionalBeanPropertyWriter(writer, empty));
      }

      return beanProperties;
   }
}
