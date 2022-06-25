package io.micronaut.core.naming;

import io.micronaut.core.annotation.NonNull;

public interface Described {
   @NonNull
   String getDescription();

   @NonNull
   default String getDescription(boolean simple) {
      return this.getDescription();
   }
}
