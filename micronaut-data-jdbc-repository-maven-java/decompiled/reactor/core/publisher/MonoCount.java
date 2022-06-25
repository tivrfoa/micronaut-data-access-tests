package reactor.core.publisher;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoCount<T> extends MonoFromFluxOperator<T, Long> implements Fuseable {
   MonoCount(Flux<? extends T> source) {
      super(source);
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super Long> actual) {
      return new MonoCount.CountSubscriber<>(actual);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class CountSubscriber<T> extends Operators.MonoSubscriber<T, Long> {
      long counter;
      Subscription s;

      CountSubscriber(CoreSubscriber<? super Long> actual) {
         super(actual);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         }
      }

      @Override
      public void cancel() {
         super.cancel();
         this.s.cancel();
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
         ++this.counter;
      }

      @Override
      public void onComplete() {
         this.complete(Long.valueOf(this.counter));
      }
   }
}
