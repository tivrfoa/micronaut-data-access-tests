package reactor.core.publisher;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class FluxRetry<T> extends InternalFluxOperator<T, T> {
   final long times;

   FluxRetry(Flux<? extends T> source, long times) {
      super(source);
      if (times < 0L) {
         throw new IllegalArgumentException("times >= 0 required but it was " + times);
      } else {
         this.times = times;
      }
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      FluxRetry.RetrySubscriber<T> parent = new FluxRetry.RetrySubscriber<>(this.source, actual, this.times);
      actual.onSubscribe(parent);
      if (!parent.isCancelled()) {
         parent.resubscribe();
      }

      return null;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class RetrySubscriber<T> extends Operators.MultiSubscriptionSubscriber<T, T> {
      final CorePublisher<? extends T> source;
      long remaining;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxRetry.RetrySubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(FluxRetry.RetrySubscriber.class, "wip");
      long produced;

      RetrySubscriber(CorePublisher<? extends T> source, CoreSubscriber<? super T> actual, long remaining) {
         super(actual);
         this.source = source;
         this.remaining = remaining;
      }

      @Override
      public void onNext(T t) {
         ++this.produced;
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         long r = this.remaining;
         if (r != Long.MAX_VALUE) {
            if (r == 0L) {
               this.actual.onError(t);
               return;
            }

            this.remaining = r - 1L;
         }

         this.resubscribe();
      }

      void resubscribe() {
         if (WIP.getAndIncrement(this) == 0) {
            do {
               if (this.isCancelled()) {
                  return;
               }

               long c = this.produced;
               if (c != 0L) {
                  this.produced = 0L;
                  this.produced(c);
               }

               this.source.subscribe(this);
            } while(WIP.decrementAndGet(this) != 0);
         }

      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
      }
   }
}
