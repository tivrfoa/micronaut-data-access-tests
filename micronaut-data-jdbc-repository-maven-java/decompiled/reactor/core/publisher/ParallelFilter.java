package reactor.core.publisher;

import java.util.function.Predicate;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class ParallelFilter<T> extends ParallelFlux<T> implements Scannable {
   final ParallelFlux<T> source;
   final Predicate<? super T> predicate;

   ParallelFilter(ParallelFlux<T> source, Predicate<? super T> predicate) {
      this.source = source;
      this.predicate = predicate;
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
   public void subscribe(CoreSubscriber<? super T>[] subscribers) {
      if (this.validate(subscribers)) {
         int n = subscribers.length;
         CoreSubscriber<? super T>[] parents = new CoreSubscriber[n];
         boolean conditional = subscribers[0] instanceof Fuseable.ConditionalSubscriber;

         for(int i = 0; i < n; ++i) {
            if (conditional) {
               parents[i] = new FluxFilter.FilterConditionalSubscriber<>((Fuseable.ConditionalSubscriber<? super T>)subscribers[i], this.predicate);
            } else {
               parents[i] = new FluxFilter.FilterSubscriber<>(subscribers[i], this.predicate);
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
