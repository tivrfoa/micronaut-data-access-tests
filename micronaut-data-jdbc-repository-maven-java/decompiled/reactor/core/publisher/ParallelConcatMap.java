package reactor.core.publisher;

import java.util.Objects;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class ParallelConcatMap<T, R> extends ParallelFlux<R> implements Scannable {
   final ParallelFlux<T> source;
   final Function<? super T, ? extends Publisher<? extends R>> mapper;
   final Supplier<? extends Queue<T>> queueSupplier;
   final int prefetch;
   final FluxConcatMap.ErrorMode errorMode;

   ParallelConcatMap(
      ParallelFlux<T> source,
      Function<? super T, ? extends Publisher<? extends R>> mapper,
      Supplier<? extends Queue<T>> queueSupplier,
      int prefetch,
      FluxConcatMap.ErrorMode errorMode
   ) {
      this.source = source;
      this.mapper = (Function)Objects.requireNonNull(mapper, "mapper");
      this.queueSupplier = (Supplier)Objects.requireNonNull(queueSupplier, "queueSupplier");
      this.prefetch = prefetch;
      this.errorMode = (FluxConcatMap.ErrorMode)Objects.requireNonNull(errorMode, "errorMode");
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else if (key == Scannable.Attr.PREFETCH) {
         return this.getPrefetch();
      } else if (key == Scannable.Attr.DELAY_ERROR) {
         return this.errorMode != FluxConcatMap.ErrorMode.IMMEDIATE;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   @Override
   public int getPrefetch() {
      return this.prefetch;
   }

   @Override
   public int parallelism() {
      return this.source.parallelism();
   }

   @Override
   public void subscribe(CoreSubscriber<? super R>[] subscribers) {
      if (this.validate(subscribers)) {
         int n = subscribers.length;
         CoreSubscriber<T>[] parents = new CoreSubscriber[n];

         for(int i = 0; i < n; ++i) {
            parents[i] = FluxConcatMap.subscriber(subscribers[i], this.mapper, this.queueSupplier, this.prefetch, this.errorMode);
         }

         this.source.subscribe(parents);
      }
   }
}
