package reactor.core.publisher;

import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class ParallelArraySource<T> extends ParallelFlux<T> implements SourceProducer<T> {
   final Publisher<T>[] sources;

   ParallelArraySource(Publisher<T>[] sources) {
      if (sources != null && sources.length != 0) {
         this.sources = sources;
      } else {
         throw new IllegalArgumentException("Zero publishers not supported");
      }
   }

   @Override
   public int parallelism() {
      return this.sources.length;
   }

   @Override
   public void subscribe(CoreSubscriber<? super T>[] subscribers) {
      if (this.validate(subscribers)) {
         int n = subscribers.length;

         for(int i = 0; i < n; ++i) {
            Flux.from(this.sources[i]).subscribe(subscribers[i]);
         }

      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : SourceProducer.super.scanUnsafe(key);
   }
}
