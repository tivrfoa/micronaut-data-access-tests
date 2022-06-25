package io.micronaut.http.client.filters;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.context.ServerRequestContext;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Internal
class ClientServerRequestTracingPublisher implements Publishers.MicronautPublisher<HttpResponse<?>> {
   private final HttpRequest<?> request;
   private final Publisher<? extends HttpResponse<?>> actual;

   public ClientServerRequestTracingPublisher(HttpRequest<?> request, Publisher<? extends HttpResponse<?>> actual) {
      this.request = request;
      this.actual = actual;
   }

   @Override
   public void subscribe(Subscriber<? super HttpResponse<?>> subscriber) {
      ServerRequestContext.with(this.request, (Runnable)(() -> this.actual.subscribe(new Subscriber<HttpResponse<?>>() {
            @Override
            public void onSubscribe(Subscription s) {
               ServerRequestContext.with(ClientServerRequestTracingPublisher.this.request, (Runnable)(() -> subscriber.onSubscribe(s)));
            }

            public void onNext(HttpResponse<?> mutableHttpResponse) {
               ServerRequestContext.with(ClientServerRequestTracingPublisher.this.request, (Runnable)(() -> subscriber.onNext(mutableHttpResponse)));
            }

            @Override
            public void onError(Throwable t) {
               ServerRequestContext.with(ClientServerRequestTracingPublisher.this.request, (Runnable)(() -> subscriber.onError(t)));
            }

            @Override
            public void onComplete() {
               ServerRequestContext.with(ClientServerRequestTracingPublisher.this.request, subscriber::onComplete);
            }
         })));
   }
}
