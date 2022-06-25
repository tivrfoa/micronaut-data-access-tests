package reactor.core.publisher;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoSupplier<T> extends Mono<T> implements Callable<T>, Fuseable, SourceProducer<T> {
   final Supplier<? extends T> supplier;

   MonoSupplier(Supplier<? extends T> callable) {
      this.supplier = (Supplier)Objects.requireNonNull(callable, "callable");
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      Operators.MonoSubscriber<T, T> sds = new Operators.MonoSubscriber<>(actual);
      actual.onSubscribe(sds);
      if (!sds.isCancelled()) {
         try {
            T t = (T)this.supplier.get();
            if (t == null) {
               sds.onComplete();
            } else {
               sds.complete(t);
            }
         } catch (Throwable var4) {
            actual.onError(Operators.onOperatorError(var4, actual.currentContext()));
         }

      }
   }

   @Nullable
   @Override
   public T block(Duration m) {
      return (T)this.supplier.get();
   }

   @Nullable
   @Override
   public T block() {
      return this.block(Duration.ZERO);
   }

   @Nullable
   public T call() throws Exception {
      return (T)this.supplier.get();
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }
}
