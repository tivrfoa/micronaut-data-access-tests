package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.stream.Stream;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class MonoFlatMap<T, R> extends InternalMonoOperator<T, R> implements Fuseable {
   final Function<? super T, ? extends Mono<? extends R>> mapper;

   MonoFlatMap(Mono<? extends T> source, Function<? super T, ? extends Mono<? extends R>> mapper) {
      super(source);
      this.mapper = (Function)Objects.requireNonNull(mapper, "mapper");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      if (FluxFlatMap.trySubscribeScalarMap(this.source, actual, this.mapper, true, false)) {
         return null;
      } else {
         MonoFlatMap.FlatMapMain<T, R> manager = new MonoFlatMap.FlatMapMain<>(actual, this.mapper);
         actual.onSubscribe(manager);
         return manager;
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class FlatMapInner<R> implements InnerConsumer<R> {
      final MonoFlatMap.FlatMapMain<?, R> parent;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<MonoFlatMap.FlatMapInner, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         MonoFlatMap.FlatMapInner.class, Subscription.class, "s"
      );
      boolean done;

      FlatMapInner(MonoFlatMap.FlatMapMain<?, R> parent) {
         this.parent = parent;
      }

      @Override
      public Context currentContext() {
         return this.parent.currentContext();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.parent;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.s == Operators.cancelledSubscription();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(R t) {
         if (this.done) {
            Operators.onNextDropped(t, this.parent.currentContext());
         } else {
            this.done = true;
            this.parent.complete(t);
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.parent.currentContext());
         } else {
            this.done = true;
            this.parent.secondError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.parent.secondComplete();
         }
      }

      void cancel() {
         Operators.terminate(S, this);
      }
   }

   static final class FlatMapMain<T, R> extends Operators.MonoSubscriber<T, R> {
      final Function<? super T, ? extends Mono<? extends R>> mapper;
      final MonoFlatMap.FlatMapInner<R> second;
      boolean done;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<MonoFlatMap.FlatMapMain, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         MonoFlatMap.FlatMapMain.class, Subscription.class, "s"
      );

      FlatMapMain(CoreSubscriber<? super R> subscriber, Function<? super T, ? extends Mono<? extends R>> mapper) {
         super(subscriber);
         this.mapper = mapper;
         this.second = new MonoFlatMap.FlatMapInner<>(this);
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.second);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.s == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            this.done = true;

            Mono<? extends R> m;
            try {
               m = (Mono)Objects.requireNonNull(this.mapper.apply(t), "The mapper returned a null Mono");
            } catch (Throwable var8) {
               this.actual.onError(Operators.onOperatorError(this.s, var8, t, this.actual.currentContext()));
               return;
            }

            if (m instanceof Callable) {
               Callable<R> c = (Callable)m;

               R v;
               try {
                  v = (R)c.call();
               } catch (Throwable var6) {
                  this.actual.onError(Operators.onOperatorError(this.s, var6, t, this.actual.currentContext()));
                  return;
               }

               if (v == null) {
                  this.actual.onComplete();
               } else {
                  this.complete(v);
               }

            } else {
               try {
                  m.subscribe(this.second);
               } catch (Throwable var7) {
                  this.actual.onError(Operators.onOperatorError(this, var7, t, this.actual.currentContext()));
               }

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
      public void cancel() {
         super.cancel();
         Operators.terminate(S, this);
         this.second.cancel();
      }

      void secondError(Throwable ex) {
         this.actual.onError(ex);
      }

      void secondComplete() {
         this.actual.onComplete();
      }
   }
}
