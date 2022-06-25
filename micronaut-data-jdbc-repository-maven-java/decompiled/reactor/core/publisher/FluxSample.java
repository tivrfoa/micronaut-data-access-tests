package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxSample<T, U> extends InternalFluxOperator<T, T> {
   final Publisher<U> other;

   FluxSample(Flux<? extends T> source, Publisher<U> other) {
      super(source);
      this.other = (Publisher)Objects.requireNonNull(other, "other");
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      CoreSubscriber<T> serial = Operators.serialize(actual);
      FluxSample.SampleMainSubscriber<T> main = new FluxSample.SampleMainSubscriber<>(serial);
      actual.onSubscribe(main);
      this.other.subscribe(new FluxSample.SampleOther<>(main));
      return main;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class SampleMainSubscriber<T> implements InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final Context ctx;
      volatile T value;
      static final AtomicReferenceFieldUpdater<FluxSample.SampleMainSubscriber, Object> VALUE = AtomicReferenceFieldUpdater.newUpdater(
         FluxSample.SampleMainSubscriber.class, Object.class, "value"
      );
      volatile Subscription main;
      static final AtomicReferenceFieldUpdater<FluxSample.SampleMainSubscriber, Subscription> MAIN = AtomicReferenceFieldUpdater.newUpdater(
         FluxSample.SampleMainSubscriber.class, Subscription.class, "main"
      );
      volatile Subscription other;
      static final AtomicReferenceFieldUpdater<FluxSample.SampleMainSubscriber, Subscription> OTHER = AtomicReferenceFieldUpdater.newUpdater(
         FluxSample.SampleMainSubscriber.class, Subscription.class, "other"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxSample.SampleMainSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxSample.SampleMainSubscriber.class, "requested"
      );

      SampleMainSubscriber(CoreSubscriber<? super T> actual) {
         this.actual = actual;
         this.ctx = actual.currentContext();
      }

      @Override
      public final CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(Scannable.from(this.other));
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.PARENT) {
            return this.main;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.main == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.value != null ? 1 : 0;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (!MAIN.compareAndSet(this, null, s)) {
            s.cancel();
            if (this.main != Operators.cancelledSubscription()) {
               Operators.reportSubscriptionSet();
            }

         } else {
            s.request(Long.MAX_VALUE);
         }
      }

      void cancelMain() {
         Subscription s = this.main;
         if (s != Operators.cancelledSubscription()) {
            s = (Subscription)MAIN.getAndSet(this, Operators.cancelledSubscription());
            if (s != null && s != Operators.cancelledSubscription()) {
               s.cancel();
            }
         }

      }

      void cancelOther() {
         Subscription s = this.other;
         if (s != Operators.cancelledSubscription()) {
            s = (Subscription)OTHER.getAndSet(this, Operators.cancelledSubscription());
            if (s != null && s != Operators.cancelledSubscription()) {
               s.cancel();
            }
         }

      }

      void setOther(Subscription s) {
         if (!OTHER.compareAndSet(this, null, s)) {
            s.cancel();
            if (this.other != Operators.cancelledSubscription()) {
               Operators.reportSubscriptionSet();
            }

         } else {
            s.request(Long.MAX_VALUE);
         }
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
         }

      }

      @Override
      public void cancel() {
         this.cancelMain();
         this.cancelOther();
      }

      @Override
      public void onNext(T t) {
         Object old = VALUE.getAndSet(this, t);
         if (old != null) {
            Operators.onDiscard(old, this.ctx);
         }

      }

      @Override
      public void onError(Throwable t) {
         this.cancelOther();
         this.actual.onError(t);
      }

      @Override
      public void onComplete() {
         this.cancelOther();
         T v = this.value;
         if (v != null) {
            this.actual.onNext(this.value);
         }

         this.actual.onComplete();
      }

      @Nullable
      T getAndNullValue() {
         return (T)VALUE.getAndSet(this, null);
      }

      void decrement() {
         REQUESTED.decrementAndGet(this);
      }
   }

   static final class SampleOther<T, U> implements InnerConsumer<U> {
      final FluxSample.SampleMainSubscriber<T> main;

      SampleOther(FluxSample.SampleMainSubscriber<T> main) {
         this.main = main;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.main.other;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.main;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.main.other == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.PREFETCH) {
            return Integer.MAX_VALUE;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public Context currentContext() {
         return this.main.currentContext();
      }

      @Override
      public void onSubscribe(Subscription s) {
         this.main.setOther(s);
      }

      @Override
      public void onNext(U t) {
         FluxSample.SampleMainSubscriber<T> m = this.main;
         T v = m.getAndNullValue();
         if (v != null) {
            if (m.requested != 0L) {
               m.actual.onNext(v);
               if (m.requested != Long.MAX_VALUE) {
                  m.decrement();
               }

               return;
            }

            m.cancel();
            m.actual.onError(Exceptions.failWithOverflow("Can't signal value due to lack of requests"));
            Operators.onDiscard(v, m.ctx);
         }

      }

      @Override
      public void onError(Throwable t) {
         FluxSample.SampleMainSubscriber<T> m = this.main;
         m.cancelMain();
         m.actual.onError(t);
      }

      @Override
      public void onComplete() {
         FluxSample.SampleMainSubscriber<T> m = this.main;
         m.cancelMain();
         m.actual.onComplete();
      }
   }
}
