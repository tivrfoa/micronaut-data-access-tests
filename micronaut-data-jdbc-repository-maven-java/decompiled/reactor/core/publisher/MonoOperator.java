package reactor.core.publisher;

import java.util.Objects;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

public abstract class MonoOperator<I, O> extends Mono<O> implements Scannable {
   protected final Mono<? extends I> source;

   protected MonoOperator(Mono<? extends I> source) {
      this.source = (Mono)Objects.requireNonNull(source);
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PREFETCH) {
         return Integer.MAX_VALUE;
      } else {
         return key == Scannable.Attr.PARENT ? this.source : null;
      }
   }
}
