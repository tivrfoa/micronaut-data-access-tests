package reactor.core.publisher;

import java.util.concurrent.Callable;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxCallable<T> extends Flux<T> implements Callable<T>, Fuseable, SourceProducer<T> {
   final Callable<T> callable;

   FluxCallable(Callable<T> callable) {
      this.callable = callable;
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      Operators.MonoSubscriber<T, T> wrapper = new Operators.MonoSubscriber<>(actual);
      actual.onSubscribe(wrapper);

      try {
         T v = (T)this.callable.call();
         if (v == null) {
            wrapper.onComplete();
         } else {
            wrapper.complete(v);
         }
      } catch (Throwable var4) {
         actual.onError(Operators.onOperatorError(var4, actual.currentContext()));
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
