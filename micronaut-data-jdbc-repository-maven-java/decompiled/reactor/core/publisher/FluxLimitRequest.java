package reactor.core.publisher;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxLimitRequest<T> extends InternalFluxOperator<T, T> {
   final long cap;

   FluxLimitRequest(Flux<T> flux, long cap) {
      super(flux);
      if (cap < 0L) {
         throw new IllegalArgumentException("cap >= 0 required but it was " + cap);
      } else {
         this.cap = cap;
      }
   }

   @Nullable
   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      if (this.cap == 0L) {
         Operators.complete(actual);
         return null;
      } else {
         return new FluxLimitRequest.FluxLimitRequestSubscriber<>(actual, this.cap);
      }
   }

   @Override
   public int getPrefetch() {
      return 0;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
         return this.cap;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
      }
   }

   static class FluxLimitRequestSubscriber<T> implements InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      Subscription parent;
      long toProduce;
      boolean done;
      volatile long requestRemaining;
      static final AtomicLongFieldUpdater<FluxLimitRequest.FluxLimitRequestSubscriber> REQUEST_REMAINING = AtomicLongFieldUpdater.newUpdater(
         FluxLimitRequest.FluxLimitRequestSubscriber.class, "requestRemaining"
      );

      FluxLimitRequestSubscriber(CoreSubscriber<? super T> actual, long cap) {
         this.actual = actual;
         this.toProduce = cap;
         this.requestRemaining = cap;
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            long r = this.toProduce;
            if (r > 0L) {
               this.toProduce = --r;
               this.actual.onNext(t);
               if (r == 0L) {
                  this.done = true;
                  this.parent.cancel();
                  this.actual.onComplete();
               }
            }

         }
      }

      @Override
      public void onError(Throwable throwable) {
         if (this.done) {
            Operators.onErrorDropped(throwable, this.currentContext());
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
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.parent, s)) {
            this.parent = s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void request(long l) {
         long r;
         long newRequest;
         long u;
         do {
            r = this.requestRemaining;
            if (r <= l) {
               newRequest = r;
            } else {
               newRequest = l;
            }

            u = r - newRequest;
         } while(!REQUEST_REMAINING.compareAndSet(this, r, u));

         if (newRequest != 0L) {
            this.parent.request(newRequest);
         }

      }

      @Override
      public void cancel() {
         this.parent.cancel();
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.parent;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }
   }
}
