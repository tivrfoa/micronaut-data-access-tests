package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class FluxSourceMono<I> extends FluxFromMonoOperator<I, I> {
   FluxSourceMono(Mono<? extends I> source) {
      super(source);
   }

   @Override
   public String stepName() {
      return this.source instanceof Scannable ? "FluxFromMono(" + Scannable.from(this.source).stepName() + ")" : "FluxFromMono(" + this.source.toString() + ")";
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
