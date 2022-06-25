package reactor.adapter;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.core.publisher.Flux;

public abstract class JdkFlowAdapter {
   public static <T> java.util.concurrent.Flow.Publisher<T> publisherToFlowPublisher(Publisher<T> publisher) {
      return new JdkFlowAdapter.PublisherAsFlowPublisher<>(publisher);
   }

   public static <T> Flux<T> flowPublisherToFlux(java.util.concurrent.Flow.Publisher<T> publisher) {
      return new JdkFlowAdapter.FlowPublisherAsFlux<>(publisher);
   }

   JdkFlowAdapter() {
   }

   private static class FlowPublisherAsFlux<T> extends Flux<T> implements Scannable {
      private final java.util.concurrent.Flow.Publisher<T> pub;

      private FlowPublisherAsFlux(java.util.concurrent.Flow.Publisher<T> pub) {
         this.pub = pub;
      }

      @Override
      public void subscribe(CoreSubscriber<? super T> actual) {
         this.pub.subscribe(new JdkFlowAdapter.SubscriberToRS<>(actual));
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return null;
      }
   }

   private static class FlowSubscriber<T> implements CoreSubscriber<T>, Subscription {
      private final Subscriber<? super T> subscriber;
      org.reactivestreams.Subscription subscription;

      public FlowSubscriber(Subscriber<? super T> subscriber) {
         this.subscriber = subscriber;
      }

      @Override
      public void onSubscribe(org.reactivestreams.Subscription s) {
         this.subscription = s;
         this.subscriber.onSubscribe(this);
      }

      @Override
      public void onNext(T o) {
         this.subscriber.onNext(o);
      }

      @Override
      public void onError(Throwable t) {
         this.subscriber.onError(t);
      }

      @Override
      public void onComplete() {
         this.subscriber.onComplete();
      }

      public void request(long n) {
         this.subscription.request(n);
      }

      public void cancel() {
         this.subscription.cancel();
      }
   }

   private static class PublisherAsFlowPublisher<T> implements java.util.concurrent.Flow.Publisher<T> {
      private final Publisher<T> pub;

      private PublisherAsFlowPublisher(Publisher<T> pub) {
         this.pub = pub;
      }

      public void subscribe(Subscriber<? super T> subscriber) {
         this.pub.subscribe(new JdkFlowAdapter.FlowSubscriber<>(subscriber));
      }
   }

   private static class SubscriberToRS<T> implements Subscriber<T>, org.reactivestreams.Subscription {
      private final org.reactivestreams.Subscriber<? super T> s;
      Subscription subscription;

      public SubscriberToRS(org.reactivestreams.Subscriber<? super T> s) {
         this.s = s;
      }

      public void onSubscribe(Subscription subscription) {
         this.subscription = subscription;
         this.s.onSubscribe(this);
      }

      public void onNext(T o) {
         this.s.onNext(o);
      }

      public void onError(Throwable throwable) {
         this.s.onError(throwable);
      }

      public void onComplete() {
         this.s.onComplete();
      }

      @Override
      public void request(long n) {
         this.subscription.request(n);
      }

      @Override
      public void cancel() {
         this.subscription.cancel();
      }
   }
}
