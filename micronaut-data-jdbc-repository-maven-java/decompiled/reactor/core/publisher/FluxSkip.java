package reactor.core.publisher;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxSkip<T> extends InternalFluxOperator<T, T> {
   final long n;

   FluxSkip(Flux<? extends T> source, long n) {
      super(source);
      if (n < 0L) {
         throw new IllegalArgumentException("n >= 0 required but it was " + n);
      } else {
         this.n = n;
      }
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxSkip.SkipSubscriber<>(actual, this.n);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class SkipSubscriber<T> implements InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final Context ctx;
      long remaining;
      Subscription s;

      SkipSubscriber(CoreSubscriber<? super T> actual, long n) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.remaining = n;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            long n = this.remaining;
            this.actual.onSubscribe(this);
            s.request(n);
         }

      }

      @Override
      public void onNext(T t) {
         long r = this.remaining;
         if (r == 0L) {
            this.actual.onNext(t);
         } else {
            Operators.onDiscard(t, this.ctx);
            this.remaining = r - 1L;
         }

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
   }
}
