package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.Nullable;

final class MonoSubscribeOnValue<T> extends Mono<T> implements Scannable {
   final T value;
   final Scheduler scheduler;

   MonoSubscribeOnValue(@Nullable T value, Scheduler scheduler) {
      this.value = value;
      this.scheduler = (Scheduler)Objects.requireNonNull(scheduler, "scheduler");
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      T v = this.value;
      if (v == null) {
         FluxSubscribeOnValue.ScheduledEmpty parent = new FluxSubscribeOnValue.ScheduledEmpty(actual);
         actual.onSubscribe(parent);

         try {
            parent.setFuture(this.scheduler.schedule(parent));
         } catch (RejectedExecutionException var5) {
            if (parent.future != OperatorDisposables.DISPOSED) {
               actual.onError(Operators.onRejectedExecution(var5, actual.currentContext()));
            }
         }
      } else {
         actual.onSubscribe(new FluxSubscribeOnValue.ScheduledScalar<>(actual, v, this.scheduler));
      }

   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.RUN_ON) {
         return this.scheduler;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : null;
      }
   }
}
