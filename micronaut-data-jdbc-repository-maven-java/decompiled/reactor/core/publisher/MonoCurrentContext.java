package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.context.Context;

@Deprecated
final class MonoCurrentContext extends Mono<Context> implements Fuseable, Scannable {
   static final MonoCurrentContext INSTANCE = new MonoCurrentContext();

   @Override
   public void subscribe(CoreSubscriber<? super Context> actual) {
      Context ctx = actual.currentContext();
      actual.onSubscribe(Operators.scalarSubscription(actual, ctx));
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }
}
