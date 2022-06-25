package reactor.core.publisher;

import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class FluxNever extends Flux<Object> implements SourceProducer<Object> {
   static final Publisher<Object> INSTANCE = new FluxNever();

   @Override
   public void subscribe(CoreSubscriber<? super Object> actual) {
      actual.onSubscribe(Operators.emptySubscription());
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }

   static <T> Flux<T> instance() {
      return (Flux<T>)INSTANCE;
   }
}
