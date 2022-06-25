package io.micronaut.core.async.processor;

import io.micronaut.core.async.subscriber.CompletionAwareSubscriber;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public abstract class SingleSubscriberProcessor<T, R> extends CompletionAwareSubscriber<T> implements Processor<T, R> {
   protected static final Subscription EMPTY_SUBSCRIPTION = new Subscription() {
      @Override
      public void request(long n) {
      }

      @Override
      public void cancel() {
      }
   };
   protected Subscription parentSubscription;
   private final AtomicReference<Subscriber<? super R>> subscriber = new AtomicReference();

   @Override
   public final void subscribe(Subscriber<? super R> subscriber) {
      Objects.requireNonNull(subscriber, "Subscriber cannot be null");
      if (!this.subscriber.compareAndSet(null, subscriber)) {
         subscriber.onSubscribe(EMPTY_SUBSCRIPTION);
         subscriber.onError(new IllegalStateException("Only one subscriber allowed"));
      } else {
         this.doSubscribe(subscriber);
      }

   }

   protected abstract void doSubscribe(Subscriber<? super R> subscriber);

   protected Subscriber<? super R> getSubscriber() {
      Subscriber<? super R> subscriber = (Subscriber)this.subscriber.get();
      this.verifyState(subscriber);
      return subscriber;
   }

   protected Optional<Subscriber<? super R>> currentSubscriber() {
      Subscriber<? super R> subscriber = (Subscriber)this.subscriber.get();
      return Optional.ofNullable(subscriber);
   }

   protected void doAfterOnError(Throwable throwable) {
   }

   protected void doAfterComplete() {
   }

   protected void doAfterOnSubscribe(Subscription subscription) {
   }

   protected void doOnSubscribe(Subscription subscription, Subscriber<? super R> subscriber) {
      subscriber.onSubscribe(subscription);
   }

   @Override
   protected final void doOnSubscribe(Subscription subscription) {
      this.parentSubscription = subscription;
      Subscriber<? super R> subscriber = (Subscriber)this.subscriber.get();
      if (this.verifyState(subscriber)) {
         this.doOnSubscribe(subscription, subscriber);
         this.doAfterOnSubscribe(subscription);
      }
   }

   @Override
   protected final void doOnError(Throwable t) {
      try {
         Subscriber<? super R> subscriber = this.getSubscriber();
         this.parentSubscription.cancel();
         subscriber.onError(t);
      } finally {
         this.doAfterOnError(t);
      }

   }

   @Override
   protected void doOnComplete() {
      try {
         Subscriber<? super R> subscriber = this.getSubscriber();
         subscriber.onComplete();
      } finally {
         this.doAfterComplete();
      }

   }

   private boolean verifyState(Subscriber<? super R> subscriber) {
      if (subscriber == null) {
         throw new IllegalStateException("No subscriber present!");
      } else {
         boolean hasParent = this.parentSubscription != null;
         if (!hasParent) {
            subscriber.onSubscribe(EMPTY_SUBSCRIPTION);
            subscriber.onError(new IllegalStateException("Upstream publisher must be subscribed to first"));
         }

         return hasParent;
      }
   }
}
