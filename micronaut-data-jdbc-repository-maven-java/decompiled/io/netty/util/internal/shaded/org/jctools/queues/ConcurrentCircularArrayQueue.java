package io.netty.util.internal.shaded.org.jctools.queues;

import io.netty.util.internal.shaded.org.jctools.util.Pow2;
import io.netty.util.internal.shaded.org.jctools.util.UnsafeRefArrayAccess;
import java.util.Iterator;
import java.util.NoSuchElementException;

abstract class ConcurrentCircularArrayQueue<E>
   extends ConcurrentCircularArrayQueueL0Pad<E>
   implements MessagePassingQueue<E>,
   IndexedQueueSizeUtil.IndexedQueue,
   QueueProgressIndicators,
   SupportsIterator {
   protected final long mask;
   protected final E[] buffer;

   ConcurrentCircularArrayQueue(int capacity) {
      int actualCapacity = Pow2.roundToPowerOfTwo(capacity);
      this.mask = (long)(actualCapacity - 1);
      this.buffer = (E[])UnsafeRefArrayAccess.allocateRefArray(actualCapacity);
   }

   @Override
   public int size() {
      return IndexedQueueSizeUtil.size(this);
   }

   @Override
   public boolean isEmpty() {
      return IndexedQueueSizeUtil.isEmpty(this);
   }

   public String toString() {
      return this.getClass().getName();
   }

   @Override
   public void clear() {
      while(this.poll() != null) {
      }

   }

   @Override
   public int capacity() {
      return (int)(this.mask + 1L);
   }

   @Override
   public long currentProducerIndex() {
      return this.lvProducerIndex();
   }

   @Override
   public long currentConsumerIndex() {
      return this.lvConsumerIndex();
   }

   public Iterator<E> iterator() {
      long cIndex = this.lvConsumerIndex();
      long pIndex = this.lvProducerIndex();
      return new ConcurrentCircularArrayQueue.WeakIterator<>(cIndex, pIndex, this.mask, this.buffer);
   }

   private static class WeakIterator<E> implements Iterator<E> {
      private final long pIndex;
      private final long mask;
      private final E[] buffer;
      private long nextIndex;
      private E nextElement;

      WeakIterator(long cIndex, long pIndex, long mask, E[] buffer) {
         this.nextIndex = cIndex;
         this.pIndex = pIndex;
         this.mask = mask;
         this.buffer = buffer;
         this.nextElement = this.getNext();
      }

      public void remove() {
         throw new UnsupportedOperationException("remove");
      }

      public boolean hasNext() {
         return this.nextElement != null;
      }

      public E next() {
         E e = this.nextElement;
         if (e == null) {
            throw new NoSuchElementException();
         } else {
            this.nextElement = this.getNext();
            return e;
         }
      }

      private E getNext() {
         while(this.nextIndex < this.pIndex) {
            long offset = UnsafeRefArrayAccess.calcCircularRefElementOffset((long)(this.nextIndex++), this.mask);
            E e = UnsafeRefArrayAccess.lvRefElement(this.buffer, offset);
            if (e != null) {
               return e;
            }
         }

         return null;
      }
   }
}
