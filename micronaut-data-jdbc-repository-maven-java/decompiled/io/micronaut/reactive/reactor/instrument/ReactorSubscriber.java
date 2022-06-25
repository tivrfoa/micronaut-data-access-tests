package io.micronaut.reactive.reactor.instrument;

import io.micronaut.core.annotation.Internal;
import io.micronaut.scheduling.instrument.Instrumentation;
import io.micronaut.scheduling.instrument.InvocationInstrumenter;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

@Internal
final class ReactorSubscriber<T> implements CoreSubscriber<T> {
   private final InvocationInstrumenter instrumenter;
   private final CoreSubscriber<? super T> subscriber;

   public ReactorSubscriber(InvocationInstrumenter instrumenter, CoreSubscriber<? super T> subscriber) {
      this.instrumenter = instrumenter;
      this.subscriber = subscriber;
   }

   @Override
   public Context currentContext() {
      return this.subscriber.currentContext();
   }

   @Override
   public void onSubscribe(Subscription s) {
      try (Instrumentation ignore = this.instrumenter.newInstrumentation()) {
         this.subscriber.onSubscribe(s);
      }

   }

   @Override
   public void onNext(T t) {
      try (Instrumentation ignore = this.instrumenter.newInstrumentation()) {
         this.subscriber.onNext(t);
      }

   }

   @Override
   public void onError(Throwable t) {
      try (Instrumentation ignore = this.instrumenter.newInstrumentation()) {
         this.subscriber.onError(t);
      }

   }

   @Override
   public void onComplete() {
      try (Instrumentation ignore = this.instrumenter.newInstrumentation()) {
         this.subscriber.onComplete();
      }

   }
}
