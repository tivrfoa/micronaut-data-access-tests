package io.micronaut.http.netty.stream;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.netty.AbstractNettyHttpRequest;
import io.micronaut.http.netty.reactive.HandlerPublisher;
import io.micronaut.http.netty.reactive.HandlerSubscriber;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

@Internal
abstract class HttpStreamsHandler<In extends HttpMessage, Out extends HttpMessage> extends ChannelDuplexHandler {
   public static final String HANDLER_BODY_PUBLISHER = "http-streams-codec-body-publisher";
   private static final Logger LOG = LoggerFactory.getLogger(HttpStreamsHandler.class);
   private final Queue<HttpStreamsHandler.Outgoing<Out>> outgoing = new LinkedList();
   private final Class<In> inClass;
   private final Class<Out> outClass;
   private In currentlyStreamedMessage;
   private boolean ignoreBodyRead;
   private boolean sendLastHttpContent;
   private boolean outgoingInFlight;

   HttpStreamsHandler(Class<In> inClass, Class<Out> outClass) {
      this.inClass = inClass;
      this.outClass = outClass;
   }

   protected abstract boolean hasBody(In in);

   protected abstract In createEmptyMessage(In in);

   protected abstract In createStreamedMessage(In in, Publisher<? extends HttpContent> stream);

   protected void receivedInMessage(ChannelHandlerContext ctx) {
   }

   protected void consumedInMessage(ChannelHandlerContext ctx) {
   }

   protected void receivedOutMessage(ChannelHandlerContext ctx) {
   }

   protected void sentOutMessage(ChannelHandlerContext ctx) {
   }

   protected void subscribeSubscriberToStream(StreamedHttpMessage msg, Subscriber<HttpContent> subscriber) {
      msg.subscribe(subscriber);
   }

   protected void bodyRequested(ChannelHandlerContext ctx) {
   }

   protected abstract boolean isClient();

   @Override
   public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
      if (this.isValidInMessage(msg)) {
         this.receivedInMessage(ctx);
         final In inMsg = (In)this.inClass.cast(msg);
         if (inMsg instanceof FullHttpMessage) {
            FullHttpMessage fullMessage = (FullHttpMessage)inMsg;
            if (fullMessage instanceof FullHttpRequest && fullMessage.content().readableBytes() != 0) {
               ctx.fireChannelRead(this.createStreamedMessage(inMsg, Flux.just(fullMessage)));
            } else {
               ctx.fireChannelRead(inMsg);
            }

            this.consumedInMessage(ctx);
         } else if (!this.hasBody(inMsg)) {
            ctx.fireChannelRead(this.createEmptyMessage(inMsg));
            this.consumedInMessage(ctx);
            this.ignoreBodyRead = true;
         } else {
            this.currentlyStreamedMessage = inMsg;
            final int streamId = this.getStreamId(msg);
            HandlerPublisher<? extends HttpContent> publisher;
            if (streamId > -1) {
               publisher = new HandlerPublisher<Http2Content>(ctx.executor(), Http2Content.class) {
                  @Override
                  protected boolean acceptInboundMessage(Object msg) {
                     return super.acceptInboundMessage(msg) && ((Http2Content)msg).stream().id() == streamId;
                  }

                  @Override
                  protected void cancelled() {
                     if (ctx.executor().inEventLoop()) {
                        HttpStreamsHandler.this.handleCancelled(ctx, inMsg);
                     } else {
                        ctx.executor().execute(() -> HttpStreamsHandler.this.handleCancelled(ctx, inMsg));
                     }

                  }

                  @Override
                  protected void requestDemand() {
                     HttpStreamsHandler.this.bodyRequested(ctx);
                     super.requestDemand();
                  }
               };
            } else {
               publisher = new HandlerPublisher<HttpContent>(ctx.executor(), HttpContent.class) {
                  @Override
                  protected void cancelled() {
                     if (ctx.executor().inEventLoop()) {
                        HttpStreamsHandler.this.handleCancelled(ctx, inMsg);
                     } else {
                        ctx.executor().execute(() -> HttpStreamsHandler.this.handleCancelled(ctx, inMsg));
                     }

                  }

                  @Override
                  protected void requestDemand() {
                     HttpStreamsHandler.this.bodyRequested(ctx);
                     super.requestDemand();
                  }
               };
            }

            ctx.channel().pipeline().addAfter(ctx.name(), "http-streams-codec-body-publisher", publisher);
            ctx.fireChannelRead(this.createStreamedMessage(inMsg, publisher));
         }
      } else if (msg instanceof HttpContent) {
         this.handleReadHttpContent(ctx, (HttpContent)msg);
      }

   }

   protected int getStreamId(Object msg) {
      return msg instanceof HttpMessage ? ((HttpMessage)msg).headers().getInt(AbstractNettyHttpRequest.STREAM_ID, -1) : -1;
   }

   private void handleCancelled(ChannelHandlerContext ctx, In msg) {
      if (this.currentlyStreamedMessage == msg) {
         this.ignoreBodyRead = true;
         if (LOG.isTraceEnabled()) {
            LOG.trace("Calling ctx.read() for cancelled subscription");
         }

         ctx.read();
         if (this.isClient()) {
            ctx.fireChannelWritabilityChanged();
         }
      }

   }

   private void handleReadHttpContent(ChannelHandlerContext ctx, HttpContent content) {
      if (!this.ignoreBodyRead) {
         ChannelHandler bodyPublisher = ctx.pipeline().get("http-streams-codec-body-publisher");
         if (bodyPublisher != null) {
            ctx.fireChannelRead(content);
            if (content instanceof LastHttpContent) {
               this.currentlyStreamedMessage = null;
               this.removeHandlerIfActive(ctx, "http-streams-codec-body-publisher");
               this.consumedInMessage(ctx);
            }
         } else {
            ReferenceCountUtil.release(content, content.refCnt());
         }
      } else {
         ReferenceCountUtil.release(content, content.refCnt());
         if (content instanceof LastHttpContent) {
            this.ignoreBodyRead = false;
            if (this.currentlyStreamedMessage != null) {
               this.removeHandlerIfActive(ctx, "http-streams-codec-body-publisher");
            }

            this.currentlyStreamedMessage = null;
         }

         ctx.read();
      }

   }

   @Override
   public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
      if (this.ignoreBodyRead) {
         ctx.read();
      } else {
         ctx.fireChannelReadComplete();
      }

   }

   @Override
   public void write(final ChannelHandlerContext ctx, Object msg, final ChannelPromise promise) throws Exception {
      if (this.isValidOutMessage(msg)) {
         this.receivedOutMessage(ctx);
         this.outgoing.add(new HttpStreamsHandler.Outgoing((HttpMessage)msg, promise));
         this.proceedWriteOutgoing(ctx);
      } else if (msg instanceof LastHttpContent) {
         this.sendLastHttpContent = false;
         ctx.write(msg, promise);
      } else {
         ctx.write(msg, promise);
      }

   }

   @Override
   public void channelWritabilityChanged(ChannelHandlerContext ctx) {
      this.proceedWriteOutgoing(ctx);
   }

   private void proceedWriteOutgoing(ChannelHandlerContext ctx) {
      while(!this.outgoingInFlight && ctx.channel().isWritable() && !this.outgoing.isEmpty()) {
         HttpStreamsHandler.Outgoing<Out> out = (HttpStreamsHandler.Outgoing)this.outgoing.remove();
         this.unbufferedWrite(ctx, out.message, out.promise);
      }

   }

   protected void unbufferedWrite(final ChannelHandlerContext ctx, final Out message, ChannelPromise promise) {
      if (message instanceof FullHttpMessage) {
         ctx.writeAndFlush(message, promise);
         this.sentOutMessage(ctx);
      } else if (message instanceof StreamedHttpMessage) {
         this.outgoingInFlight = true;
         StreamedHttpMessage streamed = (StreamedHttpMessage)message;
         HandlerSubscriber<HttpContent> subscriber = new HandlerSubscriber<HttpContent>(ctx.executor()) {
            AtomicBoolean messageWritten = new AtomicBoolean();

            public void onNext(HttpContent httpContent) {
               if (this.messageWritten.compareAndSet(false, true)) {
                  ChannelPromise messageWritePromise = ctx.newPromise();
                  this.lastWriteFuture = messageWritePromise;
                  ctx.writeAndFlush(message).addListener(f -> super.onNext(httpContent, messageWritePromise));
               } else {
                  super.onNext(httpContent);
               }

            }

            @Override
            protected void error(Throwable error) {
               try {
                  if (HttpStreamsHandler.LOG.isErrorEnabled()) {
                     HttpStreamsHandler.LOG.error("Error occurred writing stream response: " + error.getMessage(), error);
                  }

                  HttpResponseStatus responseStatus;
                  if (error instanceof HttpStatusException) {
                     responseStatus = HttpResponseStatus.valueOf(((HttpStatusException)error).getStatus().getCode(), error.getMessage());
                  } else {
                     responseStatus = HttpResponseStatus.INTERNAL_SERVER_ERROR;
                  }

                  ctx.writeAndFlush(new DefaultHttpResponse(HttpVersion.HTTP_1_1, responseStatus)).addListener(ChannelFutureListener.CLOSE);
               } finally {
                  ctx.read();
               }

            }

            @Override
            protected void complete() {
               if (this.messageWritten.compareAndSet(false, true)) {
                  ctx.writeAndFlush(message).addListener(future -> this.doOnComplete());
               } else {
                  this.doOnComplete();
               }

            }

            private void doOnComplete() {
               if (ctx.executor().inEventLoop()) {
                  HttpStreamsHandler.this.completeBody(ctx, promise);
               } else {
                  ctx.executor().execute(() -> HttpStreamsHandler.this.completeBody(ctx, promise));
               }

            }
         };
         this.sendLastHttpContent = true;
         ctx.pipeline().addAfter(ctx.name(), ctx.name() + "-body-subscriber", subscriber);
         this.subscribeSubscriberToStream(streamed, subscriber);
      }

   }

   private void completeBody(final ChannelHandlerContext ctx, ChannelPromise promise) {
      this.removeHandlerIfActive(ctx, ctx.name() + "-body-subscriber");
      if (this.sendLastHttpContent) {
         ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT, promise).addListener(f -> {
            this.sentOutMessage(ctx);
            ctx.read();
            this.outgoingInFlight = false;
            this.proceedWriteOutgoing(ctx);
         });
      } else {
         promise.setSuccess();
         this.sentOutMessage(ctx);
         ctx.read();
         this.outgoingInFlight = false;
         this.proceedWriteOutgoing(ctx);
      }

   }

   private void removeHandlerIfActive(ChannelHandlerContext ctx, String name) {
      if (ctx.channel().isActive()) {
         ChannelPipeline pipeline = ctx.pipeline();
         ChannelHandler handler = pipeline.get(name);
         if (handler != null) {
            pipeline.remove(name);
         }
      }

   }

   protected boolean isValidOutMessage(Object msg) {
      return this.outClass.isInstance(msg);
   }

   protected boolean isValidInMessage(Object msg) {
      return this.inClass.isInstance(msg);
   }

   static class Outgoing<O extends HttpMessage> {
      final O message;
      final ChannelPromise promise;

      Outgoing(O message, ChannelPromise promise) {
         this.message = message;
         this.promise = promise;
      }
   }
}
