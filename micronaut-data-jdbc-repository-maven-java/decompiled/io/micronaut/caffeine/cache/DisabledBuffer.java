package io.micronaut.caffeine.cache;

import java.util.function.Consumer;

enum DisabledBuffer implements Buffer<Object> {
   INSTANCE;

   @Override
   public int offer(Object e) {
      return 0;
   }

   @Override
   public void drainTo(Consumer<Object> consumer) {
   }

   @Override
   public int size() {
      return 0;
   }

   @Override
   public int reads() {
      return 0;
   }

   @Override
   public int writes() {
      return 0;
   }
}
