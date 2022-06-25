package reactor.core.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import reactor.core.Disposable;
import reactor.util.annotation.Nullable;

final class InstantPeriodicWorkerTask implements Disposable, Callable<Void> {
   final Runnable task;
   final ExecutorService executor;
   static final Disposable.Composite DISPOSED = new EmptyCompositeDisposable();
   static final Future<Void> CANCELLED = new FutureTask(() -> null);
   volatile Future<?> rest;
   static final AtomicReferenceFieldUpdater<InstantPeriodicWorkerTask, Future> REST = AtomicReferenceFieldUpdater.newUpdater(
      InstantPeriodicWorkerTask.class, Future.class, "rest"
   );
   volatile Future<?> first;
   static final AtomicReferenceFieldUpdater<InstantPeriodicWorkerTask, Future> FIRST = AtomicReferenceFieldUpdater.newUpdater(
      InstantPeriodicWorkerTask.class, Future.class, "first"
   );
   volatile Disposable.Composite parent;
   static final AtomicReferenceFieldUpdater<InstantPeriodicWorkerTask, Disposable.Composite> PARENT = AtomicReferenceFieldUpdater.newUpdater(
      InstantPeriodicWorkerTask.class, Disposable.Composite.class, "parent"
   );
   Thread thread;

   InstantPeriodicWorkerTask(Runnable task, ExecutorService executor) {
      this.task = task;
      this.executor = executor;
   }

   InstantPeriodicWorkerTask(Runnable task, ExecutorService executor, Disposable.Composite parent) {
      this.task = task;
      this.executor = executor;
      PARENT.lazySet(this, parent);
   }

   @Nullable
   public Void call() {
      this.thread = Thread.currentThread();

      try {
         this.task.run();
         this.setRest(this.executor.submit(this));
      } catch (Throwable var5) {
         Schedulers.handleError(var5);
      } finally {
         this.thread = null;
      }

      return null;
   }

   void setRest(Future<?> f) {
      Future o;
      do {
         o = this.rest;
         if (o == CANCELLED) {
            f.cancel(this.thread != Thread.currentThread());
            return;
         }
      } while(!REST.compareAndSet(this, o, f));

   }

   void setFirst(Future<?> f) {
      Future o;
      do {
         o = this.first;
         if (o == CANCELLED) {
            f.cancel(this.thread != Thread.currentThread());
            return;
         }
      } while(!FIRST.compareAndSet(this, o, f));

   }

   @Override
   public boolean isDisposed() {
      return this.rest == CANCELLED;
   }

   @Override
   public void dispose() {
      while(true) {
         Future f = this.first;
         if (f != CANCELLED) {
            if (!FIRST.compareAndSet(this, f, CANCELLED)) {
               continue;
            }

            if (f != null) {
               f.cancel(this.thread != Thread.currentThread());
            }
         }

         while(true) {
            f = this.rest;
            if (f == CANCELLED) {
               break;
            }

            if (REST.compareAndSet(this, f, CANCELLED)) {
               if (f != null) {
                  f.cancel(this.thread != Thread.currentThread());
               }
               break;
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
