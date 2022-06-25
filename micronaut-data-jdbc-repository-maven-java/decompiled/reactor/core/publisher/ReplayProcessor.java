package reactor.core.publisher;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.stream.Stream;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.annotation.Nullable;
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;

@Deprecated
public final class ReplayProcessor<T> extends FluxProcessor<T, T> implements Fuseable, InternalManySink<T> {
   final FluxReplay.ReplayBuffer<T> buffer;
   Subscription subscription;
   volatile FluxReplay.ReplaySubscription<T>[] subscribers;
   static final AtomicReferenceFieldUpdater<ReplayProcessor, FluxReplay.ReplaySubscription[]> SUBSCRIBERS = AtomicReferenceFieldUpdater.newUpdater(
      ReplayProcessor.class, FluxReplay.ReplaySubscription[].class, "subscribers"
   );

   @Deprecated
   public static <T> ReplayProcessor<T> cacheLast() {
      return cacheLastOrDefault((T)null);
   }

   @Deprecated
   public static <T> ReplayProcessor<T> cacheLastOrDefault(@Nullable T value) {
      ReplayProcessor<T> b = create(1);
      if (value != null) {
         b.onNext(value);
      }

      return b;
   }

   @Deprecated
   public static <E> ReplayProcessor<E> create() {
      return create(Queues.SMALL_BUFFER_SIZE, true);
   }

   @Deprecated
   public static <E> ReplayProcessor<E> create(int historySize) {
      return create(historySize, false);
   }

   @Deprecated
   public static <E> ReplayProcessor<E> create(int historySize, boolean unbounded) {
      FluxReplay.ReplayBuffer<E> buffer;
      if (unbounded) {
         buffer = new FluxReplay.UnboundedReplayBuffer<>(historySize);
      } else {
         buffer = new FluxReplay.SizeBoundReplayBuffer<>(historySize);
      }

      return new ReplayProcessor<>(buffer);
   }

   @Deprecated
   public static <T> ReplayProcessor<T> createTimeout(Duration maxAge) {
      return createTimeout(maxAge, Schedulers.parallel());
   }

   @Deprecated
   public static <T> ReplayProcessor<T> createTimeout(Duration maxAge, Scheduler scheduler) {
      return createSizeAndTimeout(Integer.MAX_VALUE, maxAge, scheduler);
   }

   @Deprecated
   public static <T> ReplayProcessor<T> createSizeAndTimeout(int size, Duration maxAge) {
      return createSizeAndTimeout(size, maxAge, Schedulers.parallel());
   }

   @Deprecated
   public static <T> ReplayProcessor<T> createSizeAndTimeout(int size, Duration maxAge, Scheduler scheduler) {
      Objects.requireNonNull(scheduler, "scheduler is null");
      if (size <= 0) {
         throw new IllegalArgumentException("size > 0 required but it was " + size);
      } else {
         return new ReplayProcessor<>(new FluxReplay.SizeAndTimeBoundReplayBuffer<>(size, maxAge.toNanos(), scheduler));
      }
   }

   ReplayProcessor(FluxReplay.ReplayBuffer<T> buffer) {
      this.buffer = buffer;
      SUBSCRIBERS.lazySet(this, FluxReplay.ReplaySubscriber.EMPTY);
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      Objects.requireNonNull(actual, "subscribe");
      FluxReplay.ReplaySubscription<T> rs = new ReplayProcessor.ReplayInner<>(actual, this);
      actual.onSubscribe(rs);
      if (this.add(rs) && rs.isCancelled()) {
         this.remove(rs);
      } else {
         this.buffer.replay(rs);
      }
   }

   @Nullable
   @Override
   public Throwable getError() {
      return this.buffer.getError();
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return this.subscription;
      } else {
         return key == Scannable.Attr.CAPACITY ? this.buffer.capacity() : super.scanUnsafe(key);
      }
   }

   @Override
   public Stream<? extends Scannable> inners() {
      return Stream.of(this.subscribers);
   }

   @Override
   public long downstreamCount() {
      return (long)this.subscribers.length;
   }

   @Override
   public boolean isTerminated() {
      return this.buffer.isDone();
   }

   boolean add(FluxReplay.ReplaySubscription<T> rs) {
      FluxReplay.ReplaySubscription<T>[] a;
      FluxReplay.ReplaySubscription<T>[] b;
      do {
         a = this.subscribers;
         if (a == FluxReplay.ReplaySubscriber.TERMINATED) {
            return false;
         }

         int n = a.length;
         b = new ReplayProcessor.ReplayInner[n + 1];
         System.arraycopy(a, 0, b, 0, n);
         b[n] = rs;
      } while(!SUBSCRIBERS.compareAndSet(this, a, b));

      return true;
   }

   void remove(FluxReplay.ReplaySubscription<T> rs) {
      label31:
      while(true) {
         FluxReplay.ReplaySubscription<T>[] a = this.subscribers;
         if (a != FluxReplay.ReplaySubscriber.TERMINATED && a != FluxReplay.ReplaySubscriber.EMPTY) {
            int n = a.length;

            for(int i = 0; i < n; ++i) {
               if (a[i] == rs) {
                  FluxReplay.ReplaySubscription<T>[] b;
                  if (n == 1) {
                     b = FluxReplay.ReplaySubscriber.EMPTY;
                  } else {
                     b = new ReplayProcessor.ReplayInner[n - 1];
                     System.arraycopy(a, 0, b, 0, i);
                     System.arraycopy(a, i + 1, b, i, n - i - 1);
                  }

                  if (!SUBSCRIBERS.compareAndSet(this, a, b)) {
                     continue label31;
                  }

                  return;
               }
            }

            return;
         }

         return;
      }
   }

   @Override
   public void onSubscribe(Subscription s) {
      if (this.buffer.isDone()) {
         s.cancel();
      } else if (Operators.validate(this.subscription, s)) {
         this.subscription = s;
         s.request(Long.MAX_VALUE);
      }

   }

   @Override
   public Context currentContext() {
      return Operators.multiSubscribersContext(this.subscribers);
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Override
   public void onComplete() {
      Sinks.EmitResult emitResult = this.tryEmitComplete();
   }

   @Override
   public Sinks.EmitResult tryEmitComplete() {
      FluxReplay.ReplayBuffer<T> b = this.buffer;
      if (b.isDone()) {
         return Sinks.EmitResult.FAIL_TERMINATED;
      } else {
         b.onComplete();
         FluxReplay.ReplaySubscription<T>[] a = (FluxReplay.ReplaySubscription[])SUBSCRIBERS.getAndSet(this, FluxReplay.ReplaySubscriber.TERMINATED);

         for(FluxReplay.ReplaySubscription<T> rs : a) {
            b.replay(rs);
         }

         return Sinks.EmitResult.OK;
      }
   }

   @Override
   public void onError(Throwable throwable) {
      this.emitError(throwable, Sinks.EmitFailureHandler.FAIL_FAST);
   }

   @Override
   public Sinks.EmitResult tryEmitError(Throwable t) {
      FluxReplay.ReplayBuffer<T> b = this.buffer;
      if (b.isDone()) {
         return Sinks.EmitResult.FAIL_TERMINATED;
      } else {
         b.onError(t);
         FluxReplay.ReplaySubscription<T>[] a = (FluxReplay.ReplaySubscription[])SUBSCRIBERS.getAndSet(this, FluxReplay.ReplaySubscriber.TERMINATED);

         for(FluxReplay.ReplaySubscription<T> rs : a) {
            b.replay(rs);
         }

         return Sinks.EmitResult.OK;
      }
   }

   @Override
   public void onNext(T t) {
      this.emitNext(t, Sinks.EmitFailureHandler.FAIL_FAST);
   }

   @Override
   public Sinks.EmitResult tryEmitNext(T t) {
      FluxReplay.ReplayBuffer<T> b = this.buffer;
      if (b.isDone()) {
         return Sinks.EmitResult.FAIL_TERMINATED;
      } else {
         b.add(t);

         for(FluxReplay.ReplaySubscription<T> rs : this.subscribers) {
            b.replay(rs);
         }

         return Sinks.EmitResult.OK;
      }
   }

   @Override
   public int currentSubscriberCount() {
      return this.subscribers.length;
   }

   @Override
   public Flux<T> asFlux() {
      return this;
   }

   @Override
   protected boolean isIdentityProcessor() {
      return true;
   }

   static final class ReplayInner<T> implements FluxReplay.ReplaySubscription<T> {
      final CoreSubscriber<? super T> actual;
      final ReplayProcessor<T> parent;
      final FluxReplay.ReplayBuffer<T> buffer;
      int index;
      int tailIndex;
      Object node;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<ReplayProcessor.ReplayInner> WIP = AtomicIntegerFieldUpdater.newUpdater(ReplayProcessor.ReplayInner.class, "wip");
      volatile long requested;
      static final AtomicLongFieldUpdater<ReplayProcessor.ReplayInner> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         ReplayProcessor.ReplayInner.class, "requested"
      );
      int fusionMode;

      ReplayInner(CoreSubscriber<? super T> actual, ReplayProcessor<T> parent) {
         this.actual = actual;
         this.parent = parent;
         this.buffer = parent.buffer;
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
         return this.buffer.poll(this);
      }

      public void clear() {
         this.buffer.clear(this);
      }

      public boolean isEmpty() {
         return this.buffer.isEmpty(this);
      }

      public int size() {
         return this.buffer.size(this);
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            if (this.fusionMode() == 0) {
               Operators.addCapCancellable(REQUESTED, this, n);
            }

            this.buffer.replay(this);
         }

      }

      @Override
      public void requestMore(int index) {
         this.index = index;
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
}
