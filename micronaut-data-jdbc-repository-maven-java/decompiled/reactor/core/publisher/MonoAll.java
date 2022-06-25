package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Predicate;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoAll<T> extends MonoFromFluxOperator<T, Boolean> implements Fuseable {
   final Predicate<? super T> predicate;

   MonoAll(Flux<? extends T> source, Predicate<? super T> predicate) {
      super(source);
      this.predicate = (Predicate)Objects.requireNonNull(predicate, "predicate");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super Boolean> actual) {
      return new MonoAll.AllSubscriber<>(actual, this.predicate);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class AllSubscriber<T> extends Operators.MonoSubscriber<T, Boolean> {
      final Predicate<? super T> predicate;
      Subscription s;
      boolean done;

      AllSubscriber(CoreSubscriber<? super Boolean> actual, Predicate<? super T> predicate) {
         super(actual);
         this.predicate = predicate;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         }
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
      public void onNext(T t) {
         if (!this.done) {
            boolean b;
            try {
               b = this.predicate.test(t);
            } catch (Throwable var4) {
               this.done = true;
               this.actual.onError(Operators.onOperatorError(this.s, var4, t, this.actual.currentContext()));
               return;
            }

            if (!b) {
               this.done = true;
               this.s.cancel();
               this.complete(Boolean.valueOf(false));
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
            this.complete(Boolean.valueOf(true));
         }
      }
   }
}
