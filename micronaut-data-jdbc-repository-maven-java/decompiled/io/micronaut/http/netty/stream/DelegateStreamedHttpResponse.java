package io.micronaut.http.netty.stream;

import io.micronaut.core.annotation.Internal;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

@Internal
final class DelegateStreamedHttpResponse extends DelegateHttpResponse implements StreamedHttpResponse {
   private final Publisher<? extends HttpContent> stream;

   DelegateStreamedHttpResponse(HttpResponse response, Publisher<? extends HttpContent> stream) {
      super(response);
      this.stream = stream;
   }

   @Override
   public void subscribe(Subscriber<? super HttpContent> subscriber) {
      this.stream.subscribe(subscriber);
   }
}
