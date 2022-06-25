package reactor.core.publisher;

import java.util.Objects;
import java.util.Queue;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class FluxMerge<T> extends Flux<T> implements SourceProducer<T> {
   final Publisher<? extends T>[] sources;
   final boolean delayError;
   final int maxConcurrency;
   final Supplier<? extends Queue<T>> mainQueueSupplier;
   final int prefetch;
   final Supplier<? extends Queue<T>> innerQueueSupplier;

   FluxMerge(
      Publisher<? extends T>[] sources,
      boolean delayError,
      int maxConcurrency,
      Supplier<? extends Queue<T>> mainQueueSupplier,
      int prefetch,
      Supplier<? extends Queue<T>> innerQueueSupplier
   ) {
      if (prefetch <= 0) {
         throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
      } else if (maxConcurrency <= 0) {
         throw new IllegalArgumentException("maxConcurrency > 0 required but it was " + maxConcurrency);
      } else {
         this.sources = (Publisher[])Objects.requireNonNull(sources, "sources");
         this.delayError = delayError;
         this.maxConcurrency = maxConcurrency;
         this.prefetch = prefetch;
         this.mainQueueSupplier = (Supplier)Objects.requireNonNull(mainQueueSupplier, "mainQueueSupplier");
         this.innerQueueSupplier = (Supplier)Objects.requireNonNull(innerQueueSupplier, "innerQueueSupplier");
      }
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      FluxFlatMap.FlatMapMain<Publisher<? extends T>, T> merger = new FluxFlatMap.FlatMapMain<>(
         actual, identityFunction(), this.delayError, this.maxConcurrency, this.mainQueueSupplier, this.prefetch, this.innerQueueSupplier
      );
      merger.onSubscribe(new FluxArray.ArraySubscription<>(merger, this.sources));
   }

   FluxMerge<T> mergeAdditionalSource(Publisher<? extends T> source, IntFunction<Supplier<? extends Queue<T>>> newQueueSupplier) {
      int n = this.sources.length;
      Publisher<? extends T>[] newArray = new Publisher[n + 1];
      System.arraycopy(this.sources, 0, newArray, 0, n);
      newArray[n] = source;
      int mc = this.maxConcurrency;
      Supplier<? extends Queue<T>> newMainQueue;
      if (mc != Integer.MAX_VALUE) {
         newMainQueue = (Supplier)newQueueSupplier.apply(++mc);
      } else {
         newMainQueue = this.mainQueueSupplier;
      }

      return new FluxMerge<>(newArray, this.delayError, mc, newMainQueue, this.prefetch, this.innerQueueSupplier);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.DELAY_ERROR) {
         return this.delayError;
      } else if (key == Scannable.Attr.PREFETCH) {
         return this.prefetch;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }
}
