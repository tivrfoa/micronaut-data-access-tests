package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxEmpty extends Flux<Object> implements Fuseable.ScalarCallable<Object>, SourceProducer<Object> {
   private static final Flux<Object> INSTANCE = new FluxEmpty();

   private FluxEmpty() {
   }

   @Override
   public void subscribe(CoreSubscriber<? super Object> actual) {
      Operators.complete(actual);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }

   public static <T> Flux<T> instance() {
      return INSTANCE;
   }

   @Nullable
   public Object call() throws Exception {
      return null;
   }
}
