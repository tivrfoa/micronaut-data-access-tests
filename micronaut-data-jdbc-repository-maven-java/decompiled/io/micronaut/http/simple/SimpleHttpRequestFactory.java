package io.micronaut.http.simple;

import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequestFactory;
import io.micronaut.http.MutableHttpRequest;

public class SimpleHttpRequestFactory implements HttpRequestFactory {
   @Override
   public <T> MutableHttpRequest<T> get(String uri) {
      return new SimpleHttpRequest<>(HttpMethod.GET, uri, (T)null);
   }

   @Override
   public <T> MutableHttpRequest<T> post(String uri, T body) {
      return new SimpleHttpRequest<>(HttpMethod.POST, uri, body);
   }

   @Override
   public <T> MutableHttpRequest<T> put(String uri, T body) {
      return new SimpleHttpRequest<>(HttpMethod.PUT, uri, body);
   }

   @Override
   public <T> MutableHttpRequest<T> patch(String uri, T body) {
      return new SimpleHttpRequest<>(HttpMethod.PATCH, uri, body);
   }

   @Override
   public <T> MutableHttpRequest<T> head(String uri) {
      return new SimpleHttpRequest<>(HttpMethod.HEAD, uri, (T)null);
   }

   @Override
   public <T> MutableHttpRequest<T> options(String uri) {
      return new SimpleHttpRequest<>(HttpMethod.OPTIONS, uri, (T)null);
   }

   @Override
   public <T> MutableHttpRequest<T> delete(String uri, T body) {
      return new SimpleHttpRequest<>(HttpMethod.DELETE, uri, body);
   }

   @Override
   public <T> MutableHttpRequest<T> create(HttpMethod httpMethod, String uri) {
      return new SimpleHttpRequest<>(httpMethod, uri, (T)null);
   }
}
