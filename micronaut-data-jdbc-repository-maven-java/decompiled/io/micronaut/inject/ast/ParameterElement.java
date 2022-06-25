package io.micronaut.inject.ast;

import io.micronaut.core.annotation.NonNull;
import java.util.Objects;

public interface ParameterElement extends TypedElement {
   @NonNull
   @Override
   ClassElement getType();

   @NonNull
   @Override
   default String getDescription(boolean simple) {
      return simple ? this.getType().getSimpleName() + " " + this.getName() : this.getType().getName() + " " + this.getName();
   }

   @NonNull
   static ParameterElement of(@NonNull Class<?> type, @NonNull String name) {
      return of(ClassElement.of(type), name);
   }

   @NonNull
   static ParameterElement of(@NonNull ClassElement type, @NonNull String name) {
      Objects.requireNonNull(name, "Name cannot be null");
      Objects.requireNonNull(type, "Type cannot be null");
      return new ReflectParameterElement(type, name);
   }
}
