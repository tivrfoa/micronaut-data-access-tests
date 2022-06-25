package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class MonoDelayUntil<T> extends Mono<T> implements Scannable, OptimizableOperator<T, T> {
   final Mono<T> source;
   Function<? super T, ? extends Publisher<?>>[] otherGenerators;
   @Nullable
   final OptimizableOperator<?, T> optimizableOperator;

   MonoDelayUntil(Mono<T> monoSource, Function<? super T, ? extends Publisher<?>> triggerGenerator) {
      this.source = (Mono)Objects.requireNonNull(monoSource, "monoSource");
      this.otherGenerators = new Function[]{(Function)Objects.requireNonNull(triggerGenerator, "triggerGenerator")};
      this.optimizableOperator = this.source instanceof OptimizableOperator ? (OptimizableOperator)this.source : null;
   }

   MonoDelayUntil(Mono<T> monoSource, Function<? super T, ? extends Publisher<?>>[] triggerGenerators) {
      this.source = (Mono)Objects.requireNonNull(monoSource, "monoSource");
      this.otherGenerators = triggerGenerators;
      if (this.source instanceof OptimizableOperator) {
         OptimizableOperator<?, T> optimSource = (OptimizableOperator)this.source;
         this.optimizableOperator = optimSource;
      } else {
         this.optimizableOperator = null;
      }

   }

   MonoDelayUntil<T> copyWithNewTriggerGenerator(boolean delayError, Function<? super T, ? extends Publisher<?>> triggerGenerator) {
      Objects.requireNonNull(triggerGenerator, "triggerGenerator");
      Function<? super T, ? extends Publisher<?>>[] oldTriggers = this.otherGenerators;
      Function<? super T, ? extends Publisher<?>>[] newTriggers = new Function[oldTriggers.length + 1];
      System.arraycopy(oldTriggers, 0, newTriggers, 0, oldTriggers.length);
      newTriggers[oldTriggers.length] = triggerGenerator;
      return new MonoDelayUntil<>(this.source, newTriggers);
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      try {
         this.source.subscribe(this.subscribeOrReturn(actual));
      } catch (Throwable var3) {
         Operators.error(actual, Operators.onOperatorError(var3, actual.currentContext()));
      }
   }

   @Override
   public final CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) throws Throwable {
      MonoDelayUntil.DelayUntilCoordinator<T> parent = new MonoDelayUntil.DelayUntilCoordinator<>(actual, this.otherGenerators);
      actual.onSubscribe(parent);
      return parent;
   }

   @Override
   public final CorePublisher<? extends T> source() {
      return this.source;
   }

   @Override
   public final OptimizableOperator<?, ? extends T> nextOptimizableSource() {
      return this.optimizableOperator;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }

   static boolean isTerminated(int state) {
      return state == Integer.MIN_VALUE;
   }

   static boolean hasValue(int state) {
      return (state & 8) == 8;
   }

   static boolean hasInner(int state) {
      return (state & 2) == 2;
   }

   static boolean hasRequest(int state) {
      return (state & 4) == 4;
   }

   static boolean hasSubscription(int state) {
      return (state & 1) == 1;
   }

   static final class DelayUntilCoordinator<T> implements InnerOperator<T, T> {
      final Function<? super T, ? extends Publisher<?>>[] otherGenerators;
      final CoreSubscriber<? super T> actual;
      int index;
      T value;
      boolean done;
      Subscription s;
      MonoDelayUntil.DelayUntilTrigger<?> triggerSubscriber;
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<MonoDelayUntil.DelayUntilCoordinator, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         MonoDelayUntil.DelayUntilCoordinator.class, Throwable.class, "error"
      );
      volatile int state;
      static final AtomicIntegerFieldUpdater<MonoDelayUntil.DelayUntilCoordinator> STATE = AtomicIntegerFieldUpdater.newUpdater(
         MonoDelayUntil.DelayUntilCoordinator.class, "state"
      );
      static final int HAS_SUBSCRIPTION = 1;
      static final int HAS_INNER = 2;
      static final int HAS_REQUEST = 4;
      static final int HAS_VALUE = 8;
      static final int TERMINATED = Integer.MIN_VALUE;

      DelayUntilCoordinator(CoreSubscriber<? super T> subscriber, Function<? super T, ? extends Publisher<?>>[] otherGenerators) {
         this.actual = subscriber;
         this.otherGenerators = otherGenerators;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            int previousState = this.markHasSubscription();
            if (MonoDelayUntil.isTerminated(previousState)) {
               s.cancel();
               return;
            }

            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onDiscard(t, this.actual.currentContext());
         } else {
            this.value = t;
            this.subscribeNextTrigger();
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            if (this.value == null) {
               this.actual.onError(t);
            } else if (!Exceptions.addThrowable(ERROR, this, t)) {
               Operators.onErrorDropped(t, this.actual.currentContext());
            } else {
               int previousState = this.markTerminated();
               if (!MonoDelayUntil.isTerminated(previousState)) {
                  if (MonoDelayUntil.hasInner(previousState)) {
                     Operators.onDiscard(this.value, this.actual.currentContext());
                     this.triggerSubscriber.cancel();
                  }

                  Throwable e = Exceptions.terminate(ERROR, this);
                  this.actual.onError(e);
               }
            }
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            if (this.value == null) {
               this.done = true;
               this.actual.onComplete();
            }

         }
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            int previousState = this.markHasRequest();
            if (MonoDelayUntil.isTerminated(previousState)) {
               return;
            }

            if (MonoDelayUntil.hasRequest(previousState)) {
               return;
            }

            if (MonoDelayUntil.hasValue(previousState)) {
               this.done = true;
               CoreSubscriber<? super T> actual = this.actual;
               T v = this.value;
               actual.onNext(v);
               actual.onComplete();
            }
         }

      }

      @Override
      public void cancel() {
         int previousState = this.markTerminated();
         if (!MonoDelayUntil.isTerminated(previousState)) {
            Throwable t = Exceptions.terminate(ERROR, this);
            if (t != null) {
               Operators.onErrorDropped(t, this.actual.currentContext());
            }

            if (MonoDelayUntil.hasSubscription(previousState)) {
               this.s.cancel();
            }

            if (MonoDelayUntil.hasInner(previousState)) {
               Operators.onDiscard(this.value, this.actual.currentContext());
               this.triggerSubscriber.cancel();
            }

         }
      }

      void subscribeNextTrigger() {
         Function<? super T, ? extends Publisher<?>> generator = this.otherGenerators[this.index];

         Publisher<?> p;
         try {
            p = (Publisher)generator.apply(this.value);
            Objects.requireNonNull(p, "mapper returned null value");
         } catch (Throwable var4) {
            this.onError(var4);
            return;
         }

         MonoDelayUntil.DelayUntilTrigger triggerSubscriber = this.triggerSubscriber;
         if (triggerSubscriber == null) {
            triggerSubscriber = new MonoDelayUntil.DelayUntilTrigger(this);
            this.triggerSubscriber = triggerSubscriber;
         }

         p.subscribe(triggerSubscriber);
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return MonoDelayUntil.isTerminated(this.state) && !this.done;
         } else if (key != Scannable.Attr.TERMINATED) {
            if (key == Scannable.Attr.PREFETCH) {
               return Integer.MAX_VALUE;
            } else {
               return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
            }
         } else {
            return MonoDelayUntil.isTerminated(this.state) && this.done;
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         MonoDelayUntil.DelayUntilTrigger<?> subscriber = this.triggerSubscriber;
         return subscriber == null ? Stream.empty() : Stream.of(subscriber);
      }

      int markHasSubscription() {
         int state;
         do {
            state = this.state;
            if (MonoDelayUntil.isTerminated(state)) {
               return Integer.MIN_VALUE;
            }
         } while(!STATE.compareAndSet(this, state, state | 1));

         return state;
      }

      int markHasRequest() {
         int state;
         int nextState;
         do {
            state = this.state;
            if (MonoDelayUntil.isTerminated(state)) {
               return Integer.MIN_VALUE;
            }

            if (MonoDelayUntil.hasRequest(state)) {
               return state;
            }

            if (MonoDelayUntil.hasValue(state)) {
               nextState = Integer.MIN_VALUE;
            } else {
               nextState = state | 4;
            }
         } while(!STATE.compareAndSet(this, state, nextState));

         return state;
      }

      int markTerminated() {
         int state;
         do {
            state = this.state;
            if (MonoDelayUntil.isTerminated(state)) {
               return Integer.MIN_VALUE;
            }
         } while(!STATE.compareAndSet(this, state, Integer.MIN_VALUE));

         return state;
      }

      void complete() {
         int s;
         do {
            s = this.state;
            if (MonoDelayUntil.isTerminated(s)) {
               return;
            }

            if (MonoDelayUntil.hasRequest(s) && STATE.compareAndSet(this, s, Integer.MIN_VALUE)) {
               CoreSubscriber<? super T> actual = this.actual;
               T v = this.value;
               actual.onNext(v);
               actual.onComplete();
               return;
            }
         } while(!STATE.compareAndSet(this, s, s | 8));

      }
   }

   static final class DelayUntilTrigger<T> implements InnerConsumer<T> {
      final MonoDelayUntil.DelayUntilCoordinator<?> parent;
      Subscription s;
      boolean done;
      Throwable error;

      DelayUntilTrigger(MonoDelayUntil.DelayUntilCoordinator<?> parent) {
         this.parent = parent;
      }

      @Override
      public Context currentContext() {
         return this.parent.currentContext();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key != Scannable.Attr.CANCELLED) {
            if (key == Scannable.Attr.PARENT) {
               return this.s;
            } else if (key == Scannable.Attr.ACTUAL) {
               return this.parent;
            } else if (key == Scannable.Attr.ERROR) {
               return this.error;
            } else if (key == Scannable.Attr.PREFETCH) {
               return Integer.MAX_VALUE;
            } else {
               return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
            }
         } else {
            return MonoDelayUntil.isTerminated(this.parent.state) && !this.done;
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            int previousState = this.markInnerActive();
            if (MonoDelayUntil.isTerminated(previousState)) {
               s.cancel();
               MonoDelayUntil.DelayUntilCoordinator<?> parent = this.parent;
               Operators.onDiscard(parent.value, parent.currentContext());
               return;
            }

            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
         Operators.onDiscard(t, this.parent.currentContext());
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.parent.currentContext());
         } else {
            MonoDelayUntil.DelayUntilCoordinator<?> parent = this.parent;
            this.done = true;
            parent.done = true;
            if (!Exceptions.addThrowable(MonoDelayUntil.DelayUntilCoordinator.ERROR, parent, t)) {
               Operators.onErrorDropped(t, parent.currentContext());
            } else {
               int previousState = parent.markTerminated();
               if (!MonoDelayUntil.isTerminated(previousState)) {
                  Operators.onDiscard(parent.value, parent.currentContext());
                  parent.s.cancel();
                  Throwable e = Exceptions.terminate(MonoDelayUntil.DelayUntilCoordinator.ERROR, parent);
                  parent.actual.onError(e);
               }
            }
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            MonoDelayUntil.DelayUntilCoordinator<?> parent = this.parent;
            int nextIndex = parent.index + 1;
            parent.index = nextIndex;
            if (nextIndex == parent.otherGenerators.length) {
               parent.complete();
            } else {
               int previousState = this.markInnerInactive();
               if (!MonoDelayUntil.isTerminated(previousState)) {
                  this.done = false;
                  this.s = null;
                  parent.subscribeNextTrigger();
               }
            }
         }
      }

      void cancel() {
         this.s.cancel();
      }

      int markInnerActive() {
         MonoDelayUntil.DelayUntilCoordinator<?> parent = this.parent;

         int state;
         do {
            state = parent.state;
            if (MonoDelayUntil.isTerminated(state)) {
               return Integer.MIN_VALUE;
            }

            if (MonoDelayUntil.hasInner(state)) {
               return state;
            }
         } while(!MonoDelayUntil.DelayUntilCoordinator.STATE.compareAndSet(parent, state, state | 2));

         return state;
      }

      int markInnerInactive() {
         MonoDelayUntil.DelayUntilCoordinator<?> parent = this.parent;

         int state;
         do {
            state = parent.state;
            if (MonoDelayUntil.isTerminated(state)) {
               return Integer.MIN_VALUE;
            }

            if (!MonoDelayUntil.hasInner(state)) {
               return state;
            }
         } while(!MonoDelayUntil.DelayUntilCoordinator.STATE.compareAndSet(parent, state, state & -3));

         return state;
      }
   }
}
