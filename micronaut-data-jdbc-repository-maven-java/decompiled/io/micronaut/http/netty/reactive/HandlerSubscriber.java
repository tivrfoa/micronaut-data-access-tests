package io.micronaut.http.netty.reactive;

import io.micronaut.core.annotation.Internal;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.EventExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Internal
public class HandlerSubscriber<T> extends ChannelDuplexHandler implements Subscriber<T> {
   protected ChannelFuture lastWriteFuture;
   private final EventExecutor executor;
   private final AtomicBoolean hasSubscription = new AtomicBoolean();
   private volatile Subscription subscription;
   private volatile ChannelHandlerContext ctx;
   private HandlerSubscriber.State state = HandlerSubscriber.State.NO_SUBSCRIPTION_OR_CONTEXT;

   public HandlerSubscriber(EventExecutor executor) {
      this.executor = executor;
   }

   protected void error(Throwable error) {
      this.doClose();
   }

   protected void complete() {
      this.doClose();
   }

   @Override
   public void handlerAdded(ChannelHandlerContext ctx) {
      this.verifyRegisteredWithRightExecutor(ctx);
      switch(this.state) {
         case NO_SUBSCRIPTION_OR_CONTEXT:
            this.ctx = ctx;
            this.state = HandlerSubscriber.State.NO_SUBSCRIPTION;
            break;
         case NO_CONTEXT:
            this.ctx = ctx;
            this.maybeStart();
            break;
         case COMPLETE:
            this.state = HandlerSubscriber.State.COMPLETE;
            ctx.close();
            break;
         default:
            throw new IllegalStateException("This handler must only be added to a pipeline once " + this.state);
      }

   }

   @Override
   public void channelRegistered(ChannelHandlerContext ctx) {
      this.verifyRegisteredWithRightExecutor(ctx);
      ctx.fireChannelRegistered();
   }

   @Override
   public void channelWritabilityChanged(ChannelHandlerContext ctx) {
      this.maybeRequestMore();
      ctx.fireChannelWritabilityChanged();
   }

   @Override
   public void channelActive(ChannelHandlerContext ctx) {
      if (this.state == HandlerSubscriber.State.INACTIVE) {
         this.state = HandlerSubscriber.State.RUNNING;
         this.maybeRequestMore();
      }

      ctx.fireChannelActive();
   }

   @Override
   public void channelInactive(ChannelHandlerContext ctx) {
      this.cancel();
      ctx.fireChannelInactive();
   }

   @Override
   public void handlerRemoved(ChannelHandlerContext ctx) {
      this.cancel();
   }

   @Override
   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
      this.cancel();
      ctx.fireExceptionCaught(cause);
   }

   @Override
   public void onSubscribe(final Subscription subscription) {
      if (subscription == null) {
         throw new NullPointerException("Null subscription");
      } else {
         if (!this.hasSubscription.compareAndSet(false, true)) {
            subscription.cancel();
         } else {
            this.subscription = subscription;
            this.executor.execute(this::provideSubscription);
         }

      }
   }

   @Override
   public void onNext(T t) {
      this.onNext(t, this.ctx.newPromise());
   }

   protected void onNext(T t, ChannelPromise promise) {
      this.lastWriteFuture = this.ctx.writeAndFlush(t, promise);
      this.lastWriteFuture.addListener(future -> this.maybeRequestMore());
   }

   @Override
   public void onError(final Throwable error) {
      if (error == null) {
         throw new NullPointerException("Null error published");
      } else {
         this.error(error);
      }
   }

   @Override
   public void onComplete() {
      if (this.lastWriteFuture == null) {
         this.complete();
      } else {
         this.lastWriteFuture.addListener(channelFuture -> this.complete());
      }

   }

   private void doClose() {
      this.executor.execute(() -> {
         switch(this.state) {
            case NO_SUBSCRIPTION:
            case RUNNING:
            case INACTIVE:
               this.ctx.close();
               this.state = HandlerSubscriber.State.COMPLETE;
         }
      });
   }

   private void maybeRequestMore() {
      if (this.ctx.channel().isWritable() && this.state != HandlerSubscriber.State.COMPLETE && this.state != HandlerSubscriber.State.CANCELLED) {
         this.subscription.request(1L);
      }

   }

   private void verifyRegisteredWithRightExecutor(ChannelHandlerContext ctx) {
      if (ctx.channel().isRegistered() && !this.executor.inEventLoop()) {
         throw new IllegalArgumentException("Channel handler MUST be registered with the same EventExecutor that it is created with.");
      }
   }

   private void cancel() {
      switch(this.state) {
         case NO_SUBSCRIPTION:
            this.state = HandlerSubscriber.State.CANCELLED;
            break;
         case RUNNING:
         case INACTIVE:
            this.subscription.cancel();
            this.state = HandlerSubscriber.State.CANCELLED;
      }

   }

   private void provideSubscription() {
      switch(this.state) {
         case NO_SUBSCRIPTION_OR_CONTEXT:
            this.state = HandlerSubscriber.State.NO_CONTEXT;
            break;
         case NO_SUBSCRIPTION:
            this.maybeStart();
            break;
         case CANCELLED:
            this.subscription.cancel();
      }

   }

   private void maybeStart() {
      if (this.ctx.channel().isActive()) {
         this.state = HandlerSubscriber.State.RUNNING;
         this.maybeRequestMore();
      } else {
         this.state = HandlerSubscriber.State.INACTIVE;
      }

   }

   static enum State {
      NO_SUBSCRIPTION_OR_CONTEXT,
      NO_SUBSCRIPTION,
      NO_CONTEXT,
      INACTIVE,
      RUNNING,
      CANCELLED,
      COMPLETE;
   }
}
