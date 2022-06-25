package io.micronaut.http.client.netty;

import io.micronaut.core.annotation.Internal;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.FluxSink;

@Internal
final class ForwardingSubscriber<T> implements Subscriber<T> {
   private final FluxSink<T> sink;

   ForwardingSubscriber(FluxSink<T> sink) {
      this.sink = sink;
   }

   @Override
   public void onSubscribe(Subscription s) {
      this.sink.onRequest(s::request);
   }

   @Override
   public void onNext(T t) {
      this.sink.next(t);
   }

   @Override
   public void onError(Throwable t) {
      this.sink.error(t);
   }

   @Override
   public void onComplete() {
      this.sink.complete();
   }
}
