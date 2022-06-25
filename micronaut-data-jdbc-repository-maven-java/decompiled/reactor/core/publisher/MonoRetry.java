package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class MonoRetry<T> extends InternalMonoOperator<T, T> {
   final long times;

   MonoRetry(Mono<? extends T> source, long times) {
      super(source);
      if (times < 0L) {
         throw new IllegalArgumentException("times >= 0 required");
      } else {
         this.times = times;
      }
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      FluxRetry.RetrySubscriber<T> parent = new FluxRetry.RetrySubscriber<>(this.source, actual, this.times);
      actual.onSubscribe(parent);
      if (!parent.isCancelled()) {
         parent.resubscribe();
      }

      return null;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
