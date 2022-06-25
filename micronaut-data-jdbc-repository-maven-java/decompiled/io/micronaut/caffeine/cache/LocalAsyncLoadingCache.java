package io.micronaut.caffeine.cache;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class LocalAsyncLoadingCache<K, V> implements LocalAsyncCache<K, V>, AsyncLoadingCache<K, V> {
   static final Logger logger = Logger.getLogger(LocalAsyncLoadingCache.class.getName());
   final boolean canBulkLoad;
   final AsyncCacheLoader<K, V> loader;
   @Nullable
   LocalAsyncLoadingCache.LoadingCacheView<K, V> cacheView;

   LocalAsyncLoadingCache(AsyncCacheLoader<? super K, V> loader) {
      this.loader = (AsyncCacheLoader<K, V>)loader;
      this.canBulkLoad = canBulkLoad(loader);
   }

   private static boolean canBulkLoad(AsyncCacheLoader<?, ?> loader) {
      try {
         Class<?> defaultLoaderClass = AsyncCacheLoader.class;
         if (loader instanceof CacheLoader) {
            defaultLoaderClass = CacheLoader.class;
            Method classLoadAll = loader.getClass().getMethod("loadAll", Iterable.class);
            Method defaultLoadAll = CacheLoader.class.getMethod("loadAll", Iterable.class);
            if (!classLoadAll.equals(defaultLoadAll)) {
               return true;
            }
         }

         Method classAsyncLoadAll = loader.getClass().getMethod("asyncLoadAll", Iterable.class, Executor.class);
         Method defaultAsyncLoadAll = defaultLoaderClass.getMethod("asyncLoadAll", Iterable.class, Executor.class);
         return !classAsyncLoadAll.equals(defaultAsyncLoadAll);
      } catch (SecurityException | NoSuchMethodException var4) {
         logger.log(Level.WARNING, "Cannot determine if CacheLoader can bulk load", var4);
         return false;
      }
   }

   @Override
   public CompletableFuture<V> get(K key) {
      return this.get(key, this.loader::asyncLoad);
   }

   @Override
   public CompletableFuture<Map<K, V>> getAll(Iterable<? extends K> keys) {
      if (this.canBulkLoad) {
         return this.getAll(keys, this.loader::asyncLoadAll);
      } else {
         Map<K, CompletableFuture<V>> result = new LinkedHashMap();
         Function<K, CompletableFuture<V>> mappingFunction = this::get;

         for(K key : keys) {
            CompletableFuture<V> future = (CompletableFuture)result.computeIfAbsent(key, mappingFunction);
            Objects.requireNonNull(future);
         }

         return this.composeResult(result);
      }
   }

   @Override
   public LoadingCache<K, V> synchronous() {
      return this.cacheView == null ? (this.cacheView = new LocalAsyncLoadingCache.LoadingCacheView<>(this)) : this.cacheView;
   }

   static final class LoadingCacheView<K, V> extends LocalAsyncCache.AbstractCacheView<K, V> implements LoadingCache<K, V> {
      private static final long serialVersionUID = 1L;
      final LocalAsyncLoadingCache<K, V> asyncCache;

      LoadingCacheView(LocalAsyncLoadingCache<K, V> asyncCache) {
         this.asyncCache = (LocalAsyncLoadingCache)Objects.requireNonNull(asyncCache);
      }

      LocalAsyncLoadingCache<K, V> asyncCache() {
         return this.asyncCache;
      }

      @Override
      public V get(K key) {
         return resolve(this.asyncCache.get(key));
      }

      @Override
      public Map<K, V> getAll(Iterable<? extends K> keys) {
         return resolve(this.asyncCache.getAll(keys));
      }

      @Override
      public void refresh(K key) {
         long[] writeTime = new long[1];
         CompletableFuture<V> oldValueFuture = (CompletableFuture)this.asyncCache.cache().getIfPresentQuietly(key, writeTime);
         if (oldValueFuture != null && (!oldValueFuture.isDone() || !oldValueFuture.isCompletedExceptionally())) {
            if (oldValueFuture.isDone()) {
               oldValueFuture.thenAccept(
                  oldValue -> {
                     long now = this.asyncCache.cache().statsTicker().read();
                     CompletableFuture<V> refreshFuture = oldValue == null
                        ? this.asyncCache.loader.asyncLoad(key, this.asyncCache.cache().executor())
                        : this.asyncCache.loader.asyncReload(key, (V)oldValue, this.asyncCache.cache().executor());
                     refreshFuture.whenComplete((newValue, error) -> {
                        long loadTime = this.asyncCache.cache().statsTicker().read() - now;
                        if (error != null) {
                           this.asyncCache.cache().statsCounter().recordLoadFailure(loadTime);
                           if (!(error instanceof CancellationException) && !(error instanceof TimeoutException)) {
                              LocalAsyncLoadingCache.logger.log(Level.WARNING, "Exception thrown during refresh", error);
                           }
   
                        } else {
                           boolean[] discard = new boolean[1];
                           this.asyncCache.cache().compute(key, (k, currentValue) -> {
                              if (currentValue == null) {
                                 return newValue == null ? null : refreshFuture;
                              } else {
                                 if (currentValue == oldValueFuture) {
                                    long expectedWriteTime = writeTime[0];
                                    if (this.asyncCache.cache().hasWriteTime()) {
                                       this.asyncCache.cache().getIfPresentQuietly(key, writeTime);
                                    }
   
                                    if (writeTime[0] == expectedWriteTime) {
                                       return newValue == null ? null : refreshFuture;
                                    }
                                 }
   
                                 discard[0] = true;
                                 return currentValue;
                              }
                           }, false, false, true);
                           if (discard[0] && this.asyncCache.cache().hasRemovalListener()) {
                              this.asyncCache.cache().notifyRemoval(key, (V)refreshFuture, RemovalCause.REPLACED);
                           }
   
                           if (newValue == null) {
                              this.asyncCache.cache().statsCounter().recordLoadFailure(loadTime);
                           } else {
                              this.asyncCache.cache().statsCounter().recordLoadSuccess(loadTime);
                           }
   
                        }
                     });
                  }
               );
            }
         } else {
            this.asyncCache.get(key, this.asyncCache.loader::asyncLoad, false);
         }
      }
   }
}
