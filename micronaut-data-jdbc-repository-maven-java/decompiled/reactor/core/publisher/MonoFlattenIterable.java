package reactor.core.publisher;

import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;

final class MonoFlattenIterable<T, R> extends FluxFromMonoOperator<T, R> implements Fuseable {
   final Function<? super T, ? extends Iterable<? extends R>> mapper;
   final int prefetch;
   final Supplier<Queue<T>> queueSupplier;

   MonoFlattenIterable(Mono<? extends T> source, Function<? super T, ? extends Iterable<? extends R>> mapper, int prefetch, Supplier<Queue<T>> queueSupplier) {
      super(source);
      if (prefetch <= 0) {
         throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
      } else {
         this.mapper = (Function)Objects.requireNonNull(mapper, "mapper");
         this.prefetch = prefetch;
         this.queueSupplier = (Supplier)Objects.requireNonNull(queueSupplier, "queueSupplier");
      }
   }

   @Override
   public int getPrefetch() {
      return this.prefetch;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) throws Exception {
      if (this.source instanceof Callable) {
         T v = (T)((Callable)this.source).call();
         if (v == null) {
            Operators.complete(actual);
            return null;
         } else {
            Iterable<? extends R> iter = (Iterable)this.mapper.apply(v);
            Iterator<? extends R> it = iter.iterator();
            boolean itFinite = FluxIterable.checkFinite(iter);
            FluxIterable.subscribe(actual, it, itFinite);
            return null;
         }
      } else {
         return new FluxFlattenIterable.FlattenIterableSubscriber<>(actual, this.mapper, this.prefetch, this.queueSupplier);
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
