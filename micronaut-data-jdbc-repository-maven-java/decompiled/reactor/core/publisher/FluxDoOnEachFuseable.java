package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Consumer;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;

final class FluxDoOnEachFuseable<T> extends InternalFluxOperator<T, T> implements Fuseable {
   final Consumer<? super Signal<T>> onSignal;

   FluxDoOnEachFuseable(Flux<? extends T> source, Consumer<? super Signal<T>> onSignal) {
      super(source);
      this.onSignal = (Consumer)Objects.requireNonNull(onSignal, "onSignal");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return FluxDoOnEach.createSubscriber(actual, this.onSignal, true, false);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
