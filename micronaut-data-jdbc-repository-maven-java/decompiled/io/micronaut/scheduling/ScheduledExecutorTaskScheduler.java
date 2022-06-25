package io.micronaut.scheduling;

import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.scheduling.cron.CronExpression;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.time.ZoneId;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Named("scheduled")
@Primary
@Singleton
public class ScheduledExecutorTaskScheduler implements TaskScheduler {
   private final ScheduledExecutorService executorService;

   public ScheduledExecutorTaskScheduler(@Named("scheduled") ExecutorService executorService) {
      if (!(executorService instanceof ScheduledExecutorService)) {
         throw new IllegalStateException("Cannot schedule tasks on ExecutorService that is not a ScheduledExecutorService: " + executorService);
      } else {
         this.executorService = (ScheduledExecutorService)executorService;
      }
   }

   @Override
   public ScheduledFuture<?> schedule(String cron, Runnable command) {
      return this.schedule(cron, null, command);
   }

   @Override
   public <V> ScheduledFuture<V> schedule(String cron, Callable<V> command) {
      return this.schedule(cron, null, command);
   }

   @Override
   public <V> ScheduledFuture<V> schedule(@NonNull String cron, @Nullable String timezoneId, @NonNull Callable<V> command) {
      if (StringUtils.isEmpty(cron)) {
         throw new IllegalArgumentException("Blank cron expression not allowed");
      } else {
         ArgumentUtils.check("command", command).notNull();
         ZoneId zoneId;
         if (timezoneId != null && !timezoneId.equals("")) {
            try {
               zoneId = ZoneId.of(timezoneId);
            } catch (Exception var6) {
               zoneId = null;
            }
         } else {
            zoneId = ZoneId.systemDefault();
         }

         if (zoneId == null) {
            throw new IllegalArgumentException("Invalid zone id for cron expression");
         } else {
            NextFireTime delaySupplier = new NextFireTime(CronExpression.create(cron), zoneId);
            return new ReschedulingTask<>(command, this, delaySupplier);
         }
      }
   }

   @Override
   public ScheduledFuture<?> schedule(Duration delay, Runnable command) {
      ArgumentUtils.check("delay", delay).notNull();
      ArgumentUtils.check("command", command).notNull();
      return this.executorService.schedule(command, delay.toMillis(), TimeUnit.MILLISECONDS);
   }

   @Override
   public <V> ScheduledFuture<V> schedule(Duration delay, Callable<V> callable) {
      ArgumentUtils.check("delay", delay).notNull();
      ArgumentUtils.check("callable", callable).notNull();
      return this.executorService.schedule(callable, delay.toMillis(), TimeUnit.MILLISECONDS);
   }

   @Override
   public ScheduledFuture<?> scheduleAtFixedRate(Duration initialDelay, Duration period, Runnable command) {
      ArgumentUtils.check("period", period).notNull();
      ArgumentUtils.check("command", command).notNull();
      long initialDelayMillis = initialDelay != null ? initialDelay.toMillis() : 0L;
      return this.executorService.scheduleAtFixedRate(command, initialDelayMillis, period.toMillis(), TimeUnit.MILLISECONDS);
   }

   @Override
   public ScheduledFuture<?> scheduleWithFixedDelay(Duration initialDelay, Duration delay, Runnable command) {
      ArgumentUtils.check("delay", delay).notNull();
      ArgumentUtils.check("command", command).notNull();
      long initialDelayMillis = initialDelay != null ? initialDelay.toMillis() : 0L;
      return this.executorService.scheduleWithFixedDelay(command, initialDelayMillis, delay.toMillis(), TimeUnit.MILLISECONDS);
   }
}
