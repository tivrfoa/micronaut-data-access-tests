package io.micronaut.caffeine.cache;

import java.lang.reflect.Constructor;
import org.checkerframework.checker.nullness.qual.Nullable;

final class LocalCacheFactory {
   public static final String MAXIMUM = "maximum";
   public static final String WINDOW_MAXIMUM = "windowMaximum";
   public static final String MAIN_PROTECTED_MAXIMUM = "mainProtectedMaximum";
   public static final String WEIGHTED_SIZE = "weightedSize";
   public static final String WINDOW_WEIGHTED_SIZE = "windowWeightedSize";
   public static final String MAIN_PROTECTED_WEIGHTED_SIZE = "mainProtectedWeightedSize";
   public static final String KEY = "key";
   public static final String VALUE = "value";
   public static final String ACCESS_TIME = "accessTime";
   public static final String WRITE_TIME = "writeTime";

   private LocalCacheFactory() {
   }

   static <K, V> BoundedLocalCache<K, V> newBoundedLocalCache(Caffeine<K, V> builder, @Nullable CacheLoader<? super K, V> cacheLoader, boolean async) {
      StringBuilder sb = new StringBuilder("io.micronaut.caffeine.cache.");
      if (builder.isStrongKeys()) {
         sb.append('S');
      } else {
         sb.append('W');
      }

      if (builder.isStrongValues()) {
         sb.append('S');
      } else {
         sb.append('I');
      }

      if (builder.removalListener != null) {
         sb.append('L');
      }

      if (builder.isRecordingStats()) {
         sb.append('S');
      }

      if (builder.evicts()) {
         sb.append('M');
         if (builder.isWeighted()) {
            sb.append('W');
         } else {
            sb.append('S');
         }
      }

      if (builder.expiresAfterAccess() || builder.expiresVariable()) {
         sb.append('A');
      }

      if (builder.expiresAfterWrite()) {
         sb.append('W');
      }

      if (builder.refreshAfterWrite()) {
         sb.append('R');
      }

      try {
         Class<?> clazz = Class.forName(sb.toString());
         Constructor<?> ctor = clazz.getDeclaredConstructor(Caffeine.class, CacheLoader.class, Boolean.TYPE);
         return (BoundedLocalCache<K, V>)ctor.newInstance(builder, cacheLoader, async);
      } catch (ReflectiveOperationException var7) {
         throw new IllegalStateException(sb.toString(), var7);
      }
   }
}
