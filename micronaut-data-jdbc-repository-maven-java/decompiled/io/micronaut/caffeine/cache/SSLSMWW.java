package io.micronaut.caffeine.cache;

class SSLSMWW<K, V> extends SSLSMW<K, V> {
   final Ticker ticker;
   final WriteOrderDeque<Node<K, V>> writeOrderDeque;
   volatile long expiresAfterWriteNanos;
   final Pacer pacer;

   SSLSMWW(Caffeine<K, V> builder, CacheLoader<? super K, V> cacheLoader, boolean async) {
      super(builder, cacheLoader, async);
      this.ticker = builder.getTicker();
      this.writeOrderDeque = new WriteOrderDeque<>();
      this.expiresAfterWriteNanos = builder.getExpiresAfterWriteNanos();
      this.pacer = builder.getScheduler() == Scheduler.disabledScheduler() ? null : new Pacer(builder.getScheduler());
   }

   @Override
   public final Ticker expirationTicker() {
      return this.ticker;
   }

   @Override
   protected final WriteOrderDeque<Node<K, V>> writeOrderDeque() {
      return this.writeOrderDeque;
   }

   @Override
   protected final boolean expiresAfterWrite() {
      return true;
   }

   @Override
   protected final long expiresAfterWriteNanos() {
      return this.expiresAfterWriteNanos;
   }

   @Override
   protected final void setExpiresAfterWriteNanos(long expiresAfterWriteNanos) {
      this.expiresAfterWriteNanos = expiresAfterWriteNanos;
   }

   @Override
   public final Pacer pacer() {
      return this.pacer;
   }
}
