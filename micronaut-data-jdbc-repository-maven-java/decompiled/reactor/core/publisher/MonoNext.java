package reactor.core.publisher;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoNext<T> extends MonoFromFluxOperator<T, T> {
   MonoNext(Flux<? extends T> source) {
      super(source);
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new MonoNext.NextSubscriber<>(actual);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class NextSubscriber<T> implements InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      Subscription s;
      boolean done;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<MonoNext.NextSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(MonoNext.NextSubscriber.class, "wip");

      NextSubscriber(CoreSubscriber<? super T> actual) {
         this.actual = actual;
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
            this.s.cancel();
            this.actual.onNext(t);
            this.onComplete();
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
         if (WIP.compareAndSet(this, 0, 1)) {
            this.s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void cancel() {
         this.s.cancel();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }
   }
}
