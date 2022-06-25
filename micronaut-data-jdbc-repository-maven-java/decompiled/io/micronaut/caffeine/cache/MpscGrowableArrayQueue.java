package io.micronaut.caffeine.cache;

final class MpscGrowableArrayQueue<E> extends MpscChunkedArrayQueue<E> {
   MpscGrowableArrayQueue(int initialCapacity, int maxCapacity) {
      super(initialCapacity, maxCapacity);
   }

   @Override
   protected int getNextBufferSize(E[] buffer) {
      long maxSize = this.maxQueueCapacity / 2L;
      if ((long)buffer.length > maxSize) {
         throw new IllegalStateException();
      } else {
         int newSize = 2 * (buffer.length - 1);
         return newSize + 1;
      }
   }

   @Override
   protected long getCurrentBufferCapacity(long mask) {
      return mask + 2L == this.maxQueueCapacity ? this.maxQueueCapacity : mask;
   }
}
