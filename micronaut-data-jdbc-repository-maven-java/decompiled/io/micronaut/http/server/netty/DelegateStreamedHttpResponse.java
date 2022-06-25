package io.micronaut.http.server.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.netty.stream.StreamedHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

@Internal
final class DelegateStreamedHttpResponse extends DelegateHttpResponse implements StreamedHttpResponse {
   private final Publisher<HttpContent> stream;

   DelegateStreamedHttpResponse(HttpResponse response, Publisher<HttpContent> stream) {
      super(response);
      this.stream = stream;
   }

   @Override
   public void subscribe(Subscriber<? super HttpContent> subscriber) {
      this.stream.subscribe(subscriber);
   }
}
