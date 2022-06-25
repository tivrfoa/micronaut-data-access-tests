package reactor.core.publisher;

import java.util.ArrayDeque;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.BooleanSupplier;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxTakeLast<T> extends InternalFluxOperator<T, T> {
   final int n;

   FluxTakeLast(Flux<? extends T> source, int n) {
      super(source);
      if (n < 0) {
         throw new IllegalArgumentException("n >= required but it was " + n);
      } else {
         this.n = n;
      }
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return (CoreSubscriber<? super T>)(this.n == 0
         ? new FluxTakeLast.TakeLastZeroSubscriber<>(actual)
         : new FluxTakeLast.TakeLastManySubscriber<>(actual, this.n));
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   static final class TakeLastManySubscriber<T> extends ArrayDeque<T> implements BooleanSupplier, InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final int n;
      volatile boolean cancelled;
      Subscription s;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxTakeLast.TakeLastManySubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxTakeLast.TakeLastManySubscriber.class, "requested"
      );

      TakeLastManySubscriber(CoreSubscriber<? super T> actual, int n) {
         this.actual = actual;
         this.n = n;
      }

      public boolean getAsBoolean() {
         return this.cancelled;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            DrainUtils.postCompleteRequest(n, this.actual, this, REQUESTED, this, this);
         }

      }

      @Override
      public void cancel() {
         this.cancelled = true;
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
         if (this.size() == this.n) {
            this.poll();
         }

         this.offer(t);
      }

      @Override
      public void onError(Throwable t) {
         this.actual.onError(t);
      }

      @Override
      public void onComplete() {
         DrainUtils.postComplete(this.actual, this, REQUESTED, this, this);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.size();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }
   }

   static final class TakeLastZeroSubscriber<T> implements InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      Subscription s;

      TakeLastZeroSubscriber(CoreSubscriber<? super T> actual) {
         this.actual = actual;
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
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
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
