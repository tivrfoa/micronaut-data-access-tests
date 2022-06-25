package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class MonoHide<T> extends InternalMonoOperator<T, T> {
   MonoHide(Mono<? extends T> source) {
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
}
