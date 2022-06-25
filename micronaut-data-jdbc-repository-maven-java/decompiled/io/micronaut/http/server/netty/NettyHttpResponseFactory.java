package io.micronaut.http.server.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpResponseFactory;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.netty.NettyMutableHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.Attribute;
import java.util.Optional;

@Internal
public class NettyHttpResponseFactory implements HttpResponseFactory {
   @Override
   public <T> MutableHttpResponse<T> ok(T body) {
      MutableHttpResponse<T> ok = new NettyMutableHttpResponse<>(ConversionService.SHARED);
      return body != null ? ok.body(body) : ok;
   }

   @Override
   public <T> MutableHttpResponse<T> status(HttpStatus status, T body) {
      MutableHttpResponse<T> ok = new NettyMutableHttpResponse<>(ConversionService.SHARED);
      ok.status(status);
      return body != null ? ok.body(body) : ok;
   }

   @Override
   public MutableHttpResponse status(HttpStatus status, String reason) {
      HttpResponseStatus nettyStatus;
      if (reason == null) {
         nettyStatus = HttpResponseStatus.valueOf(status.getCode());
      } else {
         nettyStatus = new HttpResponseStatus(status.getCode(), reason);
      }

      return new NettyMutableHttpResponse(HttpVersion.HTTP_1_1, nettyStatus, ConversionService.SHARED);
   }

   @Internal
   public static NettyMutableHttpResponse getOrCreate(NettyHttpRequest<?> request) {
      return getOr(request, HttpResponse.ok());
   }

   @Internal
   public static NettyMutableHttpResponse getOr(NettyHttpRequest<?> request, HttpResponse<?> alternative) {
      Attribute<NettyMutableHttpResponse> attr = request.attr(ServerAttributeKeys.RESPONSE_KEY);
      NettyMutableHttpResponse nettyHttpResponse = attr.get();
      if (nettyHttpResponse == null) {
         nettyHttpResponse = (NettyMutableHttpResponse)alternative;
         attr.set(nettyHttpResponse);
      }

      return nettyHttpResponse;
   }

   @Internal
   public static Optional<NettyMutableHttpResponse> get(NettyHttpRequest<?> request) {
      NettyMutableHttpResponse nettyHttpResponse = request.attr(ServerAttributeKeys.RESPONSE_KEY).get();
      return Optional.ofNullable(nettyHttpResponse);
   }

   @Internal
   public static Optional<NettyMutableHttpResponse> set(NettyHttpRequest<?> request, HttpResponse<?> response) {
      request.attr(ServerAttributeKeys.RESPONSE_KEY).set((NettyMutableHttpResponse)response);
      return Optional.ofNullable(response);
   }
}
