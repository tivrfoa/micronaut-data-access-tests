package io.micronaut.caffeine.cache;

class SSL<K, V> extends SS<K, V> {
   final RemovalListener<K, V> removalListener;

   SSL(Caffeine<K, V> builder, CacheLoader<? super K, V> cacheLoader, boolean async) {
      super(builder, cacheLoader, async);
      this.removalListener = builder.getRemovalListener(async);
   }

   @Override
   public final RemovalListener<K, V> removalListener() {
      return this.removalListener;
   }

   @Override
   public final boolean hasRemovalListener() {
      return true;
   }
}
