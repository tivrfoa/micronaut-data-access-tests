package io.micronaut.core.io.service;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.function.Supplier;

@Internal
final class DefaultServiceDefinition<S> implements ServiceDefinition<S> {
   private final String name;
   @Nullable
   private final Class<S> loadedClass;

   DefaultServiceDefinition(String name, @Nullable Class<S> loadedClass) {
      this.name = name;
      this.loadedClass = loadedClass;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public boolean isPresent() {
      return this.loadedClass != null;
   }

   @Override
   public <X extends Throwable> S orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
      if (this.loadedClass == null) {
         throw (Throwable)exceptionSupplier.get();
      } else {
         try {
            return (S)this.loadedClass.getDeclaredConstructor().newInstance();
         } catch (Throwable var3) {
            throw (Throwable)exceptionSupplier.get();
         }
      }
   }

   @Override
   public S load() {
      return (S)Optional.ofNullable(this.loadedClass).map(aClass -> {
         try {
            return aClass.getDeclaredConstructor().newInstance();
         } catch (Throwable var2) {
            throw new ServiceConfigurationError("Error loading service [" + aClass.getName() + "]: " + var2.getMessage(), var2);
         }
      }).orElseThrow(() -> new ServiceConfigurationError("Call to load() when class '" + this.name + "' is not present"));
   }
}
