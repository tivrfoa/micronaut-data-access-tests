package reactor.core.publisher;

import java.util.Objects;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;

final class FluxJust<T> extends Flux<T> implements Fuseable.ScalarCallable<T>, Fuseable, SourceProducer<T> {
   final T value;

   FluxJust(T value) {
      this.value = (T)Objects.requireNonNull(value, "value");
   }

   public T call() throws Exception {
      return this.value;
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      actual.onSubscribe(Operators.scalarSubscription(actual, this.value, "just"));
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.BUFFERED) {
         return 1;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }
}
