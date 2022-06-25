package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class MonoDematerialize<T> extends InternalMonoOperator<Signal<T>, T> {
   MonoDematerialize(Mono<Signal<T>> source) {
      super(source);
   }

   @Override
   public CoreSubscriber<? super Signal<T>> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxDematerialize.DematerializeSubscriber<>(actual, true);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
