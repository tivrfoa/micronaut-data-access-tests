package io.micronaut.caffeine.cache.stats;

import io.micronaut.caffeine.cache.RemovalCause;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

final class GuardedStatsCounter implements StatsCounter {
   static final Logger logger = Logger.getLogger(GuardedStatsCounter.class.getName());
   final StatsCounter delegate;

   GuardedStatsCounter(StatsCounter delegate) {
      this.delegate = (StatsCounter)Objects.requireNonNull(delegate);
   }

   @Override
   public void recordHits(int count) {
      try {
         this.delegate.recordHits(count);
      } catch (Throwable var3) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", var3);
      }

   }

   @Override
   public void recordMisses(int count) {
      try {
         this.delegate.recordMisses(count);
      } catch (Throwable var3) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", var3);
      }

   }

   @Override
   public void recordLoadSuccess(long loadTime) {
      try {
         this.delegate.recordLoadSuccess(loadTime);
      } catch (Throwable var4) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", var4);
      }

   }

   @Override
   public void recordLoadFailure(long loadTime) {
      try {
         this.delegate.recordLoadFailure(loadTime);
      } catch (Throwable var4) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", var4);
      }

   }

   @Override
   public void recordEviction() {
      try {
         this.delegate.recordEviction();
      } catch (Throwable var2) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", var2);
      }

   }

   @Override
   public void recordEviction(int weight) {
      try {
         this.delegate.recordEviction(weight);
      } catch (Throwable var3) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", var3);
      }

   }

   @Override
   public void recordEviction(int weight, RemovalCause cause) {
      try {
         this.delegate.recordEviction(weight, cause);
      } catch (Throwable var4) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", var4);
      }

   }

   @Override
   public CacheStats snapshot() {
      try {
         return this.delegate.snapshot();
      } catch (Throwable var2) {
         logger.log(Level.WARNING, "Exception thrown by stats counter", var2);
         return CacheStats.empty();
      }
   }

   public String toString() {
      return this.delegate.toString();
   }
}
