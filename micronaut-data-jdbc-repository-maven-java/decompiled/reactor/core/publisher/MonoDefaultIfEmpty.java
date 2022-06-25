package reactor.core.publisher;

import java.util.Objects;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class MonoDefaultIfEmpty<T> extends InternalMonoOperator<T, T> {
   final T defaultValue;

   MonoDefaultIfEmpty(Mono<? extends T> source, T defaultValue) {
      super(source);
      this.defaultValue = (T)Objects.requireNonNull(defaultValue, "defaultValue");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxDefaultIfEmpty.DefaultIfEmptySubscriber<>(actual, this.defaultValue);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
