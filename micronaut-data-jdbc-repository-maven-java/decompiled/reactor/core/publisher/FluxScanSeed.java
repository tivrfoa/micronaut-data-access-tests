package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxScanSeed<T, R> extends InternalFluxOperator<T, R> {
   final BiFunction<R, ? super T, R> accumulator;
   final Supplier<R> initialSupplier;

   FluxScanSeed(Flux<? extends T> source, Supplier<R> initialSupplier, BiFunction<R, ? super T, R> accumulator) {
      super(source);
      this.accumulator = (BiFunction)Objects.requireNonNull(accumulator, "accumulator");
      this.initialSupplier = (Supplier)Objects.requireNonNull(initialSupplier, "initialSupplier");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      FluxScanSeed.ScanSeedCoordinator<T, R> coordinator = new FluxScanSeed.ScanSeedCoordinator<>(actual, this.source, this.accumulator, this.initialSupplier);
      actual.onSubscribe(coordinator);
      if (!coordinator.isCancelled()) {
         coordinator.onComplete();
      }

      return null;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class ScanSeedCoordinator<T, R> extends Operators.MultiSubscriptionSubscriber<R, R> {
      final Supplier<R> initialSupplier;
      final Flux<? extends T> source;
      final BiFunction<R, ? super T, R> accumulator;
      volatile int wip;
      long produced;
      private FluxScanSeed.ScanSeedSubscriber<T, R> seedSubscriber;
      static final AtomicIntegerFieldUpdater<FluxScanSeed.ScanSeedCoordinator> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxScanSeed.ScanSeedCoordinator.class, "wip"
      );

      ScanSeedCoordinator(CoreSubscriber<? super R> actual, Flux<? extends T> source, BiFunction<R, ? super T, R> accumulator, Supplier<R> initialSupplier) {
         super(actual);
         this.source = source;
         this.accumulator = accumulator;
         this.initialSupplier = initialSupplier;
      }

      @Override
      public void onComplete() {
         if (WIP.getAndIncrement(this) == 0) {
            do {
               if (this.isCancelled()) {
                  return;
               }

               if (null != this.seedSubscriber && this.subscription == this.seedSubscriber) {
                  this.actual.onComplete();
                  return;
               }

               long c = this.produced;
               if (c != 0L) {
                  this.produced = 0L;
                  this.produced(c);
               }

               if (null == this.seedSubscriber) {
                  R initialValue;
                  try {
                     initialValue = (R)Objects.requireNonNull(this.initialSupplier.get(), "The initial value supplied is null");
                  } catch (Throwable var5) {
                     this.onError(Operators.onOperatorError(var5, this.actual.currentContext()));
                     return;
                  }

                  this.onSubscribe(Operators.scalarSubscription(this, initialValue));
                  this.seedSubscriber = new FluxScanSeed.ScanSeedSubscriber<>(this, this.accumulator, initialValue);
               } else {
                  this.source.subscribe(this.seedSubscriber);
               }

               if (this.isCancelled()) {
                  return;
               }
            } while(WIP.decrementAndGet(this) != 0);
         }

      }

      @Override
      public void onNext(R r) {
         ++this.produced;
         this.actual.onNext(r);
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
      }
   }

   static final class ScanSeedSubscriber<T, R> implements InnerOperator<T, R> {
      final CoreSubscriber<? super R> actual;
      final BiFunction<R, ? super T, R> accumulator;
      Subscription s;
      R value;
      boolean done;

      ScanSeedSubscriber(CoreSubscriber<? super R> actual, BiFunction<R, ? super T, R> accumulator, R initialValue) {
         this.actual = actual;
         this.accumulator = accumulator;
         this.value = initialValue;
      }

      @Override
      public CoreSubscriber<? super R> actual() {
         return this.actual;
      }

      @Override
      public void cancel() {
         this.s.cancel();
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.value = null;
            this.actual.onComplete();
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            this.value = null;
            this.actual.onError(t);
         }
      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            R r = this.value;

            try {
               r = (R)Objects.requireNonNull(this.accumulator.apply(r, t), "The accumulator returned a null value");
            } catch (Throwable var4) {
               this.onError(Operators.onOperatorError(this.s, var4, t, this.actual.currentContext()));
               return;
            }

            this.actual.onNext(r);
            this.value = r;
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void request(long n) {
         this.s.request(n);
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
