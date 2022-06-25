package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;

final class MonoLogFuseable<T> extends InternalMonoOperator<T, T> implements Fuseable {
   final SignalPeek<T> log;

   MonoLogFuseable(Mono<? extends T> source, SignalPeek<T> log) {
      super(source);
      this.log = log;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return (CoreSubscriber<? super T>)(actual instanceof Fuseable.ConditionalSubscriber
         ? new FluxPeekFuseable.PeekFuseableConditionalSubscriber<>((Fuseable.ConditionalSubscriber<? super T>)actual, this.log)
         : new FluxPeekFuseable.PeekFuseableSubscriber<>(actual, this.log));
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
