package io.micronaut.caffeine.cache;

final class SSLWR<K, V> extends SSLW<K, V> {
   volatile long refreshAfterWriteNanos;

   SSLWR(Caffeine<K, V> builder, CacheLoader<? super K, V> cacheLoader, boolean async) {
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
