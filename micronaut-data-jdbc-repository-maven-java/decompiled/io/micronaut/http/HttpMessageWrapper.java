package io.micronaut.http;

import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.type.Argument;
import java.util.Optional;

public class HttpMessageWrapper<B> implements HttpMessage<B> {
   private final HttpMessage<B> delegate;

   public HttpMessageWrapper(HttpMessage<B> delegate) {
      this.delegate = delegate;
   }

   public HttpMessage<B> getDelegate() {
      return this.delegate;
   }

   @Override
   public HttpHeaders getHeaders() {
      return this.delegate.getHeaders();
   }

   @Override
   public MutableConvertibleValues<Object> getAttributes() {
      return this.delegate.getAttributes();
   }

   @Override
   public Optional<B> getBody() {
      return this.delegate.getBody();
   }

   @Override
   public <T> Optional<T> getBody(Class<T> type) {
      return this.delegate.getBody(type);
   }

   @Override
   public <T> Optional<T> getBody(Argument<T> type) {
      return this.delegate.getBody(type);
   }
}
