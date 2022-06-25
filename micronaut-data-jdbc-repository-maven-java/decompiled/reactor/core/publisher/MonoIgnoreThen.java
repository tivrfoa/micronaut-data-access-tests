package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoIgnoreThen<T> extends Mono<T> implements Scannable {
   final Publisher<?>[] ignore;
   final Mono<T> last;

   MonoIgnoreThen(Publisher<?>[] ignore, Mono<T> last) {
      this.ignore = (Publisher[])Objects.requireNonNull(ignore, "ignore");
      this.last = (Mono)Objects.requireNonNull(last, "last");
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      MonoIgnoreThen.ThenIgnoreMain<T> manager = new MonoIgnoreThen.ThenIgnoreMain<>(actual, this.ignore, this.last);
      actual.onSubscribe(manager);
      manager.subscribeNext();
   }

   <U> MonoIgnoreThen<U> shift(Mono<U> newLast) {
      Objects.requireNonNull(newLast, "newLast");
      Publisher<?>[] a = this.ignore;
      int n = a.length;
      Publisher<?>[] b = new Publisher[n + 1];
      System.arraycopy(a, 0, b, 0, n);
      b[n] = this.last;
      return new MonoIgnoreThen<>(b, newLast);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }

   static final class ThenIgnoreMain<T> implements InnerOperator<T, T> {
      final Publisher<?>[] ignoreMonos;
      final Mono<T> lastMono;
      final CoreSubscriber<? super T> actual;
      T value;
      int index;
      Subscription activeSubscription;
      boolean done;
      volatile int state;
      private static final AtomicIntegerFieldUpdater<MonoIgnoreThen.ThenIgnoreMain> STATE = AtomicIntegerFieldUpdater.newUpdater(
         MonoIgnoreThen.ThenIgnoreMain.class, "state"
      );
      static final int HAS_REQUEST = 2;
      static final int HAS_SUBSCRIPTION = 4;
      static final int HAS_VALUE = 8;
      static final int HAS_COMPLETION = 16;
      static final int CANCELLED = 128;

      ThenIgnoreMain(CoreSubscriber<? super T> subscriber, Publisher<?>[] ignoreMonos, Mono<T> lastMono) {
         this.actual = subscriber;
         this.ignoreMonos = ignoreMonos;
         this.lastMono = lastMono;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.activeSubscription;
         } else if (key == Scannable.Attr.CANCELLED) {
            return isCancelled(this.state);
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.activeSubscription, s)) {
            this.activeSubscription = s;
            int previousState = this.markHasSubscription();
            if (isCancelled(previousState)) {
               s.cancel();
               return;
            }

            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void cancel() {
         int previousState = this.markCancelled();
         if (hasSubscription(previousState)) {
            this.activeSubscription.cancel();
         }

      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            int state;
            do {
               state = this.state;
               if (isCancelled(state)) {
                  return;
               }

               if (hasRequest(state)) {
                  return;
               }
            } while(!STATE.compareAndSet(this, state, state | 2));

            if (hasValue(state)) {
               CoreSubscriber<? super T> actual = this.actual;
               T v = this.value;
               actual.onNext(v);
               actual.onComplete();
            }

         }
      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onDiscard(t, this.currentContext());
         } else if (this.index != this.ignoreMonos.length) {
            Operators.onDiscard(t, this.currentContext());
         } else {
            this.done = true;
            this.complete(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            if (this.index != this.ignoreMonos.length) {
               int previousState = this.markUnsubscribed();
               if (!isCancelled(previousState)) {
                  this.activeSubscription = null;
                  ++this.index;
                  this.subscribeNext();
               }
            } else {
               this.done = true;
               this.actual.onComplete();
            }
         }
      }

      void subscribeNext() {
         Publisher<?>[] a = this.ignoreMonos;

         while(true) {
            int i = this.index;
            if (i == a.length) {
               Mono<T> m = this.lastMono;
               if (m instanceof Callable) {
                  if (isCancelled(this.state)) {
                     return;
                  }

                  T v;
                  try {
                     v = (T)((Callable)m).call();
                  } catch (Throwable var6) {
                     this.onError(Operators.onOperatorError(var6, this.currentContext()));
                     return;
                  }

                  if (v != null) {
                     this.onNext(v);
                  }

                  this.onComplete();
               } else {
                  m.subscribe(this);
               }

               return;
            }

            Publisher<?> m = a[i];
            if (!(m instanceof Callable)) {
               m.subscribe(this);
               return;
            }

            if (isCancelled(this.state)) {
               return;
            }

            try {
               Operators.onDiscard(((Callable)m).call(), this.currentContext());
            } catch (Throwable var7) {
               this.onError(Operators.onOperatorError(var7, this.currentContext()));
               return;
            }

            this.index = i + 1;
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual().currentContext());
         } else {
            this.done = true;
            this.actual.onError(t);
         }
      }

      final void complete(T value) {
         int s;
         do {
            s = this.state;
            if (isCancelled(s)) {
               Operators.onDiscard(value, this.actual.currentContext());
               return;
            }

            if (hasRequest(s) && STATE.compareAndSet(this, s, s | 24)) {
               CoreSubscriber<? super T> actual = this.actual;
               actual.onNext(value);
               actual.onComplete();
               return;
            }

            this.value = value;
         } while(!STATE.compareAndSet(this, s, s | 24));

      }

      final int markHasSubscription() {
         int state;
         do {
            state = this.state;
            if (state == 128) {
               return state;
            }

            if ((state & 4) == 4) {
               return state;
            }
         } while(!STATE.compareAndSet(this, state, state | 4));

         return state;
      }

      final int markUnsubscribed() {
         int state;
         do {
            state = this.state;
            if (isCancelled(state)) {
               return state;
            }

            if (!hasSubscription(state)) {
               return state;
            }
         } while(!STATE.compareAndSet(this, state, state & -5));

         return state;
      }

      final int markCancelled() {
         int state;
         do {
            state = this.state;
            if (state == 128) {
               return state;
            }
         } while(!STATE.compareAndSet(this, state, 128));

         return state;
      }

      static boolean isCancelled(int s) {
         return s == 128;
      }

      static boolean hasSubscription(int s) {
         return (s & 4) == 4;
      }

      static boolean hasRequest(int s) {
         return (s & 2) == 2;
      }

      static boolean hasValue(int s) {
         return (s & 8) == 8;
      }
   }
}
