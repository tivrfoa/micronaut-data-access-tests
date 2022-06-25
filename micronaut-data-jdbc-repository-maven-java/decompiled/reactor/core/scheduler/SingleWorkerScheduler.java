package reactor.core.scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import reactor.core.Disposable;
import reactor.core.Scannable;

final class SingleWorkerScheduler implements Scheduler, Executor, Scannable {
   final Scheduler.Worker main;

   SingleWorkerScheduler(Scheduler actual) {
      this.main = actual.createWorker();
   }

   @Override
   public void dispose() {
      this.main.dispose();
   }

   @Override
   public Disposable schedule(Runnable task) {
      return this.main.schedule(task);
   }

   @Override
   public Disposable schedule(Runnable task, long delay, TimeUnit unit) {
      return this.main.schedule(task, delay, unit);
   }

   @Override
   public Disposable schedulePeriodically(Runnable task, long initialDelay, long period, TimeUnit unit) {
      return this.main.schedulePeriodically(task, initialDelay, period, unit);
   }

   public void execute(Runnable command) {
      this.main.schedule(command);
   }

   @Override
   public Scheduler.Worker createWorker() {
      return new ExecutorScheduler.ExecutorSchedulerWorker(this);
   }

   @Override
   public boolean isDisposed() {
      return this.main.isDisposed();
   }

   public String toString() {
      Scannable mainScannable = Scannable.from(this.main);
      return mainScannable.isScanAvailable()
         ? "singleWorker(" + mainScannable.scanUnsafe(Scannable.Attr.NAME) + ")"
         : "singleWorker(" + this.main.toString() + ")";
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.TERMINATED || key == Scannable.Attr.CANCELLED) {
         return this.isDisposed();
      } else if (key == Scannable.Attr.PARENT) {
         return this.main;
      } else {
         return key == Scannable.Attr.NAME ? this.toString() : Scannable.from(this.main).scanUnsafe(key);
      }
   }
}
