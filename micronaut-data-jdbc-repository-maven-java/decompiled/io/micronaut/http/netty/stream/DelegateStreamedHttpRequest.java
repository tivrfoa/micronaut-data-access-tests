package io.micronaut.http.netty.stream;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.netty.reactive.HotObservable;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

@Internal
final class DelegateStreamedHttpRequest extends DelegateHttpRequest implements StreamedHttpRequest {
   private final Publisher<? extends HttpContent> stream;
   private boolean consumed;

   DelegateStreamedHttpRequest(HttpRequest request, Publisher<? extends HttpContent> stream) {
      super(request);
      this.stream = stream;
   }

   @Override
   public boolean isConsumed() {
      return this.consumed;
   }

   @Override
   public void subscribe(Subscriber<? super HttpContent> subscriber) {
      this.consumed = true;
      this.stream.subscribe(subscriber);
   }

   @Override
   public void closeIfNoSubscriber() {
      if (this.stream instanceof HotObservable) {
         ((HotObservable)this.stream).closeIfNoSubscriber();
      }

   }
}
