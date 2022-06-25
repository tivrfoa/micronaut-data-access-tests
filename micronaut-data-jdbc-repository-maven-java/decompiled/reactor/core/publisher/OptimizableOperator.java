package reactor.core.publisher;

import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.util.annotation.Nullable;

interface OptimizableOperator<IN, OUT> extends CorePublisher<IN> {
   @Nullable
   CoreSubscriber<? super OUT> subscribeOrReturn(CoreSubscriber<? super IN> var1) throws Throwable;

   CorePublisher<? extends OUT> source();

   @Nullable
   OptimizableOperator<?, ? extends OUT> nextOptimizableSource();
}
