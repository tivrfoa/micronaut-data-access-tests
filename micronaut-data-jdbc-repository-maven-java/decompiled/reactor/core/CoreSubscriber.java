package reactor.core;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.util.context.Context;

public interface CoreSubscriber<T> extends Subscriber<T> {
   default Context currentContext() {
      return Context.empty();
   }

   @Override
   void onSubscribe(Subscription var1);
}
