package io.micronaut.inject.ast;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Internal
class ReflectClassElement extends ReflectTypeElement<Class<?>> {
   ReflectClassElement(Class<?> type) {
      super(type);
   }

   @Override
   public boolean isArray() {
      return this.type.isArray();
   }

   @Override
   public int getArrayDimensions() {
      return this.computeDimensions(this.type);
   }

   private int computeDimensions(Class<?> type) {
      int i;
      for(i = 0; type.isArray(); type = type.getComponentType()) {
         ++i;
      }

      return i;
   }

   @Override
   public ClassElement toArray() {
      Class<?> arrayType = Array.newInstance(this.type, 0).getClass();
      return ClassElement.of(arrayType);
   }

   @Override
   public ClassElement fromArray() {
      return new ReflectClassElement(this.type.getComponentType());
   }

   @NonNull
   @Override
   public List<? extends GenericPlaceholderElement> getDeclaredGenericPlaceholders() {
      return (List<? extends GenericPlaceholderElement>)Arrays.stream(this.type.getTypeParameters())
         .map(tv -> new ReflectGenericPlaceholderElement(tv, 0))
         .collect(Collectors.toList());
   }
}
