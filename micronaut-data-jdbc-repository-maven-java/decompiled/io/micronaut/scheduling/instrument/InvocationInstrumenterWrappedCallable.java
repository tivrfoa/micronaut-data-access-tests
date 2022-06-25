package io.micronaut.scheduling.instrument;

import io.micronaut.core.annotation.Internal;
import java.util.concurrent.Callable;

@Internal
final class InvocationInstrumenterWrappedCallable<V> implements Callable<V> {
   private final InvocationInstrumenter invocationInstrumenter;
   private final Callable<V> callable;

   InvocationInstrumenterWrappedCallable(InvocationInstrumenter invocationInstrumenter, Callable<V> callable) {
      this.invocationInstrumenter = invocationInstrumenter;
      this.callable = callable;
   }

   public V call() throws Exception {
      Object var3;
      try (Instrumentation ignored = this.invocationInstrumenter.newInstrumentation().forceCleanup()) {
         var3 = this.callable.call();
      }

      return (V)var3;
   }
}
