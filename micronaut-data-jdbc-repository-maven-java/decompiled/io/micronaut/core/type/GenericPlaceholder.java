package io.micronaut.core.type;

import io.micronaut.core.annotation.NonNull;

public interface GenericPlaceholder<T> extends Argument<T> {
   @NonNull
   default String getVariableName() {
      return this.getName();
   }

   @Override
   default boolean isTypeVariable() {
      return true;
   }
}
