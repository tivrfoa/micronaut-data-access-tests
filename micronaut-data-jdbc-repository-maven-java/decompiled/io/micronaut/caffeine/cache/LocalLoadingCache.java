package io.micronaut.caffeine.cache;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

interface LocalLoadingCache<K, V> extends LocalManualCache<K, V>, LoadingCache<K, V> {
   Logger logger = Logger.getLogger(LocalLoadingCache.class.getName());

   CacheLoader<? super K, V> cacheLoader();

   Function<K, V> mappingFunction();

   @Nullable
   Function<Iterable<? extends K>, Map<K, V>> bulkMappingFunction();

   @Nullable
   @Override
   default V get(K key) {
      return this.cache().computeIfAbsent(key, this.mappingFunction());
   }

   @Override
   default Map<K, V> getAll(Iterable<? extends K> keys) {
      Function<Iterable<? extends K>, Map<K, V>> mappingFunction = this.bulkMappingFunction();
      return mappingFunction == null ? this.loadSequentially(keys) : this.getAll(keys, mappingFunction);
   }

   default Map<K, V> loadSequentially(Iterable<? extends K> keys) {
      Set<K> uniqueKeys = new LinkedHashSet();

      for(K key : keys) {
         uniqueKeys.add(key);
      }

      int count = 0;
      Map<K, V> result = new LinkedHashMap(uniqueKeys.size());

      try {
         for(K key : uniqueKeys) {
            ++count;
            V value = this.get(key);
            if (value != null) {
               result.put(key, value);
            }
         }
      } catch (Throwable var8) {
         this.cache().statsCounter().recordMisses(uniqueKeys.size() - count);
         throw var8;
      }

      return Collections.unmodifiableMap(result);
   }

   @Override
   default void refresh(K key) {
      long[] writeTime = new long[1];
      long startTime = this.cache().statsTicker().read();
      V oldValue = this.cache().getIfPresentQuietly(key, writeTime);
      CompletableFuture<V> refreshFuture = oldValue == null
         ? this.cacheLoader().asyncLoad(key, this.cache().executor())
         : this.cacheLoader().asyncReload(key, oldValue, this.cache().executor());
      refreshFuture.whenComplete((newValue, error) -> {
         long loadTime = this.cache().statsTicker().read() - startTime;
         if (error != null) {
            if (!(error instanceof CancellationException) && !(error instanceof TimeoutException)) {
               logger.log(Level.WARNING, "Exception thrown during refresh", error);
            }

            this.cache().statsCounter().recordLoadFailure(loadTime);
         } else {
            boolean[] discard = new boolean[1];
            this.cache().compute(key, (k, currentValue) -> {
               if (currentValue == null) {
                  return newValue;
               } else {
                  if (currentValue == oldValue) {
                     long expectedWriteTime = writeTime[0];
                     if (this.cache().hasWriteTime()) {
                        this.cache().getIfPresentQuietly(key, writeTime);
                     }

                     if (writeTime[0] == expectedWriteTime) {
                        return newValue;
                     }
                  }

                  discard[0] = true;
                  return currentValue;
               }
            }, false, false, true);
            if (discard[0] && this.cache().hasRemovalListener()) {
               this.cache().notifyRemoval(key, (V)newValue, RemovalCause.REPLACED);
            }

            if (newValue == null) {
               this.cache().statsCounter().recordLoadFailure(loadTime);
            } else {
               this.cache().statsCounter().recordLoadSuccess(loadTime);
            }

         }
      });
   }

   static <K, V> Function<K, V> newMappingFunction(CacheLoader<? super K, V> cacheLoader) {
      return key -> {
         try {
            return cacheLoader.load((K)key);
         } catch (RuntimeException var3) {
            throw var3;
         } catch (InterruptedException var4) {
            Thread.currentThread().interrupt();
            throw new CompletionException(var4);
         } catch (Exception var5) {
            throw new CompletionException(var5);
         }
      };
   }

   @Nullable
   static <K, V> Function<Iterable<? extends K>, Map<K, V>> newBulkMappingFunction(CacheLoader<? super K, V> cacheLoader) {
      return !hasLoadAll(cacheLoader) ? null : keysToLoad -> {
         try {
            return cacheLoader.loadAll(keysToLoad);
         } catch (RuntimeException var3) {
            throw var3;
         } catch (InterruptedException var4) {
            Thread.currentThread().interrupt();
            throw new CompletionException(var4);
         } catch (Exception var5) {
            throw new CompletionException(var5);
         }
      };
   }

   static boolean hasLoadAll(CacheLoader<?, ?> loader) {
      try {
         Method classLoadAll = loader.getClass().getMethod("loadAll", Iterable.class);
         Method defaultLoadAll = CacheLoader.class.getMethod("loadAll", Iterable.class);
         return !classLoadAll.equals(defaultLoadAll);
      } catch (SecurityException | NoSuchMethodException var3) {
         logger.log(Level.WARNING, "Cannot determine if CacheLoader can bulk load", var3);
         return false;
      }
   }
}
