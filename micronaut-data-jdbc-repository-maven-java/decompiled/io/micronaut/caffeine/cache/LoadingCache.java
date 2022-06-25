package io.micronaut.caffeine.cache;

import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface LoadingCache<K, V> extends Cache<K, V> {
   @Nullable
   V get(@NonNull K var1);

   @NonNull
   Map<K, V> getAll(@NonNull Iterable<? extends K> var1);

   void refresh(@NonNull K var1);
}
