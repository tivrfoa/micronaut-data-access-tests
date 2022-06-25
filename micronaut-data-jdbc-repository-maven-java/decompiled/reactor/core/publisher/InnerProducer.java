package reactor.core.publisher;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

interface InnerProducer<O> extends Scannable, Subscription {
   CoreSubscriber<? super O> actual();

   @Nullable
   @Override
   default Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.ACTUAL ? this.actual() : null;
   }
}
