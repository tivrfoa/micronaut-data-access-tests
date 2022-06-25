package reactor.core.publisher;

import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxDistinct<T, K, C> extends InternalFluxOperator<T, T> {
   final Function<? super T, ? extends K> keyExtractor;
   final Supplier<C> collectionSupplier;
   final BiPredicate<C, K> distinctPredicate;
   final Consumer<C> cleanupCallback;

   FluxDistinct(
      Flux<? extends T> source,
      Function<? super T, ? extends K> keyExtractor,
      Supplier<C> collectionSupplier,
      BiPredicate<C, K> distinctPredicate,
      Consumer<C> cleanupCallback
   ) {
      super(source);
      this.keyExtractor = (Function)Objects.requireNonNull(keyExtractor, "keyExtractor");
      this.collectionSupplier = (Supplier)Objects.requireNonNull(collectionSupplier, "collectionSupplier");
      this.distinctPredicate = (BiPredicate)Objects.requireNonNull(distinctPredicate, "distinctPredicate");
      this.cleanupCallback = (Consumer)Objects.requireNonNull(cleanupCallback, "cleanupCallback");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      C collection = (C)Objects.requireNonNull(this.collectionSupplier.get(), "The collectionSupplier returned a null collection");
      return (CoreSubscriber<? super T>)(actual instanceof Fuseable.ConditionalSubscriber
         ? new FluxDistinct.DistinctConditionalSubscriber<>(
            (Fuseable.ConditionalSubscriber<? super T>)actual, collection, this.keyExtractor, this.distinctPredicate, this.cleanupCallback
         )
         : new FluxDistinct.DistinctSubscriber<>(actual, collection, this.keyExtractor, this.distinctPredicate, this.cleanupCallback));
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class DistinctConditionalSubscriber<T, K, C> implements Fuseable.ConditionalSubscriber<T>, InnerOperator<T, T> {
      final Fuseable.ConditionalSubscriber<? super T> actual;
      final Context ctx;
      final C collection;
      final Function<? super T, ? extends K> keyExtractor;
      final BiPredicate<C, K> distinctPredicate;
      final Consumer<C> cleanupCallback;
      Subscription s;
      boolean done;

      DistinctConditionalSubscriber(
         Fuseable.ConditionalSubscriber<? super T> actual,
         C collection,
         Function<? super T, ? extends K> keyExtractor,
         BiPredicate<C, K> distinctPredicate,
         Consumer<C> cleanupCallback
      ) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.collection = collection;
         this.keyExtractor = keyExtractor;
         this.distinctPredicate = distinctPredicate;
         this.cleanupCallback = cleanupCallback;
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
            Operators.onNextDropped(t, this.ctx);
         } else {
            K k;
            try {
               k = (K)Objects.requireNonNull(this.keyExtractor.apply(t), "The distinct extractor returned a null value.");
            } catch (Throwable var6) {
               this.onError(Operators.onOperatorError(this.s, var6, t, this.ctx));
               Operators.onDiscard(t, this.ctx);
               return;
            }

            boolean b;
            try {
               b = this.distinctPredicate.test(this.collection, k);
            } catch (Throwable var5) {
               this.onError(Operators.onOperatorError(this.s, var5, t, this.ctx));
               Operators.onDiscard(t, this.ctx);
               return;
            }

            if (b) {
               this.actual.onNext(t);
            } else {
               Operators.onDiscard(t, this.ctx);
               this.s.request(1L);
            }

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

            boolean b;
            try {
               b = this.distinctPredicate.test(this.collection, k);
            } catch (Throwable var5) {
               this.onError(Operators.onOperatorError(this.s, var5, t, this.ctx));
               Operators.onDiscard(t, this.ctx);
               return true;
            }

            if (b) {
               return this.actual.tryOnNext(t);
            } else {
               Operators.onDiscard(t, this.ctx);
               return false;
            }
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.ctx);
         } else {
            this.done = true;
            this.cleanupCallback.accept(this.collection);
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.cleanupCallback.accept(this.collection);
            this.actual.onComplete();
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
         if (this.collection != null) {
            this.cleanupCallback.accept(this.collection);
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
   }

   static final class DistinctFuseableSubscriber<T, K, C> implements Fuseable.ConditionalSubscriber<T>, InnerOperator<T, T>, Fuseable.QueueSubscription<T> {
      final CoreSubscriber<? super T> actual;
      final Context ctx;
      final C collection;
      final Function<? super T, ? extends K> keyExtractor;
      final BiPredicate<C, K> distinctPredicate;
      final Consumer<C> cleanupCallback;
      Fuseable.QueueSubscription<T> qs;
      boolean done;
      int sourceMode;

      DistinctFuseableSubscriber(
         CoreSubscriber<? super T> actual, C collection, Function<? super T, ? extends K> keyExtractor, BiPredicate<C, K> predicate, Consumer<C> callback
      ) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.collection = collection;
         this.keyExtractor = keyExtractor;
         this.distinctPredicate = predicate;
         this.cleanupCallback = callback;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.qs, s)) {
            this.qs = (Fuseable.QueueSubscription)s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         if (!this.tryOnNext(t)) {
            this.qs.request(1L);
         }

      }

      @Override
      public boolean tryOnNext(T t) {
         if (this.sourceMode == 2) {
            this.actual.onNext((T)null);
            return true;
         } else if (this.done) {
            Operators.onNextDropped(t, this.ctx);
            return true;
         } else {
            K k;
            try {
               k = (K)Objects.requireNonNull(this.keyExtractor.apply(t), "The distinct extractor returned a null value.");
            } catch (Throwable var6) {
               this.onError(Operators.onOperatorError(this.qs, var6, t, this.ctx));
               Operators.onDiscard(t, this.ctx);
               return true;
            }

            boolean b;
            try {
               b = this.distinctPredicate.test(this.collection, k);
            } catch (Throwable var5) {
               this.onError(Operators.onOperatorError(this.qs, var5, t, this.ctx));
               Operators.onDiscard(t, this.ctx);
               return true;
            }

            if (b) {
               this.actual.onNext(t);
               return true;
            } else {
               Operators.onDiscard(t, this.ctx);
               return false;
            }
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.ctx);
         } else {
            this.done = true;
            this.cleanupCallback.accept(this.collection);
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.cleanupCallback.accept(this.collection);
            this.actual.onComplete();
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public void request(long n) {
         this.qs.request(n);
      }

      @Override
      public void cancel() {
         this.qs.cancel();
         if (this.collection != null) {
            this.cleanupCallback.accept(this.collection);
         }

      }

      @Override
      public int requestFusion(int requestedMode) {
         int m = this.qs.requestFusion(requestedMode);
         this.sourceMode = m;
         return m;
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.qs;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Nullable
      public T poll() {
         if (this.sourceMode == 2) {
            long dropped = 0L;

            while(true) {
               T v = (T)this.qs.poll();
               if (v == null) {
                  return null;
               }

               try {
                  K r = (K)Objects.requireNonNull(this.keyExtractor.apply(v), "The keyExtractor returned a null collection");
                  if (this.distinctPredicate.test(this.collection, r)) {
                     if (dropped != 0L) {
                        this.request(dropped);
                     }

                     return v;
                  }

                  Operators.onDiscard(v, this.ctx);
                  ++dropped;
               } catch (Throwable var5) {
                  Operators.onDiscard(v, this.ctx);
                  throw var5;
               }
            }
         } else {
            while(true) {
               T v = (T)this.qs.poll();
               if (v == null) {
                  return null;
               }

               try {
                  K r = (K)Objects.requireNonNull(this.keyExtractor.apply(v), "The keyExtractor returned a null collection");
                  if (this.distinctPredicate.test(this.collection, r)) {
                     return v;
                  }

                  Operators.onDiscard(v, this.ctx);
               } catch (Throwable var6) {
                  Operators.onDiscard(v, this.ctx);
                  throw var6;
               }
            }
         }
      }

      public boolean isEmpty() {
         return this.qs.isEmpty();
      }

      public void clear() {
         this.qs.clear();
         this.cleanupCallback.accept(this.collection);
      }

      public int size() {
         return this.qs.size();
      }
   }

   static final class DistinctSubscriber<T, K, C> implements Fuseable.ConditionalSubscriber<T>, InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final Context ctx;
      final C collection;
      final Function<? super T, ? extends K> keyExtractor;
      final BiPredicate<C, K> distinctPredicate;
      final Consumer<C> cleanupCallback;
      Subscription s;
      boolean done;

      DistinctSubscriber(
         CoreSubscriber<? super T> actual,
         C collection,
         Function<? super T, ? extends K> keyExtractor,
         BiPredicate<C, K> distinctPredicate,
         Consumer<C> cleanupCallback
      ) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.collection = collection;
         this.keyExtractor = keyExtractor;
         this.distinctPredicate = distinctPredicate;
         this.cleanupCallback = cleanupCallback;
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

            boolean b;
            try {
               b = this.distinctPredicate.test(this.collection, k);
            } catch (Throwable var5) {
               this.onError(Operators.onOperatorError(this.s, var5, t, this.ctx));
               Operators.onDiscard(t, this.ctx);
               return true;
            }

            if (b) {
               this.actual.onNext(t);
               return true;
            } else {
               Operators.onDiscard(t, this.ctx);
               return false;
            }
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.ctx);
         } else {
            this.done = true;
            this.cleanupCallback.accept(this.collection);
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.cleanupCallback.accept(this.collection);
            this.actual.onComplete();
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
         if (this.collection != null) {
            this.cleanupCallback.accept(this.collection);
         }

      }

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
   }
}
