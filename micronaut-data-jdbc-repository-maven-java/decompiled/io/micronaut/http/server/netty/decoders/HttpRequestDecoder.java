package io.micronaut.http.server.netty.decoders;

import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.order.Ordered;
import io.micronaut.http.context.event.HttpRequestReceivedEvent;
import io.micronaut.http.netty.stream.StreamedHttpRequest;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.netty.NettyHttpRequest;
import io.micronaut.http.server.netty.NettyHttpServer;
import io.micronaut.runtime.server.EmbeddedServer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
@Internal
public class HttpRequestDecoder extends MessageToMessageDecoder<HttpRequest> implements Ordered {
   public static final String ID = "micronaut-http-decoder";
   private static final Logger LOG = LoggerFactory.getLogger(NettyHttpServer.class);
   private final EmbeddedServer embeddedServer;
   private final ConversionService<?> conversionService;
   private final HttpServerConfiguration configuration;
   private final ApplicationEventPublisher<HttpRequestReceivedEvent> httpRequestReceivedEventPublisher;

   public HttpRequestDecoder(
      EmbeddedServer embeddedServer,
      ConversionService<?> conversionService,
      HttpServerConfiguration configuration,
      ApplicationEventPublisher<HttpRequestReceivedEvent> httpRequestReceivedEventPublisher
   ) {
      this.embeddedServer = embeddedServer;
      this.conversionService = conversionService;
      this.configuration = configuration;
      this.httpRequestReceivedEventPublisher = httpRequestReceivedEventPublisher;
   }

   protected void decode(ChannelHandlerContext ctx, HttpRequest msg, List<Object> out) {
      if (LOG.isTraceEnabled()) {
         LOG.trace("Server {}:{} Received Request: {} {}", this.embeddedServer.getHost(), this.embeddedServer.getPort(), msg.method(), msg.uri());
      }

      try {
         NettyHttpRequest<Object> request = new NettyHttpRequest<>(msg, ctx, this.conversionService, this.configuration);
         if (this.httpRequestReceivedEventPublisher != ApplicationEventPublisher.NO_OP) {
            try {
               ctx.executor().execute(() -> {
                  try {
                     this.httpRequestReceivedEventPublisher.publishEvent(new HttpRequestReceivedEvent(request));
                  } catch (Exception var3x) {
                     if (LOG.isErrorEnabled()) {
                        LOG.error("Error publishing Http request received event: " + var3x.getMessage(), var3x);
                     }
                  }

               });
            } catch (Exception var6) {
               if (LOG.isErrorEnabled()) {
                  LOG.error("Error publishing Http request received event: " + var6.getMessage(), var6);
               }
            }
         }

         out.add(request);
      } catch (IllegalArgumentException var7) {
         new NettyHttpRequest(new DefaultHttpRequest(msg.protocolVersion(), msg.method(), "/"), ctx, this.conversionService, this.configuration);
         Throwable cause = var7.getCause();
         ctx.fireExceptionCaught((Throwable)(cause != null ? cause : var7));
         if (msg instanceof StreamedHttpRequest) {
            ((StreamedHttpRequest)msg).closeIfNoSubscriber();
         }
      }

   }
}
