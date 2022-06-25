package io.micronaut.caffeine.cache;

class SSLSA<K, V> extends SSLS<K, V> {
   final Ticker ticker;
   final AccessOrderDeque<Node<K, V>> accessOrderWindowDeque;
   final Expiry<K, V> expiry;
   final TimerWheel<K, V> timerWheel;
   volatile long expiresAfterAccessNanos;
   final Pacer pacer;

   SSLSA(Caffeine<K, V> builder, CacheLoader<? super K, V> cacheLoader, boolean async) {
      super(builder, cacheLoader, async);
      this.ticker = builder.getTicker();
      this.accessOrderWindowDeque = !builder.evicts() && !builder.expiresAfterAccess() ? null : new AccessOrderDeque<>();
      this.expiry = builder.getExpiry(this.isAsync);
      this.timerWheel = builder.expiresVariable() ? new TimerWheel<>(this) : null;
      this.expiresAfterAccessNanos = builder.getExpiresAfterAccessNanos();
      this.pacer = builder.getScheduler() == Scheduler.disabledScheduler() ? null : new Pacer(builder.getScheduler());
   }

   @Override
   public final Ticker expirationTicker() {
      return this.ticker;
   }

   @Override
   protected final AccessOrderDeque<Node<K, V>> accessOrderWindowDeque() {
      return this.accessOrderWindowDeque;
   }

   @Override
   protected final boolean expiresVariable() {
      return this.timerWheel != null;
   }

   @Override
   protected final Expiry<K, V> expiry() {
      return this.expiry;
   }

   @Override
   protected final TimerWheel<K, V> timerWheel() {
      return this.timerWheel;
   }

   @Override
   protected final boolean expiresAfterAccess() {
      return this.timerWheel == null;
   }

   @Override
   protected final long expiresAfterAccessNanos() {
      return this.expiresAfterAccessNanos;
   }

   @Override
   protected final void setExpiresAfterAccessNanos(long expiresAfterAccessNanos) {
      this.expiresAfterAccessNanos = expiresAfterAccessNanos;
   }

   @Override
   public final Pacer pacer() {
      return this.pacer;
   }
}
