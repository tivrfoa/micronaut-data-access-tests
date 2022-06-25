package io.micronaut.http.netty.websocket;

import io.micronaut.buffer.netty.NettyByteBufferFactory;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.bind.ArgumentBinderRegistry;
import io.micronaut.core.bind.BoundExecutable;
import io.micronaut.core.bind.DefaultExecutableBinder;
import io.micronaut.core.bind.ExecutableBinder;
import io.micronaut.core.bind.exceptions.UnsatisfiedArgumentException;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.bind.RequestBinderRegistry;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.MethodExecutionHandle;
import io.micronaut.websocket.CloseReason;
import io.micronaut.websocket.WebSocketPongMessage;
import io.micronaut.websocket.bind.WebSocketState;
import io.micronaut.websocket.bind.WebSocketStateBinderRegistry;
import io.micronaut.websocket.context.WebSocketBean;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Internal
public abstract class AbstractNettyWebSocketHandler extends SimpleChannelInboundHandler<Object> {
   public static final String ID = "websocket-handler";
   protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
   protected final ArgumentBinderRegistry<WebSocketState> webSocketBinder;
   protected final Map<String, Object> uriVariables;
   protected final WebSocketBean<?> webSocketBean;
   protected final HttpRequest<?> originatingRequest;
   protected final MethodExecutionHandle<?, ?> messageHandler;
   protected final MethodExecutionHandle<?, ?> pongHandler;
   protected final NettyWebSocketSession session;
   protected final MediaTypeCodecRegistry mediaTypeCodecRegistry;
   protected final WebSocketVersion webSocketVersion;
   protected final String subProtocol;
   protected final WebSocketSessionRepository webSocketSessionRepository;
   private final Argument<?> bodyArgument;
   private final Argument<?> pongArgument;
   private final AtomicBoolean closed = new AtomicBoolean(false);
   private AtomicReference<CompositeByteBuf> frameBuffer = new AtomicReference();

   protected AbstractNettyWebSocketHandler(
      ChannelHandlerContext ctx,
      RequestBinderRegistry binderRegistry,
      MediaTypeCodecRegistry mediaTypeCodecRegistry,
      WebSocketBean<?> webSocketBean,
      HttpRequest<?> request,
      Map<String, Object> uriVariables,
      WebSocketVersion version,
      String subProtocol,
      WebSocketSessionRepository webSocketSessionRepository
   ) {
      this.subProtocol = subProtocol;
      this.webSocketSessionRepository = webSocketSessionRepository;
      this.webSocketBinder = new WebSocketStateBinderRegistry(binderRegistry);
      this.uriVariables = uriVariables;
      this.webSocketBean = webSocketBean;
      this.originatingRequest = request;
      this.messageHandler = (MethodExecutionHandle)webSocketBean.messageMethod().orElse(null);
      this.pongHandler = (MethodExecutionHandle)webSocketBean.pongMethod().orElse(null);
      this.mediaTypeCodecRegistry = mediaTypeCodecRegistry;
      this.webSocketVersion = version;
      this.session = this.createWebSocketSession(ctx);
      if (this.session != null) {
         ExecutableBinder<WebSocketState> binder = new DefaultExecutableBinder<>();
         if (this.messageHandler != null) {
            BoundExecutable<?, ?> bound = binder.tryBind(
               this.messageHandler.getExecutableMethod(), this.webSocketBinder, new WebSocketState(this.session, this.originatingRequest)
            );
            List<Argument<?>> unboundArguments = bound.getUnboundArguments();
            if (unboundArguments.size() == 1) {
               this.bodyArgument = (Argument)unboundArguments.iterator().next();
            } else {
               this.bodyArgument = null;
               if (this.LOG.isErrorEnabled()) {
                  this.LOG
                     .error(
                        "WebSocket @OnMessage method "
                           + webSocketBean.getTarget()
                           + "."
                           + this.messageHandler.getExecutableMethod()
                           + " should define exactly 1 message parameter, but found 2 possible candidates: "
                           + unboundArguments
                     );
               }

               if (this.session.isOpen()) {
                  this.session.close(CloseReason.INTERNAL_ERROR);
               }
            }
         } else {
            this.bodyArgument = null;
         }

         if (this.pongHandler != null) {
            BoundExecutable<?, ?> bound = binder.tryBind(
               this.pongHandler.getExecutableMethod(), this.webSocketBinder, new WebSocketState(this.session, this.originatingRequest)
            );
            List<Argument<?>> unboundArguments = bound.getUnboundArguments();
            if (unboundArguments.size() == 1 && ((Argument)unboundArguments.get(0)).isAssignableFrom(WebSocketPongMessage.class)) {
               this.pongArgument = (Argument)unboundArguments.get(0);
            } else {
               this.pongArgument = null;
               if (this.LOG.isErrorEnabled()) {
                  this.LOG
                     .error(
                        "WebSocket @OnMessage pong handler method "
                           + webSocketBean.getTarget()
                           + "."
                           + this.pongHandler.getExecutableMethod()
                           + " should define exactly 1 message parameter assignable from a WebSocketPongMessage, but found: "
                           + unboundArguments
                     );
               }

               if (this.session.isOpen()) {
                  this.session.close(CloseReason.INTERNAL_ERROR);
               }
            }
         } else {
            this.pongArgument = null;
         }
      } else {
         this.bodyArgument = null;
         this.pongArgument = null;
      }

   }

   protected void callOpenMethod(ChannelHandlerContext ctx) {
      if (this.session != null) {
         Optional<? extends MethodExecutionHandle<?, ?>> executionHandle = this.webSocketBean.openMethod();
         if (executionHandle.isPresent()) {
            MethodExecutionHandle<?, ?> openMethod = (MethodExecutionHandle)executionHandle.get();
            BoundExecutable boundExecutable = null;

            try {
               boundExecutable = this.bindMethod(this.originatingRequest, this.webSocketBinder, openMethod, Collections.emptyList());
            } catch (Throwable var9) {
               if (this.LOG.isErrorEnabled()) {
                  this.LOG.error("Error Binding method @OnOpen for WebSocket [" + this.webSocketBean + "]: " + var9.getMessage(), var9);
               }

               if (this.session.isOpen()) {
                  this.session.close(CloseReason.INTERNAL_ERROR);
               }
            }

            if (boundExecutable != null) {
               try {
                  Object result = this.invokeExecutable(boundExecutable, openMethod);
                  if (Publishers.isConvertibleToPublisher(result)) {
                     Flux<?> flowable = Flux.from(this.instrumentPublisher(ctx, result));
                     flowable.subscribe(o -> {
                     }, error -> {
                        if (this.LOG.isErrorEnabled()) {
                           this.LOG.error("Error Opening WebSocket [" + this.webSocketBean + "]: " + error.getMessage(), error);
                        }

                        if (this.session.isOpen()) {
                           this.session.close(CloseReason.INTERNAL_ERROR);
                        }

                     }, () -> {
                     });
                  }
               } catch (Throwable var8) {
                  this.forwardErrorToUser(ctx, t -> {
                     if (this.LOG.isErrorEnabled()) {
                        this.LOG.error("Error Opening WebSocket [" + this.webSocketBean + "]: " + t.getMessage(), t);
                     }

                  }, var8);
                  if (this.session.isOpen()) {
                     this.session.close(CloseReason.INTERNAL_ERROR);
                  }
               }
            }
         }

      }
   }

   public Argument<?> getBodyArgument() {
      return this.bodyArgument;
   }

   public Argument<?> getPongArgument() {
      return this.pongArgument;
   }

   public NettyWebSocketSession getSession() {
      return this.session;
   }

   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      this.cleanupBuffer();
      this.forwardErrorToUser(ctx, e -> this.handleUnexpected(ctx, e), cause);
   }

   private void forwardErrorToUser(ChannelHandlerContext ctx, Consumer<Throwable> fallback, Throwable cause) {
      Optional<? extends MethodExecutionHandle<?, ?>> opt = this.webSocketBean.errorMethod();
      if (opt.isPresent()) {
         MethodExecutionHandle<?, ?> errorMethod = (MethodExecutionHandle)opt.get();

         try {
            BoundExecutable boundExecutable = this.bindMethod(this.originatingRequest, this.webSocketBinder, errorMethod, Collections.singletonList(cause));
            Object target = errorMethod.getTarget();

            Object result;
            try {
               result = boundExecutable.invoke(target);
            } catch (Exception var10) {
               if (this.LOG.isErrorEnabled()) {
                  this.LOG
                     .error(
                        "Error invoking to @OnError handler "
                           + target.getClass().getSimpleName()
                           + "."
                           + errorMethod.getExecutableMethod()
                           + ": "
                           + var10.getMessage(),
                        var10
                     );
               }

               fallback.accept(var10);
               return;
            }

            if (Publishers.isConvertibleToPublisher(result)) {
               Flux<?> flowable = Flux.from(this.instrumentPublisher(ctx, result));
               flowable.collectList()
                  .subscribe(
                     objects -> fallback.accept(cause),
                     throwable -> {
                        if (throwable != null && this.LOG.isErrorEnabled()) {
                           this.LOG
                              .error(
                                 "Error subscribing to @OnError handler "
                                    + target.getClass().getSimpleName()
                                    + "."
                                    + errorMethod.getExecutableMethod()
                                    + ": "
                                    + throwable.getMessage(),
                                 throwable
                              );
                        }
      
                        fallback.accept(cause);
                     }
                  );
            }
         } catch (UnsatisfiedArgumentException var11) {
            fallback.accept(cause);
         }
      } else {
         fallback.accept(cause);
      }

   }

   @Override
   public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
      this.handleCloseReason(ctx, CloseReason.ABNORMAL_CLOSURE, false);
   }

   protected abstract NettyWebSocketSession createWebSocketSession(ChannelHandlerContext ctx);

   protected Publisher<?> instrumentPublisher(ChannelHandlerContext ctx, Object result) {
      Publisher<?> actual = Publishers.convertPublisher(result, Publisher.class);
      return Flux.from(actual).subscribeOn(Schedulers.fromExecutorService(ctx.channel().eventLoop()));
   }

   protected Object invokeExecutable(BoundExecutable boundExecutable, MethodExecutionHandle<?, ?> messageHandler) {
      return boundExecutable.invoke(messageHandler.getTarget());
   }

   @Override
   protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
      if (msg instanceof WebSocketFrame) {
         this.handleWebSocketFrame(ctx, (WebSocketFrame)msg);
      } else {
         ctx.fireChannelRead(msg);
      }

   }

   protected void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame msg) {
      if (!(msg instanceof TextWebSocketFrame) && !(msg instanceof BinaryWebSocketFrame) && !(msg instanceof ContinuationWebSocketFrame)) {
         if (msg instanceof PingWebSocketFrame) {
            PingWebSocketFrame frame = (PingWebSocketFrame)msg.retain();
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content()));
         } else if (msg instanceof PongWebSocketFrame) {
            if (this.pongHandler != null) {
               ByteBuf content = msg.content();
               WebSocketPongMessage message = new WebSocketPongMessage(NettyByteBufferFactory.DEFAULT.wrap(content));
               NettyWebSocketSession currentSession = this.getSession();
               ExecutableBinder<WebSocketState> executableBinder = new DefaultExecutableBinder<>(Collections.singletonMap(this.getPongArgument(), message));

               try {
                  BoundExecutable boundExecutable = executableBinder.bind(
                     this.pongHandler.getExecutableMethod(), this.webSocketBinder, new WebSocketState(currentSession, this.originatingRequest)
                  );
                  Object result = this.invokeExecutable(boundExecutable, this.pongHandler);
                  if (Publishers.isConvertibleToPublisher(result)) {
                     content.retain();
                     Flux<?> flowable = Flux.from(this.instrumentPublisher(ctx, result));
                     flowable.subscribe(o -> {
                     }, error -> {
                        if (this.LOG.isErrorEnabled()) {
                           this.LOG.error("Error Processing WebSocket Pong Message [" + this.webSocketBean + "]: " + error.getMessage(), error);
                        }

                        this.exceptionCaught(ctx, error);
                     }, content::release);
                  }
               } catch (Throwable var17) {
                  if (this.LOG.isErrorEnabled()) {
                     this.LOG.error("Error Processing WebSocket Message [" + this.webSocketBean + "]: " + var17.getMessage(), var17);
                  }

                  this.exceptionCaught(ctx, var17);
               }
            }
         } else if (msg instanceof CloseWebSocketFrame) {
            CloseWebSocketFrame cwsf = (CloseWebSocketFrame)msg;
            this.handleCloseFrame(ctx, cwsf);
         } else {
            this.writeCloseFrameAndTerminate(ctx, CloseReason.UNSUPPORTED_DATA);
         }
      } else if (this.messageHandler == null) {
         if (this.LOG.isDebugEnabled()) {
            this.LOG.debug("WebSocket bean [" + this.webSocketBean.getTarget() + "] received message, but defined no @OnMessage handler. Dropping frame...");
         }

         this.writeCloseFrameAndTerminate(ctx, CloseReason.UNSUPPORTED_DATA);
      } else {
         ByteBuf msgContent = msg.content().retain();
         if (!msg.isFinalFragment()) {
            this.frameBuffer.updateAndGet(buffer -> {
               if (buffer == null) {
                  buffer = ctx.alloc().compositeBuffer();
               }

               buffer.addComponent(true, msgContent);
               return buffer;
            });
            return;
         }

         CompositeByteBuf buffer = (CompositeByteBuf)this.frameBuffer.getAndSet(null);
         ByteBuf content;
         if (buffer == null) {
            content = msgContent;
         } else {
            buffer.addComponent(true, msgContent);
            content = buffer;
         }

         Argument<?> bodyArgument = this.getBodyArgument();
         Optional<?> converted = ConversionService.SHARED.convert(content, bodyArgument);
         content.release();
         if (!converted.isPresent()) {
            MediaType mediaType;
            try {
               mediaType = (MediaType)this.messageHandler.stringValue(Consumes.class).map(MediaType::of).orElse(MediaType.APPLICATION_JSON_TYPE);
            } catch (IllegalArgumentException var16) {
               this.exceptionCaught(ctx, var16);
               return;
            }

            try {
               converted = this.mediaTypeCodecRegistry
                  .findCodec(mediaType)
                  .map(codec -> codec.decode(bodyArgument, new NettyByteBufferFactory(ctx.alloc()).wrap(msg.content())));
            } catch (CodecException var15) {
               this.messageProcessingException(ctx, var15);
               return;
            }
         }

         if (converted.isPresent()) {
            Object v = converted.get();
            NettyWebSocketSession currentSession = this.getSession();
            ExecutableBinder<WebSocketState> executableBinder = new DefaultExecutableBinder<>(Collections.singletonMap(bodyArgument, v));

            try {
               BoundExecutable boundExecutable = executableBinder.bind(
                  this.messageHandler.getExecutableMethod(), this.webSocketBinder, new WebSocketState(currentSession, this.originatingRequest)
               );
               Object result = this.invokeExecutable(boundExecutable, this.messageHandler);
               if (Publishers.isConvertibleToPublisher(result)) {
                  Flux<?> flowable = Flux.from(this.instrumentPublisher(ctx, result));
                  flowable.subscribe(o -> {
                  }, error -> this.messageProcessingException(ctx, error), () -> this.messageHandled(ctx, this.session, v));
               } else {
                  this.messageHandled(ctx, this.session, v);
               }
            } catch (Throwable var14) {
               this.messageProcessingException(ctx, var14);
            }
         } else {
            this.writeCloseFrameAndTerminate(
               ctx,
               CloseReason.UNSUPPORTED_DATA.getCode(),
               CloseReason.UNSUPPORTED_DATA.getReason() + ": Received data cannot be converted to target type: " + bodyArgument
            );
         }
      }

   }

   private void messageProcessingException(ChannelHandlerContext ctx, Throwable e) {
      if (this.LOG.isErrorEnabled()) {
         this.LOG.error("Error Processing WebSocket Message [" + this.webSocketBean + "]: " + e.getMessage(), e);
      }

      this.exceptionCaught(ctx, e);
   }

   protected void messageHandled(ChannelHandlerContext ctx, NettyWebSocketSession session, Object message) {
   }

   protected void writeCloseFrameAndTerminate(ChannelHandlerContext ctx, CloseReason closeReason) {
      int code = closeReason.getCode();
      String reason = closeReason.getReason();
      this.writeCloseFrameAndTerminate(ctx, code, reason);
   }

   private void handleCloseReason(ChannelHandlerContext ctx, CloseReason cr, boolean writeCloseReason) {
      this.cleanupBuffer();
      if (this.closed.compareAndSet(false, true)) {
         if (this.LOG.isDebugEnabled()) {
            this.LOG.debug("Closing WebSocket session {} with reason {}", this.getSession(), cr);
         }

         Optional<? extends MethodExecutionHandle<?, ?>> opt = this.webSocketBean.closeMethod();
         if (opt.isPresent()) {
            MethodExecutionHandle<?, ?> methodExecutionHandle = (MethodExecutionHandle)opt.get();
            Object target = methodExecutionHandle.getTarget();

            try {
               BoundExecutable boundExecutable = this.bindMethod(
                  this.originatingRequest, this.webSocketBinder, methodExecutionHandle, Collections.singletonList(cr)
               );
               this.invokeAndClose(ctx, target, boundExecutable, methodExecutionHandle, true);
            } catch (Throwable var8) {
               if (this.LOG.isErrorEnabled()) {
                  this.LOG.error("Error invoking @OnClose handler for WebSocket bean [" + target + "]: " + var8.getMessage(), var8);
               }
            }
         } else if (writeCloseReason) {
            this.writeCloseFrameAndTerminate(ctx, cr);
         }
      }

   }

   private void handleCloseFrame(ChannelHandlerContext ctx, CloseWebSocketFrame cwsf) {
      CloseReason cr = new CloseReason(cwsf.statusCode(), cwsf.reasonText());
      this.handleCloseReason(ctx, cr, true);
   }

   private void invokeAndClose(
      ChannelHandlerContext ctx, Object target, BoundExecutable boundExecutable, MethodExecutionHandle<?, ?> methodExecutionHandle, boolean isClose
   ) {
      Object result;
      try {
         result = this.invokeExecutable(boundExecutable, methodExecutionHandle);
      } catch (Exception var8) {
         if (this.LOG.isErrorEnabled()) {
            this.LOG
               .error(
                  "Error invoking @OnClose handler "
                     + target.getClass().getSimpleName()
                     + "."
                     + methodExecutionHandle.getExecutableMethod()
                     + ": "
                     + var8.getMessage(),
                  var8
               );
         }

         ctx.close();
         return;
      }

      if (Publishers.isConvertibleToPublisher(result)) {
         Flux<?> reactiveSequence = Flux.from(this.instrumentPublisher(ctx, result));
         reactiveSequence.collectList()
            .subscribe(
               objects -> {
               },
               throwable -> {
                  if (throwable != null && this.LOG.isErrorEnabled()) {
                     this.LOG
                        .error(
                           "Error subscribing to @"
                              + (isClose ? "OnClose" : "OnError")
                              + " handler for WebSocket bean ["
                              + target
                              + "]: "
                              + throwable.getMessage(),
                           throwable
                        );
                  }
      
                  ctx.close();
               }
            );
      } else {
         ctx.close();
      }

   }

   private BoundExecutable bindMethod(
      HttpRequest<?> request, ArgumentBinderRegistry<WebSocketState> binderRegistry, MethodExecutionHandle<?, ?> openMethod, List<?> parameters
   ) {
      ExecutableMethod<?, ?> executable = openMethod.getExecutableMethod();
      Map<Argument<?>, Object> preBound = this.prepareBoundVariables(executable, parameters);
      ExecutableBinder<WebSocketState> executableBinder = new DefaultExecutableBinder<>(preBound);
      return executableBinder.bind(executable, binderRegistry, new WebSocketState(this.getSession(), request));
   }

   private Map<Argument<?>, Object> prepareBoundVariables(ExecutableMethod<?, ?> executable, List<?> parameters) {
      Map<Argument<?>, Object> preBound = new HashMap(executable.getArguments().length);

      for(Argument argument : executable.getArguments()) {
         Class type = argument.getType();

         for(Object object : parameters) {
            if (type.isInstance(object)) {
               preBound.put(argument, object);
               break;
            }
         }
      }

      return preBound;
   }

   private void handleUnexpected(ChannelHandlerContext ctx, Throwable cause) {
      if (cause instanceof IOException) {
         String msg = cause.getMessage();
         if (msg != null && msg.contains("Connection reset")) {
            return;
         }
      }

      if (this.LOG.isErrorEnabled()) {
         this.LOG.error("Unexpected Exception in WebSocket [" + this.webSocketBean.getTarget() + "]: " + cause.getMessage(), cause);
      }

      Channel channel = ctx.channel();
      if (channel.isOpen()) {
         CloseReason internalError = CloseReason.INTERNAL_ERROR;
         this.writeCloseFrameAndTerminate(ctx, internalError);
      }

   }

   private void writeCloseFrameAndTerminate(ChannelHandlerContext ctx, int code, String reason) {
      this.cleanupBuffer();
      CloseWebSocketFrame closeFrame = new CloseWebSocketFrame(code, reason);
      ctx.channel().writeAndFlush(closeFrame).addListener(future -> this.handleCloseFrame(ctx, new CloseWebSocketFrame(code, reason)));
   }

   private void cleanupBuffer() {
      CompositeByteBuf buffer = (CompositeByteBuf)this.frameBuffer.getAndSet(null);
      if (buffer != null) {
         buffer.release();
      }

   }
}
