package io.micronaut.http.server.netty;

import io.micronaut.core.annotation.Internal;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

@Internal
class DelegateHttpResponse extends DelegateHttpMessage implements HttpResponse {
   protected final HttpResponse response;

   DelegateHttpResponse(HttpResponse response) {
      super(response);
      this.response = response;
   }

   @Override
   public HttpResponse setStatus(HttpResponseStatus status) {
      this.response.setStatus(status);
      return this;
   }

   @Deprecated
   @Override
   public HttpResponseStatus getStatus() {
      return this.response.status();
   }

   @Override
   public HttpResponseStatus status() {
      return this.response.status();
   }

   @Override
   public HttpResponse setProtocolVersion(HttpVersion version) {
      super.setProtocolVersion(version);
      return this;
   }
}
