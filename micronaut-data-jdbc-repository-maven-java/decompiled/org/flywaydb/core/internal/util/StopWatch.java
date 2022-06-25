package org.flywaydb.core.internal.util;

import java.util.concurrent.TimeUnit;

public class StopWatch {
   private long start;
   private long stop;

   public void start() {
      this.start = this.nanoTime();
   }

   public void stop() {
      this.stop = this.nanoTime();
   }

   private long nanoTime() {
      return System.nanoTime();
   }

   public long getTotalTimeMillis() {
      long duration = this.stop - this.start;
      return TimeUnit.NANOSECONDS.toMillis(duration);
   }
}
