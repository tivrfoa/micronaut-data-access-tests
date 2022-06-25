package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.function.Tuple2;

final class MonoElapsed<T> extends InternalMonoOperator<T, Tuple2<Long, T>> implements Fuseable {
   final Scheduler scheduler;

   MonoElapsed(Mono<T> source, Scheduler scheduler) {
      super(source);
      this.scheduler = scheduler;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super Tuple2<Long, T>> actual) {
      return new FluxElapsed.ElapsedSubscriber<>(actual, this.scheduler);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.RUN_ON) {
         return this.scheduler;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
      }
   }
}
