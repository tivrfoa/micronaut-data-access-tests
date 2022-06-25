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

final class FluxSkipUntilOther<T, U> extends InternalFluxOperator<T, T> {
   final Publisher<U> other;

   FluxSkipUntilOther(Flux<? extends T> source, Publisher<U> other) {
      super(source);
      this.other = (Publisher)Objects.requireNonNull(other, "other");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      FluxSkipUntilOther.SkipUntilMainSubscriber<T> mainSubscriber = new FluxSkipUntilOther.SkipUntilMainSubscriber<>(actual);
      FluxSkipUntilOther.SkipUntilOtherSubscriber<U> otherSubscriber = new FluxSkipUntilOther.SkipUntilOtherSubscriber<>(mainSubscriber);
      this.other.subscribe(otherSubscriber);
      return mainSubscriber;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class SkipUntilMainSubscriber<T> implements InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final Context ctx;
      volatile Subscription main;
      static final AtomicReferenceFieldUpdater<FluxSkipUntilOther.SkipUntilMainSubscriber, Subscription> MAIN = AtomicReferenceFieldUpdater.newUpdater(
         FluxSkipUntilOther.SkipUntilMainSubscriber.class, Subscription.class, "main"
      );
      volatile Subscription other;
      static final AtomicReferenceFieldUpdater<FluxSkipUntilOther.SkipUntilMainSubscriber, Subscription> OTHER = AtomicReferenceFieldUpdater.newUpdater(
         FluxSkipUntilOther.SkipUntilMainSubscriber.class, Subscription.class, "other"
      );
      volatile boolean gate;

      SkipUntilMainSubscriber(CoreSubscriber<? super T> actual) {
         this.actual = Operators.serialize(actual);
         this.ctx = actual.currentContext();
      }

      @Override
      public final CoreSubscriber<? super T> actual() {
         return this.actual;
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

      @Override
      public void cancel() {
         Operators.terminate(MAIN, this);
         Operators.terminate(OTHER, this);
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
         if (this.gate) {
            this.actual.onNext(t);
         } else {
            Operators.onDiscard(t, this.ctx);
            this.main.request(1L);
         }

      }

      @Override
      public void onError(Throwable t) {
         if (MAIN.compareAndSet(this, null, Operators.cancelledSubscription())) {
            Operators.error(this.actual, t);
         } else if (this.main == Operators.cancelledSubscription()) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.cancel();
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         Operators.terminate(OTHER, this);
         this.actual.onComplete();
      }
   }

   static final class SkipUntilOtherSubscriber<U> implements InnerConsumer<U> {
      final FluxSkipUntilOther.SkipUntilMainSubscriber<?> main;

      SkipUntilOtherSubscriber(FluxSkipUntilOther.SkipUntilMainSubscriber<?> main) {
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
         if (!this.main.gate) {
            FluxSkipUntilOther.SkipUntilMainSubscriber<?> m = this.main;
            m.other.cancel();
            m.gate = true;
            m.other = Operators.cancelledSubscription();
         }
      }

      @Override
      public void onError(Throwable t) {
         FluxSkipUntilOther.SkipUntilMainSubscriber<?> m = this.main;
         if (m.gate) {
            Operators.onErrorDropped(t, this.main.currentContext());
         } else {
            m.onError(t);
         }
      }

      @Override
      public void onComplete() {
         FluxSkipUntilOther.SkipUntilMainSubscriber<?> m = this.main;
         if (!m.gate) {
            m.gate = true;
            m.other = Operators.cancelledSubscription();
         }
      }
   }
}
