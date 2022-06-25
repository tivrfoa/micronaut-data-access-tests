package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.Nullable;

final class FluxSubscribeOnCallable<T> extends Flux<T> implements Fuseable, Scannable {
   final Callable<? extends T> callable;
   final Scheduler scheduler;

   FluxSubscribeOnCallable(Callable<? extends T> callable, Scheduler scheduler) {
      this.callable = (Callable)Objects.requireNonNull(callable, "callable");
      this.scheduler = (Scheduler)Objects.requireNonNull(scheduler, "scheduler");
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      FluxSubscribeOnCallable.CallableSubscribeOnSubscription<T> parent = new FluxSubscribeOnCallable.CallableSubscribeOnSubscription<>(
         actual, this.callable, this.scheduler
      );
      actual.onSubscribe(parent);

      try {
         Disposable f = this.scheduler.schedule(parent);
         parent.setMainFuture(f);
      } catch (RejectedExecutionException var4) {
         if (parent.state != 4) {
            actual.onError(Operators.onRejectedExecution(var4, actual.currentContext()));
         }
      }

   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.RUN_ON) {
         return this.scheduler;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : null;
      }
   }

   static final class CallableSubscribeOnSubscription<T> implements Fuseable.QueueSubscription<T>, InnerProducer<T>, Runnable {
      final CoreSubscriber<? super T> actual;
      final Callable<? extends T> callable;
      final Scheduler scheduler;
      volatile int state;
      static final AtomicIntegerFieldUpdater<FluxSubscribeOnCallable.CallableSubscribeOnSubscription> STATE = AtomicIntegerFieldUpdater.newUpdater(
         FluxSubscribeOnCallable.CallableSubscribeOnSubscription.class, "state"
      );
      T value;
      static final int NO_REQUEST_HAS_VALUE = 1;
      static final int HAS_REQUEST_NO_VALUE = 2;
      static final int HAS_REQUEST_HAS_VALUE = 3;
      static final int HAS_CANCELLED = 4;
      int fusionState;
      static final int NO_VALUE = 1;
      static final int HAS_VALUE = 2;
      static final int COMPLETE = 3;
      volatile Disposable mainFuture;
      static final AtomicReferenceFieldUpdater<FluxSubscribeOnCallable.CallableSubscribeOnSubscription, Disposable> MAIN_FUTURE = AtomicReferenceFieldUpdater.newUpdater(
         FluxSubscribeOnCallable.CallableSubscribeOnSubscription.class, Disposable.class, "mainFuture"
      );
      volatile Disposable requestFuture;
      static final AtomicReferenceFieldUpdater<FluxSubscribeOnCallable.CallableSubscribeOnSubscription, Disposable> REQUEST_FUTURE = AtomicReferenceFieldUpdater.newUpdater(
         FluxSubscribeOnCallable.CallableSubscribeOnSubscription.class, Disposable.class, "requestFuture"
      );

      CallableSubscribeOnSubscription(CoreSubscriber<? super T> actual, Callable<? extends T> callable, Scheduler scheduler) {
         this.actual = actual;
         this.callable = callable;
         this.scheduler = scheduler;
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.state == 4;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.value != null ? 1 : 0;
         } else if (key == Scannable.Attr.RUN_ON) {
            return this.scheduler;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : InnerProducer.super.scanUnsafe(key);
         }
      }

      @Override
      public void cancel() {
         this.state = 4;
         this.fusionState = 3;
         Disposable a = this.mainFuture;
         if (a != OperatorDisposables.DISPOSED) {
            a = (Disposable)MAIN_FUTURE.getAndSet(this, OperatorDisposables.DISPOSED);
            if (a != null && a != OperatorDisposables.DISPOSED) {
               a.dispose();
            }
         }

         a = this.requestFuture;
         if (a != OperatorDisposables.DISPOSED) {
            a = (Disposable)REQUEST_FUTURE.getAndSet(this, OperatorDisposables.DISPOSED);
            if (a != null && a != OperatorDisposables.DISPOSED) {
               a.dispose();
            }
         }

      }

      public void clear() {
         this.value = null;
         this.fusionState = 3;
      }

      public boolean isEmpty() {
         return this.fusionState == 3;
      }

      @Nullable
      public T poll() {
         if (this.fusionState == 2) {
            this.fusionState = 3;
            return this.value;
         } else {
            return null;
         }
      }

      @Override
      public int requestFusion(int requestedMode) {
         if ((requestedMode & 2) != 0 && (requestedMode & 4) == 0) {
            this.fusionState = 1;
            return 2;
         } else {
            return 0;
         }
      }

      public int size() {
         return this.isEmpty() ? 0 : 1;
      }

      void setMainFuture(Disposable c) {
         Disposable a;
         do {
            a = this.mainFuture;
            if (a == OperatorDisposables.DISPOSED) {
               c.dispose();
               return;
            }
         } while(!MAIN_FUTURE.compareAndSet(this, a, c));

      }

      void setRequestFuture(Disposable c) {
         Disposable a;
         do {
            a = this.requestFuture;
            if (a == OperatorDisposables.DISPOSED) {
               c.dispose();
               return;
            }
         } while(!REQUEST_FUTURE.compareAndSet(this, a, c));

      }

      public void run() {
         T v;
         try {
            v = (T)this.callable.call();
         } catch (Throwable var3) {
            this.actual.onError(Operators.onOperatorError(this, var3, this.actual.currentContext()));
            return;
         }

         if (v == null) {
            this.fusionState = 3;
            this.actual.onComplete();
         } else {
            int s;
            do {
               s = this.state;
               if (s == 4 || s == 3 || s == 1) {
                  return;
               }

               if (s == 2) {
                  if (this.fusionState == 1) {
                     this.value = v;
                     this.fusionState = 2;
                  }

                  this.actual.onNext(v);
                  if (this.state != 4) {
                     this.actual.onComplete();
                  }

                  return;
               }

               this.value = v;
            } while(!STATE.compareAndSet(this, s, 1));

         }
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            int s;
            do {
               s = this.state;
               if (s == 4 || s == 2 || s == 3) {
                  return;
               }

               if (s == 1) {
                  if (STATE.compareAndSet(this, s, 3)) {
                     try {
                        Disposable f = this.scheduler.schedule(this::emitValue);
                        this.setRequestFuture(f);
                     } catch (RejectedExecutionException var5) {
                        this.actual.onError(Operators.onRejectedExecution(var5, this.actual.currentContext()));
                     }
                  }

                  return;
               }
            } while(!STATE.compareAndSet(this, s, 2));

         }
      }

      void emitValue() {
         if (this.fusionState == 1) {
            this.fusionState = 2;
         }

         T v = this.value;
         this.clear();
         if (v != null) {
            this.actual.onNext(v);
         }

         if (this.state != 4) {
            this.actual.onComplete();
         }

      }
   }
}
