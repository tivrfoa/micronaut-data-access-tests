package reactor.core.publisher;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoRunnable<T> extends Mono<T> implements Callable<Void>, SourceProducer<T> {
   final Runnable run;

   MonoRunnable(Runnable run) {
      this.run = (Runnable)Objects.requireNonNull(run, "run");
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      MonoRunnable.MonoRunnableEagerSubscription s = new MonoRunnable.MonoRunnableEagerSubscription();
      actual.onSubscribe(s);
      if (!s.isCancelled()) {
         try {
            this.run.run();
            actual.onComplete();
         } catch (Throwable var4) {
            actual.onError(Operators.onOperatorError(var4, actual.currentContext()));
         }

      }
   }

   @Nullable
   @Override
   public T block(Duration m) {
      this.run.run();
      return null;
   }

   @Nullable
   @Override
   public T block() {
      this.run.run();
      return null;
   }

   @Nullable
   public Void call() throws Exception {
      this.run.run();
      return null;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }

   static final class MonoRunnableEagerSubscription extends AtomicBoolean implements Subscription {
      @Override
      public void request(long n) {
      }

      @Override
      public void cancel() {
         this.set(true);
      }

      public boolean isCancelled() {
         return this.get();
      }
   }
}
