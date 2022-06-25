package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
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

final class FluxSampleFirst<T, U> extends InternalFluxOperator<T, T> {
   final Function<? super T, ? extends Publisher<U>> throttler;

   FluxSampleFirst(Flux<? extends T> source, Function<? super T, ? extends Publisher<U>> throttler) {
      super(source);
      this.throttler = (Function)Objects.requireNonNull(throttler, "throttler");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      FluxSampleFirst.SampleFirstMain<T, U> main = new FluxSampleFirst.SampleFirstMain<>(actual, this.throttler);
      actual.onSubscribe(main);
      return main;
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class SampleFirstMain<T, U> implements InnerOperator<T, T> {
      final Function<? super T, ? extends Publisher<U>> throttler;
      final CoreSubscriber<? super T> actual;
      final Context ctx;
      volatile boolean gate;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<FluxSampleFirst.SampleFirstMain, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         FluxSampleFirst.SampleFirstMain.class, Subscription.class, "s"
      );
      volatile Subscription other;
      static final AtomicReferenceFieldUpdater<FluxSampleFirst.SampleFirstMain, Subscription> OTHER = AtomicReferenceFieldUpdater.newUpdater(
         FluxSampleFirst.SampleFirstMain.class, Subscription.class, "other"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxSampleFirst.SampleFirstMain> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxSampleFirst.SampleFirstMain.class, "requested"
      );
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxSampleFirst.SampleFirstMain> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxSampleFirst.SampleFirstMain.class, "wip"
      );
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxSampleFirst.SampleFirstMain, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxSampleFirst.SampleFirstMain.class, Throwable.class, "error"
      );

      SampleFirstMain(CoreSubscriber<? super T> actual, Function<? super T, ? extends Publisher<U>> throttler) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.throttler = throttler;
      }

      @Override
      public final CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(Scannable.from(this.other));
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.s == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
         }

      }

      @Override
      public void cancel() {
         Operators.terminate(S, this);
         Operators.terminate(OTHER, this);
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
         if (!this.gate) {
            this.gate = true;
            if (this.wip != 0 || !WIP.compareAndSet(this, 0, 1)) {
               return;
            }

            this.actual.onNext(t);
            if (WIP.decrementAndGet(this) != 0) {
               this.handleTermination();
               return;
            }

            Publisher<U> p;
            try {
               p = (Publisher)Objects.requireNonNull(this.throttler.apply(t), "The throttler returned a null publisher");
            } catch (Throwable var4) {
               Operators.terminate(S, this);
               this.error(Operators.onOperatorError(null, var4, t, this.ctx));
               return;
            }

            FluxSampleFirst.SampleFirstOther<U> other = new FluxSampleFirst.SampleFirstOther<>(this);
            if (Operators.replace(OTHER, this, other)) {
               p.subscribe(other);
            }
         } else {
            Operators.onDiscard(t, this.ctx);
         }

      }

      void handleTermination() {
         Throwable e = Exceptions.terminate(ERROR, this);
         if (e != null && e != Exceptions.TERMINATED) {
            this.actual.onError(e);
         } else {
            this.actual.onComplete();
         }

      }

      void error(Throwable e) {
         if (Exceptions.addThrowable(ERROR, this, e)) {
            if (WIP.getAndIncrement(this) == 0) {
               this.handleTermination();
            }
         } else {
            Operators.onErrorDropped(e, this.ctx);
         }

      }

      @Override
      public void onError(Throwable t) {
         Operators.terminate(OTHER, this);
         this.error(t);
      }

      @Override
      public void onComplete() {
         Operators.terminate(OTHER, this);
         if (WIP.getAndIncrement(this) == 0) {
            this.handleTermination();
         }

      }

      void otherNext() {
         this.gate = false;
      }

      void otherError(Throwable e) {
         Operators.terminate(S, this);
         this.error(e);
      }
   }

   static final class SampleFirstOther<U> extends Operators.DeferredSubscription implements InnerConsumer<U> {
      final FluxSampleFirst.SampleFirstMain<?, U> main;

      SampleFirstOther(FluxSampleFirst.SampleFirstMain<?, U> main) {
         this.main = main;
      }

      @Override
      public Context currentContext() {
         return this.main.currentContext();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.ACTUAL) {
            return this.main;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (this.set(s)) {
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(U t) {
         this.cancel();
         this.main.otherNext();
      }

      @Override
      public void onError(Throwable t) {
         this.main.otherError(t);
      }

      @Override
      public void onComplete() {
         this.main.otherNext();
      }
   }
}
