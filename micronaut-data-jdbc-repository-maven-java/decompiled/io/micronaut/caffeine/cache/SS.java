package io.micronaut.caffeine.cache;

class SS<K, V> extends BoundedLocalCache<K, V> {
   SS(Caffeine<K, V> builder, CacheLoader<? super K, V> cacheLoader, boolean async) {
      super(builder, (CacheLoader<K, V>)cacheLoader, async);
   }
}
