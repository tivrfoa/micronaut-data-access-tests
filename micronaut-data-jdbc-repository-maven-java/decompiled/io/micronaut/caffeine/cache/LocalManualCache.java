package io.micronaut.caffeine.cache;

import io.micronaut.caffeine.cache.stats.CacheStats;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;

interface LocalManualCache<K, V> extends Cache<K, V> {
   LocalCache<K, V> cache();

   @Override
   default long estimatedSize() {
      return this.cache().estimatedSize();
   }

   @Override
   default void cleanUp() {
      this.cache().cleanUp();
   }

   @Nullable
   @Override
   default V getIfPresent(Object key) {
      return this.cache().getIfPresent(key, true);
   }

   @Nullable
   @Override
   default V get(K key, Function<? super K, ? extends V> mappingFunction) {
      return this.cache().computeIfAbsent(key, mappingFunction);
   }

   @Override
   default Map<K, V> getAllPresent(Iterable<?> keys) {
      return this.cache().getAllPresent(keys);
   }

   @Override
   default Map<K, V> getAll(Iterable<? extends K> keys, Function<Iterable<? extends K>, Map<K, V>> mappingFunction) {
      Objects.requireNonNull(mappingFunction);
      Set<K> keysToLoad = new LinkedHashSet();
      Map<K, V> found = this.cache().getAllPresent(keys);
      Map<K, V> result = new LinkedHashMap(found.size());

      for(K key : keys) {
         V value = (V)found.get(key);
         if (value == null) {
            keysToLoad.add(key);
         }

         result.put(key, value);
      }

      if (keysToLoad.isEmpty()) {
         return found;
      } else {
         this.bulkLoad(keysToLoad, result, mappingFunction);
         return Collections.unmodifiableMap(result);
      }
   }

   // $FF: Could not verify finally blocks. A semaphore variable has been added to preserve control flow.
   // Please report this to the Quiltflower issue tracker, at https://github.com/QuiltMC/quiltflower/issues with a copy of the class file (if you have the rights to distribute it!)
   default void bulkLoad(Set<K> keysToLoad, Map<K, V> result, Function<Iterable<? extends K>, Map<K, V>> mappingFunction) {
      boolean success = false;
      long startTime = this.cache().statsTicker().read();
      boolean var17 = false;

      try {
         var17 = true;
         Map<K, V> e = (Map)mappingFunction.apply(keysToLoad);
         e.forEach((keyx, valuex) -> this.cache().put((K)keyx, (V)valuex, false));

         for(K key : keysToLoad) {
            V value = (V)e.get(key);
            if (value == null) {
               result.remove(key);
            } else {
               result.put(key, value);
            }
         }

         success = !e.isEmpty();
         var17 = false;
      } catch (RuntimeException var18) {
         throw var18;
      } catch (Exception var19) {
         throw new CompletionException(var19);
      } finally {
         if (var17) {
            long loadTime = this.cache().statsTicker().read() - startTime;
            if (success) {
               this.cache().statsCounter().recordLoadSuccess(loadTime);
            } else {
               this.cache().statsCounter().recordLoadFailure(loadTime);
            }

         }
      }

      long loadTime = this.cache().statsTicker().read() - startTime;
      if (success) {
         this.cache().statsCounter().recordLoadSuccess(loadTime);
      } else {
         this.cache().statsCounter().recordLoadFailure(loadTime);
      }

   }

   @Override
   default void put(K key, V value) {
      this.cache().put(key, value);
   }

   @Override
   default void putAll(Map<? extends K, ? extends V> map) {
      this.cache().putAll(map);
   }

   @Override
   default void invalidate(Object key) {
      this.cache().remove(key);
   }

   @Override
   default void invalidateAll(Iterable<?> keys) {
      this.cache().invalidateAll(keys);
   }

   @Override
   default void invalidateAll() {
      this.cache().clear();
   }

   @Override
   default CacheStats stats() {
      return this.cache().statsCounter().snapshot();
   }

   @Override
   default ConcurrentMap<K, V> asMap() {
      return this.cache();
   }
}
