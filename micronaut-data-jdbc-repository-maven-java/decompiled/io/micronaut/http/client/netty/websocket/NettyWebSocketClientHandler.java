package io.micronaut.http.client.netty.websocket;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.bind.BoundExecutable;
import io.micronaut.core.bind.DefaultExecutableBinder;
import io.micronaut.core.bind.ExecutableBinder;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.bind.DefaultRequestBinderRegistry;
import io.micronaut.http.bind.RequestBinderRegistry;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.netty.websocket.AbstractNettyWebSocketHandler;
import io.micronaut.http.netty.websocket.NettyWebSocketSession;
import io.micronaut.http.uri.UriMatchInfo;
import io.micronaut.http.uri.UriMatchTemplate;
import io.micronaut.inject.MethodExecutionHandle;
import io.micronaut.websocket.CloseReason;
import io.micronaut.websocket.WebSocketPongMessage;
import io.micronaut.websocket.annotation.ClientWebSocket;
import io.micronaut.websocket.bind.WebSocketState;
import io.micronaut.websocket.bind.WebSocketStateBinderRegistry;
import io.micronaut.websocket.context.WebSocketBean;
import io.micronaut.websocket.exceptions.WebSocketClientException;
import io.micronaut.websocket.exceptions.WebSocketSessionException;
import io.micronaut.websocket.interceptor.WebSocketSessionAware;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Internal
public class NettyWebSocketClientHandler<T> extends AbstractNettyWebSocketHandler {
   private final WebSocketClientHandshaker handshaker;
   private final WebSocketBean<T> genericWebSocketBean;
   private final FluxSink<T> emitter;
   private final UriMatchInfo matchInfo;
   private final MediaTypeCodecRegistry codecRegistry;
   private ChannelPromise handshakeFuture;
   private NettyWebSocketSession clientSession;
   private final WebSocketStateBinderRegistry webSocketStateBinderRegistry;
   private FullHttpResponse handshakeResponse;
   private Argument<?> clientBodyArgument;
   private Argument<?> clientPongArgument;

   public NettyWebSocketClientHandler(
      MutableHttpRequest<?> request,
      WebSocketBean<T> webSocketBean,
      final WebSocketClientHandshaker handshaker,
      RequestBinderRegistry requestBinderRegistry,
      MediaTypeCodecRegistry mediaTypeCodecRegistry,
      FluxSink<T> emitter
   ) {
      super(
         null,
         requestBinderRegistry,
         mediaTypeCodecRegistry,
         webSocketBean,
         request,
         Collections.emptyMap(),
         handshaker.version(),
         handshaker.actualSubprotocol(),
         null
      );
      this.codecRegistry = mediaTypeCodecRegistry;
      this.handshaker = handshaker;
      this.genericWebSocketBean = webSocketBean;
      this.emitter = emitter;
      this.webSocketStateBinderRegistry = new WebSocketStateBinderRegistry(
         (RequestBinderRegistry)(requestBinderRegistry != null ? requestBinderRegistry : new DefaultRequestBinderRegistry(ConversionService.SHARED))
      );
      String clientPath = (String)webSocketBean.getBeanDefinition().stringValue(ClientWebSocket.class).orElse("");
      UriMatchTemplate matchTemplate = UriMatchTemplate.of(clientPath);
      this.matchInfo = (UriMatchInfo)matchTemplate.match(request.getPath()).orElse(null);
      this.callOpenMethod(null);
   }

   @Override
   public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
      if (evt instanceof IdleStateEvent) {
         IdleStateEvent idleStateEvent = (IdleStateEvent)evt;
         if (idleStateEvent.state() == IdleState.ALL_IDLE && this.clientSession != null && this.clientSession.isOpen()) {
            this.clientSession.close(CloseReason.NORMAL);
         }
      } else {
         super.userEventTriggered(ctx, evt);
      }

   }

   @Override
   public Argument<?> getBodyArgument() {
      return this.clientBodyArgument;
   }

   @Override
   public Argument<?> getPongArgument() {
      return this.clientPongArgument;
   }

   @Override
   public NettyWebSocketSession getSession() {
      return this.clientSession;
   }

   @Override
   public void handlerAdded(final ChannelHandlerContext ctx) {
      this.handshakeFuture = ctx.newPromise();
   }

   @Override
   public void channelActive(final ChannelHandlerContext ctx) {
      this.handshaker.handshake(ctx.channel());
   }

   @Override
   protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
      Channel ch = ctx.channel();
      if (!this.handshaker.isHandshakeComplete()) {
         FullHttpResponse res = (FullHttpResponse)msg;
         this.handshakeResponse = res;

         try {
            this.handshaker.finishHandshake(ch, res);
         } catch (Exception var32) {
            Exception e = var32;

            try {
               this.emitter.error(new WebSocketClientException("Error finishing WebSocket handshake: " + e.getMessage(), e));
            } finally {
               ch.writeAndFlush(new CloseWebSocketFrame(CloseReason.INTERNAL_ERROR.getCode(), CloseReason.INTERNAL_ERROR.getReason()));
               ch.close();
            }

            return;
         }

         this.handshakeFuture.setSuccess();
         this.clientSession = this.createWebSocketSession(ctx);
         T targetBean = this.genericWebSocketBean.getTarget();
         if (targetBean instanceof WebSocketSessionAware) {
            ((WebSocketSessionAware)targetBean).setWebSocketSession(this.clientSession);
         }

         ExecutableBinder<WebSocketState> binder = new DefaultExecutableBinder<>();
         BoundExecutable<?, ?> bound = binder.tryBind(
            this.messageHandler.getExecutableMethod(), this.webSocketBinder, new WebSocketState(this.clientSession, this.originatingRequest)
         );
         List<Argument<?>> unboundArguments = bound.getUnboundArguments();
         if (unboundArguments.size() != 1) {
            this.clientBodyArgument = null;

            try {
               this.emitter
                  .error(
                     new WebSocketClientException(
                        "WebSocket @OnMessage method "
                           + targetBean.getClass().getSimpleName()
                           + "."
                           + this.messageHandler.getExecutableMethod()
                           + " should define exactly 1 message parameter, but found 2 possible candidates: "
                           + unboundArguments
                     )
                  );
            } finally {
               if (this.getSession().isOpen()) {
                  this.getSession().close(CloseReason.INTERNAL_ERROR);
               }

            }

         } else {
            this.clientBodyArgument = (Argument)unboundArguments.iterator().next();
            if (this.pongHandler != null) {
               BoundExecutable<?, ?> boundPong = binder.tryBind(
                  this.pongHandler.getExecutableMethod(), this.webSocketBinder, new WebSocketState(this.clientSession, this.originatingRequest)
               );
               List<Argument<?>> unboundPongArguments = boundPong.getUnboundArguments();
               if (unboundPongArguments.size() != 1 || !((Argument)unboundPongArguments.get(0)).isAssignableFrom(WebSocketPongMessage.class)) {
                  this.clientPongArgument = null;

                  try {
                     this.emitter
                        .error(
                           new WebSocketClientException(
                              "WebSocket @OnMessage pong handler method "
                                 + targetBean.getClass().getSimpleName()
                                 + "."
                                 + this.messageHandler.getExecutableMethod()
                                 + " should define exactly 1 pong message parameter, but found: "
                                 + unboundArguments
                           )
                        );
                  } finally {
                     if (this.getSession().isOpen()) {
                        this.getSession().close(CloseReason.INTERNAL_ERROR);
                     }

                  }

                  return;
               }

               this.clientPongArgument = (Argument)unboundPongArguments.get(0);
            }

            Optional<? extends MethodExecutionHandle<?, ?>> opt = this.webSocketBean.openMethod();
            if (opt.isPresent()) {
               MethodExecutionHandle<?, ?> openMethod = (MethodExecutionHandle)opt.get();
               WebSocketState webSocketState = new WebSocketState(this.clientSession, this.originatingRequest);

               try {
                  BoundExecutable openMethodBound = binder.bind(openMethod.getExecutableMethod(), this.webSocketStateBinderRegistry, webSocketState);
                  Object target = openMethod.getTarget();
                  Object result = openMethodBound.invoke(target);
                  if (Publishers.isConvertibleToPublisher(result)) {
                     Publisher<?> reactiveSequence = Publishers.convertPublisher(result, Publisher.class);
                     Flux.from(reactiveSequence)
                        .subscribe(
                           o -> {
                           },
                           error -> this.emitter.error(new WebSocketSessionException("Error opening WebSocket client session: " + error.getMessage(), error)),
                           () -> {
                              this.emitter.next(targetBean);
                              this.emitter.complete();
                           }
                        );
                  } else {
                     this.emitter.next(targetBean);
                     this.emitter.complete();
                  }
               } catch (Throwable var35) {
                  this.emitter.error(new WebSocketClientException("Error opening WebSocket client session: " + var35.getMessage(), var35));
                  if (this.getSession().isOpen()) {
                     this.getSession().close(CloseReason.INTERNAL_ERROR);
                  }
               }
            } else {
               this.emitter.next(targetBean);
               this.emitter.complete();
            }

         }
      } else {
         if (msg instanceof WebSocketFrame) {
            this.handleWebSocketFrame(ctx, (WebSocketFrame)msg);
         } else {
            ctx.fireChannelRead(msg);
         }

      }
   }

   @Override
   protected NettyWebSocketSession createWebSocketSession(ChannelHandlerContext ctx) {
      return ctx != null
         ? new NettyWebSocketSession(
            this.handshakeResponse.headers().get(HttpHeaderNames.SEC_WEBSOCKET_ACCEPT),
            ctx.channel(),
            this.originatingRequest,
            this.codecRegistry,
            this.handshaker.version().toHttpHeaderValue(),
            ctx.pipeline().get(SslHandler.class) != null
         ) {
            @Override
            public ConvertibleValues<Object> getUriVariables() {
               return NettyWebSocketClientHandler.this.matchInfo != null
                  ? ConvertibleValues.of(NettyWebSocketClientHandler.this.matchInfo.getVariableValues())
                  : ConvertibleValues.empty();
            }
         }
         : null;
   }

   @Override
   public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
      if (!this.handshakeFuture.isDone()) {
         this.handshakeFuture.setFailure(cause);
      }

      super.exceptionCaught(ctx, cause);
   }
}
