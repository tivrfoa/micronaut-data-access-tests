package io.micronaut.caffeine.cache;

final class SILSMWAR<K, V> extends SILSMWA<K, V> {
   volatile long refreshAfterWriteNanos;

   SILSMWAR(Caffeine<K, V> builder, CacheLoader<? super K, V> cacheLoader, boolean async) {
      super(builder, cacheLoader, async);
      this.refreshAfterWriteNanos = builder.getRefreshAfterWriteNanos();
   }

   @Override
   protected boolean refreshAfterWrite() {
      return true;
   }

   @Override
   protected long refreshAfterWriteNanos() {
      return this.refreshAfterWriteNanos;
   }

   @Override
   protected void setRefreshAfterWriteNanos(long refreshAfterWriteNanos) {
      this.refreshAfterWriteNanos = refreshAfterWriteNanos;
   }
}
