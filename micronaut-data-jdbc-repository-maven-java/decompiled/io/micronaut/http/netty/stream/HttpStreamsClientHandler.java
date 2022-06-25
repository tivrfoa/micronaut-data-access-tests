package io.micronaut.http.netty.stream;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.netty.reactive.CancelledSubscriber;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.publisher.Flux;

@Internal
public class HttpStreamsClientHandler extends HttpStreamsHandler<HttpResponse, HttpRequest> {
   private int inFlight = 0;
   private int withServer = 0;
   private ChannelPromise closeOnZeroInFlight = null;
   private Subscriber<HttpContent> awaiting100Continue;
   private StreamedHttpMessage awaiting100ContinueMessage;
   private boolean ignoreResponseBody = false;

   public HttpStreamsClientHandler() {
      super(HttpResponse.class, HttpRequest.class);
   }

   protected boolean hasBody(HttpResponse response) {
      if (response.status().code() >= HttpStatus.CONTINUE.getCode() && response.status().code() < HttpStatus.OK.getCode()) {
         return false;
      } else if (response.status().equals(HttpResponseStatus.NO_CONTENT) || response.status().equals(HttpResponseStatus.NOT_MODIFIED)) {
         return false;
      } else if (HttpUtil.isTransferEncodingChunked(response)) {
         return true;
      } else if (HttpUtil.isContentLengthSet(response)) {
         return HttpUtil.getContentLength(response) > 0L;
      } else {
         return true;
      }
   }

   @Override
   public void close(ChannelHandlerContext ctx, ChannelPromise future) throws Exception {
      if (this.inFlight == 0) {
         ctx.close(future);
      } else {
         this.closeOnZeroInFlight = future;
      }

   }

   @Override
   protected void consumedInMessage(ChannelHandlerContext ctx) {
      --this.inFlight;
      --this.withServer;
      if (this.inFlight == 0 && this.closeOnZeroInFlight != null) {
         ctx.close(this.closeOnZeroInFlight);
      }

   }

   @Override
   protected void receivedOutMessage(ChannelHandlerContext ctx) {
      ++this.inFlight;
   }

   @Override
   protected void sentOutMessage(ChannelHandlerContext ctx) {
      ++this.withServer;
   }

   protected HttpResponse createEmptyMessage(HttpResponse response) {
      return new EmptyHttpResponse(response);
   }

   protected HttpResponse createStreamedMessage(HttpResponse response, Publisher<? extends HttpContent> stream) {
      return new DelegateStreamedHttpResponse(response, stream);
   }

   @Override
   protected void subscribeSubscriberToStream(StreamedHttpMessage msg, Subscriber<HttpContent> subscriber) {
      if (HttpUtil.is100ContinueExpected(msg)) {
         this.awaiting100Continue = subscriber;
         this.awaiting100ContinueMessage = msg;
      } else {
         super.subscribeSubscriberToStream(msg, subscriber);
      }

   }

   @Override
   protected final boolean isClient() {
      return true;
   }

   @Override
   public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      if (msg instanceof HttpResponse && this.awaiting100Continue != null && this.withServer == 0) {
         HttpResponse response = (HttpResponse)msg;
         if (response.status().equals(HttpResponseStatus.CONTINUE)) {
            super.subscribeSubscriberToStream(this.awaiting100ContinueMessage, this.awaiting100Continue);
            this.awaiting100Continue = null;
            this.awaiting100ContinueMessage = null;
            if (msg instanceof FullHttpResponse) {
               ReferenceCountUtil.release(msg);
            } else {
               this.ignoreResponseBody = true;
            }
         } else {
            this.awaiting100ContinueMessage.subscribe(new CancelledSubscriber<>());
            this.awaiting100ContinueMessage = null;
            Flux.<HttpContent>empty().subscribe(this.awaiting100Continue);
            this.awaiting100Continue = null;
            super.channelRead(ctx, msg);
         }
      } else if (this.ignoreResponseBody && msg instanceof HttpContent) {
         ReferenceCountUtil.release(msg);
         if (msg instanceof LastHttpContent) {
            this.ignoreResponseBody = false;
         }
      } else {
         super.channelRead(ctx, msg);
      }

   }

   @Override
   public void write(final ChannelHandlerContext ctx, Object msg, final ChannelPromise promise) throws Exception {
      if (ctx.channel().attr(AttributeKey.valueOf("chunk-writer")).get() == Boolean.TRUE) {
         ctx.write(msg, promise);
      } else {
         super.write(ctx, msg, promise);
      }

   }
}
