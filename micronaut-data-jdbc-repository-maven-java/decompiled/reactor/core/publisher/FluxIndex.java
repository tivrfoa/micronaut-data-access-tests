package reactor.core.publisher;

import java.util.Objects;
import java.util.function.BiFunction;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxIndex<T, I> extends InternalFluxOperator<T, I> {
   private final BiFunction<? super Long, ? super T, ? extends I> indexMapper;

   FluxIndex(Flux<T> source, BiFunction<? super Long, ? super T, ? extends I> indexMapper) {
      super(source);
      this.indexMapper = FluxIndex.NullSafeIndexMapper.create(
         (BiFunction<? super Long, ? super T, ? extends I>)Objects.requireNonNull(indexMapper, "indexMapper must be non null")
      );
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super I> actual) {
      if (actual instanceof Fuseable.ConditionalSubscriber) {
         Fuseable.ConditionalSubscriber<? super I> cs = (Fuseable.ConditionalSubscriber)actual;
         return new FluxIndex.IndexConditionalSubscriber<>(cs, this.indexMapper);
      } else {
         return new FluxIndex.IndexSubscriber<>(actual, this.indexMapper);
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class IndexConditionalSubscriber<T, I> implements InnerOperator<T, I>, Fuseable.ConditionalSubscriber<T> {
      final Fuseable.ConditionalSubscriber<? super I> actual;
      final BiFunction<? super Long, ? super T, ? extends I> indexMapper;
      Subscription s;
      boolean done;
      long index;

      IndexConditionalSubscriber(Fuseable.ConditionalSubscriber<? super I> cs, BiFunction<? super Long, ? super T, ? extends I> indexMapper) {
         this.actual = cs;
         this.indexMapper = indexMapper;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public boolean tryOnNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
            return true;
         } else {
            long i = this.index;

            I typedIndex;
            try {
               typedIndex = (I)this.indexMapper.apply(i, t);
               this.index = i + 1L;
            } catch (Throwable var6) {
               this.onError(Operators.onOperatorError(this.s, var6, t, this.actual.currentContext()));
               return true;
            }

            return this.actual.tryOnNext(typedIndex);
         }
      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            long i = this.index;

            try {
               I typedIndex = (I)this.indexMapper.apply(i, t);
               this.index = i + 1L;
               this.actual.onNext(typedIndex);
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

   static final class IndexSubscriber<T, I> implements InnerOperator<T, I> {
      final CoreSubscriber<? super I> actual;
      final BiFunction<? super Long, ? super T, ? extends I> indexMapper;
      boolean done;
      Subscription s;
      long index = 0L;

      IndexSubscriber(CoreSubscriber<? super I> actual, BiFunction<? super Long, ? super T, ? extends I> indexMapper) {
         this.actual = actual;
         this.indexMapper = indexMapper;
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
            long i = this.index;

            try {
               I typedIndex = (I)this.indexMapper.apply(i, t);
               this.index = i + 1L;
               this.actual.onNext(typedIndex);
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

   static class NullSafeIndexMapper<T, I> implements BiFunction<Long, T, I> {
      private final BiFunction<? super Long, ? super T, ? extends I> indexMapper;

      private NullSafeIndexMapper(BiFunction<? super Long, ? super T, ? extends I> indexMapper) {
         this.indexMapper = indexMapper;
      }

      public I apply(Long i, T t) {
         I typedIndex = (I)this.indexMapper.apply(i, t);
         if (typedIndex == null) {
            throw new NullPointerException("indexMapper returned a null value at raw index " + i + " for value " + t);
         } else {
            return typedIndex;
         }
      }

      static <T, I> BiFunction<? super Long, ? super T, ? extends I> create(BiFunction<? super Long, ? super T, ? extends I> indexMapper) {
         return (BiFunction<? super Long, ? super T, ? extends I>)(indexMapper == Flux.TUPLE2_BIFUNCTION
            ? indexMapper
            : new FluxIndex.NullSafeIndexMapper(indexMapper));
      }
   }
}
