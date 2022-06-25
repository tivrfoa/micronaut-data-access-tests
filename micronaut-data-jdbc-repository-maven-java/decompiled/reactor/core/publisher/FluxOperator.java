package reactor.core.publisher;

import java.util.Objects;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

public abstract class FluxOperator<I, O> extends Flux<O> implements Scannable {
   protected final Flux<? extends I> source;

   protected FluxOperator(Flux<? extends I> source) {
      this.source = (Flux)Objects.requireNonNull(source);
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PREFETCH) {
         return this.getPrefetch();
      } else {
         return key == Scannable.Attr.PARENT ? this.source : null;
      }
   }
}
