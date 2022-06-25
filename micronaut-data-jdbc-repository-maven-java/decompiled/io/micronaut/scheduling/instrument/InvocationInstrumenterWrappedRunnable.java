package io.micronaut.scheduling.instrument;

import io.micronaut.core.annotation.Internal;

@Internal
final class InvocationInstrumenterWrappedRunnable implements Runnable {
   private final InvocationInstrumenter invocationInstrumenter;
   private final Runnable runnable;

   InvocationInstrumenterWrappedRunnable(InvocationInstrumenter invocationInstrumenter, Runnable runnable) {
      this.invocationInstrumenter = invocationInstrumenter;
      this.runnable = runnable;
   }

   public void run() {
      try (Instrumentation ignore = this.invocationInstrumenter.newInstrumentation().forceCleanup()) {
         this.runnable.run();
      }

   }
}
