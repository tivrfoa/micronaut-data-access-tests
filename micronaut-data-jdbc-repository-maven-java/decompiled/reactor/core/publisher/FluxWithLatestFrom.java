package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxWithLatestFrom<T, U, R> extends InternalFluxOperator<T, R> {
   final Publisher<? extends U> other;
   final BiFunction<? super T, ? super U, ? extends R> combiner;

   FluxWithLatestFrom(Flux<? extends T> source, Publisher<? extends U> other, BiFunction<? super T, ? super U, ? extends R> combiner) {
      super(source);
      this.other = (Publisher)Objects.requireNonNull(other, "other");
      this.combiner = (BiFunction)Objects.requireNonNull(combiner, "combiner");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      CoreSubscriber<R> serial = Operators.serialize(actual);
      FluxWithLatestFrom.WithLatestFromSubscriber<T, U, R> main = new FluxWithLatestFrom.WithLatestFromSubscriber<>(serial, this.combiner);
      FluxWithLatestFrom.WithLatestFromOtherSubscriber<U> secondary = new FluxWithLatestFrom.WithLatestFromOtherSubscriber<>(main);
      this.other.subscribe(secondary);
      return main;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class WithLatestFromOtherSubscriber<U> implements InnerConsumer<U> {
      final FluxWithLatestFrom.WithLatestFromSubscriber<?, U, ?> main;

      WithLatestFromOtherSubscriber(FluxWithLatestFrom.WithLatestFromSubscriber<?, U, ?> main) {
         this.main = main;
      }

      @Override
      public void onSubscribe(Subscription s) {
         this.main.setOther(s);
         s.request(Long.MAX_VALUE);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.ACTUAL) {
            return this.main;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public Context currentContext() {
         return this.main.currentContext();
      }

      @Override
      public void onNext(U t) {
         this.main.otherValue = t;
      }

      @Override
      public void onError(Throwable t) {
         this.main.otherError(t);
      }

      @Override
      public void onComplete() {
         this.main.otherComplete();
      }
   }

   static final class WithLatestFromSubscriber<T, U, R> implements InnerOperator<T, R> {
      final CoreSubscriber<? super R> actual;
      final BiFunction<? super T, ? super U, ? extends R> combiner;
      volatile Subscription main;
      static final AtomicReferenceFieldUpdater<FluxWithLatestFrom.WithLatestFromSubscriber, Subscription> MAIN = AtomicReferenceFieldUpdater.newUpdater(
         FluxWithLatestFrom.WithLatestFromSubscriber.class, Subscription.class, "main"
      );
      volatile Subscription other;
      static final AtomicReferenceFieldUpdater<FluxWithLatestFrom.WithLatestFromSubscriber, Subscription> OTHER = AtomicReferenceFieldUpdater.newUpdater(
         FluxWithLatestFrom.WithLatestFromSubscriber.class, Subscription.class, "other"
      );
      volatile U otherValue;

      WithLatestFromSubscriber(CoreSubscriber<? super R> actual, BiFunction<? super T, ? super U, ? extends R> combiner) {
         this.actual = actual;
         this.combiner = combiner;
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
      public CoreSubscriber<? super R> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.main == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.PARENT) {
            return this.main;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(Scannable.from(this.other));
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
         U u = this.otherValue;
         if (u != null) {
            R r;
            try {
               r = (R)Objects.requireNonNull(this.combiner.apply(t, u), "The combiner returned a null value");
            } catch (Throwable var5) {
               this.onError(Operators.onOperatorError(this, var5, t, this.actual.currentContext()));
               return;
            }

            this.actual.onNext(r);
         } else {
            this.main.request(1L);
         }

      }

      @Override
      public void onError(Throwable t) {
         if (this.main == null && MAIN.compareAndSet(this, null, Operators.cancelledSubscription())) {
            this.cancelOther();
            Operators.error(this.actual, t);
         } else {
            this.cancelOther();
            this.otherValue = null;
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         this.cancelOther();
         this.otherValue = null;
         this.actual.onComplete();
      }

      void otherError(Throwable t) {
         if (this.main == null && MAIN.compareAndSet(this, null, Operators.cancelledSubscription())) {
            this.cancelMain();
            Operators.error(this.actual, t);
         } else {
            this.cancelMain();
            this.otherValue = null;
            this.actual.onError(t);
         }
      }

      void otherComplete() {
         if (this.otherValue == null) {
            if (this.main == null && MAIN.compareAndSet(this, null, Operators.cancelledSubscription())) {
               this.cancelMain();
               Operators.complete(this.actual);
               return;
            }

            this.cancelMain();
            this.actual.onComplete();
         }

      }
   }
}
