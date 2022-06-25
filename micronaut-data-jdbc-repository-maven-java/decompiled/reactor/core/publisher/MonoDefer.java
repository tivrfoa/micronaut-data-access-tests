package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Supplier;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class MonoDefer<T> extends Mono<T> implements SourceProducer<T> {
   final Supplier<? extends Mono<? extends T>> supplier;

   MonoDefer(Supplier<? extends Mono<? extends T>> supplier) {
      this.supplier = (Supplier)Objects.requireNonNull(supplier, "supplier");
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      Mono<? extends T> p;
      try {
         p = (Mono)Objects.requireNonNull(this.supplier.get(), "The Mono returned by the supplier is null");
      } catch (Throwable var4) {
         Operators.error(actual, Operators.onOperatorError(var4, actual.currentContext()));
         return;
      }

      p.subscribe(actual);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }
}
