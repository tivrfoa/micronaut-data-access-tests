package io.micronaut.caffeine.cache;

import java.lang.ref.ReferenceQueue;

class SI<K, V> extends BoundedLocalCache<K, V> {
   final ReferenceQueue<V> valueReferenceQueue = new ReferenceQueue();

   SI(Caffeine<K, V> builder, CacheLoader<? super K, V> cacheLoader, boolean async) {
      super(builder, (CacheLoader<K, V>)cacheLoader, async);
   }

   @Override
   protected final ReferenceQueue<V> valueReferenceQueue() {
      return this.valueReferenceQueue;
   }

   @Override
   protected final boolean collectValues() {
      return true;
   }
}
