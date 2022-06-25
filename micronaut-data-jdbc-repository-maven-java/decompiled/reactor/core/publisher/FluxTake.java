package reactor.core.publisher;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxTake<T> extends InternalFluxOperator<T, T> {
   final long n;

   FluxTake(Flux<? extends T> source, long n) {
      super(source);
      if (n < 0L) {
         throw new IllegalArgumentException("n >= 0 required but it was " + n);
      } else {
         this.n = n;
      }
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return (CoreSubscriber<? super T>)(actual instanceof Fuseable.ConditionalSubscriber
         ? new FluxTake.TakeConditionalSubscriber<>((Fuseable.ConditionalSubscriber<? super T>)actual, this.n)
         : new FluxTake.TakeSubscriber<>(actual, this.n));
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class TakeConditionalSubscriber<T> implements Fuseable.ConditionalSubscriber<T>, InnerOperator<T, T> {
      final Fuseable.ConditionalSubscriber<? super T> actual;
      final long n;
      long remaining;
      Subscription s;
      boolean done;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxTake.TakeConditionalSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxTake.TakeConditionalSubscriber.class, "wip"
      );

      TakeConditionalSubscriber(Fuseable.ConditionalSubscriber<? super T> actual, long n) {
         this.actual = actual;
         this.n = n;
         this.remaining = n;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            if (this.n == 0L) {
               s.cancel();
               this.done = true;
               Operators.complete(this.actual);
            } else {
               this.s = s;
               this.actual.onSubscribe(this);
            }
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            long r = this.remaining;
            if (r == 0L) {
               this.s.cancel();
               this.onComplete();
            } else {
               this.remaining = --r;
               boolean stop = r == 0L;
               this.actual.onNext(t);
               if (stop) {
                  this.s.cancel();
                  this.onComplete();
               }

            }
         }
      }

      @Override
      public boolean tryOnNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
            return true;
         } else {
            long r = this.remaining;
            if (r == 0L) {
               this.s.cancel();
               this.onComplete();
               return true;
            } else {
               this.remaining = --r;
               boolean stop = r == 0L;
               boolean b = this.actual.tryOnNext(t);
               if (stop) {
                  this.s.cancel();
                  this.onComplete();
               }

               return b;
            }
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.actual.onComplete();
         }
      }

      @Override
      public void request(long n) {
         if (this.wip == 0 && WIP.compareAndSet(this, 0, 1)) {
            if (n >= this.n) {
               this.s.request(Long.MAX_VALUE);
            } else {
               this.s.request(n);
            }

         } else {
            this.s.request(n);
         }
      }

      @Override
      public void cancel() {
         this.s.cancel();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }
   }

   static final class TakeFuseableSubscriber<T> implements Fuseable.QueueSubscription<T>, InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final long n;
      long remaining;
      Fuseable.QueueSubscription<T> qs;
      boolean done;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxTake.TakeFuseableSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxTake.TakeFuseableSubscriber.class, "wip"
      );
      int inputMode;

      TakeFuseableSubscriber(CoreSubscriber<? super T> actual, long n) {
         this.actual = actual;
         this.n = n;
         this.remaining = n;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.qs, s)) {
            if (this.n == 0L) {
               s.cancel();
               this.done = true;
               Operators.complete(this.actual);
            } else {
               this.qs = (Fuseable.QueueSubscription)s;
               this.actual.onSubscribe(this);
            }
         }

      }

      @Override
      public void onNext(T t) {
         if (this.inputMode == 2) {
            this.actual.onNext((T)null);
         } else if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            long r = this.remaining;
            if (r == 0L) {
               this.qs.cancel();
               this.onComplete();
            } else {
               this.remaining = --r;
               boolean stop = r == 0L;
               this.actual.onNext(t);
               if (stop) {
                  this.qs.cancel();
                  this.onComplete();
               }

            }
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.actual.onComplete();
         }
      }

      @Override
      public void request(long n) {
         if (this.wip == 0 && WIP.compareAndSet(this, 0, 1)) {
            if (n >= this.n) {
               this.qs.request(Long.MAX_VALUE);
            } else {
               this.qs.request(n);
            }

         } else {
            this.qs.request(n);
         }
      }

      @Override
      public void cancel() {
         this.qs.cancel();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.PARENT) {
            return this.qs;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public int requestFusion(int requestedMode) {
         int m = this.qs.requestFusion(requestedMode);
         this.inputMode = m;
         return m;
      }

      @Nullable
      public T poll() {
         if (this.done) {
            return null;
         } else {
            long r = this.remaining;
            T v = (T)this.qs.poll();
            if (r == 0L) {
               this.done = true;
               if (this.inputMode == 2) {
                  this.qs.cancel();
                  this.actual.onComplete();
               }

               return null;
            } else {
               if (v != null) {
                  this.remaining = --r;
                  if (r == 0L && !this.done) {
                     this.done = true;
                     if (this.inputMode == 2) {
                        this.qs.cancel();
                        this.actual.onComplete();
                     }
                  }
               }

               return v;
            }
         }
      }

      public boolean isEmpty() {
         return this.remaining == 0L || this.qs.isEmpty();
      }

      public void clear() {
         this.qs.clear();
      }

      public int size() {
         return this.qs.size();
      }
   }

   static final class TakeSubscriber<T> implements InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final long n;
      long remaining;
      Subscription s;
      boolean done;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxTake.TakeSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(FluxTake.TakeSubscriber.class, "wip");

      public TakeSubscriber(CoreSubscriber<? super T> actual, long n) {
         this.actual = actual;
         this.n = n;
         this.remaining = n;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            if (this.n == 0L) {
               s.cancel();
               this.done = true;
               Operators.complete(this.actual);
            } else {
               this.s = s;
               this.actual.onSubscribe(this);
            }
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            long r = this.remaining;
            if (r == 0L) {
               this.s.cancel();
               this.onComplete();
            } else {
               this.remaining = --r;
               boolean stop = r == 0L;
               this.actual.onNext(t);
               if (stop) {
                  this.s.cancel();
                  this.onComplete();
               }

            }
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.actual.onComplete();
         }
      }

      @Override
      public void request(long n) {
         if (this.wip == 0 && WIP.compareAndSet(this, 0, 1)) {
            if (n >= this.n) {
               this.s.request(Long.MAX_VALUE);
            } else {
               this.s.request(n);
            }

         } else {
            this.s.request(n);
         }
      }

      @Override
      public void cancel() {
         this.s.cancel();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }
   }
}
