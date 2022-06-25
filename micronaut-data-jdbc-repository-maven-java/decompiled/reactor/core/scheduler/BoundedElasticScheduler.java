package reactor.core.scheduler;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.stream.Stream;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;

final class BoundedElasticScheduler implements Scheduler, Scannable {
   static final Logger LOGGER = Loggers.getLogger(BoundedElasticScheduler.class);
   static final int DEFAULT_TTL_SECONDS = 60;
   static final AtomicLong EVICTOR_COUNTER = new AtomicLong();
   static final ThreadFactory EVICTOR_FACTORY = r -> {
      Thread t = new Thread(r, "boundedElastic-evictor-" + EVICTOR_COUNTER.incrementAndGet());
      t.setDaemon(true);
      return t;
   };
   static final BoundedElasticScheduler.BoundedServices SHUTDOWN = new BoundedElasticScheduler.BoundedServices();
   static final BoundedElasticScheduler.BoundedState CREATING;
   final int maxThreads;
   final int maxTaskQueuedPerThread;
   final Clock clock;
   final ThreadFactory factory;
   final long ttlMillis;
   volatile BoundedElasticScheduler.BoundedServices boundedServices;
   static final AtomicReferenceFieldUpdater<BoundedElasticScheduler, BoundedElasticScheduler.BoundedServices> BOUNDED_SERVICES = AtomicReferenceFieldUpdater.newUpdater(
      BoundedElasticScheduler.class, BoundedElasticScheduler.BoundedServices.class, "boundedServices"
   );
   volatile ScheduledExecutorService evictor;
   static final AtomicReferenceFieldUpdater<BoundedElasticScheduler, ScheduledExecutorService> EVICTOR = AtomicReferenceFieldUpdater.newUpdater(
      BoundedElasticScheduler.class, ScheduledExecutorService.class, "evictor"
   );

   BoundedElasticScheduler(int maxThreads, int maxTaskQueuedPerThread, ThreadFactory threadFactory, long ttlMillis, Clock clock) {
      if (ttlMillis <= 0L) {
         throw new IllegalArgumentException("TTL must be strictly positive, was " + ttlMillis + "ms");
      } else if (maxThreads <= 0) {
         throw new IllegalArgumentException("maxThreads must be strictly positive, was " + maxThreads);
      } else if (maxTaskQueuedPerThread <= 0) {
         throw new IllegalArgumentException("maxTaskQueuedPerThread must be strictly positive, was " + maxTaskQueuedPerThread);
      } else {
         this.maxThreads = maxThreads;
         this.maxTaskQueuedPerThread = maxTaskQueuedPerThread;
         this.factory = threadFactory;
         this.clock = (Clock)Objects.requireNonNull(clock, "A Clock must be provided");
         this.ttlMillis = ttlMillis;
         this.boundedServices = SHUTDOWN;
      }
   }

   BoundedElasticScheduler(int maxThreads, int maxTaskQueuedPerThread, ThreadFactory factory, int ttlSeconds) {
      this(maxThreads, maxTaskQueuedPerThread, factory, (long)ttlSeconds * 1000L, Clock.tickSeconds(BoundedElasticScheduler.BoundedServices.ZONE_UTC));
   }

   BoundedElasticScheduler.BoundedScheduledExecutorService createBoundedExecutorService() {
      return new BoundedElasticScheduler.BoundedScheduledExecutorService(this.maxTaskQueuedPerThread, this.factory);
   }

   @Override
   public boolean isDisposed() {
      return BOUNDED_SERVICES.get(this) == SHUTDOWN;
   }

   @Override
   public void start() {
      BoundedElasticScheduler.BoundedServices services;
      BoundedElasticScheduler.BoundedServices newServices;
      do {
         services = (BoundedElasticScheduler.BoundedServices)BOUNDED_SERVICES.get(this);
         if (services != SHUTDOWN) {
            return;
         }

         newServices = new BoundedElasticScheduler.BoundedServices(this);
      } while(!BOUNDED_SERVICES.compareAndSet(this, services, newServices));

      ScheduledExecutorService e = Executors.newScheduledThreadPool(1, EVICTOR_FACTORY);
      if (EVICTOR.compareAndSet(this, null, e)) {
         try {
            e.scheduleAtFixedRate(newServices::eviction, this.ttlMillis, this.ttlMillis, TimeUnit.MILLISECONDS);
         } catch (RejectedExecutionException var5) {
            if (!this.isDisposed()) {
               throw var5;
            }
         }
      } else {
         e.shutdownNow();
      }

   }

   @Override
   public void dispose() {
      BoundedElasticScheduler.BoundedServices services = (BoundedElasticScheduler.BoundedServices)BOUNDED_SERVICES.get(this);
      if (services != SHUTDOWN && BOUNDED_SERVICES.compareAndSet(this, services, SHUTDOWN)) {
         ScheduledExecutorService e = (ScheduledExecutorService)EVICTOR.getAndSet(this, null);
         if (e != null) {
            e.shutdownNow();
         }

         services.dispose();
      }

   }

   @Override
   public Disposable schedule(Runnable task) {
      BoundedElasticScheduler.BoundedState picked = ((BoundedElasticScheduler.BoundedServices)BOUNDED_SERVICES.get(this)).pick();
      return Schedulers.directSchedule(picked.executor, task, picked, 0L, TimeUnit.MILLISECONDS);
   }

   @Override
   public Disposable schedule(Runnable task, long delay, TimeUnit unit) {
      BoundedElasticScheduler.BoundedState picked = ((BoundedElasticScheduler.BoundedServices)BOUNDED_SERVICES.get(this)).pick();
      return Schedulers.directSchedule(picked.executor, task, picked, delay, unit);
   }

   @Override
   public Disposable schedulePeriodically(Runnable task, long initialDelay, long period, TimeUnit unit) {
      BoundedElasticScheduler.BoundedState picked = ((BoundedElasticScheduler.BoundedServices)BOUNDED_SERVICES.get(this)).pick();
      Disposable scheduledTask = Schedulers.directSchedulePeriodically(picked.executor, task, initialDelay, period, unit);
      return Disposables.composite(scheduledTask, picked);
   }

   public String toString() {
      StringBuilder ts = new StringBuilder("boundedElastic").append('(');
      if (this.factory instanceof ReactorThreadFactory) {
         ts.append('"').append(((ReactorThreadFactory)this.factory).get()).append("\",");
      }

      ts.append("maxThreads=")
         .append(this.maxThreads)
         .append(",maxTaskQueuedPerThread=")
         .append(this.maxTaskQueuedPerThread == Integer.MAX_VALUE ? "unbounded" : this.maxTaskQueuedPerThread)
         .append(",ttl=");
      if (this.ttlMillis < 1000L) {
         ts.append(this.ttlMillis).append("ms)");
      } else {
         ts.append(this.ttlMillis / 1000L).append("s)");
      }

      return ts.toString();
   }

   int estimateSize() {
      return ((BoundedElasticScheduler.BoundedServices)BOUNDED_SERVICES.get(this)).get();
   }

   int estimateBusy() {
      return ((BoundedElasticScheduler.BoundedServices)BOUNDED_SERVICES.get(this)).busyArray.length;
   }

   int estimateIdle() {
      return ((BoundedElasticScheduler.BoundedServices)BOUNDED_SERVICES.get(this)).idleQueue.size();
   }

   int estimateRemainingTaskCapacity() {
      BoundedElasticScheduler.BoundedState[] busyArray = ((BoundedElasticScheduler.BoundedServices)BOUNDED_SERVICES.get(this)).busyArray;
      int totalTaskCapacity = this.maxTaskQueuedPerThread * this.maxThreads;

      for(BoundedElasticScheduler.BoundedState state : busyArray) {
         int stateQueueSize = state.estimateQueueSize();
         if (stateQueueSize < 0) {
            return -1;
         }

         totalTaskCapacity -= stateQueueSize;
      }

      return totalTaskCapacity;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.TERMINATED || key == Scannable.Attr.CANCELLED) {
         return this.isDisposed();
      } else if (key == Scannable.Attr.BUFFERED) {
         return this.estimateSize();
      } else if (key == Scannable.Attr.CAPACITY) {
         return this.maxThreads;
      } else {
         return key == Scannable.Attr.NAME ? this.toString() : null;
      }
   }

   @Override
   public Stream<? extends Scannable> inners() {
      BoundedElasticScheduler.BoundedServices services = (BoundedElasticScheduler.BoundedServices)BOUNDED_SERVICES.get(this);
      return Stream.concat(Stream.of(services.busyArray), services.idleQueue.stream()).filter(obj -> obj != null && obj != CREATING);
   }

   @Override
   public Scheduler.Worker createWorker() {
      BoundedElasticScheduler.BoundedState picked = ((BoundedElasticScheduler.BoundedServices)BOUNDED_SERVICES.get(this)).pick();
      ExecutorServiceWorker worker = new ExecutorServiceWorker(picked.executor);
      worker.disposables.add(picked);
      return worker;
   }

   static {
      SHUTDOWN.dispose();
      ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
      s.shutdownNow();
      CREATING = new BoundedElasticScheduler.BoundedState(SHUTDOWN, s) {
         @Override
         public String toString() {
            return "CREATING BoundedState";
         }
      };
      CREATING.markCount = -1;
      CREATING.idleSinceTimestamp = -1L;
   }

   static final class BoundedScheduledExecutorService extends ScheduledThreadPoolExecutor implements Scannable {
      final int queueCapacity;

      BoundedScheduledExecutorService(int queueCapacity, ThreadFactory factory) {
         super(1, factory);
         this.setMaximumPoolSize(1);
         this.setRemoveOnCancelPolicy(true);
         if (queueCapacity < 1) {
            throw new IllegalArgumentException("was expecting a non-zero positive queue capacity");
         } else {
            this.queueCapacity = queueCapacity;
         }
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (Scannable.Attr.TERMINATED == key) {
            return this.isTerminated();
         } else if (Scannable.Attr.BUFFERED == key) {
            return this.getQueue().size();
         } else {
            return Scannable.Attr.CAPACITY == key ? this.queueCapacity : null;
         }
      }

      public String toString() {
         int queued = this.getQueue().size();
         long completed = this.getCompletedTaskCount();
         String state = this.getActiveCount() > 0 ? "ACTIVE" : "IDLE";
         return this.queueCapacity == Integer.MAX_VALUE
            ? "BoundedScheduledExecutorService{" + state + ", queued=" + queued + "/unbounded, completed=" + completed + '}'
            : "BoundedScheduledExecutorService{" + state + ", queued=" + queued + "/" + this.queueCapacity + ", completed=" + completed + '}';
      }

      void ensureQueueCapacity(int taskCount) {
         if (this.queueCapacity != Integer.MAX_VALUE) {
            int queueSize = super.getQueue().size();
            if (queueSize + taskCount > this.queueCapacity) {
               throw Exceptions.failWithRejected(
                  "Task capacity of bounded elastic scheduler reached while scheduling "
                     + taskCount
                     + " tasks ("
                     + (queueSize + taskCount)
                     + "/"
                     + this.queueCapacity
                     + ")"
               );
            }
         }
      }

      public synchronized ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
         this.ensureQueueCapacity(1);
         return super.schedule(command, delay, unit);
      }

      public synchronized <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
         this.ensureQueueCapacity(1);
         return super.schedule(callable, delay, unit);
      }

      public synchronized ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
         this.ensureQueueCapacity(1);
         return super.scheduleAtFixedRate(command, initialDelay, period, unit);
      }

      public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
         this.ensureQueueCapacity(1);
         return super.scheduleWithFixedDelay(command, initialDelay, delay, unit);
      }

      public void shutdown() {
         super.shutdown();
      }

      public List<Runnable> shutdownNow() {
         return super.shutdownNow();
      }

      public boolean isShutdown() {
         return super.isShutdown();
      }

      public boolean isTerminated() {
         return super.isTerminated();
      }

      public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
         return super.awaitTermination(timeout, unit);
      }

      public synchronized <T> Future<T> submit(Callable<T> task) {
         this.ensureQueueCapacity(1);
         return super.submit(task);
      }

      public synchronized <T> Future<T> submit(Runnable task, T result) {
         this.ensureQueueCapacity(1);
         return super.submit(task, result);
      }

      public synchronized Future<?> submit(Runnable task) {
         this.ensureQueueCapacity(1);
         return super.submit(task);
      }

      public synchronized <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
         this.ensureQueueCapacity(tasks.size());
         return super.invokeAll(tasks);
      }

      public synchronized <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
         this.ensureQueueCapacity(tasks.size());
         return super.invokeAll(tasks, timeout, unit);
      }

      public synchronized <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
         this.ensureQueueCapacity(tasks.size());
         return (T)super.invokeAny(tasks);
      }

      public synchronized <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
         this.ensureQueueCapacity(tasks.size());
         return (T)super.invokeAny(tasks, timeout, unit);
      }

      public synchronized void execute(Runnable command) {
         this.ensureQueueCapacity(1);
         super.submit(command);
      }
   }

   static final class BoundedServices extends AtomicInteger implements Disposable {
      static final int DISPOSED = -1;
      static final ZoneId ZONE_UTC = ZoneId.of("UTC");
      final BoundedElasticScheduler parent;
      final Clock clock;
      final Deque<BoundedElasticScheduler.BoundedState> idleQueue;
      volatile BoundedElasticScheduler.BoundedState[] busyArray;
      static final AtomicReferenceFieldUpdater<BoundedElasticScheduler.BoundedServices, BoundedElasticScheduler.BoundedState[]> BUSY_ARRAY = AtomicReferenceFieldUpdater.newUpdater(
         BoundedElasticScheduler.BoundedServices.class, BoundedElasticScheduler.BoundedState[].class, "busyArray"
      );
      static final BoundedElasticScheduler.BoundedState[] ALL_IDLE = new BoundedElasticScheduler.BoundedState[0];
      static final BoundedElasticScheduler.BoundedState[] ALL_SHUTDOWN = new BoundedElasticScheduler.BoundedState[0];

      private BoundedServices() {
         this.parent = null;
         this.clock = Clock.fixed(Instant.EPOCH, ZONE_UTC);
         this.idleQueue = new ConcurrentLinkedDeque();
         this.busyArray = ALL_SHUTDOWN;
      }

      BoundedServices(BoundedElasticScheduler parent) {
         this.parent = parent;
         this.clock = parent.clock;
         this.idleQueue = new ConcurrentLinkedDeque();
         this.busyArray = ALL_IDLE;
      }

      void eviction() {
         long evictionTimestamp = this.parent.clock.millis();

         for(BoundedElasticScheduler.BoundedState candidate : new ArrayList(this.idleQueue)) {
            if (candidate.tryEvict(evictionTimestamp, this.parent.ttlMillis)) {
               this.idleQueue.remove(candidate);
               this.decrementAndGet();
            }
         }

      }

      boolean setBusy(BoundedElasticScheduler.BoundedState bs) {
         BoundedElasticScheduler.BoundedState[] previous;
         BoundedElasticScheduler.BoundedState[] replacement;
         do {
            previous = this.busyArray;
            if (previous == ALL_SHUTDOWN) {
               return false;
            }

            int len = previous.length;
            replacement = new BoundedElasticScheduler.BoundedState[len + 1];
            System.arraycopy(previous, 0, replacement, 0, len);
            replacement[len] = bs;
         } while(!BUSY_ARRAY.compareAndSet(this, previous, replacement));

         return true;
      }

      void setIdle(BoundedElasticScheduler.BoundedState boundedState) {
         BoundedElasticScheduler.BoundedState[] arr;
         BoundedElasticScheduler.BoundedState[] replacement;
         do {
            arr = this.busyArray;
            int len = arr.length;
            if (len == 0) {
               return;
            }

            replacement = null;
            if (len == 1) {
               if (arr[0] == boundedState) {
                  replacement = ALL_IDLE;
               }
            } else {
               for(int i = 0; i < len; ++i) {
                  BoundedElasticScheduler.BoundedState state = arr[i];
                  if (state == boundedState) {
                     replacement = new BoundedElasticScheduler.BoundedState[len - 1];
                     System.arraycopy(arr, 0, replacement, 0, i);
                     System.arraycopy(arr, i + 1, replacement, i, len - i - 1);
                     break;
                  }
               }
            }

            if (replacement == null) {
               return;
            }
         } while(!BUSY_ARRAY.compareAndSet(this, arr, replacement));

         this.idleQueue.add(boundedState);
      }

      BoundedElasticScheduler.BoundedState pick() {
         while(true) {
            int a = this.get();
            if (a != -1 && this.busyArray != ALL_SHUTDOWN) {
               if (!this.idleQueue.isEmpty()) {
                  BoundedElasticScheduler.BoundedState bs = (BoundedElasticScheduler.BoundedState)this.idleQueue.pollLast();
                  if (bs == null || !bs.markPicked()) {
                     continue;
                  }

                  this.setBusy(bs);
                  return bs;
               }

               if (a < this.parent.maxThreads) {
                  if (!this.compareAndSet(a, a + 1)) {
                     continue;
                  }

                  ScheduledExecutorService s = Schedulers.decorateExecutorService(this.parent, this.parent.createBoundedExecutorService());
                  BoundedElasticScheduler.BoundedState newState = new BoundedElasticScheduler.BoundedState(this, s);
                  if (!newState.markPicked()) {
                     continue;
                  }

                  this.setBusy(newState);
                  return newState;
               }

               BoundedElasticScheduler.BoundedState s = this.choseOneBusy();
               if (s == null || !s.markPicked()) {
                  continue;
               }

               return s;
            }

            return BoundedElasticScheduler.CREATING;
         }
      }

      @Nullable
      private BoundedElasticScheduler.BoundedState choseOneBusy() {
         BoundedElasticScheduler.BoundedState[] arr = this.busyArray;
         int len = arr.length;
         if (len == 0) {
            return null;
         } else if (len == 1) {
            return arr[0];
         } else {
            BoundedElasticScheduler.BoundedState choice = arr[0];
            int leastBusy = Integer.MAX_VALUE;

            for(int i = 0; i < arr.length; ++i) {
               BoundedElasticScheduler.BoundedState state = arr[i];
               int busy = state.markCount;
               if (busy < leastBusy) {
                  leastBusy = busy;
                  choice = state;
               }
            }

            return choice;
         }
      }

      @Override
      public boolean isDisposed() {
         return this.get() == -1;
      }

      @Override
      public void dispose() {
         this.set(-1);
         this.idleQueue.forEach(BoundedElasticScheduler.BoundedState::shutdown);
         BoundedElasticScheduler.BoundedState[] arr = (BoundedElasticScheduler.BoundedState[])BUSY_ARRAY.getAndSet(this, ALL_SHUTDOWN);

         for(int i = 0; i < arr.length; ++i) {
            arr[i].shutdown();
         }

      }
   }

   static class BoundedState implements Disposable, Scannable {
      static final int EVICTED = -1;
      final BoundedElasticScheduler.BoundedServices parent;
      final ScheduledExecutorService executor;
      long idleSinceTimestamp = -1L;
      volatile int markCount;
      static final AtomicIntegerFieldUpdater<BoundedElasticScheduler.BoundedState> MARK_COUNT = AtomicIntegerFieldUpdater.newUpdater(
         BoundedElasticScheduler.BoundedState.class, "markCount"
      );

      BoundedState(BoundedElasticScheduler.BoundedServices parent, ScheduledExecutorService executor) {
         this.parent = parent;
         this.executor = executor;
      }

      int estimateQueueSize() {
         return this.executor instanceof ScheduledThreadPoolExecutor ? ((ScheduledThreadPoolExecutor)this.executor).getQueue().size() : -1;
      }

      boolean markPicked() {
         int i;
         do {
            i = MARK_COUNT.get(this);
            if (i == -1) {
               return false;
            }
         } while(!MARK_COUNT.compareAndSet(this, i, i + 1));

         return true;
      }

      boolean tryEvict(long evictionTimestamp, long ttlMillis) {
         long idleSince = this.idleSinceTimestamp;
         if (idleSince < 0L) {
            return false;
         } else {
            long elapsed = evictionTimestamp - idleSince;
            if (elapsed >= ttlMillis && MARK_COUNT.compareAndSet(this, 0, -1)) {
               this.executor.shutdownNow();
               return true;
            } else {
               return false;
            }
         }
      }

      void release() {
         int picked = MARK_COUNT.decrementAndGet(this);
         if (picked >= 0) {
            if (picked == 0) {
               this.idleSinceTimestamp = this.parent.clock.millis();
               this.parent.setIdle(this);
            } else {
               this.idleSinceTimestamp = -1L;
            }

         }
      }

      void shutdown() {
         this.idleSinceTimestamp = -1L;
         MARK_COUNT.set(this, -1);
         this.executor.shutdownNow();
      }

      @Override
      public void dispose() {
         this.release();
      }

      @Override
      public boolean isDisposed() {
         return MARK_COUNT.get(this) <= 0;
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return Schedulers.scanExecutor(this.executor, key);
      }

      public String toString() {
         return "BoundedState@"
            + System.identityHashCode(this)
            + "{ backing="
            + MARK_COUNT.get(this)
            + ", idleSince="
            + this.idleSinceTimestamp
            + ", executor="
            + this.executor
            + '}';
      }
   }
}
