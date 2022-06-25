package io.netty.util;

import io.netty.util.concurrent.FastThreadLocal;
import io.netty.util.internal.ObjectPool;
import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.SystemPropertyUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public abstract class Recycler<T> {
   private static final InternalLogger logger = InternalLoggerFactory.getInstance(Recycler.class);
   private static final Recycler.Handle<?> NOOP_HANDLE = new Recycler.Handle<Object>() {
      @Override
      public void recycle(Object object) {
      }

      public String toString() {
         return "NOOP_HANDLE";
      }
   };
   private static final int DEFAULT_INITIAL_MAX_CAPACITY_PER_THREAD = 4096;
   private static final int DEFAULT_MAX_CAPACITY_PER_THREAD;
   private static final int RATIO;
   private static final int DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD;
   private static final boolean BLOCKING_POOL;
   private final int maxCapacityPerThread;
   private final int interval;
   private final int chunkSize;
   private final FastThreadLocal<Recycler.LocalPool<T>> threadLocal = new FastThreadLocal<Recycler.LocalPool<T>>() {
      protected Recycler.LocalPool<T> initialValue() {
         return new Recycler.LocalPool<>(Recycler.this.maxCapacityPerThread, Recycler.this.interval, Recycler.this.chunkSize);
      }

      protected void onRemoval(Recycler.LocalPool<T> value) throws Exception {
         super.onRemoval(value);
         MessagePassingQueue<Recycler.DefaultHandle<T>> handles = value.pooledHandles;
         value.pooledHandles = null;
         handles.clear();
      }
   };

   protected Recycler() {
      this(DEFAULT_MAX_CAPACITY_PER_THREAD);
   }

   protected Recycler(int maxCapacityPerThread) {
      this(maxCapacityPerThread, RATIO, DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD);
   }

   @Deprecated
   protected Recycler(int maxCapacityPerThread, int maxSharedCapacityFactor) {
      this(maxCapacityPerThread, RATIO, DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD);
   }

   @Deprecated
   protected Recycler(int maxCapacityPerThread, int maxSharedCapacityFactor, int ratio, int maxDelayedQueuesPerThread) {
      this(maxCapacityPerThread, ratio, DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD);
   }

   @Deprecated
   protected Recycler(int maxCapacityPerThread, int maxSharedCapacityFactor, int ratio, int maxDelayedQueuesPerThread, int delayedQueueRatio) {
      this(maxCapacityPerThread, ratio, DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD);
   }

   protected Recycler(int maxCapacityPerThread, int ratio, int chunkSize) {
      this.interval = Math.max(0, ratio);
      if (maxCapacityPerThread <= 0) {
         this.maxCapacityPerThread = 0;
         this.chunkSize = 0;
      } else {
         this.maxCapacityPerThread = Math.max(4, maxCapacityPerThread);
         this.chunkSize = Math.max(2, Math.min(chunkSize, this.maxCapacityPerThread >> 1));
      }

   }

   public final T get() {
      if (this.maxCapacityPerThread == 0) {
         return this.newObject(NOOP_HANDLE);
      } else {
         Recycler.LocalPool<T> localPool = this.threadLocal.get();
         Recycler.DefaultHandle<T> handle = localPool.claim();
         T obj;
         if (handle == null) {
            handle = localPool.newHandle();
            if (handle != null) {
               obj = this.newObject(handle);
               handle.set(obj);
            } else {
               obj = this.newObject(NOOP_HANDLE);
            }
         } else {
            obj = handle.get();
         }

         return obj;
      }
   }

   @Deprecated
   public final boolean recycle(T o, Recycler.Handle<T> handle) {
      if (handle == NOOP_HANDLE) {
         return false;
      } else {
         handle.recycle(o);
         return true;
      }
   }

   final int threadLocalSize() {
      return this.threadLocal.get().pooledHandles.size();
   }

   protected abstract T newObject(Recycler.Handle<T> var1);

   static {
      int maxCapacityPerThread = SystemPropertyUtil.getInt(
         "io.netty.recycler.maxCapacityPerThread", SystemPropertyUtil.getInt("io.netty.recycler.maxCapacity", 4096)
      );
      if (maxCapacityPerThread < 0) {
         maxCapacityPerThread = 4096;
      }

      DEFAULT_MAX_CAPACITY_PER_THREAD = maxCapacityPerThread;
      DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD = SystemPropertyUtil.getInt("io.netty.recycler.chunkSize", 32);
      RATIO = Math.max(0, SystemPropertyUtil.getInt("io.netty.recycler.ratio", 8));
      BLOCKING_POOL = SystemPropertyUtil.getBoolean("io.netty.recycler.blocking", false);
      if (logger.isDebugEnabled()) {
         if (DEFAULT_MAX_CAPACITY_PER_THREAD == 0) {
            logger.debug("-Dio.netty.recycler.maxCapacityPerThread: disabled");
            logger.debug("-Dio.netty.recycler.ratio: disabled");
            logger.debug("-Dio.netty.recycler.chunkSize: disabled");
            logger.debug("-Dio.netty.recycler.blocking: disabled");
         } else {
            logger.debug("-Dio.netty.recycler.maxCapacityPerThread: {}", DEFAULT_MAX_CAPACITY_PER_THREAD);
            logger.debug("-Dio.netty.recycler.ratio: {}", RATIO);
            logger.debug("-Dio.netty.recycler.chunkSize: {}", DEFAULT_QUEUE_CHUNK_SIZE_PER_THREAD);
            logger.debug("-Dio.netty.recycler.blocking: {}", BLOCKING_POOL);
         }
      }

   }

   private static final class BlockingMessageQueue<T> implements MessagePassingQueue<T> {
      private final Queue<T> deque;
      private final int maxCapacity;

      BlockingMessageQueue(int maxCapacity) {
         this.maxCapacity = maxCapacity;
         this.deque = new ArrayDeque();
      }

      @Override
      public synchronized boolean offer(T e) {
         return this.deque.size() == this.maxCapacity ? false : this.deque.offer(e);
      }

      @Override
      public synchronized T poll() {
         return (T)this.deque.poll();
      }

      @Override
      public synchronized T peek() {
         return (T)this.deque.peek();
      }

      @Override
      public synchronized int size() {
         return this.deque.size();
      }

      @Override
      public synchronized void clear() {
         this.deque.clear();
      }

      @Override
      public synchronized boolean isEmpty() {
         return this.deque.isEmpty();
      }

      @Override
      public int capacity() {
         return this.maxCapacity;
      }

      @Override
      public boolean relaxedOffer(T e) {
         return this.offer(e);
      }

      @Override
      public T relaxedPoll() {
         return this.poll();
      }

      @Override
      public T relaxedPeek() {
         return this.peek();
      }

      @Override
      public int drain(MessagePassingQueue.Consumer<T> c, int limit) {
         throw new UnsupportedOperationException();
      }

      @Override
      public int fill(MessagePassingQueue.Supplier<T> s, int limit) {
         throw new UnsupportedOperationException();
      }

      @Override
      public int drain(MessagePassingQueue.Consumer<T> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public int fill(MessagePassingQueue.Supplier<T> s) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void drain(MessagePassingQueue.Consumer<T> c, MessagePassingQueue.WaitStrategy wait, MessagePassingQueue.ExitCondition exit) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void fill(MessagePassingQueue.Supplier<T> s, MessagePassingQueue.WaitStrategy wait, MessagePassingQueue.ExitCondition exit) {
         throw new UnsupportedOperationException();
      }
   }

   private static final class DefaultHandle<T> implements Recycler.Handle<T> {
      private static final int STATE_CLAIMED = 0;
      private static final int STATE_AVAILABLE = 1;
      private static final AtomicIntegerFieldUpdater<Recycler.DefaultHandle<?>> STATE_UPDATER;
      private volatile int state;
      private final Recycler.LocalPool<T> localPool;
      private T value;

      DefaultHandle(Recycler.LocalPool<T> localPool) {
         this.localPool = localPool;
      }

      @Override
      public void recycle(Object object) {
         if (object != this.value) {
            throw new IllegalArgumentException("object does not belong to handle");
         } else {
            this.localPool.release(this);
         }
      }

      T get() {
         return this.value;
      }

      void set(T value) {
         this.value = value;
      }

      boolean availableToClaim() {
         return this.state != 1 ? false : STATE_UPDATER.compareAndSet(this, 1, 0);
      }

      void toAvailable() {
         int prev = STATE_UPDATER.getAndSet(this, 1);
         if (prev == 1) {
            throw new IllegalStateException("Object has been recycled already.");
         }
      }

      static {
         AtomicIntegerFieldUpdater<?> updater = AtomicIntegerFieldUpdater.newUpdater(Recycler.DefaultHandle.class, "state");
         STATE_UPDATER = updater;
      }
   }

   public interface Handle<T> extends ObjectPool.Handle<T> {
   }

   private static final class LocalPool<T> {
      private final int ratioInterval;
      private volatile MessagePassingQueue<Recycler.DefaultHandle<T>> pooledHandles;
      private int ratioCounter;

      LocalPool(int maxCapacity, int ratioInterval, int chunkSize) {
         this.ratioInterval = ratioInterval;
         if (Recycler.BLOCKING_POOL) {
            this.pooledHandles = new Recycler.BlockingMessageQueue<>(maxCapacity);
         } else {
            this.pooledHandles = (MessagePassingQueue)PlatformDependent.<Recycler.DefaultHandle<T>>newMpscQueue(chunkSize, maxCapacity);
         }

         this.ratioCounter = ratioInterval;
      }

      Recycler.DefaultHandle<T> claim() {
         MessagePassingQueue<Recycler.DefaultHandle<T>> handles = this.pooledHandles;
         if (handles == null) {
            return null;
         } else {
            Recycler.DefaultHandle<T> handle;
            do {
               handle = handles.relaxedPoll();
            } while(handle != null && !handle.availableToClaim());

            return handle;
         }
      }

      void release(Recycler.DefaultHandle<T> handle) {
         MessagePassingQueue<Recycler.DefaultHandle<T>> handles = this.pooledHandles;
         handle.toAvailable();
         if (handles != null) {
            handles.relaxedOffer(handle);
         }

      }

      Recycler.DefaultHandle<T> newHandle() {
         if (++this.ratioCounter >= this.ratioInterval) {
            this.ratioCounter = 0;
            return new Recycler.DefaultHandle<>(this);
         } else {
            return null;
         }
      }
   }
}
