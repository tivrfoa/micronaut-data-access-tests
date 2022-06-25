package io.micronaut.inject.ast;

import io.micronaut.core.annotation.NonNull;

public interface TypedElement extends Element {
   @NonNull
   ClassElement getType();

   @NonNull
   default ClassElement getGenericType() {
      return this.getType();
   }

   default boolean isPrimitive() {
      return false;
   }

   default boolean isArray() {
      return this.getArrayDimensions() != 0;
   }

   default int getArrayDimensions() {
      return 0;
   }
}
