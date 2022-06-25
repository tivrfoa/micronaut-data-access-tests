package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;

final class FluxDoFirstFuseable<T> extends InternalFluxOperator<T, T> implements Fuseable {
   final Runnable onFirst;

   FluxDoFirstFuseable(Flux<? extends T> fuseableSource, Runnable onFirst) {
      super(fuseableSource);
      this.onFirst = onFirst;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      this.onFirst.run();
      return actual;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
