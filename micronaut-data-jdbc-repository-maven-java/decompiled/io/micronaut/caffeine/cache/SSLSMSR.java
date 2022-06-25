package io.micronaut.caffeine.cache;

final class SSLSMSR<K, V> extends SSLSMS<K, V> {
   final Ticker ticker;
   volatile long refreshAfterWriteNanos;

   SSLSMSR(Caffeine<K, V> builder, CacheLoader<? super K, V> cacheLoader, boolean async) {
      super(builder, cacheLoader, async);
      this.ticker = builder.getTicker();
      this.refreshAfterWriteNanos = builder.getRefreshAfterWriteNanos();
   }

   @Override
   public Ticker expirationTicker() {
      return this.ticker;
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
