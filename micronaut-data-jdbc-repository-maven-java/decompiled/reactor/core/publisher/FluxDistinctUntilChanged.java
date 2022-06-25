package reactor.core.publisher;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxDistinctUntilChanged<T, K> extends InternalFluxOperator<T, T> {
   final Function<? super T, K> keyExtractor;
   final BiPredicate<? super K, ? super K> keyComparator;

   FluxDistinctUntilChanged(Flux<? extends T> source, Function<? super T, K> keyExtractor, BiPredicate<? super K, ? super K> keyComparator) {
      super(source);
      this.keyExtractor = (Function)Objects.requireNonNull(keyExtractor, "keyExtractor");
      this.keyComparator = (BiPredicate)Objects.requireNonNull(keyComparator, "keyComparator");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return (CoreSubscriber<? super T>)(actual instanceof Fuseable.ConditionalSubscriber
         ? new FluxDistinctUntilChanged.DistinctUntilChangedConditionalSubscriber<>(
            (Fuseable.ConditionalSubscriber<? super T>)actual, this.keyExtractor, this.keyComparator
         )
         : new FluxDistinctUntilChanged.DistinctUntilChangedSubscriber<>(actual, this.keyExtractor, this.keyComparator));
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class DistinctUntilChangedConditionalSubscriber<T, K> implements Fuseable.ConditionalSubscriber<T>, InnerOperator<T, T> {
      final Fuseable.ConditionalSubscriber<? super T> actual;
      final Context ctx;
      final Function<? super T, K> keyExtractor;
      final BiPredicate<? super K, ? super K> keyComparator;
      Subscription s;
      boolean done;
      @Nullable
      K lastKey;

      DistinctUntilChangedConditionalSubscriber(
         Fuseable.ConditionalSubscriber<? super T> actual, Function<? super T, K> keyExtractor, BiPredicate<? super K, ? super K> keyComparator
      ) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.keyExtractor = keyExtractor;
         this.keyComparator = keyComparator;
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
         if (!this.tryOnNext(t)) {
            this.s.request(1L);
         }

      }

      @Override
      public boolean tryOnNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.ctx);
            return true;
         } else {
            K k;
            try {
               k = (K)Objects.requireNonNull(this.keyExtractor.apply(t), "The distinct extractor returned a null value.");
            } catch (Throwable var6) {
               this.onError(Operators.onOperatorError(this.s, var6, t, this.ctx));
               Operators.onDiscard(t, this.ctx);
               return true;
            }

            if (null == this.lastKey) {
               this.lastKey = k;
               return this.actual.tryOnNext(t);
            } else {
               boolean equiv;
               try {
                  equiv = this.keyComparator.test(this.lastKey, k);
               } catch (Throwable var5) {
                  this.onError(Operators.onOperatorError(this.s, var5, t, this.ctx));
                  Operators.onDiscard(t, this.ctx);
                  return true;
               }

               if (equiv) {
                  Operators.onDiscard(t, this.ctx);
                  return false;
               } else {
                  this.lastKey = k;
                  return this.actual.tryOnNext(t);
               }
            }
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.ctx);
         } else {
            this.done = true;
            this.lastKey = null;
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.lastKey = null;
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
         this.lastKey = null;
      }
   }

   static final class DistinctUntilChangedSubscriber<T, K> implements Fuseable.ConditionalSubscriber<T>, InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final Context ctx;
      final Function<? super T, K> keyExtractor;
      final BiPredicate<? super K, ? super K> keyComparator;
      Subscription s;
      boolean done;
      @Nullable
      K lastKey;

      DistinctUntilChangedSubscriber(CoreSubscriber<? super T> actual, Function<? super T, K> keyExtractor, BiPredicate<? super K, ? super K> keyComparator) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.keyExtractor = keyExtractor;
         this.keyComparator = keyComparator;
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
         if (!this.tryOnNext(t)) {
            this.s.request(1L);
         }

      }

      @Override
      public boolean tryOnNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.ctx);
            return true;
         } else {
            K k;
            try {
               k = (K)Objects.requireNonNull(this.keyExtractor.apply(t), "The distinct extractor returned a null value.");
            } catch (Throwable var6) {
               this.onError(Operators.onOperatorError(this.s, var6, t, this.ctx));
               Operators.onDiscard(t, this.ctx);
               return true;
            }

            if (null == this.lastKey) {
               this.lastKey = k;
               this.actual.onNext(t);
               return true;
            } else {
               boolean equiv;
               try {
                  equiv = this.keyComparator.test(this.lastKey, k);
               } catch (Throwable var5) {
                  this.onError(Operators.onOperatorError(this.s, var5, t, this.ctx));
                  Operators.onDiscard(t, this.ctx);
                  return true;
               }

               if (equiv) {
                  Operators.onDiscard(t, this.ctx);
                  return false;
               } else {
                  this.lastKey = k;
                  this.actual.onNext(t);
                  return true;
               }
            }
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.ctx);
         } else {
            this.done = true;
            this.lastKey = null;
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.lastKey = null;
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
         this.lastKey = null;
      }
   }
}
