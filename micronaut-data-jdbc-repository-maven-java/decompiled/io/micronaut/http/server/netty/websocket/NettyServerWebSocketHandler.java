package io.micronaut.http.server.netty.websocket;

import io.micronaut.context.event.ApplicationEventPublisher;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.bind.BoundExecutable;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.type.Executable;
import io.micronaut.core.util.KotlinUtils;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.bind.binders.ContinuationArgumentBinder;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.netty.websocket.AbstractNettyWebSocketHandler;
import io.micronaut.http.netty.websocket.NettyWebSocketSession;
import io.micronaut.http.netty.websocket.WebSocketSessionRepository;
import io.micronaut.http.server.CoroutineHelper;
import io.micronaut.http.server.netty.NettyEmbeddedServices;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.MethodExecutionHandle;
import io.micronaut.web.router.UriRouteMatch;
import io.micronaut.websocket.CloseReason;
import io.micronaut.websocket.WebSocketSession;
import io.micronaut.websocket.context.WebSocketBean;
import io.micronaut.websocket.event.WebSocketMessageProcessedEvent;
import io.micronaut.websocket.event.WebSocketSessionClosedEvent;
import io.micronaut.websocket.event.WebSocketSessionOpenEvent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateEvent;
import java.security.Principal;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Internal
public class NettyServerWebSocketHandler extends AbstractNettyWebSocketHandler {
   public static final String ID = "websocket-handler";
   private final NettyEmbeddedServices nettyEmbeddedServices;
   @Nullable
   private final CoroutineHelper coroutineHelper;

   NettyServerWebSocketHandler(
      NettyEmbeddedServices nettyEmbeddedServices,
      WebSocketSessionRepository webSocketSessionRepository,
      WebSocketServerHandshaker handshaker,
      WebSocketBean<?> webSocketBean,
      HttpRequest<?> request,
      UriRouteMatch<Object, Object> routeMatch,
      ChannelHandlerContext ctx,
      @Nullable CoroutineHelper coroutineHelper
   ) {
      super(
         ctx,
         nettyEmbeddedServices.getRequestArgumentSatisfier().getBinderRegistry(),
         nettyEmbeddedServices.getMediaTypeCodecRegistry(),
         webSocketBean,
         request,
         routeMatch.getVariableValues(),
         handshaker.version(),
         handshaker.selectedSubprotocol(),
         webSocketSessionRepository
      );
      this.nettyEmbeddedServices = nettyEmbeddedServices;
      this.coroutineHelper = coroutineHelper;
      request.setAttribute(HttpAttributes.ROUTE_MATCH, routeMatch);
      request.setAttribute(HttpAttributes.ROUTE, routeMatch.getRoute());
      this.callOpenMethod(ctx);
      ApplicationEventPublisher<WebSocketSessionOpenEvent> eventPublisher = nettyEmbeddedServices.getEventPublisher(WebSocketSessionOpenEvent.class);

      try {
         eventPublisher.publishEvent(new WebSocketSessionOpenEvent(this.session));
      } catch (Exception var11) {
         if (this.LOG.isErrorEnabled()) {
            this.LOG.error("Error publishing WebSocket opened event: " + var11.getMessage(), var11);
         }
      }

   }

   @Override
   public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      if (evt instanceof IdleStateEvent) {
         this.writeCloseFrameAndTerminate(ctx, CloseReason.GOING_AWAY);
      } else {
         super.userEventTriggered(ctx, evt);
      }

   }

   @Override
   public boolean acceptInboundMessage(Object msg) {
      return msg instanceof WebSocketFrame;
   }

   @Override
   protected NettyWebSocketSession createWebSocketSession(ChannelHandlerContext ctx) {
      String id = this.originatingRequest.getHeaders().get(HttpHeaderNames.SEC_WEBSOCKET_KEY);
      Channel channel = ctx.channel();
      NettyWebSocketSession session = new NettyWebSocketSession(
         id,
         channel,
         this.originatingRequest,
         this.mediaTypeCodecRegistry,
         this.webSocketVersion.toHttpHeaderValue(),
         ctx.pipeline().get(SslHandler.class) != null
      ) {
         private final ConvertibleValues<Object> uriVars = ConvertibleValues.of(NettyServerWebSocketHandler.this.uriVariables);

         @Override
         public Optional<String> getSubprotocol() {
            return Optional.ofNullable(NettyServerWebSocketHandler.this.subProtocol);
         }

         @Override
         public Set<? extends WebSocketSession> getOpenSessions() {
            return (Set<? extends WebSocketSession>)NettyServerWebSocketHandler.this.webSocketSessionRepository.getChannelGroup().stream().flatMap(ch -> {
               NettyWebSocketSession s = ch.attr(NettyWebSocketSession.WEB_SOCKET_SESSION_KEY).get();
               return s != null && s.isOpen() ? Stream.of(s) : Stream.empty();
            }).collect(Collectors.toSet());
         }

         @Override
         public void close(CloseReason closeReason) {
            super.close(closeReason);
            NettyServerWebSocketHandler.this.webSocketSessionRepository.removeChannel(ctx.channel());
         }

         @Override
         public Optional<Principal> getUserPrincipal() {
            return NettyServerWebSocketHandler.this.originatingRequest.getAttribute(HttpAttributes.PRINCIPAL, Principal.class);
         }

         @Override
         public ConvertibleValues<Object> getUriVariables() {
            return this.uriVars;
         }
      };
      this.webSocketSessionRepository.addChannel(channel);
      return session;
   }

   @Override
   protected Publisher<?> instrumentPublisher(ChannelHandlerContext ctx, Object result) {
      Publisher<?> actual = Publishers.convertPublisher(result, Publisher.class);
      Publisher<?> traced = subscriber -> ServerRequestContext.with(this.originatingRequest, (Runnable)(() -> actual.subscribe(new Subscriber<Object>() {
               @Override
               public void onSubscribe(Subscription s) {
                  ServerRequestContext.with(NettyServerWebSocketHandler.this.originatingRequest, (Runnable)(() -> subscriber.onSubscribe(s)));
               }

               @Override
               public void onNext(Object object) {
                  ServerRequestContext.with(NettyServerWebSocketHandler.this.originatingRequest, (Runnable)(() -> subscriber.onNext(object)));
               }

               @Override
               public void onError(Throwable t) {
                  ServerRequestContext.with(NettyServerWebSocketHandler.this.originatingRequest, (Runnable)(() -> subscriber.onError(t)));
               }

               @Override
               public void onComplete() {
                  ServerRequestContext.with(NettyServerWebSocketHandler.this.originatingRequest, subscriber::onComplete);
               }
            })));
      return Flux.from(traced).subscribeOn(Schedulers.fromExecutorService(ctx.channel().eventLoop()));
   }

   @Override
   protected Object invokeExecutable(BoundExecutable boundExecutable, MethodExecutionHandle<?, ?> messageHandler) {
      if (this.coroutineHelper != null) {
         Executable<?, ?> target = boundExecutable.getTarget();
         if (target instanceof ExecutableMethod) {
            ExecutableMethod<?, ?> executableMethod = (ExecutableMethod)target;
            if (executableMethod.isSuspend()) {
               return Flux.deferContextual(
                  ctx -> {
                     try {
                        this.coroutineHelper.setupCoroutineContext(this.originatingRequest, ctx);
                        Object immediateReturnValue = this.invokeExecutable0(boundExecutable, messageHandler);
                        return KotlinUtils.isKotlinCoroutineSuspended(immediateReturnValue)
                           ? Mono.fromCompletionStage(ContinuationArgumentBinder.extractContinuationCompletableFutureSupplier(this.originatingRequest))
                           : Mono.empty();
                     } catch (Exception var5) {
                        return Flux.error(var5);
                     }
                  }
               );
            }
         }
      }

      return this.invokeExecutable0(boundExecutable, messageHandler);
   }

   private Object invokeExecutable0(BoundExecutable boundExecutable, MethodExecutionHandle<?, ?> messageHandler) {
      return ServerRequestContext.with(this.originatingRequest, (Supplier)(() -> boundExecutable.invoke(messageHandler.getTarget())));
   }

   @Override
   protected void messageHandled(ChannelHandlerContext ctx, NettyWebSocketSession session, Object message) {
      ctx.executor()
         .execute(
            () -> {
               try {
                  this.nettyEmbeddedServices
                     .getEventPublisher(WebSocketMessageProcessedEvent.class)
                     .publishEvent(new WebSocketMessageProcessedEvent<Object>(session, message));
               } catch (Exception var4) {
                  if (this.LOG.isErrorEnabled()) {
                     this.LOG.error("Error publishing WebSocket message processed event: " + var4.getMessage(), var4);
                  }
               }
      
            }
         );
   }

   @Override
   public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
      Channel channel = ctx.channel();
      channel.attr(NettyWebSocketSession.WEB_SOCKET_SESSION_KEY).set(null);
      if (this.LOG.isDebugEnabled()) {
         this.LOG.debug("Removing WebSocket Server session: " + this.session);
      }

      this.webSocketSessionRepository.removeChannel(channel);

      try {
         this.nettyEmbeddedServices.getEventPublisher(WebSocketSessionClosedEvent.class).publishEvent(new WebSocketSessionClosedEvent(this.session));
      } catch (Exception var4) {
         if (this.LOG.isErrorEnabled()) {
            this.LOG.error("Error publishing WebSocket closed event: " + var4.getMessage(), var4);
         }
      }

      super.handlerRemoved(ctx);
   }
}
