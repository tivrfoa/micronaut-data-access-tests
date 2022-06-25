package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.context.Context;

final class MonoCompletionStage<T> extends Mono<T> implements Fuseable, Scannable {
   final CompletionStage<? extends T> future;

   MonoCompletionStage(CompletionStage<? extends T> future) {
      this.future = (CompletionStage)Objects.requireNonNull(future, "future");
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      Operators.MonoSubscriber<T, T> sds = new Operators.MonoSubscriber<>(actual);
      actual.onSubscribe(sds);
      if (!sds.isCancelled()) {
         this.future.whenComplete((v, e) -> {
            if (!sds.isCancelled()) {
               try {
                  if (e instanceof CompletionException) {
                     actual.onError(e.getCause());
                  } else if (e != null) {
                     actual.onError(e);
                  } else if (v != null) {
                     sds.complete((T)v);
                  } else {
                     actual.onComplete();
                  }

               } catch (Throwable var5) {
                  Operators.onErrorDropped(var5, actual.currentContext());
                  throw Exceptions.bubble(var5);
               }
            } else {
               Context ctx = sds.currentContext();
               if (e != null && !(e instanceof CancellationException)) {
                  Operators.onErrorDropped(e, ctx);
                  Operators.onDiscard(v, ctx);
               } else {
                  Operators.onDiscard(v, ctx);
               }

            }
         });
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : null;
   }
}
