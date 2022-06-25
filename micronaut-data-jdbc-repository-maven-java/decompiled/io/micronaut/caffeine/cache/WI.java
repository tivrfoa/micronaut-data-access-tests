package io.micronaut.caffeine.cache;

import java.lang.ref.ReferenceQueue;

class WI<K, V> extends BoundedLocalCache<K, V> {
   final ReferenceQueue<K> keyReferenceQueue = new ReferenceQueue();
   final ReferenceQueue<V> valueReferenceQueue = new ReferenceQueue();

   WI(Caffeine<K, V> builder, CacheLoader<? super K, V> cacheLoader, boolean async) {
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

   @Override
   protected final ReferenceQueue<V> valueReferenceQueue() {
      return this.valueReferenceQueue;
   }

   @Override
   protected final boolean collectValues() {
      return true;
   }
}
