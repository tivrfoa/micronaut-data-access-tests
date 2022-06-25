package reactor.core.scheduler;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Scannable;

final class ExecutorServiceWorker implements Scheduler.Worker, Disposable, Scannable {
   final ScheduledExecutorService exec;
   final Disposable.Composite disposables;

   ExecutorServiceWorker(ScheduledExecutorService exec) {
      this.exec = exec;
      this.disposables = Disposables.composite();
   }

   @Override
   public Disposable schedule(Runnable task) {
      return Schedulers.workerSchedule(this.exec, this.disposables, task, 0L, TimeUnit.MILLISECONDS);
   }

   @Override
   public Disposable schedule(Runnable task, long delay, TimeUnit unit) {
      return Schedulers.workerSchedule(this.exec, this.disposables, task, delay, unit);
   }

   @Override
   public Disposable schedulePeriodically(Runnable task, long initialDelay, long period, TimeUnit unit) {
      return Schedulers.workerSchedulePeriodically(this.exec, this.disposables, task, initialDelay, period, unit);
   }

   @Override
   public void dispose() {
      this.disposables.dispose();
   }

   @Override
   public boolean isDisposed() {
      return this.disposables.isDisposed();
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.BUFFERED) {
         return this.disposables.size();
      } else if (key == Scannable.Attr.TERMINATED || key == Scannable.Attr.CANCELLED) {
         return this.isDisposed();
      } else {
         return key == Scannable.Attr.NAME ? "ExecutorServiceWorker" : Schedulers.scanExecutor(this.exec, key);
      }
   }
}
