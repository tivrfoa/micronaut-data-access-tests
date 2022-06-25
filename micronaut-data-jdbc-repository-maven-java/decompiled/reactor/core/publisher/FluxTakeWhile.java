package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Predicate;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxTakeWhile<T> extends InternalFluxOperator<T, T> {
   final Predicate<? super T> predicate;

   FluxTakeWhile(Flux<? extends T> source, Predicate<? super T> predicate) {
      super(source);
      this.predicate = (Predicate)Objects.requireNonNull(predicate, "predicate");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxTakeWhile.TakeWhileSubscriber<>(actual, this.predicate);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class TakeWhileSubscriber<T> implements InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final Predicate<? super T> predicate;
      Subscription s;
      boolean done;

      TakeWhileSubscriber(CoreSubscriber<? super T> actual, Predicate<? super T> predicate) {
         this.actual = actual;
         this.predicate = predicate;
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
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            boolean b;
            try {
               b = this.predicate.test(t);
            } catch (Throwable var4) {
               this.onError(Operators.onOperatorError(this.s, var4, t, this.actual.currentContext()));
               return;
            }

            if (!b) {
               this.s.cancel();
               this.onComplete();
            } else {
               this.actual.onNext(t);
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

      @Override
      public void request(long n) {
         this.s.request(n);
      }

      @Override
      public void cancel() {
         this.s.cancel();
      }
   }
}
