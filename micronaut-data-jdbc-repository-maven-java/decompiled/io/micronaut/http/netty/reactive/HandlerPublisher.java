package io.micronaut.http.netty.reactive;

import io.micronaut.core.annotation.Internal;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.internal.TypeParameterMatcher;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Internal
public class HandlerPublisher<T> extends ChannelDuplexHandler implements HotObservable<T> {
   private static final Logger LOG = LoggerFactory.getLogger(HandlerPublisher.class);
   private static final Object COMPLETE = new Object() {
      public String toString() {
         return "COMPLETE";
      }
   };
   private final AtomicBoolean completed = new AtomicBoolean(false);
   private final EventExecutor executor;
   private final TypeParameterMatcher matcher;
   private final Queue<Object> buffer = new LinkedList();
   private final AtomicBoolean hasSubscriber = new AtomicBoolean();
   private HandlerPublisher.State state = HandlerPublisher.State.NO_SUBSCRIBER_OR_CONTEXT;
   private volatile Subscriber<? super T> subscriber;
   private ChannelHandlerContext ctx;
   private volatile long outstandingDemand = 0L;
   private Throwable noSubscriberError;

   public HandlerPublisher(EventExecutor executor, Class<? extends T> subscriberMessageType) {
      this.executor = executor;
      this.matcher = TypeParameterMatcher.get(subscriberMessageType);
   }

   @Override
   public void subscribe(final Subscriber<? super T> subscriber) {
      if (subscriber == null) {
         throw new NullPointerException("Null subscriber");
      } else {
         if (!this.hasSubscriber.compareAndSet(false, true)) {
            subscriber.onSubscribe(new Subscription() {
               @Override
               public void request(long n) {
               }

               @Override
               public void cancel() {
               }
            });
            subscriber.onError(new IllegalStateException("This publisher only supports one subscriber"));
         } else {
            this.executor.execute(() -> this.provideSubscriber(subscriber));
         }

      }
   }

   protected boolean acceptInboundMessage(Object msg) {
      return this.matcher.match(msg);
   }

   protected void cancelled() {
      this.ctx.close();
   }

   protected void requestDemand() {
      if (LOG.isTraceEnabled()) {
         LOG.trace("Demand received for next message (state = " + this.state + "). Calling context.read()");
      }

      this.ctx.read();
   }

   private void provideSubscriber(Subscriber<? super T> subscriber) {
      this.subscriber = subscriber;
      switch(this.state) {
         case NO_SUBSCRIBER_OR_CONTEXT:
            this.state = HandlerPublisher.State.NO_CONTEXT;
            break;
         case NO_SUBSCRIBER:
            if (this.buffer.isEmpty()) {
               this.state = HandlerPublisher.State.IDLE;
            } else {
               this.state = HandlerPublisher.State.BUFFERING;
            }

            subscriber.onSubscribe(new HandlerPublisher.ChannelSubscription());
            break;
         case DRAINING:
            subscriber.onSubscribe(new HandlerPublisher.ChannelSubscription());
            break;
         case NO_SUBSCRIBER_ERROR:
            this.cleanup();
            this.state = HandlerPublisher.State.DONE;
            subscriber.onSubscribe(new HandlerPublisher.ChannelSubscription());
            subscriber.onError(this.noSubscriberError);
            break;
         case DONE:
            subscriber.onSubscribe(new HandlerPublisher.ChannelSubscription());
            subscriber.onComplete();
      }

   }

   @Override
   public void handlerAdded(ChannelHandlerContext ctx) {
      if (ctx.channel().isRegistered()) {
         this.provideChannelContext(ctx);
      }

   }

   @Override
   public void channelRegistered(ChannelHandlerContext ctx) {
      this.provideChannelContext(ctx);
      ctx.fireChannelRegistered();
   }

   private void provideChannelContext(ChannelHandlerContext ctx) {
      switch(this.state) {
         case NO_SUBSCRIBER_OR_CONTEXT:
            this.verifyRegisteredWithRightExecutor();
            this.ctx = ctx;
            this.state = HandlerPublisher.State.NO_SUBSCRIBER;
            break;
         case NO_CONTEXT:
            this.verifyRegisteredWithRightExecutor();
            this.ctx = ctx;
            this.state = HandlerPublisher.State.IDLE;
            this.subscriber.onSubscribe(new HandlerPublisher.ChannelSubscription());
      }

   }

   private void verifyRegisteredWithRightExecutor() {
      if (!this.executor.inEventLoop()) {
         throw new IllegalArgumentException("Channel handler MUST be registered with the same EventExecutor that it is created with.");
      }
   }

   @Override
   public void channelActive(ChannelHandlerContext ctx) {
      if (this.state == HandlerPublisher.State.DEMANDING) {
         this.requestDemand();
      }

      ctx.fireChannelActive();
   }

   private void receivedCancel() {
      if (LOG.isTraceEnabled()) {
         LOG.trace("HandlerPublisher (state: {}) received cancellation request", this.state);
      }

      switch(this.state) {
         case BUFFERING:
         case DEMANDING:
         case IDLE:
            this.cancelled();
         case DRAINING:
            this.state = HandlerPublisher.State.DONE;
         case NO_SUBSCRIBER_ERROR:
         case DONE:
         case NO_CONTEXT:
         default:
            this.cleanup();
            this.subscriber = null;
      }
   }

   @Override
   public void channelRead(ChannelHandlerContext ctx, Object message) {
      if (this.acceptInboundMessage(message)) {
         this.publishMessageLater(message);
      } else {
         ctx.fireChannelRead(message);
      }

   }

   private Object messageForTrace(Object message) {
      Object msg = message;
      if (message instanceof HttpContent) {
         HttpContent content = (HttpContent)message;
         msg = content.content().toString(StandardCharsets.UTF_8);
      }

      return msg;
   }

   private void publishMessageLater(Object message) {
      ReferenceCountUtil.touch(message);
      switch(this.state) {
         case NO_SUBSCRIBER_OR_CONTEXT:
         case NO_CONTEXT:
            throw new IllegalStateException("Message received before added to the channel context");
         case NO_SUBSCRIBER:
         case BUFFERING:
            if (LOG.isTraceEnabled()) {
               Object msg = this.messageForTrace(message);
               LOG.trace("HandlerPublisher (state: BUFFERING) buffering message: {}", msg);
            }

            this.buffer.add(message);
            break;
         case DRAINING:
         case DONE:
            if (LOG.isTraceEnabled()) {
               Object msg = this.messageForTrace(message);
               LOG.trace("HandlerPublisher (state: DONE) releasing message: {}", msg);
            }

            ReferenceCountUtil.release(message);
         case NO_SUBSCRIBER_ERROR:
         default:
            break;
         case DEMANDING:
            this.state = HandlerPublisher.State.BUFFERING;
            this.buffer.add(message);
            this.flushBuffer();
            break;
         case IDLE:
            if (LOG.isTraceEnabled()) {
               Object msg = this.messageForTrace(message);
               LOG.trace("HandlerPublisher (state: IDLE) buffering message: {}", msg);
            }

            this.buffer.add(message);
            this.state = HandlerPublisher.State.BUFFERING;
      }

   }

   private void publishMessage(Object message) {
      if (COMPLETE.equals(message)) {
         if (LOG.isTraceEnabled()) {
            LOG.trace("HandlerPublisher (state: {}) complete. Calling onComplete()", this.state);
         }

         this.subscriber.onComplete();
         this.state = HandlerPublisher.State.DONE;
      } else {
         if (LOG.isTraceEnabled()) {
            LOG.trace("HandlerPublisher (state: {}) emitting next message: {}", this.state, this.messageForTrace(message));
         }

         ReferenceCountUtil.touch(message, this.subscriber);
         this.subscriber.onNext((T)message);
         if (this.outstandingDemand < Long.MAX_VALUE) {
            --this.outstandingDemand;
            if (this.outstandingDemand == 0L && this.state != HandlerPublisher.State.DRAINING) {
               if (this.buffer.isEmpty()) {
                  this.state = HandlerPublisher.State.IDLE;
               } else {
                  this.state = HandlerPublisher.State.BUFFERING;
               }
            } else if (this.outstandingDemand > 0L
               && (
                  this.state == HandlerPublisher.State.DEMANDING
                     || this.state == HandlerPublisher.State.BUFFERING
                     || this.state == HandlerPublisher.State.DRAINING
               )) {
               this.requestDemand();
            }
         } else {
            this.requestDemand();
         }
      }

   }

   @Override
   public void channelInactive(ChannelHandlerContext ctx) {
      this.complete();
   }

   @Override
   public void handlerRemoved(ChannelHandlerContext ctx) {
      this.complete();
   }

   private void complete() {
      if (this.completed.compareAndSet(false, true)) {
         this.publishMessageLater(COMPLETE);
      }

   }

   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      switch(this.state) {
         case NO_SUBSCRIBER:
            this.noSubscriberError = cause;
            this.state = HandlerPublisher.State.NO_SUBSCRIBER_ERROR;
            this.cleanup();
            break;
         case DRAINING:
         case BUFFERING:
         case DEMANDING:
         case IDLE:
            this.state = HandlerPublisher.State.DONE;
            this.cleanup();
            this.subscriber.onError(cause);
         case NO_SUBSCRIBER_ERROR:
         case DONE:
         case NO_CONTEXT:
      }

   }

   @Override
   public void closeIfNoSubscriber() {
      if (this.subscriber == null) {
         this.state = HandlerPublisher.State.DONE;
         this.cleanup();
      }

   }

   private void cleanup() {
      while(!this.buffer.isEmpty()) {
         ReferenceCountUtil.release(this.buffer.remove());
      }

   }

   private void flushBuffer() {
      for(; !this.buffer.isEmpty() && this.outstandingDemand > 0L; this.publishMessage(this.buffer.remove())) {
         if (LOG.isTraceEnabled()) {
            LOG.trace("HandlerPublisher (state: {}) release message from buffer to satisfy demand: {}", this.state, this.outstandingDemand);
         }
      }

      if (this.buffer.isEmpty()) {
         if (this.outstandingDemand > 0L) {
            if (this.state == HandlerPublisher.State.BUFFERING) {
               this.state = HandlerPublisher.State.DEMANDING;
            }

            if (!this.completed.get()) {
               this.requestDemand();
            }
         } else if (this.state == HandlerPublisher.State.BUFFERING) {
            this.state = HandlerPublisher.State.IDLE;
         }
      }

   }

   private class ChannelSubscription implements Subscription {
      private volatile boolean cancelled = false;

      private ChannelSubscription() {
      }

      @Override
      public void request(final long demand) {
         HandlerPublisher.this.executor.execute(() -> this.receivedDemand(demand));
      }

      @Override
      public void cancel() {
         HandlerPublisher.this.executor.execute(() -> HandlerPublisher.this.receivedCancel());
         this.cancelled = true;
         HandlerPublisher.this.outstandingDemand = 0L;
      }

      private void illegalDemand() {
         HandlerPublisher.this.cleanup();
         HandlerPublisher.this.subscriber
            .onError(new IllegalArgumentException("Request for 0 or negative elements in violation of Section 3.9 of the Reactive Streams specification"));
         HandlerPublisher.this.ctx.close();
         HandlerPublisher.this.state = HandlerPublisher.State.DONE;
      }

      private boolean addDemand(long demand) {
         if (demand <= 0L) {
            this.illegalDemand();
            return false;
         } else {
            if (HandlerPublisher.this.outstandingDemand < Long.MAX_VALUE) {
               HandlerPublisher.this.outstandingDemand = HandlerPublisher.this.outstandingDemand + demand;
               if (HandlerPublisher.this.outstandingDemand < 0L) {
                  HandlerPublisher.this.outstandingDemand = Long.MAX_VALUE;
               }
            }

            return true;
         }
      }

      private void receivedDemand(long demand) {
         if (!this.cancelled) {
            switch(HandlerPublisher.this.state) {
               case DRAINING:
               case BUFFERING:
               case DEMANDING:
                  if (HandlerPublisher.LOG.isTraceEnabled()) {
                     HandlerPublisher.LOG.trace("HandlerPublisher (state: {}) received demand: {}", HandlerPublisher.this.state, demand);
                  }

                  if (this.addDemand(demand)) {
                     HandlerPublisher.this.flushBuffer();
                  }
               case NO_SUBSCRIBER_ERROR:
               case DONE:
               case NO_CONTEXT:
               default:
                  break;
               case IDLE:
                  if (HandlerPublisher.LOG.isTraceEnabled()) {
                     HandlerPublisher.LOG.trace("HandlerPublisher (state: {}) received demand: {}", HandlerPublisher.this.state, demand);
                  }

                  if (this.addDemand(demand)) {
                     HandlerPublisher.this.state = HandlerPublisher.State.DEMANDING;
                     HandlerPublisher.this.requestDemand();
                  }
            }

         }
      }
   }

   static enum State {
      NO_SUBSCRIBER_OR_CONTEXT,
      NO_CONTEXT,
      NO_SUBSCRIBER,
      NO_SUBSCRIBER_ERROR,
      IDLE,
      BUFFERING,
      DEMANDING,
      DRAINING,
      DONE;
   }
}
