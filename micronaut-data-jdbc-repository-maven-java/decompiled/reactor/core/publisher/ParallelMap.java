package reactor.core.publisher;

import java.util.function.Function;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class ParallelMap<T, R> extends ParallelFlux<R> implements Scannable {
   final ParallelFlux<T> source;
   final Function<? super T, ? extends R> mapper;

   ParallelMap(ParallelFlux<T> source, Function<? super T, ? extends R> mapper) {
      this.source = source;
      this.mapper = mapper;
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else if (key == Scannable.Attr.PREFETCH) {
         return this.getPrefetch();
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   @Override
   public void subscribe(CoreSubscriber<? super R>[] subscribers) {
      if (this.validate(subscribers)) {
         int n = subscribers.length;
         CoreSubscriber<? super T>[] parents = new CoreSubscriber[n];
         boolean conditional = subscribers[0] instanceof Fuseable.ConditionalSubscriber;

         for(int i = 0; i < n; ++i) {
            if (conditional) {
               parents[i] = new FluxMap.MapConditionalSubscriber<>((Fuseable.ConditionalSubscriber<? super R>)subscribers[i], this.mapper);
            } else {
               parents[i] = new FluxMap.MapSubscriber<>(subscribers[i], this.mapper);
            }
         }

         this.source.subscribe(parents);
      }
   }

   @Override
   public int parallelism() {
      return this.source.parallelism();
   }
}
