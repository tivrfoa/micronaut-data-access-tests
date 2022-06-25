package reactor.util.concurrent;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import reactor.core.publisher.Hooks;
import reactor.util.annotation.Nullable;

public final class Queues {
   public static final int CAPACITY_UNSURE = Integer.MIN_VALUE;
   public static final int XS_BUFFER_SIZE = Math.max(8, Integer.parseInt(System.getProperty("reactor.bufferSize.x", "32")));
   public static final int SMALL_BUFFER_SIZE = Math.max(16, Integer.parseInt(System.getProperty("reactor.bufferSize.small", "256")));
   static final Supplier ZERO_SUPPLIER = () -> Hooks.wrapQueue(new Queues.ZeroQueue());
   static final Supplier ONE_SUPPLIER = () -> Hooks.wrapQueue(new Queues.OneQueue());
   static final Supplier XS_SUPPLIER = () -> Hooks.wrapQueue(new SpscArrayQueue(XS_BUFFER_SIZE));
   static final Supplier SMALL_SUPPLIER = () -> Hooks.wrapQueue(new SpscArrayQueue(SMALL_BUFFER_SIZE));
   static final Supplier SMALL_UNBOUNDED = () -> Hooks.wrapQueue(new SpscLinkedArrayQueue(SMALL_BUFFER_SIZE));
   static final Supplier XS_UNBOUNDED = () -> Hooks.wrapQueue(new SpscLinkedArrayQueue(XS_BUFFER_SIZE));

   public static final int capacity(Queue q) {
      if (q instanceof Queues.ZeroQueue) {
         return 0;
      } else if (q instanceof Queues.OneQueue) {
         return 1;
      } else if (q instanceof SpscLinkedArrayQueue) {
         return Integer.MAX_VALUE;
      } else if (q instanceof SpscArrayQueue) {
         return ((SpscArrayQueue)q).length();
      } else if (q instanceof MpscLinkedQueue) {
         return Integer.MAX_VALUE;
      } else if (q instanceof BlockingQueue) {
         return ((BlockingQueue)q).remainingCapacity();
      } else {
         return q instanceof ConcurrentLinkedQueue ? Integer.MAX_VALUE : Integer.MIN_VALUE;
      }
   }

   public static int ceilingNextPowerOfTwo(int x) {
      return 1 << 32 - Integer.numberOfLeadingZeros(x - 1);
   }

   public static <T> Supplier<Queue<T>> get(int batchSize) {
      if (batchSize == Integer.MAX_VALUE) {
         return SMALL_UNBOUNDED;
      } else if (batchSize == XS_BUFFER_SIZE) {
         return XS_SUPPLIER;
      } else if (batchSize == SMALL_BUFFER_SIZE) {
         return SMALL_SUPPLIER;
      } else if (batchSize == 1) {
         return ONE_SUPPLIER;
      } else if (batchSize == 0) {
         return ZERO_SUPPLIER;
      } else {
         int adjustedBatchSize = Math.max(8, batchSize);
         return adjustedBatchSize > 10000000 ? SMALL_UNBOUNDED : () -> Hooks.wrapQueue(new SpscArrayQueue<>(adjustedBatchSize));
      }
   }

   public static boolean isPowerOfTwo(int x) {
      return Integer.bitCount(x) == 1;
   }

   public static <T> Supplier<Queue<T>> empty() {
      return ZERO_SUPPLIER;
   }

   public static <T> Supplier<Queue<T>> one() {
      return ONE_SUPPLIER;
   }

   public static <T> Supplier<Queue<T>> small() {
      return SMALL_SUPPLIER;
   }

   public static <T> Supplier<Queue<T>> unbounded() {
      return SMALL_UNBOUNDED;
   }

   public static <T> Supplier<Queue<T>> unbounded(int linkSize) {
      if (linkSize == XS_BUFFER_SIZE) {
         return XS_UNBOUNDED;
      } else {
         return linkSize != Integer.MAX_VALUE && linkSize != SMALL_BUFFER_SIZE ? () -> Hooks.wrapQueue(new SpscLinkedArrayQueue<T>(linkSize)) : unbounded();
      }
   }

   public static <T> Supplier<Queue<T>> xs() {
      return XS_SUPPLIER;
   }

   public static <T> Supplier<Queue<T>> unboundedMultiproducer() {
      return () -> Hooks.wrapQueue(new MpscLinkedQueue<T>());
   }

   private Queues() {
   }

   static final class OneQueue<T> extends AtomicReference<T> implements Queue<T> {
      private static final long serialVersionUID = -6079491923525372331L;

      public boolean add(T t) {
         while(!this.offer(t)) {
         }

         return true;
      }

      public boolean addAll(Collection<? extends T> c) {
         return false;
      }

      public void clear() {
         this.set(null);
      }

      public boolean contains(Object o) {
         return Objects.equals(this.get(), o);
      }

      public boolean containsAll(Collection<?> c) {
         return false;
      }

      public T element() {
         return (T)this.get();
      }

      public boolean isEmpty() {
         return this.get() == null;
      }

      public Iterator<T> iterator() {
         return new Queues.QueueIterator<>(this);
      }

      public boolean offer(T t) {
         if (this.get() != null) {
            return false;
         } else {
            this.lazySet(t);
            return true;
         }
      }

      @Nullable
      public T peek() {
         return (T)this.get();
      }

      @Nullable
      public T poll() {
         T v = (T)this.get();
         if (v != null) {
            this.lazySet(null);
         }

         return v;
      }

      public T remove() {
         return (T)this.getAndSet(null);
      }

      public boolean remove(Object o) {
         return false;
      }

      public boolean removeAll(Collection<?> c) {
         return false;
      }

      public boolean retainAll(Collection<?> c) {
         return false;
      }

      public int size() {
         return this.get() == null ? 0 : 1;
      }

      public Object[] toArray() {
         T t = (T)this.get();
         return t == null ? new Object[0] : new Object[]{t};
      }

      public <T1> T1[] toArray(T1[] a) {
         int size = this.size();
         if (a.length < size) {
            a = (T1[])((Object[])Array.newInstance(a.getClass().getComponentType(), size));
         }

         if (size == 1) {
            a[0] = (T1)this.get();
         }

         if (a.length > size) {
            a[size] = null;
         }

         return a;
      }
   }

   static final class QueueIterator<T> implements Iterator<T> {
      final Queue<T> queue;

      public QueueIterator(Queue<T> queue) {
         this.queue = queue;
      }

      public boolean hasNext() {
         return !this.queue.isEmpty();
      }

      public T next() {
         return (T)this.queue.poll();
      }

      public void remove() {
         this.queue.remove();
      }
   }

   static final class ZeroQueue<T> implements Queue<T>, Serializable {
      private static final long serialVersionUID = -8876883675795156827L;

      public boolean add(T t) {
         return false;
      }

      public boolean addAll(Collection<? extends T> c) {
         return false;
      }

      public void clear() {
      }

      public boolean contains(Object o) {
         return false;
      }

      public boolean containsAll(Collection<?> c) {
         return false;
      }

      public T element() {
         throw new NoSuchElementException("immutable empty queue");
      }

      public boolean isEmpty() {
         return true;
      }

      public Iterator<T> iterator() {
         return Collections.emptyIterator();
      }

      public boolean offer(T t) {
         return false;
      }

      @Nullable
      public T peek() {
         return null;
      }

      @Nullable
      public T poll() {
         return null;
      }

      public T remove() {
         throw new NoSuchElementException("immutable empty queue");
      }

      public boolean remove(Object o) {
         return false;
      }

      public boolean removeAll(Collection<?> c) {
         return false;
      }

      public boolean retainAll(Collection<?> c) {
         return false;
      }

      public int size() {
         return 0;
      }

      public Object[] toArray() {
         return new Object[0];
      }

      public <T1> T1[] toArray(T1[] a) {
         if (a.length > 0) {
            a[0] = null;
         }

         return a;
      }
   }
}
