package io.micronaut.http.server.netty.async;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.subscriber.CompletionAwareSubscriber;
import io.micronaut.http.netty.reactive.HandlerPublisher;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import org.reactivestreams.Subscription;

@Internal
public abstract class ContextCompletionAwareSubscriber<T> extends CompletionAwareSubscriber<T> {
   private final ChannelHandlerContext context;
   private Subscription s;
   private Object message;

   protected ContextCompletionAwareSubscriber(ChannelHandlerContext context) {
      this.context = context;
   }

   @Override
   protected void doOnSubscribe(Subscription subscription) {
      this.s = subscription;
      this.s.request(1L);
   }

   @Override
   protected void doOnNext(T message) {
      this.message = message;
   }

   @Override
   protected void doOnError(Throwable t) {
      this.s.cancel();
      ChannelPipeline pipeline = this.context.pipeline();
      HandlerPublisher handlerPublisher = pipeline.get(HandlerPublisher.class);
      if (handlerPublisher != null) {
         pipeline.remove(handlerPublisher);
      }

      pipeline.fireExceptionCaught(t);
   }

   @Override
   protected void doOnComplete() {
      this.onComplete((T)this.message);
   }

   protected abstract void onComplete(T message);
}
