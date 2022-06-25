package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxTakeUntilOther<T, U> extends InternalFluxOperator<T, T> {
   final Publisher<U> other;

   FluxTakeUntilOther(Flux<? extends T> source, Publisher<U> other) {
      super(source);
      this.other = (Publisher)Objects.requireNonNull(other, "other");
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      FluxTakeUntilOther.TakeUntilMainSubscriber<T> mainSubscriber = new FluxTakeUntilOther.TakeUntilMainSubscriber<>(actual);
      FluxTakeUntilOther.TakeUntilOtherSubscriber<U> otherSubscriber = new FluxTakeUntilOther.TakeUntilOtherSubscriber<>(mainSubscriber);
      this.other.subscribe(otherSubscriber);
      return mainSubscriber;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class TakeUntilMainSubscriber<T> implements InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      volatile Subscription main;
      static final AtomicReferenceFieldUpdater<FluxTakeUntilOther.TakeUntilMainSubscriber, Subscription> MAIN = AtomicReferenceFieldUpdater.newUpdater(
         FluxTakeUntilOther.TakeUntilMainSubscriber.class, Subscription.class, "main"
      );
      volatile Subscription other;
      static final AtomicReferenceFieldUpdater<FluxTakeUntilOther.TakeUntilMainSubscriber, Subscription> OTHER = AtomicReferenceFieldUpdater.newUpdater(
         FluxTakeUntilOther.TakeUntilMainSubscriber.class, Subscription.class, "other"
      );

      TakeUntilMainSubscriber(CoreSubscriber<? super T> actual) {
         this.actual = Operators.serialize(actual);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.main;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.main == Operators.cancelledSubscription();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public final CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(Scannable.from(this.other));
      }

      void setOther(Subscription s) {
         if (!OTHER.compareAndSet(this, null, s)) {
            s.cancel();
            if (this.other != Operators.cancelledSubscription()) {
               Operators.reportSubscriptionSet();
            }
         }

      }

      @Override
      public void request(long n) {
         this.main.request(n);
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

      @Override
      public void cancel() {
         this.cancelMain();
         this.cancelOther();
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (!MAIN.compareAndSet(this, null, s)) {
            s.cancel();
            if (this.main != Operators.cancelledSubscription()) {
               Operators.reportSubscriptionSet();
            }
         } else {
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         if (this.main == null && MAIN.compareAndSet(this, null, Operators.cancelledSubscription())) {
            Operators.error(this.actual, t);
         } else {
            this.cancel();
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (this.main == null && MAIN.compareAndSet(this, null, Operators.cancelledSubscription())) {
            this.cancelOther();
            Operators.complete(this.actual);
         } else {
            this.cancel();
            this.actual.onComplete();
         }
      }
   }

   static final class TakeUntilOtherSubscriber<U> implements InnerConsumer<U> {
      final FluxTakeUntilOther.TakeUntilMainSubscriber<?> main;
      boolean once;

      TakeUntilOtherSubscriber(FluxTakeUntilOther.TakeUntilMainSubscriber<?> main) {
         this.main = main;
      }

      @Override
      public Context currentContext() {
         return this.main.currentContext();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.main.other == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.PARENT) {
            return this.main.other;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.main;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         this.main.setOther(s);
         s.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(U t) {
         this.onComplete();
      }

      @Override
      public void onError(Throwable t) {
         if (!this.once) {
            this.once = true;
            this.main.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.once) {
            this.once = true;
            this.main.onComplete();
         }
      }
   }
}
