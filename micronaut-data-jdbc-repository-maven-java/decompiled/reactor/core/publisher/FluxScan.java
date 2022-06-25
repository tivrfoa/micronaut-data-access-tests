package reactor.core.publisher;

import java.util.Objects;
import java.util.function.BiFunction;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxScan<T> extends InternalFluxOperator<T, T> {
   final BiFunction<T, ? super T, T> accumulator;

   FluxScan(Flux<? extends T> source, BiFunction<T, ? super T, T> accumulator) {
      super(source);
      this.accumulator = (BiFunction)Objects.requireNonNull(accumulator, "accumulator");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxScan.ScanSubscriber<>(actual, this.accumulator);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class ScanSubscriber<T> implements InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final BiFunction<T, ? super T, T> accumulator;
      Subscription s;
      T value;
      boolean done;

      ScanSubscriber(CoreSubscriber<? super T> actual, BiFunction<T, ? super T, T> accumulator) {
         this.actual = actual;
         this.accumulator = accumulator;
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
            T v = this.value;
            if (v != null) {
               try {
                  t = (T)Objects.requireNonNull(this.accumulator.apply(v, t), "The accumulator returned a null value");
               } catch (Throwable var4) {
                  this.onError(Operators.onOperatorError(this.s, var4, t, this.actual.currentContext()));
                  return;
               }
            }

            this.value = t;
            this.actual.onNext(t);
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            this.value = null;
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.value = null;
            this.actual.onComplete();
         }
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.value != null ? 1 : 0;
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
