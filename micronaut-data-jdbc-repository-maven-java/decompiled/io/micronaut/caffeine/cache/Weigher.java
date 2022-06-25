package io.micronaut.caffeine.cache;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
public interface Weigher<K, V> {
   @NonNegative
   int weigh(@NonNull K var1, @NonNull V var2);

   @NonNull
   static <K, V> Weigher<K, V> singletonWeigher() {
      Weigher<K, V> self = SingletonWeigher.INSTANCE;
      return self;
   }

   @NonNull
   static <K, V> Weigher<K, V> boundedWeigher(@NonNull Weigher<K, V> delegate) {
      return new BoundedWeigher<>(delegate);
   }
}
