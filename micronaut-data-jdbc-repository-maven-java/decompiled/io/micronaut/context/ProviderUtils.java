package io.micronaut.context;

import jakarta.inject.Provider;
import javax.validation.constraints.NotNull;

public class ProviderUtils {
   public static <T> Provider<T> memoized(Provider<T> delegate) {
      return new ProviderUtils.MemoizingProvider<>(delegate);
   }

   private static final class MemoizingProvider<T> implements Provider<T> {
      private Provider<T> actual;
      private Provider<T> delegate = this::initialize;
      private boolean initialized;

      MemoizingProvider(@NotNull Provider<T> actual) {
         this.actual = actual;
      }

      @Override
      public T get() {
         return this.delegate.get();
      }

      private synchronized T initialize() {
         if (!this.initialized) {
            T value = this.actual.get();
            this.delegate = () -> value;
            this.initialized = true;
            this.actual = null;
         }

         return this.delegate.get();
      }

      public String toString() {
         return this.initialized ? "Provider of " + this.delegate.get() : "ProviderUtils.memoized(" + this.actual + ")";
      }
   }
}
