package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class MonoUsingWhen<T, S> extends Mono<T> implements SourceProducer<T> {
   final Publisher<S> resourceSupplier;
   final Function<? super S, ? extends Mono<? extends T>> resourceClosure;
   final Function<? super S, ? extends Publisher<?>> asyncComplete;
   final BiFunction<? super S, ? super Throwable, ? extends Publisher<?>> asyncError;
   @Nullable
   final Function<? super S, ? extends Publisher<?>> asyncCancel;

   MonoUsingWhen(
      Publisher<S> resourceSupplier,
      Function<? super S, ? extends Mono<? extends T>> resourceClosure,
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
               Mono<? extends T> p = deriveMonoFromResource(resource, this.resourceClosure);
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
               new MonoUsingWhen.ResourceSubscriber<>(
                  actual, this.resourceClosure, this.asyncComplete, this.asyncError, this.asyncCancel, this.resourceSupplier instanceof Mono
               )
            );
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }

   private static <RESOURCE, T> Mono<? extends T> deriveMonoFromResource(
      RESOURCE resource, Function<? super RESOURCE, ? extends Mono<? extends T>> resourceClosure
   ) {
      Mono<? extends T> p;
      try {
         p = (Mono)Objects.requireNonNull(resourceClosure.apply(resource), "The resourceClosure function returned a null value");
      } catch (Throwable var4) {
         p = Mono.error(var4);
      }

      return p;
   }

   private static <RESOURCE, T> MonoUsingWhen.MonoUsingWhenSubscriber<? super T, RESOURCE> prepareSubscriberForResource(
      RESOURCE resource,
      CoreSubscriber<? super T> actual,
      Function<? super RESOURCE, ? extends Publisher<?>> asyncComplete,
      BiFunction<? super RESOURCE, ? super Throwable, ? extends Publisher<?>> asyncError,
      @Nullable Function<? super RESOURCE, ? extends Publisher<?>> asyncCancel,
      @Nullable Operators.DeferredSubscription arbiter
   ) {
      return new MonoUsingWhen.MonoUsingWhenSubscriber<>(actual, resource, asyncComplete, asyncError, asyncCancel, arbiter);
   }

   static class MonoUsingWhenSubscriber<T, S> extends FluxUsingWhen.UsingWhenSubscriber<T, S> {
      T value;

      MonoUsingWhenSubscriber(
         CoreSubscriber<? super T> actual,
         S resource,
         Function<? super S, ? extends Publisher<?>> asyncComplete,
         BiFunction<? super S, ? super Throwable, ? extends Publisher<?>> asyncError,
         @Nullable Function<? super S, ? extends Publisher<?>> asyncCancel,
         @Nullable Operators.DeferredSubscription arbiter
      ) {
         super(actual, resource, asyncComplete, asyncError, asyncCancel, arbiter);
      }

      @Override
      public void onNext(T value) {
         this.value = value;
      }

      @Override
      public void deferredComplete() {
         this.error = Exceptions.TERMINATED;
         if (this.value != null) {
            this.actual.onNext(this.value);
         }

         this.actual.onComplete();
      }

      @Override
      public void deferredError(Throwable error) {
         Operators.onDiscard(this.value, this.actual.currentContext());
         this.error = error;
         this.actual.onError(error);
      }
   }

   static class ResourceSubscriber<S, T> extends Operators.DeferredSubscription implements InnerConsumer<S> {
      final CoreSubscriber<? super T> actual;
      final Function<? super S, ? extends Mono<? extends T>> resourceClosure;
      final Function<? super S, ? extends Publisher<?>> asyncComplete;
      final BiFunction<? super S, ? super Throwable, ? extends Publisher<?>> asyncError;
      @Nullable
      final Function<? super S, ? extends Publisher<?>> asyncCancel;
      final boolean isMonoSource;
      Subscription resourceSubscription;
      boolean resourceProvided;

      ResourceSubscriber(
         CoreSubscriber<? super T> actual,
         Function<? super S, ? extends Mono<? extends T>> resourceClosure,
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
      public Context currentContext() {
         return this.actual.currentContext();
      }

      @Override
      public void onNext(S resource) {
         if (this.resourceProvided) {
            Operators.onNextDropped(resource, this.actual.currentContext());
         } else {
            this.resourceProvided = true;
            Mono<? extends T> p = MonoUsingWhen.deriveMonoFromResource(resource, this.resourceClosure);
            p.subscribe(MonoUsingWhen.prepareSubscriberForResource(resource, this.actual, this.asyncComplete, this.asyncError, this.asyncCancel, this));
            if (!this.isMonoSource) {
               this.resourceSubscription.cancel();
            }

         }
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
}
