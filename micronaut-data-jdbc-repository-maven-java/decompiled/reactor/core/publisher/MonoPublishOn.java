package reactor.core.publisher;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.Nullable;

final class MonoPublishOn<T> extends InternalMonoOperator<T, T> {
   final Scheduler scheduler;

   MonoPublishOn(Mono<? extends T> source, Scheduler scheduler) {
      super(source);
      this.scheduler = scheduler;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new MonoPublishOn.PublishOnSubscriber<>(actual, this.scheduler);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.RUN_ON) {
         return this.scheduler;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : super.scanUnsafe(key);
      }
   }

   static final class PublishOnSubscriber<T> implements InnerOperator<T, T>, Runnable {
      final CoreSubscriber<? super T> actual;
      final Scheduler scheduler;
      Subscription s;
      volatile Disposable future;
      static final AtomicReferenceFieldUpdater<MonoPublishOn.PublishOnSubscriber, Disposable> FUTURE = AtomicReferenceFieldUpdater.newUpdater(
         MonoPublishOn.PublishOnSubscriber.class, Disposable.class, "future"
      );
      volatile T value;
      static final AtomicReferenceFieldUpdater<MonoPublishOn.PublishOnSubscriber, Object> VALUE = AtomicReferenceFieldUpdater.newUpdater(
         MonoPublishOn.PublishOnSubscriber.class, Object.class, "value"
      );
      volatile Throwable error;

      PublishOnSubscriber(CoreSubscriber<? super T> actual, Scheduler scheduler) {
         this.actual = actual;
         this.scheduler = scheduler;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.future == OperatorDisposables.DISPOSED;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.RUN_ON) {
            return this.scheduler;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         this.value = t;
         this.trySchedule(this, null, t);
      }

      @Override
      public void onError(Throwable t) {
         this.error = t;
         this.trySchedule(null, t, null);
      }

      @Override
      public void onComplete() {
         if (this.value == null) {
            this.trySchedule(null, null, null);
         }

      }

      void trySchedule(@Nullable Subscription subscription, @Nullable Throwable suppressed, @Nullable Object dataSignal) {
         if (this.future == null) {
            try {
               this.future = this.scheduler.schedule(this);
            } catch (RejectedExecutionException var5) {
               this.actual.onError(Operators.onRejectedExecution(var5, subscription, suppressed, dataSignal, this.actual.currentContext()));
            }

         }
      }

      @Override
      public void request(long n) {
         this.s.request(n);
      }

      @Override
      public void cancel() {
         Disposable c = this.future;
         if (c != OperatorDisposables.DISPOSED) {
            c = (Disposable)FUTURE.getAndSet(this, OperatorDisposables.DISPOSED);
            if (c != null && !OperatorDisposables.isDisposed(c)) {
               c.dispose();
            }

            this.value = null;
         }

         this.s.cancel();
      }

      public void run() {
         if (!OperatorDisposables.isDisposed(this.future)) {
            T v = (T)VALUE.getAndSet(this, null);
            if (v != null) {
               this.actual.onNext(v);
               this.actual.onComplete();
            } else {
               Throwable e = this.error;
               if (e != null) {
                  this.actual.onError(e);
               } else {
                  this.actual.onComplete();
               }
            }

         }
      }
   }
}
