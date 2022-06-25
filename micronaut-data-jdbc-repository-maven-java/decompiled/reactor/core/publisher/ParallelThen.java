package reactor.core.publisher;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class ParallelThen extends Mono<Void> implements Scannable, Fuseable {
   final ParallelFlux<?> source;

   ParallelThen(ParallelFlux<?> source) {
      this.source = source;
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   @Override
   public void subscribe(CoreSubscriber<? super Void> actual) {
      ParallelThen.ThenMain parent = new ParallelThen.ThenMain(actual, this.source.parallelism());
      actual.onSubscribe(parent);
      this.source.subscribe(parent.subscribers);
   }

   static final class ThenInner implements InnerConsumer<Object> {
      final ParallelThen.ThenMain parent;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<ParallelThen.ThenInner, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         ParallelThen.ThenInner.class, Subscription.class, "s"
      );

      ThenInner(ParallelThen.ThenMain parent) {
         this.parent = parent;
      }

      @Override
      public Context currentContext() {
         return this.parent.currentContext();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.s == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.parent;
         } else if (key == Scannable.Attr.PREFETCH) {
            return Integer.MAX_VALUE;
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
      public void onNext(Object t) {
         Operators.onDiscard(t, this.parent.currentContext());
      }

      @Override
      public void onError(Throwable t) {
         this.parent.innerError(t);
      }

      @Override
      public void onComplete() {
         this.parent.innerComplete();
      }

      void cancel() {
         Operators.terminate(S, this);
      }
   }

   static final class ThenMain extends Operators.MonoSubscriber<Object, Void> {
      final ParallelThen.ThenInner[] subscribers;
      volatile int remaining;
      static final AtomicIntegerFieldUpdater<ParallelThen.ThenMain> REMAINING = AtomicIntegerFieldUpdater.newUpdater(ParallelThen.ThenMain.class, "remaining");
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<ParallelThen.ThenMain, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         ParallelThen.ThenMain.class, Throwable.class, "error"
      );

      ThenMain(CoreSubscriber<? super Void> subscriber, int n) {
         super(subscriber);
         ParallelThen.ThenInner[] a = new ParallelThen.ThenInner[n];

         for(int i = 0; i < n; ++i) {
            a[i] = new ParallelThen.ThenInner(this);
         }

         this.subscribers = a;
         REMAINING.lazySet(this, n);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.TERMINATED) {
            return REMAINING.get(this) == 0;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         }
      }

      @Override
      public void cancel() {
         for(ParallelThen.ThenInner inner : this.subscribers) {
            inner.cancel();
         }

         super.cancel();
      }

      void innerError(Throwable ex) {
         if (ERROR.compareAndSet(this, null, ex)) {
            this.cancel();
            this.actual.onError(ex);
         } else if (this.error != ex) {
            Operators.onErrorDropped(ex, this.actual.currentContext());
         }

      }

      void innerComplete() {
         if (REMAINING.decrementAndGet(this) == 0) {
            this.actual.onComplete();
         }

      }
   }
}
