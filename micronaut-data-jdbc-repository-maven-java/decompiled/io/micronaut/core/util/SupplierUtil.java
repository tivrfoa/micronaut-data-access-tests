package io.micronaut.core.util;

import java.util.Optional;
import java.util.function.Supplier;

public class SupplierUtil {
   public static <T> Supplier<T> memoized(Supplier<T> actual) {
      return new Supplier<T>() {
         Supplier<T> delegate = this::initialize;
         boolean initialized;

         public T get() {
            return (T)this.delegate.get();
         }

         private synchronized T initialize() {
            if (!this.initialized) {
               T value = (T)actual.get();
               this.delegate = () -> value;
               this.initialized = true;
            }

            return (T)this.delegate.get();
         }
      };
   }

   public static <T> Supplier<T> memoizedNonEmpty(Supplier<T> actual) {
      return new Supplier<T>() {
         Supplier<T> delegate = this::initialize;
         boolean initialized;

         public T get() {
            return (T)this.delegate.get();
         }

         private synchronized T initialize() {
            if (!this.initialized) {
               T value = (T)actual.get();
               if (value == null) {
                  return null;
               }

               if (value instanceof Optional && !((Optional)value).isPresent()) {
                  return value;
               }

               this.delegate = () -> value;
               this.initialized = true;
            }

            return (T)this.delegate.get();
         }
      };
   }
}
