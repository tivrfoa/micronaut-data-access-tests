package io.micronaut.caffeine.cache;

enum SystemTicker implements Ticker {
   INSTANCE;

   @Override
   public long read() {
      return System.nanoTime();
   }
}
