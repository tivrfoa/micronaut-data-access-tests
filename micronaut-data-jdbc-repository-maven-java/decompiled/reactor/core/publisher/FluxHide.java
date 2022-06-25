package reactor.core.publisher;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxHide<T> extends InternalFluxOperator<T, T> {
   FluxHide(Flux<? extends T> source) {
      super(source);
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxHide.HideSubscriber<>(actual);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class HideSubscriber<T> implements InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      Subscription s;

      HideSubscriber(CoreSubscriber<? super T> actual) {
         this.actual = actual;
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public void request(long n) {
         this.s.request(n);
      }

      @Override
      public void cancel() {
         this.s.cancel();
      }

      @Override
      public void onSubscribe(Subscription s) {
         this.s = s;
         this.actual.onSubscribe(this);
      }

      @Override
      public void onNext(T t) {
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         this.actual.onError(t);
      }

      @Override
      public void onComplete() {
         this.actual.onComplete();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }
   }

   static final class SuppressFuseableSubscriber<T> implements InnerOperator<T, T>, Fuseable.QueueSubscription<T> {
      final CoreSubscriber<? super T> actual;
      Subscription s;

      SuppressFuseableSubscriber(CoreSubscriber<? super T> actual) {
         this.actual = actual;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public void onNext(T t) {
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         this.actual.onError(t);
      }

      @Override
      public void onComplete() {
         this.actual.onComplete();
      }

      @Override
      public void request(long n) {
         this.s.request(n);
      }

      @Override
      public void cancel() {
         this.s.cancel();
      }

      @Override
      public int requestFusion(int requestedMode) {
         return 0;
      }

      @Nullable
      public T poll() {
         return null;
      }

      public boolean isEmpty() {
         return false;
      }

      public void clear() {
      }

      public int size() {
         return 0;
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }
   }
}
