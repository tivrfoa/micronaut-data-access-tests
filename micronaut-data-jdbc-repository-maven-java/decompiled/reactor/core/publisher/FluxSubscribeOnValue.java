package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.reactivestreams.Subscriber;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.Nullable;

final class FluxSubscribeOnValue<T> extends Flux<T> implements Fuseable, Scannable {
   final T value;
   final Scheduler scheduler;

   FluxSubscribeOnValue(@Nullable T value, Scheduler scheduler) {
      this.value = value;
      this.scheduler = (Scheduler)Objects.requireNonNull(scheduler, "scheduler");
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      T v = this.value;
      if (v == null) {
         FluxSubscribeOnValue.ScheduledEmpty parent = new FluxSubscribeOnValue.ScheduledEmpty(actual);
         actual.onSubscribe(parent);

         try {
            parent.setFuture(this.scheduler.schedule(parent));
         } catch (RejectedExecutionException var5) {
            if (parent.future != OperatorDisposables.DISPOSED) {
               actual.onError(Operators.onRejectedExecution(var5, actual.currentContext()));
            }
         }
      } else {
         actual.onSubscribe(new FluxSubscribeOnValue.ScheduledScalar<>(actual, v, this.scheduler));
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

   static final class ScheduledEmpty implements Fuseable.QueueSubscription<Void>, Runnable {
      final Subscriber<?> actual;
      volatile Disposable future;
      static final AtomicReferenceFieldUpdater<FluxSubscribeOnValue.ScheduledEmpty, Disposable> FUTURE = AtomicReferenceFieldUpdater.newUpdater(
         FluxSubscribeOnValue.ScheduledEmpty.class, Disposable.class, "future"
      );
      static final Disposable FINISHED = Disposables.disposed();

      ScheduledEmpty(Subscriber<?> actual) {
         this.actual = actual;
      }

      @Override
      public void request(long n) {
         Operators.validate(n);
      }

      @Override
      public void cancel() {
         Disposable f = this.future;
         if (f != OperatorDisposables.DISPOSED && f != FINISHED) {
            f = (Disposable)FUTURE.getAndSet(this, OperatorDisposables.DISPOSED);
            if (f != null && f != OperatorDisposables.DISPOSED && f != FINISHED) {
               f.dispose();
            }
         }

      }

      public void run() {
         try {
            this.actual.onComplete();
         } finally {
            FUTURE.lazySet(this, FINISHED);
         }

      }

      void setFuture(Disposable f) {
         if (!FUTURE.compareAndSet(this, null, f)) {
            Disposable a = this.future;
            if (a != FINISHED && a != OperatorDisposables.DISPOSED) {
               f.dispose();
            }
         }

      }

      @Override
      public int requestFusion(int requestedMode) {
         return requestedMode & 2;
      }

      @Nullable
      public Void poll() {
         return null;
      }

      public boolean isEmpty() {
         return true;
      }

      public int size() {
         return 0;
      }

      public void clear() {
      }
   }

   static final class ScheduledScalar<T> implements Fuseable.QueueSubscription<T>, InnerProducer<T>, Runnable {
      final CoreSubscriber<? super T> actual;
      final T value;
      final Scheduler scheduler;
      volatile int once;
      static final AtomicIntegerFieldUpdater<FluxSubscribeOnValue.ScheduledScalar> ONCE = AtomicIntegerFieldUpdater.newUpdater(
         FluxSubscribeOnValue.ScheduledScalar.class, "once"
      );
      volatile Disposable future;
      static final AtomicReferenceFieldUpdater<FluxSubscribeOnValue.ScheduledScalar, Disposable> FUTURE = AtomicReferenceFieldUpdater.newUpdater(
         FluxSubscribeOnValue.ScheduledScalar.class, Disposable.class, "future"
      );
      static final Disposable FINISHED = Disposables.disposed();
      int fusionState;
      static final int NO_VALUE = 1;
      static final int HAS_VALUE = 2;
      static final int COMPLETE = 3;

      ScheduledScalar(CoreSubscriber<? super T> actual, T value, Scheduler scheduler) {
         this.actual = actual;
         this.value = value;
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
            return this.future == OperatorDisposables.DISPOSED;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.future == FINISHED;
         } else if (key == Scannable.Attr.BUFFERED) {
            return 1;
         } else if (key == Scannable.Attr.RUN_ON) {
            return this.scheduler;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : InnerProducer.super.scanUnsafe(key);
         }
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n) && ONCE.compareAndSet(this, 0, 1)) {
            try {
               Disposable f = this.scheduler.schedule(this);
               if (!FUTURE.compareAndSet(this, null, f) && this.future != FINISHED && this.future != OperatorDisposables.DISPOSED) {
                  f.dispose();
               }
            } catch (RejectedExecutionException var4) {
               if (this.future != FINISHED && this.future != OperatorDisposables.DISPOSED) {
                  this.actual.onError(Operators.onRejectedExecution(var4, this, null, this.value, this.actual.currentContext()));
               }
            }
         }

      }

      @Override
      public void cancel() {
         ONCE.lazySet(this, 1);
         Disposable f = this.future;
         if (f != OperatorDisposables.DISPOSED && this.future != FINISHED) {
            f = (Disposable)FUTURE.getAndSet(this, OperatorDisposables.DISPOSED);
            if (f != null && f != OperatorDisposables.DISPOSED && f != FINISHED) {
               f.dispose();
            }
         }

      }

      public void run() {
         try {
            if (this.fusionState == 1) {
               this.fusionState = 2;
            }

            this.actual.onNext(this.value);
            this.actual.onComplete();
         } finally {
            FUTURE.lazySet(this, FINISHED);
         }

      }

      @Override
      public int requestFusion(int requestedMode) {
         if ((requestedMode & 2) != 0) {
            this.fusionState = 1;
            return 2;
         } else {
            return 0;
         }
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

      public boolean isEmpty() {
         return this.fusionState != 2;
      }

      public int size() {
         return this.isEmpty() ? 0 : 1;
      }

      public void clear() {
         this.fusionState = 3;
      }
   }
}
