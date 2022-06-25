package io.micronaut.caffeine.cache;

import java.lang.ref.ReferenceQueue;

class WS<K, V> extends BoundedLocalCache<K, V> {
   final ReferenceQueue<K> keyReferenceQueue = new ReferenceQueue();

   WS(Caffeine<K, V> builder, CacheLoader<? super K, V> cacheLoader, boolean async) {
      super(builder, (CacheLoader<K, V>)cacheLoader, async);
   }

   @Override
   protected final ReferenceQueue<K> keyReferenceQueue() {
      return this.keyReferenceQueue;
   }

   @Override
   protected final boolean collectKeys() {
      return true;
   }
}
