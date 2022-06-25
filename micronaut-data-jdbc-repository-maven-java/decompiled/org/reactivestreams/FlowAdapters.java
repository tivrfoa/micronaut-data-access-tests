package org.reactivestreams;

import java.util.Objects;

public final class FlowAdapters {
   private FlowAdapters() {
      throw new IllegalStateException("No instances!");
   }

   public static <T> Publisher<T> toPublisher(java.util.concurrent.Flow.Publisher<? extends T> flowPublisher) {
      Objects.requireNonNull(flowPublisher, "flowPublisher");
      Publisher<T> publisher;
      if (flowPublisher instanceof FlowAdapters.FlowPublisherFromReactive) {
         publisher = ((FlowAdapters.FlowPublisherFromReactive)flowPublisher).reactiveStreams;
      } else if (flowPublisher instanceof Publisher) {
         publisher = (Publisher)flowPublisher;
      } else {
         publisher = new FlowAdapters.ReactivePublisherFromFlow<>(flowPublisher);
      }

      return publisher;
   }

   public static <T> java.util.concurrent.Flow.Publisher<T> toFlowPublisher(Publisher<? extends T> reactiveStreamsPublisher) {
      Objects.requireNonNull(reactiveStreamsPublisher, "reactiveStreamsPublisher");
      java.util.concurrent.Flow.Publisher<T> flowPublisher;
      if (reactiveStreamsPublisher instanceof FlowAdapters.ReactivePublisherFromFlow) {
         flowPublisher = ((FlowAdapters.ReactivePublisherFromFlow)reactiveStreamsPublisher).flow;
      } else if (reactiveStreamsPublisher instanceof java.util.concurrent.Flow.Publisher) {
         flowPublisher = (java.util.concurrent.Flow.Publisher)reactiveStreamsPublisher;
      } else {
         flowPublisher = new FlowAdapters.FlowPublisherFromReactive<>(reactiveStreamsPublisher);
      }

      return flowPublisher;
   }

   public static <T, U> Processor<T, U> toProcessor(java.util.concurrent.Flow.Processor<? super T, ? extends U> flowProcessor) {
      Objects.requireNonNull(flowProcessor, "flowProcessor");
      Processor<T, U> processor;
      if (flowProcessor instanceof FlowAdapters.FlowToReactiveProcessor) {
         processor = (Processor<T, U>)((FlowAdapters.FlowToReactiveProcessor)flowProcessor).reactiveStreams;
      } else if (flowProcessor instanceof Processor) {
         processor = (Processor)flowProcessor;
      } else {
         processor = new FlowAdapters.ReactiveToFlowProcessor<>(flowProcessor);
      }

      return processor;
   }

   public static <T, U> java.util.concurrent.Flow.Processor<T, U> toFlowProcessor(Processor<? super T, ? extends U> reactiveStreamsProcessor) {
      Objects.requireNonNull(reactiveStreamsProcessor, "reactiveStreamsProcessor");
      java.util.concurrent.Flow.Processor<T, U> flowProcessor;
      if (reactiveStreamsProcessor instanceof FlowAdapters.ReactiveToFlowProcessor) {
         flowProcessor = (java.util.concurrent.Flow.Processor<T, U>)((FlowAdapters.ReactiveToFlowProcessor)reactiveStreamsProcessor).flow;
      } else if (reactiveStreamsProcessor instanceof java.util.concurrent.Flow.Processor) {
         flowProcessor = (java.util.concurrent.Flow.Processor)reactiveStreamsProcessor;
      } else {
         flowProcessor = new FlowAdapters.FlowToReactiveProcessor<>(reactiveStreamsProcessor);
      }

      return flowProcessor;
   }

   public static <T> java.util.concurrent.Flow.Subscriber<T> toFlowSubscriber(Subscriber<T> reactiveStreamsSubscriber) {
      Objects.requireNonNull(reactiveStreamsSubscriber, "reactiveStreamsSubscriber");
      java.util.concurrent.Flow.Subscriber<T> flowSubscriber;
      if (reactiveStreamsSubscriber instanceof FlowAdapters.ReactiveToFlowSubscriber) {
         flowSubscriber = (java.util.concurrent.Flow.Subscriber<T>)((FlowAdapters.ReactiveToFlowSubscriber)reactiveStreamsSubscriber).flow;
      } else if (reactiveStreamsSubscriber instanceof java.util.concurrent.Flow.Subscriber) {
         flowSubscriber = (java.util.concurrent.Flow.Subscriber)reactiveStreamsSubscriber;
      } else {
         flowSubscriber = new FlowAdapters.FlowToReactiveSubscriber<>(reactiveStreamsSubscriber);
      }

      return flowSubscriber;
   }

   public static <T> Subscriber<T> toSubscriber(java.util.concurrent.Flow.Subscriber<T> flowSubscriber) {
      Objects.requireNonNull(flowSubscriber, "flowSubscriber");
      Subscriber<T> subscriber;
      if (flowSubscriber instanceof FlowAdapters.FlowToReactiveSubscriber) {
         subscriber = (Subscriber<T>)((FlowAdapters.FlowToReactiveSubscriber)flowSubscriber).reactiveStreams;
      } else if (flowSubscriber instanceof Subscriber) {
         subscriber = (Subscriber)flowSubscriber;
      } else {
         subscriber = new FlowAdapters.ReactiveToFlowSubscriber<>(flowSubscriber);
      }

      return subscriber;
   }

   static final class FlowPublisherFromReactive<T> implements java.util.concurrent.Flow.Publisher<T> {
      final Publisher<? extends T> reactiveStreams;

      public FlowPublisherFromReactive(Publisher<? extends T> reactivePublisher) {
         this.reactiveStreams = reactivePublisher;
      }

      public void subscribe(java.util.concurrent.Flow.Subscriber<? super T> flow) {
         this.reactiveStreams.subscribe(flow == null ? null : new FlowAdapters.ReactiveToFlowSubscriber<>(flow));
      }
   }

   static final class FlowToReactiveProcessor<T, U> implements java.util.concurrent.Flow.Processor<T, U> {
      final Processor<? super T, ? extends U> reactiveStreams;

      public FlowToReactiveProcessor(Processor<? super T, ? extends U> reactive) {
         this.reactiveStreams = reactive;
      }

      public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
         this.reactiveStreams.onSubscribe(subscription == null ? null : new FlowAdapters.ReactiveToFlowSubscription(subscription));
      }

      public void onNext(T t) {
         this.reactiveStreams.onNext(t);
      }

      public void onError(Throwable t) {
         this.reactiveStreams.onError(t);
      }

      public void onComplete() {
         this.reactiveStreams.onComplete();
      }

      public void subscribe(java.util.concurrent.Flow.Subscriber<? super U> s) {
         this.reactiveStreams.subscribe(s == null ? null : new FlowAdapters.ReactiveToFlowSubscriber<>(s));
      }
   }

   static final class FlowToReactiveSubscriber<T> implements java.util.concurrent.Flow.Subscriber<T> {
      final Subscriber<? super T> reactiveStreams;

      public FlowToReactiveSubscriber(Subscriber<? super T> reactive) {
         this.reactiveStreams = reactive;
      }

      public void onSubscribe(java.util.concurrent.Flow.Subscription subscription) {
         this.reactiveStreams.onSubscribe(subscription == null ? null : new FlowAdapters.ReactiveToFlowSubscription(subscription));
      }

      public void onNext(T item) {
         this.reactiveStreams.onNext(item);
      }

      public void onError(Throwable throwable) {
         this.reactiveStreams.onError(throwable);
      }

      public void onComplete() {
         this.reactiveStreams.onComplete();
      }
   }

   static final class FlowToReactiveSubscription implements java.util.concurrent.Flow.Subscription {
      final Subscription reactiveStreams;

      public FlowToReactiveSubscription(Subscription reactive) {
         this.reactiveStreams = reactive;
      }

      public void request(long n) {
         this.reactiveStreams.request(n);
      }

      public void cancel() {
         this.reactiveStreams.cancel();
      }
   }

   static final class ReactivePublisherFromFlow<T> implements Publisher<T> {
      final java.util.concurrent.Flow.Publisher<? extends T> flow;

      public ReactivePublisherFromFlow(java.util.concurrent.Flow.Publisher<? extends T> flowPublisher) {
         this.flow = flowPublisher;
      }

      @Override
      public void subscribe(Subscriber<? super T> reactive) {
         this.flow.subscribe(reactive == null ? null : new FlowAdapters.FlowToReactiveSubscriber<>(reactive));
      }
   }

   static final class ReactiveToFlowProcessor<T, U> implements Processor<T, U> {
      final java.util.concurrent.Flow.Processor<? super T, ? extends U> flow;

      public ReactiveToFlowProcessor(java.util.concurrent.Flow.Processor<? super T, ? extends U> flow) {
         this.flow = flow;
      }

      @Override
      public void onSubscribe(Subscription subscription) {
         this.flow.onSubscribe(subscription == null ? null : new FlowAdapters.FlowToReactiveSubscription(subscription));
      }

      @Override
      public void onNext(T t) {
         this.flow.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         this.flow.onError(t);
      }

      @Override
      public void onComplete() {
         this.flow.onComplete();
      }

      @Override
      public void subscribe(Subscriber<? super U> s) {
         this.flow.subscribe(s == null ? null : new FlowAdapters.FlowToReactiveSubscriber<>(s));
      }
   }

   static final class ReactiveToFlowSubscriber<T> implements Subscriber<T> {
      final java.util.concurrent.Flow.Subscriber<? super T> flow;

      public ReactiveToFlowSubscriber(java.util.concurrent.Flow.Subscriber<? super T> flow) {
         this.flow = flow;
      }

      @Override
      public void onSubscribe(Subscription subscription) {
         this.flow.onSubscribe(subscription == null ? null : new FlowAdapters.FlowToReactiveSubscription(subscription));
      }

      @Override
      public void onNext(T item) {
         this.flow.onNext(item);
      }

      @Override
      public void onError(Throwable throwable) {
         this.flow.onError(throwable);
      }

      @Override
      public void onComplete() {
         this.flow.onComplete();
      }
   }

   static final class ReactiveToFlowSubscription implements Subscription {
      final java.util.concurrent.Flow.Subscription flow;

      public ReactiveToFlowSubscription(java.util.concurrent.Flow.Subscription flow) {
         this.flow = flow;
      }

      @Override
      public void request(long n) {
         this.flow.request(n);
      }

      @Override
      public void cancel() {
         this.flow.cancel();
      }
   }
}
