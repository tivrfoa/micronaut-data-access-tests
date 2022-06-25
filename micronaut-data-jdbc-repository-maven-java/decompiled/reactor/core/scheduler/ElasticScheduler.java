package reactor.core.scheduler;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class ElasticScheduler implements Scheduler, Scannable {
   static final AtomicLong COUNTER = new AtomicLong();
   static final ThreadFactory EVICTOR_FACTORY = r -> {
      Thread t = new Thread(r, "elastic-evictor-" + COUNTER.incrementAndGet());
      t.setDaemon(true);
      return t;
   };
   static final ElasticScheduler.CachedService SHUTDOWN = new ElasticScheduler.CachedService(null);
   static final int DEFAULT_TTL_SECONDS = 60;
   final ThreadFactory factory;
   final int ttlSeconds;
   final Deque<ElasticScheduler.ScheduledExecutorServiceExpiry> cache;
   final Queue<ElasticScheduler.CachedService> all;
   ScheduledExecutorService evictor;
   volatile boolean shutdown;

   ElasticScheduler(ThreadFactory factory, int ttlSeconds) {
      if (ttlSeconds < 0) {
         throw new IllegalArgumentException("ttlSeconds must be positive, was: " + ttlSeconds);
      } else {
         this.ttlSeconds = ttlSeconds;
         this.factory = factory;
         this.cache = new ConcurrentLinkedDeque();
         this.all = new ConcurrentLinkedQueue();
         this.shutdown = true;
      }
   }

   public ScheduledExecutorService createUndecoratedService() {
      ScheduledThreadPoolExecutor poolExecutor = new ScheduledThreadPoolExecutor(1, this.factory);
      poolExecutor.setMaximumPoolSize(1);
      poolExecutor.setRemoveOnCancelPolicy(true);
      return poolExecutor;
   }

   @Override
   public void start() {
      if (this.shutdown) {
         this.evictor = Executors.newScheduledThreadPool(1, EVICTOR_FACTORY);
         this.evictor.scheduleAtFixedRate(this::eviction, (long)this.ttlSeconds, (long)this.ttlSeconds, TimeUnit.SECONDS);
         this.shutdown = false;
      }
   }

   @Override
   public boolean isDisposed() {
      return this.shutdown;
   }

   @Override
   public void dispose() {
      if (!this.shutdown) {
         this.shutdown = true;
         this.evictor.shutdownNow();
         this.cache.clear();

         ElasticScheduler.CachedService cached;
         while((cached = (ElasticScheduler.CachedService)this.all.poll()) != null) {
            cached.exec.shutdownNow();
         }

      }
   }

   ElasticScheduler.CachedService pick() {
      if (this.shutdown) {
         return SHUTDOWN;
      } else {
         ElasticScheduler.ScheduledExecutorServiceExpiry e = (ElasticScheduler.ScheduledExecutorServiceExpiry)this.cache.pollLast();
         if (e != null) {
            return e.cached;
         } else {
            ElasticScheduler.CachedService result = new ElasticScheduler.CachedService(this);
            this.all.offer(result);
            if (this.shutdown) {
               this.all.remove(result);
               return SHUTDOWN;
            } else {
               return result;
            }
         }
      }
   }

   @Override
   public Disposable schedule(Runnable task) {
      ElasticScheduler.CachedService cached = this.pick();
      return Schedulers.directSchedule(cached.exec, task, cached, 0L, TimeUnit.MILLISECONDS);
   }

   @Override
   public Disposable schedule(Runnable task, long delay, TimeUnit unit) {
      ElasticScheduler.CachedService cached = this.pick();
      return Schedulers.directSchedule(cached.exec, task, cached, delay, unit);
   }

   @Override
   public Disposable schedulePeriodically(Runnable task, long initialDelay, long period, TimeUnit unit) {
      ElasticScheduler.CachedService cached = this.pick();
      return Disposables.composite(Schedulers.directSchedulePeriodically(cached.exec, task, initialDelay, period, unit), cached);
   }

   public String toString() {
      StringBuilder ts = new StringBuilder("elastic").append('(');
      if (this.factory instanceof ReactorThreadFactory) {
         ts.append('"').append(((ReactorThreadFactory)this.factory).get()).append('"');
      }

      ts.append(')');
      return ts.toString();
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.TERMINATED || key == Scannable.Attr.CANCELLED) {
         return this.isDisposed();
      } else if (key == Scannable.Attr.CAPACITY) {
         return Integer.MAX_VALUE;
      } else if (key == Scannable.Attr.BUFFERED) {
         return this.cache.size();
      } else {
         return key == Scannable.Attr.NAME ? this.toString() : null;
      }
   }

   @Override
   public Stream<? extends Scannable> inners() {
      return this.cache.stream().map(cached -> cached.cached);
   }

   @Override
   public Scheduler.Worker createWorker() {
      return new ElasticScheduler.ElasticWorker(this.pick());
   }

   void eviction() {
      long now = System.currentTimeMillis();

      for(ElasticScheduler.ScheduledExecutorServiceExpiry e : new ArrayList(this.cache)) {
         if (e.expireMillis < now && this.cache.remove(e)) {
            e.cached.exec.shutdownNow();
            this.all.remove(e.cached);
         }
      }

   }

   static final class CachedService implements Disposable, Scannable {
      final ElasticScheduler parent;
      final ScheduledExecutorService exec;

      CachedService(@Nullable ElasticScheduler parent) {
         this.parent = parent;
         if (parent != null) {
            this.exec = Schedulers.decorateExecutorService(parent, parent.createUndecoratedService());
         } else {
            this.exec = Executors.newSingleThreadScheduledExecutor();
            this.exec.shutdownNow();
         }

      }

      @Override
      public void dispose() {
         if (this.exec != null && this != ElasticScheduler.SHUTDOWN && !this.parent.shutdown) {
            ElasticScheduler.ScheduledExecutorServiceExpiry e = new ElasticScheduler.ScheduledExecutorServiceExpiry(
               this, System.currentTimeMillis() + (long)this.parent.ttlSeconds * 1000L
            );
            this.parent.cache.offerLast(e);
            if (this.parent.shutdown && this.parent.cache.remove(e)) {
               this.exec.shutdownNow();
            }
         }

      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.NAME) {
            return this.parent.scanUnsafe(key);
         } else if (key == Scannable.Attr.PARENT) {
            return this.parent;
         } else if (key != Scannable.Attr.TERMINATED && key != Scannable.Attr.CANCELLED) {
            if (key == Scannable.Attr.CAPACITY) {
               Integer capacity = (Integer)Schedulers.scanExecutor(this.exec, key);
               if (capacity == null || capacity == -1) {
                  return 1;
               }
            }

            return Schedulers.scanExecutor(this.exec, key);
         } else {
            return this.isDisposed();
         }
      }
   }

   static final class ElasticWorker extends AtomicBoolean implements Scheduler.Worker, Scannable {
      final ElasticScheduler.CachedService cached;
      final Disposable.Composite tasks;

      ElasticWorker(ElasticScheduler.CachedService cached) {
         this.cached = cached;
         this.tasks = Disposables.composite();
      }

      @Override
      public Disposable schedule(Runnable task) {
         return Schedulers.workerSchedule(this.cached.exec, this.tasks, task, 0L, TimeUnit.MILLISECONDS);
      }

      @Override
      public Disposable schedule(Runnable task, long delay, TimeUnit unit) {
         return Schedulers.workerSchedule(this.cached.exec, this.tasks, task, delay, unit);
      }

      @Override
      public Disposable schedulePeriodically(Runnable task, long initialDelay, long period, TimeUnit unit) {
         return Schedulers.workerSchedulePeriodically(this.cached.exec, this.tasks, task, initialDelay, period, unit);
      }

      @Override
      public void dispose() {
         if (this.compareAndSet(false, true)) {
            this.tasks.dispose();
            this.cached.dispose();
         }

      }

      @Override
      public boolean isDisposed() {
         return this.tasks.isDisposed();
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED || key == Scannable.Attr.CANCELLED) {
            return this.isDisposed();
         } else if (key == Scannable.Attr.NAME) {
            return this.cached.scanUnsafe(key) + ".worker";
         } else {
            return key == Scannable.Attr.PARENT ? this.cached.parent : this.cached.scanUnsafe(key);
         }
      }
   }

   static final class ScheduledExecutorServiceExpiry {
      final ElasticScheduler.CachedService cached;
      final long expireMillis;

      ScheduledExecutorServiceExpiry(ElasticScheduler.CachedService cached, long expireMillis) {
         this.cached = cached;
         this.expireMillis = expireMillis;
      }
   }
}
