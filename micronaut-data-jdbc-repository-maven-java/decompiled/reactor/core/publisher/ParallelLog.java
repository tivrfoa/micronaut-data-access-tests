package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class ParallelLog<T> extends ParallelFlux<T> implements Scannable {
   final ParallelFlux<T> source;
   final SignalPeek<T> log;

   ParallelLog(ParallelFlux<T> source, SignalPeek<T> log) {
      this.source = source;
      this.log = log;
   }

   @Override
   public void subscribe(CoreSubscriber<? super T>[] subscribers) {
      if (this.validate(subscribers)) {
         int n = subscribers.length;
         CoreSubscriber<? super T>[] parents = new CoreSubscriber[n];
         boolean conditional = subscribers[0] instanceof Fuseable.ConditionalSubscriber;

         for(int i = 0; i < n; ++i) {
            if (conditional) {
               parents[i] = new FluxPeekFuseable.PeekConditionalSubscriber<>((Fuseable.ConditionalSubscriber<? super T>)subscribers[i], this.log);
            } else {
               parents[i] = new FluxPeek.PeekSubscriber<>(subscribers[i], this.log);
            }
         }

         this.source.subscribe(parents);
      }
   }

   @Override
   public int parallelism() {
      return this.source.parallelism();
   }

   @Override
   public int getPrefetch() {
      return this.source.getPrefetch();
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
}
