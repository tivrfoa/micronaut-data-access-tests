package reactor.core.publisher;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;

final class MonoErrorSupplied<T> extends Mono<T> implements Fuseable.ScalarCallable<T>, SourceProducer<T> {
   final Supplier<? extends Throwable> errorSupplier;

   MonoErrorSupplied(Supplier<? extends Throwable> errorSupplier) {
      this.errorSupplier = (Supplier)Objects.requireNonNull(errorSupplier, "errorSupplier");
   }

   @Override
   public T block(Duration m) {
      Throwable error = (Throwable)Objects.requireNonNull(this.errorSupplier.get(), "the errorSupplier returned null");
      throw Exceptions.propagate(error);
   }

   @Override
   public T block() {
      Throwable error = (Throwable)Objects.requireNonNull(this.errorSupplier.get(), "the errorSupplier returned null");
      throw Exceptions.propagate(error);
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      Throwable error = (Throwable)Objects.requireNonNull(this.errorSupplier.get(), "the errorSupplier returned null");
      Operators.error(actual, error);
   }

   public T call() throws Exception {
      Throwable error = (Throwable)Objects.requireNonNull(this.errorSupplier.get(), "the errorSupplier returned null");
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
