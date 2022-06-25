package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;

final class FluxLog<T> extends InternalFluxOperator<T, T> {
   final SignalPeek<T> log;

   FluxLog(Flux<? extends T> source, SignalPeek<T> log) {
      super(source);
      this.log = log;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      if (actual instanceof Fuseable.ConditionalSubscriber) {
         Fuseable.ConditionalSubscriber<T> s2 = (Fuseable.ConditionalSubscriber)actual;
         return new FluxPeekFuseable.PeekConditionalSubscriber<>(s2, this.log);
      } else {
         return new FluxPeek.PeekSubscriber<>(actual, this.log);
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
