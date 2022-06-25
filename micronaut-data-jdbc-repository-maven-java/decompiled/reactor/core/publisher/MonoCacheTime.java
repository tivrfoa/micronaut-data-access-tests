package reactor.core.publisher;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.function.Supplier;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

class MonoCacheTime<T> extends InternalMonoOperator<T, T> implements Runnable {
   private static final Duration DURATION_INFINITE = Duration.ofMillis(Long.MAX_VALUE);
   private static final Logger LOGGER = Loggers.getLogger(MonoCacheTime.class);
   final Function<? super Signal<T>, Duration> ttlGenerator;
   final Scheduler clock;
   volatile Signal<T> state;
   static final AtomicReferenceFieldUpdater<MonoCacheTime, Signal> STATE = AtomicReferenceFieldUpdater.newUpdater(MonoCacheTime.class, Signal.class, "state");
   static final Signal<?> EMPTY = new ImmutableSignal(Context.empty(), SignalType.ON_NEXT, (T)null, null, null);

   MonoCacheTime(Mono<? extends T> source, Duration ttl, Scheduler clock) {
      super(source);
      Objects.requireNonNull(ttl, "ttl must not be null");
      Objects.requireNonNull(clock, "clock must not be null");
      this.ttlGenerator = ignoredSignal -> ttl;
      this.clock = clock;
      Signal<T> state = EMPTY;
      this.state = state;
   }

   MonoCacheTime(Mono<? extends T> source) {
      this(source, (Function<? super Signal<T>, Duration>)(sig -> DURATION_INFINITE), Schedulers.immediate());
   }

   MonoCacheTime(Mono<? extends T> source, Function<? super Signal<T>, Duration> ttlGenerator, Scheduler clock) {
      super(source);
      this.ttlGenerator = ttlGenerator;
      this.clock = clock;
      Signal<T> state = EMPTY;
      this.state = state;
   }

   MonoCacheTime(
      Mono<? extends T> source,
      Function<? super T, Duration> valueTtlGenerator,
      Function<Throwable, Duration> errorTtlGenerator,
      Supplier<Duration> emptyTtlGenerator,
      Scheduler clock
   ) {
      super(source);
      Objects.requireNonNull(valueTtlGenerator, "valueTtlGenerator must not be null");
      Objects.requireNonNull(errorTtlGenerator, "errorTtlGenerator must not be null");
      Objects.requireNonNull(emptyTtlGenerator, "emptyTtlGenerator must not be null");
      Objects.requireNonNull(clock, "clock must not be null");
      this.ttlGenerator = sig -> {
         if (sig.isOnNext()) {
            return (Duration)valueTtlGenerator.apply(sig.get());
         } else {
            return sig.isOnError() ? (Duration)errorTtlGenerator.apply(sig.getThrowable()) : (Duration)emptyTtlGenerator.get();
         }
      };
      this.clock = clock;
      Signal<T> emptyState = EMPTY;
      this.state = emptyState;
   }

   public void run() {
      LOGGER.debug("expired {}", this.state);
      Signal<T> emptyState = EMPTY;
      this.state = emptyState;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      MonoCacheTime.CacheMonoSubscriber<T> inner = new MonoCacheTime.CacheMonoSubscriber<>(actual);
      actual.onSubscribe(inner);

      while(true) {
         Signal<T> state = this.state;
         if (state != EMPTY && !(state instanceof MonoCacheTime.CoordinatorSubscriber)) {
            if (state.isOnNext()) {
               inner.complete(state.get());
            } else if (state.isOnComplete()) {
               inner.onComplete();
            } else {
               inner.onError(state.getThrowable());
            }
            break;
         }

         boolean subscribe = false;
         MonoCacheTime.CoordinatorSubscriber<T> coordinator;
         if (state == EMPTY) {
            coordinator = new MonoCacheTime.CoordinatorSubscriber<>(this);
            if (!STATE.compareAndSet(this, EMPTY, coordinator)) {
               continue;
            }

            subscribe = true;
         } else {
            coordinator = (MonoCacheTime.CoordinatorSubscriber)state;
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
            break;
         }
      }

      return null;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class CacheMonoSubscriber<T> extends Operators.MonoSubscriber<T, T> {
      MonoCacheTime.CoordinatorSubscriber<T> coordinator;

      CacheMonoSubscriber(CoreSubscriber<? super T> actual) {
         super(actual);
      }

      @Override
      public void cancel() {
         super.cancel();
         MonoCacheTime.CoordinatorSubscriber<T> coordinator = this.coordinator;
         if (coordinator != null) {
            coordinator.remove(this);
         }

      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
      }
   }

   static final class CoordinatorSubscriber<T> implements InnerConsumer<T>, Signal<T> {
      final MonoCacheTime<T> main;
      volatile Subscription subscription;
      static final AtomicReferenceFieldUpdater<MonoCacheTime.CoordinatorSubscriber, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         MonoCacheTime.CoordinatorSubscriber.class, Subscription.class, "subscription"
      );
      volatile Operators.MonoSubscriber<T, T>[] subscribers;
      static final AtomicReferenceFieldUpdater<MonoCacheTime.CoordinatorSubscriber, Operators.MonoSubscriber[]> SUBSCRIBERS = AtomicReferenceFieldUpdater.newUpdater(
         MonoCacheTime.CoordinatorSubscriber.class, Operators.MonoSubscriber[].class, "subscribers"
      );
      private static final Operators.MonoSubscriber[] TERMINATED = new Operators.MonoSubscriber[0];
      private static final Operators.MonoSubscriber[] EMPTY = new Operators.MonoSubscriber[0];

      CoordinatorSubscriber(MonoCacheTime<T> main) {
         this.main = main;
         this.subscribers = EMPTY;
      }

      @Override
      public Throwable getThrowable() {
         throw new UnsupportedOperationException("illegal signal use");
      }

      @Override
      public Subscription getSubscription() {
         throw new UnsupportedOperationException("illegal signal use");
      }

      @Override
      public T get() {
         throw new UnsupportedOperationException("illegal signal use");
      }

      @Override
      public SignalType getType() {
         throw new UnsupportedOperationException("illegal signal use");
      }

      @Override
      public ContextView getContextView() {
         throw new UnsupportedOperationException("illegal signal use: getContextView");
      }

      final boolean add(Operators.MonoSubscriber<T, T> toAdd) {
         Operators.MonoSubscriber<T, T>[] a;
         Operators.MonoSubscriber<T, T>[] b;
         do {
            a = this.subscribers;
            if (a == TERMINATED) {
               return false;
            }

            int n = a.length;
            b = new Operators.MonoSubscriber[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = toAdd;
         } while(!SUBSCRIBERS.compareAndSet(this, a, b));

         return true;
      }

      final void remove(Operators.MonoSubscriber<T, T> toRemove) {
         while(true) {
            Operators.MonoSubscriber<T, T>[] a = this.subscribers;
            if (a != TERMINATED && a != EMPTY) {
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

               Operators.MonoSubscriber<?, ?>[] b;
               if (n == 1) {
                  b = EMPTY;
               } else {
                  b = new Operators.MonoSubscriber[n - 1];
                  System.arraycopy(a, 0, b, 0, j);
                  System.arraycopy(a, j + 1, b, j, n - j - 1);
               }

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

      private void signalCached(Signal<T> signal) {
         Signal<T> signalToPropagate = signal;
         if (MonoCacheTime.STATE.compareAndSet(this.main, this, signal)) {
            Duration ttl = null;

            try {
               ttl = (Duration)this.main.ttlGenerator.apply(signal);
            } catch (Throwable var7) {
               signalToPropagate = Signal.error(var7);
               MonoCacheTime.STATE.set(this.main, signalToPropagate);
               if (signal.isOnError()) {
                  Exceptions.addSuppressed(var7, signal.getThrowable());
               }
            }

            if (ttl != null) {
               if (ttl.isZero()) {
                  this.main.run();
               } else if (!ttl.equals(MonoCacheTime.DURATION_INFINITE)) {
                  this.main.clock.schedule(this.main, ttl.toNanos(), TimeUnit.NANOSECONDS);
               }
            } else {
               if (signal.isOnNext()) {
                  Operators.onNextDropped(signal.get(), this.currentContext());
               }

               this.main.run();
            }
         }

         for(Operators.MonoSubscriber<T, T> inner : (Operators.MonoSubscriber[])SUBSCRIBERS.getAndSet(this, TERMINATED)) {
            if (signalToPropagate.isOnNext()) {
               inner.complete(signalToPropagate.get());
            } else if (signalToPropagate.isOnError()) {
               inner.onError(signalToPropagate.getThrowable());
            } else {
               inner.onComplete();
            }
         }

      }

      @Override
      public void onNext(T t) {
         if (this.main.state != this) {
            Operators.onNextDroppedMulticast(t, this.subscribers);
         } else {
            this.signalCached(Signal.next(t));
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.main.state != this) {
            Operators.onErrorDroppedMulticast(t, this.subscribers);
         } else {
            this.signalCached(Signal.error(t));
         }
      }

      @Override
      public void onComplete() {
         this.signalCached(Signal.complete());
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
}
