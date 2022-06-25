package io.micronaut.caffeine.cache;

import io.micronaut.caffeine.cache.stats.StatsCounter;

class WIS<K, V> extends WI<K, V> {
   final StatsCounter statsCounter;

   WIS(Caffeine<K, V> builder, CacheLoader<? super K, V> cacheLoader, boolean async) {
      super(builder, cacheLoader, async);
      this.statsCounter = (StatsCounter)builder.getStatsCounterSupplier().get();
   }

   @Override
   public final boolean isRecordingStats() {
      return true;
   }

   @Override
   public final Ticker statsTicker() {
      return Ticker.systemTicker();
   }

   @Override
   public final StatsCounter statsCounter() {
      return this.statsCounter;
   }
}
