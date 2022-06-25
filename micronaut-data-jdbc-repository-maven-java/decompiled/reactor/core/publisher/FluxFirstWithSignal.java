package reactor.core.publisher;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxFirstWithSignal<T> extends Flux<T> implements SourceProducer<T> {
   final Publisher<? extends T>[] array;
   final Iterable<? extends Publisher<? extends T>> iterable;

   @SafeVarargs
   FluxFirstWithSignal(Publisher<? extends T>... array) {
      this.array = (Publisher[])Objects.requireNonNull(array, "array");
      this.iterable = null;
   }

   FluxFirstWithSignal(Iterable<? extends Publisher<? extends T>> iterable) {
      this.array = null;
      this.iterable = (Iterable)Objects.requireNonNull(iterable);
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      Publisher<? extends T>[] a = this.array;
      int n;
      if (a == null) {
         n = 0;
         a = new Publisher[8];

         Iterator<? extends Publisher<? extends T>> it;
         try {
            it = (Iterator)Objects.requireNonNull(this.iterable.iterator(), "The iterator returned is null");
         } catch (Throwable var10) {
            Operators.error(actual, Operators.onOperatorError(var10, actual.currentContext()));
            return;
         }

         while(true) {
            boolean b;
            try {
               b = it.hasNext();
            } catch (Throwable var8) {
               Operators.error(actual, Operators.onOperatorError(var8, actual.currentContext()));
               return;
            }

            if (!b) {
               break;
            }

            Publisher<? extends T> p;
            try {
               p = (Publisher)Objects.requireNonNull(it.next(), "The Publisher returned by the iterator is null");
            } catch (Throwable var9) {
               Operators.error(actual, Operators.onOperatorError(var9, actual.currentContext()));
               return;
            }

            if (n == a.length) {
               Publisher<? extends T>[] c = new Publisher[n + (n >> 2)];
               System.arraycopy(a, 0, c, 0, n);
               a = c;
            }

            a[n++] = p;
         }
      } else {
         n = a.length;
      }

      if (n == 0) {
         Operators.complete(actual);
      } else if (n == 1) {
         Publisher<? extends T> p = a[0];
         if (p == null) {
            Operators.error(actual, new NullPointerException("The single source Publisher is null"));
         } else {
            p.subscribe(actual);
         }

      } else {
         FluxFirstWithSignal.RaceCoordinator<T> coordinator = new FluxFirstWithSignal.RaceCoordinator<>(n);
         coordinator.subscribe(a, n, actual);
      }
   }

   @Nullable
   FluxFirstWithSignal<T> orAdditionalSource(Publisher<? extends T> source) {
      if (this.array != null) {
         int n = this.array.length;
         Publisher<? extends T>[] newArray = new Publisher[n + 1];
         System.arraycopy(this.array, 0, newArray, 0, n);
         newArray[n] = source;
         return new FluxFirstWithSignal<>(newArray);
      } else {
         return null;
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }

   static final class FirstEmittingSubscriber<T> extends Operators.DeferredSubscription implements InnerOperator<T, T> {
      final FluxFirstWithSignal.RaceCoordinator<T> parent;
      final CoreSubscriber<? super T> actual;
      final int index;
      boolean won;

      FirstEmittingSubscriber(CoreSubscriber<? super T> actual, FluxFirstWithSignal.RaceCoordinator<T> parent, int index) {
         this.actual = actual;
         this.parent = parent;
         this.index = index;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.parent.cancelled;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         this.set(s);
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public void onNext(T t) {
         if (this.won) {
            this.actual.onNext(t);
         } else if (this.parent.tryWin(this.index)) {
            this.won = true;
            this.actual.onNext(t);
         }

      }

      @Override
      public void onError(Throwable t) {
         if (this.won) {
            this.actual.onError(t);
         } else if (this.parent.tryWin(this.index)) {
            this.won = true;
            this.actual.onError(t);
         }

      }

      @Override
      public void onComplete() {
         if (this.won) {
            this.actual.onComplete();
         } else if (this.parent.tryWin(this.index)) {
            this.won = true;
            this.actual.onComplete();
         }

      }
   }

   static final class RaceCoordinator<T> implements Subscription, Scannable {
      final FluxFirstWithSignal.FirstEmittingSubscriber<T>[] subscribers;
      volatile boolean cancelled;
      volatile int winner;
      static final AtomicIntegerFieldUpdater<FluxFirstWithSignal.RaceCoordinator> WINNER = AtomicIntegerFieldUpdater.newUpdater(
         FluxFirstWithSignal.RaceCoordinator.class, "winner"
      );

      RaceCoordinator(int n) {
         this.subscribers = new FluxFirstWithSignal.FirstEmittingSubscriber[n];
         this.winner = Integer.MIN_VALUE;
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.subscribers);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.CANCELLED ? this.cancelled : null;
      }

      void subscribe(Publisher<? extends T>[] sources, int n, CoreSubscriber<? super T> actual) {
         FluxFirstWithSignal.FirstEmittingSubscriber<T>[] a = this.subscribers;

         for(int i = 0; i < n; ++i) {
            a[i] = new FluxFirstWithSignal.FirstEmittingSubscriber<>(actual, this, i);
         }

         actual.onSubscribe(this);

         for(int i = 0; i < n; ++i) {
            if (this.cancelled || this.winner != Integer.MIN_VALUE) {
               return;
            }

            Publisher<? extends T> p = sources[i];
            if (p == null) {
               if (WINNER.compareAndSet(this, Integer.MIN_VALUE, -1)) {
                  actual.onError(new NullPointerException("The " + i + " th Publisher source is null"));
               }

               return;
            }

            p.subscribe(a[i]);
         }

      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            int w = this.winner;
            if (w >= 0) {
               this.subscribers[w].request(n);
            } else {
               for(FluxFirstWithSignal.FirstEmittingSubscriber<T> s : this.subscribers) {
                  s.request(n);
               }
            }
         }

      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            int w = this.winner;
            if (w >= 0) {
               this.subscribers[w].cancel();
            } else {
               for(FluxFirstWithSignal.FirstEmittingSubscriber<T> s : this.subscribers) {
                  s.cancel();
               }
            }

         }
      }

      boolean tryWin(int index) {
         if (this.winner == Integer.MIN_VALUE && WINNER.compareAndSet(this, Integer.MIN_VALUE, index)) {
            FluxFirstWithSignal.FirstEmittingSubscriber<T>[] a = this.subscribers;
            int n = a.length;

            for(int i = 0; i < n; ++i) {
               if (i != index) {
                  a[i].cancel();
               }
            }

            return true;
         } else {
            return false;
         }
      }
   }
}
