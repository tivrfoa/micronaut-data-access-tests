package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class MonoSingleMono<T> extends InternalMonoOperator<T, T> {
   MonoSingleMono(Mono<? extends T> source) {
      super(source);
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new MonoSingle.SingleSubscriber<>(actual, (T)null, false);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
