package io.micronaut.core.async.subscriber;

import java.util.LinkedList;
import java.util.Queue;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public abstract class SingleThreadedBufferingSubscriber<T> implements Subscriber<T>, Emitter<T> {
   protected final Queue<T> upstreamBuffer = new LinkedList();
   protected SingleThreadedBufferingSubscriber.BackPressureState upstreamState = SingleThreadedBufferingSubscriber.BackPressureState.NO_SUBSCRIBER;
   protected long upstreamDemand = 0L;
   protected Subscription upstreamSubscription;

   @Override
   public final synchronized void onSubscribe(Subscription subscription) {
      this.upstreamSubscription = subscription;
      switch(this.upstreamState) {
         case NO_SUBSCRIBER:
            if (this.upstreamBuffer.isEmpty()) {
               this.upstreamState = SingleThreadedBufferingSubscriber.BackPressureState.IDLE;
            } else {
               this.upstreamState = SingleThreadedBufferingSubscriber.BackPressureState.BUFFERING;
            }
            break;
         case FLOWING:
         case IDLE:
            this.doOnSubscribe(subscription);
      }

   }

   @Override
   public final void onComplete() {
      switch(this.upstreamState) {
         case NO_SUBSCRIBER:
         case BUFFERING:
            this.upstreamState = SingleThreadedBufferingSubscriber.BackPressureState.FLOWING;
            break;
         case FLOWING:
         case IDLE:
         default:
            this.doOnComplete();
            this.upstreamState = SingleThreadedBufferingSubscriber.BackPressureState.DONE;
            break;
         case DONE:
            return;
      }

   }

   @Override
   public final void onNext(T message) {
      switch(this.upstreamState) {
         case NO_SUBSCRIBER:
         case BUFFERING:
            this.upstreamBuffer.add(message);
         case FLOWING:
         case DONE:
         default:
            break;
         case IDLE:
            this.upstreamBuffer.add(message);
            this.upstreamState = SingleThreadedBufferingSubscriber.BackPressureState.BUFFERING;
            break;
         case DEMANDING:
            try {
               this.forwardMessageDownstream(message);
            } finally {
               if (this.upstreamDemand == 0L
                  && this.upstreamState != SingleThreadedBufferingSubscriber.BackPressureState.FLOWING
                  && this.upstreamState != SingleThreadedBufferingSubscriber.BackPressureState.DONE) {
                  if (this.upstreamBuffer.isEmpty()) {
                     this.upstreamState = SingleThreadedBufferingSubscriber.BackPressureState.IDLE;
                  } else {
                     this.upstreamState = SingleThreadedBufferingSubscriber.BackPressureState.BUFFERING;
                  }
               }

            }
      }

   }

   private void forwardMessageDownstream(T message) {
      try {
         this.doOnNext(message);
      } catch (Exception var6) {
         this.onError(var6);
      } finally {
         if (this.upstreamState != SingleThreadedBufferingSubscriber.BackPressureState.DONE && this.upstreamDemand < Long.MAX_VALUE) {
            --this.upstreamDemand;
         }

      }

   }

   @Override
   public final void onError(Throwable t) {
      if (this.upstreamState != SingleThreadedBufferingSubscriber.BackPressureState.DONE) {
         try {
            if (this.upstreamSubscription != null) {
               this.upstreamSubscription.cancel();
            }
         } finally {
            this.upstreamState = SingleThreadedBufferingSubscriber.BackPressureState.DONE;
            this.upstreamBuffer.clear();
            this.doOnError(t);
         }
      }

   }

   protected abstract void doOnSubscribe(Subscription subscription);

   protected abstract void doOnNext(T message);

   protected abstract void doOnError(Throwable t);

   protected abstract void doOnComplete();

   protected void provideDownstreamSubscription(Subscriber subscriber) {
      subscriber.onSubscribe(this.newDownstreamSubscription());
   }

   protected Subscription newDownstreamSubscription() {
      return new SingleThreadedBufferingSubscriber.DownstreamSubscription();
   }

   private boolean registerDemand(long demand) {
      if (demand <= 0L) {
         this.illegalDemand();
         return false;
      } else {
         if (this.upstreamDemand < Long.MAX_VALUE) {
            this.upstreamDemand += demand;
            if (this.upstreamDemand < 0L) {
               this.upstreamDemand = Long.MAX_VALUE;
            }
         }

         return true;
      }
   }

   private void flushBuffer() {
      while(!this.upstreamBuffer.isEmpty() && (this.upstreamDemand > 0L || this.upstreamDemand == Long.MAX_VALUE)) {
         this.forwardMessageDownstream((T)this.upstreamBuffer.remove());
      }

      if (this.upstreamBuffer.isEmpty()) {
         if (this.upstreamState == SingleThreadedBufferingSubscriber.BackPressureState.FLOWING) {
            this.doOnComplete();
            this.upstreamState = SingleThreadedBufferingSubscriber.BackPressureState.DONE;
         } else if (this.upstreamDemand > 0L) {
            if (this.upstreamState == SingleThreadedBufferingSubscriber.BackPressureState.BUFFERING) {
               this.upstreamState = SingleThreadedBufferingSubscriber.BackPressureState.DEMANDING;
            }

            this.upstreamSubscription.request(this.upstreamDemand);
         } else if (this.upstreamState == SingleThreadedBufferingSubscriber.BackPressureState.BUFFERING) {
            this.upstreamState = SingleThreadedBufferingSubscriber.BackPressureState.IDLE;
         }
      }

   }

   private void illegalDemand() {
      this.onError(new IllegalArgumentException("Request for 0 or negative elements in violation of Section 3.9 of the Reactive Streams specification"));
   }

   protected static enum BackPressureState {
      NO_SUBSCRIBER,
      IDLE,
      BUFFERING,
      DEMANDING,
      FLOWING,
      DONE;
   }

   protected class DownstreamSubscription implements Subscription {
      @Override
      public synchronized void request(long n) {
         this.processDemand(n);
         SingleThreadedBufferingSubscriber.this.upstreamSubscription.request(n);
      }

      @Override
      public synchronized void cancel() {
         SingleThreadedBufferingSubscriber.this.upstreamSubscription.cancel();
      }

      private void processDemand(long demand) {
         switch(SingleThreadedBufferingSubscriber.this.upstreamState) {
            case FLOWING:
            case BUFFERING:
               if (SingleThreadedBufferingSubscriber.this.registerDemand(demand)) {
                  SingleThreadedBufferingSubscriber.this.flushBuffer();
               }
               break;
            case IDLE:
               if (SingleThreadedBufferingSubscriber.this.registerDemand(demand)) {
                  SingleThreadedBufferingSubscriber.this.upstreamState = SingleThreadedBufferingSubscriber.BackPressureState.DEMANDING;
                  SingleThreadedBufferingSubscriber.this.flushBuffer();
               }
            case DONE:
            default:
               break;
            case DEMANDING:
               SingleThreadedBufferingSubscriber.this.registerDemand(demand);
         }

      }
   }
}
