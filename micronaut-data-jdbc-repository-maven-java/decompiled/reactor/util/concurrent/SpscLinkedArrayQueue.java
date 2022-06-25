package reactor.util.concurrent;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.BiPredicate;
import reactor.util.annotation.Nullable;

final class SpscLinkedArrayQueue<T> extends AbstractQueue<T> implements BiPredicate<T, T> {
   final int mask;
   volatile long producerIndex;
   static final AtomicLongFieldUpdater<SpscLinkedArrayQueue> PRODUCER_INDEX = AtomicLongFieldUpdater.newUpdater(SpscLinkedArrayQueue.class, "producerIndex");
   AtomicReferenceArray<Object> producerArray;
   volatile long consumerIndex;
   static final AtomicLongFieldUpdater<SpscLinkedArrayQueue> CONSUMER_INDEX = AtomicLongFieldUpdater.newUpdater(SpscLinkedArrayQueue.class, "consumerIndex");
   AtomicReferenceArray<Object> consumerArray;
   static final Object NEXT = new Object();

   SpscLinkedArrayQueue(int linkSize) {
      int c = Queues.ceilingNextPowerOfTwo(Math.max(8, linkSize));
      this.producerArray = this.consumerArray = new AtomicReferenceArray(c + 1);
      this.mask = c - 1;
   }

   public boolean offer(T e) {
      Objects.requireNonNull(e);
      long pi = this.producerIndex;
      AtomicReferenceArray<Object> a = this.producerArray;
      int m = this.mask;
      int offset = (int)(pi + 1L) & m;
      if (a.get(offset) != null) {
         offset = (int)pi & m;
         AtomicReferenceArray<Object> b = new AtomicReferenceArray(m + 2);
         this.producerArray = b;
         b.lazySet(offset, e);
         a.lazySet(m + 1, b);
         a.lazySet(offset, NEXT);
         PRODUCER_INDEX.lazySet(this, pi + 1L);
      } else {
         offset = (int)pi & m;
         a.lazySet(offset, e);
         PRODUCER_INDEX.lazySet(this, pi + 1L);
      }

      return true;
   }

   public boolean test(T first, T second) {
      AtomicReferenceArray<Object> buffer = this.producerArray;
      long p = this.producerIndex;
      int m = this.mask;
      int pi = (int)(p + 2L) & m;
      if (null != buffer.get(pi)) {
         AtomicReferenceArray<Object> newBuffer = new AtomicReferenceArray(m + 2);
         this.producerArray = newBuffer;
         pi = (int)p & m;
         newBuffer.lazySet(pi + 1, second);
         newBuffer.lazySet(pi, first);
         buffer.lazySet(buffer.length() - 1, newBuffer);
         buffer.lazySet(pi, NEXT);
         PRODUCER_INDEX.lazySet(this, p + 2L);
      } else {
         pi = (int)p & m;
         buffer.lazySet(pi + 1, second);
         buffer.lazySet(pi, first);
         PRODUCER_INDEX.lazySet(this, p + 2L);
      }

      return true;
   }

   @Nullable
   public T poll() {
      long ci = this.consumerIndex;
      AtomicReferenceArray<Object> a = this.consumerArray;
      int m = this.mask;
      int offset = (int)ci & m;
      Object o = a.get(offset);
      if (o == null) {
         return null;
      } else {
         if (o == NEXT) {
            AtomicReferenceArray<Object> b = (AtomicReferenceArray)a.get(m + 1);
            a.lazySet(m + 1, null);
            o = b.get(offset);
            a = b;
            this.consumerArray = b;
         }

         a.lazySet(offset, null);
         CONSUMER_INDEX.lazySet(this, ci + 1L);
         return (T)o;
      }
   }

   @Nullable
   public T peek() {
      long ci = this.consumerIndex;
      AtomicReferenceArray<Object> a = this.consumerArray;
      int m = this.mask;
      int offset = (int)ci & m;
      Object o = a.get(offset);
      if (o == null) {
         return null;
      } else {
         if (o == NEXT) {
            a = (AtomicReferenceArray)a.get(m + 1);
            o = a.get(offset);
         }

         return (T)o;
      }
   }

   public boolean isEmpty() {
      return this.producerIndex == this.consumerIndex;
   }

   public int size() {
      long ci = this.consumerIndex;

      while(true) {
         long pi = this.producerIndex;
         long ci2 = this.consumerIndex;
         if (ci == ci2) {
            return (int)(pi - ci);
         }

         ci = ci2;
      }
   }

   public void clear() {
      while(this.poll() != null && !this.isEmpty()) {
      }

   }

   public Iterator<T> iterator() {
      throw new UnsupportedOperationException();
   }
}
