package reactor.core.publisher;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxDetach<T> extends InternalFluxOperator<T, T> {
   FluxDetach(Flux<? extends T> source) {
      super(source);
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxDetach.DetachSubscriber<>(actual);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class DetachSubscriber<T> implements InnerOperator<T, T> {
      CoreSubscriber<? super T> actual;
      Subscription s;

      DetachSubscriber(CoreSubscriber<? super T> actual) {
         this.actual = actual;
      }

      @Override
      public Context currentContext() {
         return this.actual == null ? Context.empty() : this.actual.currentContext();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.actual == null;
         } else if (key != Scannable.Attr.CANCELLED) {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         } else {
            return this.actual == null && this.s == null;
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
      public void onNext(T t) {
         Subscriber<? super T> a = this.actual;
         if (a != null) {
            a.onNext(t);
         }

      }

      @Override
      public void onError(Throwable t) {
         Subscriber<? super T> a = this.actual;
         if (a != null) {
            this.actual = null;
            this.s = null;
            a.onError(t);
         }

      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public void onComplete() {
         Subscriber<? super T> a = this.actual;
         if (a != null) {
            this.actual = null;
            this.s = null;
            a.onComplete();
         }

      }

      @Override
      public void request(long n) {
         Subscription a = this.s;
         if (a != null) {
            a.request(n);
         }

      }

      @Override
      public void cancel() {
         Subscription a = this.s;
         if (a != null) {
            this.actual = null;
            this.s = null;
            a.cancel();
         }

      }
   }
}
