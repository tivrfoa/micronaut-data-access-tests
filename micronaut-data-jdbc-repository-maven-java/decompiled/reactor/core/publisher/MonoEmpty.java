package reactor.core.publisher;

import java.time.Duration;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoEmpty extends Mono<Object> implements Fuseable.ScalarCallable<Object>, SourceProducer<Object> {
   static final Publisher<Object> INSTANCE = new MonoEmpty();

   @Override
   public void subscribe(CoreSubscriber<? super Object> actual) {
      Operators.complete(actual);
   }

   static <T> Mono<T> instance() {
      return (Mono<T>)INSTANCE;
   }

   @Nullable
   public Object call() throws Exception {
      return null;
   }

   @Nullable
   @Override
   public Object block(Duration m) {
      return null;
   }

   @Nullable
   @Override
   public Object block() {
      return null;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }
}
