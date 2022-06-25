package io.micronaut.inject.ast;

import io.micronaut.core.annotation.NonNull;
import java.util.Optional;

public interface PropertyElement extends TypedElement, MemberElement {
   @NonNull
   @Override
   ClassElement getType();

   default boolean isReadOnly() {
      return !this.getWriteMethod().isPresent();
   }

   default Optional<MethodElement> getWriteMethod() {
      return Optional.empty();
   }

   default Optional<MethodElement> getReadMethod() {
      return Optional.empty();
   }
}
