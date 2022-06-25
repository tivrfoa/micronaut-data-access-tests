package io.micronaut.caffeine.cache;

enum DisabledTicker implements Ticker {
   INSTANCE;

   @Override
   public long read() {
      return 0L;
   }
}
