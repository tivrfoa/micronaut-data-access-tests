package reactor.core.publisher;

import java.util.Objects;
import org.reactivestreams.Publisher;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxSource<I> extends Flux<I> implements SourceProducer<I>, OptimizableOperator<I, I> {
   final Publisher<? extends I> source;
   @Nullable
   final OptimizableOperator<?, I> optimizableOperator;

   FluxSource(Publisher<? extends I> source) {
      this.source = (Publisher)Objects.requireNonNull(source);
      if (source instanceof OptimizableOperator) {
         OptimizableOperator<?, I> optimSource = (OptimizableOperator)source;
         this.optimizableOperator = optimSource;
      } else {
         this.optimizableOperator = null;
      }

   }

   @Override
   public void subscribe(CoreSubscriber<? super I> actual) {
      this.source.subscribe(actual);
   }

   @Override
   public final CorePublisher<? extends I> source() {
      return this;
   }

   @Override
   public final OptimizableOperator<?, ? extends I> nextOptimizableSource() {
      return this.optimizableOperator;
   }

   @Override
   public CoreSubscriber<? super I> subscribeOrReturn(CoreSubscriber<? super I> actual) {
      return actual;
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PREFETCH) {
         return this.getPrefetch();
      } else if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.from(this.source).scanUnsafe(key) : null;
      }
   }
}
