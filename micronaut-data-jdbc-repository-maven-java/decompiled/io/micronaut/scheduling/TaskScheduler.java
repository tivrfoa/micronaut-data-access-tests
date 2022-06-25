package io.micronaut.scheduling;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;

public interface TaskScheduler {
   ScheduledFuture<?> schedule(String cron, Runnable command);

   <V> ScheduledFuture<V> schedule(String cron, Callable<V> command);

   default ScheduledFuture<?> schedule(@NonNull String cron, @Nullable String timezoneId, @NonNull Runnable command) {
      return this.schedule(cron, timezoneId, (Callable)(() -> {
         command.run();
         return null;
      }));
   }

   default <V> ScheduledFuture<V> schedule(@NonNull String cron, @Nullable String timezoneId, @NonNull Callable<V> command) {
      return this.schedule(cron, command);
   }

   ScheduledFuture<?> schedule(Duration delay, Runnable command);

   <V> ScheduledFuture<V> schedule(Duration delay, Callable<V> callable);

   ScheduledFuture<?> scheduleAtFixedRate(@Nullable Duration initialDelay, Duration period, Runnable command);

   ScheduledFuture<?> scheduleWithFixedDelay(@Nullable Duration initialDelay, Duration delay, Runnable command);
}
