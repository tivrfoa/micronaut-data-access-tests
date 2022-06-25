package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Exceptions;

public abstract class BaseSubscriber<T> implements CoreSubscriber<T>, Subscription, Disposable {
   volatile Subscription subscription;
   static AtomicReferenceFieldUpdater<BaseSubscriber, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
      BaseSubscriber.class, Subscription.class, "subscription"
   );

   protected Subscription upstream() {
      return this.subscription;
   }

   @Override
   public boolean isDisposed() {
      return this.subscription == Operators.cancelledSubscription();
   }

   @Override
   public void dispose() {
      this.cancel();
   }

   protected void hookOnSubscribe(Subscription subscription) {
      subscription.request(Long.MAX_VALUE);
   }

   protected void hookOnNext(T value) {
   }

   protected void hookOnComplete() {
   }

   protected void hookOnError(Throwable throwable) {
      throw Exceptions.errorCallbackNotImplemented(throwable);
   }

   protected void hookOnCancel() {
   }

   protected void hookFinally(SignalType type) {
   }

   @Override
   public final void onSubscribe(Subscription s) {
      if (Operators.setOnce(S, this, s)) {
         try {
            this.hookOnSubscribe(s);
         } catch (Throwable var3) {
            this.onError(Operators.onOperatorError(s, var3, this.currentContext()));
         }
      }

   }

   @Override
   public final void onNext(T value) {
      Objects.requireNonNull(value, "onNext");

      try {
         this.hookOnNext(value);
      } catch (Throwable var3) {
         this.onError(Operators.onOperatorError(this.subscription, var3, value, this.currentContext()));
      }

   }

   @Override
   public final void onError(Throwable t) {
      Objects.requireNonNull(t, "onError");
      if (S.getAndSet(this, Operators.cancelledSubscription()) == Operators.cancelledSubscription()) {
         Operators.onErrorDropped(t, this.currentContext());
      } else {
         try {
            this.hookOnError(t);
         } catch (Throwable var6) {
            Throwable e = Exceptions.addSuppressed(var6, t);
            Operators.onErrorDropped(e, this.currentContext());
         } finally {
            this.safeHookFinally(SignalType.ON_ERROR);
         }

      }
   }

   @Override
   public final void onComplete() {
      if (S.getAndSet(this, Operators.cancelledSubscription()) != Operators.cancelledSubscription()) {
         try {
            this.hookOnComplete();
         } catch (Throwable var5) {
            this.hookOnError(Operators.onOperatorError(var5, this.currentContext()));
         } finally {
            this.safeHookFinally(SignalType.ON_COMPLETE);
         }
      }

   }

   @Override
   public final void request(long n) {
      if (Operators.validate(n)) {
         Subscription s = this.subscription;
         if (s != null) {
            s.request(n);
         }
      }

   }

   public final void requestUnbounded() {
      this.request(Long.MAX_VALUE);
   }

   @Override
   public final void cancel() {
      if (Operators.terminate(S, this)) {
         try {
            this.hookOnCancel();
         } catch (Throwable var5) {
            this.hookOnError(Operators.onOperatorError(this.subscription, var5, this.currentContext()));
         } finally {
            this.safeHookFinally(SignalType.CANCEL);
         }
      }

   }

   void safeHookFinally(SignalType type) {
      try {
         this.hookFinally(type);
      } catch (Throwable var3) {
         Operators.onErrorDropped(var3, this.currentContext());
      }

   }

   public String toString() {
      return this.getClass().getSimpleName();
   }
}
