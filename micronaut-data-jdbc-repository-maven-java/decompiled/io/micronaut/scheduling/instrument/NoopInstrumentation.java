package io.micronaut.scheduling.instrument;

import io.micronaut.core.annotation.NonNull;

enum NoopInstrumentation implements Instrumentation {
   INSTANCE;

   @Override
   public void close(boolean cleanup) {
   }

   @Override
   public void close() {
   }

   @NonNull
   @Override
   public Instrumentation forceCleanup() {
      return this;
   }
}
