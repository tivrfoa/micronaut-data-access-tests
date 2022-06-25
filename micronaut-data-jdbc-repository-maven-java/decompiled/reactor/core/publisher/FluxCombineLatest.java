package reactor.core.publisher;

import java.util.Iterator;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxCombineLatest<T, R> extends Flux<R> implements Fuseable, SourceProducer<R> {
   final Publisher<? extends T>[] array;
   final Iterable<? extends Publisher<? extends T>> iterable;
   final Function<Object[], R> combiner;
   final Supplier<? extends Queue<FluxCombineLatest.SourceAndArray>> queueSupplier;
   final int prefetch;

   FluxCombineLatest(
      Publisher<? extends T>[] array, Function<Object[], R> combiner, Supplier<? extends Queue<FluxCombineLatest.SourceAndArray>> queueSupplier, int prefetch
   ) {
      if (prefetch <= 0) {
         throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
      } else {
         this.array = (Publisher[])Objects.requireNonNull(array, "array");
         this.iterable = null;
         this.combiner = (Function)Objects.requireNonNull(combiner, "combiner");
         this.queueSupplier = (Supplier)Objects.requireNonNull(queueSupplier, "queueSupplier");
         this.prefetch = prefetch;
      }
   }

   FluxCombineLatest(
      Iterable<? extends Publisher<? extends T>> iterable,
      Function<Object[], R> combiner,
      Supplier<? extends Queue<FluxCombineLatest.SourceAndArray>> queueSupplier,
      int prefetch
   ) {
      if (prefetch < 0) {
         throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
      } else {
         this.array = null;
         this.iterable = (Iterable)Objects.requireNonNull(iterable, "iterable");
         this.combiner = (Function)Objects.requireNonNull(combiner, "combiner");
         this.queueSupplier = (Supplier)Objects.requireNonNull(queueSupplier, "queueSupplier");
         this.prefetch = prefetch;
      }
   }

   @Override
   public int getPrefetch() {
      return this.prefetch;
   }

   @Override
   public void subscribe(CoreSubscriber<? super R> actual) {
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
      } else {
         if (n == 1) {
            Function<T, R> f = t -> this.combiner.apply(new Object[]{t});
            if (a[0] instanceof Fuseable) {
               new FluxMapFuseable<R, R>(from(a[0]), f).subscribe(actual);
               return;
            }

            if (!(actual instanceof Fuseable.QueueSubscription)) {
               new FluxMap<R, R>(from(a[0]), f).subscribe(actual);
               return;
            }
         }

         Queue<FluxCombineLatest.SourceAndArray> queue = (Queue)this.queueSupplier.get();
         FluxCombineLatest.CombineLatestCoordinator<T, R> coordinator = new FluxCombineLatest.CombineLatestCoordinator<>(
            actual, this.combiner, n, queue, this.prefetch
         );
         actual.onSubscribe(coordinator);
         coordinator.subscribe(a, n);
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PREFETCH) {
         return this.prefetch;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   static final class CombineLatestCoordinator<T, R> implements Fuseable.QueueSubscription<R>, InnerProducer<R> {
      final Function<Object[], R> combiner;
      final FluxCombineLatest.CombineLatestInner<T>[] subscribers;
      final Queue<FluxCombineLatest.SourceAndArray> queue;
      final Object[] latest;
      final CoreSubscriber<? super R> actual;
      boolean outputFused;
      int nonEmptySources;
      int completedSources;
      volatile boolean cancelled;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxCombineLatest.CombineLatestCoordinator> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxCombineLatest.CombineLatestCoordinator.class, "requested"
      );
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxCombineLatest.CombineLatestCoordinator> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxCombineLatest.CombineLatestCoordinator.class, "wip"
      );
      volatile boolean done;
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxCombineLatest.CombineLatestCoordinator, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxCombineLatest.CombineLatestCoordinator.class, Throwable.class, "error"
      );

      CombineLatestCoordinator(
         CoreSubscriber<? super R> actual, Function<Object[], R> combiner, int n, Queue<FluxCombineLatest.SourceAndArray> queue, int prefetch
      ) {
         this.actual = actual;
         this.combiner = combiner;
         FluxCombineLatest.CombineLatestInner<T>[] a = new FluxCombineLatest.CombineLatestInner[n];

         for(int i = 0; i < n; ++i) {
            a[i] = new FluxCombineLatest.CombineLatestInner<>(this, i, prefetch);
         }

         this.subscribers = a;
         this.latest = new Object[n];
         this.queue = queue;
      }

      @Override
      public final CoreSubscriber<? super R> actual() {
         return this.actual;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
            this.drain();
         }

      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            this.cancelAll();
            if (WIP.getAndIncrement(this) == 0) {
               this.clear();
            }

         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.subscribers);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else {
            return key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM ? this.requested : InnerProducer.super.scanUnsafe(key);
         }
      }

      void subscribe(Publisher<? extends T>[] sources, int n) {
         FluxCombineLatest.CombineLatestInner<T>[] a = this.subscribers;

         for(int i = 0; i < n; ++i) {
            if (this.done || this.cancelled) {
               return;
            }

            sources[i].subscribe(a[i]);
         }

      }

      void innerValue(int index, T value) {
         boolean replenishInsteadOfDrain;
         synchronized(this) {
            Object[] os = this.latest;
            int localNonEmptySources = this.nonEmptySources;
            if (os[index] == null) {
               this.nonEmptySources = ++localNonEmptySources;
            }

            os[index] = value;
            if (os.length == localNonEmptySources) {
               FluxCombineLatest.SourceAndArray sa = new FluxCombineLatest.SourceAndArray(this.subscribers[index], os.clone());
               if (!this.queue.offer(sa)) {
                  this.innerError(
                     Operators.onOperatorError(
                        this, Exceptions.failWithOverflow("Queue is full: Reactive Streams source doesn't respect backpressure"), this.actual.currentContext()
                     )
                  );
                  return;
               }

               replenishInsteadOfDrain = false;
            } else {
               replenishInsteadOfDrain = true;
            }
         }

         if (replenishInsteadOfDrain) {
            this.subscribers[index].requestOne();
         } else {
            this.drain();
         }

      }

      void innerComplete(int index) {
         synchronized(this) {
            Object[] os = this.latest;
            if (os[index] != null) {
               int localCompletedSources = this.completedSources + 1;
               if (localCompletedSources != os.length) {
                  this.completedSources = localCompletedSources;
                  return;
               }

               this.done = true;
            } else {
               this.done = true;
            }
         }

         this.drain();
      }

      void innerError(Throwable e) {
         if (Exceptions.addThrowable(ERROR, this, e)) {
            this.done = true;
            this.drain();
         } else {
            this.discardQueue(this.queue);
            Operators.onErrorDropped(e, this.actual.currentContext());
         }

      }

      void drainOutput() {
         CoreSubscriber<? super R> a = this.actual;
         Queue<FluxCombineLatest.SourceAndArray> q = this.queue;
         int missed = 1;

         while(!this.cancelled) {
            Throwable ex = this.error;
            if (ex != null) {
               this.discardQueue(q);
               a.onError(ex);
               return;
            }

            boolean d = this.done;
            boolean empty = q.isEmpty();
            if (!empty) {
               a.onNext((R)null);
            }

            if (d && empty) {
               a.onComplete();
               return;
            }

            missed = WIP.addAndGet(this, -missed);
            if (missed == 0) {
               return;
            }
         }

         this.discardQueue(q);
      }

      void drainAsync() {
         Queue<FluxCombineLatest.SourceAndArray> q = this.queue;
         int missed = 1;

         do {
            long r = this.requested;

            long e;
            for(e = 0L; e != r; ++e) {
               boolean d = this.done;
               FluxCombineLatest.SourceAndArray v = (FluxCombineLatest.SourceAndArray)q.poll();
               boolean empty = v == null;
               if (this.checkTerminated(d, empty, q)) {
                  return;
               }

               if (empty) {
                  break;
               }

               R w;
               try {
                  w = (R)Objects.requireNonNull(this.combiner.apply(v.array), "Combiner returned null");
               } catch (Throwable var13) {
                  Context ctx = this.actual.currentContext();
                  Operators.onDiscardMultiple(Stream.of(v.array), ctx);
                  Throwable ex = Operators.onOperatorError(this, var13, v.array, ctx);
                  Exceptions.addThrowable(ERROR, this, ex);
                  ex = Exceptions.terminate(ERROR, this);
                  this.actual.onError(ex);
                  return;
               }

               this.actual.onNext(w);
               v.source.requestOne();
            }

            if (e == r && this.checkTerminated(this.done, q.isEmpty(), q)) {
               return;
            }

            if (e != 0L && r != Long.MAX_VALUE) {
               REQUESTED.addAndGet(this, -e);
            }

            missed = WIP.addAndGet(this, -missed);
         } while(missed != 0);

      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            if (this.outputFused) {
               this.drainOutput();
            } else {
               this.drainAsync();
            }

         }
      }

      boolean checkTerminated(boolean d, boolean empty, Queue<FluxCombineLatest.SourceAndArray> q) {
         if (this.cancelled) {
            this.cancelAll();
            this.discardQueue(q);
            return true;
         } else {
            if (d) {
               Throwable e = Exceptions.terminate(ERROR, this);
               if (e != null && e != Exceptions.TERMINATED) {
                  this.cancelAll();
                  this.discardQueue(q);
                  this.actual.onError(e);
                  return true;
               }

               if (empty) {
                  this.cancelAll();
                  this.actual.onComplete();
                  return true;
               }
            }

            return false;
         }
      }

      void cancelAll() {
         for(FluxCombineLatest.CombineLatestInner<T> inner : this.subscribers) {
            inner.cancel();
         }

      }

      @Override
      public int requestFusion(int requestedMode) {
         if ((requestedMode & 4) != 0) {
            return 0;
         } else {
            int m = requestedMode & 2;
            this.outputFused = m != 0;
            return m;
         }
      }

      @Nullable
      public R poll() {
         FluxCombineLatest.SourceAndArray e = (FluxCombineLatest.SourceAndArray)this.queue.poll();
         if (e == null) {
            return null;
         } else {
            R r = (R)this.combiner.apply(e.array);
            e.source.requestOne();
            return r;
         }
      }

      private void discardQueue(Queue<FluxCombineLatest.SourceAndArray> q) {
         Operators.onDiscardQueueWithClear(q, this.actual.currentContext(), FluxCombineLatest.SourceAndArray::toStream);
      }

      public void clear() {
         this.discardQueue(this.queue);
      }

      public boolean isEmpty() {
         return this.queue.isEmpty();
      }

      public int size() {
         return this.queue.size();
      }
   }

   static final class CombineLatestInner<T> implements InnerConsumer<T> {
      final FluxCombineLatest.CombineLatestCoordinator<T, ?> parent;
      final int index;
      final int prefetch;
      final int limit;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<FluxCombineLatest.CombineLatestInner, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         FluxCombineLatest.CombineLatestInner.class, Subscription.class, "s"
      );
      int produced;

      CombineLatestInner(FluxCombineLatest.CombineLatestCoordinator<T, ?> parent, int index, int prefetch) {
         this.parent = parent;
         this.index = index;
         this.prefetch = prefetch;
         this.limit = Operators.unboundedOrLimit(prefetch);
      }

      @Override
      public Context currentContext() {
         return this.parent.actual.currentContext();
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            s.request(Operators.unboundedOrPrefetch(this.prefetch));
         }

      }

      @Override
      public void onNext(T t) {
         this.parent.innerValue(this.index, t);
      }

      @Override
      public void onError(Throwable t) {
         this.parent.innerError(t);
      }

      @Override
      public void onComplete() {
         this.parent.innerComplete(this.index);
      }

      public void cancel() {
         Operators.terminate(S, this);
      }

      void requestOne() {
         int p = this.produced + 1;
         if (p == this.limit) {
            this.produced = 0;
            this.s.request((long)p);
         } else {
            this.produced = p;
         }

      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.parent;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.s == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.PREFETCH) {
            return this.prefetch;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }
   }

   static final class SourceAndArray {
      final FluxCombineLatest.CombineLatestInner<?> source;
      final Object[] array;

      SourceAndArray(FluxCombineLatest.CombineLatestInner<?> source, Object[] array) {
         this.source = source;
         this.array = array;
      }

      final Stream<?> toStream() {
         return Stream.of(this.array);
      }
   }
}
