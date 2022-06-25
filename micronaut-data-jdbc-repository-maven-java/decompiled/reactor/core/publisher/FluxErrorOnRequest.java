package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxErrorOnRequest<T> extends Flux<T> implements SourceProducer<T> {
   final Throwable error;

   FluxErrorOnRequest(Throwable error) {
      this.error = (Throwable)Objects.requireNonNull(error);
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      actual.onSubscribe(new FluxErrorOnRequest.ErrorSubscription(actual, this.error));
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }

   static final class ErrorSubscription implements InnerProducer {
      final CoreSubscriber<?> actual;
      final Throwable error;
      volatile int once;
      static final AtomicIntegerFieldUpdater<FluxErrorOnRequest.ErrorSubscription> ONCE = AtomicIntegerFieldUpdater.newUpdater(
         FluxErrorOnRequest.ErrorSubscription.class, "once"
      );

      ErrorSubscription(CoreSubscriber<?> actual, Throwable error) {
         this.actual = actual;
         this.error = error;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n) && ONCE.compareAndSet(this, 0, 1)) {
            this.actual.onError(this.error);
         }

      }

      @Override
      public void cancel() {
         this.once = 1;
      }

      @Override
      public CoreSubscriber actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.CANCELLED || key == Scannable.Attr.TERMINATED) {
            return this.once == 1;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerProducer.super.scanUnsafe(key);
         }
      }
   }
}
