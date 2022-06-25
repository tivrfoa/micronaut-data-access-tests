package io.micronaut.http;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.uri.UriBuilder;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public interface MutableHttpRequest<B> extends HttpRequest<B>, MutableHttpMessage<B> {
   MutableHttpRequest<B> cookie(Cookie cookie);

   default MutableHttpRequest<B> cookies(Set<Cookie> cookies) {
      for(Cookie cookie : cookies) {
         this.cookie(cookie);
      }

      return this;
   }

   MutableHttpRequest<B> uri(URI uri);

   <T> MutableHttpRequest<T> body(T body);

   @Override
   MutableHttpHeaders getHeaders();

   MutableHttpParameters getParameters();

   @NonNull
   default MutableHttpRequest<B> uri(@NonNull Consumer<UriBuilder> consumer) {
      Objects.requireNonNull(consumer, "URI builder cannot be null");
      UriBuilder builder = UriBuilder.of(this.getUri());
      consumer.accept(builder);
      return this.uri(builder.build());
   }

   default MutableHttpRequest<B> accept(MediaType... mediaTypes) {
      if (ArrayUtils.isNotEmpty(mediaTypes)) {
         String acceptString = String.join(",", mediaTypes);
         this.header("Accept", acceptString);
      }

      return this;
   }

   default MutableHttpRequest<B> accept(CharSequence... mediaTypes) {
      if (ArrayUtils.isNotEmpty(mediaTypes)) {
         String acceptString = String.join(",", mediaTypes);
         this.header("Accept", acceptString);
      }

      return this;
   }

   default MutableHttpRequest<B> headers(Consumer<MutableHttpHeaders> headers) {
      return (MutableHttpRequest<B>)MutableHttpMessage.super.headers(headers);
   }

   default MutableHttpRequest<B> header(CharSequence name, CharSequence value) {
      return (MutableHttpRequest<B>)MutableHttpMessage.super.header(name, value);
   }

   default MutableHttpRequest<B> basicAuth(CharSequence username, CharSequence password) {
      return (MutableHttpRequest<B>)MutableHttpMessage.super.basicAuth(username, password);
   }

   default MutableHttpRequest<B> bearerAuth(CharSequence token) {
      return (MutableHttpRequest<B>)MutableHttpMessage.super.bearerAuth(token);
   }

   default MutableHttpRequest<B> headers(Map<CharSequence, CharSequence> namesAndValues) {
      return (MutableHttpRequest<B>)MutableHttpMessage.super.headers(namesAndValues);
   }

   default MutableHttpRequest<B> contentLength(long length) {
      return (MutableHttpRequest<B>)MutableHttpMessage.super.contentLength(length);
   }

   default MutableHttpRequest<B> contentType(CharSequence contentType) {
      return (MutableHttpRequest<B>)MutableHttpMessage.super.contentType(contentType);
   }

   default MutableHttpRequest<B> contentType(MediaType mediaType) {
      return (MutableHttpRequest<B>)MutableHttpMessage.super.contentType(mediaType);
   }

   default MutableHttpRequest<B> contentEncoding(CharSequence encoding) {
      return (MutableHttpRequest<B>)MutableHttpMessage.super.contentEncoding(encoding);
   }
}
