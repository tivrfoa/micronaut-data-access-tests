package io.micronaut.inject.ast;

import io.micronaut.core.annotation.Internal;

@Internal
public interface ArrayableClassElement extends ClassElement {
   @Override
   default ClassElement toArray() {
      return this.withArrayDimensions(this.getArrayDimensions() + 1);
   }

   @Override
   default ClassElement fromArray() {
      if (!this.isArray()) {
         throw new IllegalStateException("Not an array");
      } else {
         return this.withArrayDimensions(this.getArrayDimensions() - 1);
      }
   }

   ClassElement withArrayDimensions(int arrayDimensions);
}
