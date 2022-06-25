package reactor.core.publisher;

import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

abstract class InternalMonoOperator<I, O> extends MonoOperator<I, O> implements Scannable, OptimizableOperator<O, I> {
   @Nullable
   final OptimizableOperator<?, I> optimizableOperator;

   protected InternalMonoOperator(Mono<? extends I> source) {
      super(source);
      if (source instanceof OptimizableOperator) {
         OptimizableOperator<?, I> optimSource = (OptimizableOperator)source;
         this.optimizableOperator = optimSource;
      } else {
         this.optimizableOperator = null;
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
   public abstract CoreSubscriber<? super I> subscribeOrReturn(CoreSubscriber<? super O> var1) throws Throwable;

   @Override
   public final CorePublisher<? extends I> source() {
      return this.source;
   }

   @Override
   public final OptimizableOperator<?, ? extends I> nextOptimizableSource() {
      return this.optimizableOperator;
   }
}
