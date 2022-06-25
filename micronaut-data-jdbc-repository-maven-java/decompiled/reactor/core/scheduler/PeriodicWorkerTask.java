package reactor.core.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import reactor.core.Disposable;
import reactor.util.annotation.Nullable;

final class PeriodicWorkerTask implements Runnable, Disposable, Callable<Void> {
   final Runnable task;
   static final Disposable.Composite DISPOSED = new EmptyCompositeDisposable();
   static final Future<Void> CANCELLED = new FutureTask(() -> null);
   volatile Future<?> future;
   static final AtomicReferenceFieldUpdater<PeriodicWorkerTask, Future> FUTURE = AtomicReferenceFieldUpdater.newUpdater(
      PeriodicWorkerTask.class, Future.class, "future"
   );
   volatile Disposable.Composite parent;
   static final AtomicReferenceFieldUpdater<PeriodicWorkerTask, Disposable.Composite> PARENT = AtomicReferenceFieldUpdater.newUpdater(
      PeriodicWorkerTask.class, Disposable.Composite.class, "parent"
   );
   Thread thread;

   PeriodicWorkerTask(Runnable task, Disposable.Composite parent) {
      this.task = task;
      PARENT.lazySet(this, parent);
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

         do {
            o = this.parent;
            if (o == DISPOSED || o == null) {
               return;
            }
         } while(!PARENT.compareAndSet(this, o, DISPOSED));

         o.remove(this);
         return;
      }
   }
}
