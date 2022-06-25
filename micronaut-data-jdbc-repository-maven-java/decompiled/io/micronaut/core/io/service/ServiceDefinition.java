package io.micronaut.core.io.service;

import java.util.function.Supplier;

public interface ServiceDefinition<T> {
   String getName();

   default boolean isPresent() {
      return false;
   }

   default <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
      try {
         return this.load();
      } catch (Throwable var3) {
         throw (Throwable)exceptionSupplier.get();
      }
   }

   T load();
}
