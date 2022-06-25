package io.micronaut.caffeine.cache;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface AsyncLoadingCache<K, V> extends AsyncCache<K, V> {
   @NonNull
   CompletableFuture<V> get(@NonNull K var1);

   @NonNull
   CompletableFuture<Map<K, V>> getAll(@NonNull Iterable<? extends K> var1);

   @NonNull
   @Override
   default ConcurrentMap<K, CompletableFuture<V>> asMap() {
      throw new UnsupportedOperationException();
   }

   @NonNull
   LoadingCache<K, V> synchronous();
}
