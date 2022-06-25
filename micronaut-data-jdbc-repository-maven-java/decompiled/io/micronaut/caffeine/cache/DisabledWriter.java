package io.micronaut.caffeine.cache;

import org.checkerframework.checker.nullness.qual.Nullable;

enum DisabledWriter implements CacheWriter<Object, Object> {
   INSTANCE;

   @Override
   public void write(Object key, Object value) {
   }

   @Override
   public void delete(Object key, @Nullable Object value, RemovalCause cause) {
   }
}
