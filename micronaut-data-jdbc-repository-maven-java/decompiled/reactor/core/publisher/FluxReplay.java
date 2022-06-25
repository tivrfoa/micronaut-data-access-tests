package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.Nullable;
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;

final class FluxReplay<T> extends ConnectableFlux<T> implements Scannable, Fuseable, OptimizableOperator<T, T> {
   final CorePublisher<T> source;
   final int history;
   final long ttl;
   final Scheduler scheduler;
   volatile FluxReplay.ReplaySubscriber<T> connection;
   static final AtomicReferenceFieldUpdater<FluxReplay, FluxReplay.ReplaySubscriber> CONNECTION = AtomicReferenceFieldUpdater.newUpdater(
      FluxReplay.class, FluxReplay.ReplaySubscriber.class, "connection"
   );
   @Nullable
   final OptimizableOperator<?, T> optimizableOperator;

   FluxReplay(CorePublisher<T> source, int history, long ttl, @Nullable Scheduler scheduler) {
      this.source = (CorePublisher)Objects.requireNonNull(source, "source");
      if (source instanceof OptimizableOperator) {
         OptimizableOperator<?, T> optimSource = (OptimizableOperator)source;
         this.optimizableOperator = optimSource;
      } else {
         this.optimizableOperator = null;
      }

      if (history <= 0) {
         throw new IllegalArgumentException("History cannot be zero or negative : " + history);
      } else {
         this.history = history;
         if (scheduler != null && ttl < 0L) {
            throw new IllegalArgumentException("TTL cannot be negative : " + ttl);
         } else {
            this.ttl = ttl;
            this.scheduler = scheduler;
         }
      }
   }

   @Override
   public int getPrefetch() {
      return this.history;
   }

   FluxReplay.ReplaySubscriber<T> newState() {
      if (this.scheduler != null) {
         return new FluxReplay.ReplaySubscriber<>(new FluxReplay.SizeAndTimeBoundReplayBuffer<>(this.history, this.ttl, this.scheduler), this, this.history);
      } else {
         return this.history != Integer.MAX_VALUE
            ? new FluxReplay.ReplaySubscriber<>(new FluxReplay.SizeBoundReplayBuffer<>(this.history), this, this.history)
            : new FluxReplay.ReplaySubscriber<>(new FluxReplay.UnboundedReplayBuffer<>(Queues.SMALL_BUFFER_SIZE), this, Queues.SMALL_BUFFER_SIZE);
      }
   }

   @Override
   public void connect(Consumer<? super Disposable> cancelSupport) {
      while(true) {
         FluxReplay.ReplaySubscriber<T> s = this.connection;
         if (s == null) {
            FluxReplay.ReplaySubscriber<T> u = this.newState();
            if (!CONNECTION.compareAndSet(this, null, u)) {
               continue;
            }

            s = u;
         }

         boolean doConnect = s.tryConnect();
         cancelSupport.accept(s);
         if (doConnect) {
            try {
               this.source.subscribe(s);
            } catch (Throwable var5) {
               Operators.reportThrowInSubscribe(s, var5);
            }
         }

         return;
      }
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      try {
         CoreSubscriber nextSubscriber = this.subscribeOrReturn(actual);
         if (nextSubscriber == null) {
            return;
         }

         this.source.subscribe(nextSubscriber);
      } catch (Throwable var3) {
         Operators.error(actual, Operators.onOperatorError(var3, actual.currentContext()));
      }

   }

   @Override
   public final CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) throws Throwable {
      while(true) {
         FluxReplay.ReplaySubscriber<T> c = this.connection;
         boolean expired = this.scheduler != null && c != null && c.buffer.isExpired();
         if (c == null || expired) {
            FluxReplay.ReplaySubscriber<T> u = this.newState();
            if (!CONNECTION.compareAndSet(this, c, u)) {
               continue;
            }

            c = u;
         }

         FluxReplay.ReplayInner<T> inner = new FluxReplay.ReplayInner<>(actual, c);
         actual.onSubscribe(inner);
         c.add(inner);
         if (inner.isCancelled()) {
            c.remove(inner);
            return null;
         }

         c.buffer.replay(inner);
         if (expired) {
            return c;
         }

         return null;
      }
   }

   @Override
   public final CorePublisher<? extends T> source() {
      return this.source;
   }

   @Override
   public final OptimizableOperator<?, ? extends T> nextOptimizableSource() {
      return this.optimizableOperator;
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PREFETCH) {
         return this.getPrefetch();
      } else if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else if (key == Scannable.Attr.RUN_ON) {
         return this.scheduler;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   interface ReplayBuffer<T> {
      void add(T var1);

      void onError(Throwable var1);

      @Nullable
      Throwable getError();

      void onComplete();

      void replay(FluxReplay.ReplaySubscription<T> var1);

      boolean isDone();

      @Nullable
      T poll(FluxReplay.ReplaySubscription<T> var1);

      void clear(FluxReplay.ReplaySubscription<T> var1);

      boolean isEmpty(FluxReplay.ReplaySubscription<T> var1);

      int size(FluxReplay.ReplaySubscription<T> var1);

      int size();

      int capacity();

      boolean isExpired();
   }

   static final class ReplayInner<T> implements FluxReplay.ReplaySubscription<T> {
      final CoreSubscriber<? super T> actual;
      final FluxReplay.ReplaySubscriber<T> parent;
      int index;
      int tailIndex;
      Object node;
      int fusionMode;
      long totalRequested;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxReplay.ReplayInner> WIP = AtomicIntegerFieldUpdater.newUpdater(FluxReplay.ReplayInner.class, "wip");
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxReplay.ReplayInner> REQUESTED = AtomicLongFieldUpdater.newUpdater(FluxReplay.ReplayInner.class, "requested");

      ReplayInner(CoreSubscriber<? super T> actual, FluxReplay.ReplaySubscriber<T> parent) {
         this.actual = actual;
         this.parent = parent;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n) && Operators.addCapCancellable(REQUESTED, this, n) != Long.MIN_VALUE) {
            this.totalRequested = Operators.addCap(this.totalRequested, n);
            this.parent.buffer.replay(this);
         }

      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.parent;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.parent.isTerminated();
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.size();
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.isCancelled();
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return Math.max(0L, this.requested);
         } else {
            return key == Scannable.Attr.RUN_ON ? this.parent.parent.scheduler : FluxReplay.ReplaySubscription.super.scanUnsafe(key);
         }
      }

      @Override
      public void cancel() {
         if (REQUESTED.getAndSet(this, Long.MIN_VALUE) != Long.MIN_VALUE) {
            this.parent.remove(this);
            if (this.enter()) {
               this.node = null;
            }
         }

      }

      @Override
      public long requested() {
         return this.requested;
      }

      @Override
      public boolean isCancelled() {
         return this.requested == Long.MIN_VALUE;
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public int requestFusion(int requestedMode) {
         if ((requestedMode & 2) != 0) {
            this.fusionMode = 2;
            return 2;
         } else {
            return 0;
         }
      }

      @Nullable
      public T poll() {
         return this.parent.buffer.poll(this);
      }

      public void clear() {
         this.parent.buffer.clear(this);
      }

      public boolean isEmpty() {
         return this.parent.buffer.isEmpty(this);
      }

      public int size() {
         return this.parent.buffer.size(this);
      }

      @Override
      public void node(@Nullable Object node) {
         this.node = node;
      }

      @Override
      public int fusionMode() {
         return this.fusionMode;
      }

      @Nullable
      @Override
      public Object node() {
         return this.node;
      }

      @Override
      public int index() {
         return this.index;
      }

      @Override
      public void index(int index) {
         this.index = index;
      }

      @Override
      public void requestMore(int index) {
         this.index = index;
         long previousState = FluxReplay.ReplaySubscriber.markWorkAdded(this.parent);
         if (!FluxReplay.ReplaySubscriber.isDisposed(previousState)) {
            if (!FluxReplay.ReplaySubscriber.isWorkInProgress(previousState)) {
               this.parent.manageRequest(previousState + 1L);
            }
         }
      }

      @Override
      public int tailIndex() {
         return this.tailIndex;
      }

      @Override
      public void tailIndex(int tailIndex) {
         this.tailIndex = tailIndex;
      }

      @Override
      public boolean enter() {
         return WIP.getAndIncrement(this) == 0;
      }

      @Override
      public int leave(int missed) {
         return WIP.addAndGet(this, -missed);
      }

      @Override
      public void produced(long n) {
         REQUESTED.addAndGet(this, -n);
      }
   }

   static final class ReplaySubscriber<T> implements InnerConsumer<T>, Disposable {
      final FluxReplay<T> parent;
      final FluxReplay.ReplayBuffer<T> buffer;
      final long prefetch;
      final int limit;
      Subscription s;
      int produced;
      int nextPrefetchIndex;
      volatile FluxReplay.ReplaySubscription<T>[] subscribers;
      volatile long state;
      static final AtomicLongFieldUpdater<FluxReplay.ReplaySubscriber> STATE = AtomicLongFieldUpdater.newUpdater(FluxReplay.ReplaySubscriber.class, "state");
      static final FluxReplay.ReplaySubscription[] EMPTY = new FluxReplay.ReplaySubscription[0];
      static final FluxReplay.ReplaySubscription[] TERMINATED = new FluxReplay.ReplaySubscription[0];
      static final long CONNECTED_FLAG = 1152921504606846976L;
      static final long SUBSCRIBED_FLAG = 2305843009213693952L;
      static final long DISPOSED_FLAG = Long.MIN_VALUE;
      static final long WORK_IN_PROGRESS_MAX_VALUE = 1152921504606846975L;

      ReplaySubscriber(FluxReplay.ReplayBuffer<T> buffer, FluxReplay<T> parent, int prefetch) {
         this.buffer = buffer;
         this.parent = parent;
         this.subscribers = EMPTY;
         this.prefetch = Operators.unboundedOrPrefetch(prefetch);
         this.limit = Operators.unboundedOrLimit(prefetch);
         this.nextPrefetchIndex = this.limit;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (this.buffer.isDone()) {
            s.cancel();
         } else {
            if (Operators.validate(this.s, s)) {
               this.s = s;
               long previousState = markSubscribed(this);
               if (isDisposed(previousState)) {
                  s.cancel();
                  return;
               }

               s.request(this.prefetch);
            }

         }
      }

      void manageRequest(long currentState) {
         Subscription p = this.s;

         do {
            int nextPrefetchIndex = this.nextPrefetchIndex;
            FluxReplay.ReplaySubscription<T>[] subscribers = this.subscribers;
            boolean shouldPrefetch;
            if (subscribers.length > 0) {
               shouldPrefetch = true;

               for(FluxReplay.ReplaySubscription<T> rp : subscribers) {
                  if (rp.index() < nextPrefetchIndex) {
                     shouldPrefetch = false;
                     break;
                  }
               }
            } else {
               shouldPrefetch = this.produced >= nextPrefetchIndex;
            }

            if (shouldPrefetch) {
               int limit = this.limit;
               this.nextPrefetchIndex = nextPrefetchIndex + limit;
               p.request((long)limit);
            }

            currentState = markWorkDone(this, currentState);
            if (isDisposed(currentState)) {
               return;
            }
         } while(isWorkInProgress(currentState));

      }

      @Override
      public void onNext(T t) {
         FluxReplay.ReplayBuffer<T> b = this.buffer;
         if (b.isDone()) {
            Operators.onNextDropped(t, this.currentContext());
         } else {
            ++this.produced;
            b.add(t);
            FluxReplay.ReplaySubscription<T>[] subscribers = this.subscribers;
            if (subscribers.length == 0) {
               if (this.produced % this.limit == 0) {
                  long previousState = markWorkAdded(this);
                  if (isDisposed(previousState)) {
                     return;
                  }

                  if (isWorkInProgress(previousState)) {
                     return;
                  }

                  this.manageRequest(previousState + 1L);
               }

            } else {
               for(FluxReplay.ReplaySubscription<T> rs : subscribers) {
                  b.replay(rs);
               }

            }
         }
      }

      @Override
      public void onError(Throwable t) {
         FluxReplay.ReplayBuffer<T> b = this.buffer;
         if (b.isDone()) {
            Operators.onErrorDropped(t, this.currentContext());
         } else {
            b.onError(t);

            for(FluxReplay.ReplaySubscription<T> rs : this.terminate()) {
               b.replay(rs);
            }
         }

      }

      @Override
      public void onComplete() {
         FluxReplay.ReplayBuffer<T> b = this.buffer;
         if (!b.isDone()) {
            b.onComplete();

            for(FluxReplay.ReplaySubscription<T> rs : this.terminate()) {
               b.replay(rs);
            }
         }

      }

      @Override
      public void dispose() {
         long previousState = markDisposed(this);
         if (!isDisposed(previousState)) {
            if (isSubscribed(previousState)) {
               this.s.cancel();
            }

            FluxReplay.CONNECTION.lazySet(this.parent, null);
            CancellationException ex = new CancellationException("Disconnected");
            FluxReplay.ReplayBuffer<T> buffer = this.buffer;
            buffer.onError(ex);

            for(FluxReplay.ReplaySubscription<T> inner : this.terminate()) {
               buffer.replay(inner);
            }

         }
      }

      boolean add(FluxReplay.ReplayInner<T> inner) {
         if (this.subscribers == TERMINATED) {
            return false;
         } else {
            synchronized(this) {
               FluxReplay.ReplaySubscription<T>[] a = this.subscribers;
               if (a == TERMINATED) {
                  return false;
               } else {
                  int n = a.length;
                  FluxReplay.ReplayInner<T>[] b = new FluxReplay.ReplayInner[n + 1];
                  System.arraycopy(a, 0, b, 0, n);
                  b[n] = inner;
                  this.subscribers = b;
                  return true;
               }
            }
         }
      }

      void remove(FluxReplay.ReplaySubscription<T> inner) {
         FluxReplay.ReplaySubscription<T>[] a = this.subscribers;
         if (a != TERMINATED && a != EMPTY) {
            synchronized(this) {
               a = this.subscribers;
               if (a != TERMINATED && a != EMPTY) {
                  int j = -1;
                  int n = a.length;

                  for(int i = 0; i < n; ++i) {
                     if (a[i] == inner) {
                        j = i;
                        break;
                     }
                  }

                  if (j >= 0) {
                     FluxReplay.ReplaySubscription<T>[] b;
                     if (n == 1) {
                        b = EMPTY;
                     } else {
                        b = new FluxReplay.ReplayInner[n - 1];
                        System.arraycopy(a, 0, b, 0, j);
                        System.arraycopy(a, j + 1, b, j, n - j - 1);
                     }

                     this.subscribers = b;
                  }
               }
            }
         }
      }

      FluxReplay.ReplaySubscription<T>[] terminate() {
         FluxReplay.ReplaySubscription<T>[] a = this.subscribers;
         if (a == TERMINATED) {
            return a;
         } else {
            synchronized(this) {
               a = this.subscribers;
               if (a != TERMINATED) {
                  this.subscribers = TERMINATED;
               }

               return a;
            }
         }
      }

      boolean isTerminated() {
         return this.subscribers == TERMINATED;
      }

      boolean tryConnect() {
         return markConnected(this);
      }

      @Override
      public Context currentContext() {
         return Operators.multiSubscribersContext(this.subscribers);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.PREFETCH) {
            return Integer.MAX_VALUE;
         } else if (key == Scannable.Attr.CAPACITY) {
            return this.buffer.capacity();
         } else if (key == Scannable.Attr.ERROR) {
            return this.buffer.getError();
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.buffer.size();
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.isTerminated();
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.isDisposed();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.subscribers);
      }

      @Override
      public boolean isDisposed() {
         return isDisposed(this.state);
      }

      static boolean markConnected(FluxReplay.ReplaySubscriber<?> instance) {
         long state;
         do {
            state = instance.state;
            if (isConnected(state)) {
               return false;
            }
         } while(!STATE.compareAndSet(instance, state, state | 1152921504606846976L));

         return true;
      }

      static long markSubscribed(FluxReplay.ReplaySubscriber<?> instance) {
         long state;
         do {
            state = instance.state;
            if (isDisposed(state)) {
               return state;
            }
         } while(!STATE.compareAndSet(instance, state, state | 2305843009213693952L));

         return state;
      }

      static long markWorkAdded(FluxReplay.ReplaySubscriber<?> instance) {
         long state;
         do {
            state = instance.state;
            if (isDisposed(state)) {
               return state;
            }

            if ((state & 1152921504606846975L) == 1152921504606846975L) {
               return state;
            }
         } while(!STATE.compareAndSet(instance, state, state + 1L));

         return state;
      }

      static long markWorkDone(FluxReplay.ReplaySubscriber<?> instance, long currentState) {
         long state;
         long nextState;
         do {
            state = instance.state;
            if (currentState != state) {
               return state;
            }

            nextState = state & -1152921504606846976L;
         } while(!STATE.compareAndSet(instance, state, nextState));

         return nextState;
      }

      static long markDisposed(FluxReplay.ReplaySubscriber<?> instance) {
         long state;
         do {
            state = instance.state;
            if (isDisposed(state)) {
               return state;
            }
         } while(!STATE.compareAndSet(instance, state, state | Long.MIN_VALUE));

         return state;
      }

      static boolean isConnected(long state) {
         return (state & 1152921504606846976L) == 1152921504606846976L;
      }

      static boolean isSubscribed(long state) {
         return (state & 2305843009213693952L) == 2305843009213693952L;
      }

      static boolean isWorkInProgress(long state) {
         return (state & 1152921504606846975L) > 0L;
      }

      static boolean isDisposed(long state) {
         return (state & Long.MIN_VALUE) == Long.MIN_VALUE;
      }
   }

   interface ReplaySubscription<T> extends Fuseable.QueueSubscription<T>, InnerProducer<T> {
      @Override
      CoreSubscriber<? super T> actual();

      boolean enter();

      int leave(int var1);

      void produced(long var1);

      void node(@Nullable Object var1);

      @Nullable
      Object node();

      int tailIndex();

      void tailIndex(int var1);

      int index();

      void index(int var1);

      int fusionMode();

      boolean isCancelled();

      long requested();

      void requestMore(int var1);
   }

   static final class SizeAndTimeBoundReplayBuffer<T> implements FluxReplay.ReplayBuffer<T> {
      final int limit;
      final int indexUpdateLimit;
      final long maxAge;
      final Scheduler scheduler;
      int size;
      volatile FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> head;
      FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> tail;
      Throwable error;
      static final long NOT_DONE = Long.MIN_VALUE;
      volatile long done = Long.MIN_VALUE;

      SizeAndTimeBoundReplayBuffer(int limit, long maxAge, Scheduler scheduler) {
         this.limit = limit;
         this.indexUpdateLimit = Operators.unboundedOrLimit(limit);
         this.maxAge = maxAge;
         this.scheduler = scheduler;
         FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> h = new FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<>(-1, (T)null, 0L);
         this.tail = h;
         this.head = h;
      }

      @Override
      public boolean isExpired() {
         long done = this.done;
         return done != Long.MIN_VALUE && this.scheduler.now(TimeUnit.NANOSECONDS) - this.maxAge > done;
      }

      void replayNormal(FluxReplay.ReplaySubscription<T> rs) {
         int missed = 1;
         Subscriber<? super T> a = rs.actual();

         do {
            FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> node = (FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode)rs.node();
            if (node == null) {
               node = this.head;
               if (this.done == Long.MIN_VALUE) {
                  long limit = this.scheduler.now(TimeUnit.NANOSECONDS) - this.maxAge;

                  for(FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> next = node;
                     next != null;
                     next = (FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode)next.get()
                  ) {
                     long ts = next.time;
                     if (ts > limit) {
                        break;
                     }

                     node = next;
                  }
               }
            }

            long r = rs.requested();
            long e = 0L;

            while(e != r) {
               if (rs.isCancelled()) {
                  rs.node(null);
                  return;
               }

               boolean d = this.done != Long.MIN_VALUE;
               FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> next = (FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode)node.get();
               boolean empty = next == null;
               if (d && empty) {
                  rs.node(null);
                  Throwable ex = this.error;
                  if (ex != null) {
                     a.onError(ex);
                  } else {
                     a.onComplete();
                  }

                  return;
               }

               if (empty) {
                  break;
               }

               a.onNext(next.value);
               ++e;
               node = next;
               if ((next.index + 1) % this.indexUpdateLimit == 0) {
                  rs.requestMore(next.index + 1);
               }
            }

            if (e == r) {
               if (rs.isCancelled()) {
                  rs.node(null);
                  return;
               }

               boolean d = this.done != Long.MIN_VALUE;
               boolean empty = node.get() == null;
               if (d && empty) {
                  rs.node(null);
                  Throwable ex = this.error;
                  if (ex != null) {
                     a.onError(ex);
                  } else {
                     a.onComplete();
                  }

                  return;
               }
            }

            if (e != 0L && r != Long.MAX_VALUE) {
               rs.produced(e);
            }

            rs.node(node);
            missed = rs.leave(missed);
         } while(missed != 0);

      }

      void replayFused(FluxReplay.ReplaySubscription<T> rs) {
         int missed = 1;
         Subscriber<? super T> a = rs.actual();

         while(!rs.isCancelled()) {
            boolean d = this.done != Long.MIN_VALUE;
            a.onNext((T)null);
            if (d) {
               Throwable ex = this.error;
               if (ex != null) {
                  a.onError(ex);
               } else {
                  a.onComplete();
               }

               return;
            }

            missed = rs.leave(missed);
            if (missed == 0) {
               return;
            }
         }

         rs.node(null);
      }

      @Override
      public void onError(Throwable ex) {
         this.done = this.scheduler.now(TimeUnit.NANOSECONDS);
         this.error = ex;
      }

      @Nullable
      @Override
      public Throwable getError() {
         return this.error;
      }

      @Override
      public void onComplete() {
         this.done = this.scheduler.now(TimeUnit.NANOSECONDS);
      }

      @Override
      public boolean isDone() {
         return this.done != Long.MIN_VALUE;
      }

      FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> latestHead(FluxReplay.ReplaySubscription<T> rs) {
         long now = this.scheduler.now(TimeUnit.NANOSECONDS) - this.maxAge;
         FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> h = (FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode)rs.node();
         if (h == null) {
            h = this.head;
         }

         FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> n;
         while((n = (FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode)h.get()) != null && n.time <= now) {
            h = n;
         }

         return h;
      }

      @Nullable
      @Override
      public T poll(FluxReplay.ReplaySubscription<T> rs) {
         FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> node = this.latestHead(rs);

         FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> next;
         for(long now = this.scheduler.now(TimeUnit.NANOSECONDS) - this.maxAge;
            (next = (FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode)node.get()) != null;
            node = next
         ) {
            if (next.time > now) {
               node = next;
               break;
            }
         }

         if (next == null) {
            if (node.index != -1 && (node.index + 1) % this.indexUpdateLimit == 0) {
               rs.requestMore(node.index + 1);
            }

            return null;
         } else {
            rs.node(next);
            if ((next.index + 1) % this.indexUpdateLimit == 0) {
               rs.requestMore(next.index + 1);
            }

            return node.value;
         }
      }

      @Override
      public void clear(FluxReplay.ReplaySubscription<T> rs) {
         rs.node(null);
      }

      @Override
      public boolean isEmpty(FluxReplay.ReplaySubscription<T> rs) {
         FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> node = this.latestHead(rs);
         return node.get() == null;
      }

      @Override
      public int size(FluxReplay.ReplaySubscription<T> rs) {
         FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> node = this.latestHead(rs);

         int count;
         FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> next;
         for(count = 0; (next = (FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode)node.get()) != null && count != Integer.MAX_VALUE; node = next) {
            ++count;
         }

         return count;
      }

      @Override
      public int size() {
         FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> node = this.head;

         int count;
         FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> next;
         for(count = 0; (next = (FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode)node.get()) != null && count != Integer.MAX_VALUE; node = next) {
            ++count;
         }

         return count;
      }

      @Override
      public int capacity() {
         return this.limit;
      }

      @Override
      public void add(T value) {
         FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> tail = this.tail;
         FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> valueNode = new FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<>(
            tail.index + 1, value, this.scheduler.now(TimeUnit.NANOSECONDS)
         );
         tail.set(valueNode);
         this.tail = valueNode;
         int s = this.size;
         if (s == this.limit) {
            this.head = (FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode)this.head.get();
         } else {
            this.size = s + 1;
         }

         long limit = this.scheduler.now(TimeUnit.NANOSECONDS) - this.maxAge;
         if (this.maxAge == 0L) {
            this.head = valueNode;
         } else {
            FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> h = this.head;
            int removed = 0;

            while(true) {
               FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T> next = (FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode)h.get();
               if (next == null) {
                  break;
               }

               if (next.time > limit || next == valueNode) {
                  if (removed != 0) {
                     this.size -= removed;
                     this.head = h;
                  }
                  break;
               }

               h = next;
               ++removed;
            }

         }
      }

      @Override
      public void replay(FluxReplay.ReplaySubscription<T> rs) {
         if (rs.enter()) {
            if (rs.fusionMode() == 0) {
               this.replayNormal(rs);
            } else {
               this.replayFused(rs);
            }

         }
      }

      static final class TimedNode<T> extends AtomicReference<FluxReplay.SizeAndTimeBoundReplayBuffer.TimedNode<T>> {
         final int index;
         final T value;
         final long time;

         TimedNode(int index, @Nullable T value, long time) {
            this.index = index;
            this.value = value;
            this.time = time;
         }

         public String toString() {
            return "TimedNode{index=" + this.index + ", value=" + this.value + ", time=" + this.time + '}';
         }
      }
   }

   static final class SizeBoundReplayBuffer<T> implements FluxReplay.ReplayBuffer<T> {
      final int limit;
      final int indexUpdateLimit;
      volatile FluxReplay.SizeBoundReplayBuffer.Node<T> head;
      FluxReplay.SizeBoundReplayBuffer.Node<T> tail;
      int size;
      volatile boolean done;
      Throwable error;

      SizeBoundReplayBuffer(int limit) {
         if (limit < 0) {
            throw new IllegalArgumentException("Limit cannot be negative");
         } else {
            this.limit = limit;
            this.indexUpdateLimit = Operators.unboundedOrLimit(limit);
            FluxReplay.SizeBoundReplayBuffer.Node<T> n = new FluxReplay.SizeBoundReplayBuffer.Node<>(-1, (T)null);
            this.tail = n;
            this.head = n;
         }
      }

      @Override
      public boolean isExpired() {
         return false;
      }

      @Override
      public int capacity() {
         return this.limit;
      }

      @Override
      public void add(T value) {
         FluxReplay.SizeBoundReplayBuffer.Node<T> tail = this.tail;
         FluxReplay.SizeBoundReplayBuffer.Node<T> n = new FluxReplay.SizeBoundReplayBuffer.Node<>(tail.index + 1, value);
         tail.set(n);
         this.tail = n;
         int s = this.size;
         if (s == this.limit) {
            this.head = (FluxReplay.SizeBoundReplayBuffer.Node)this.head.get();
         } else {
            this.size = s + 1;
         }

      }

      @Override
      public void onError(Throwable ex) {
         this.error = ex;
         this.done = true;
      }

      @Override
      public void onComplete() {
         this.done = true;
      }

      void replayNormal(FluxReplay.ReplaySubscription<T> rs) {
         Subscriber<? super T> a = rs.actual();
         int missed = 1;

         do {
            long r = rs.requested();
            long e = 0L;
            FluxReplay.SizeBoundReplayBuffer.Node<T> node = (FluxReplay.SizeBoundReplayBuffer.Node)rs.node();
            if (node == null) {
               node = this.head;
            }

            while(e != r) {
               if (rs.isCancelled()) {
                  rs.node(null);
                  return;
               }

               boolean d = this.done;
               FluxReplay.SizeBoundReplayBuffer.Node<T> next = (FluxReplay.SizeBoundReplayBuffer.Node)node.get();
               boolean empty = next == null;
               if (d && empty) {
                  rs.node(null);
                  Throwable ex = this.error;
                  if (ex != null) {
                     a.onError(ex);
                  } else {
                     a.onComplete();
                  }

                  return;
               }

               if (empty) {
                  break;
               }

               a.onNext(next.value);
               ++e;
               node = next;
               if ((next.index + 1) % this.indexUpdateLimit == 0) {
                  rs.requestMore(next.index + 1);
               }
            }

            if (e == r) {
               if (rs.isCancelled()) {
                  rs.node(null);
                  return;
               }

               boolean d = this.done;
               boolean empty = node.get() == null;
               if (d && empty) {
                  rs.node(null);
                  Throwable ex = this.error;
                  if (ex != null) {
                     a.onError(ex);
                  } else {
                     a.onComplete();
                  }

                  return;
               }
            }

            if (e != 0L && r != Long.MAX_VALUE) {
               rs.produced(e);
            }

            rs.node(node);
            missed = rs.leave(missed);
         } while(missed != 0);

      }

      void replayFused(FluxReplay.ReplaySubscription<T> rs) {
         int missed = 1;
         Subscriber<? super T> a = rs.actual();

         while(!rs.isCancelled()) {
            boolean d = this.done;
            a.onNext((T)null);
            if (d) {
               Throwable ex = this.error;
               if (ex != null) {
                  a.onError(ex);
               } else {
                  a.onComplete();
               }

               return;
            }

            missed = rs.leave(missed);
            if (missed == 0) {
               return;
            }
         }

         rs.node(null);
      }

      @Override
      public void replay(FluxReplay.ReplaySubscription<T> rs) {
         if (rs.enter()) {
            if (rs.fusionMode() == 0) {
               this.replayNormal(rs);
            } else {
               this.replayFused(rs);
            }

         }
      }

      @Nullable
      @Override
      public Throwable getError() {
         return this.error;
      }

      @Override
      public boolean isDone() {
         return this.done;
      }

      @Nullable
      @Override
      public T poll(FluxReplay.ReplaySubscription<T> rs) {
         FluxReplay.SizeBoundReplayBuffer.Node<T> node = (FluxReplay.SizeBoundReplayBuffer.Node)rs.node();
         if (node == null) {
            node = this.head;
            rs.node(node);
         }

         FluxReplay.SizeBoundReplayBuffer.Node<T> next = (FluxReplay.SizeBoundReplayBuffer.Node)node.get();
         if (next == null) {
            return null;
         } else {
            rs.node(next);
            if ((next.index + 1) % this.indexUpdateLimit == 0) {
               rs.requestMore(next.index + 1);
            }

            return next.value;
         }
      }

      @Override
      public void clear(FluxReplay.ReplaySubscription<T> rs) {
         rs.node(null);
      }

      @Override
      public boolean isEmpty(FluxReplay.ReplaySubscription<T> rs) {
         FluxReplay.SizeBoundReplayBuffer.Node<T> node = (FluxReplay.SizeBoundReplayBuffer.Node)rs.node();
         if (node == null) {
            node = this.head;
            rs.node(node);
         }

         return node.get() == null;
      }

      @Override
      public int size(FluxReplay.ReplaySubscription<T> rs) {
         FluxReplay.SizeBoundReplayBuffer.Node<T> node = (FluxReplay.SizeBoundReplayBuffer.Node)rs.node();
         if (node == null) {
            node = this.head;
         }

         int count;
         FluxReplay.SizeBoundReplayBuffer.Node<T> next;
         for(count = 0; (next = (FluxReplay.SizeBoundReplayBuffer.Node)node.get()) != null && count != Integer.MAX_VALUE; node = next) {
            ++count;
         }

         return count;
      }

      @Override
      public int size() {
         FluxReplay.SizeBoundReplayBuffer.Node<T> node = this.head;

         int count;
         FluxReplay.SizeBoundReplayBuffer.Node<T> next;
         for(count = 0; (next = (FluxReplay.SizeBoundReplayBuffer.Node)node.get()) != null && count != Integer.MAX_VALUE; node = next) {
            ++count;
         }

         return count;
      }

      static final class Node<T> extends AtomicReference<FluxReplay.SizeBoundReplayBuffer.Node<T>> {
         private static final long serialVersionUID = 3713592843205853725L;
         final int index;
         final T value;

         Node(int index, @Nullable T value) {
            this.index = index;
            this.value = value;
         }

         public String toString() {
            return "Node(" + this.value + ")";
         }
      }
   }

   static final class UnboundedReplayBuffer<T> implements FluxReplay.ReplayBuffer<T> {
      final int batchSize;
      final int indexUpdateLimit;
      volatile int size;
      final Object[] head;
      Object[] tail;
      int tailIndex;
      volatile boolean done;
      Throwable error;

      UnboundedReplayBuffer(int batchSize) {
         this.batchSize = batchSize;
         this.indexUpdateLimit = Operators.unboundedOrLimit(batchSize);
         Object[] n = new Object[batchSize + 1];
         this.tail = n;
         this.head = n;
      }

      @Override
      public boolean isExpired() {
         return false;
      }

      @Nullable
      @Override
      public Throwable getError() {
         return this.error;
      }

      @Override
      public int capacity() {
         return Integer.MAX_VALUE;
      }

      @Override
      public void add(T value) {
         int i = this.tailIndex;
         Object[] a = this.tail;
         if (i == a.length - 1) {
            Object[] b = new Object[a.length];
            b[0] = value;
            this.tailIndex = 1;
            a[i] = b;
            this.tail = b;
         } else {
            a[i] = value;
            this.tailIndex = i + 1;
         }

         ++this.size;
      }

      @Override
      public void onError(Throwable ex) {
         this.error = ex;
         this.done = true;
      }

      @Override
      public void onComplete() {
         this.done = true;
      }

      void replayNormal(FluxReplay.ReplaySubscription<T> rs) {
         int missed = 1;
         Subscriber<? super T> a = rs.actual();
         int n = this.batchSize;

         do {
            long r = rs.requested();
            long e = 0L;
            Object[] node = rs.node();
            if (node == null) {
               node = this.head;
            }

            int tailIndex = rs.tailIndex();
            int index = rs.index();

            while(e != r) {
               if (rs.isCancelled()) {
                  rs.node(null);
                  return;
               }

               boolean d = this.done;
               boolean empty = index == this.size;
               if (d && empty) {
                  rs.node(null);
                  Throwable ex = this.error;
                  if (ex != null) {
                     a.onError(ex);
                  } else {
                     a.onComplete();
                  }

                  return;
               }

               if (empty) {
                  break;
               }

               if (tailIndex == n) {
                  node = node[tailIndex];
                  tailIndex = 0;
               }

               T v = (T)node[tailIndex];
               a.onNext(v);
               ++e;
               ++tailIndex;
               ++index;
               if (index % this.indexUpdateLimit == 0) {
                  rs.requestMore(index);
               }
            }

            if (e == r) {
               if (rs.isCancelled()) {
                  rs.node(null);
                  return;
               }

               boolean d = this.done;
               boolean empty = index == this.size;
               if (d && empty) {
                  rs.node(null);
                  Throwable ex = this.error;
                  if (ex != null) {
                     a.onError(ex);
                  } else {
                     a.onComplete();
                  }

                  return;
               }
            }

            if (e != 0L && r != Long.MAX_VALUE) {
               rs.produced(e);
            }

            rs.index(index);
            rs.tailIndex(tailIndex);
            rs.node(node);
            missed = rs.leave(missed);
         } while(missed != 0);

      }

      void replayFused(FluxReplay.ReplaySubscription<T> rs) {
         int missed = 1;
         Subscriber<? super T> a = rs.actual();

         while(!rs.isCancelled()) {
            boolean d = this.done;
            a.onNext((T)null);
            if (d) {
               Throwable ex = this.error;
               if (ex != null) {
                  a.onError(ex);
               } else {
                  a.onComplete();
               }

               return;
            }

            missed = rs.leave(missed);
            if (missed == 0) {
               return;
            }
         }

         rs.node(null);
      }

      @Override
      public void replay(FluxReplay.ReplaySubscription<T> rs) {
         if (rs.enter()) {
            if (rs.fusionMode() == 0) {
               this.replayNormal(rs);
            } else {
               this.replayFused(rs);
            }

         }
      }

      @Override
      public boolean isDone() {
         return this.done;
      }

      @Nullable
      @Override
      public T poll(FluxReplay.ReplaySubscription<T> rs) {
         int index = rs.index();
         if (index == this.size) {
            return null;
         } else {
            Object[] node = rs.node();
            if (node == null) {
               node = this.head;
               rs.node(node);
            }

            int tailIndex = rs.tailIndex();
            if (tailIndex == this.batchSize) {
               node = node[tailIndex];
               tailIndex = 0;
               rs.node(node);
            }

            T v = (T)node[tailIndex];
            rs.tailIndex(tailIndex + 1);
            if ((index + 1) % this.indexUpdateLimit == 0) {
               rs.requestMore(index + 1);
            } else {
               rs.index(index + 1);
            }

            return v;
         }
      }

      @Override
      public void clear(FluxReplay.ReplaySubscription<T> rs) {
         rs.node(null);
      }

      @Override
      public boolean isEmpty(FluxReplay.ReplaySubscription<T> rs) {
         return rs.index() == this.size;
      }

      @Override
      public int size(FluxReplay.ReplaySubscription<T> rs) {
         return this.size - rs.index();
      }

      @Override
      public int size() {
         return this.size;
      }
   }
}
