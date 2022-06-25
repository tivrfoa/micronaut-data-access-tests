package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class FluxDefer<T> extends Flux<T> implements SourceProducer<T> {
   final Supplier<? extends Publisher<? extends T>> supplier;

   FluxDefer(Supplier<? extends Publisher<? extends T>> supplier) {
      this.supplier = (Supplier)Objects.requireNonNull(supplier, "supplier");
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      Publisher<? extends T> p;
      try {
         p = (Publisher)Objects.requireNonNull(this.supplier.get(), "The Publisher returned by the supplier is null");
      } catch (Throwable var4) {
         Operators.error(actual, Operators.onOperatorError(var4, actual.currentContext()));
         return;
      }

      from(p).subscribe(actual);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }
}
