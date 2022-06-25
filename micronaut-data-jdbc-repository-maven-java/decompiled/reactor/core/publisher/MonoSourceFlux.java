package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class MonoSourceFlux<I> extends MonoFromFluxOperator<I, I> {
   MonoSourceFlux(Flux<? extends I> source) {
      super(source);
   }

   @Override
   public CoreSubscriber<? super I> subscribeOrReturn(CoreSubscriber<? super I> actual) {
      return actual;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.from(this.source).scanUnsafe(key) : super.scanUnsafe(key);
   }
}
