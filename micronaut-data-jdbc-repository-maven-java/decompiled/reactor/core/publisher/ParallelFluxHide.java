package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class ParallelFluxHide<T> extends ParallelFlux<T> implements Scannable {
   final ParallelFlux<T> source;

   ParallelFluxHide(ParallelFlux<T> source) {
      this.source = source;
   }

   @Override
   public int getPrefetch() {
      return this.source.getPrefetch();
   }

   @Override
   public int parallelism() {
      return this.source.parallelism();
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

         for(int i = 0; i < n; ++i) {
            parents[i] = new FluxHide.HideSubscriber<>(subscribers[i]);
         }

         this.source.subscribe(parents);
      }
   }
}
