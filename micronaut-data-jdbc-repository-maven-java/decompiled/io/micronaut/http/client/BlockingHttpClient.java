package io.micronaut.http.client;

import io.micronaut.core.annotation.Blocking;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import java.io.Closeable;
import java.util.Optional;

@Blocking
public interface BlockingHttpClient extends Closeable {
   <I, O, E> HttpResponse<O> exchange(HttpRequest<I> request, Argument<O> bodyType, Argument<E> errorType);

   default <I, O> HttpResponse<O> exchange(HttpRequest<I> request, Argument<O> bodyType) {
      return this.exchange(request, bodyType, HttpClient.DEFAULT_ERROR_TYPE);
   }

   default <I, O> HttpResponse<O> exchange(HttpRequest<I> request) {
      return this.exchange(request, (Argument<O>)null);
   }

   default <I, O> HttpResponse<O> exchange(HttpRequest<I> request, Class<O> bodyType) {
      return this.exchange(request, Argument.of(bodyType));
   }

   default <I, O> O retrieve(HttpRequest<I> request, Argument<O> bodyType) {
      return this.retrieve(request, bodyType, HttpClient.DEFAULT_ERROR_TYPE);
   }

   default <I, O, E> O retrieve(HttpRequest<I> request, Argument<O> bodyType, Argument<E> errorType) {
      HttpResponse<O> response = this.exchange(request, bodyType, errorType);
      if (HttpStatus.class.isAssignableFrom(bodyType.getType())) {
         return (O)response.getStatus();
      } else {
         Optional<O> body = response.getBody();
         if (!body.isPresent() && response.getBody(Argument.of(byte[].class)).isPresent()) {
            throw new HttpClientResponseException(
               String.format("Failed to decode the body for the given content type [%s]", response.getContentType().orElse(null)), response
            );
         } else {
            return (O)body.orElseThrow(() -> new HttpClientResponseException("Empty body", response));
         }
      }
   }

   default <I, O> O retrieve(HttpRequest<I> request, Class<O> bodyType) {
      return this.retrieve(request, Argument.of(bodyType));
   }

   default <I> String retrieve(HttpRequest<I> request) {
      return this.retrieve(request, String.class);
   }

   default String retrieve(String uri) {
      return this.retrieve(HttpRequest.GET(uri), String.class);
   }

   default <O> O retrieve(String uri, Class<O> bodyType) {
      return this.retrieve(HttpRequest.GET(uri), bodyType);
   }

   default <O, E> O retrieve(String uri, Class<O> bodyType, Class<E> errorType) {
      return this.retrieve(HttpRequest.GET(uri), Argument.of(bodyType), Argument.of(errorType));
   }

   default <O> HttpResponse<O> exchange(String uri) {
      return this.exchange(HttpRequest.GET(uri), (Argument<O>)null);
   }

   default <O> HttpResponse<O> exchange(String uri, Class<O> bodyType) {
      return this.exchange(HttpRequest.GET(uri), Argument.of(bodyType));
   }

   default <O, E> HttpResponse<O> exchange(String uri, Class<O> bodyType, Class<E> errorType) {
      return this.exchange(HttpRequest.GET(uri), Argument.of(bodyType), Argument.of(errorType));
   }
}
