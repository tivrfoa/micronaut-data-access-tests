package io.micronaut.core.async.publisher;

import io.micronaut.core.annotation.Internal;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Internal
public class CompletableFuturePublisher<T> implements Publishers.MicronautPublisher<T> {
   private final Supplier<CompletableFuture<T>> futureSupplier;

   CompletableFuturePublisher(Supplier<CompletableFuture<T>> futureSupplier) {
      this.futureSupplier = futureSupplier;
   }

   @Override
   public final void subscribe(Subscriber<? super T> subscriber) {
      Objects.requireNonNull(subscriber, "Subscriber cannot be null");
      subscriber.onSubscribe(new CompletableFuturePublisher.CompletableFutureSubscription(subscriber));
   }

   class CompletableFutureSubscription implements Subscription {
      private final Subscriber<? super T> subscriber;
      private final AtomicBoolean completed = new AtomicBoolean(false);
      private CompletableFuture<T> future;

      CompletableFutureSubscription(Subscriber<? super T> subscriber) {
         this.subscriber = subscriber;
      }

      @Override
      public synchronized void request(long n) {
         if (n != 0L && !this.completed.get()) {
            if (n < 0L) {
               IllegalArgumentException ex = new IllegalArgumentException("Cannot request a negative number");
               this.subscriber.onError(ex);
            } else {
               try {
                  CompletableFuture<T> future = (CompletableFuture)CompletableFuturePublisher.this.futureSupplier.get();
                  if (future == null) {
                     this.subscriber.onComplete();
                  } else {
                     this.future = future;
                     future.whenComplete((s, throwable) -> {
                        if (this.completed.compareAndSet(false, true)) {
                           if (throwable != null) {
                              this.subscriber.onError(throwable);
                           } else {
                              if (s != null) {
                                 this.subscriber.onNext((T)s);
                              }

                              this.subscriber.onComplete();
                           }
                        }

                     });
                  }
               } catch (Throwable var4) {
                  this.subscriber.onError(var4);
               }
            }
         }

      }

      @Override
      public synchronized void cancel() {
         if (this.completed.compareAndSet(false, true) && this.future != null) {
            this.future.cancel(false);
         }

      }
   }
}
