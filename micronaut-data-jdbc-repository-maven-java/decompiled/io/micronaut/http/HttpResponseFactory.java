package io.micronaut.http;

public interface HttpResponseFactory {
   HttpResponseFactory INSTANCE = DefaultHttpFactories.resolveDefaultResponseFactory();

   <T> MutableHttpResponse<T> ok(T body);

   <T> MutableHttpResponse<T> status(HttpStatus status, String reason);

   <T> MutableHttpResponse<T> status(HttpStatus status, T body);

   default <T> MutableHttpResponse<T> ok() {
      return this.ok((T)null);
   }

   default <T> MutableHttpResponse<T> status(HttpStatus status) {
      return this.status(status, null);
   }
}
