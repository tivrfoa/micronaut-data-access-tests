package reactor.util.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import reactor.util.annotation.Nullable;

final class SpscArrayQueue<T> extends SpscArrayQueueP3<T> implements Queue<T> {
   private static final long serialVersionUID = 494623116936946976L;

   SpscArrayQueue(int capacity) {
      super(Queues.ceilingNextPowerOfTwo(capacity));
   }

   public boolean offer(T e) {
      Objects.requireNonNull(e, "e");
      long pi = this.producerIndex;
      int offset = (int)pi & this.mask;
      if (this.get(offset) != null) {
         return false;
      } else {
         this.lazySet(offset, e);
         PRODUCER_INDEX.lazySet(this, pi + 1L);
         return true;
      }
   }

   @Nullable
   public T poll() {
      long ci = this.consumerIndex;
      int offset = (int)ci & this.mask;
      T v = (T)this.get(offset);
      if (v != null) {
         this.lazySet(offset, null);
         CONSUMER_INDEX.lazySet(this, ci + 1L);
      }

      return v;
   }

   @Nullable
   public T peek() {
      int offset = (int)this.consumerIndex & this.mask;
      return (T)this.get(offset);
   }

   public boolean isEmpty() {
      return this.producerIndex == this.consumerIndex;
   }

   public void clear() {
      while(this.poll() != null && !this.isEmpty()) {
      }

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

   public boolean contains(Object o) {
      throw new UnsupportedOperationException();
   }

   public Iterator<T> iterator() {
      throw new UnsupportedOperationException();
   }

   public Object[] toArray() {
      throw new UnsupportedOperationException();
   }

   public <R> R[] toArray(R[] a) {
      throw new UnsupportedOperationException();
   }

   public boolean remove(Object o) {
      throw new UnsupportedOperationException();
   }

   public boolean containsAll(Collection<?> c) {
      throw new UnsupportedOperationException();
   }

   public boolean addAll(Collection<? extends T> c) {
      throw new UnsupportedOperationException();
   }

   public boolean removeAll(Collection<?> c) {
      throw new UnsupportedOperationException();
   }

   public boolean retainAll(Collection<?> c) {
      throw new UnsupportedOperationException();
   }

   public boolean add(T e) {
      throw new UnsupportedOperationException();
   }

   public T remove() {
      throw new UnsupportedOperationException();
   }

   public T element() {
      throw new UnsupportedOperationException();
   }
}
