package reactor.core.publisher;

import java.util.function.Consumer;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;

final class FluxDoFinallyFuseable<T> extends InternalFluxOperator<T, T> implements Fuseable {
   final Consumer<SignalType> onFinally;

   FluxDoFinallyFuseable(Flux<? extends T> source, Consumer<SignalType> onFinally) {
      super(source);
      this.onFinally = onFinally;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return FluxDoFinally.createSubscriber(actual, this.onFinally, true);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
