package io.micronaut.caffeine.cache;

final class WSMSAWR<K, V> extends WSMSAW<K, V> {
   volatile long refreshAfterWriteNanos;

   WSMSAWR(Caffeine<K, V> builder, CacheLoader<? super K, V> cacheLoader, boolean async) {
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
