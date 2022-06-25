package reactor.core.publisher;

import java.util.Objects;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxDefaultIfEmpty<T> extends InternalFluxOperator<T, T> {
   final T value;

   FluxDefaultIfEmpty(Flux<? extends T> source, T value) {
      super(source);
      this.value = (T)Objects.requireNonNull(value, "value");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxDefaultIfEmpty.DefaultIfEmptySubscriber<>(actual, this.value);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class DefaultIfEmptySubscriber<T> extends Operators.MonoSubscriber<T, T> {
      Subscription s;
      boolean hasValue;

      DefaultIfEmptySubscriber(CoreSubscriber<? super T> actual, T value) {
         super(actual);
         this.value = value;
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
      public void request(long n) {
         super.request(n);
         this.s.request(n);
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
         }

      }

      @Override
      public void onNext(T t) {
         if (!this.hasValue) {
            this.hasValue = true;
         }

         this.actual.onNext(t);
      }

      @Override
      public void onComplete() {
         if (this.hasValue) {
            this.actual.onComplete();
         } else {
            this.complete(this.value);
         }

      }

      @Override
      public void setValue(T value) {
      }

      @Override
      public int requestFusion(int requestedMode) {
         return 0;
      }
   }
}
