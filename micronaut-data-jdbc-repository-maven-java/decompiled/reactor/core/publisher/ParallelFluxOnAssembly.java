package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class ParallelFluxOnAssembly<T> extends ParallelFlux<T> implements Fuseable, AssemblyOp, Scannable {
   final ParallelFlux<T> source;
   final FluxOnAssembly.AssemblySnapshot stacktrace;

   ParallelFluxOnAssembly(ParallelFlux<T> source, FluxOnAssembly.AssemblySnapshot stacktrace) {
      this.source = source;
      this.stacktrace = stacktrace;
   }

   @Override
   public int getPrefetch() {
      return this.source.getPrefetch();
   }

   @Override
   public int parallelism() {
      return this.source.parallelism();
   }

   @Override
   public void subscribe(CoreSubscriber<? super T>[] subscribers) {
      if (this.validate(subscribers)) {
         int n = subscribers.length;
         CoreSubscriber<? super T>[] parents = new CoreSubscriber[n];

         for(int i = 0; i < n; ++i) {
            CoreSubscriber<? super T> s = subscribers[i];
            if (s instanceof Fuseable.ConditionalSubscriber) {
               Fuseable.ConditionalSubscriber<? super T> cs = (Fuseable.ConditionalSubscriber)s;
               s = new FluxOnAssembly.OnAssemblyConditionalSubscriber<>(cs, this.stacktrace, this.source, this);
            } else {
               s = new FluxOnAssembly.OnAssemblySubscriber<>(s, this.stacktrace, this.source, this);
            }

            parents[i] = s;
         }

         this.source.subscribe(parents);
      }
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else if (key == Scannable.Attr.PREFETCH) {
         return this.getPrefetch();
      } else if (key == Scannable.Attr.ACTUAL_METADATA) {
         return !this.stacktrace.isCheckpoint;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   @Override
   public String stepName() {
      return this.stacktrace.operatorAssemblyInformation();
   }

   @Override
   public String toString() {
      return this.stacktrace.operatorAssemblyInformation();
   }
}
