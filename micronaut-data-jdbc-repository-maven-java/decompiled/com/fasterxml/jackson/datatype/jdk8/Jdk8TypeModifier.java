package com.fasterxml.jackson.datatype.jdk8;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public class Jdk8TypeModifier extends TypeModifier implements Serializable {
   private static final long serialVersionUID = 1L;

   @Override
   public JavaType modifyType(JavaType type, Type jdkType, TypeBindings bindings, TypeFactory typeFactory) {
      if (!type.isReferenceType() && !type.isContainerType()) {
         Class<?> raw = type.getRawClass();
         JavaType refType;
         if (raw == Optional.class) {
            refType = type.containedTypeOrUnknown(0);
         } else if (raw == OptionalInt.class) {
            refType = typeFactory.constructType(Integer.TYPE);
         } else if (raw == OptionalLong.class) {
            refType = typeFactory.constructType(Long.TYPE);
         } else {
            if (raw != OptionalDouble.class) {
               return type;
            }

            refType = typeFactory.constructType(Double.TYPE);
         }

         return ReferenceType.upgradeFrom(type, refType);
      } else {
         return type;
      }
   }
}
