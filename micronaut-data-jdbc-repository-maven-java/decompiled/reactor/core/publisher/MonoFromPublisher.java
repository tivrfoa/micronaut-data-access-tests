package reactor.core.publisher;

import java.util.Objects;
import org.reactivestreams.Publisher;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoFromPublisher<T> extends Mono<T> implements Scannable, OptimizableOperator<T, T> {
   final Publisher<? extends T> source;
   @Nullable
   final OptimizableOperator<?, T> optimizableOperator;

   MonoFromPublisher(Publisher<? extends T> source) {
      this.source = (Publisher)Objects.requireNonNull(source, "publisher");
      if (source instanceof OptimizableOperator) {
         OptimizableOperator<?, T> optimSource = (OptimizableOperator)source;
         this.optimizableOperator = optimSource;
      } else {
         this.optimizableOperator = null;
      }

   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      try {
         CoreSubscriber<? super T> subscriber = this.subscribeOrReturn(actual);
         if (subscriber != null) {
            this.source.subscribe(subscriber);
         }
      } catch (Throwable var3) {
         Operators.error(actual, Operators.onOperatorError(var3, actual.currentContext()));
      }
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) throws Throwable {
      return new MonoNext.NextSubscriber<>(actual);
   }

   @Override
   public final CorePublisher<? extends T> source() {
      return this;
   }

   @Override
   public final OptimizableOperator<?, ? extends T> nextOptimizableSource() {
      return this.optimizableOperator;
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }
}
