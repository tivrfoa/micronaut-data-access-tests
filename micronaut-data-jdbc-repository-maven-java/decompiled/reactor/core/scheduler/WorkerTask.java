package reactor.core.scheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import reactor.core.Disposable;
import reactor.util.annotation.Nullable;

final class WorkerTask implements Runnable, Disposable, Callable<Void> {
   final Runnable task;
   static final Disposable.Composite DISPOSED = new EmptyCompositeDisposable();
   static final Disposable.Composite DONE = new EmptyCompositeDisposable();
   static final Future<Void> FINISHED = new FutureTask(() -> null);
   static final Future<Void> SYNC_CANCELLED = new FutureTask(() -> null);
   static final Future<Void> ASYNC_CANCELLED = new FutureTask(() -> null);
   volatile Future<?> future;
   static final AtomicReferenceFieldUpdater<WorkerTask, Future> FUTURE = AtomicReferenceFieldUpdater.newUpdater(WorkerTask.class, Future.class, "future");
   volatile Disposable.Composite parent;
   static final AtomicReferenceFieldUpdater<WorkerTask, Disposable.Composite> PARENT = AtomicReferenceFieldUpdater.newUpdater(
      WorkerTask.class, Disposable.Composite.class, "parent"
   );
   volatile Thread thread;
   static final AtomicReferenceFieldUpdater<WorkerTask, Thread> THREAD = AtomicReferenceFieldUpdater.newUpdater(WorkerTask.class, Thread.class, "thread");

   WorkerTask(Runnable task, Disposable.Composite parent) {
      this.task = task;
      PARENT.lazySet(this, parent);
   }

   @Nullable
   public Void call() {
      THREAD.lazySet(this, Thread.currentThread());

      try {
         this.task.run();
      } catch (Throwable var8) {
         Schedulers.handleError(var8);
      } finally {
         THREAD.lazySet(this, null);
         Disposable.Composite o = this.parent;
         if (o != DISPOSED && PARENT.compareAndSet(this, o, DONE) && o != null) {
            o.remove(this);
         }

         Future f;
         do {
            f = this.future;
         } while(f != SYNC_CANCELLED && f != ASYNC_CANCELLED && !FUTURE.compareAndSet(this, f, FINISHED));

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

         if (o == SYNC_CANCELLED) {
            f.cancel(false);
            return;
         }

         if (o == ASYNC_CANCELLED) {
            f.cancel(true);
            return;
         }
      } while(!FUTURE.compareAndSet(this, o, f));

   }

   @Override
   public boolean isDisposed() {
      Disposable.Composite o = (Disposable.Composite)PARENT.get(this);
      return o == DISPOSED || o == DONE;
   }

   @Override
   public void dispose() {
      while(true) {
         Future f = this.future;
         if (f != FINISHED && f != SYNC_CANCELLED && f != ASYNC_CANCELLED) {
            boolean async = this.thread != Thread.currentThread();
            if (!FUTURE.compareAndSet(this, f, async ? ASYNC_CANCELLED : SYNC_CANCELLED)) {
               continue;
            }

            if (f != null) {
               f.cancel(async);
            }
         }

         do {
            o = this.parent;
            if (o == DONE || o == DISPOSED || o == null) {
               return;
            }
         } while(!PARENT.compareAndSet(this, o, DISPOSED));

         o.remove(this);
         return;
      }
   }
}
