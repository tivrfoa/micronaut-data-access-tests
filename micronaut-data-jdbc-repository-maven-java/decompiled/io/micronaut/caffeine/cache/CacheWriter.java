package io.micronaut.caffeine.cache;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Deprecated
public interface CacheWriter<K, V> {
   void write(@NonNull K var1, @NonNull V var2);

   void delete(@NonNull K var1, @Nullable V var2, @NonNull RemovalCause var3);

   @NonNull
   static <K, V> CacheWriter<K, V> disabledWriter() {
      CacheWriter<K, V> writer = DisabledWriter.INSTANCE;
      return writer;
   }
}
