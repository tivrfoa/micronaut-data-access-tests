package io.micronaut.caffeine.cache;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
public interface AsyncCacheLoader<K, V> {
   @NonNull
   CompletableFuture<V> asyncLoad(@NonNull K var1, @NonNull Executor var2);

   @NonNull
   default CompletableFuture<Map<K, V>> asyncLoadAll(@NonNull Iterable<? extends K> keys, @NonNull Executor executor) {
      throw new UnsupportedOperationException();
   }

   @NonNull
   default CompletableFuture<V> asyncReload(@NonNull K key, @NonNull V oldValue, @NonNull Executor executor) {
      return this.asyncLoad(key, executor);
   }
}
