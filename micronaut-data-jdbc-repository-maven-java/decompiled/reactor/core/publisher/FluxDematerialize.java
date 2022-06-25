package reactor.core.publisher;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxDematerialize<T> extends InternalFluxOperator<Signal<T>, T> {
   FluxDematerialize(Flux<Signal<T>> source) {
      super(source);
   }

   @Override
   public CoreSubscriber<? super Signal<T>> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxDematerialize.DematerializeSubscriber<>(actual, false);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class DematerializeSubscriber<T> implements InnerOperator<Signal<T>, T> {
      final CoreSubscriber<? super T> actual;
      final boolean completeAfterOnNext;
      Subscription s;
      boolean done;
      volatile boolean cancelled;

      DematerializeSubscriber(CoreSubscriber<? super T> subscriber, boolean completeAfterOnNext) {
         this.actual = subscriber;
         this.completeAfterOnNext = completeAfterOnNext;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.PREFETCH) {
            return 0;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      public void onNext(Signal<T> t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            if (t.isOnComplete()) {
               this.s.cancel();
               this.onComplete();
            } else if (t.isOnError()) {
               this.s.cancel();
               this.onError(t.getThrowable());
            } else if (t.isOnNext()) {
               this.actual.onNext(t.get());
               if (this.completeAfterOnNext) {
                  this.onComplete();
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
      public void request(long n) {
         if (Operators.validate(n)) {
            this.s.request(n);
         }

      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            this.s.cancel();
         }

      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }
   }
}
