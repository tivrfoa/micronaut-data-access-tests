package io.micronaut.core.async.subscriber;

import java.util.concurrent.atomic.AtomicBoolean;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CompletionAwareSubscriber<T> implements Subscriber<T>, Emitter<T> {
   private static final Logger LOG = LoggerFactory.getLogger(CompletionAwareSubscriber.class);
   protected Subscription subscription;
   private final AtomicBoolean complete = new AtomicBoolean(false);

   @Override
   public final void onSubscribe(Subscription s) {
      this.subscription = s;
      this.doOnSubscribe(this.subscription);
   }

   public boolean isComplete() {
      return this.complete.get();
   }

   @Override
   public final void onNext(T t) {
      if (!this.complete.get()) {
         try {
            this.doOnNext(t);
         } catch (Throwable var3) {
            this.onError(var3);
         }
      }

   }

   @Override
   public final void onError(Throwable t) {
      if (this.subscription != null && this.complete.compareAndSet(false, true)) {
         this.subscription.cancel();
         this.doOnError(t);
      } else if (LOG.isDebugEnabled()) {
         LOG.debug("Discarding error because subscriber has already completed", t);
      }

   }

   @Override
   public final void onComplete() {
      if (this.complete.compareAndSet(false, true)) {
         try {
            this.doOnComplete();
         } catch (Exception var2) {
            this.doOnError(var2);
         }
      }

   }

   protected abstract void doOnSubscribe(Subscription subscription);

   protected abstract void doOnNext(T message);

   protected abstract void doOnError(Throwable t);

   protected abstract void doOnComplete();
}
