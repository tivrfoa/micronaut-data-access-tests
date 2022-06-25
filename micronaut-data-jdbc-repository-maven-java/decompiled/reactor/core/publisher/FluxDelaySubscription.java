package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Consumer;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxDelaySubscription<T, U> extends InternalFluxOperator<T, T> implements Consumer<FluxDelaySubscription.DelaySubscriptionOtherSubscriber<T, U>> {
   final Publisher<U> other;

   FluxDelaySubscription(Flux<? extends T> source, Publisher<U> other) {
      super(source);
      this.other = (Publisher)Objects.requireNonNull(other, "other");
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      this.other.subscribe(new FluxDelaySubscription.DelaySubscriptionOtherSubscriber<>(actual, this));
      return null;
   }

   public void accept(FluxDelaySubscription.DelaySubscriptionOtherSubscriber<T, U> s) {
      this.source.subscribe(new FluxDelaySubscription.DelaySubscriptionMainSubscriber<>(s.actual, s));
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class DelaySubscriptionMainSubscriber<T> implements InnerConsumer<T> {
      final CoreSubscriber<? super T> actual;
      final FluxDelaySubscription.DelaySubscriptionOtherSubscriber<?, ?> arbiter;

      DelaySubscriptionMainSubscriber(CoreSubscriber<? super T> actual, FluxDelaySubscription.DelaySubscriptionOtherSubscriber<?, ?> arbiter) {
         this.actual = actual;
         this.arbiter = arbiter;
      }

      @Override
      public Context currentContext() {
         return this.arbiter.currentContext();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.ACTUAL) {
            return this.actual;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         this.arbiter.set(s);
      }

      @Override
      public void onNext(T t) {
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         this.actual.onError(t);
      }

      @Override
      public void onComplete() {
         this.actual.onComplete();
      }
   }

   static final class DelaySubscriptionOtherSubscriber<T, U> extends Operators.DeferredSubscription implements InnerOperator<U, T> {
      final Consumer<FluxDelaySubscription.DelaySubscriptionOtherSubscriber<T, U>> source;
      final CoreSubscriber<? super T> actual;
      Subscription s;
      boolean done;

      DelaySubscriptionOtherSubscriber(CoreSubscriber<? super T> actual, Consumer<FluxDelaySubscription.DelaySubscriptionOtherSubscriber<T, U>> source) {
         this.actual = actual;
         this.source = source;
      }

      @Override
      public Context currentContext() {
         return this.actual.currentContext();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.actual;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public void cancel() {
         this.s.cancel();
         super.cancel();
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
      public void onNext(U t) {
         if (!this.done) {
            this.done = true;
            this.s.cancel();
            this.source.accept(this);
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
            this.source.accept(this);
         }
      }
   }
}
