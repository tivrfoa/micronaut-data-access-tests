package io.micronaut.inject.ast;

import io.micronaut.core.annotation.NonNull;

public interface ConstructorElement extends MethodElement {
   @Override
   default String getName() {
      return "<init>";
   }

   @NonNull
   @Override
   default ClassElement getReturnType() {
      return this.getDeclaringType();
   }
}
