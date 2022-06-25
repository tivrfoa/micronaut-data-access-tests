package io.micronaut.http.netty.stream;

import io.micronaut.core.annotation.Internal;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;

@Internal
class DelegateHttpRequest extends DelegateHttpMessage implements HttpRequest {
   protected final HttpRequest request;

   DelegateHttpRequest(HttpRequest request) {
      super(request);
      this.request = request;
   }

   @Override
   public HttpRequest setMethod(HttpMethod method) {
      this.request.setMethod(method);
      return this;
   }

   @Override
   public HttpRequest setUri(String uri) {
      this.request.setUri(uri);
      return this;
   }

   @Deprecated
   @Override
   public HttpMethod getMethod() {
      return this.request.method();
   }

   @Override
   public HttpMethod method() {
      return this.request.method();
   }

   @Deprecated
   @Override
   public String getUri() {
      return this.request.uri();
   }

   @Override
   public String uri() {
      return this.request.uri();
   }

   @Override
   public HttpRequest setProtocolVersion(HttpVersion version) {
      super.setProtocolVersion(version);
      return this;
   }
}
