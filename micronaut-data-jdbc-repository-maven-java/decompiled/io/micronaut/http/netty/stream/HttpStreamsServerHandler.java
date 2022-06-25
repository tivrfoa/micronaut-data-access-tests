package io.micronaut.http.netty.stream;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.netty.reactive.CancelledSubscriber;
import io.micronaut.http.netty.reactive.HandlerPublisher;
import io.micronaut.http.netty.reactive.HandlerSubscriber;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import org.reactivestreams.Publisher;

@Internal
public class HttpStreamsServerHandler extends HttpStreamsHandler<HttpRequest, HttpResponse> {
   private HttpRequest lastRequest = null;
   private HttpResponse webSocketResponse = null;
   private ChannelPromise webSocketResponseChannelPromise = null;
   private int inFlight = 0;
   private boolean continueExpected = true;
   private boolean sendContinue = false;
   private boolean close = false;
   private final List<ChannelHandler> dependentHandlers;

   public HttpStreamsServerHandler() {
      this(Collections.emptyList());
   }

   public HttpStreamsServerHandler(List<ChannelHandler> dependentHandlers) {
      super(HttpRequest.class, HttpResponse.class);
      this.dependentHandlers = dependentHandlers;
   }

   protected boolean hasBody(HttpRequest request) {
      if (request.decoderResult().isFailure()) {
         return false;
      } else {
         int contentLength;
         try {
            contentLength = HttpUtil.getContentLength(request, 0);
         } catch (NumberFormatException var4) {
            contentLength = 0;
         }

         return contentLength != 0 || HttpUtil.isTransferEncodingChunked(request);
      }
   }

   protected HttpRequest createEmptyMessage(HttpRequest request) {
      return new EmptyHttpRequest(request);
   }

   protected HttpRequest createStreamedMessage(HttpRequest httpRequest, Publisher<? extends HttpContent> stream) {
      return new DelegateStreamedHttpRequest(httpRequest, stream);
   }

   @Override
   public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      this.continueExpected = false;
      this.sendContinue = false;
      if (msg instanceof HttpRequest) {
         HttpRequest request = (HttpRequest)msg;
         this.lastRequest = request;
         if (HttpUtil.is100ContinueExpected(request)) {
            this.continueExpected = true;
         }
      }

      super.channelRead(ctx, msg);
   }

   @Override
   protected void receivedInMessage(ChannelHandlerContext ctx) {
      ++this.inFlight;
   }

   @Override
   protected void sentOutMessage(ChannelHandlerContext ctx) {
      --this.inFlight;
      if (this.inFlight == 1 && this.continueExpected && this.sendContinue) {
         ctx.writeAndFlush(new DefaultFullHttpResponse(this.lastRequest.protocolVersion(), HttpResponseStatus.CONTINUE));
         this.sendContinue = false;
         this.continueExpected = false;
      }

      if (this.close) {
         ctx.close();
      }

   }

   protected void unbufferedWrite(ChannelHandlerContext ctx, HttpResponse message, ChannelPromise promise) {
      if (message instanceof WebSocketHttpResponse) {
         if (!(this.lastRequest instanceof FullHttpRequest) && this.hasBody(this.lastRequest)) {
            this.webSocketResponse = message;
            this.webSocketResponseChannelPromise = promise;
         } else {
            this.handleWebSocketResponse(ctx, message, promise);
         }
      } else {
         if (this.lastRequest.protocolVersion().isKeepAliveDefault()) {
            if (message.headers().contains(HttpHeaderNames.CONNECTION, "close", true)) {
               this.close = true;
            }
         } else if (!message.headers().contains(HttpHeaderNames.CONNECTION, "keep-alive", true)) {
            this.close = true;
         }

         if (this.inFlight == 1 && this.continueExpected) {
            HttpUtil.setKeepAlive(message, false);
            this.close = true;
            this.continueExpected = false;
         }

         if (!HttpUtil.isContentLengthSet(message) && !HttpUtil.isTransferEncodingChunked(message) && this.canHaveBody(message)) {
            HttpUtil.setKeepAlive(message, false);
            this.close = true;
         }

         super.unbufferedWrite(ctx, message, promise);
      }

   }

   @Override
   protected boolean isValidOutMessage(Object msg) {
      return msg instanceof FullHttpResponse || msg instanceof StreamedHttpResponse || msg instanceof WebSocketHttpResponse;
   }

   private boolean canHaveBody(HttpResponse message) {
      HttpResponseStatus status = message.status();
      return status != HttpResponseStatus.CONTINUE
         && status != HttpResponseStatus.SWITCHING_PROTOCOLS
         && status != HttpResponseStatus.PROCESSING
         && status != HttpResponseStatus.NO_CONTENT
         && status != HttpResponseStatus.NOT_MODIFIED;
   }

   @Override
   protected void consumedInMessage(ChannelHandlerContext ctx) {
      if (this.webSocketResponse != null) {
         this.handleWebSocketResponse(ctx, this.webSocketResponse, this.webSocketResponseChannelPromise);
         this.webSocketResponse = null;
         this.webSocketResponseChannelPromise = null;
      }

   }

   private void handleWebSocketResponse(ChannelHandlerContext ctx, HttpResponse message, ChannelPromise promise) {
      WebSocketHttpResponse response = (WebSocketHttpResponse)message;
      WebSocketServerHandshaker handshaker = response.handshakerFactory().newHandshaker(this.lastRequest);
      if (handshaker == null) {
         HttpResponse res = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UPGRADE_REQUIRED);
         res.headers().set(HttpHeaderNames.SEC_WEBSOCKET_VERSION, WebSocketVersion.V13.toHttpHeaderValue());
         HttpUtil.setContentLength(res, 0L);
         super.unbufferedWrite(ctx, message, promise);
         response.subscribe(new CancelledSubscriber<>());
      } else {
         ChannelPipeline pipeline = ctx.pipeline();
         HandlerPublisher<WebSocketFrame> publisher = new HandlerPublisher<>(ctx.executor(), WebSocketFrame.class);
         HandlerSubscriber<WebSocketFrame> subscriber = new HandlerSubscriber<>(ctx.executor());
         pipeline.addAfter(ctx.executor(), ctx.name(), "websocket-subscriber", subscriber);
         pipeline.addAfter(ctx.executor(), ctx.name(), "websocket-publisher", publisher);
         ctx.pipeline().remove(ctx.name());
         handshaker.handshake(ctx.channel(), (FullHttpRequest)(new EmptyHttpRequest(this.lastRequest)));
         response.subscribe(subscriber);
         publisher.subscribe(response);
      }

   }

   @Override
   protected void bodyRequested(ChannelHandlerContext ctx) {
      if (this.continueExpected) {
         if (this.inFlight == 1) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(this.lastRequest.protocolVersion(), HttpResponseStatus.CONTINUE));
            this.continueExpected = false;
         } else {
            this.sendContinue = true;
         }
      }

   }

   @Override
   protected final boolean isClient() {
      return false;
   }

   @Override
   public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
      super.handlerRemoved(ctx);

      for(ChannelHandler dependent : this.dependentHandlers) {
         try {
            ctx.pipeline().remove(dependent);
         } catch (NoSuchElementException var5) {
         }
      }

   }
}
