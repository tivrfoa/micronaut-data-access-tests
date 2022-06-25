package reactor.core.publisher;

import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;

final class MonoCancelOn<T> extends InternalMonoOperator<T, T> {
   final Scheduler scheduler;

   MonoCancelOn(Mono<T> source, Scheduler scheduler) {
      super(source);
      this.scheduler = scheduler;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxCancelOn.CancelSubscriber<>(actual, this.scheduler);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.RUN_ON) {
         return this.scheduler;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : super.scanUnsafe(key);
      }
   }
}
