package io.micronaut.caffeine.cache;

import com.google.errorprone.annotations.CompatibleWith;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface AsyncCache<K, V> {
   @Nullable
   CompletableFuture<V> getIfPresent(@CompatibleWith("K") @NonNull Object var1);

   @NonNull
   CompletableFuture<V> get(@NonNull K var1, @NonNull Function<? super K, ? extends V> var2);

   @NonNull
   CompletableFuture<V> get(@NonNull K var1, @NonNull BiFunction<? super K, Executor, CompletableFuture<V>> var2);

   @NonNull
   default CompletableFuture<Map<K, V>> getAll(@NonNull Iterable<? extends K> keys, @NonNull Function<Iterable<? extends K>, Map<K, V>> mappingFunction) {
      throw new UnsupportedOperationException();
   }

   @NonNull
   default CompletableFuture<Map<K, V>> getAll(
      @NonNull Iterable<? extends K> keys, @NonNull BiFunction<Iterable<? extends K>, Executor, CompletableFuture<Map<K, V>>> mappingFunction
   ) {
      throw new UnsupportedOperationException();
   }

   void put(@NonNull K var1, @NonNull CompletableFuture<V> var2);

   @NonNull
   ConcurrentMap<K, CompletableFuture<V>> asMap();

   @NonNull
   Cache<K, V> synchronous();
}
