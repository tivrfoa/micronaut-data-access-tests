package reactor.core.publisher;

import java.util.Objects;
import java.util.function.Supplier;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;

final class FluxErrorSupplied<T> extends Flux<T> implements Fuseable.ScalarCallable, SourceProducer<T> {
   final Supplier<? extends Throwable> errorSupplier;

   FluxErrorSupplied(Supplier<? extends Throwable> errorSupplier) {
      this.errorSupplier = (Supplier)Objects.requireNonNull(errorSupplier, "errorSupplier");
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      Throwable error = (Throwable)Objects.requireNonNull(this.errorSupplier.get(), "errorSupplier produced a null Throwable");
      Operators.error(actual, error);
   }

   public Object call() throws Exception {
      Throwable error = (Throwable)Objects.requireNonNull(this.errorSupplier.get(), "errorSupplier produced a null Throwable");
      if (error instanceof Exception) {
         throw (Exception)error;
      } else {
         throw Exceptions.propagate(error);
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }
}
