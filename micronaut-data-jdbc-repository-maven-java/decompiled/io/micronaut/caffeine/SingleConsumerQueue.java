package io.micronaut.caffeine;

import io.micronaut.caffeine.base.UnsafeAccess;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Deprecated
public final class SingleConsumerQueue<E> extends SCQHeader.HeadAndTailRef<E> implements Queue<E>, Serializable {
   static final int NCPU = Runtime.getRuntime().availableProcessors();
   static final int ARENA_LENGTH = ceilingPowerOfTwo((NCPU + 1) / 2);
   static final int ARENA_MASK = ARENA_LENGTH - 1;
   static final Function<?, ?> OPTIMISIC = SingleConsumerQueue.Node::new;
   static final int SPINS = NCPU == 1 ? 0 : 2000;
   static final long PROBE = UnsafeAccess.objectFieldOffset(Thread.class, "threadLocalRandomProbe");
   final AtomicReference<SingleConsumerQueue.Node<E>>[] arena = new AtomicReference[ARENA_LENGTH];
   final Function<E, SingleConsumerQueue.Node<E>> factory;
   static final long serialVersionUID = 1L;

   static int ceilingPowerOfTwo(int x) {
      return 1 << -Integer.numberOfLeadingZeros(x - 1);
   }

   private SingleConsumerQueue(Function<E, SingleConsumerQueue.Node<E>> factory) {
      for(int i = 0; i < ARENA_LENGTH; ++i) {
         this.arena[i] = new AtomicReference();
      }

      SingleConsumerQueue.Node<E> node = new SingleConsumerQueue.Node<>((E)null);
      this.factory = factory;
      this.lazySetTail(node);
      this.head = node;
   }

   public static <E> SingleConsumerQueue<E> optimistic() {
      Function<E, SingleConsumerQueue.Node<E>> factory = OPTIMISIC;
      return new SingleConsumerQueue<>(factory);
   }

   public static <E> SingleConsumerQueue<E> linearizable() {
      return new SingleConsumerQueue<>(SingleConsumerQueue.LinearizableNode::new);
   }

   public boolean isEmpty() {
      return this.head == this.tail;
   }

   public int size() {
      SingleConsumerQueue.Node<E> cursor = this.head;
      SingleConsumerQueue.Node<E> t = this.tail;

      int size;
      for(size = 0; cursor != t && size != Integer.MAX_VALUE; ++size) {
         SingleConsumerQueue.Node<E> next = cursor.getNextRelaxed();
         if (next == null) {
            while(true) {
               next = cursor.next;
               if (cursor.next == null) {
                  continue;
               }
            }
         }

         cursor = next;
      }

      return size;
   }

   public boolean contains(Object o) {
      if (o == null) {
         return false;
      } else {
         Iterator<E> it = this.iterator();

         while(it.hasNext()) {
            if (o.equals(it.next())) {
               return true;
            }
         }

         return false;
      }
   }

   public E peek() {
      SingleConsumerQueue.Node<E> h = this.head;
      SingleConsumerQueue.Node<E> t = this.tail;
      if (h == t) {
         return null;
      } else {
         SingleConsumerQueue.Node<E> next = h.getNextRelaxed();
         if (next == null) {
            while(true) {
               next = h.next;
               if (h.next == null) {
                  continue;
               }
            }
         }

         return next.value;
      }
   }

   public boolean offer(E e) {
      Objects.requireNonNull(e);
      SingleConsumerQueue.Node<E> node = (SingleConsumerQueue.Node)this.factory.apply(e);
      this.append(node, node);
      return true;
   }

   public E poll() {
      SingleConsumerQueue.Node<E> h = this.head;
      SingleConsumerQueue.Node<E> next = h.getNextRelaxed();
      if (next == null) {
         if (h == this.tail) {
            return null;
         }

         while(true) {
            next = h.next;
            if (h.next == null) {
               continue;
            }
         }
      }

      E e = next.value;
      next.value = null;
      this.head = next;
      if (this.factory == OPTIMISIC) {
         h.next = null;
      }

      return e;
   }

   public boolean add(E e) {
      return this.offer(e);
   }

   public boolean addAll(Collection<? extends E> c) {
      SingleConsumerQueue.Node<E> first = null;
      SingleConsumerQueue.Node<E> last = null;

      for(E e : c) {
         Objects.requireNonNull(e);
         if (first == null) {
            first = (SingleConsumerQueue.Node)this.factory.apply(e);
            last = first;
         } else {
            SingleConsumerQueue.Node<E> newLast = new SingleConsumerQueue.Node<>(e);
            last.lazySetNext(newLast);
            last = newLast;
         }
      }

      if (first == null) {
         return false;
      } else {
         this.append(first, last);
         return true;
      }
   }

   void append(@NonNull SingleConsumerQueue.Node<E> first, @NonNull SingleConsumerQueue.Node<E> last) {
      while(true) {
         SingleConsumerQueue.Node<E> t = this.tail;
         if (this.casTail(t, last)) {
            t.lazySetNext(first);
            if (this.factory == OPTIMISIC) {
               return;
            }

            while(true) {
               first.complete();
               if (first == last) {
                  return;
               }

               SingleConsumerQueue.Node<E> next = first.getNextRelaxed();
               if (next == null) {
                  return;
               }

               if (next.value == null) {
                  first.next = null;
               }

               first = next;
            }
         }

         SingleConsumerQueue.Node<E> node = this.transferOrCombine(first, last);
         if (node == null) {
            first.await();
            return;
         }

         if (node != first) {
            last = node;
         }
      }
   }

   @Nullable
   SingleConsumerQueue.Node<E> transferOrCombine(@NonNull SingleConsumerQueue.Node<E> first, SingleConsumerQueue.Node<E> last) {
      int index = index();
      AtomicReference<SingleConsumerQueue.Node<E>> slot = this.arena[index];

      while(true) {
         SingleConsumerQueue.Node<E> found = (SingleConsumerQueue.Node)slot.get();
         if (found == null) {
            if (slot.compareAndSet(null, first)) {
               for(int spin = 0; spin < SPINS; ++spin) {
                  if (slot.get() != first) {
                     return null;
                  }
               }

               return slot.compareAndSet(first, null) ? first : null;
            }
         } else if (slot.compareAndSet(found, null)) {
            last.lazySetNext(found);
            last = findLast(found);

            for(int i = 1; i < ARENA_LENGTH; ++i) {
               slot = this.arena[i + index & ARENA_MASK];
               found = (SingleConsumerQueue.Node)slot.get();
               if (found != null && slot.compareAndSet(found, null)) {
                  last.lazySetNext(found);
                  last = findLast(found);
               }
            }

            return last;
         }
      }
   }

   static int index() {
      int probe = UnsafeAccess.UNSAFE.getInt(Thread.currentThread(), PROBE);
      if (probe == 0) {
         ThreadLocalRandom.current();
         probe = UnsafeAccess.UNSAFE.getInt(Thread.currentThread(), PROBE);
      }

      return probe & ARENA_MASK;
   }

   @NonNull
   static <E> SingleConsumerQueue.Node<E> findLast(@NonNull SingleConsumerQueue.Node<E> node) {
      SingleConsumerQueue.Node<E> next;
      while((next = node.getNextRelaxed()) != null) {
         node = next;
      }

      return node;
   }

   public Iterator<E> iterator() {
      return new Iterator<E>() {
         SingleConsumerQueue.Node<E> prev;
         SingleConsumerQueue.Node<E> t = SingleConsumerQueue.this.tail;
         SingleConsumerQueue.Node<E> cursor = SingleConsumerQueue.this.head;
         boolean failOnRemoval = true;

         public boolean hasNext() {
            return this.cursor != this.t;
         }

         public E next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               this.advance();
               this.failOnRemoval = false;
               return this.cursor.value;
            }
         }

         private void advance() {
            if (this.prev == null || !this.failOnRemoval) {
               this.prev = this.cursor;
            }

            this.cursor = this.awaitNext();
         }

         public void remove() {
            if (this.failOnRemoval) {
               throw new IllegalStateException();
            } else {
               this.failOnRemoval = true;
               this.cursor.value = null;
               if (this.t == this.cursor) {
                  this.prev.lazySetNext(null);
                  if (SingleConsumerQueue.this.casTail(this.t, this.prev)) {
                     return;
                  }
               }

               this.prev.lazySetNext(this.awaitNext());
            }
         }

         SingleConsumerQueue.Node<E> awaitNext() {
            if (this.cursor.getNextRelaxed() == null) {
               while(this.cursor.next == null) {
               }
            }

            return this.cursor.getNextRelaxed();
         }
      };
   }

   Object writeReplace() {
      return new SingleConsumerQueue.SerializationProxy<>(this);
   }

   private void readObject(ObjectInputStream stream) throws InvalidObjectException {
      throw new InvalidObjectException("Proxy required");
   }

   static final class LinearizableNode<E> extends SingleConsumerQueue.Node<E> {
      volatile boolean done;

      LinearizableNode(@Nullable E value) {
         super(value);
      }

      @Override
      void complete() {
         this.done = true;
      }

      @Override
      void await() {
         while(!this.done) {
         }

      }

      @Override
      boolean isDone() {
         return this.done;
      }
   }

   static class Node<E> {
      static final long NEXT_OFFSET = UnsafeAccess.objectFieldOffset(SingleConsumerQueue.Node.class, "next");
      @Nullable
      E value;
      @Nullable
      volatile SingleConsumerQueue.Node<E> next;

      Node(@Nullable E value) {
         this.value = value;
      }

      @Nullable
      SingleConsumerQueue.Node<E> getNextRelaxed() {
         return (SingleConsumerQueue.Node<E>)UnsafeAccess.UNSAFE.getObject(this, NEXT_OFFSET);
      }

      void lazySetNext(@Nullable SingleConsumerQueue.Node<E> newNext) {
         UnsafeAccess.UNSAFE.putOrderedObject(this, NEXT_OFFSET, newNext);
      }

      void complete() {
      }

      void await() {
      }

      boolean isDone() {
         return true;
      }

      public String toString() {
         return this.getClass().getSimpleName() + "[" + this.value + "]";
      }
   }

   static final class SerializationProxy<E> implements Serializable {
      final boolean linearizable;
      final List<E> elements;
      static final long serialVersionUID = 1L;

      SerializationProxy(SingleConsumerQueue<E> queue) {
         this.linearizable = queue.factory.apply(null) instanceof SingleConsumerQueue.LinearizableNode;
         this.elements = new ArrayList(queue);
      }

      Object readResolve() {
         SingleConsumerQueue<E> queue = this.linearizable ? SingleConsumerQueue.linearizable() : SingleConsumerQueue.optimistic();
         queue.addAll(this.elements);
         return queue;
      }
   }
}
