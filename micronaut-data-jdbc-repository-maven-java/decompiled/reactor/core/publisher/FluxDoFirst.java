package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class FluxDoFirst<T> extends InternalFluxOperator<T, T> {
   final Runnable onFirst;

   FluxDoFirst(Flux<? extends T> source, Runnable onFirst) {
      super(source);
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
