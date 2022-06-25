package io.micronaut.caffeine.cache;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@FunctionalInterface
public interface RemovalListener<K, V> {
   void onRemoval(@Nullable K var1, @Nullable V var2, @NonNull RemovalCause var3);
}
