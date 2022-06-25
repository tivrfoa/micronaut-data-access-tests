package io.micronaut.http.server.netty.websocket;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.bind.RequestBinderRegistry;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.netty.NettyHttpHeaders;
import io.micronaut.http.netty.websocket.WebSocketSessionRepository;
import io.micronaut.http.server.CoroutineHelper;
import io.micronaut.http.server.RouteExecutor;
import io.micronaut.http.server.netty.NettyEmbeddedServices;
import io.micronaut.http.server.netty.NettyHttpRequest;
import io.micronaut.web.router.Router;
import io.micronaut.web.router.UriRouteMatch;
import io.micronaut.websocket.CloseReason;
import io.micronaut.websocket.annotation.OnMessage;
import io.micronaut.websocket.annotation.OnOpen;
import io.micronaut.websocket.annotation.ServerWebSocket;
import io.micronaut.websocket.context.WebSocketBean;
import io.micronaut.websocket.context.WebSocketBeanRegistry;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.AsciiString;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Internal
public class NettyServerWebSocketUpgradeHandler extends SimpleChannelInboundHandler<NettyHttpRequest<?>> {
   public static final String ID = "websocket-upgrade-handler";
   public static final String SCHEME_WEBSOCKET = "ws://";
   public static final String SCHEME_SECURE_WEBSOCKET = "wss://";
   public static final String COMPRESSION_HANDLER = "WebSocketServerCompressionHandler";
   private static final Logger LOG = LoggerFactory.getLogger(NettyServerWebSocketUpgradeHandler.class);
   private static final AsciiString WEB_SOCKET_HEADER_VALUE = AsciiString.cached("websocket");
   private final Router router;
   private final RequestBinderRegistry binderRegistry;
   private final WebSocketBeanRegistry webSocketBeanRegistry;
   private final MediaTypeCodecRegistry mediaTypeCodecRegistry;
   private final WebSocketSessionRepository webSocketSessionRepository;
   private final RouteExecutor routeExecutor;
   private final NettyEmbeddedServices nettyEmbeddedServices;
   private WebSocketServerHandshaker handshaker;

   public NettyServerWebSocketUpgradeHandler(NettyEmbeddedServices embeddedServices, WebSocketSessionRepository webSocketSessionRepository) {
      this.router = embeddedServices.getRouter();
      this.binderRegistry = embeddedServices.getRequestArgumentSatisfier().getBinderRegistry();
      this.webSocketBeanRegistry = embeddedServices.getWebSocketBeanRegistry();
      this.mediaTypeCodecRegistry = embeddedServices.getMediaTypeCodecRegistry();
      this.webSocketSessionRepository = webSocketSessionRepository;
      this.routeExecutor = embeddedServices.getRouteExecutor();
      this.nettyEmbeddedServices = embeddedServices;
   }

   @Override
   public boolean acceptInboundMessage(Object msg) {
      return msg instanceof NettyHttpRequest && this.isWebSocketUpgrade((NettyHttpRequest<?>)msg);
   }

   private boolean isWebSocketUpgrade(@NonNull NettyHttpRequest<?> request) {
      HttpHeaders headers = request.getNativeRequest().headers();
      return headers.containsValue(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE, true)
         ? headers.containsValue(HttpHeaderNames.UPGRADE, WEB_SOCKET_HEADER_VALUE, true)
         : false;
   }

   protected final void channelRead0(ChannelHandlerContext ctx, NettyHttpRequest<?> msg) {
      ServerRequestContext.set(msg);
      Optional<UriRouteMatch<Object, Object>> optionalRoute = this.router
         .find(HttpMethod.GET, msg.getUri().toString(), msg)
         .filter(rm -> rm.isAnnotationPresent(OnMessage.class) || rm.isAnnotationPresent(OnOpen.class))
         .findFirst();
      MutableHttpResponse<?> proceed = HttpResponse.ok();
      AtomicReference<HttpRequest<?>> requestReference = new AtomicReference(msg);
      Flux<MutableHttpResponse<?>> responsePublisher;
      if (optionalRoute.isPresent()) {
         UriRouteMatch<Object, Object> rm = (UriRouteMatch)optionalRoute.get();
         msg.setAttribute(HttpAttributes.ROUTE_MATCH, rm);
         msg.setAttribute(HttpAttributes.ROUTE_INFO, rm);
         proceed.setAttribute(HttpAttributes.ROUTE_MATCH, rm);
         proceed.setAttribute(HttpAttributes.ROUTE_INFO, rm);
         responsePublisher = Flux.just(proceed);
      } else {
         responsePublisher = this.routeExecutor.onError(new HttpStatusException(HttpStatus.NOT_FOUND, "WebSocket Not Found"), msg);
      }

      Publisher<? extends MutableHttpResponse<?>> finalPublisher = this.routeExecutor.filterPublisher(requestReference, responsePublisher);
      Scheduler scheduler = Schedulers.fromExecutorService(ctx.channel().eventLoop());
      Mono.from(finalPublisher)
         .publishOn(scheduler)
         .subscribeOn(scheduler)
         .contextWrite(reactorContext -> reactorContext.put("micronaut.http.server.request", requestReference.get()))
         .subscribe(
            actualResponse -> {
               if (actualResponse == proceed) {
                  UriRouteMatch routeMatch = (UriRouteMatch)actualResponse.getAttribute(HttpAttributes.ROUTE_MATCH, UriRouteMatch.class).get();
                  WebSocketBean<?> webSocketBean = this.webSocketBeanRegistry.getWebSocket(routeMatch.getTarget().getClass());
                  this.handleHandshake(ctx, msg, webSocketBean, actualResponse);
                  ChannelPipeline pipeline = ctx.pipeline();
      
                  try {
                     NettyServerWebSocketHandler webSocketHandler = new NettyServerWebSocketHandler(
                        this.nettyEmbeddedServices,
                        this.webSocketSessionRepository,
                        this.handshaker,
                        webSocketBean,
                        msg,
                        routeMatch,
                        ctx,
                        (CoroutineHelper)this.routeExecutor.getCoroutineHelper().orElse(null)
                     );
                     pipeline.addBefore(ctx.name(), "websocket-handler", webSocketHandler);
                     pipeline.remove("http-streams-codec");
                     pipeline.remove(this);
                     ChannelHandler accessLoggerHandler = pipeline.get("http-access-logger");
                     if (accessLoggerHandler != null) {
                        pipeline.remove(accessLoggerHandler);
                     }
                  } catch (Throwable var10) {
                     if (LOG.isErrorEnabled()) {
                        LOG.error("Error opening WebSocket: " + var10.getMessage(), var10);
                     }
      
                     ctx.writeAndFlush(new CloseWebSocketFrame(CloseReason.INTERNAL_ERROR.getCode(), CloseReason.INTERNAL_ERROR.getReason()));
                  }
               } else {
                  ctx.writeAndFlush(actualResponse);
               }
      
            }
         );
   }

   protected ChannelFuture handleHandshake(ChannelHandlerContext ctx, NettyHttpRequest req, WebSocketBean<?> webSocketBean, MutableHttpResponse<?> response) {
      int maxFramePayloadLength = webSocketBean.messageMethod().map(m -> m.intValue(OnMessage.class, "maxPayloadLength").orElse(65536)).orElse(65536);
      String subprotocols = (String)webSocketBean.getBeanDefinition()
         .stringValue(ServerWebSocket.class, "subprotocols")
         .filter(s -> !StringUtils.isEmpty(s))
         .orElse(null);
      WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
         this.getWebSocketURL(ctx, req), subprotocols, true, maxFramePayloadLength
      );
      this.handshaker = wsFactory.newHandshaker(req.getNativeRequest());
      MutableHttpHeaders headers = response.getHeaders();
      HttpHeaders nettyHeaders;
      if (headers instanceof NettyHttpHeaders) {
         nettyHeaders = ((NettyHttpHeaders)headers).getNettyHeaders();
      } else {
         nettyHeaders = new DefaultHttpHeaders();

         for(Entry<String, List<String>> entry : headers) {
            nettyHeaders.add((String)entry.getKey(), (Iterable<?>)entry.getValue());
         }
      }

      Channel channel = ctx.channel();
      return this.handshaker == null
         ? WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channel)
         : this.handshaker.handshake(channel, req.getNativeRequest(), nettyHeaders, channel.newPromise());
   }

   protected String getWebSocketURL(ChannelHandlerContext ctx, HttpRequest req) {
      boolean isSecure = ctx.pipeline().get(SslHandler.class) != null;
      return (isSecure ? "wss://" : "ws://") + (String)req.getHeaders().get(HttpHeaderNames.HOST) + req.getUri();
   }
}
