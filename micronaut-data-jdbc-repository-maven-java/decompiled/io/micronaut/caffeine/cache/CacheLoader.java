package io.micronaut.caffeine.cache;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@FunctionalInterface
public interface CacheLoader<K, V> extends AsyncCacheLoader<K, V> {
   @Nullable
   V load(@NonNull K var1) throws Exception;

   @NonNull
   default Map<K, V> loadAll(@NonNull Iterable<? extends K> keys) throws Exception {
      throw new UnsupportedOperationException();
   }

   @NonNull
   @Override
   default CompletableFuture<V> asyncLoad(@NonNull K key, @NonNull Executor executor) {
      Objects.requireNonNull(key);
      Objects.requireNonNull(executor);
      return CompletableFuture.supplyAsync(() -> {
         try {
            return this.load(key);
         } catch (RuntimeException var3) {
            throw var3;
         } catch (Exception var4) {
            throw new CompletionException(var4);
         }
      }, executor);
   }

   @NonNull
   @Override
   default CompletableFuture<Map<K, V>> asyncLoadAll(@NonNull Iterable<? extends K> keys, @NonNull Executor executor) {
      Objects.requireNonNull(keys);
      Objects.requireNonNull(executor);
      return CompletableFuture.supplyAsync(() -> {
         try {
            return this.loadAll(keys);
         } catch (RuntimeException var3) {
            throw var3;
         } catch (Exception var4) {
            throw new CompletionException(var4);
         }
      }, executor);
   }

   @Nullable
   default V reload(@NonNull K key, @NonNull V oldValue) throws Exception {
      return this.load(key);
   }

   @NonNull
   @Override
   default CompletableFuture<V> asyncReload(@NonNull K key, @NonNull V oldValue, @NonNull Executor executor) {
      Objects.requireNonNull(key);
      Objects.requireNonNull(executor);
      return CompletableFuture.supplyAsync(() -> {
         try {
            return this.reload(key, oldValue);
         } catch (RuntimeException var4) {
            throw var4;
         } catch (Exception var5) {
            throw new CompletionException(var5);
         }
      }, executor);
   }
}
