package io.micronaut.http.client.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.beans.BeanMap;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequestFactory;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.uri.UriTemplate;
import java.util.Map;

@Internal
public class NettyClientHttpRequestFactory implements HttpRequestFactory {
   @Override
   public <T> MutableHttpRequest<T> get(String uri) {
      return new NettyClientHttpRequest<>(HttpMethod.GET, uri);
   }

   @Override
   public <T> MutableHttpRequest<T> post(String uri, T body) {
      HttpMethod method = HttpMethod.POST;
      return this.buildRequest(uri, body, method);
   }

   @Override
   public <T> MutableHttpRequest<T> put(String uri, T body) {
      return this.buildRequest(uri, body, HttpMethod.PUT);
   }

   @Override
   public <T> MutableHttpRequest<T> patch(String uri, T body) {
      return this.buildRequest(uri, body, HttpMethod.PATCH);
   }

   @Override
   public <T> MutableHttpRequest<T> head(String uri) {
      return new NettyClientHttpRequest<>(HttpMethod.HEAD, uri);
   }

   @Override
   public <T> MutableHttpRequest<T> options(String uri) {
      return new NettyClientHttpRequest<>(HttpMethod.OPTIONS, uri);
   }

   @Override
   public <T> MutableHttpRequest<T> delete(String uri, T body) {
      return this.buildRequest(uri, body, HttpMethod.DELETE);
   }

   @Override
   public <T> MutableHttpRequest<T> create(HttpMethod httpMethod, String uri) {
      return new NettyClientHttpRequest<>(httpMethod, uri);
   }

   @Override
   public <T> MutableHttpRequest<T> create(HttpMethod httpMethod, String uri, String httpMethodName) {
      return new NettyClientHttpRequest<>(httpMethod, uri, httpMethodName);
   }

   private <T> MutableHttpRequest<T> buildRequest(String uri, T body, HttpMethod method) {
      if (uri.indexOf(123) > -1 && body != null) {
         if (body instanceof Map) {
            uri = UriTemplate.of(uri).expand((Map<String, Object>)body);
         } else {
            uri = UriTemplate.of(uri).expand(BeanMap.of(body));
         }
      }

      return new NettyClientHttpRequest(method, uri).body(body);
   }
}
