package reactor.core.publisher;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxOnBackpressureLatest<T> extends InternalFluxOperator<T, T> {
   FluxOnBackpressureLatest(Flux<? extends T> source) {
      super(source);
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxOnBackpressureLatest.LatestSubscriber<>(actual);
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class LatestSubscriber<T> implements InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final Context ctx;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxOnBackpressureLatest.LatestSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxOnBackpressureLatest.LatestSubscriber.class, "requested"
      );
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxOnBackpressureLatest.LatestSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxOnBackpressureLatest.LatestSubscriber.class, "wip"
      );
      Subscription s;
      Throwable error;
      volatile boolean done;
      volatile boolean cancelled;
      volatile T value;
      static final AtomicReferenceFieldUpdater<FluxOnBackpressureLatest.LatestSubscriber, Object> VALUE = AtomicReferenceFieldUpdater.newUpdater(
         FluxOnBackpressureLatest.LatestSubscriber.class, Object.class, "value"
      );

      LatestSubscriber(CoreSubscriber<? super T> actual) {
         this.actual = actual;
         this.ctx = actual.currentContext();
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
            this.drain();
         }

      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            this.s.cancel();
            if (WIP.getAndIncrement(this) == 0) {
               Object toDiscard = VALUE.getAndSet(this, null);
               if (toDiscard != null) {
                  Operators.onDiscard(toDiscard, this.ctx);
               }
            }
         }

      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
         Object toDiscard = VALUE.getAndSet(this, t);
         if (toDiscard != null) {
            Operators.onDiscard(toDiscard, this.ctx);
         }

         this.drain();
      }

      @Override
      public void onError(Throwable t) {
         this.error = t;
         this.done = true;
         this.drain();
      }

      @Override
      public void onComplete() {
         this.done = true;
         this.drain();
      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            Subscriber<? super T> a = this.actual;
            int missed = 1;

            while(!this.checkTerminated(this.done, this.value == null, a)) {
               long r = this.requested;

               long e;
               for(e = 0L; r != e; ++e) {
                  boolean d = this.done;
                  T v = (T)VALUE.getAndSet(this, null);
                  boolean empty = v == null;
                  if (this.checkTerminated(d, empty, a)) {
                     return;
                  }

                  if (empty) {
                     break;
                  }

                  a.onNext(v);
               }

               if (r == e && this.checkTerminated(this.done, this.value == null, a)) {
                  return;
               }

               if (e != 0L && r != Long.MAX_VALUE) {
                  Operators.produced(REQUESTED, this, e);
               }

               missed = WIP.addAndGet(this, -missed);
               if (missed == 0) {
                  return;
               }
            }

         }
      }

      boolean checkTerminated(boolean d, boolean empty, Subscriber<? super T> a) {
         if (this.cancelled) {
            Object toDiscard = VALUE.getAndSet(this, null);
            if (toDiscard != null) {
               Operators.onDiscard(toDiscard, this.ctx);
            }

            return true;
         } else {
            if (d) {
               Throwable e = this.error;
               if (e != null) {
                  Object toDiscard = VALUE.getAndSet(this, null);
                  if (toDiscard != null) {
                     Operators.onDiscard(toDiscard, this.ctx);
                  }

                  a.onError(e);
                  return true;
               }

               if (empty) {
                  a.onComplete();
                  return true;
               }
            }

            return false;
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.value != null ? 1 : 0;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.PREFETCH) {
            return Integer.MAX_VALUE;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }
   }
}
