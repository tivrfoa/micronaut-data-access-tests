package reactor.core.publisher;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoReduceSeed<T, R> extends MonoFromFluxOperator<T, R> implements Fuseable {
   final Supplier<R> initialSupplier;
   final BiFunction<R, ? super T, R> accumulator;

   MonoReduceSeed(Flux<? extends T> source, Supplier<R> initialSupplier, BiFunction<R, ? super T, R> accumulator) {
      super(source);
      this.initialSupplier = (Supplier)Objects.requireNonNull(initialSupplier, "initialSupplier");
      this.accumulator = (BiFunction)Objects.requireNonNull(accumulator, "accumulator");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      R initialValue = (R)Objects.requireNonNull(this.initialSupplier.get(), "The initial value supplied is null");
      return new MonoReduceSeed.ReduceSeedSubscriber<>(actual, this.accumulator, initialValue);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class ReduceSeedSubscriber<T, R> extends Operators.MonoSubscriber<T, R> {
      final BiFunction<R, ? super T, R> accumulator;
      Subscription s;
      boolean done;

      ReduceSeedSubscriber(CoreSubscriber<? super R> actual, BiFunction<R, ? super T, R> accumulator, R value) {
         super(actual);
         this.accumulator = accumulator;
         this.value = value;
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
         super.cancel();
         this.s.cancel();
      }

      @Override
      public void setValue(R value) {
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
         R v = this.value;
         if (v != null) {
            R accumulated;
            try {
               accumulated = (R)Objects.requireNonNull(this.accumulator.apply(v, t), "The accumulator returned a null value");
            } catch (Throwable var5) {
               this.onError(Operators.onOperatorError(this, var5, t, this.actual.currentContext()));
               return;
            }

            if (STATE.get(this) == 4) {
               this.discard(accumulated);
               this.value = null;
            } else {
               this.value = accumulated;
            }
         } else {
            Operators.onDiscard(t, this.actual.currentContext());
         }

      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            this.discard(this.value);
            this.value = null;
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.complete(this.value);
         }
      }
   }
}
