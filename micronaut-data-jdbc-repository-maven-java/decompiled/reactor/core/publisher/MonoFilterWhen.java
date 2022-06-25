package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

class MonoFilterWhen<T> extends InternalMonoOperator<T, T> {
   final Function<? super T, ? extends Publisher<Boolean>> asyncPredicate;

   MonoFilterWhen(Mono<T> source, Function<? super T, ? extends Publisher<Boolean>> asyncPredicate) {
      super(source);
      this.asyncPredicate = asyncPredicate;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new MonoFilterWhen.MonoFilterWhenMain<>(actual, this.asyncPredicate);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class FilterWhenInner implements InnerConsumer<Boolean> {
      final MonoFilterWhen.MonoFilterWhenMain<?> main;
      final boolean cancelOnNext;
      boolean done;
      volatile Subscription sub;
      static final AtomicReferenceFieldUpdater<MonoFilterWhen.FilterWhenInner, Subscription> SUB = AtomicReferenceFieldUpdater.newUpdater(
         MonoFilterWhen.FilterWhenInner.class, Subscription.class, "sub"
      );

      FilterWhenInner(MonoFilterWhen.MonoFilterWhenMain<?> main, boolean cancelOnNext) {
         this.main = main;
         this.cancelOnNext = cancelOnNext;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(SUB, this, s)) {
            s.request(Long.MAX_VALUE);
         }

      }

      public void onNext(Boolean t) {
         if (!this.done) {
            if (this.cancelOnNext) {
               this.sub.cancel();
            }

            this.done = true;
            this.main.innerResult(t);
         }

      }

      @Override
      public void onError(Throwable t) {
         if (!this.done) {
            this.done = true;
            this.main.innerError(t);
         } else {
            Operators.onErrorDropped(t, this.main.currentContext());
         }

      }

      @Override
      public Context currentContext() {
         return this.main.currentContext();
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.main.innerResult(null);
         }

      }

      void cancel() {
         Operators.terminate(SUB, this);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.sub;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.main;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.sub == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.PREFETCH) {
            return Integer.MAX_VALUE;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.done ? 0L : 1L;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }
   }

   static final class MonoFilterWhenMain<T> extends Operators.MonoSubscriber<T, T> {
      final Function<? super T, ? extends Publisher<Boolean>> asyncPredicate;
      boolean sourceValued;
      Subscription upstream;
      volatile MonoFilterWhen.FilterWhenInner asyncFilter;
      static final AtomicReferenceFieldUpdater<MonoFilterWhen.MonoFilterWhenMain, MonoFilterWhen.FilterWhenInner> ASYNC_FILTER = AtomicReferenceFieldUpdater.newUpdater(
         MonoFilterWhen.MonoFilterWhenMain.class, MonoFilterWhen.FilterWhenInner.class, "asyncFilter"
      );
      static final MonoFilterWhen.FilterWhenInner INNER_CANCELLED = new MonoFilterWhen.FilterWhenInner(null, false);

      MonoFilterWhenMain(CoreSubscriber<? super T> actual, Function<? super T, ? extends Publisher<Boolean>> asyncPredicate) {
         super(actual);
         this.asyncPredicate = asyncPredicate;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.upstream, s)) {
            this.upstream = s;
            this.actual.onSubscribe(this);
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
         this.sourceValued = true;
         this.setValue(t);

         Publisher<Boolean> p;
         try {
            p = (Publisher)Objects.requireNonNull(this.asyncPredicate.apply(t), "The asyncPredicate returned a null value");
         } catch (Throwable var6) {
            Exceptions.throwIfFatal(var6);
            super.onError(var6);
            Operators.onDiscard(t, this.actual.currentContext());
            return;
         }

         if (p instanceof Callable) {
            Boolean u;
            try {
               u = (Boolean)((Callable)p).call();
            } catch (Throwable var5) {
               Exceptions.throwIfFatal(var5);
               super.onError(var5);
               Operators.onDiscard(t, this.actual.currentContext());
               return;
            }

            if (u != null && u) {
               this.complete(t);
            } else {
               this.actual.onComplete();
               Operators.onDiscard(t, this.actual.currentContext());
            }
         } else {
            MonoFilterWhen.FilterWhenInner inner = new MonoFilterWhen.FilterWhenInner(this, !(p instanceof Mono));
            if (ASYNC_FILTER.compareAndSet(this, null, inner)) {
               p.subscribe(inner);
            }
         }

      }

      @Override
      public void onComplete() {
         if (!this.sourceValued) {
            super.onComplete();
         }

      }

      @Override
      public void cancel() {
         if (super.state != 4) {
            super.cancel();
            this.upstream.cancel();
            this.cancelInner();
         }

      }

      void cancelInner() {
         MonoFilterWhen.FilterWhenInner a = this.asyncFilter;
         if (a != INNER_CANCELLED) {
            a = (MonoFilterWhen.FilterWhenInner)ASYNC_FILTER.getAndSet(this, INNER_CANCELLED);
            if (a != null && a != INNER_CANCELLED) {
               a.cancel();
            }
         }

      }

      void innerResult(@Nullable Boolean item) {
         if (item != null && item) {
            this.complete(this.value);
         } else {
            super.onComplete();
            this.discard(this.value);
         }

      }

      void innerError(Throwable ex) {
         super.onError(ex);
         this.discard(this.value);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.upstream;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.asyncFilter != null ? this.asyncFilter.scanUnsafe(Scannable.Attr.TERMINATED) : super.scanUnsafe(Scannable.Attr.TERMINATED);
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         MonoFilterWhen.FilterWhenInner c = this.asyncFilter;
         return c == null ? Stream.empty() : Stream.of(c);
      }
   }
}
