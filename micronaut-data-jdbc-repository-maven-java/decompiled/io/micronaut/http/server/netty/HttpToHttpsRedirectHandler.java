package io.micronaut.http.server.netty;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.netty.NettyHttpResponseBuilder;
import io.micronaut.http.server.util.HttpHostResolver;
import io.micronaut.http.ssl.ServerSslConfiguration;
import io.micronaut.http.uri.UriBuilder;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.ssl.SslHandler;

@ChannelHandler.Sharable
@Internal
final class HttpToHttpsRedirectHandler extends ChannelDuplexHandler {
   private final ServerSslConfiguration sslConfiguration;
   private final HttpHostResolver hostResolver;

   public HttpToHttpsRedirectHandler(ServerSslConfiguration sslConfiguration, HttpHostResolver hostResolver) {
      this.hostResolver = hostResolver;
      this.sslConfiguration = sslConfiguration;
   }

   @Override
   public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      if (msg instanceof HttpRequest && ctx.pipeline().get(SslHandler.class) == null) {
         HttpRequest<?> request = (HttpRequest)msg;
         UriBuilder uriBuilder = UriBuilder.of(this.hostResolver.resolve(request));
         uriBuilder.scheme("https");
         int port = this.sslConfiguration.getPort();
         if (port == 443) {
            uriBuilder.port(-1);
         } else {
            uriBuilder.port(port);
         }

         uriBuilder.path(request.getPath());
         MutableHttpResponse<?> response = HttpResponse.permanentRedirect(uriBuilder.build()).header(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
         io.netty.handler.codec.http.HttpResponse nettyResponse = NettyHttpResponseBuilder.toHttpResponse(response);
         ctx.writeAndFlush(nettyResponse);
      } else {
         ctx.fireChannelRead(msg);
      }

   }
}
