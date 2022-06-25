package reactor.core.publisher;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class ParallelReduceSeed<T, R> extends ParallelFlux<R> implements Scannable, Fuseable {
   final ParallelFlux<? extends T> source;
   final Supplier<R> initialSupplier;
   final BiFunction<R, ? super T, R> reducer;

   ParallelReduceSeed(ParallelFlux<? extends T> source, Supplier<R> initialSupplier, BiFunction<R, ? super T, R> reducer) {
      this.source = source;
      this.initialSupplier = initialSupplier;
      this.reducer = reducer;
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else if (key == Scannable.Attr.PREFETCH) {
         return this.getPrefetch();
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Override
   public void subscribe(CoreSubscriber<? super R>[] subscribers) {
      if (this.validate(subscribers)) {
         int n = subscribers.length;
         CoreSubscriber<T>[] parents = new CoreSubscriber[n];

         for(int i = 0; i < n; ++i) {
            R initialValue;
            try {
               initialValue = (R)Objects.requireNonNull(this.initialSupplier.get(), "The initialSupplier returned a null value");
            } catch (Throwable var7) {
               this.reportError(subscribers, Operators.onOperatorError(var7, subscribers[i].currentContext()));
               return;
            }

            parents[i] = new ParallelReduceSeed.ParallelReduceSeedSubscriber<>(subscribers[i], initialValue, this.reducer);
         }

         this.source.subscribe(parents);
      }
   }

   void reportError(Subscriber<?>[] subscribers, Throwable ex) {
      for(Subscriber<?> s : subscribers) {
         Operators.error(s, ex);
      }

   }

   @Override
   public int parallelism() {
      return this.source.parallelism();
   }

   static final class ParallelReduceSeedSubscriber<T, R> extends Operators.MonoSubscriber<T, R> {
      final BiFunction<R, ? super T, R> reducer;
      R accumulator;
      Subscription s;
      boolean done;

      ParallelReduceSeedSubscriber(CoreSubscriber<? super R> subscriber, R initialValue, BiFunction<R, ? super T, R> reducer) {
         super(subscriber);
         this.accumulator = initialValue;
         this.reducer = reducer;
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
            R v;
            try {
               v = (R)Objects.requireNonNull(this.reducer.apply(this.accumulator, t), "The reducer returned a null value");
            } catch (Throwable var4) {
               this.onError(Operators.onOperatorError(this, var4, t, this.actual.currentContext()));
               return;
            }

            this.accumulator = v;
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            this.accumulator = null;
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            R a = this.accumulator;
            this.accumulator = null;
            this.complete(a);
         }
      }

      @Override
      public void cancel() {
         super.cancel();
         this.s.cancel();
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
      }
   }
}
