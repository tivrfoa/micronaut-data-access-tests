package io.micronaut.http;

import java.util.Base64;
import java.util.Map;
import java.util.function.Consumer;

public interface MutableHttpMessage<B> extends HttpMessage<B> {
   MutableHttpHeaders getHeaders();

   <T> MutableHttpMessage<T> body(T body);

   default MutableHttpMessage<B> headers(Consumer<MutableHttpHeaders> headers) {
      headers.accept(this.getHeaders());
      return this;
   }

   default MutableHttpMessage<B> header(CharSequence name, CharSequence value) {
      this.getHeaders().add(name, value);
      return this;
   }

   default MutableHttpMessage<B> basicAuth(CharSequence username, CharSequence password) {
      StringBuilder sb = new StringBuilder();
      sb.append(username);
      sb.append(":");
      sb.append(password);
      StringBuilder value = new StringBuilder();
      value.append("Basic");
      value.append(" ");
      value.append(new String(Base64.getEncoder().encode(sb.toString().getBytes())));
      this.header("Authorization", value.toString());
      return this;
   }

   default MutableHttpMessage<B> bearerAuth(CharSequence token) {
      String sb = "Bearer " + token;
      this.header("Authorization", sb);
      return this;
   }

   default MutableHttpMessage<B> headers(Map<CharSequence, CharSequence> namesAndValues) {
      MutableHttpHeaders headers = this.getHeaders();
      namesAndValues.forEach(headers::add);
      return this;
   }

   default MutableHttpMessage<B> contentLength(long length) {
      this.getHeaders().set("Content-Length", String.valueOf(length));
      return this;
   }

   default MutableHttpMessage<B> contentType(CharSequence contentType) {
      if (contentType == null) {
         this.getHeaders().remove("Content-Type");
      } else {
         this.getHeaders().set("Content-Type", contentType);
      }

      return this;
   }

   default MutableHttpMessage<B> contentType(MediaType mediaType) {
      if (mediaType == null) {
         this.getHeaders().remove("Content-Type");
      } else {
         this.getHeaders().set("Content-Type", mediaType);
      }

      return this;
   }

   default MutableHttpMessage<B> contentEncoding(CharSequence encoding) {
      if (encoding == null) {
         this.getHeaders().remove("Content-Encoding");
      } else {
         this.getHeaders().set("Content-Encoding", encoding);
      }

      return this;
   }
}
