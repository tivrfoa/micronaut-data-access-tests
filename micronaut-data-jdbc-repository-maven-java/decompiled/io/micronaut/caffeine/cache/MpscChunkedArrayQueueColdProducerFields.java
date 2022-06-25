package io.micronaut.caffeine.cache;

abstract class MpscChunkedArrayQueueColdProducerFields<E> extends BaseMpscLinkedArrayQueue<E> {
   protected final long maxQueueCapacity;

   MpscChunkedArrayQueueColdProducerFields(int initialCapacity, int maxCapacity) {
      super(initialCapacity);
      if (maxCapacity < 4) {
         throw new IllegalArgumentException("Max capacity must be 4 or more");
      } else if (Caffeine.ceilingPowerOfTwo(initialCapacity) >= Caffeine.ceilingPowerOfTwo(maxCapacity)) {
         throw new IllegalArgumentException("Initial capacity cannot exceed maximum capacity(both rounded up to a power of 2)");
      } else {
         this.maxQueueCapacity = (long)Caffeine.ceilingPowerOfTwo(maxCapacity) << 1;
      }
   }
}
