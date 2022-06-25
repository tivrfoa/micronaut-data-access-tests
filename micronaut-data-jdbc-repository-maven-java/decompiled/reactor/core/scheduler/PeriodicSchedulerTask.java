package reactor.core.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import reactor.core.Disposable;
import reactor.util.annotation.Nullable;

final class PeriodicSchedulerTask implements Runnable, Disposable, Callable<Void> {
   final Runnable task;
   static final Future<Void> CANCELLED = new FutureTask(() -> null);
   volatile Future<?> future;
   static final AtomicReferenceFieldUpdater<PeriodicSchedulerTask, Future> FUTURE = AtomicReferenceFieldUpdater.newUpdater(
      PeriodicSchedulerTask.class, Future.class, "future"
   );
   Thread thread;

   PeriodicSchedulerTask(Runnable task) {
      this.task = task;
   }

   @Nullable
   public Void call() {
      this.thread = Thread.currentThread();

      try {
         this.task.run();
      } catch (Throwable var5) {
         Schedulers.handleError(var5);
      } finally {
         this.thread = null;
      }

      return null;
   }

   public void run() {
      this.call();
   }

   void setFuture(Future<?> f) {
      Future o;
      do {
         o = this.future;
         if (o == CANCELLED) {
            f.cancel(this.thread != Thread.currentThread());
            return;
         }
      } while(!FUTURE.compareAndSet(this, o, f));

   }

   @Override
   public boolean isDisposed() {
      return this.future == CANCELLED;
   }

   @Override
   public void dispose() {
      while(true) {
         Future f = this.future;
         if (f != CANCELLED) {
            if (!FUTURE.compareAndSet(this, f, CANCELLED)) {
               continue;
            }

            if (f != null) {
               f.cancel(this.thread != Thread.currentThread());
            }
         }

         return;
      }
   }
}
