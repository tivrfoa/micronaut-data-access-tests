package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Consumer;
import java.util.function.Function;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class MonoUsing<T, S> extends Mono<T> implements Fuseable, SourceProducer<T> {
   final Callable<S> resourceSupplier;
   final Function<? super S, ? extends Mono<? extends T>> sourceFactory;
   final Consumer<? super S> resourceCleanup;
   final boolean eager;

   MonoUsing(Callable<S> resourceSupplier, Function<? super S, ? extends Mono<? extends T>> sourceFactory, Consumer<? super S> resourceCleanup, boolean eager) {
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
      } catch (Throwable var8) {
         Operators.error(actual, Operators.onOperatorError(var8, actual.currentContext()));
         return;
      }

      Mono<? extends T> p;
      try {
         p = (Mono)Objects.requireNonNull(this.sourceFactory.apply(resource), "The sourceFactory returned a null value");
      } catch (Throwable var7) {
         Throwable e = var7;

         try {
            this.resourceCleanup.accept(resource);
         } catch (Throwable var6) {
            e = Exceptions.addSuppressed(var6, Operators.onOperatorError(var7, actual.currentContext()));
         }

         Operators.error(actual, Operators.onOperatorError(e, actual.currentContext()));
         return;
      }

      if (p instanceof Fuseable) {
         p.subscribe(new MonoUsing.MonoUsingSubscriber<>(actual, this.resourceCleanup, resource, this.eager, true));
      } else {
         p.subscribe(new MonoUsing.MonoUsingSubscriber<>(actual, this.resourceCleanup, resource, this.eager, false));
      }

   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }

   static final class MonoUsingSubscriber<T, S> implements InnerOperator<T, T>, Fuseable.QueueSubscription<T> {
      final CoreSubscriber<? super T> actual;
      final Consumer<? super S> resourceCleanup;
      final S resource;
      final boolean eager;
      final boolean allowFusion;
      Subscription s;
      @Nullable
      Fuseable.QueueSubscription<T> qs;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<MonoUsing.MonoUsingSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         MonoUsing.MonoUsingSubscriber.class, "wip"
      );
      int mode;
      boolean valued;

      MonoUsingSubscriber(CoreSubscriber<? super T> actual, Consumer<? super S> resourceCleanup, S resource, boolean eager, boolean allowFusion) {
         this.actual = actual;
         this.resourceCleanup = resourceCleanup;
         this.resource = resource;
         this.eager = eager;
         this.allowFusion = allowFusion;
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
            if (s instanceof Fuseable.QueueSubscription) {
               this.qs = (Fuseable.QueueSubscription)s;
            }

            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.mode == 2) {
            this.actual.onNext((T)null);
         } else {
            this.valued = true;
            if (this.eager && WIP.compareAndSet(this, 0, 1)) {
               try {
                  this.resourceCleanup.accept(this.resource);
               } catch (Throwable var5) {
                  Context ctx = this.actual.currentContext();
                  this.actual.onError(Operators.onOperatorError(var5, ctx));
                  Operators.onDiscard(t, ctx);
                  return;
               }
            }

            this.actual.onNext(t);
            this.actual.onComplete();
            if (!this.eager && WIP.compareAndSet(this, 0, 1)) {
               try {
                  this.resourceCleanup.accept(this.resource);
               } catch (Throwable var4) {
                  Operators.onErrorDropped(var4, this.actual.currentContext());
               }
            }

         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.valued && this.mode != 2) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
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
      }

      @Override
      public void onComplete() {
         if (!this.valued || this.mode == 2) {
            if (this.eager && WIP.compareAndSet(this, 0, 1)) {
               try {
                  this.resourceCleanup.accept(this.resource);
               } catch (Throwable var3) {
                  this.actual.onError(Operators.onOperatorError(var3, this.actual.currentContext()));
                  return;
               }
            }

            this.actual.onComplete();
            if (!this.eager && WIP.compareAndSet(this, 0, 1)) {
               try {
                  this.resourceCleanup.accept(this.resource);
               } catch (Throwable var2) {
                  Operators.onErrorDropped(var2, this.actual.currentContext());
               }
            }

         }
      }

      public void clear() {
         if (this.qs != null) {
            this.qs.clear();
         }

      }

      public boolean isEmpty() {
         return this.qs == null || this.qs.isEmpty();
      }

      @Nullable
      public T poll() {
         if (this.mode != 0 && this.qs != null) {
            T v = (T)this.qs.poll();
            if (v != null) {
               this.valued = true;
               if (this.eager && WIP.compareAndSet(this, 0, 1)) {
                  try {
                     this.resourceCleanup.accept(this.resource);
                  } catch (Throwable var3) {
                     Operators.onDiscard(v, this.actual.currentContext());
                     throw var3;
                  }
               }
            } else if (this.mode == 1 && !this.eager && WIP.compareAndSet(this, 0, 1)) {
               try {
                  this.resourceCleanup.accept(this.resource);
               } catch (Throwable var4) {
                  if (!this.valued) {
                     throw var4;
                  }

                  Operators.onErrorDropped(var4, this.actual.currentContext());
               }
            }

            return v;
         } else {
            return null;
         }
      }

      @Override
      public int requestFusion(int requestedMode) {
         if (this.qs == null) {
            this.mode = 0;
            return 0;
         } else {
            int m = this.qs.requestFusion(requestedMode);
            this.mode = m;
            return m;
         }
      }

      public int size() {
         return this.qs == null ? 0 : this.qs.size();
      }
   }
}
