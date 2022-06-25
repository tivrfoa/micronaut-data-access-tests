package io.micronaut.http;

public interface HttpRequestFactory {
   HttpRequestFactory INSTANCE = DefaultHttpFactories.resolveDefaultRequestFactory();

   <T> MutableHttpRequest<T> get(String uri);

   <T> MutableHttpRequest<T> post(String uri, T body);

   <T> MutableHttpRequest<T> put(String uri, T body);

   <T> MutableHttpRequest<T> patch(String uri, T body);

   <T> MutableHttpRequest<T> head(String uri);

   <T> MutableHttpRequest<T> options(String uri);

   <T> MutableHttpRequest<T> delete(String uri, T body);

   <T> MutableHttpRequest<T> create(HttpMethod httpMethod, String uri);

   default <T> MutableHttpRequest<T> create(HttpMethod httpMethod, String uri, String httpMethodName) {
      return this.create(httpMethod, uri);
   }
}
