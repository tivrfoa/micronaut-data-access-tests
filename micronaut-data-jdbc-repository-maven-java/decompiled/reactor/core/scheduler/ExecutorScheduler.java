package reactor.core.scheduler;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Exceptions;
import reactor.core.Scannable;

final class ExecutorScheduler implements Scheduler, Scannable {
   final Executor executor;
   final boolean trampoline;
   volatile boolean terminated;

   ExecutorScheduler(Executor executor, boolean trampoline) {
      this.executor = executor;
      this.trampoline = trampoline;
   }

   @Override
   public Disposable schedule(Runnable task) {
      if (this.terminated) {
         throw Exceptions.failWithRejected();
      } else {
         Objects.requireNonNull(task, "task");
         ExecutorScheduler.ExecutorPlainRunnable r = new ExecutorScheduler.ExecutorPlainRunnable(task);

         try {
            this.executor.execute(r);
            return r;
         } catch (Throwable var4) {
            if (this.executor instanceof ExecutorService && ((ExecutorService)this.executor).isShutdown()) {
               this.terminated = true;
            }

            Schedulers.handleError(var4);
            throw Exceptions.failWithRejected(var4);
         }
      }
   }

   @Override
   public void dispose() {
      this.terminated = true;
   }

   @Override
   public boolean isDisposed() {
      return this.terminated;
   }

   @Override
   public Scheduler.Worker createWorker() {
      return (Scheduler.Worker)(this.trampoline
         ? new ExecutorScheduler.ExecutorSchedulerTrampolineWorker(this.executor)
         : new ExecutorScheduler.ExecutorSchedulerWorker(this.executor));
   }

   public String toString() {
      StringBuilder ts = new StringBuilder("fromExecutor").append('(').append(this.executor);
      if (this.trampoline) {
         ts.append(",trampolining");
      }

      ts.append(')');
      return ts.toString();
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.TERMINATED || key == Scannable.Attr.CANCELLED) {
         return this.isDisposed();
      } else {
         return key == Scannable.Attr.NAME ? this.toString() : null;
      }
   }

   static final class ExecutorPlainRunnable extends AtomicBoolean implements Runnable, Disposable {
      private static final long serialVersionUID = 5116223460201378097L;
      final Runnable task;

      ExecutorPlainRunnable(Runnable task) {
         this.task = task;
      }

      public void run() {
         if (!this.get()) {
            try {
               this.task.run();
            } catch (Throwable var5) {
               Schedulers.handleError(var5);
            } finally {
               this.lazySet(true);
            }
         }

      }

      @Override
      public boolean isDisposed() {
         return this.get();
      }

      @Override
      public void dispose() {
         this.set(true);
      }
   }

   static final class ExecutorSchedulerTrampolineWorker implements Scheduler.Worker, ExecutorScheduler.WorkerDelete, Runnable, Scannable {
      final Executor executor;
      final Queue<ExecutorScheduler.ExecutorTrackedRunnable> queue;
      volatile boolean terminated;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<ExecutorScheduler.ExecutorSchedulerTrampolineWorker> WIP = AtomicIntegerFieldUpdater.newUpdater(
         ExecutorScheduler.ExecutorSchedulerTrampolineWorker.class, "wip"
      );

      ExecutorSchedulerTrampolineWorker(Executor executor) {
         this.executor = executor;
         this.queue = new ConcurrentLinkedQueue();
      }

      @Override
      public Disposable schedule(Runnable task) {
         Objects.requireNonNull(task, "task");
         if (this.terminated) {
            throw Exceptions.failWithRejected();
         } else {
            ExecutorScheduler.ExecutorTrackedRunnable r = new ExecutorScheduler.ExecutorTrackedRunnable(task, this, false);
            synchronized(this) {
               if (this.terminated) {
                  throw Exceptions.failWithRejected();
               }

               this.queue.offer(r);
            }

            if (WIP.getAndIncrement(this) == 0) {
               try {
                  this.executor.execute(this);
               } catch (Throwable var5) {
                  r.dispose();
                  Schedulers.handleError(var5);
                  throw Exceptions.failWithRejected(var5);
               }
            }

            return r;
         }
      }

      @Override
      public void dispose() {
         if (!this.terminated) {
            this.terminated = true;
            Queue<ExecutorScheduler.ExecutorTrackedRunnable> q = this.queue;

            ExecutorScheduler.ExecutorTrackedRunnable r;
            while((r = (ExecutorScheduler.ExecutorTrackedRunnable)q.poll()) != null && !q.isEmpty()) {
               r.dispose();
            }

         }
      }

      @Override
      public boolean isDisposed() {
         return this.terminated;
      }

      @Override
      public void delete(ExecutorScheduler.ExecutorTrackedRunnable r) {
         synchronized(this) {
            if (!this.terminated) {
               this.queue.remove(r);
            }

         }
      }

      public void run() {
         Queue<ExecutorScheduler.ExecutorTrackedRunnable> q = this.queue;

         int e;
         do {
            e = 0;

            int r;
            for(r = this.wip; e != r; ++e) {
               if (this.terminated) {
                  return;
               }

               ExecutorScheduler.ExecutorTrackedRunnable task = (ExecutorScheduler.ExecutorTrackedRunnable)q.poll();
               if (task == null) {
                  break;
               }

               task.run();
            }

            if (e == r && this.terminated) {
               return;
            }
         } while(WIP.addAndGet(this, -e) != 0);

      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED || key == Scannable.Attr.CANCELLED) {
            return this.isDisposed();
         } else if (key == Scannable.Attr.PARENT) {
            return this.executor instanceof Scannable ? this.executor : null;
         } else if (key == Scannable.Attr.NAME) {
            return "fromExecutor(" + this.executor + ",trampolining).worker";
         } else {
            return key != Scannable.Attr.BUFFERED && key != Scannable.Attr.LARGE_BUFFERED ? Schedulers.scanExecutor(this.executor, key) : this.queue.size();
         }
      }
   }

   static final class ExecutorSchedulerWorker implements Scheduler.Worker, ExecutorScheduler.WorkerDelete, Scannable {
      final Executor executor;
      final Disposable.Composite tasks;

      ExecutorSchedulerWorker(Executor executor) {
         this.executor = executor;
         this.tasks = Disposables.composite();
      }

      @Override
      public Disposable schedule(Runnable task) {
         Objects.requireNonNull(task, "task");
         ExecutorScheduler.ExecutorTrackedRunnable r = new ExecutorScheduler.ExecutorTrackedRunnable(task, this, true);
         if (!this.tasks.add(r)) {
            throw Exceptions.failWithRejected();
         } else {
            try {
               this.executor.execute(r);
               return r;
            } catch (Throwable var4) {
               this.tasks.remove(r);
               Schedulers.handleError(var4);
               throw Exceptions.failWithRejected(var4);
            }
         }
      }

      @Override
      public void dispose() {
         this.tasks.dispose();
      }

      @Override
      public boolean isDisposed() {
         return this.tasks.isDisposed();
      }

      @Override
      public void delete(ExecutorScheduler.ExecutorTrackedRunnable r) {
         this.tasks.remove(r);
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED || key == Scannable.Attr.CANCELLED) {
            return this.isDisposed();
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.tasks.size();
         } else if (key == Scannable.Attr.PARENT) {
            return this.executor instanceof Scannable ? this.executor : null;
         } else if (key != Scannable.Attr.NAME) {
            return Schedulers.scanExecutor(this.executor, key);
         } else {
            return this.executor instanceof SingleWorkerScheduler ? this.executor + ".worker" : "fromExecutor(" + this.executor + ").worker";
         }
      }
   }

   static final class ExecutorTrackedRunnable extends AtomicBoolean implements Runnable, Disposable {
      private static final long serialVersionUID = 3503344795919906192L;
      final Runnable task;
      final ExecutorScheduler.WorkerDelete parent;
      final boolean callRemoveOnFinish;

      ExecutorTrackedRunnable(Runnable task, ExecutorScheduler.WorkerDelete parent, boolean callRemoveOnFinish) {
         this.task = task;
         this.parent = parent;
         this.callRemoveOnFinish = callRemoveOnFinish;
      }

      public void run() {
         if (!this.get()) {
            try {
               this.task.run();
            } catch (Throwable var5) {
               Schedulers.handleError(var5);
            } finally {
               if (this.callRemoveOnFinish) {
                  this.dispose();
               } else {
                  this.lazySet(true);
               }

            }
         }

      }

      @Override
      public void dispose() {
         if (this.compareAndSet(false, true)) {
            this.parent.delete(this);
         }

      }

      @Override
      public boolean isDisposed() {
         return this.get();
      }
   }

   interface WorkerDelete {
      void delete(ExecutorScheduler.ExecutorTrackedRunnable var1);
   }
}
