package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Consumer;
import java.util.function.Function;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxUsing<T, S> extends Flux<T> implements Fuseable, SourceProducer<T> {
   final Callable<S> resourceSupplier;
   final Function<? super S, ? extends Publisher<? extends T>> sourceFactory;
   final Consumer<? super S> resourceCleanup;
   final boolean eager;

   FluxUsing(
      Callable<S> resourceSupplier, Function<? super S, ? extends Publisher<? extends T>> sourceFactory, Consumer<? super S> resourceCleanup, boolean eager
   ) {
      this.resourceSupplier = (Callable)Objects.requireNonNull(resourceSupplier, "resourceSupplier");
      this.sourceFactory = (Function)Objects.requireNonNull(sourceFactory, "sourceFactory");
      this.resourceCleanup = (Consumer)Objects.requireNonNull(resourceCleanup, "resourceCleanup");
      this.eager = eager;
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      S resource;
      try {
         resource = (S)this.resourceSupplier.call();
      } catch (Throwable var9) {
         Operators.error(actual, Operators.onOperatorError(var9, actual.currentContext()));
         return;
      }

      Publisher<? extends T> p;
      try {
         p = (Publisher)Objects.requireNonNull(this.sourceFactory.apply(resource), "The sourceFactory returned a null value");
      } catch (Throwable var8) {
         Throwable _e = Operators.onOperatorError(var8, actual.currentContext());

         try {
            this.resourceCleanup.accept(resource);
         } catch (Throwable var7) {
            _e = Exceptions.addSuppressed(var7, _e);
         }

         Operators.error(actual, _e);
         return;
      }

      if (p instanceof Fuseable) {
         from(p).subscribe(new FluxUsing.UsingFuseableSubscriber<>(actual, this.resourceCleanup, resource, this.eager));
      } else if (actual instanceof Fuseable.ConditionalSubscriber) {
         from(p)
            .subscribe(
               new FluxUsing.UsingConditionalSubscriber<>((Fuseable.ConditionalSubscriber<? super T>)actual, this.resourceCleanup, resource, this.eager)
            );
      } else {
         from(p).subscribe(new FluxUsing.UsingSubscriber<>(actual, this.resourceCleanup, resource, this.eager));
      }

   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }

   static final class UsingConditionalSubscriber<T, S> implements Fuseable.ConditionalSubscriber<T>, InnerOperator<T, T>, Fuseable.QueueSubscription<T> {
      final Fuseable.ConditionalSubscriber<? super T> actual;
      final Consumer<? super S> resourceCleanup;
      final S resource;
      final boolean eager;
      Subscription s;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxUsing.UsingConditionalSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxUsing.UsingConditionalSubscriber.class, "wip"
      );

      UsingConditionalSubscriber(Fuseable.ConditionalSubscriber<? super T> actual, Consumer<? super S> resourceCleanup, S resource, boolean eager) {
         this.actual = actual;
         this.resourceCleanup = resourceCleanup;
         this.resource = resource;
         this.eager = eager;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED || key == Scannable.Attr.CANCELLED) {
            return this.wip == 1;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public void request(long n) {
         this.s.request(n);
      }

      @Override
      public void cancel() {
         if (WIP.compareAndSet(this, 0, 1)) {
            this.s.cancel();
            this.cleanup();
         }

      }

      void cleanup() {
         try {
            this.resourceCleanup.accept(this.resource);
         } catch (Throwable var2) {
            Operators.onErrorDropped(var2, this.actual.currentContext());
         }

      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         this.actual.onNext(t);
      }

      @Override
      public boolean tryOnNext(T t) {
         return this.actual.tryOnNext(t);
      }

      @Override
      public void onError(Throwable t) {
         if (this.eager && WIP.compareAndSet(this, 0, 1)) {
            try {
               this.resourceCleanup.accept(this.resource);
            } catch (Throwable var4) {
               Throwable _e = Operators.onOperatorError(var4, this.actual.currentContext());
               t = Exceptions.addSuppressed(_e, t);
            }
         }

         this.actual.onError(t);
         if (!this.eager && WIP.compareAndSet(this, 0, 1)) {
            this.cleanup();
         }

      }

      @Override
      public void onComplete() {
         if (this.eager && WIP.compareAndSet(this, 0, 1)) {
            try {
               this.resourceCleanup.accept(this.resource);
            } catch (Throwable var2) {
               this.actual.onError(Operators.onOperatorError(var2, this.actual.currentContext()));
               return;
            }
         }

         this.actual.onComplete();
         if (!this.eager && WIP.compareAndSet(this, 0, 1)) {
            this.cleanup();
         }

      }

      @Override
      public int requestFusion(int requestedMode) {
         return 0;
      }

      public void clear() {
      }

      public boolean isEmpty() {
         return true;
      }

      @Nullable
      public T poll() {
         return null;
      }

      public int size() {
         return 0;
      }
   }

   static final class UsingFuseableSubscriber<T, S> implements InnerOperator<T, T>, Fuseable.QueueSubscription<T> {
      final CoreSubscriber<? super T> actual;
      final Consumer<? super S> resourceCleanup;
      final S resource;
      final boolean eager;
      Fuseable.QueueSubscription<T> s;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxUsing.UsingFuseableSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxUsing.UsingFuseableSubscriber.class, "wip"
      );
      int mode;

      UsingFuseableSubscriber(CoreSubscriber<? super T> actual, Consumer<? super S> resourceCleanup, S resource, boolean eager) {
         this.actual = actual;
         this.resourceCleanup = resourceCleanup;
         this.resource = resource;
         this.eager = eager;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED || key == Scannable.Attr.CANCELLED) {
            return this.wip == 1;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public void request(long n) {
         this.s.request(n);
      }

      @Override
      public void cancel() {
         if (WIP.compareAndSet(this, 0, 1)) {
            this.s.cancel();
            this.cleanup();
         }

      }

      void cleanup() {
         try {
            this.resourceCleanup.accept(this.resource);
         } catch (Throwable var2) {
            Operators.onErrorDropped(var2, this.actual.currentContext());
         }

      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = (Fuseable.QueueSubscription)s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         if (this.eager && WIP.compareAndSet(this, 0, 1)) {
            try {
               this.resourceCleanup.accept(this.resource);
            } catch (Throwable var4) {
               Throwable _e = Operators.onOperatorError(var4, this.actual.currentContext());
               t = Exceptions.addSuppressed(_e, t);
            }
         }

         this.actual.onError(t);
         if (!this.eager && WIP.compareAndSet(this, 0, 1)) {
            this.cleanup();
         }

      }

      @Override
      public void onComplete() {
         if (this.eager && WIP.compareAndSet(this, 0, 1)) {
            try {
               this.resourceCleanup.accept(this.resource);
            } catch (Throwable var2) {
               this.actual.onError(Operators.onOperatorError(var2, this.actual.currentContext()));
               return;
            }
         }

         this.actual.onComplete();
         if (!this.eager && WIP.compareAndSet(this, 0, 1)) {
            this.cleanup();
         }

      }

      public void clear() {
         this.s.clear();
      }

      public boolean isEmpty() {
         return this.s.isEmpty();
      }

      @Nullable
      public T poll() {
         T v = (T)this.s.poll();
         if (v == null && this.mode == 1 && WIP.compareAndSet(this, 0, 1)) {
            this.resourceCleanup.accept(this.resource);
         }

         return v;
      }

      @Override
      public int requestFusion(int requestedMode) {
         int m = this.s.requestFusion(requestedMode);
         this.mode = m;
         return m;
      }

      public int size() {
         return this.s.size();
      }
   }

   static final class UsingSubscriber<T, S> implements InnerOperator<T, T>, Fuseable.QueueSubscription<T> {
      final CoreSubscriber<? super T> actual;
      final Consumer<? super S> resourceCleanup;
      final S resource;
      final boolean eager;
      Subscription s;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxUsing.UsingSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(FluxUsing.UsingSubscriber.class, "wip");

      UsingSubscriber(CoreSubscriber<? super T> actual, Consumer<? super S> resourceCleanup, S resource, boolean eager) {
         this.actual = actual;
         this.resourceCleanup = resourceCleanup;
         this.resource = resource;
         this.eager = eager;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED || key == Scannable.Attr.CANCELLED) {
            return this.wip == 1;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public void request(long n) {
         this.s.request(n);
      }

      @Override
      public void cancel() {
         if (WIP.compareAndSet(this, 0, 1)) {
            this.s.cancel();
            this.cleanup();
         }

      }

      void cleanup() {
         try {
            this.resourceCleanup.accept(this.resource);
         } catch (Throwable var2) {
            Operators.onErrorDropped(var2, this.actual.currentContext());
         }

      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         if (this.eager && WIP.compareAndSet(this, 0, 1)) {
            try {
               this.resourceCleanup.accept(this.resource);
            } catch (Throwable var4) {
               Throwable _e = Operators.onOperatorError(var4, this.actual.currentContext());
               t = Exceptions.addSuppressed(_e, t);
            }
         }

         this.actual.onError(t);
         if (!this.eager && WIP.compareAndSet(this, 0, 1)) {
            this.cleanup();
         }

      }

      @Override
      public void onComplete() {
         if (this.eager && WIP.compareAndSet(this, 0, 1)) {
            try {
               this.resourceCleanup.accept(this.resource);
            } catch (Throwable var2) {
               this.actual.onError(Operators.onOperatorError(var2, this.actual.currentContext()));
               return;
            }
         }

         this.actual.onComplete();
         if (!this.eager && WIP.compareAndSet(this, 0, 1)) {
            this.cleanup();
         }

      }

      @Override
      public int requestFusion(int requestedMode) {
         return 0;
      }

      public void clear() {
      }

      public boolean isEmpty() {
         return true;
      }

      @Nullable
      public T poll() {
         return null;
      }

      public int size() {
         return 0;
      }
   }
}
