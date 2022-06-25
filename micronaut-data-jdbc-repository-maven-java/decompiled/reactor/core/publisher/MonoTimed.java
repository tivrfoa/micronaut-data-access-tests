package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.Nullable;

final class MonoTimed<T> extends InternalMonoOperator<T, Timed<T>> {
   final Scheduler clock;

   MonoTimed(Mono<? extends T> source, Scheduler clock) {
      super(source);
      this.clock = clock;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super Timed<T>> actual) {
      return new FluxTimed.TimedSubscriber<>(actual, this.clock);
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PREFETCH) {
         return 0;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
      }
   }
}
