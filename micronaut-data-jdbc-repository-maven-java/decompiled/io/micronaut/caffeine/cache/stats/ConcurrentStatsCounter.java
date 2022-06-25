package io.micronaut.caffeine.cache.stats;

import io.micronaut.caffeine.cache.RemovalCause;
import java.util.concurrent.atomic.LongAdder;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class ConcurrentStatsCounter implements StatsCounter {
   private final LongAdder hitCount = new LongAdder();
   private final LongAdder missCount = new LongAdder();
   private final LongAdder loadSuccessCount = new LongAdder();
   private final LongAdder loadFailureCount = new LongAdder();
   private final LongAdder totalLoadTime = new LongAdder();
   private final LongAdder evictionCount = new LongAdder();
   private final LongAdder evictionWeight = new LongAdder();

   @Override
   public void recordHits(int count) {
      this.hitCount.add((long)count);
   }

   @Override
   public void recordMisses(int count) {
      this.missCount.add((long)count);
   }

   @Override
   public void recordLoadSuccess(long loadTime) {
      this.loadSuccessCount.increment();
      this.totalLoadTime.add(loadTime);
   }

   @Override
   public void recordLoadFailure(long loadTime) {
      this.loadFailureCount.increment();
      this.totalLoadTime.add(loadTime);
   }

   @Deprecated
   @Override
   public void recordEviction() {
      this.evictionCount.increment();
   }

   @Deprecated
   @Override
   public void recordEviction(int weight) {
      this.evictionCount.increment();
      this.evictionWeight.add((long)weight);
   }

   @Override
   public void recordEviction(int weight, RemovalCause cause) {
      this.evictionCount.increment();
      this.evictionWeight.add((long)weight);
   }

   @Override
   public CacheStats snapshot() {
      return CacheStats.of(
         negativeToMaxValue(this.hitCount.sum()),
         negativeToMaxValue(this.missCount.sum()),
         negativeToMaxValue(this.loadSuccessCount.sum()),
         negativeToMaxValue(this.loadFailureCount.sum()),
         negativeToMaxValue(this.totalLoadTime.sum()),
         negativeToMaxValue(this.evictionCount.sum()),
         negativeToMaxValue(this.evictionWeight.sum())
      );
   }

   private static long negativeToMaxValue(long value) {
      return value >= 0L ? value : Long.MAX_VALUE;
   }

   public void incrementBy(@NonNull StatsCounter other) {
      CacheStats otherStats = other.snapshot();
      this.hitCount.add(otherStats.hitCount());
      this.missCount.add(otherStats.missCount());
      this.loadSuccessCount.add(otherStats.loadSuccessCount());
      this.loadFailureCount.add(otherStats.loadFailureCount());
      this.totalLoadTime.add(otherStats.totalLoadTime());
      this.evictionCount.add(otherStats.evictionCount());
      this.evictionWeight.add(otherStats.evictionWeight());
   }

   public String toString() {
      return this.snapshot().toString();
   }
}
