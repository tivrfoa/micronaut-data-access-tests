package io.micronaut.http;

import io.micronaut.core.type.Argument;
import java.util.Optional;

public class FullHttpRequest<B> extends HttpRequestWrapper<B> {
   private final Argument<B> bodyType;

   public FullHttpRequest(HttpRequest<B> delegate, Argument<B> bodyType) {
      super(delegate);
      this.bodyType = bodyType;
   }

   @Override
   public Optional<B> getBody() {
      return super.getBody(this.bodyType);
   }
}
