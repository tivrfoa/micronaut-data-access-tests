package io.micronaut.http.netty.stream;

import io.micronaut.core.annotation.Internal;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

@Internal
public class DefaultStreamedHttpResponse extends DefaultHttpResponse implements StreamedHttpResponse {
   private final Publisher<HttpContent> stream;

   public DefaultStreamedHttpResponse(HttpVersion version, HttpResponseStatus status, Publisher<HttpContent> stream) {
      super(version, status);
      this.stream = stream;
   }

   public DefaultStreamedHttpResponse(HttpVersion version, HttpResponseStatus status, boolean validateHeaders, Publisher<HttpContent> stream) {
      super(version, status, validateHeaders);
      this.stream = stream;
   }

   public DefaultStreamedHttpResponse(HttpVersion version, HttpResponseStatus status, HttpHeaders headers, Publisher<HttpContent> stream) {
      super(version, status, headers);
      this.stream = stream;
   }

   @Override
   public void subscribe(Subscriber<? super HttpContent> subscriber) {
      this.stream.subscribe(subscriber);
   }
}
