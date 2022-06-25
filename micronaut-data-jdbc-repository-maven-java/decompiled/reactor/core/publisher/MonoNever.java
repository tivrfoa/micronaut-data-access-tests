package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class MonoNever extends Mono<Object> implements SourceProducer<Object> {
   static final Mono<Object> INSTANCE = new MonoNever();

   @Override
   public void subscribe(CoreSubscriber<? super Object> actual) {
      actual.onSubscribe(Operators.emptySubscription());
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }

   static <T> Mono<T> instance() {
      return INSTANCE;
   }
}
