package io.micronaut.http.client;

import io.micronaut.context.LifeCycle;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.hateoas.JsonError;
import java.io.Closeable;
import java.net.URL;
import java.util.Optional;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public interface HttpClient extends Closeable, LifeCycle<HttpClient> {
   Argument<JsonError> DEFAULT_ERROR_TYPE = Argument.of(JsonError.class);

   BlockingHttpClient toBlocking();

   <I, O, E> Publisher<HttpResponse<O>> exchange(@NonNull HttpRequest<I> request, @NonNull Argument<O> bodyType, @NonNull Argument<E> errorType);

   default <I, O> Publisher<HttpResponse<O>> exchange(@NonNull HttpRequest<I> request, @NonNull Argument<O> bodyType) {
      return this.exchange(request, bodyType, DEFAULT_ERROR_TYPE);
   }

   default <I> Publisher<HttpResponse<ByteBuffer>> exchange(@NonNull HttpRequest<I> request) {
      return this.exchange(request, ByteBuffer.class);
   }

   default Publisher<HttpResponse<ByteBuffer>> exchange(@NonNull String uri) {
      return this.exchange(HttpRequest.GET(uri), ByteBuffer.class);
   }

   default <O> Publisher<HttpResponse<O>> exchange(@NonNull String uri, @NonNull Class<O> bodyType) {
      return this.exchange(HttpRequest.GET(uri), Argument.of(bodyType));
   }

   default <I, O> Publisher<HttpResponse<O>> exchange(@NonNull HttpRequest<I> request, @NonNull Class<O> bodyType) {
      return this.exchange(request, Argument.of(bodyType));
   }

   default <I, O, E> Publisher<O> retrieve(@NonNull HttpRequest<I> request, @NonNull Argument<O> bodyType, @NonNull Argument<E> errorType) {
      return Flux.from(this.exchange(request, bodyType, errorType))
         .map(
            response -> {
               if (bodyType.getType() == HttpStatus.class) {
                  return response.getStatus();
               } else {
                  Optional<O> body = response.getBody();
                  if (!body.isPresent() && response.getBody(byte[].class).isPresent()) {
                     throw new HttpClientResponseException(
                        String.format("Failed to decode the body for the given content type [%s]", response.getContentType().orElse(null)), response
                     );
                  } else {
                     return body.orElseThrow(() -> new HttpClientResponseException("Empty body", response));
                  }
               }
            }
         );
   }

   default <I, O> Publisher<O> retrieve(@NonNull HttpRequest<I> request, @NonNull Argument<O> bodyType) {
      return this.retrieve(request, bodyType, DEFAULT_ERROR_TYPE);
   }

   default <I, O> Publisher<O> retrieve(@NonNull HttpRequest<I> request, @NonNull Class<O> bodyType) {
      return this.retrieve(request, Argument.of(bodyType));
   }

   default <I> Publisher<String> retrieve(@NonNull HttpRequest<I> request) {
      return this.retrieve(request, String.class);
   }

   default Publisher<String> retrieve(@NonNull String uri) {
      return this.retrieve(HttpRequest.GET(uri), String.class);
   }

   default HttpClient refresh() {
      this.stop();
      return this.start();
   }

   static HttpClient create(@Nullable URL url) {
      return HttpClientFactoryResolver.getFactory().createClient(url);
   }

   static HttpClient create(@Nullable URL url, @NonNull HttpClientConfiguration configuration) {
      return HttpClientFactoryResolver.getFactory().createClient(url, configuration);
   }
}
