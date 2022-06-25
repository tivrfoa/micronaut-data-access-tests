package io.micronaut.scheduling;

import io.micronaut.core.annotation.Internal;
import io.micronaut.scheduling.cron.CronExpression;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.function.Supplier;

@Internal
final class NextFireTime implements Supplier<Duration> {
   private Duration duration;
   private ZonedDateTime nextFireTime;
   private final CronExpression cron;

   NextFireTime(CronExpression cron) {
      this.cron = cron;
      this.nextFireTime = ZonedDateTime.now();
   }

   NextFireTime(CronExpression cron, ZoneId zoneId) {
      this.cron = cron;
      this.nextFireTime = ZonedDateTime.now(zoneId);
   }

   public Duration get() {
      ZonedDateTime now = ZonedDateTime.now();
      this.computeNextFireTime(now.isAfter(this.nextFireTime) ? now : this.nextFireTime);
      return this.duration;
   }

   private void computeNextFireTime(ZonedDateTime currentFireTime) {
      this.nextFireTime = this.cron.nextTimeAfter(currentFireTime);
      this.duration = Duration.ofMillis(this.nextFireTime.toInstant().toEpochMilli() - ZonedDateTime.now().toInstant().toEpochMilli());
   }
}
