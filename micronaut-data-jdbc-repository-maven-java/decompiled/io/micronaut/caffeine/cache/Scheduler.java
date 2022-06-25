package io.micronaut.caffeine.cache;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
public interface Scheduler {
   @NonNull
   Future<?> schedule(@NonNull Executor var1, @NonNull Runnable var2, @Positive long var3, @NonNull TimeUnit var5);

   @NonNull
   static Scheduler disabledScheduler() {
      return DisabledScheduler.INSTANCE;
   }

   @NonNull
   static Scheduler systemScheduler() {
      return (Scheduler)(SystemScheduler.isPresent() ? SystemScheduler.INSTANCE : disabledScheduler());
   }

   @NonNull
   static Scheduler forScheduledExecutorService(@NonNull ScheduledExecutorService scheduledExecutorService) {
      return new ExecutorServiceScheduler(scheduledExecutorService);
   }

   @NonNull
   static Scheduler guardedScheduler(@NonNull Scheduler scheduler) {
      return (Scheduler)(scheduler instanceof GuardedScheduler ? scheduler : new GuardedScheduler(scheduler));
   }
}
