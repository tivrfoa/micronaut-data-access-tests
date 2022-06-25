package reactor.core.publisher;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Predicate;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class MonoCacheInvalidateIf<T> extends InternalMonoOperator<T, T> {
   static final MonoCacheInvalidateIf.State<?> EMPTY_STATE = new MonoCacheInvalidateIf.State<Object>() {
      @Nullable
      @Override
      public Object get() {
         return null;
      }

      @Override
      public void clear() {
      }
   };
   final Predicate<? super T> shouldInvalidatePredicate;
   volatile MonoCacheInvalidateIf.State<T> state;
   static final AtomicReferenceFieldUpdater<MonoCacheInvalidateIf, MonoCacheInvalidateIf.State> STATE = AtomicReferenceFieldUpdater.newUpdater(
      MonoCacheInvalidateIf.class, MonoCacheInvalidateIf.State.class, "state"
   );

   MonoCacheInvalidateIf(Mono<T> source, Predicate<? super T> invalidationPredicate) {
      super(source);
      this.shouldInvalidatePredicate = (Predicate)Objects.requireNonNull(invalidationPredicate, "invalidationPredicate");
      MonoCacheInvalidateIf.State<T> state = EMPTY_STATE;
      this.state = state;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      MonoCacheInvalidateIf.CacheMonoSubscriber<T> inner = new MonoCacheInvalidateIf.CacheMonoSubscriber<>(actual);

      MonoCacheInvalidateIf.State<T> state;
      while(true) {
         state = this.state;
         if (state != EMPTY_STATE && !(state instanceof MonoCacheInvalidateIf.CoordinatorSubscriber)) {
            T cached = state.get();

            try {
               boolean invalidated = this.shouldInvalidatePredicate.test(cached);
               if (!invalidated) {
                  break;
               }

               if (STATE.compareAndSet(this, state, EMPTY_STATE)) {
                  Operators.onDiscard(cached, actual.currentContext());
               }
            } catch (Throwable var6) {
               if (!STATE.compareAndSet(this, state, EMPTY_STATE)) {
                  break;
               }

               Operators.onDiscard(cached, actual.currentContext());
               Operators.error(actual, var6);
               return null;
            }
         } else {
            boolean connectToUpstream = false;
            MonoCacheInvalidateIf.CoordinatorSubscriber<T> coordinator;
            if (state == EMPTY_STATE) {
               coordinator = new MonoCacheInvalidateIf.CoordinatorSubscriber<>(this, this.source);
               if (!STATE.compareAndSet(this, EMPTY_STATE, coordinator)) {
                  continue;
               }

               connectToUpstream = true;
            } else {
               coordinator = (MonoCacheInvalidateIf.CoordinatorSubscriber)state;
            }

            if (coordinator.add(inner)) {
               if (inner.isCancelled()) {
                  coordinator.remove(inner);
               } else {
                  inner.coordinator = coordinator;
                  actual.onSubscribe(inner);
               }

               if (connectToUpstream) {
                  coordinator.delayedSubscribe();
               }

               return null;
            }
         }
      }

      actual.onSubscribe(inner);
      inner.complete(state.get());
      return null;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class CacheMonoSubscriber<T> extends Operators.MonoSubscriber<T, T> {
      MonoCacheInvalidateIf.CoordinatorSubscriber<T> coordinator;

      CacheMonoSubscriber(CoreSubscriber<? super T> actual) {
         super(actual);
      }

      @Override
      public void cancel() {
         super.cancel();
         MonoCacheInvalidateIf.CoordinatorSubscriber<T> coordinator = this.coordinator;
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
      final MonoCacheInvalidateIf<T> main;
      final Mono<? extends T> source;
      boolean done = false;
      volatile Subscription upstream;
      static final AtomicReferenceFieldUpdater<MonoCacheInvalidateIf.CoordinatorSubscriber, Subscription> UPSTREAM = AtomicReferenceFieldUpdater.newUpdater(
         MonoCacheInvalidateIf.CoordinatorSubscriber.class, Subscription.class, "upstream"
      );
      volatile MonoCacheInvalidateIf.CacheMonoSubscriber<T>[] subscribers;
      static final AtomicReferenceFieldUpdater<MonoCacheInvalidateIf.CoordinatorSubscriber, MonoCacheInvalidateIf.CacheMonoSubscriber[]> SUBSCRIBERS = AtomicReferenceFieldUpdater.newUpdater(
         MonoCacheInvalidateIf.CoordinatorSubscriber.class, MonoCacheInvalidateIf.CacheMonoSubscriber[].class, "subscribers"
      );
      private static final MonoCacheInvalidateIf.CacheMonoSubscriber[] COORDINATOR_DONE = new MonoCacheInvalidateIf.CacheMonoSubscriber[0];
      private static final MonoCacheInvalidateIf.CacheMonoSubscriber[] COORDINATOR_INIT = new MonoCacheInvalidateIf.CacheMonoSubscriber[0];

      CoordinatorSubscriber(MonoCacheInvalidateIf<T> main, Mono<? extends T> source) {
         this.main = main;
         this.source = source;
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

      final boolean add(MonoCacheInvalidateIf.CacheMonoSubscriber<T> toAdd) {
         MonoCacheInvalidateIf.CacheMonoSubscriber<T>[] a;
         MonoCacheInvalidateIf.CacheMonoSubscriber<T>[] b;
         do {
            a = this.subscribers;
            if (a == COORDINATOR_DONE) {
               return false;
            }

            int n = a.length;
            b = new MonoCacheInvalidateIf.CacheMonoSubscriber[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = toAdd;
         } while(!SUBSCRIBERS.compareAndSet(this, a, b));

         return true;
      }

      final void remove(MonoCacheInvalidateIf.CacheMonoSubscriber<T> toRemove) {
         while(true) {
            MonoCacheInvalidateIf.CacheMonoSubscriber<T>[] a = this.subscribers;
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

                  this.upstream.cancel();
                  MonoCacheInvalidateIf.STATE.compareAndSet(this.main, this, MonoCacheInvalidateIf.EMPTY_STATE);
                  return;
               }

               MonoCacheInvalidateIf.CacheMonoSubscriber<?>[] b = new MonoCacheInvalidateIf.CacheMonoSubscriber[n - 1];
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

      void delayedSubscribe() {
         Subscription old = (Subscription)UPSTREAM.getAndSet(this, null);
         if (old != null && old != Operators.cancelledSubscription()) {
            old.cancel();
         }

         this.source.subscribe(this);
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (UPSTREAM.compareAndSet(this, null, s)) {
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.main.state == this && !this.done) {
            this.done = true;
            MonoCacheInvalidateIf.State<T> valueState = new MonoCacheInvalidateIf.ValueState<>(t);
            if (MonoCacheInvalidateIf.STATE.compareAndSet(this.main, this, valueState)) {
               for(MonoCacheInvalidateIf.CacheMonoSubscriber<T> inner : (MonoCacheInvalidateIf.CacheMonoSubscriber[])SUBSCRIBERS.getAndSet(
                  this, COORDINATOR_DONE
               )) {
                  inner.complete(t);
               }
            }

         } else {
            Operators.onNextDropped(t, this.currentContext());
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.main.state == this && !this.done) {
            if (MonoCacheInvalidateIf.STATE.compareAndSet(this.main, this, MonoCacheInvalidateIf.EMPTY_STATE)) {
               for(MonoCacheInvalidateIf.CacheMonoSubscriber<T> inner : (MonoCacheInvalidateIf.CacheMonoSubscriber[])SUBSCRIBERS.getAndSet(
                  this, COORDINATOR_DONE
               )) {
                  inner.onError(t);
               }
            }

         } else {
            Operators.onErrorDropped(t, this.currentContext());
         }
      }

      @Override
      public void onComplete() {
         if (this.done) {
            this.done = false;
         } else {
            if (MonoCacheInvalidateIf.STATE.compareAndSet(this.main, this, MonoCacheInvalidateIf.EMPTY_STATE)) {
               for(MonoCacheInvalidateIf.CacheMonoSubscriber<T> inner : (MonoCacheInvalidateIf.CacheMonoSubscriber[])SUBSCRIBERS.getAndSet(
                  this, COORDINATOR_DONE
               )) {
                  inner.onError(new NoSuchElementException("cacheInvalidateWhen expects a value, source completed empty"));
               }
            }

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

   interface State<T> {
      @Nullable
      T get();

      void clear();
   }

   static final class ValueState<T> implements MonoCacheInvalidateIf.State<T> {
      @Nullable
      T value;

      ValueState(T value) {
         this.value = value;
      }

      @Nullable
      @Override
      public T get() {
         return this.value;
      }

      @Override
      public void clear() {
         this.value = null;
      }
   }
}
