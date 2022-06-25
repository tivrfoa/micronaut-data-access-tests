package io.micronaut.http;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.cookie.Cookie;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public interface MutableHttpResponse<B> extends HttpResponse<B>, MutableHttpMessage<B> {
   MutableHttpResponse<B> cookie(Cookie cookie);

   default MutableHttpResponse<B> cookies(Set<Cookie> cookies) {
      for(Cookie cookie : cookies) {
         this.cookie(cookie);
      }

      return this;
   }

   <T> MutableHttpResponse<T> body(@Nullable T body);

   MutableHttpResponse<B> status(HttpStatus status, CharSequence message);

   default MutableHttpResponse<B> headers(Consumer<MutableHttpHeaders> headers) {
      return (MutableHttpResponse<B>)MutableHttpMessage.super.headers(headers);
   }

   default MutableHttpResponse<B> header(CharSequence name, CharSequence value) {
      return (MutableHttpResponse<B>)MutableHttpMessage.super.header(name, value);
   }

   default MutableHttpResponse<B> headers(Map<CharSequence, CharSequence> namesAndValues) {
      return (MutableHttpResponse<B>)MutableHttpMessage.super.headers(namesAndValues);
   }

   default MutableHttpResponse<B> characterEncoding(CharSequence encoding) {
      if (encoding != null) {
         this.getContentType()
            .ifPresent(mediaType -> this.contentType(new MediaType(mediaType.toString(), Collections.singletonMap("charset", encoding.toString()))));
      }

      return this;
   }

   default MutableHttpResponse<B> characterEncoding(Charset encoding) {
      return this.characterEncoding(encoding.toString());
   }

   default MutableHttpResponse<B> contentLength(long length) {
      return (MutableHttpResponse<B>)MutableHttpMessage.super.contentLength(length);
   }

   default MutableHttpResponse<B> contentType(CharSequence contentType) {
      return (MutableHttpResponse<B>)MutableHttpMessage.super.contentType(contentType);
   }

   default MutableHttpResponse<B> contentType(MediaType mediaType) {
      return (MutableHttpResponse<B>)MutableHttpMessage.super.contentType(mediaType);
   }

   default MutableHttpResponse<B> contentEncoding(CharSequence encoding) {
      return (MutableHttpResponse<B>)MutableHttpMessage.super.contentEncoding(encoding);
   }

   default MutableHttpResponse<B> locale(Locale locale) {
      this.getHeaders().add("Content-Language", locale.toString());
      return this;
   }

   default MutableHttpResponse<B> status(int status) {
      return this.status(HttpStatus.valueOf(status));
   }

   default MutableHttpResponse<B> status(int status, CharSequence message) {
      return this.status(HttpStatus.valueOf(status), message);
   }

   default MutableHttpResponse<B> status(HttpStatus status) {
      return this.status(status, null);
   }

   default MutableHttpResponse<B> attribute(CharSequence name, Object value) {
      return (MutableHttpResponse<B>)this.setAttribute(name, value);
   }
}
