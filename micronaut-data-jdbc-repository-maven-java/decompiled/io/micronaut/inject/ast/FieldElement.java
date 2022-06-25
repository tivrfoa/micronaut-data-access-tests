package io.micronaut.inject.ast;

import io.micronaut.core.annotation.NonNull;

public interface FieldElement extends TypedElement, MemberElement {
   default ClassElement getGenericField() {
      return this.getGenericType();
   }

   @NonNull
   @Override
   default String getDescription(boolean simple) {
      return simple ? this.getType().getSimpleName() + " " + this.getName() : this.getType().getName() + " " + this.getName();
   }
}
