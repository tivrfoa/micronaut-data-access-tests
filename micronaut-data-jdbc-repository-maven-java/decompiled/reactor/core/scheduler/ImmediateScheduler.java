package reactor.core.scheduler;

import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Exceptions;
import reactor.core.Scannable;

final class ImmediateScheduler implements Scheduler, Scannable {
   private static final ImmediateScheduler INSTANCE = new ImmediateScheduler();
   static final Disposable FINISHED = Disposables.disposed();

   public static Scheduler instance() {
      return INSTANCE;
   }

   private ImmediateScheduler() {
   }

   @Override
   public Disposable schedule(Runnable task) {
      task.run();
      return FINISHED;
   }

   @Override
   public void dispose() {
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.TERMINATED || key == Scannable.Attr.CANCELLED) {
         return this.isDisposed();
      } else {
         return key == Scannable.Attr.NAME ? "immediate" : null;
      }
   }

   @Override
   public Scheduler.Worker createWorker() {
      return new ImmediateScheduler.ImmediateSchedulerWorker();
   }

   static {
      INSTANCE.start();
   }

   static final class ImmediateSchedulerWorker implements Scheduler.Worker, Scannable {
      volatile boolean shutdown;

      @Override
      public Disposable schedule(Runnable task) {
         if (this.shutdown) {
            throw Exceptions.failWithRejected();
         } else {
            task.run();
            return ImmediateScheduler.FINISHED;
         }
      }

      @Override
      public void dispose() {
         this.shutdown = true;
      }

      @Override
      public boolean isDisposed() {
         return this.shutdown;
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED || key == Scannable.Attr.CANCELLED) {
            return this.shutdown;
         } else {
            return key == Scannable.Attr.NAME ? "immediate.worker" : null;
         }
      }
   }
}
