package io.micronaut.caffeine.cache;

class WSLMSAW<K, V> extends WSLMSA<K, V> {
   final WriteOrderDeque<Node<K, V>> writeOrderDeque = new WriteOrderDeque<>();
   volatile long expiresAfterWriteNanos;

   WSLMSAW(Caffeine<K, V> builder, CacheLoader<? super K, V> cacheLoader, boolean async) {
      super(builder, cacheLoader, async);
      this.expiresAfterWriteNanos = builder.getExpiresAfterWriteNanos();
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
}
