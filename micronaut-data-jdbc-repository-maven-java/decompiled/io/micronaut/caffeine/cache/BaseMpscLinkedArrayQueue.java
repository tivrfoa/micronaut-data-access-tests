package io.micronaut.caffeine.cache;

import java.lang.reflect.Field;
import java.util.Iterator;

abstract class BaseMpscLinkedArrayQueue<E> extends BaseMpscLinkedArrayQueueColdProducerFields<E> {
   private static final long P_INDEX_OFFSET;
   private static final long C_INDEX_OFFSET;
   private static final long P_LIMIT_OFFSET;
   private static final Object JUMP;

   BaseMpscLinkedArrayQueue(int initialCapacity) {
      if (initialCapacity < 2) {
         throw new IllegalArgumentException("Initial capacity must be 2 or more");
      } else {
         int p2capacity = Caffeine.ceilingPowerOfTwo(initialCapacity);
         long mask = (long)p2capacity - 1L << 1;
         E[] buffer = (E[])allocate(p2capacity + 1);
         this.producerBuffer = buffer;
         this.producerMask = mask;
         this.consumerBuffer = buffer;
         this.consumerMask = mask;
         this.soProducerLimit(mask);
      }
   }

   public final Iterator<E> iterator() {
      throw new UnsupportedOperationException();
   }

   public String toString() {
      return this.getClass().getName() + "@" + Integer.toHexString(this.hashCode());
   }

   public boolean offer(E e) {
      if (null == e) {
         throw new NullPointerException();
      } else {
         while(true) {
            long producerLimit = this.lvProducerLimit();
            long pIndex = this.lvProducerIndex();
            if ((pIndex & 1L) != 1L) {
               long mask = this.producerMask;
               E[] buffer = this.producerBuffer;
               if (producerLimit <= pIndex) {
                  int result = this.offerSlowPath(mask, pIndex, producerLimit);
                  switch(result) {
                     case 0:
                     default:
                        break;
                     case 1:
                        continue;
                     case 2:
                        return false;
                     case 3:
                        this.resize(mask, buffer, pIndex, e);
                        return true;
                  }
               }

               if (this.casProducerIndex(pIndex, pIndex + 2L)) {
                  producerLimit = modifiedCalcElementOffset(pIndex, mask);
                  UnsafeRefArrayAccess.soElement(buffer, producerLimit, e);
                  return true;
               }
            }
         }
      }
   }

   private int offerSlowPath(long mask, long pIndex, long producerLimit) {
      long cIndex = this.lvConsumerIndex();
      long bufferCapacity = this.getCurrentBufferCapacity(mask);
      int result = 0;
      if (cIndex + bufferCapacity > pIndex) {
         if (!this.casProducerLimit(producerLimit, cIndex + bufferCapacity)) {
            result = 1;
         }
      } else if (this.availableInQueue(pIndex, cIndex) <= 0L) {
         result = 2;
      } else if (this.casProducerIndex(pIndex, pIndex + 1L)) {
         result = 3;
      } else {
         result = 1;
      }

      return result;
   }

   protected abstract long availableInQueue(long var1, long var3);

   private static long modifiedCalcElementOffset(long index, long mask) {
      return UnsafeRefArrayAccess.REF_ARRAY_BASE + ((index & mask) << UnsafeRefArrayAccess.REF_ELEMENT_SHIFT - 1);
   }

   public E poll() {
      E[] buffer = this.consumerBuffer;
      long index = this.consumerIndex;
      long mask = this.consumerMask;
      long offset = modifiedCalcElementOffset(index, mask);
      Object e = UnsafeRefArrayAccess.lvElement(buffer, offset);
      if (e == null) {
         if (index == this.lvProducerIndex()) {
            return null;
         }

         do {
            e = UnsafeRefArrayAccess.lvElement(buffer, offset);
         } while(e == null);
      }

      if (e == JUMP) {
         E[] nextBuffer = this.getNextBuffer(buffer, mask);
         return this.newBufferPoll(nextBuffer, index);
      } else {
         UnsafeRefArrayAccess.soElement(buffer, offset, (E)null);
         this.soConsumerIndex(index + 2L);
         return (E)e;
      }
   }

   public E peek() {
      E[] buffer = this.consumerBuffer;
      long index = this.consumerIndex;
      long mask = this.consumerMask;
      long offset = modifiedCalcElementOffset(index, mask);
      Object e = UnsafeRefArrayAccess.lvElement(buffer, offset);
      if (e == null && index != this.lvProducerIndex()) {
         while((e = UnsafeRefArrayAccess.lvElement(buffer, offset)) == null) {
         }
      }

      return (E)(e == JUMP ? this.newBufferPeek(this.getNextBuffer(buffer, mask), index) : e);
   }

   private E[] getNextBuffer(E[] buffer, long mask) {
      long nextArrayOffset = nextArrayOffset(mask);
      E[] nextBuffer = (E[])((Object[])UnsafeRefArrayAccess.lvElement(buffer, nextArrayOffset));
      UnsafeRefArrayAccess.soElement(buffer, nextArrayOffset, (E)null);
      return nextBuffer;
   }

   private static long nextArrayOffset(long mask) {
      return modifiedCalcElementOffset(mask + 2L, Long.MAX_VALUE);
   }

   private E newBufferPoll(E[] nextBuffer, long index) {
      long offsetInNew = this.newBufferAndOffset(nextBuffer, index);
      E n = UnsafeRefArrayAccess.lvElement(nextBuffer, offsetInNew);
      if (n == null) {
         throw new IllegalStateException("new buffer must have at least one element");
      } else {
         UnsafeRefArrayAccess.soElement(nextBuffer, offsetInNew, (E)null);
         this.soConsumerIndex(index + 2L);
         return n;
      }
   }

   private E newBufferPeek(E[] nextBuffer, long index) {
      long offsetInNew = this.newBufferAndOffset(nextBuffer, index);
      E n = UnsafeRefArrayAccess.lvElement(nextBuffer, offsetInNew);
      if (null == n) {
         throw new IllegalStateException("new buffer must have at least one element");
      } else {
         return n;
      }
   }

   private long newBufferAndOffset(E[] nextBuffer, long index) {
      this.consumerBuffer = nextBuffer;
      this.consumerMask = (long)nextBuffer.length - 2L << 1;
      return modifiedCalcElementOffset(index, this.consumerMask);
   }

   public final int size() {
      long after = this.lvConsumerIndex();

      long before;
      long currentProducerIndex;
      do {
         before = after;
         currentProducerIndex = this.lvProducerIndex();
         after = this.lvConsumerIndex();
      } while(before != after);

      long size = currentProducerIndex - after >> 1;
      return size > 2147483647L ? Integer.MAX_VALUE : (int)size;
   }

   public final boolean isEmpty() {
      return this.lvConsumerIndex() == this.lvProducerIndex();
   }

   private long lvProducerIndex() {
      return UnsafeAccess.UNSAFE.getLongVolatile(this, P_INDEX_OFFSET);
   }

   private long lvConsumerIndex() {
      return UnsafeAccess.UNSAFE.getLongVolatile(this, C_INDEX_OFFSET);
   }

   private void soProducerIndex(long v) {
      UnsafeAccess.UNSAFE.putOrderedLong(this, P_INDEX_OFFSET, v);
   }

   private boolean casProducerIndex(long expect, long newValue) {
      return UnsafeAccess.UNSAFE.compareAndSwapLong(this, P_INDEX_OFFSET, expect, newValue);
   }

   private void soConsumerIndex(long v) {
      UnsafeAccess.UNSAFE.putOrderedLong(this, C_INDEX_OFFSET, v);
   }

   private long lvProducerLimit() {
      return this.producerLimit;
   }

   private boolean casProducerLimit(long expect, long newValue) {
      return UnsafeAccess.UNSAFE.compareAndSwapLong(this, P_LIMIT_OFFSET, expect, newValue);
   }

   private void soProducerLimit(long v) {
      UnsafeAccess.UNSAFE.putOrderedLong(this, P_LIMIT_OFFSET, v);
   }

   public long currentProducerIndex() {
      return this.lvProducerIndex() / 2L;
   }

   public long currentConsumerIndex() {
      return this.lvConsumerIndex() / 2L;
   }

   public abstract int capacity();

   public boolean relaxedOffer(E e) {
      return this.offer(e);
   }

   public E relaxedPoll() {
      E[] buffer = this.consumerBuffer;
      long index = this.consumerIndex;
      long mask = this.consumerMask;
      long offset = modifiedCalcElementOffset(index, mask);
      Object e = UnsafeRefArrayAccess.lvElement(buffer, offset);
      if (e == null) {
         return null;
      } else if (e == JUMP) {
         E[] nextBuffer = this.getNextBuffer(buffer, mask);
         return this.newBufferPoll(nextBuffer, index);
      } else {
         UnsafeRefArrayAccess.soElement(buffer, offset, (E)null);
         this.soConsumerIndex(index + 2L);
         return (E)e;
      }
   }

   public E relaxedPeek() {
      E[] buffer = this.consumerBuffer;
      long index = this.consumerIndex;
      long mask = this.consumerMask;
      long offset = modifiedCalcElementOffset(index, mask);
      Object e = UnsafeRefArrayAccess.lvElement(buffer, offset);
      return (E)(e == JUMP ? this.newBufferPeek(this.getNextBuffer(buffer, mask), index) : e);
   }

   private void resize(long oldMask, E[] oldBuffer, long pIndex, E e) {
      int newBufferLength = this.getNextBufferSize(oldBuffer);
      E[] newBuffer = (E[])allocate(newBufferLength);
      this.producerBuffer = newBuffer;
      int newMask = newBufferLength - 2 << 1;
      this.producerMask = (long)newMask;
      long offsetInOld = modifiedCalcElementOffset(pIndex, oldMask);
      long offsetInNew = modifiedCalcElementOffset(pIndex, (long)newMask);
      UnsafeRefArrayAccess.soElement(newBuffer, offsetInNew, e);
      UnsafeRefArrayAccess.soElement(oldBuffer, nextArrayOffset(oldMask), (E)newBuffer);
      long cIndex = this.lvConsumerIndex();
      long availableInQueue = this.availableInQueue(pIndex, cIndex);
      if (availableInQueue <= 0L) {
         throw new IllegalStateException();
      } else {
         this.soProducerLimit(pIndex + Math.min((long)newMask, availableInQueue));
         this.soProducerIndex(pIndex + 2L);
         UnsafeRefArrayAccess.soElement(oldBuffer, offsetInOld, JUMP);
      }
   }

   public static <E> E[] allocate(int capacity) {
      return (E[])(new Object[capacity]);
   }

   protected abstract int getNextBufferSize(E[] var1);

   protected abstract long getCurrentBufferCapacity(long var1);

   static {
      try {
         Field iField = BaseMpscLinkedArrayQueueProducerFields.class.getDeclaredField("producerIndex");
         P_INDEX_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(iField);
      } catch (NoSuchFieldException var3) {
         throw new RuntimeException(var3);
      }

      try {
         Field iField = BaseMpscLinkedArrayQueueConsumerFields.class.getDeclaredField("consumerIndex");
         C_INDEX_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(iField);
      } catch (NoSuchFieldException var2) {
         throw new RuntimeException(var2);
      }

      try {
         Field iField = BaseMpscLinkedArrayQueueColdProducerFields.class.getDeclaredField("producerLimit");
         P_LIMIT_OFFSET = UnsafeAccess.UNSAFE.objectFieldOffset(iField);
      } catch (NoSuchFieldException var1) {
         throw new RuntimeException(var1);
      }

      JUMP = new Object();
   }
}
