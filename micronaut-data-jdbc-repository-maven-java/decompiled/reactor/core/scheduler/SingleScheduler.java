package reactor.core.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;
import reactor.core.Disposable;
import reactor.core.Scannable;

final class SingleScheduler implements Scheduler, Supplier<ScheduledExecutorService>, Scannable {
   static final AtomicLong COUNTER = new AtomicLong();
   final ThreadFactory factory;
   volatile ScheduledExecutorService executor;
   static final AtomicReferenceFieldUpdater<SingleScheduler, ScheduledExecutorService> EXECUTORS = AtomicReferenceFieldUpdater.newUpdater(
      SingleScheduler.class, ScheduledExecutorService.class, "executor"
   );
   static final ScheduledExecutorService TERMINATED = Executors.newSingleThreadScheduledExecutor();

   SingleScheduler(ThreadFactory factory) {
      this.factory = factory;
   }

   public ScheduledExecutorService get() {
      ScheduledThreadPoolExecutor e = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1, this.factory);
      e.setRemoveOnCancelPolicy(true);
      e.setMaximumPoolSize(1);
      return e;
   }

   @Override
   public boolean isDisposed() {
      return this.executor == TERMINATED;
   }

   @Override
   public void start() {
      ScheduledExecutorService b = null;

      ScheduledExecutorService a;
      do {
         a = this.executor;
         if (a != TERMINATED && a != null) {
            if (b != null) {
               b.shutdownNow();
            }

            return;
         }

         if (b == null) {
            b = Schedulers.decorateExecutorService(this, this.get());
         }
      } while(!EXECUTORS.compareAndSet(this, a, b));

   }

   @Override
   public void dispose() {
      ScheduledExecutorService a = this.executor;
      if (a != TERMINATED) {
         a = (ScheduledExecutorService)EXECUTORS.getAndSet(this, TERMINATED);
         if (a != TERMINATED && a != null) {
            a.shutdownNow();
         }
      }

   }

   @Override
   public Disposable schedule(Runnable task) {
      return Schedulers.directSchedule(this.executor, task, null, 0L, TimeUnit.MILLISECONDS);
   }

   @Override
   public Disposable schedule(Runnable task, long delay, TimeUnit unit) {
      return Schedulers.directSchedule(this.executor, task, null, delay, unit);
   }

   @Override
   public Disposable schedulePeriodically(Runnable task, long initialDelay, long period, TimeUnit unit) {
      return Schedulers.directSchedulePeriodically(this.executor, task, initialDelay, period, unit);
   }

   public String toString() {
      StringBuilder ts = new StringBuilder("single").append('(');
      if (this.factory instanceof ReactorThreadFactory) {
         ts.append('"').append(((ReactorThreadFactory)this.factory).get()).append('"');
      }

      return ts.append(')').toString();
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.TERMINATED || key == Scannable.Attr.CANCELLED) {
         return this.isDisposed();
      } else if (key == Scannable.Attr.NAME) {
         return this.toString();
      } else {
         return key != Scannable.Attr.CAPACITY && key != Scannable.Attr.BUFFERED ? Schedulers.scanExecutor(this.executor, key) : 1;
      }
   }

   @Override
   public Scheduler.Worker createWorker() {
      return new ExecutorServiceWorker(this.executor);
   }

   static {
      TERMINATED.shutdownNow();
   }
}
