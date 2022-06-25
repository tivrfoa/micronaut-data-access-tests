package io.micronaut.scheduling.instrument;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.CollectionUtils;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public interface InvocationInstrumenter {
   InvocationInstrumenter NOOP = Instrumentation::noop;

   @NonNull
   Instrumentation newInstrumentation();

   @NonNull
   static InvocationInstrumenter combine(Collection<InvocationInstrumenter> invocationInstrumenters) {
      if (CollectionUtils.isEmpty(invocationInstrumenters)) {
         return NOOP;
      } else {
         return (InvocationInstrumenter)(invocationInstrumenters.size() == 1
            ? (InvocationInstrumenter)invocationInstrumenters.iterator().next()
            : new MultipleInvocationInstrumenter(invocationInstrumenters));
      }
   }

   @NonNull
   static Runnable instrument(@NonNull Runnable runnable, Collection<InvocationInstrumenter> invocationInstrumenters) {
      return CollectionUtils.isEmpty(invocationInstrumenters) ? runnable : instrument(runnable, combine(invocationInstrumenters));
   }

   @NonNull
   static <V> Callable<V> instrument(@NonNull Callable<V> callable, Collection<InvocationInstrumenter> invocationInstrumenters) {
      return CollectionUtils.isEmpty(invocationInstrumenters) ? callable : instrument(callable, combine(invocationInstrumenters));
   }

   @NonNull
   static Runnable instrument(@NonNull Runnable runnable, InvocationInstrumenter invocationInstrumenter) {
      return (Runnable)(runnable instanceof InvocationInstrumenterWrappedRunnable
         ? runnable
         : new InvocationInstrumenterWrappedRunnable(invocationInstrumenter, runnable));
   }

   @NonNull
   static <V> Callable<V> instrument(@NonNull Callable<V> callable, InvocationInstrumenter invocationInstrumenter) {
      return (Callable<V>)(callable instanceof InvocationInstrumenterWrappedCallable
         ? callable
         : new InvocationInstrumenterWrappedCallable<>(invocationInstrumenter, callable));
   }

   static Executor instrument(@NonNull Executor executor, @NonNull InvocationInstrumenter invocationInstrumenter) {
      ArgumentUtils.requireNonNull("executor", executor);
      ArgumentUtils.requireNonNull("invocationInstrumenter", invocationInstrumenter);
      if (executor instanceof ScheduledExecutorService) {
         return new InstrumentedScheduledExecutorService() {
            @Override
            public ScheduledExecutorService getTarget() {
               return (ScheduledExecutorService)executor;
            }

            @Override
            public <T> Callable<T> instrument(Callable<T> callable) {
               return InvocationInstrumenter.instrument(callable, invocationInstrumenter);
            }

            @Override
            public Runnable instrument(Runnable runnable) {
               return InvocationInstrumenter.instrument(runnable, invocationInstrumenter);
            }
         };
      } else {
         return (Executor)(executor instanceof ExecutorService ? new InstrumentedExecutorService() {
            @Override
            public ExecutorService getTarget() {
               return (ExecutorService)executor;
            }

            @Override
            public <T> Callable<T> instrument(Callable<T> callable) {
               return InvocationInstrumenter.instrument(callable, invocationInstrumenter);
            }

            @Override
            public Runnable instrument(Runnable runnable) {
               return InvocationInstrumenter.instrument(runnable, invocationInstrumenter);
            }
         } : new InstrumentedExecutor() {
            @Override
            public Executor getTarget() {
               return executor;
            }

            @Override
            public Runnable instrument(Runnable runnable) {
               return InvocationInstrumenter.instrument(runnable, invocationInstrumenter);
            }
         });
      }
   }
}
