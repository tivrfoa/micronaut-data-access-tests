package reactor.core.publisher;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxFirstWithValue<T> extends Flux<T> implements SourceProducer<T> {
   final Publisher<? extends T>[] array;
   final Iterable<? extends Publisher<? extends T>> iterable;

   private FluxFirstWithValue(Publisher<? extends T>[] array) {
      this.array = (Publisher[])Objects.requireNonNull(array, "array");
      this.iterable = null;
   }

   @SafeVarargs
   FluxFirstWithValue(Publisher<? extends T> first, Publisher<? extends T>... others) {
      Objects.requireNonNull(first, "first");
      Objects.requireNonNull(others, "others");
      Publisher<? extends T>[] newArray = new Publisher[others.length + 1];
      newArray[0] = first;
      System.arraycopy(others, 0, newArray, 1, others.length);
      this.array = newArray;
      this.iterable = null;
   }

   FluxFirstWithValue(Iterable<? extends Publisher<? extends T>> iterable) {
      this.array = null;
      this.iterable = (Iterable)Objects.requireNonNull(iterable);
   }

   @SafeVarargs
   @Nullable
   final FluxFirstWithValue<T> firstValuedAdditionalSources(Publisher<? extends T>... others) {
      Objects.requireNonNull(others, "others");
      if (others.length == 0) {
         return this;
      } else if (this.array == null) {
         return null;
      } else {
         int currentSize = this.array.length;
         int otherSize = others.length;
         Publisher<? extends T>[] newArray = new Publisher[currentSize + otherSize];
         System.arraycopy(this.array, 0, newArray, 0, currentSize);
         System.arraycopy(others, 0, newArray, currentSize, otherSize);
         return new FluxFirstWithValue<>(newArray);
      }
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
         FluxFirstWithValue.RaceValuesCoordinator<T> coordinator = new FluxFirstWithValue.RaceValuesCoordinator<>(n);
         coordinator.subscribe(a, n, actual);
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }

   static final class FirstValuesEmittingSubscriber<T> extends Operators.DeferredSubscription implements InnerOperator<T, T> {
      final FluxFirstWithValue.RaceValuesCoordinator<T> parent;
      final CoreSubscriber<? super T> actual;
      final int index;
      boolean won;

      FirstValuesEmittingSubscriber(CoreSubscriber<? super T> actual, FluxFirstWithValue.RaceValuesCoordinator<T> parent, int index) {
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
         } else {
            this.recordTerminalSignals(t);
         }

      }

      @Override
      public void onComplete() {
         if (this.won) {
            this.actual.onComplete();
         } else {
            this.recordTerminalSignals(new NoSuchElementException("source at index " + this.index + " completed empty"));
         }

      }

      void recordTerminalSignals(Throwable t) {
         this.parent.errorsOrCompleteEmpty[this.index] = t;
         int nb = FluxFirstWithValue.RaceValuesCoordinator.ERRORS_OR_COMPLETED_EMPTY.incrementAndGet(this.parent);
         if (nb == this.parent.subscribers.length) {
            NoSuchElementException e = new NoSuchElementException("All sources completed with error or without values");
            e.initCause(Exceptions.multiple(this.parent.errorsOrCompleteEmpty));
            this.actual.onError(e);
         }

      }
   }

   static final class RaceValuesCoordinator<T> implements Subscription, Scannable {
      final FluxFirstWithValue.FirstValuesEmittingSubscriber<T>[] subscribers;
      final Throwable[] errorsOrCompleteEmpty;
      volatile boolean cancelled;
      volatile int winner;
      static final AtomicIntegerFieldUpdater<FluxFirstWithValue.RaceValuesCoordinator> WINNER = AtomicIntegerFieldUpdater.newUpdater(
         FluxFirstWithValue.RaceValuesCoordinator.class, "winner"
      );
      volatile int nbErrorsOrCompletedEmpty;
      static final AtomicIntegerFieldUpdater<FluxFirstWithValue.RaceValuesCoordinator> ERRORS_OR_COMPLETED_EMPTY = AtomicIntegerFieldUpdater.newUpdater(
         FluxFirstWithValue.RaceValuesCoordinator.class, "nbErrorsOrCompletedEmpty"
      );

      public RaceValuesCoordinator(int n) {
         this.subscribers = new FluxFirstWithValue.FirstValuesEmittingSubscriber[n];
         this.errorsOrCompleteEmpty = new Throwable[n];
         this.winner = Integer.MIN_VALUE;
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.subscribers);
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.CANCELLED ? this.cancelled : null;
      }

      void subscribe(Publisher<? extends T>[] sources, int n, CoreSubscriber<? super T> actual) {
         for(int i = 0; i < n; ++i) {
            this.subscribers[i] = new FluxFirstWithValue.FirstValuesEmittingSubscriber<>(actual, this, i);
         }

         actual.onSubscribe(this);

         for(int i = 0; i < n; ++i) {
            if (this.cancelled || this.winner != Integer.MIN_VALUE) {
               return;
            }

            if (sources[i] == null) {
               actual.onError(new NullPointerException("The " + i + " th Publisher source is null"));
               return;
            }

            sources[i].subscribe(this.subscribers[i]);
         }

      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            int w = this.winner;
            if (w >= 0) {
               this.subscribers[w].request(n);
            } else {
               for(FluxFirstWithValue.FirstValuesEmittingSubscriber<T> s : this.subscribers) {
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
               for(FluxFirstWithValue.FirstValuesEmittingSubscriber<T> s : this.subscribers) {
                  s.cancel();
               }
            }

         }
      }

      boolean tryWin(int index) {
         if (this.winner == Integer.MIN_VALUE && WINNER.compareAndSet(this, Integer.MIN_VALUE, index)) {
            for(int i = 0; i < this.subscribers.length; ++i) {
               if (i != index) {
                  this.subscribers[i].cancel();
                  this.errorsOrCompleteEmpty[i] = null;
               }
            }

            return true;
         } else {
            return false;
         }
      }
   }
}
