package reactor.core.publisher;

import java.util.function.Function;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class MonoExpand<T> extends FluxFromMonoOperator<T, T> {
   final boolean breadthFirst;
   final Function<? super T, ? extends Publisher<? extends T>> expander;
   final int capacityHint;

   MonoExpand(Mono<T> source, Function<? super T, ? extends Publisher<? extends T>> expander, boolean breadthFirst, int capacityHint) {
      super(source);
      this.expander = expander;
      this.breadthFirst = breadthFirst;
      this.capacityHint = capacityHint;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> s) {
      if (this.breadthFirst) {
         FluxExpand.ExpandBreathSubscriber<T> parent = new FluxExpand.ExpandBreathSubscriber<>(s, this.expander, this.capacityHint);
         parent.queue.offer(this.source);
         s.onSubscribe(parent);
         parent.drainQueue();
      } else {
         FluxExpand.ExpandDepthSubscription<T> parent = new FluxExpand.ExpandDepthSubscription<>(s, this.expander, this.capacityHint);
         parent.source = this.source;
         s.onSubscribe(parent);
      }

      return null;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
