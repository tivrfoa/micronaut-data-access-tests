package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class MonoFlatMapMany<T, R> extends FluxFromMonoOperator<T, R> {
   final Function<? super T, ? extends Publisher<? extends R>> mapper;

   MonoFlatMapMany(Mono<? extends T> source, Function<? super T, ? extends Publisher<? extends R>> mapper) {
      super(source);
      this.mapper = mapper;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      return FluxFlatMap.trySubscribeScalarMap(this.source, actual, this.mapper, false, false)
         ? null
         : new MonoFlatMapMany.FlatMapManyMain<>(actual, this.mapper);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class FlatMapManyInner<R> implements InnerConsumer<R> {
      final MonoFlatMapMany.FlatMapManyMain<?, R> parent;
      final CoreSubscriber<? super R> actual;

      FlatMapManyInner(MonoFlatMapMany.FlatMapManyMain<?, R> parent, CoreSubscriber<? super R> actual) {
         this.parent = parent;
         this.actual = actual;
      }

      @Override
      public Context currentContext() {
         return this.actual.currentContext();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.parent.inner;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.parent;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.parent.requested;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         this.parent.onSubscribeInner(s);
      }

      @Override
      public void onNext(R t) {
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         this.actual.onError(t);
      }

      @Override
      public void onComplete() {
         this.actual.onComplete();
      }
   }

   static final class FlatMapManyMain<T, R> implements InnerOperator<T, R> {
      final CoreSubscriber<? super R> actual;
      final Function<? super T, ? extends Publisher<? extends R>> mapper;
      Subscription main;
      volatile Subscription inner;
      static final AtomicReferenceFieldUpdater<MonoFlatMapMany.FlatMapManyMain, Subscription> INNER = AtomicReferenceFieldUpdater.newUpdater(
         MonoFlatMapMany.FlatMapManyMain.class, Subscription.class, "inner"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<MonoFlatMapMany.FlatMapManyMain> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         MonoFlatMapMany.FlatMapManyMain.class, "requested"
      );
      boolean hasValue;

      FlatMapManyMain(CoreSubscriber<? super R> actual, Function<? super T, ? extends Publisher<? extends R>> mapper) {
         this.actual = actual;
         this.mapper = mapper;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.main;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(Scannable.from(this.inner));
      }

      @Override
      public CoreSubscriber<? super R> actual() {
         return this.actual;
      }

      @Override
      public void request(long n) {
         Subscription a = this.inner;
         if (a != null) {
            a.request(n);
         } else if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
            a = this.inner;
            if (a != null) {
               n = REQUESTED.getAndSet(this, 0L);
               if (n != 0L) {
                  a.request(n);
               }
            }
         }

      }

      @Override
      public void cancel() {
         this.main.cancel();
         Operators.terminate(INNER, this);
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.main, s)) {
            this.main = s;
            this.actual.onSubscribe(this);
            s.request(Long.MAX_VALUE);
         }

      }

      void onSubscribeInner(Subscription s) {
         if (Operators.setOnce(INNER, this, s)) {
            long r = REQUESTED.getAndSet(this, 0L);
            if (r != 0L) {
               s.request(r);
            }
         }

      }

      @Override
      public void onNext(T t) {
         this.hasValue = true;

         Publisher<? extends R> p;
         try {
            p = (Publisher)Objects.requireNonNull(this.mapper.apply(t), "The mapper returned a null Publisher.");
         } catch (Throwable var6) {
            this.actual.onError(Operators.onOperatorError(this, var6, t, this.actual.currentContext()));
            return;
         }

         if (p instanceof Callable) {
            R v;
            try {
               v = (R)((Callable)p).call();
            } catch (Throwable var5) {
               this.actual.onError(Operators.onOperatorError(this, var5, t, this.actual.currentContext()));
               return;
            }

            if (v == null) {
               this.actual.onComplete();
            } else {
               this.onSubscribeInner(Operators.scalarSubscription(this.actual, v));
            }

         } else {
            p.subscribe(new MonoFlatMapMany.FlatMapManyInner<>(this, this.actual));
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.hasValue) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.hasValue) {
            this.actual.onComplete();
         }

      }
   }
}
