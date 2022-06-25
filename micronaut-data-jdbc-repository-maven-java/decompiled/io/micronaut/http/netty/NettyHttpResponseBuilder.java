package io.micronaut.http.netty;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpResponseWrapper;
import io.micronaut.http.netty.stream.DefaultStreamedHttpResponse;
import io.micronaut.http.netty.stream.StreamedHttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.util.Objects;

public interface NettyHttpResponseBuilder {
   @NonNull
   FullHttpResponse toFullHttpResponse();

   @NonNull
   default StreamedHttpResponse toStreamHttpResponse() {
      FullHttpResponse fullHttpResponse = this.toFullHttpResponse();
      DefaultStreamedHttpResponse streamedHttpResponse = new DefaultStreamedHttpResponse(
         fullHttpResponse.protocolVersion(), fullHttpResponse.status(), true, Publishers.just(new DefaultLastHttpContent(fullHttpResponse.content()))
      );
      streamedHttpResponse.headers().setAll(fullHttpResponse.headers());
      return streamedHttpResponse;
   }

   @NonNull
   default io.netty.handler.codec.http.HttpResponse toHttpResponse() {
      return (io.netty.handler.codec.http.HttpResponse)(this.isStream() ? this.toStreamHttpResponse() : this.toFullHttpResponse());
   }

   boolean isStream();

   @NonNull
   static io.netty.handler.codec.http.HttpResponse toHttpResponse(@NonNull HttpResponse<?> response) {
      Objects.requireNonNull(response, "The response cannot be null");

      while(response instanceof HttpResponseWrapper) {
         response = ((HttpResponseWrapper)response).getDelegate();
      }

      if (response instanceof NettyHttpResponseBuilder) {
         return ((NettyHttpResponseBuilder)response).toHttpResponse();
      } else {
         ByteBuf byteBuf = (ByteBuf)response.getBody(ByteBuf.class).orElse(null);
         io.netty.handler.codec.http.HttpResponse fullHttpResponse;
         if (byteBuf != null) {
            fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(response.code(), response.reason()), byteBuf);
         } else {
            fullHttpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(response.code(), response.reason()));
         }

         response.getHeaders().forEach((s, strings) -> fullHttpResponse.headers().add(s, strings));
         return fullHttpResponse;
      }
   }

   @NonNull
   static StreamedHttpResponse toStreamResponse(@NonNull HttpResponse<?> response) {
      Objects.requireNonNull(response, "The response cannot be null");

      while(response instanceof HttpResponseWrapper) {
         response = ((HttpResponseWrapper)response).getDelegate();
      }

      if (response instanceof NettyHttpResponseBuilder) {
         NettyHttpResponseBuilder builder = (NettyHttpResponseBuilder)response;
         if (builder.isStream()) {
            return builder.toStreamHttpResponse();
         } else {
            FullHttpResponse fullHttpResponse = builder.toFullHttpResponse();
            return new DefaultStreamedHttpResponse(
               HttpVersion.HTTP_1_1,
               HttpResponseStatus.valueOf(response.code(), response.reason()),
               Publishers.just(new DefaultLastHttpContent(fullHttpResponse.content()))
            );
         }
      } else {
         ByteBuf byteBuf = (ByteBuf)response.getBody(ByteBuf.class).orElse(null);
         StreamedHttpResponse fullHttpResponse;
         if (byteBuf != null) {
            fullHttpResponse = new DefaultStreamedHttpResponse(
               HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(response.code(), response.reason()), Publishers.just(new DefaultLastHttpContent(byteBuf))
            );
         } else {
            fullHttpResponse = new DefaultStreamedHttpResponse(
               HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(response.code(), response.reason()), Publishers.empty()
            );
         }

         response.getHeaders().forEach((s, strings) -> fullHttpResponse.headers().add(s, strings));
         return fullHttpResponse;
      }
   }
}
