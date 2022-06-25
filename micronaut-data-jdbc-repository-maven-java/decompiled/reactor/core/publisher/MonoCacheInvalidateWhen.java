package reactor.core.publisher;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;
import java.util.function.Function;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class MonoCacheInvalidateWhen<T> extends InternalMonoOperator<T, T> {
   private static final Logger LOGGER = Loggers.getLogger(MonoCacheInvalidateWhen.class);
   final Function<? super T, Mono<Void>> invalidationTriggerGenerator;
   @Nullable
   final Consumer<? super T> invalidateHandler;
   volatile MonoCacheInvalidateIf.State<T> state;
   static final AtomicReferenceFieldUpdater<MonoCacheInvalidateWhen, MonoCacheInvalidateIf.State> STATE = AtomicReferenceFieldUpdater.newUpdater(
      MonoCacheInvalidateWhen.class, MonoCacheInvalidateIf.State.class, "state"
   );

   MonoCacheInvalidateWhen(Mono<T> source, Function<? super T, Mono<Void>> invalidationTriggerGenerator, @Nullable Consumer<? super T> invalidateHandler) {
      super(source);
      this.invalidationTriggerGenerator = (Function)Objects.requireNonNull(invalidationTriggerGenerator, "invalidationTriggerGenerator");
      this.invalidateHandler = invalidateHandler;
      MonoCacheInvalidateIf.State<T> state = MonoCacheInvalidateIf.EMPTY_STATE;
      this.state = state;
   }

   boolean compareAndInvalidate(MonoCacheInvalidateIf.State<T> expected) {
      if (STATE.compareAndSet(this, expected, MonoCacheInvalidateIf.EMPTY_STATE)) {
         if (expected instanceof MonoCacheInvalidateIf.ValueState) {
            LOGGER.trace("invalidated {}", expected.get());
            this.safeInvalidateHandler(expected.get());
         }

         return true;
      } else {
         return false;
      }
   }

   void invalidate() {
      MonoCacheInvalidateIf.State<T> oldState = (MonoCacheInvalidateIf.State)STATE.getAndSet(this, MonoCacheInvalidateIf.EMPTY_STATE);
      if (oldState instanceof MonoCacheInvalidateIf.ValueState) {
         LOGGER.trace("invalidated {}", oldState.get());
         this.safeInvalidateHandler(oldState.get());
      }

   }

   void safeInvalidateHandler(@Nullable T value) {
      if (value != null && this.invalidateHandler != null) {
         try {
            this.invalidateHandler.accept(value);
         } catch (Throwable var3) {
            LOGGER.warnOrDebug(isVerbose -> isVerbose ? "Failed to apply invalidate handler on value " + value : "Failed to apply invalidate handler", var3);
         }
      }

   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      MonoCacheInvalidateWhen.CacheMonoSubscriber<T> inner = new MonoCacheInvalidateWhen.CacheMonoSubscriber<>(actual);
      actual.onSubscribe(inner);

      while(true) {
         boolean subscribe;
         MonoCacheInvalidateWhen.CoordinatorSubscriber<T> coordinator;
         while(true) {
            MonoCacheInvalidateIf.State<T> state = this.state;
            if (state != MonoCacheInvalidateIf.EMPTY_STATE && !(state instanceof MonoCacheInvalidateWhen.CoordinatorSubscriber)) {
               inner.complete(state.get());
               return null;
            }

            subscribe = false;
            if (state == MonoCacheInvalidateIf.EMPTY_STATE) {
               coordinator = new MonoCacheInvalidateWhen.CoordinatorSubscriber<>(this);
               if (!STATE.compareAndSet(this, MonoCacheInvalidateIf.EMPTY_STATE, coordinator)) {
                  continue;
               }

               subscribe = true;
               break;
            }

            coordinator = (MonoCacheInvalidateWhen.CoordinatorSubscriber)state;
            break;
         }

         if (coordinator.add(inner)) {
            if (inner.isCancelled()) {
               coordinator.remove(inner);
            } else {
               inner.coordinator = coordinator;
            }

            if (subscribe) {
               this.source.subscribe(coordinator);
            }

            return null;
         }
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class CacheMonoSubscriber<T> extends Operators.MonoSubscriber<T, T> {
      MonoCacheInvalidateWhen.CoordinatorSubscriber<T> coordinator;

      CacheMonoSubscriber(CoreSubscriber<? super T> actual) {
         super(actual);
      }

      @Override
      public void cancel() {
         super.cancel();
         MonoCacheInvalidateWhen.CoordinatorSubscriber<T> coordinator = this.coordinator;
         if (coordinator != null) {
            coordinator.remove(this);
         }

      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.coordinator.main;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         }
      }
   }

   static final class CoordinatorSubscriber<T> implements InnerConsumer<T>, MonoCacheInvalidateIf.State<T> {
      final MonoCacheInvalidateWhen<T> main;
      Subscription subscription;
      volatile MonoCacheInvalidateWhen.CacheMonoSubscriber<T>[] subscribers;
      static final AtomicReferenceFieldUpdater<MonoCacheInvalidateWhen.CoordinatorSubscriber, MonoCacheInvalidateWhen.CacheMonoSubscriber[]> SUBSCRIBERS = AtomicReferenceFieldUpdater.newUpdater(
         MonoCacheInvalidateWhen.CoordinatorSubscriber.class, MonoCacheInvalidateWhen.CacheMonoSubscriber[].class, "subscribers"
      );
      private static final MonoCacheInvalidateWhen.CacheMonoSubscriber[] COORDINATOR_DONE = new MonoCacheInvalidateWhen.CacheMonoSubscriber[0];
      private static final MonoCacheInvalidateWhen.CacheMonoSubscriber[] COORDINATOR_INIT = new MonoCacheInvalidateWhen.CacheMonoSubscriber[0];

      CoordinatorSubscriber(MonoCacheInvalidateWhen<T> main) {
         this.main = main;
         this.subscribers = COORDINATOR_INIT;
      }

      @Nullable
      @Override
      public T get() {
         throw new UnsupportedOperationException("coordinator State#get shouldn't be used");
      }

      @Override
      public void clear() {
      }

      final boolean add(MonoCacheInvalidateWhen.CacheMonoSubscriber<T> toAdd) {
         MonoCacheInvalidateWhen.CacheMonoSubscriber<T>[] a;
         MonoCacheInvalidateWhen.CacheMonoSubscriber<T>[] b;
         do {
            a = this.subscribers;
            if (a == COORDINATOR_DONE) {
               return false;
            }

            int n = a.length;
            b = new MonoCacheInvalidateWhen.CacheMonoSubscriber[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = toAdd;
         } while(!SUBSCRIBERS.compareAndSet(this, a, b));

         return true;
      }

      final void remove(MonoCacheInvalidateWhen.CacheMonoSubscriber<T> toRemove) {
         while(true) {
            MonoCacheInvalidateWhen.CacheMonoSubscriber<T>[] a = this.subscribers;
            if (a != COORDINATOR_DONE && a != COORDINATOR_INIT) {
               int n = a.length;
               int j = -1;

               for(int i = 0; i < n; ++i) {
                  if (a[i] == toRemove) {
                     j = i;
                     break;
                  }
               }

               if (j < 0) {
                  return;
               }

               if (n == 1) {
                  if (!SUBSCRIBERS.compareAndSet(this, a, COORDINATOR_DONE)) {
                     continue;
                  }

                  if (this.main.compareAndInvalidate(this)) {
                     this.subscription.cancel();
                  }

                  return;
               }

               MonoCacheInvalidateWhen.CacheMonoSubscriber<?>[] b = new MonoCacheInvalidateWhen.CacheMonoSubscriber[n - 1];
               System.arraycopy(a, 0, b, 0, j);
               System.arraycopy(a, j + 1, b, j, n - j - 1);
               if (!SUBSCRIBERS.compareAndSet(this, a, b)) {
                  continue;
               }

               return;
            }

            return;
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.subscription, s)) {
            this.subscription = s;
            s.request(Long.MAX_VALUE);
         }

      }

      boolean cacheLoadFailure(MonoCacheInvalidateIf.State<T> expected, Throwable failure) {
         if (!MonoCacheInvalidateWhen.STATE.compareAndSet(this.main, expected, MonoCacheInvalidateIf.EMPTY_STATE)) {
            return false;
         } else {
            for(MonoCacheInvalidateWhen.CacheMonoSubscriber<T> inner : (MonoCacheInvalidateWhen.CacheMonoSubscriber[])SUBSCRIBERS.getAndSet(
               this, COORDINATOR_DONE
            )) {
               inner.onError(failure);
            }

            return true;
         }
      }

      void cacheLoad(T value) {
         MonoCacheInvalidateIf.State<T> valueState = new MonoCacheInvalidateIf.ValueState<>(value);
         if (MonoCacheInvalidateWhen.STATE.compareAndSet(this.main, this, valueState)) {
            Mono<Void> invalidateTrigger = null;

            try {
               invalidateTrigger = (Mono)Objects.requireNonNull(
                  this.main.invalidationTriggerGenerator.apply(value), "invalidationTriggerGenerator produced a null trigger"
               );
            } catch (Throwable var8) {
               if (this.cacheLoadFailure(valueState, var8)) {
                  this.main.safeInvalidateHandler(value);
               }

               return;
            }

            for(MonoCacheInvalidateWhen.CacheMonoSubscriber<T> inner : (MonoCacheInvalidateWhen.CacheMonoSubscriber[])SUBSCRIBERS.getAndSet(
               this, COORDINATOR_DONE
            )) {
               inner.complete(value);
            }

            invalidateTrigger.subscribe(new MonoCacheInvalidateWhen.TriggerSubscriber(this.main));
         }

      }

      @Override
      public void onNext(T t) {
         if (this.main.state != this) {
            Operators.onNextDroppedMulticast(t, this.subscribers);
         } else {
            this.cacheLoad(t);
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.main.state != this) {
            Operators.onErrorDroppedMulticast(t, this.subscribers);
         } else {
            this.cacheLoadFailure(this, t);
         }
      }

      @Override
      public void onComplete() {
         if (this.main.state == this) {
            this.cacheLoadFailure(this, new NoSuchElementException("cacheInvalidateWhen expects a value, source completed empty"));
         }

      }

      @Override
      public Context currentContext() {
         return Operators.multiSubscribersContext(this.subscribers);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   static final class TriggerSubscriber implements InnerConsumer<Void> {
      final MonoCacheInvalidateWhen<?> main;

      TriggerSubscriber(MonoCacheInvalidateWhen<?> main) {
         this.main = main;
      }

      @Override
      public void onSubscribe(Subscription s) {
         s.request(1L);
      }

      public void onNext(Void unused) {
      }

      @Override
      public void onError(Throwable t) {
         MonoCacheInvalidateWhen.LOGGER.debug("Invalidation triggered by onError(" + t + ")");
         this.main.invalidate();
      }

      @Override
      public void onComplete() {
         this.main.invalidate();
      }

      @Override
      public Context currentContext() {
         return Context.empty();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.main;
         } else if (key != Scannable.Attr.TERMINATED) {
            if (key == Scannable.Attr.RUN_STYLE) {
               return Scannable.Attr.RunStyle.SYNC;
            } else {
               return key == Scannable.Attr.PREFETCH ? 1 : null;
            }
         } else {
            return this.main.state == MonoCacheInvalidateIf.EMPTY_STATE || this.main.state instanceof MonoCacheInvalidateWhen.CoordinatorSubscriber;
         }
      }
   }
}
