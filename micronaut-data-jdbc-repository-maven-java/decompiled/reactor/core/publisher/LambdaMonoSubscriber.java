package reactor.core.publisher;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class LambdaMonoSubscriber<T> implements InnerConsumer<T>, Disposable {
   final Consumer<? super T> consumer;
   final Consumer<? super Throwable> errorConsumer;
   final Runnable completeConsumer;
   final Consumer<? super Subscription> subscriptionConsumer;
   final Context initialContext;
   volatile Subscription subscription;
   static final AtomicReferenceFieldUpdater<LambdaMonoSubscriber, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
      LambdaMonoSubscriber.class, Subscription.class, "subscription"
   );

   LambdaMonoSubscriber(
      @Nullable Consumer<? super T> consumer,
      @Nullable Consumer<? super Throwable> errorConsumer,
      @Nullable Runnable completeConsumer,
      @Nullable Consumer<? super Subscription> subscriptionConsumer,
      @Nullable Context initialContext
   ) {
      this.consumer = consumer;
      this.errorConsumer = errorConsumer;
      this.completeConsumer = completeConsumer;
      this.subscriptionConsumer = subscriptionConsumer;
      this.initialContext = initialContext == null ? Context.empty() : initialContext;
   }

   LambdaMonoSubscriber(
      @Nullable Consumer<? super T> consumer,
      @Nullable Consumer<? super Throwable> errorConsumer,
      @Nullable Runnable completeConsumer,
      @Nullable Consumer<? super Subscription> subscriptionConsumer
   ) {
      this(consumer, errorConsumer, completeConsumer, subscriptionConsumer, null);
   }

   @Override
   public Context currentContext() {
      return this.initialContext;
   }

   @Override
   public final void onSubscribe(Subscription s) {
      if (Operators.validate(this.subscription, s)) {
         this.subscription = s;
         if (this.subscriptionConsumer != null) {
            try {
               this.subscriptionConsumer.accept(s);
            } catch (Throwable var3) {
               Exceptions.throwIfFatal(var3);
               s.cancel();
               this.onError(var3);
            }
         } else {
            s.request(Long.MAX_VALUE);
         }
      }

   }

   @Override
   public final void onComplete() {
      Subscription s = (Subscription)S.getAndSet(this, Operators.cancelledSubscription());
      if (s != Operators.cancelledSubscription()) {
         if (this.completeConsumer != null) {
            try {
               this.completeConsumer.run();
            } catch (Throwable var3) {
               Operators.onErrorDropped(var3, this.initialContext);
            }
         }

      }
   }

   @Override
   public final void onError(Throwable t) {
      Subscription s = (Subscription)S.getAndSet(this, Operators.cancelledSubscription());
      if (s == Operators.cancelledSubscription()) {
         Operators.onErrorDropped(t, this.initialContext);
      } else {
         this.doError(t);
      }
   }

   void doError(Throwable t) {
      if (this.errorConsumer != null) {
         this.errorConsumer.accept(t);
      } else {
         Operators.onErrorDropped(Exceptions.errorCallbackNotImplemented(t), this.initialContext);
      }

   }

   @Override
   public final void onNext(T x) {
      Subscription s = (Subscription)S.getAndSet(this, Operators.cancelledSubscription());
      if (s == Operators.cancelledSubscription()) {
         Operators.onNextDropped(x, this.initialContext);
      } else {
         if (this.consumer != null) {
            try {
               this.consumer.accept(x);
            } catch (Throwable var5) {
               Exceptions.throwIfFatal(var5);
               s.cancel();
               this.doError(var5);
            }
         }

         if (this.completeConsumer != null) {
            try {
               this.completeConsumer.run();
            } catch (Throwable var4) {
               Operators.onErrorDropped(var4, this.initialContext);
            }
         }

      }
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return this.subscription;
      } else if (key == Scannable.Attr.PREFETCH) {
         return Integer.MAX_VALUE;
      } else if (key == Scannable.Attr.TERMINATED || key == Scannable.Attr.CANCELLED) {
         return this.isDisposed();
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   @Override
   public boolean isDisposed() {
      return this.subscription == Operators.cancelledSubscription();
   }

   @Override
   public void dispose() {
      Subscription s = (Subscription)S.getAndSet(this, Operators.cancelledSubscription());
      if (s != null && s != Operators.cancelledSubscription()) {
         s.cancel();
      }

   }
}
