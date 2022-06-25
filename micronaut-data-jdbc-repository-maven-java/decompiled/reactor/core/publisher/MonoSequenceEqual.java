package reactor.core.publisher;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiPredicate;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;

final class MonoSequenceEqual<T> extends Mono<Boolean> implements SourceProducer<Boolean> {
   final Publisher<? extends T> first;
   final Publisher<? extends T> second;
   final BiPredicate<? super T, ? super T> comparer;
   final int prefetch;

   MonoSequenceEqual(Publisher<? extends T> first, Publisher<? extends T> second, BiPredicate<? super T, ? super T> comparer, int prefetch) {
      this.first = (Publisher)Objects.requireNonNull(first, "first");
      this.second = (Publisher)Objects.requireNonNull(second, "second");
      this.comparer = (BiPredicate)Objects.requireNonNull(comparer, "comparer");
      if (prefetch < 1) {
         throw new IllegalArgumentException("Buffer size must be strictly positive: " + prefetch);
      } else {
         this.prefetch = prefetch;
      }
   }

   @Override
   public void subscribe(CoreSubscriber<? super Boolean> actual) {
      MonoSequenceEqual.EqualCoordinator<T> ec = new MonoSequenceEqual.EqualCoordinator<>(actual, this.prefetch, this.first, this.second, this.comparer);
      actual.onSubscribe(ec);
      ec.subscribe();
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PREFETCH) {
         return this.prefetch;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   static final class EqualCoordinator<T> implements InnerProducer<Boolean> {
      final CoreSubscriber<? super Boolean> actual;
      final BiPredicate<? super T, ? super T> comparer;
      final Publisher<? extends T> first;
      final Publisher<? extends T> second;
      final MonoSequenceEqual.EqualSubscriber<T> firstSubscriber;
      final MonoSequenceEqual.EqualSubscriber<T> secondSubscriber;
      volatile boolean cancelled;
      volatile int once;
      static final AtomicIntegerFieldUpdater<MonoSequenceEqual.EqualCoordinator> ONCE = AtomicIntegerFieldUpdater.newUpdater(
         MonoSequenceEqual.EqualCoordinator.class, "once"
      );
      T v1;
      T v2;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<MonoSequenceEqual.EqualCoordinator> WIP = AtomicIntegerFieldUpdater.newUpdater(
         MonoSequenceEqual.EqualCoordinator.class, "wip"
      );

      EqualCoordinator(
         CoreSubscriber<? super Boolean> actual,
         int prefetch,
         Publisher<? extends T> first,
         Publisher<? extends T> second,
         BiPredicate<? super T, ? super T> comparer
      ) {
         this.actual = actual;
         this.first = first;
         this.second = second;
         this.comparer = comparer;
         this.firstSubscriber = new MonoSequenceEqual.EqualSubscriber<>(this, prefetch);
         this.secondSubscriber = new MonoSequenceEqual.EqualSubscriber<>(this, prefetch);
      }

      @Override
      public CoreSubscriber<? super Boolean> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerProducer.super.scanUnsafe(key);
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.firstSubscriber, this.secondSubscriber);
      }

      void subscribe() {
         if (ONCE.compareAndSet(this, 0, 1)) {
            this.first.subscribe(this.firstSubscriber);
            this.second.subscribe(this.secondSubscriber);
         }

      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            if (ONCE.compareAndSet(this, 0, 1)) {
               this.first.subscribe(this.firstSubscriber);
               this.second.subscribe(this.secondSubscriber);
            }

         }
      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            this.cancelInner(this.firstSubscriber);
            this.cancelInner(this.secondSubscriber);
            if (WIP.getAndIncrement(this) == 0) {
               this.firstSubscriber.queue.clear();
               this.secondSubscriber.queue.clear();
            }
         }

      }

      void cancel(MonoSequenceEqual.EqualSubscriber<T> s1, Queue<T> q1, MonoSequenceEqual.EqualSubscriber<T> s2, Queue<T> q2) {
         this.cancelled = true;
         this.cancelInner(s1);
         q1.clear();
         this.cancelInner(s2);
         q2.clear();
      }

      void cancelInner(MonoSequenceEqual.EqualSubscriber<T> innerSubscriber) {
         Subscription s = innerSubscriber.subscription;
         if (s != Operators.cancelledSubscription()) {
            s = (Subscription)MonoSequenceEqual.EqualSubscriber.S.getAndSet(innerSubscriber, Operators.cancelledSubscription());
            if (s != null && s != Operators.cancelledSubscription()) {
               s.cancel();
            }
         }

      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            int missed = 1;
            MonoSequenceEqual.EqualSubscriber<T> s1 = this.firstSubscriber;
            Queue<T> q1 = s1.queue;
            MonoSequenceEqual.EqualSubscriber<T> s2 = this.secondSubscriber;
            Queue<T> q2 = s2.queue;

            label99:
            while(true) {
               long r = 0L;

               while(!this.cancelled) {
                  boolean d1 = s1.done;
                  if (d1) {
                     Throwable e = s1.error;
                     if (e != null) {
                        this.cancel(s1, q1, s2, q2);
                        this.actual.onError(e);
                        return;
                     }
                  }

                  boolean d2 = s2.done;
                  if (d2) {
                     Throwable e = s2.error;
                     if (e != null) {
                        this.cancel(s1, q1, s2, q2);
                        this.actual.onError(e);
                        return;
                     }
                  }

                  if (this.v1 == null) {
                     this.v1 = (T)q1.poll();
                  }

                  boolean e1 = this.v1 == null;
                  if (this.v2 == null) {
                     this.v2 = (T)q2.poll();
                  }

                  boolean e2 = this.v2 == null;
                  if (d1 && d2 && e1 && e2) {
                     this.actual.onNext((T)Boolean.valueOf(true));
                     this.actual.onComplete();
                     return;
                  }

                  if (d1 && d2 && e1 != e2) {
                     this.cancel(s1, q1, s2, q2);
                     this.actual.onNext((T)Boolean.valueOf(false));
                     this.actual.onComplete();
                     return;
                  }

                  if (!e1 && !e2) {
                     boolean c;
                     try {
                        c = this.comparer.test(this.v1, this.v2);
                     } catch (Throwable var14) {
                        Exceptions.throwIfFatal(var14);
                        this.cancel(s1, q1, s2, q2);
                        this.actual.onError(Operators.onOperatorError(var14, this.actual.currentContext()));
                        return;
                     }

                     if (!c) {
                        this.cancel(s1, q1, s2, q2);
                        this.actual.onNext((T)Boolean.valueOf(false));
                        this.actual.onComplete();
                        return;
                     }

                     ++r;
                     this.v1 = null;
                     this.v2 = null;
                  }

                  if (e1 || e2) {
                     if (r != 0L) {
                        s1.cachedSubscription.request(r);
                        s2.cachedSubscription.request(r);
                     }

                     missed = WIP.addAndGet(this, -missed);
                     if (missed == 0) {
                        break label99;
                     }
                     continue label99;
                  }
               }

               q1.clear();
               q2.clear();
               return;
            }

         }
      }
   }

   static final class EqualSubscriber<T> implements InnerConsumer<T> {
      final MonoSequenceEqual.EqualCoordinator<T> parent;
      final Queue<T> queue;
      final int prefetch;
      volatile boolean done;
      Throwable error;
      Subscription cachedSubscription;
      volatile Subscription subscription;
      static final AtomicReferenceFieldUpdater<MonoSequenceEqual.EqualSubscriber, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         MonoSequenceEqual.EqualSubscriber.class, Subscription.class, "subscription"
      );

      EqualSubscriber(MonoSequenceEqual.EqualCoordinator<T> parent, int prefetch) {
         this.parent = parent;
         this.prefetch = prefetch;
         this.queue = (Queue)Queues.get(prefetch).get();
      }

      @Override
      public Context currentContext() {
         return this.parent.actual.currentContext();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.parent;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.subscription == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.PARENT) {
            return this.subscription;
         } else if (key == Scannable.Attr.PREFETCH) {
            return this.prefetch;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue.size();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            this.cachedSubscription = s;
            s.request(Operators.unboundedOrPrefetch(this.prefetch));
         }

      }

      @Override
      public void onNext(T t) {
         if (!this.queue.offer(t)) {
            this.onError(
               Operators.onOperatorError(
                  this.cachedSubscription,
                  Exceptions.failWithOverflow("Queue is full: Reactive Streams source doesn't respect backpressure"),
                  t,
                  this.currentContext()
               )
            );
         } else {
            this.parent.drain();
         }
      }

      @Override
      public void onError(Throwable t) {
         this.error = t;
         this.done = true;
         this.parent.drain();
      }

      @Override
      public void onComplete() {
         this.done = true;
         this.parent.drain();
      }
   }
}
