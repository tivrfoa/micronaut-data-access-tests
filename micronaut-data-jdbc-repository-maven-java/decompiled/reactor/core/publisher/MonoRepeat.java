package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class MonoRepeat<T> extends FluxFromMonoOperator<T, T> {
   final long times;

   MonoRepeat(Mono<? extends T> source, long times) {
      super(source);
      if (times <= 0L) {
         throw new IllegalArgumentException("times > 0 required");
      } else {
         this.times = times;
      }
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      FluxRepeat.RepeatSubscriber<T> parent = new FluxRepeat.RepeatSubscriber<>(this.source, actual, this.times + 1L);
      actual.onSubscribe(parent);
      if (!parent.isCancelled()) {
         parent.onComplete();
      }

      return null;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
