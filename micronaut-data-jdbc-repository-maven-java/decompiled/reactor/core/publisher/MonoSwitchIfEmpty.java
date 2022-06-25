package reactor.core.publisher;

import java.util.Objects;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class MonoSwitchIfEmpty<T> extends InternalMonoOperator<T, T> {
   final Mono<? extends T> other;

   MonoSwitchIfEmpty(Mono<? extends T> source, Mono<? extends T> other) {
      super(source);
      this.other = (Mono)Objects.requireNonNull(other, "other");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      FluxSwitchIfEmpty.SwitchIfEmptySubscriber<T> parent = new FluxSwitchIfEmpty.SwitchIfEmptySubscriber<>(actual, this.other);
      actual.onSubscribe(parent);
      return parent;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
