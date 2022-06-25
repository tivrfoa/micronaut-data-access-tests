package reactor.core.publisher;

import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxGroupBy<T, K, V> extends InternalFluxOperator<T, GroupedFlux<K, V>> implements Fuseable {
   final Function<? super T, ? extends K> keySelector;
   final Function<? super T, ? extends V> valueSelector;
   final Supplier<? extends Queue<V>> groupQueueSupplier;
   final Supplier<? extends Queue<GroupedFlux<K, V>>> mainQueueSupplier;
   final int prefetch;

   FluxGroupBy(
      Flux<? extends T> source,
      Function<? super T, ? extends K> keySelector,
      Function<? super T, ? extends V> valueSelector,
      Supplier<? extends Queue<GroupedFlux<K, V>>> mainQueueSupplier,
      Supplier<? extends Queue<V>> groupQueueSupplier,
      int prefetch
   ) {
      super(source);
      if (prefetch <= 0) {
         throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
      } else {
         this.keySelector = (Function)Objects.requireNonNull(keySelector, "keySelector");
         this.valueSelector = (Function)Objects.requireNonNull(valueSelector, "valueSelector");
         this.mainQueueSupplier = (Supplier)Objects.requireNonNull(mainQueueSupplier, "mainQueueSupplier");
         this.groupQueueSupplier = (Supplier)Objects.requireNonNull(groupQueueSupplier, "groupQueueSupplier");
         this.prefetch = prefetch;
      }
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super GroupedFlux<K, V>> actual) {
      return new FluxGroupBy.GroupByMain<>(
         actual, (Queue<GroupedFlux<K, V>>)this.mainQueueSupplier.get(), this.groupQueueSupplier, this.prefetch, this.keySelector, this.valueSelector
      );
   }

   @Override
   public int getPrefetch() {
      return this.prefetch;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class GroupByMain<T, K, V> implements Fuseable.QueueSubscription<GroupedFlux<K, V>>, InnerOperator<T, GroupedFlux<K, V>> {
      final Function<? super T, ? extends K> keySelector;
      final Function<? super T, ? extends V> valueSelector;
      final Queue<GroupedFlux<K, V>> queue;
      final Supplier<? extends Queue<V>> groupQueueSupplier;
      final int prefetch;
      final Map<K, FluxGroupBy.UnicastGroupedFlux<K, V>> groupMap;
      final CoreSubscriber<? super GroupedFlux<K, V>> actual;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxGroupBy.GroupByMain> WIP = AtomicIntegerFieldUpdater.newUpdater(FluxGroupBy.GroupByMain.class, "wip");
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxGroupBy.GroupByMain> REQUESTED = AtomicLongFieldUpdater.newUpdater(FluxGroupBy.GroupByMain.class, "requested");
      volatile boolean done;
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxGroupBy.GroupByMain, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxGroupBy.GroupByMain.class, Throwable.class, "error"
      );
      volatile int cancelled;
      static final AtomicIntegerFieldUpdater<FluxGroupBy.GroupByMain> CANCELLED = AtomicIntegerFieldUpdater.newUpdater(
         FluxGroupBy.GroupByMain.class, "cancelled"
      );
      volatile int groupCount;
      static final AtomicIntegerFieldUpdater<FluxGroupBy.GroupByMain> GROUP_COUNT = AtomicIntegerFieldUpdater.newUpdater(
         FluxGroupBy.GroupByMain.class, "groupCount"
      );
      Subscription s;
      volatile boolean enableAsyncFusion;

      GroupByMain(
         CoreSubscriber<? super GroupedFlux<K, V>> actual,
         Queue<GroupedFlux<K, V>> queue,
         Supplier<? extends Queue<V>> groupQueueSupplier,
         int prefetch,
         Function<? super T, ? extends K> keySelector,
         Function<? super T, ? extends V> valueSelector
      ) {
         this.actual = actual;
         this.queue = queue;
         this.groupQueueSupplier = groupQueueSupplier;
         this.prefetch = prefetch;
         this.groupMap = new ConcurrentHashMap();
         this.keySelector = keySelector;
         this.valueSelector = valueSelector;
         GROUP_COUNT.lazySet(this, 1);
      }

      @Override
      public final CoreSubscriber<? super GroupedFlux<K, V>> actual() {
         return this.actual;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
            s.request(Operators.unboundedOrPrefetch(this.prefetch));
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            K key;
            V value;
            try {
               key = (K)Objects.requireNonNull(this.keySelector.apply(t), "The keySelector returned a null value");
               value = (V)Objects.requireNonNull(this.valueSelector.apply(t), "The valueSelector returned a null value");
            } catch (Throwable var6) {
               this.onError(Operators.onOperatorError(this.s, var6, t, this.actual.currentContext()));
               return;
            }

            FluxGroupBy.UnicastGroupedFlux<K, V> g = (FluxGroupBy.UnicastGroupedFlux)this.groupMap.get(key);
            if (g == null) {
               if (this.cancelled == 0) {
                  Queue<V> q = (Queue)this.groupQueueSupplier.get();
                  GROUP_COUNT.getAndIncrement(this);
                  g = new FluxGroupBy.UnicastGroupedFlux<>(key, q, this, this.prefetch);
                  g.onNext(value);
                  this.groupMap.put(key, g);
                  this.queue.offer(g);
                  this.drain();
               }
            } else {
               g.onNext(value);
            }

         }
      }

      @Override
      public void onError(Throwable t) {
         if (Exceptions.addThrowable(ERROR, this, t)) {
            this.done = true;
            this.drain();
         } else {
            Operators.onErrorDropped(t, this.actual.currentContext());
         }

      }

      @Override
      public void onComplete() {
         if (!this.done) {
            for(FluxGroupBy.UnicastGroupedFlux<K, V> g : this.groupMap.values()) {
               g.onComplete();
            }

            this.groupMap.clear();
            this.done = true;
            this.drain();
         }
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.PREFETCH) {
            return this.prefetch;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue.size();
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled == 1;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return this.groupMap.values().stream();
      }

      void signalAsyncError() {
         Throwable e = Exceptions.terminate(ERROR, this);
         if (e == null) {
            e = new IllegalStateException("FluxGroupBy.signalAsyncError called without error set");
         }

         this.groupCount = 0;

         for(FluxGroupBy.UnicastGroupedFlux<K, V> g : this.groupMap.values()) {
            g.onError(e);
         }

         this.actual.onError(e);
         this.groupMap.clear();
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
         if (CANCELLED.compareAndSet(this, 0, 1)) {
            if (GROUP_COUNT.decrementAndGet(this) == 0) {
               this.s.cancel();
            } else if (!this.enableAsyncFusion && WIP.getAndIncrement(this) == 0) {
               GroupedFlux<K, V> g;
               while((g = (GroupedFlux)this.queue.poll()) != null) {
                  ((FluxGroupBy.UnicastGroupedFlux)g).cancel();
               }

               if (WIP.decrementAndGet(this) == 0) {
                  return;
               }

               this.drainLoop();
            }
         }

      }

      void groupTerminated(K key) {
         if (this.groupCount != 0) {
            this.groupMap.remove(key);
            int groupRemaining = GROUP_COUNT.decrementAndGet(this);
            if (groupRemaining == 0) {
               this.s.cancel();
            } else if (groupRemaining == 1) {
               this.s.request(Operators.unboundedOrPrefetch(this.prefetch));
            }

         }
      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            if (this.enableAsyncFusion) {
               this.drainFused();
            } else {
               this.drainLoop();
            }

         }
      }

      void drainFused() {
         int missed = 1;
         Subscriber<? super GroupedFlux<K, V>> a = this.actual;
         Queue<GroupedFlux<K, V>> q = this.queue;

         while(this.cancelled == 0) {
            boolean d = this.done;
            a.onNext(null);
            if (d) {
               Throwable ex = this.error;
               if (ex != null) {
                  this.signalAsyncError();
               } else {
                  a.onComplete();
               }

               return;
            }

            missed = WIP.addAndGet(this, -missed);
            if (missed == 0) {
               return;
            }
         }

         q.clear();
      }

      void drainLoop() {
         int missed = 1;
         Subscriber<? super GroupedFlux<K, V>> a = this.actual;
         Queue<GroupedFlux<K, V>> q = this.queue;

         do {
            long r = this.requested;

            long e;
            for(e = 0L; e != r; ++e) {
               boolean d = this.done;
               GroupedFlux<K, V> v = (GroupedFlux)q.poll();
               boolean empty = v == null;
               if (this.checkTerminated(d, empty, a, q)) {
                  return;
               }

               if (empty) {
                  break;
               }

               a.onNext(v);
            }

            if (e == r && this.checkTerminated(this.done, q.isEmpty(), a, q)) {
               return;
            }

            if (e != 0L) {
               this.s.request(e);
               if (r != Long.MAX_VALUE) {
                  REQUESTED.addAndGet(this, -e);
               }
            }

            missed = WIP.addAndGet(this, -missed);
         } while(missed != 0);

      }

      boolean checkTerminated(boolean d, boolean empty, Subscriber<?> a, Queue<GroupedFlux<K, V>> q) {
         if (d) {
            Throwable e = this.error;
            if (e != null && e != Exceptions.TERMINATED) {
               q.clear();
               this.signalAsyncError();
               return true;
            }

            if (empty) {
               a.onComplete();
               return true;
            }
         }

         return false;
      }

      @Nullable
      public GroupedFlux<K, V> poll() {
         return (GroupedFlux<K, V>)this.queue.poll();
      }

      public int size() {
         return this.queue.size();
      }

      public boolean isEmpty() {
         return this.queue.isEmpty();
      }

      public void clear() {
         this.queue.clear();
      }

      @Override
      public int requestFusion(int requestedMode) {
         if ((requestedMode & 2) != 0) {
            this.enableAsyncFusion = true;
            return 2;
         } else {
            return 0;
         }
      }
   }

   static final class UnicastGroupedFlux<K, V> extends GroupedFlux<K, V> implements Fuseable, Fuseable.QueueSubscription<V>, InnerProducer<V> {
      final K key;
      final int limit;
      final Context context;
      final Queue<V> queue;
      volatile FluxGroupBy.GroupByMain<?, K, V> parent;
      static final AtomicReferenceFieldUpdater<FluxGroupBy.UnicastGroupedFlux, FluxGroupBy.GroupByMain> PARENT = AtomicReferenceFieldUpdater.newUpdater(
         FluxGroupBy.UnicastGroupedFlux.class, FluxGroupBy.GroupByMain.class, "parent"
      );
      volatile boolean done;
      Throwable error;
      volatile CoreSubscriber<? super V> actual;
      static final AtomicReferenceFieldUpdater<FluxGroupBy.UnicastGroupedFlux, CoreSubscriber> ACTUAL = AtomicReferenceFieldUpdater.newUpdater(
         FluxGroupBy.UnicastGroupedFlux.class, CoreSubscriber.class, "actual"
      );
      volatile boolean cancelled;
      volatile int once;
      static final AtomicIntegerFieldUpdater<FluxGroupBy.UnicastGroupedFlux> ONCE = AtomicIntegerFieldUpdater.newUpdater(
         FluxGroupBy.UnicastGroupedFlux.class, "once"
      );
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxGroupBy.UnicastGroupedFlux> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxGroupBy.UnicastGroupedFlux.class, "wip"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxGroupBy.UnicastGroupedFlux> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxGroupBy.UnicastGroupedFlux.class, "requested"
      );
      volatile boolean outputFused;
      int produced;
      boolean isFirstRequest = true;

      @Override
      public K key() {
         return this.key;
      }

      UnicastGroupedFlux(K key, Queue<V> queue, FluxGroupBy.GroupByMain<?, K, V> parent, int prefetch) {
         this.key = key;
         this.queue = queue;
         this.context = parent.currentContext();
         this.parent = parent;
         this.limit = Operators.unboundedOrLimit(prefetch);
      }

      void doTerminate() {
         FluxGroupBy.GroupByMain<?, K, V> r = this.parent;
         if (r != null && PARENT.compareAndSet(this, r, null)) {
            r.groupTerminated(this.key);
         }

      }

      void drainRegular(Subscriber<? super V> a) {
         int missed = 1;
         Queue<V> q = this.queue;

         do {
            long r = this.requested;

            long e;
            for(e = 0L; r != e; ++e) {
               boolean d = this.done;
               V t = (V)q.poll();
               boolean empty = t == null;
               if (this.checkTerminated(d, empty, a, q)) {
                  return;
               }

               if (empty) {
                  break;
               }

               a.onNext(t);
            }

            if (r == e && this.checkTerminated(this.done, q.isEmpty(), a, q)) {
               return;
            }

            if (e != 0L) {
               FluxGroupBy.GroupByMain<?, K, V> main = this.parent;
               if (main != null) {
                  if (this.isFirstRequest) {
                     this.isFirstRequest = false;
                     long toRequest = e - 1L;
                     if (toRequest > 0L) {
                        main.s.request(toRequest);
                     }
                  } else {
                     main.s.request(e);
                  }
               }

               if (r != Long.MAX_VALUE) {
                  REQUESTED.addAndGet(this, -e);
               }
            }

            missed = WIP.addAndGet(this, -missed);
         } while(missed != 0);

      }

      void drainFused(Subscriber<? super V> a) {
         int missed = 1;
         Queue<V> q = this.queue;

         while(!this.cancelled) {
            boolean d = this.done;
            a.onNext((V)null);
            if (d) {
               this.actual = null;
               Throwable ex = this.error;
               if (ex != null) {
                  a.onError(ex);
               } else {
                  a.onComplete();
               }

               return;
            }

            missed = WIP.addAndGet(this, -missed);
            if (missed == 0) {
               return;
            }
         }

         q.clear();
         this.actual = null;
      }

      void drain() {
         Subscriber<? super V> a = this.actual;
         if (a != null) {
            if (WIP.getAndIncrement(this) != 0) {
               return;
            }

            if (this.outputFused) {
               this.drainFused(a);
            } else {
               this.drainRegular(a);
            }
         }

      }

      boolean checkTerminated(boolean d, boolean empty, Subscriber<?> a, Queue<?> q) {
         if (this.cancelled) {
            q.clear();
            this.actual = null;
            return true;
         } else if (d && empty) {
            Throwable e = this.error;
            this.actual = null;
            if (e != null) {
               a.onError(e);
            } else {
               a.onComplete();
            }

            return true;
         } else {
            return false;
         }
      }

      public void onNext(V t) {
         Subscriber<? super V> a = this.actual;
         if (!this.queue.offer(t)) {
            this.onError(
               Operators.onOperatorError(
                  this, Exceptions.failWithOverflow("Queue is full: Reactive Streams source doesn't respect backpressure"), t, this.actual.currentContext()
               )
            );
         } else {
            if (this.outputFused) {
               if (a != null) {
                  a.onNext((V)null);
               }
            } else {
               this.drain();
            }

         }
      }

      public void onError(Throwable t) {
         this.error = t;
         this.done = true;
         this.doTerminate();
         this.drain();
      }

      public void onComplete() {
         this.done = true;
         this.doTerminate();
         this.drain();
      }

      @Override
      public void subscribe(CoreSubscriber<? super V> actual) {
         if (this.once == 0 && ONCE.compareAndSet(this, 0, 1)) {
            actual.onSubscribe(this);
            ACTUAL.lazySet(this, actual);
            this.drain();
         } else {
            actual.onError(new IllegalStateException("GroupedFlux allows only one Subscriber"));
         }

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
            this.doTerminate();
            if (!this.outputFused && WIP.getAndIncrement(this) == 0) {
               this.queue.clear();
            }

         }
      }

      @Nullable
      public V poll() {
         V v = (V)this.queue.poll();
         if (v != null) {
            ++this.produced;
         } else {
            this.tryReplenish();
         }

         return v;
      }

      void tryReplenish() {
         int p = this.produced;
         if (p != 0) {
            this.produced = 0;
            FluxGroupBy.GroupByMain<?, K, V> main = this.parent;
            if (main != null) {
               if (this.isFirstRequest) {
                  this.isFirstRequest = false;
                  if (--p > 0) {
                     main.s.request((long)p);
                  }
               } else {
                  main.s.request((long)p);
               }
            }
         }

      }

      public int size() {
         return this.queue.size();
      }

      public boolean isEmpty() {
         if (this.queue.isEmpty()) {
            this.tryReplenish();
            return true;
         } else {
            return false;
         }
      }

      public void clear() {
         this.queue.clear();
      }

      @Override
      public int requestFusion(int requestedMode) {
         if ((requestedMode & 2) != 0) {
            this.outputFused = true;
            return 2;
         } else {
            return 0;
         }
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.parent;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue != null ? this.queue.size() : 0;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerProducer.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super V> actual() {
         return this.actual;
      }
   }
}
