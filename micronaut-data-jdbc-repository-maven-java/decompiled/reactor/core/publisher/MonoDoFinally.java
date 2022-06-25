package reactor.core.publisher;

import java.util.function.Consumer;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class MonoDoFinally<T> extends InternalMonoOperator<T, T> {
   final Consumer<SignalType> onFinally;

   MonoDoFinally(Mono<? extends T> source, Consumer<SignalType> onFinally) {
      super(source);
      this.onFinally = onFinally;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return FluxDoFinally.createSubscriber(actual, this.onFinally, false);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
