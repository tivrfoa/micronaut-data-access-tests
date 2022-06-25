package io.micronaut.core.async.processor;

import io.micronaut.core.async.subscriber.SingleThreadedBufferingSubscriber;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public abstract class SingleThreadedBufferingProcessor<R, T> extends SingleThreadedBufferingSubscriber<R> implements Processor<R, T> {
   private final AtomicReference<Subscriber<? super T>> downstreamSubscriber = new AtomicReference();

   @Override
   public void subscribe(Subscriber<? super T> downstreamSubscriber) {
      this.subscribeDownstream(downstreamSubscriber);
   }

   @Override
   protected void doOnComplete() {
      try {
         this.currentDownstreamSubscriber().ifPresent(Subscriber::onComplete);
      } catch (Exception var2) {
         this.onError(var2);
      }

   }

   @Override
   protected void doOnNext(R message) {
      this.onUpstreamMessage(message);
   }

   @Override
   protected void doOnSubscribe(Subscription subscription) {
      this.currentDownstreamSubscriber().ifPresent(this::provideDownstreamSubscription);
   }

   @Override
   protected void doOnError(Throwable t) {
      this.currentDownstreamSubscriber().ifPresent(subscriber -> subscriber.onError(t));
   }

   protected void subscribeDownstream(Subscriber<? super T> downstreamSubscriber) {
      if (!this.downstreamSubscriber.compareAndSet(null, downstreamSubscriber)) {
         throw new IllegalStateException("Only one subscriber allowed");
      } else {
         switch(this.upstreamState) {
            case NO_SUBSCRIBER:
               if (this.upstreamBuffer.isEmpty()) {
                  this.upstreamState = SingleThreadedBufferingSubscriber.BackPressureState.IDLE;
               } else {
                  this.upstreamState = SingleThreadedBufferingSubscriber.BackPressureState.BUFFERING;
               }
               break;
            case IDLE:
            case BUFFERING:
            case FLOWING:
               this.provideDownstreamSubscription(downstreamSubscriber);
         }

      }
   }

   protected abstract void onUpstreamMessage(R message);

   protected Optional<Subscriber<? super T>> currentDownstreamSubscriber() {
      return Optional.ofNullable(this.downstreamSubscriber.get());
   }

   protected Subscriber<? super T> getDownstreamSubscriber() {
      return (Subscriber<? super T>)Optional.ofNullable(this.downstreamSubscriber.get()).orElseThrow(() -> new IllegalStateException("No subscriber present!"));
   }
}
