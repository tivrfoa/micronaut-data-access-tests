package io.micronaut.caffeine.cache;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface Ticker {
   long read();

   @NonNull
   static Ticker systemTicker() {
      return SystemTicker.INSTANCE;
   }

   @NonNull
   static Ticker disabledTicker() {
      return DisabledTicker.INSTANCE;
   }
}
