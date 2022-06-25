package io.micronaut.caffeine.cache;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.NoSuchElementException;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class AbstractLinkedDeque<E> extends AbstractCollection<E> implements LinkedDeque<E> {
   @Nullable
   E first;
   @Nullable
   E last;

   void linkFirst(E e) {
      E f = this.first;
      this.first = e;
      if (f == null) {
         this.last = e;
      } else {
         this.setPrevious(f, e);
         this.setNext(e, f);
      }

   }

   void linkLast(E e) {
      E l = this.last;
      this.last = e;
      if (l == null) {
         this.first = e;
      } else {
         this.setNext(l, e);
         this.setPrevious(e, l);
      }

   }

   E unlinkFirst() {
      E f = this.first;
      E next = this.getNext(f);
      this.setNext(f, (E)null);
      this.first = next;
      if (next == null) {
         this.last = null;
      } else {
         this.setPrevious(next, (E)null);
      }

      return f;
   }

   E unlinkLast() {
      E l = this.last;
      E prev = this.getPrevious(l);
      this.setPrevious(l, (E)null);
      this.last = prev;
      if (prev == null) {
         this.first = null;
      } else {
         this.setNext(prev, (E)null);
      }

      return l;
   }

   void unlink(E e) {
      E prev = this.getPrevious(e);
      E next = this.getNext(e);
      if (prev == null) {
         this.first = next;
      } else {
         this.setNext(prev, next);
         this.setPrevious(e, (E)null);
      }

      if (next == null) {
         this.last = prev;
      } else {
         this.setPrevious(next, prev);
         this.setNext(e, (E)null);
      }

   }

   public boolean isEmpty() {
      return this.first == null;
   }

   void checkNotEmpty() {
      if (this.isEmpty()) {
         throw new NoSuchElementException();
      }
   }

   public int size() {
      int size = 0;

      for(E e = this.first; e != null; e = this.getNext(e)) {
         ++size;
      }

      return size;
   }

   public void clear() {
      E next;
      for(E e = this.first; e != null; e = next) {
         next = this.getNext(e);
         this.setPrevious(e, (E)null);
         this.setNext(e, (E)null);
      }

      this.first = this.last = null;
   }

   public abstract boolean contains(Object var1);

   @Override
   public boolean isFirst(E e) {
      return e != null && e == this.first;
   }

   @Override
   public boolean isLast(E e) {
      return e != null && e == this.last;
   }

   @Override
   public void moveToFront(E e) {
      if (e != this.first) {
         this.unlink(e);
         this.linkFirst(e);
      }

   }

   @Override
   public void moveToBack(E e) {
      if (e != this.last) {
         this.unlink(e);
         this.linkLast(e);
      }

   }

   @Nullable
   public E peek() {
      return this.peekFirst();
   }

   @Nullable
   public E peekFirst() {
      return this.first;
   }

   @Nullable
   public E peekLast() {
      return this.last;
   }

   public E getFirst() {
      this.checkNotEmpty();
      return this.peekFirst();
   }

   public E getLast() {
      this.checkNotEmpty();
      return this.peekLast();
   }

   public E element() {
      return this.getFirst();
   }

   public boolean offer(E e) {
      return this.offerLast(e);
   }

   public boolean offerFirst(E e) {
      if (this.contains(e)) {
         return false;
      } else {
         this.linkFirst(e);
         return true;
      }
   }

   public boolean offerLast(E e) {
      if (this.contains(e)) {
         return false;
      } else {
         this.linkLast(e);
         return true;
      }
   }

   public boolean add(E e) {
      return this.offerLast(e);
   }

   public void addFirst(E e) {
      if (!this.offerFirst(e)) {
         throw new IllegalArgumentException();
      }
   }

   public void addLast(E e) {
      if (!this.offerLast(e)) {
         throw new IllegalArgumentException();
      }
   }

   @Nullable
   public E poll() {
      return this.pollFirst();
   }

   @Nullable
   public E pollFirst() {
      return this.isEmpty() ? null : this.unlinkFirst();
   }

   @Nullable
   public E pollLast() {
      return this.isEmpty() ? null : this.unlinkLast();
   }

   public E remove() {
      return this.removeFirst();
   }

   public E removeFirst() {
      this.checkNotEmpty();
      return this.pollFirst();
   }

   public boolean removeFirstOccurrence(Object o) {
      return this.remove(o);
   }

   public E removeLast() {
      this.checkNotEmpty();
      return this.pollLast();
   }

   public boolean removeLastOccurrence(Object o) {
      return this.remove(o);
   }

   public boolean removeAll(Collection<?> c) {
      boolean modified = false;

      for(Object o : c) {
         modified |= this.remove(o);
      }

      return modified;
   }

   public void push(E e) {
      this.addFirst(e);
   }

   public E pop() {
      return this.removeFirst();
   }

   @Override
   public LinkedDeque.PeekingIterator<E> iterator() {
      return new AbstractLinkedDeque<E>.AbstractLinkedIterator(this.first) {
         @Nullable
         @Override
         E computeNext() {
            return (E)AbstractLinkedDeque.this.getNext(this.cursor);
         }
      };
   }

   @Override
   public LinkedDeque.PeekingIterator<E> descendingIterator() {
      return new AbstractLinkedDeque<E>.AbstractLinkedIterator(this.last) {
         @Nullable
         @Override
         E computeNext() {
            return (E)AbstractLinkedDeque.this.getPrevious(this.cursor);
         }
      };
   }

   abstract class AbstractLinkedIterator implements LinkedDeque.PeekingIterator<E> {
      @Nullable
      E previous;
      @Nullable
      E cursor;

      AbstractLinkedIterator(@Nullable E start) {
         this.cursor = start;
      }

      public boolean hasNext() {
         return this.cursor != null;
      }

      @Nullable
      @Override
      public E peek() {
         return this.cursor;
      }

      public E next() {
         if (!this.hasNext()) {
            throw new NoSuchElementException();
         } else {
            this.previous = this.cursor;
            this.cursor = (E)this.computeNext();
            return this.previous;
         }
      }

      @Nullable
      abstract E computeNext();

      public void remove() {
         if (this.previous == null) {
            throw new IllegalStateException();
         } else {
            AbstractLinkedDeque.this.remove(this.previous);
            this.previous = null;
         }
      }
   }
}
