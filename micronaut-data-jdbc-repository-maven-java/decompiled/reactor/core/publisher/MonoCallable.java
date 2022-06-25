package reactor.core.publisher;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoCallable<T> extends Mono<T> implements Callable<T>, Fuseable, SourceProducer<T> {
   final Callable<? extends T> callable;

   MonoCallable(Callable<? extends T> callable) {
      this.callable = (Callable)Objects.requireNonNull(callable, "callable");
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      Operators.MonoSubscriber<T, T> sds = new Operators.MonoSubscriber<>(actual);
      actual.onSubscribe(sds);
      if (!sds.isCancelled()) {
         try {
            T t = (T)this.callable.call();
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
   public T block() {
      return this.block(Duration.ZERO);
   }

   @Nullable
   @Override
   public T block(Duration m) {
      try {
         return (T)this.callable.call();
      } catch (Throwable var3) {
         throw Exceptions.propagate(var3);
      }
   }

   @Nullable
   public T call() throws Exception {
      return (T)this.callable.call();
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }
}
