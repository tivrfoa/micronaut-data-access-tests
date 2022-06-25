package io.micronaut.caffeine.cache;

import io.micronaut.caffeine.cache.stats.StatsCounter;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

interface LocalCache<K, V> extends ConcurrentMap<K, V> {
   boolean isRecordingStats();

   @NonNull
   StatsCounter statsCounter();

   boolean hasRemovalListener();

   RemovalListener<K, V> removalListener();

   void notifyRemoval(@Nullable K var1, @Nullable V var2, RemovalCause var3);

   @NonNull
   Executor executor();

   boolean hasWriteTime();

   @NonNull
   Ticker expirationTicker();

   @NonNull
   Ticker statsTicker();

   long estimatedSize();

   @Nullable
   V getIfPresent(@NonNull Object var1, boolean var2);

   @Nullable
   V getIfPresentQuietly(@NonNull Object var1, long[] var2);

   @NonNull
   Map<K, V> getAllPresent(@NonNull Iterable<?> var1);

   @Nullable
   V put(@NonNull K var1, @NonNull V var2, boolean var3);

   @Nullable
   default V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      return this.compute(key, remappingFunction, false, true, true);
   }

   @Nullable
   V compute(K var1, BiFunction<? super K, ? super V, ? extends V> var2, boolean var3, boolean var4, boolean var5);

   @Nullable
   default V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
      return this.computeIfAbsent(key, mappingFunction, true, true);
   }

   @Nullable
   V computeIfAbsent(K var1, Function<? super K, ? extends V> var2, boolean var3, boolean var4);

   default void invalidateAll(Iterable<?> keys) {
      for(Object key : keys) {
         this.remove(key);
      }

   }

   void cleanUp();

   default <T, R> Function<? super T, ? extends R> statsAware(Function<? super T, ? extends R> mappingFunction, boolean recordLoad) {
      return !this.isRecordingStats() ? mappingFunction : key -> {
         this.statsCounter().recordMisses(1);
         long startTime = this.statsTicker().read();

         R value;
         try {
            value = (R)mappingFunction.apply(key);
         } catch (Error | RuntimeException var9) {
            this.statsCounter().recordLoadFailure(this.statsTicker().read() - startTime);
            throw var9;
         }

         long loadTime = this.statsTicker().read() - startTime;
         if (recordLoad) {
            if (value == null) {
               this.statsCounter().recordLoadFailure(loadTime);
            } else {
               this.statsCounter().recordLoadSuccess(loadTime);
            }
         }

         return value;
      };
   }

   default <T, U, R> BiFunction<? super T, ? super U, ? extends R> statsAware(BiFunction<? super T, ? super U, ? extends R> remappingFunction) {
      return this.statsAware(remappingFunction, true, true, true);
   }

   default <T, U, R> BiFunction<? super T, ? super U, ? extends R> statsAware(
      BiFunction<? super T, ? super U, ? extends R> remappingFunction, boolean recordMiss, boolean recordLoad, boolean recordLoadFailure
   ) {
      return !this.isRecordingStats() ? remappingFunction : (t, u) -> {
         if (u == null && recordMiss) {
            this.statsCounter().recordMisses(1);
         }

         long startTime = this.statsTicker().read();

         R result;
         try {
            result = (R)remappingFunction.apply(t, u);
         } catch (Error | RuntimeException var12) {
            if (recordLoadFailure) {
               this.statsCounter().recordLoadFailure(this.statsTicker().read() - startTime);
            }

            throw var12;
         }

         long loadTime = this.statsTicker().read() - startTime;
         if (recordLoad) {
            if (result == null) {
               this.statsCounter().recordLoadFailure(loadTime);
            } else {
               this.statsCounter().recordLoadSuccess(loadTime);
            }
         }

         return result;
      };
   }
}
