package io.micronaut.caffeine.cache.stats;

enum DisabledStatsCounter implements StatsCounter {
   INSTANCE;

   @Override
   public void recordHits(int count) {
   }

   @Override
   public void recordMisses(int count) {
   }

   @Override
   public void recordLoadSuccess(long loadTime) {
   }

   @Override
   public void recordLoadFailure(long loadTime) {
   }

   @Override
   public void recordEviction() {
   }

   @Override
   public CacheStats snapshot() {
      return CacheStats.empty();
   }

   public String toString() {
      return this.snapshot().toString();
   }
}
