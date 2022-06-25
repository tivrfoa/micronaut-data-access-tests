package reactor.core.publisher;

import java.util.Objects;
import java.util.function.BiFunction;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxIndexFuseable<T, I> extends InternalFluxOperator<T, I> implements Fuseable {
   private final BiFunction<? super Long, ? super T, ? extends I> indexMapper;

   FluxIndexFuseable(Flux<T> source, BiFunction<? super Long, ? super T, ? extends I> indexMapper) {
      super(source);
      this.indexMapper = FluxIndex.NullSafeIndexMapper.create(
         (BiFunction<? super Long, ? super T, ? extends I>)Objects.requireNonNull(indexMapper, "indexMapper must be non null")
      );
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super I> actual) {
      if (actual instanceof Fuseable.ConditionalSubscriber) {
         Fuseable.ConditionalSubscriber<? super I> cs = (Fuseable.ConditionalSubscriber)actual;
         return new FluxIndexFuseable.IndexFuseableConditionalSubscriber<>(cs, this.indexMapper);
      } else {
         return new FluxIndexFuseable.IndexFuseableSubscriber<>(actual, this.indexMapper);
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class IndexFuseableConditionalSubscriber<I, T> implements InnerOperator<T, I>, Fuseable.ConditionalSubscriber<T>, Fuseable.QueueSubscription<I> {
      final Fuseable.ConditionalSubscriber<? super I> actual;
      final BiFunction<? super Long, ? super T, ? extends I> indexMapper;
      boolean done;
      long index;
      Fuseable.QueueSubscription<T> s;
      int sourceMode;

      IndexFuseableConditionalSubscriber(Fuseable.ConditionalSubscriber<? super I> cs, BiFunction<? super Long, ? super T, ? extends I> indexMapper) {
         this.actual = cs;
         this.indexMapper = indexMapper;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            Fuseable.QueueSubscription<T> qs = (Fuseable.QueueSubscription)s;
            this.s = qs;
            this.actual.onSubscribe(this);
         }

      }

      @Nullable
      public I poll() {
         T v = (T)this.s.poll();
         if (v != null) {
            long i = this.index;
            I indexed = (I)this.indexMapper.apply(i, v);
            this.index = i + 1L;
            return indexed;
         } else {
            return null;
         }
      }

      @Override
      public boolean tryOnNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
            return true;
         } else {
            long i = this.index;

            I indexed;
            try {
               indexed = (I)this.indexMapper.apply(i, t);
               this.index = i + 1L;
            } catch (Throwable var6) {
               this.onError(Operators.onOperatorError(this.s, var6, t, this.actual.currentContext()));
               return true;
            }

            return this.actual.tryOnNext(indexed);
         }
      }

      @Override
      public void onNext(T t) {
         if (this.sourceMode == 2) {
            this.actual.onNext((I)null);
         } else {
            if (this.done) {
               Operators.onNextDropped(t, this.actual.currentContext());
               return;
            }

            long i = this.index;

            try {
               I indexed = (I)this.indexMapper.apply(i, t);
               this.index = i + 1L;
               this.actual.onNext(indexed);
            } catch (Throwable var5) {
               this.onError(Operators.onOperatorError(this.s, var5, t, this.actual.currentContext()));
            }
         }

      }

      @Override
      public void onError(Throwable throwable) {
         if (this.done) {
            Operators.onErrorDropped(throwable, this.actual.currentContext());
         } else {
            this.done = true;
            this.actual.onError(throwable);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.actual.onComplete();
         }
      }

      @Override
      public CoreSubscriber<? super I> actual() {
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

      public boolean isEmpty() {
         return this.s.isEmpty();
      }

      public void clear() {
         this.s.clear();
      }

      @Override
      public int requestFusion(int requestedMode) {
         if (this.indexMapper != Flux.TUPLE2_BIFUNCTION && (requestedMode & 4) != 0) {
            return 0;
         } else {
            int m = this.s.requestFusion(requestedMode);
            this.sourceMode = m;
            return m;
         }
      }

      public int size() {
         return this.s.size();
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

   static final class IndexFuseableSubscriber<I, T> implements InnerOperator<T, I>, Fuseable.QueueSubscription<I> {
      final CoreSubscriber<? super I> actual;
      final BiFunction<? super Long, ? super T, ? extends I> indexMapper;
      boolean done;
      long index;
      Fuseable.QueueSubscription<T> s;
      int sourceMode;

      IndexFuseableSubscriber(CoreSubscriber<? super I> actual, BiFunction<? super Long, ? super T, ? extends I> indexMapper) {
         this.actual = actual;
         this.indexMapper = indexMapper;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            Fuseable.QueueSubscription<T> qs = (Fuseable.QueueSubscription)s;
            this.s = qs;
            this.actual.onSubscribe(this);
         }

      }

      @Nullable
      public I poll() {
         T v = (T)this.s.poll();
         if (v != null) {
            long i = this.index;
            I indexed = (I)this.indexMapper.apply(i, v);
            this.index = i + 1L;
            return indexed;
         } else {
            return null;
         }
      }

      @Override
      public void onNext(T t) {
         if (this.sourceMode == 2) {
            this.actual.onNext((I)null);
         } else {
            if (this.done) {
               Operators.onNextDropped(t, this.actual.currentContext());
               return;
            }

            long i = this.index;

            try {
               I indexed = (I)this.indexMapper.apply(i, t);
               this.index = i + 1L;
               this.actual.onNext(indexed);
            } catch (Throwable var5) {
               this.onError(Operators.onOperatorError(this.s, var5, t, this.actual.currentContext()));
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

      @Override
      public CoreSubscriber<? super I> actual() {
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

      public boolean isEmpty() {
         return this.s.isEmpty();
      }

      public void clear() {
         this.s.clear();
      }

      @Override
      public int requestFusion(int requestedMode) {
         if (this.indexMapper != Flux.TUPLE2_BIFUNCTION && (requestedMode & 4) != 0) {
            return 0;
         } else {
            int m = this.s.requestFusion(requestedMode);
            this.sourceMode = m;
            return m;
         }
      }

      public int size() {
         return this.s.size();
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
}
