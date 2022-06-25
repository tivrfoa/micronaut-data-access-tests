package io.micronaut.core.async.publisher;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class AsyncSingleResultPublisher<T> implements Publishers.MicronautPublisher<T> {
   private final ExecutorService executor;
   private final Supplier<T> supplier;

   public AsyncSingleResultPublisher(ExecutorService executor, Supplier<T> supplier) {
      this.executor = executor;
      this.supplier = supplier;
   }

   public AsyncSingleResultPublisher(Supplier<T> supplier) {
      this(ForkJoinPool.commonPool(), supplier);
   }

   @Override
   public final void subscribe(Subscriber<? super T> subscriber) {
      Objects.requireNonNull(subscriber, "Subscriber cannot be null");
      subscriber.onSubscribe(new AsyncSingleResultPublisher.ExecutorServiceSubscription<>(subscriber, this.supplier, this.executor));
   }

   static class ExecutorServiceSubscription<S> implements Subscription {
      private final Subscriber<? super S> subscriber;
      private final ExecutorService executor;
      private final Supplier<S> supplier;
      private Future<?> future;
      private boolean completed;

      ExecutorServiceSubscription(Subscriber<? super S> subscriber, Supplier<S> supplier, ExecutorService executor) {
         this.subscriber = subscriber;
         this.supplier = supplier;
         this.executor = executor;
      }

      @Override
      public synchronized void request(long n) {
         if (n != 0L && !this.completed) {
            this.completed = true;
            if (n < 0L) {
               IllegalArgumentException ex = new IllegalArgumentException();
               this.executor.execute(() -> this.subscriber.onError(ex));
            } else {
               this.future = this.executor.submit(() -> {
                  try {
                     S value = (S)this.supplier.get();
                     if (value != null) {
                        this.subscriber.onNext(value);
                     }

                     this.subscriber.onComplete();
                  } catch (Exception var2) {
                     this.subscriber.onError(var2);
                  }

               });
            }
         }

      }

      @Override
      public synchronized void cancel() {
         this.completed = true;
         if (this.future != null) {
            this.future.cancel(false);
         }

      }
   }
}
