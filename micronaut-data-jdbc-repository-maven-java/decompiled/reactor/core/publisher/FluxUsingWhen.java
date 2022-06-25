package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxUsingWhen<T, S> extends Flux<T> implements SourceProducer<T> {
   final Publisher<S> resourceSupplier;
   final Function<? super S, ? extends Publisher<? extends T>> resourceClosure;
   final Function<? super S, ? extends Publisher<?>> asyncComplete;
   final BiFunction<? super S, ? super Throwable, ? extends Publisher<?>> asyncError;
   @Nullable
   final Function<? super S, ? extends Publisher<?>> asyncCancel;

   FluxUsingWhen(
      Publisher<S> resourceSupplier,
      Function<? super S, ? extends Publisher<? extends T>> resourceClosure,
      Function<? super S, ? extends Publisher<?>> asyncComplete,
      BiFunction<? super S, ? super Throwable, ? extends Publisher<?>> asyncError,
      @Nullable Function<? super S, ? extends Publisher<?>> asyncCancel
   ) {
      this.resourceSupplier = (Publisher)Objects.requireNonNull(resourceSupplier, "resourceSupplier");
      this.resourceClosure = (Function)Objects.requireNonNull(resourceClosure, "resourceClosure");
      this.asyncComplete = (Function)Objects.requireNonNull(asyncComplete, "asyncComplete");
      this.asyncError = (BiFunction)Objects.requireNonNull(asyncError, "asyncError");
      this.asyncCancel = asyncCancel;
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      if (this.resourceSupplier instanceof Callable) {
         try {
            Callable<S> resourceCallable = (Callable)this.resourceSupplier;
            S resource = (S)resourceCallable.call();
            if (resource == null) {
               Operators.complete(actual);
            } else {
               Publisher<? extends T> p = deriveFluxFromResource(resource, this.resourceClosure);
               FluxUsingWhen.UsingWhenSubscriber<? super T, S> subscriber = prepareSubscriberForResource(
                  resource, actual, this.asyncComplete, this.asyncError, this.asyncCancel, null
               );
               p.subscribe(subscriber);
            }
         } catch (Throwable var6) {
            Operators.error(actual, var6);
         }

      } else {
         this.resourceSupplier
            .subscribe(
               new FluxUsingWhen.ResourceSubscriber<>(
                  actual, this.resourceClosure, this.asyncComplete, this.asyncError, this.asyncCancel, this.resourceSupplier instanceof Mono
               )
            );
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }

   private static <RESOURCE, T> Publisher<? extends T> deriveFluxFromResource(
      RESOURCE resource, Function<? super RESOURCE, ? extends Publisher<? extends T>> resourceClosure
   ) {
      Publisher<? extends T> p;
      try {
         p = (Publisher)Objects.requireNonNull(resourceClosure.apply(resource), "The resourceClosure function returned a null value");
      } catch (Throwable var4) {
         p = Flux.error(var4);
      }

      return p;
   }

   private static <RESOURCE, T> FluxUsingWhen.UsingWhenSubscriber<? super T, RESOURCE> prepareSubscriberForResource(
      RESOURCE resource,
      CoreSubscriber<? super T> actual,
      Function<? super RESOURCE, ? extends Publisher<?>> asyncComplete,
      BiFunction<? super RESOURCE, ? super Throwable, ? extends Publisher<?>> asyncError,
      @Nullable Function<? super RESOURCE, ? extends Publisher<?>> asyncCancel,
      @Nullable Operators.DeferredSubscription arbiter
   ) {
      if (actual instanceof Fuseable.ConditionalSubscriber) {
         Fuseable.ConditionalSubscriber<? super T> conditionalActual = (Fuseable.ConditionalSubscriber)actual;
         return new FluxUsingWhen.UsingWhenConditionalSubscriber<>(conditionalActual, resource, asyncComplete, asyncError, asyncCancel, arbiter);
      } else {
         return new FluxUsingWhen.UsingWhenSubscriber<>(actual, resource, asyncComplete, asyncError, asyncCancel, arbiter);
      }
   }

   static final class CancelInner implements InnerConsumer<Object> {
      final FluxUsingWhen.UsingWhenParent parent;

      CancelInner(FluxUsingWhen.UsingWhenParent ts) {
         this.parent = ts;
      }

      @Override
      public Context currentContext() {
         return this.parent.currentContext();
      }

      @Override
      public void onSubscribe(Subscription s) {
         ((Subscription)Objects.requireNonNull(s, "Subscription cannot be null")).request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(Object o) {
      }

      @Override
      public void onError(Throwable e) {
         Loggers.getLogger(FluxUsingWhen.class).warn("Async resource cleanup failed after cancel", e);
      }

      @Override
      public void onComplete() {
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.parent;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.parent.actual();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }
   }

   static final class CommitInner implements InnerConsumer<Object> {
      final FluxUsingWhen.UsingWhenParent parent;
      boolean done;

      CommitInner(FluxUsingWhen.UsingWhenParent ts) {
         this.parent = ts;
      }

      @Override
      public Context currentContext() {
         return this.parent.currentContext();
      }

      @Override
      public void onSubscribe(Subscription s) {
         ((Subscription)Objects.requireNonNull(s, "Subscription cannot be null")).request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(Object o) {
      }

      @Override
      public void onError(Throwable e) {
         this.done = true;
         Throwable e_ = Operators.onOperatorError(e, this.parent.currentContext());
         Throwable commitError = new RuntimeException("Async resource cleanup failed after onComplete", e_);
         this.parent.deferredError(commitError);
      }

      @Override
      public void onComplete() {
         this.done = true;
         this.parent.deferredComplete();
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.parent;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.parent.actual();
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }
   }

   static class ResourceSubscriber<S, T> extends Operators.DeferredSubscription implements InnerConsumer<S> {
      final CoreSubscriber<? super T> actual;
      final Function<? super S, ? extends Publisher<? extends T>> resourceClosure;
      final Function<? super S, ? extends Publisher<?>> asyncComplete;
      final BiFunction<? super S, ? super Throwable, ? extends Publisher<?>> asyncError;
      @Nullable
      final Function<? super S, ? extends Publisher<?>> asyncCancel;
      final boolean isMonoSource;
      Subscription resourceSubscription;
      boolean resourceProvided;

      ResourceSubscriber(
         CoreSubscriber<? super T> actual,
         Function<? super S, ? extends Publisher<? extends T>> resourceClosure,
         Function<? super S, ? extends Publisher<?>> asyncComplete,
         BiFunction<? super S, ? super Throwable, ? extends Publisher<?>> asyncError,
         @Nullable Function<? super S, ? extends Publisher<?>> asyncCancel,
         boolean isMonoSource
      ) {
         this.actual = (CoreSubscriber)Objects.requireNonNull(actual, "actual");
         this.resourceClosure = (Function)Objects.requireNonNull(resourceClosure, "resourceClosure");
         this.asyncComplete = (Function)Objects.requireNonNull(asyncComplete, "asyncComplete");
         this.asyncError = (BiFunction)Objects.requireNonNull(asyncError, "asyncError");
         this.asyncCancel = asyncCancel;
         this.isMonoSource = isMonoSource;
      }

      @Override
      public void onNext(S resource) {
         if (this.resourceProvided) {
            Operators.onNextDropped(resource, this.actual.currentContext());
         } else {
            this.resourceProvided = true;
            Publisher<? extends T> p = FluxUsingWhen.deriveFluxFromResource(resource, this.resourceClosure);
            p.subscribe(FluxUsingWhen.prepareSubscriberForResource(resource, this.actual, this.asyncComplete, this.asyncError, this.asyncCancel, this));
            if (!this.isMonoSource) {
               this.resourceSubscription.cancel();
            }

         }
      }

      @Override
      public Context currentContext() {
         return this.actual.currentContext();
      }

      @Override
      public void onError(Throwable throwable) {
         if (this.resourceProvided) {
            Operators.onErrorDropped(throwable, this.actual.currentContext());
         } else {
            this.actual.onError(throwable);
         }
      }

      @Override
      public void onComplete() {
         if (!this.resourceProvided) {
            this.actual.onComplete();
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.resourceSubscription, s)) {
            this.resourceSubscription = s;
            this.actual.onSubscribe(this);
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void cancel() {
         if (!this.resourceProvided) {
            this.resourceSubscription.cancel();
         }

         super.cancel();
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.resourceSubscription;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.actual;
         } else if (key == Scannable.Attr.PREFETCH) {
            return Integer.MAX_VALUE;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.resourceProvided;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }
   }

   static final class RollbackInner implements InnerConsumer<Object> {
      final FluxUsingWhen.UsingWhenParent parent;
      final Throwable rollbackCause;
      boolean done;

      RollbackInner(FluxUsingWhen.UsingWhenParent ts, Throwable rollbackCause) {
         this.parent = ts;
         this.rollbackCause = rollbackCause;
      }

      @Override
      public Context currentContext() {
         return this.parent.currentContext();
      }

      @Override
      public void onSubscribe(Subscription s) {
         ((Subscription)Objects.requireNonNull(s, "Subscription cannot be null")).request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(Object o) {
      }

      @Override
      public void onError(Throwable e) {
         this.done = true;
         RuntimeException rollbackError = new RuntimeException("Async resource cleanup failed after onError", e);
         this.parent.deferredError(Exceptions.addSuppressed(rollbackError, this.rollbackCause));
      }

      @Override
      public void onComplete() {
         this.done = true;
         this.parent.deferredError(this.rollbackCause);
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.parent;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.parent.actual();
         } else if (key == Scannable.Attr.ERROR) {
            return this.rollbackCause;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }
   }

   static final class UsingWhenConditionalSubscriber<T, S> extends FluxUsingWhen.UsingWhenSubscriber<T, S> implements Fuseable.ConditionalSubscriber<T> {
      final Fuseable.ConditionalSubscriber<? super T> actual;

      UsingWhenConditionalSubscriber(
         Fuseable.ConditionalSubscriber<? super T> actual,
         S resource,
         Function<? super S, ? extends Publisher<?>> asyncComplete,
         BiFunction<? super S, ? super Throwable, ? extends Publisher<?>> asyncError,
         @Nullable Function<? super S, ? extends Publisher<?>> asyncCancel,
         @Nullable Operators.DeferredSubscription arbiter
      ) {
         super(actual, resource, asyncComplete, asyncError, asyncCancel, arbiter);
         this.actual = actual;
      }

      @Override
      public boolean tryOnNext(T t) {
         return this.actual.tryOnNext(t);
      }
   }

   private interface UsingWhenParent<T> extends InnerOperator<T, T> {
      void deferredComplete();

      void deferredError(Throwable var1);
   }

   static class UsingWhenSubscriber<T, S> implements FluxUsingWhen.UsingWhenParent<T> {
      final CoreSubscriber<? super T> actual;
      final S resource;
      final Function<? super S, ? extends Publisher<?>> asyncComplete;
      final BiFunction<? super S, ? super Throwable, ? extends Publisher<?>> asyncError;
      @Nullable
      final Function<? super S, ? extends Publisher<?>> asyncCancel;
      @Nullable
      final Operators.DeferredSubscription arbiter;
      volatile int callbackApplied;
      static final AtomicIntegerFieldUpdater<FluxUsingWhen.UsingWhenSubscriber> CALLBACK_APPLIED = AtomicIntegerFieldUpdater.newUpdater(
         FluxUsingWhen.UsingWhenSubscriber.class, "callbackApplied"
      );
      Throwable error;
      Subscription s;

      UsingWhenSubscriber(
         CoreSubscriber<? super T> actual,
         S resource,
         Function<? super S, ? extends Publisher<?>> asyncComplete,
         BiFunction<? super S, ? super Throwable, ? extends Publisher<?>> asyncError,
         @Nullable Function<? super S, ? extends Publisher<?>> asyncCancel,
         @Nullable Operators.DeferredSubscription arbiter
      ) {
         this.actual = actual;
         this.resource = resource;
         this.asyncComplete = asyncComplete;
         this.asyncError = asyncError;
         this.asyncCancel = asyncCancel;
         this.arbiter = arbiter;
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.error != null;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error == Exceptions.TERMINATED ? null : this.error;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.callbackApplied == 3;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : FluxUsingWhen.UsingWhenParent.super.scanUnsafe(key);
         }
      }

      @Override
      public void request(long l) {
         if (Operators.validate(l)) {
            this.s.request(l);
         }

      }

      @Override
      public void cancel() {
         if (CALLBACK_APPLIED.compareAndSet(this, 0, 3)) {
            this.s.cancel();

            try {
               if (this.asyncCancel != null) {
                  Flux.<Object>from((Publisher<? extends Object>)this.asyncCancel.apply(this.resource)).subscribe(new FluxUsingWhen.CancelInner(this));
               } else {
                  Flux.<Object>from((Publisher<? extends Object>)this.asyncComplete.apply(this.resource)).subscribe(new FluxUsingWhen.CancelInner(this));
               }
            } catch (Throwable var2) {
               Loggers.getLogger(FluxUsingWhen.class).warn("Error generating async resource cleanup during onCancel", var2);
            }
         }

      }

      @Override
      public void onNext(T t) {
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         if (CALLBACK_APPLIED.compareAndSet(this, 0, 2)) {
            Publisher<?> p;
            try {
               p = (Publisher)Objects.requireNonNull(this.asyncError.apply(this.resource, t), "The asyncError returned a null Publisher");
            } catch (Throwable var5) {
               Throwable _e = Operators.onOperatorError(var5, this.actual.currentContext());
               _e = Exceptions.addSuppressed(_e, t);
               this.actual.onError(_e);
               return;
            }

            p.subscribe(new FluxUsingWhen.RollbackInner(this, t));
         }

      }

      @Override
      public void onComplete() {
         if (CALLBACK_APPLIED.compareAndSet(this, 0, 1)) {
            Publisher<?> p;
            try {
               p = (Publisher)Objects.requireNonNull(this.asyncComplete.apply(this.resource), "The asyncComplete returned a null Publisher");
            } catch (Throwable var4) {
               Throwable _e = Operators.onOperatorError(var4, this.actual.currentContext());
               this.deferredError(_e);
               return;
            }

            p.subscribe(new FluxUsingWhen.CommitInner(this));
         }

      }

      @Override
      public void deferredComplete() {
         this.error = Exceptions.TERMINATED;
         this.actual.onComplete();
      }

      @Override
      public void deferredError(Throwable error) {
         this.error = error;
         this.actual.onError(error);
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            if (this.arbiter == null) {
               this.actual.onSubscribe(this);
            } else {
               this.arbiter.set(this);
            }
         }

      }
   }
}
