package io.micronaut.http.netty;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpRequestWrapper;
import io.micronaut.http.netty.stream.StreamedHttpRequest;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import java.util.Objects;

public interface NettyHttpRequestBuilder {
   @NonNull
   FullHttpRequest toFullHttpRequest();

   @NonNull
   StreamedHttpRequest toStreamHttpRequest();

   @NonNull
   io.netty.handler.codec.http.HttpRequest toHttpRequest();

   boolean isStream();

   @NonNull
   static io.netty.handler.codec.http.HttpRequest toHttpRequest(@NonNull HttpRequest<?> request) {
      Objects.requireNonNull(request, "The request cannot be null");

      while(request instanceof HttpRequestWrapper) {
         request = ((HttpRequestWrapper)request).getDelegate();
      }

      if (request instanceof NettyHttpRequestBuilder) {
         return ((NettyHttpRequestBuilder)request).toHttpRequest();
      } else {
         ByteBuf byteBuf = (ByteBuf)request.getBody(ByteBuf.class).orElse(null);
         io.netty.handler.codec.http.HttpRequest nettyRequest;
         if (byteBuf != null) {
            nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.valueOf(request.getMethodName()), request.getUri().toString(), byteBuf);
         } else {
            nettyRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.valueOf(request.getMethodName()), request.getUri().toString());
         }

         request.getHeaders().forEach((s, strings) -> nettyRequest.headers().add(s, strings));
         return nettyRequest;
      }
   }
}
