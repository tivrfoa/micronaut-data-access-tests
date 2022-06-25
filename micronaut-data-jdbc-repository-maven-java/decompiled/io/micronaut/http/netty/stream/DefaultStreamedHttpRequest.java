package io.micronaut.http.netty.stream;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.netty.reactive.HotObservable;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

@Internal
public class DefaultStreamedHttpRequest extends DefaultHttpRequest implements StreamedHttpRequest {
   private final Publisher<HttpContent> stream;
   private boolean consumed;

   public DefaultStreamedHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, Publisher<HttpContent> stream) {
      super(httpVersion, method, uri);
      this.stream = stream;
   }

   public DefaultStreamedHttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, boolean validateHeaders, Publisher<HttpContent> stream) {
      super(httpVersion, method, uri, validateHeaders);
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
