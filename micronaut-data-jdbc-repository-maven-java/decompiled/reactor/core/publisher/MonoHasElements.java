package reactor.core.publisher;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoHasElements<T> extends MonoFromFluxOperator<T, Boolean> implements Fuseable {
   MonoHasElements(Flux<? extends T> source) {
      super(source);
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super Boolean> actual) {
      return new MonoHasElements.HasElementsSubscriber<>(actual);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class HasElementsSubscriber<T> extends Operators.MonoSubscriber<T, Boolean> {
      Subscription s;

      HasElementsSubscriber(CoreSubscriber<? super Boolean> actual) {
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
         this.s.cancel();
         this.complete(Boolean.valueOf(true));
      }

      @Override
      public void onComplete() {
         this.complete(Boolean.valueOf(false));
      }
   }
}
