package reactor.core.publisher;

import java.util.Objects;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

abstract class MonoFromFluxOperator<I, O> extends Mono<O> implements Scannable, OptimizableOperator<O, I> {
   protected final Flux<? extends I> source;
   @Nullable
   final OptimizableOperator<?, I> optimizableOperator;

   protected MonoFromFluxOperator(Flux<? extends I> source) {
      this.source = (Flux)Objects.requireNonNull(source);
      if (source instanceof OptimizableOperator) {
         OptimizableOperator<?, I> sourceOptim = (OptimizableOperator)source;
         this.optimizableOperator = sourceOptim;
      } else {
         this.optimizableOperator = null;
      }

   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PREFETCH) {
         return Integer.MAX_VALUE;
      } else {
         return key == Scannable.Attr.PARENT ? this.source : null;
      }
   }

   @Override
   public final void subscribe(CoreSubscriber<? super O> subscriber) {
      OptimizableOperator operator = this;

      try {
         while(true) {
            subscriber = operator.subscribeOrReturn(subscriber);
            if (subscriber == null) {
               return;
            }

            OptimizableOperator newSource = operator.nextOptimizableSource();
            if (newSource == null) {
               operator.source().subscribe(subscriber);
               return;
            }

            operator = newSource;
         }
      } catch (Throwable var4) {
         Operators.reportThrowInSubscribe(subscriber, var4);
      }
   }

   @Nullable
   @Override
   public abstract CoreSubscriber<? super I> subscribeOrReturn(CoreSubscriber<? super O> var1);

   @Override
   public final CorePublisher<? extends I> source() {
      return this.source;
   }

   @Override
   public final OptimizableOperator<?, ? extends I> nextOptimizableSource() {
      return this.optimizableOperator;
   }
}
