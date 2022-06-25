package io.micronaut.http.client.netty;

import io.micronaut.core.annotation.Internal;
import java.util.function.Consumer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;
import reactor.util.context.Context;

@Internal
class MicronautFlux<T> extends Flux<T> {
   private final Flux<T> flux;

   MicronautFlux(Flux<T> publisher) {
      this.flux = publisher;
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      this.flux.subscribe(actual);
   }

   public Flux<T> doAfterNext(Consumer<? super T> afterNext) {
      return onAssembly(new MicronautFlux.AfterNextOperator<>(this.flux, afterNext));
   }

   static class AfterNextOperator<T> extends FluxOperator<T, T> {
      private final Consumer<? super T> afterNext;

      protected AfterNextOperator(Flux<? extends T> source, Consumer<? super T> afterNext) {
         super(source);
         this.afterNext = afterNext;
      }

      @Override
      public void subscribe(CoreSubscriber<? super T> actual) {
         this.source.subscribe(new CoreSubscriber<T>() {
            @Override
            public Context currentContext() {
               return actual.currentContext();
            }

            @Override
            public void onSubscribe(Subscription s) {
               actual.onSubscribe(s);
            }

            @Override
            public void onNext(T t) {
               actual.onNext(t);
               AfterNextOperator.this.afterNext.accept(t);
            }

            @Override
            public void onError(Throwable t) {
               actual.onError(t);
            }

            @Override
            public void onComplete() {
               actual.onComplete();
            }
         });
      }
   }
}
