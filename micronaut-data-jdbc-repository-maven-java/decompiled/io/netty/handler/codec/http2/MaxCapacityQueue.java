package io.netty.handler.codec.http2;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

final class MaxCapacityQueue<E> implements Queue<E> {
   private final Queue<E> queue;
   private final int maxCapacity;

   MaxCapacityQueue(Queue<E> queue, int maxCapacity) {
      this.queue = queue;
      this.maxCapacity = maxCapacity;
   }

   public boolean add(E element) {
      if (this.offer(element)) {
         return true;
      } else {
         throw new IllegalStateException();
      }
   }

   public boolean offer(E element) {
      return this.maxCapacity <= this.queue.size() ? false : this.queue.offer(element);
   }

   public E remove() {
      return (E)this.queue.remove();
   }

   public E poll() {
      return (E)this.queue.poll();
   }

   public E element() {
      return (E)this.queue.element();
   }

   public E peek() {
      return (E)this.queue.peek();
   }

   public int size() {
      return this.queue.size();
   }

   public boolean isEmpty() {
      return this.queue.isEmpty();
   }

   public boolean contains(Object o) {
      return this.queue.contains(o);
   }

   public Iterator<E> iterator() {
      return this.queue.iterator();
   }

   public Object[] toArray() {
      return this.queue.toArray();
   }

   public <T> T[] toArray(T[] a) {
      return (T[])this.queue.toArray(a);
   }

   public boolean remove(Object o) {
      return this.queue.remove(o);
   }

   public boolean containsAll(Collection<?> c) {
      return this.queue.containsAll(c);
   }

   public boolean addAll(Collection<? extends E> c) {
      if (this.maxCapacity >= this.size() + c.size()) {
         return this.queue.addAll(c);
      } else {
         throw new IllegalStateException();
      }
   }

   public boolean removeAll(Collection<?> c) {
      return this.queue.removeAll(c);
   }

   public boolean retainAll(Collection<?> c) {
      return this.queue.retainAll(c);
   }

   public void clear() {
      this.queue.clear();
   }
}
