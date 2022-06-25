package io.micronaut.core.async.publisher;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public abstract class SingleSubscriberPublisher<T> implements Publisher<T> {
   protected static final Subscription EMPTY_SUBSCRIPTION = new Subscription() {
      @Override
      public void request(long n) {
      }

      @Override
      public void cancel() {
      }
   };
   private final AtomicReference<Subscriber<? super T>> subscriber = new AtomicReference();

   @Override
   public final void subscribe(Subscriber<? super T> subscriber) {
      Objects.requireNonNull(subscriber, "Subscriber cannot be null");
      if (!this.subscriber.compareAndSet(null, subscriber)) {
         subscriber.onSubscribe(EMPTY_SUBSCRIPTION);
         subscriber.onError(new IllegalStateException("Only one subscriber allowed"));
      } else {
         this.doSubscribe(subscriber);
      }

   }

   protected abstract void doSubscribe(Subscriber<? super T> subscriber);

   protected Optional<Subscriber<? super T>> currentSubscriber() {
      return Optional.ofNullable(this.subscriber.get());
   }
}
