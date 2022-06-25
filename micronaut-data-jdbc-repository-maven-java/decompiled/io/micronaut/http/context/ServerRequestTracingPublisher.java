package io.micronaut.http.context;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Internal
public final class ServerRequestTracingPublisher implements Publishers.MicronautPublisher<MutableHttpResponse<?>> {
   private final HttpRequest<?> request;
   private final Publisher<MutableHttpResponse<?>> actual;

   public ServerRequestTracingPublisher(HttpRequest<?> request, Publisher<MutableHttpResponse<?>> actual) {
      this.request = request;
      this.actual = actual;
   }

   @Override
   public void subscribe(Subscriber<? super MutableHttpResponse<?>> subscriber) {
      ServerRequestContext.with(this.request, (Runnable)(() -> this.actual.subscribe(new Subscriber<MutableHttpResponse<?>>() {
            @Override
            public void onSubscribe(Subscription s) {
               ServerRequestContext.with(ServerRequestTracingPublisher.this.request, (Runnable)(() -> subscriber.onSubscribe(s)));
            }

            public void onNext(MutableHttpResponse<?> mutableHttpResponse) {
               ServerRequestContext.with(ServerRequestTracingPublisher.this.request, (Runnable)(() -> subscriber.onNext(mutableHttpResponse)));
            }

            @Override
            public void onError(Throwable t) {
               ServerRequestContext.with(ServerRequestTracingPublisher.this.request, (Runnable)(() -> subscriber.onError(t)));
            }

            @Override
            public void onComplete() {
               ServerRequestContext.with(ServerRequestTracingPublisher.this.request, subscriber::onComplete);
            }
         })));
   }
}
