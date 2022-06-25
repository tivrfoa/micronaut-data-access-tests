package io.micronaut.caffeine.cache;

import java.util.function.Consumer;
import org.checkerframework.checker.nullness.qual.NonNull;

interface Buffer<E> {
   int FULL = 1;
   int SUCCESS = 0;
   int FAILED = -1;

   static <E> Buffer<E> disabled() {
      return DisabledBuffer.INSTANCE;
   }

   int offer(@NonNull E var1);

   void drainTo(@NonNull Consumer<E> var1);

   default int size() {
      return this.writes() - this.reads();
   }

   int reads();

   int writes();
}
