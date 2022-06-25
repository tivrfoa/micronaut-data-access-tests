package reactor.core.publisher;

import java.util.Comparator;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class ParallelMergeOrdered<T> extends Flux<T> implements Scannable {
   final ParallelFlux<? extends T> source;
   final int prefetch;
   final Comparator<? super T> valueComparator;

   ParallelMergeOrdered(ParallelFlux<? extends T> source, int prefetch, Comparator<? super T> valueComparator) {
      if (prefetch <= 0) {
         throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
      } else {
         this.source = source;
         this.prefetch = prefetch;
         this.valueComparator = valueComparator;
      }
   }

   @Override
   public int getPrefetch() {
      return this.prefetch;
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else if (key == Scannable.Attr.PREFETCH) {
         return this.prefetch;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      FluxMergeComparing.MergeOrderedMainProducer<T> main = new FluxMergeComparing.MergeOrderedMainProducer<>(
         actual, this.valueComparator, this.prefetch, this.source.parallelism(), true
      );
      actual.onSubscribe(main);
      this.source.subscribe(main.subscribers);
   }
}
