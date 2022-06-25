package io.micronaut.scheduling.instrument;

import io.micronaut.core.annotation.NonNull;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface InstrumentedScheduledExecutorService extends InstrumentedExecutorService, ScheduledExecutorService {
   ScheduledExecutorService getTarget();

   default ScheduledFuture<?> schedule(@NonNull Runnable command, long delay, @NonNull TimeUnit unit) {
      return this.getTarget().schedule(this.instrument(command), delay, unit);
   }

   default <V> ScheduledFuture<V> schedule(@NonNull Callable<V> callable, long delay, @NonNull TimeUnit unit) {
      return this.getTarget().schedule(this.instrument(callable), delay, unit);
   }

   default ScheduledFuture<?> scheduleAtFixedRate(@NonNull Runnable command, long initialDelay, long period, @NonNull TimeUnit unit) {
      return this.getTarget().scheduleAtFixedRate(this.instrument(command), initialDelay, period, unit);
   }

   default ScheduledFuture<?> scheduleWithFixedDelay(@NonNull Runnable command, long initialDelay, long delay, @NonNull TimeUnit unit) {
      return this.getTarget().scheduleWithFixedDelay(this.instrument(command), initialDelay, delay, unit);
   }
}
