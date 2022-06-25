package io.micronaut.http.simple;

import io.micronaut.http.HttpResponseFactory;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;

public class SimpleHttpResponseFactory implements HttpResponseFactory {
   @Override
   public <T> MutableHttpResponse<T> ok(T body) {
      return new SimpleHttpResponse().body(body);
   }

   @Override
   public <T> MutableHttpResponse<T> status(HttpStatus status, String reason) {
      return new SimpleHttpResponse<T>().status(status, reason);
   }

   @Override
   public <T> MutableHttpResponse<T> status(HttpStatus status, T body) {
      return new SimpleHttpResponse().status(status).body(body);
   }
}
