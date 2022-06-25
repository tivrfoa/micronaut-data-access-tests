package reactor.core.publisher;

import java.util.Objects;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;

final class FluxError<T> extends Flux<T> implements Fuseable.ScalarCallable, SourceProducer<T> {
   final Throwable error;

   FluxError(Throwable error) {
      this.error = (Throwable)Objects.requireNonNull(error);
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      Operators.error(actual, this.error);
   }

   public Object call() throws Exception {
      if (this.error instanceof Exception) {
         throw (Exception)this.error;
      } else {
         throw Exceptions.propagate(this.error);
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }
}
