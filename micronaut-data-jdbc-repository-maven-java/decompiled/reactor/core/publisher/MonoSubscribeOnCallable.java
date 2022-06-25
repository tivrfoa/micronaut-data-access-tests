package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;

final class MonoSubscribeOnCallable<T> extends Mono<T> implements Fuseable, Scannable {
   final Callable<? extends T> callable;
   final Scheduler scheduler;

   MonoSubscribeOnCallable(Callable<? extends T> callable, Scheduler scheduler) {
      this.callable = (Callable)Objects.requireNonNull(callable, "callable");
      this.scheduler = (Scheduler)Objects.requireNonNull(scheduler, "scheduler");
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      FluxSubscribeOnCallable.CallableSubscribeOnSubscription<T> parent = new FluxSubscribeOnCallable.CallableSubscribeOnSubscription<>(
         actual, this.callable, this.scheduler
      );
      actual.onSubscribe(parent);

      try {
         parent.setMainFuture(this.scheduler.schedule(parent));
      } catch (RejectedExecutionException var4) {
         if (parent.state != 4) {
            actual.onError(Operators.onRejectedExecution(var4, actual.currentContext()));
         }
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
