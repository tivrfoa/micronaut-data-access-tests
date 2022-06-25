package reactor.core.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.util.annotation.Nullable;

final class SchedulerTask implements Runnable, Disposable, Callable<Void> {
   final Runnable task;
   static final Future<Void> FINISHED = new FutureTask(() -> null);
   static final Future<Void> CANCELLED = new FutureTask(() -> null);
   static final Disposable TAKEN = Disposables.disposed();
   volatile Future<?> future;
   static final AtomicReferenceFieldUpdater<SchedulerTask, Future> FUTURE = AtomicReferenceFieldUpdater.newUpdater(SchedulerTask.class, Future.class, "future");
   volatile Disposable parent;
   static final AtomicReferenceFieldUpdater<SchedulerTask, Disposable> PARENT = AtomicReferenceFieldUpdater.newUpdater(
      SchedulerTask.class, Disposable.class, "parent"
   );
   Thread thread;

   SchedulerTask(Runnable task, @Nullable Disposable parent) {
      this.task = task;
      PARENT.lazySet(this, parent);
   }

   @Nullable
   public Void call() {
      this.thread = Thread.currentThread();
      Disposable d = null;

      try {
         while(true) {
            d = this.parent;
            if (d == TAKEN || d == null || PARENT.compareAndSet(this, d, TAKEN)) {
               try {
                  this.task.run();
               } catch (Throwable var7) {
                  Schedulers.handleError(var7);
               }
               break;
            }
         }
      } finally {
         this.thread = null;

         Future f;
         do {
            f = this.future;
         } while(f != CANCELLED && !FUTURE.compareAndSet(this, f, FINISHED));

         if (d != null) {
            d.dispose();
         }

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
         if (o == FINISHED) {
            return;
         }

         if (o == CANCELLED) {
            f.cancel(this.thread != Thread.currentThread());
            return;
         }
      } while(!FUTURE.compareAndSet(this, o, f));

   }

   @Override
   public boolean isDisposed() {
      Future<?> a = this.future;
      return FINISHED == a || CANCELLED == a;
   }

   @Override
   public void dispose() {
      while(true) {
         Future f = this.future;
         if (f != FINISHED && f != CANCELLED) {
            if (!FUTURE.compareAndSet(this, f, CANCELLED)) {
               continue;
            }

            if (f != null) {
               f.cancel(this.thread != Thread.currentThread());
            }
         }

         while(true) {
            Disposable d = this.parent;
            if (d == TAKEN || d == null) {
               break;
            }

            if (PARENT.compareAndSet(this, d, TAKEN)) {
               d.dispose();
               break;
            }
         }

         return;
      }
   }
}
