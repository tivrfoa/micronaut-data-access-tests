package reactor.core.publisher;

import java.util.Objects;
import java.util.function.BiFunction;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class MonoReduce<T> extends MonoFromFluxOperator<T, T> implements Fuseable {
   final BiFunction<T, T, T> aggregator;

   MonoReduce(Flux<? extends T> source, BiFunction<T, T, T> aggregator) {
      super(source);
      this.aggregator = (BiFunction)Objects.requireNonNull(aggregator, "aggregator");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new MonoReduce.ReduceSubscriber<>(actual, this.aggregator);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class ReduceSubscriber<T> extends Operators.MonoSubscriber<T, T> {
      final BiFunction<T, T, T> aggregator;
      Subscription s;
      boolean done;

      ReduceSubscriber(CoreSubscriber<? super T> actual, BiFunction<T, T, T> aggregator) {
         super(actual);
         this.aggregator = aggregator;
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
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            T r = this.value;
            if (r == null) {
               this.setValue(t);
            } else {
               try {
                  r = (T)Objects.requireNonNull(this.aggregator.apply(r, t), "The aggregator returned a null value");
               } catch (Throwable var5) {
                  this.done = true;
                  Context ctx = this.actual.currentContext();
                  Operators.onDiscard(t, ctx);
                  Operators.onDiscard(this.value, ctx);
                  this.value = null;
                  this.actual.onError(Operators.onOperatorError(this.s, var5, t, this.actual.currentContext()));
                  return;
               }

               this.setValue(r);
            }

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
            T r = this.value;
            if (r != null) {
               this.complete(r);
            } else {
               this.actual.onComplete();
            }

         }
      }

      @Override
      public void cancel() {
         super.cancel();
         this.s.cancel();
      }
   }
}
