package reactor.core.publisher;

import org.reactivestreams.Publisher;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

interface SourceProducer<O> extends Scannable, Publisher<O> {
   @Nullable
   @Override
   default Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return Scannable.from(null);
      } else {
         return key == Scannable.Attr.ACTUAL ? Scannable.from(null) : null;
      }
   }

   @Override
   default String stepName() {
      return "source(" + this.getClass().getSimpleName() + ")";
   }
}
