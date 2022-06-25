package io.micronaut.caffeine.cache;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Expiry<K, V> {
   long expireAfterCreate(@NonNull K var1, @NonNull V var2, long var3);

   long expireAfterUpdate(@NonNull K var1, @NonNull V var2, long var3, @NonNegative long var5);

   long expireAfterRead(@NonNull K var1, @NonNull V var2, long var3, @NonNegative long var5);
}
