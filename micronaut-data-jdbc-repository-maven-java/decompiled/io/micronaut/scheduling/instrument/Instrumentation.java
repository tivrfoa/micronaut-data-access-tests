package io.micronaut.scheduling.instrument;

import io.micronaut.core.annotation.NonNull;

public interface Instrumentation extends AutoCloseable {
   void close(boolean cleanup);

   default void close() {
      this.close(false);
   }

   @NonNull
   default Instrumentation forceCleanup() {
      return new Instrumentation() {
         @Override
         public void close(boolean cleanup) {
            Instrumentation.this.close(true);
         }

         @Override
         public void close() {
            Instrumentation.this.close(true);
         }

         @NonNull
         @Override
         public Instrumentation forceCleanup() {
            return this;
         }
      };
   }

   @NonNull
   static Instrumentation noop() {
      return NoopInstrumentation.INSTANCE;
   }
}
