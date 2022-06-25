package io.micronaut.caffeine.cache;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.NonNull;

final class GuardedScheduler implements Scheduler, Serializable {
   static final Logger logger = Logger.getLogger(GuardedScheduler.class.getName());
   static final long serialVersionUID = 1L;
   final Scheduler delegate;

   GuardedScheduler(Scheduler delegate) {
      this.delegate = (Scheduler)Objects.requireNonNull(delegate);
   }

   @NonNull
   @Override
   public Future<?> schedule(@NonNull Executor executor, @NonNull Runnable command, long delay, @NonNull TimeUnit unit) {
      try {
         Future<?> future = this.delegate.schedule(executor, command, delay, unit);
         return (Future<?>)(future == null ? DisabledFuture.INSTANCE : future);
      } catch (Throwable var7) {
         logger.log(Level.WARNING, "Exception thrown by scheduler; discarded task", var7);
         return DisabledFuture.INSTANCE;
      }
   }
}
