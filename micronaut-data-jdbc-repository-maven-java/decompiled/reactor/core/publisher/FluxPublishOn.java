package reactor.core.publisher;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.Supplier;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.Nullable;

final class FluxPublishOn<T> extends InternalFluxOperator<T, T> implements Fuseable {
   final Scheduler scheduler;
   final boolean delayError;
   final Supplier<? extends Queue<T>> queueSupplier;
   final int prefetch;
   final int lowTide;

   FluxPublishOn(Flux<? extends T> source, Scheduler scheduler, boolean delayError, int prefetch, int lowTide, Supplier<? extends Queue<T>> queueSupplier) {
      super(source);
      if (prefetch <= 0) {
         throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
      } else {
         this.scheduler = (Scheduler)Objects.requireNonNull(scheduler, "scheduler");
         this.delayError = delayError;
         this.prefetch = prefetch;
         this.lowTide = lowTide;
         this.queueSupplier = (Supplier)Objects.requireNonNull(queueSupplier, "queueSupplier");
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.RUN_ON) {
         return this.scheduler;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : super.scanUnsafe(key);
      }
   }

   @Override
   public int getPrefetch() {
      return this.prefetch;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      Scheduler.Worker worker = (Scheduler.Worker)Objects.requireNonNull(this.scheduler.createWorker(), "The scheduler returned a null worker");
      if (actual instanceof Fuseable.ConditionalSubscriber) {
         Fuseable.ConditionalSubscriber<? super T> cs = (Fuseable.ConditionalSubscriber)actual;
         this.source
            .subscribe(
               new FluxPublishOn.PublishOnConditionalSubscriber<>(cs, this.scheduler, worker, this.delayError, this.prefetch, this.lowTide, this.queueSupplier)
            );
         return null;
      } else {
         return new FluxPublishOn.PublishOnSubscriber<>(actual, this.scheduler, worker, this.delayError, this.prefetch, this.lowTide, this.queueSupplier);
      }
   }

   static final class PublishOnConditionalSubscriber<T> implements Fuseable.QueueSubscription<T>, Runnable, InnerOperator<T, T> {
      final Fuseable.ConditionalSubscriber<? super T> actual;
      final Scheduler.Worker worker;
      final Scheduler scheduler;
      final boolean delayError;
      final int prefetch;
      final int limit;
      final Supplier<? extends Queue<T>> queueSupplier;
      Subscription s;
      Queue<T> queue;
      volatile boolean cancelled;
      volatile boolean done;
      Throwable error;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxPublishOn.PublishOnConditionalSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxPublishOn.PublishOnConditionalSubscriber.class, "wip"
      );
      volatile int discardGuard;
      static final AtomicIntegerFieldUpdater<FluxPublishOn.PublishOnConditionalSubscriber> DISCARD_GUARD = AtomicIntegerFieldUpdater.newUpdater(
         FluxPublishOn.PublishOnConditionalSubscriber.class, "discardGuard"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxPublishOn.PublishOnConditionalSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxPublishOn.PublishOnConditionalSubscriber.class, "requested"
      );
      int sourceMode;
      long produced;
      long consumed;
      boolean outputFused;

      PublishOnConditionalSubscriber(
         Fuseable.ConditionalSubscriber<? super T> actual,
         Scheduler scheduler,
         Scheduler.Worker worker,
         boolean delayError,
         int prefetch,
         int lowTide,
         Supplier<? extends Queue<T>> queueSupplier
      ) {
         this.actual = actual;
         this.worker = worker;
         this.scheduler = scheduler;
         this.delayError = delayError;
         this.prefetch = prefetch;
         this.queueSupplier = queueSupplier;
         this.limit = Operators.unboundedOrLimit(prefetch, lowTide);
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            if (s instanceof Fuseable.QueueSubscription) {
               Fuseable.QueueSubscription<T> f = (Fuseable.QueueSubscription)s;
               int m = f.requestFusion(7);
               if (m == 1) {
                  this.sourceMode = 1;
                  this.queue = f;
                  this.done = true;
                  this.actual.onSubscribe(this);
                  return;
               }

               if (m == 2) {
                  this.sourceMode = 2;
                  this.queue = f;
                  this.actual.onSubscribe(this);
                  s.request(Operators.unboundedOrPrefetch(this.prefetch));
                  return;
               }
            }

            this.queue = (Queue)this.queueSupplier.get();
            this.actual.onSubscribe(this);
            s.request(Operators.unboundedOrPrefetch(this.prefetch));
         }

      }

      @Override
      public void onNext(T t) {
         if (this.sourceMode == 2) {
            this.trySchedule(this, null, null);
         } else if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else if (this.cancelled) {
            Operators.onDiscard(t, this.actual.currentContext());
         } else {
            if (!this.queue.offer(t)) {
               Operators.onDiscard(t, this.actual.currentContext());
               this.error = Operators.onOperatorError(
                  this.s, Exceptions.failWithOverflow("Queue is full: Reactive Streams source doesn't respect backpressure"), t, this.actual.currentContext()
               );
               this.done = true;
            }

            this.trySchedule(this, null, t);
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.error = t;
            this.done = true;
            this.trySchedule(null, t, null);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.trySchedule(null, null, null);
         }
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
            this.trySchedule(this, null, null);
         }

      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            this.s.cancel();
            this.worker.dispose();
            if (WIP.getAndIncrement(this) == 0) {
               if (this.sourceMode == 2) {
                  this.queue.clear();
               } else if (!this.outputFused) {
                  Operators.onDiscardQueueWithClear(this.queue, this.actual.currentContext(), null);
               }
            }

         }
      }

      void trySchedule(@Nullable Subscription subscription, @Nullable Throwable suppressed, @Nullable Object dataSignal) {
         if (WIP.getAndIncrement(this) != 0) {
            if (this.cancelled) {
               if (this.sourceMode == 2) {
                  this.queue.clear();
               } else {
                  Operators.onDiscard(dataSignal, this.actual.currentContext());
               }
            }

         } else {
            try {
               this.worker.schedule(this);
            } catch (RejectedExecutionException var5) {
               if (this.sourceMode == 2) {
                  this.queue.clear();
               } else if (this.outputFused) {
                  this.clear();
               } else {
                  Operators.onDiscardQueueWithClear(this.queue, this.actual.currentContext(), null);
               }

               this.actual.onError(Operators.onRejectedExecution(var5, subscription, suppressed, dataSignal, this.actual.currentContext()));
            }

         }
      }

      void runSync() {
         int missed = 1;
         Fuseable.ConditionalSubscriber<? super T> a = this.actual;
         Queue<T> q = this.queue;
         long e = this.produced;

         while(true) {
            long r = this.requested;

            while(e != r) {
               T v;
               try {
                  v = (T)q.poll();
               } catch (Throwable var10) {
                  this.doError(a, Operators.onOperatorError(this.s, var10, this.actual.currentContext()));
                  return;
               }

               if (this.cancelled) {
                  Operators.onDiscard(v, this.actual.currentContext());
                  Operators.onDiscardQueueWithClear(q, this.actual.currentContext(), null);
                  return;
               }

               if (v == null) {
                  this.doComplete(a);
                  return;
               }

               if (a.tryOnNext(v)) {
                  ++e;
               }
            }

            if (this.cancelled) {
               Operators.onDiscardQueueWithClear(q, this.actual.currentContext(), null);
               return;
            }

            if (q.isEmpty()) {
               this.doComplete(a);
               return;
            }

            int w = this.wip;
            if (missed == w) {
               this.produced = e;
               missed = WIP.addAndGet(this, -missed);
               if (missed == 0) {
                  return;
               }
            } else {
               missed = w;
            }
         }
      }

      void runAsync() {
         int missed = 1;
         Fuseable.ConditionalSubscriber<? super T> a = this.actual;
         Queue<T> q = this.queue;
         long emitted = this.produced;
         long polled = this.consumed;

         while(true) {
            long r = this.requested;

            while(emitted != r) {
               boolean d = this.done;

               T v;
               try {
                  v = (T)q.poll();
               } catch (Throwable var13) {
                  Exceptions.throwIfFatal(var13);
                  this.s.cancel();
                  q.clear();
                  this.doError(a, Operators.onOperatorError(var13, this.actual.currentContext()));
                  return;
               }

               boolean empty = v == null;
               if (this.checkTerminated(d, empty, a, v)) {
                  return;
               }

               if (empty) {
                  break;
               }

               if (a.tryOnNext(v)) {
                  ++emitted;
               }

               ++polled;
               if (polled == (long)this.limit) {
                  this.s.request(polled);
                  polled = 0L;
               }
            }

            if (emitted == r && this.checkTerminated(this.done, q.isEmpty(), a, (T)null)) {
               return;
            }

            int w = this.wip;
            if (missed == w) {
               this.produced = emitted;
               this.consumed = polled;
               missed = WIP.addAndGet(this, -missed);
               if (missed == 0) {
                  return;
               }
            } else {
               missed = w;
            }
         }
      }

      void runBackfused() {
         int missed = 1;

         while(!this.cancelled) {
            boolean d = this.done;
            this.actual.onNext((T)null);
            if (d) {
               Throwable e = this.error;
               if (e != null) {
                  this.doError(this.actual, e);
               } else {
                  this.doComplete(this.actual);
               }

               return;
            }

            missed = WIP.addAndGet(this, -missed);
            if (missed == 0) {
               return;
            }
         }

         this.clear();
      }

      public void run() {
         if (this.outputFused) {
            this.runBackfused();
         } else if (this.sourceMode == 1) {
            this.runSync();
         } else {
            this.runAsync();
         }

      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue != null ? this.queue.size() : 0;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.DELAY_ERROR) {
            return this.delayError;
         } else if (key == Scannable.Attr.PREFETCH) {
            return this.prefetch;
         } else if (key == Scannable.Attr.RUN_ON) {
            return this.worker;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      void doComplete(Subscriber<?> a) {
         a.onComplete();
         this.worker.dispose();
      }

      void doError(Subscriber<?> a, Throwable e) {
         try {
            a.onError(e);
         } finally {
            this.worker.dispose();
         }

      }

      boolean checkTerminated(boolean d, boolean empty, Subscriber<?> a, @Nullable T v) {
         if (this.cancelled) {
            Operators.onDiscard(v, this.actual.currentContext());
            if (this.sourceMode == 2) {
               this.queue.clear();
            } else {
               Operators.onDiscardQueueWithClear(this.queue, this.actual.currentContext(), null);
            }

            return true;
         } else {
            if (d) {
               if (this.delayError) {
                  if (empty) {
                     Throwable e = this.error;
                     if (e != null) {
                        this.doError(a, e);
                     } else {
                        this.doComplete(a);
                     }

                     return true;
                  }
               } else {
                  Throwable e = this.error;
                  if (e != null) {
                     Operators.onDiscard(v, this.actual.currentContext());
                     if (this.sourceMode == 2) {
                        this.queue.clear();
                     } else {
                        Operators.onDiscardQueueWithClear(this.queue, this.actual.currentContext(), null);
                     }

                     this.doError(a, e);
                     return true;
                  }

                  if (empty) {
                     this.doComplete(a);
                     return true;
                  }
               }
            }

            return false;
         }
      }

      public void clear() {
         if (DISCARD_GUARD.getAndIncrement(this) == 0) {
            int missed = 1;

            while(true) {
               Operators.onDiscardQueueWithClear(this.queue, this.actual.currentContext(), null);
               int dg = this.discardGuard;
               if (missed == dg) {
                  missed = DISCARD_GUARD.addAndGet(this, -missed);
                  if (missed == 0) {
                     return;
                  }
               } else {
                  missed = dg;
               }
            }
         }
      }

      public boolean isEmpty() {
         return this.queue.isEmpty();
      }

      @Nullable
      public T poll() {
         T v = (T)this.queue.poll();
         if (v != null && this.sourceMode != 1) {
            long p = this.consumed + 1L;
            if (p == (long)this.limit) {
               this.consumed = 0L;
               this.s.request(p);
            } else {
               this.consumed = p;
            }
         }

         return v;
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

      public int size() {
         return this.queue.size();
      }
   }

   static final class PublishOnSubscriber<T> implements Fuseable.QueueSubscription<T>, Runnable, InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final Scheduler scheduler;
      final Scheduler.Worker worker;
      final boolean delayError;
      final int prefetch;
      final int limit;
      final Supplier<? extends Queue<T>> queueSupplier;
      Subscription s;
      Queue<T> queue;
      volatile boolean cancelled;
      volatile boolean done;
      Throwable error;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxPublishOn.PublishOnSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxPublishOn.PublishOnSubscriber.class, "wip"
      );
      volatile int discardGuard;
      static final AtomicIntegerFieldUpdater<FluxPublishOn.PublishOnSubscriber> DISCARD_GUARD = AtomicIntegerFieldUpdater.newUpdater(
         FluxPublishOn.PublishOnSubscriber.class, "discardGuard"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxPublishOn.PublishOnSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxPublishOn.PublishOnSubscriber.class, "requested"
      );
      int sourceMode;
      long produced;
      boolean outputFused;

      PublishOnSubscriber(
         CoreSubscriber<? super T> actual,
         Scheduler scheduler,
         Scheduler.Worker worker,
         boolean delayError,
         int prefetch,
         int lowTide,
         Supplier<? extends Queue<T>> queueSupplier
      ) {
         this.actual = actual;
         this.worker = worker;
         this.scheduler = scheduler;
         this.delayError = delayError;
         this.prefetch = prefetch;
         this.queueSupplier = queueSupplier;
         this.limit = Operators.unboundedOrLimit(prefetch, lowTide);
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            if (s instanceof Fuseable.QueueSubscription) {
               Fuseable.QueueSubscription<T> f = (Fuseable.QueueSubscription)s;
               int m = f.requestFusion(7);
               if (m == 1) {
                  this.sourceMode = 1;
                  this.queue = f;
                  this.done = true;
                  this.actual.onSubscribe(this);
                  return;
               }

               if (m == 2) {
                  this.sourceMode = 2;
                  this.queue = f;
                  this.actual.onSubscribe(this);
                  s.request(Operators.unboundedOrPrefetch(this.prefetch));
                  return;
               }
            }

            this.queue = (Queue)this.queueSupplier.get();
            this.actual.onSubscribe(this);
            s.request(Operators.unboundedOrPrefetch(this.prefetch));
         }

      }

      @Override
      public void onNext(T t) {
         if (this.sourceMode == 2) {
            this.trySchedule(this, null, null);
         } else if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else if (this.cancelled) {
            Operators.onDiscard(t, this.actual.currentContext());
         } else {
            if (!this.queue.offer(t)) {
               Operators.onDiscard(t, this.actual.currentContext());
               this.error = Operators.onOperatorError(
                  this.s, Exceptions.failWithOverflow("Queue is full: Reactive Streams source doesn't respect backpressure"), t, this.actual.currentContext()
               );
               this.done = true;
            }

            this.trySchedule(this, null, t);
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.error = t;
            this.done = true;
            this.trySchedule(null, t, null);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.trySchedule(null, null, null);
         }
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
            this.trySchedule(this, null, null);
         }

      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            this.s.cancel();
            this.worker.dispose();
            if (WIP.getAndIncrement(this) == 0) {
               if (this.sourceMode == 2) {
                  this.queue.clear();
               } else if (!this.outputFused) {
                  Operators.onDiscardQueueWithClear(this.queue, this.actual.currentContext(), null);
               }
            }

         }
      }

      void trySchedule(@Nullable Subscription subscription, @Nullable Throwable suppressed, @Nullable Object dataSignal) {
         if (WIP.getAndIncrement(this) != 0) {
            if (this.cancelled) {
               if (this.sourceMode == 2) {
                  this.queue.clear();
               } else {
                  Operators.onDiscard(dataSignal, this.actual.currentContext());
               }
            }

         } else {
            try {
               this.worker.schedule(this);
            } catch (RejectedExecutionException var5) {
               if (this.sourceMode == 2) {
                  this.queue.clear();
               } else if (this.outputFused) {
                  this.clear();
               } else {
                  Operators.onDiscardQueueWithClear(this.queue, this.actual.currentContext(), null);
               }

               this.actual.onError(Operators.onRejectedExecution(var5, subscription, suppressed, dataSignal, this.actual.currentContext()));
            }

         }
      }

      void runSync() {
         int missed = 1;
         Subscriber<? super T> a = this.actual;
         Queue<T> q = this.queue;
         long e = this.produced;

         while(true) {
            for(long r = this.requested; e != r; ++e) {
               T v;
               try {
                  v = (T)q.poll();
               } catch (Throwable var10) {
                  this.doError(a, Operators.onOperatorError(this.s, var10, this.actual.currentContext()));
                  return;
               }

               if (this.cancelled) {
                  Operators.onDiscard(v, this.actual.currentContext());
                  Operators.onDiscardQueueWithClear(q, this.actual.currentContext(), null);
                  return;
               }

               if (v == null) {
                  this.doComplete(a);
                  return;
               }

               a.onNext(v);
            }

            if (this.cancelled) {
               Operators.onDiscardQueueWithClear(q, this.actual.currentContext(), null);
               return;
            }

            if (q.isEmpty()) {
               this.doComplete(a);
               return;
            }

            int w = this.wip;
            if (missed == w) {
               this.produced = e;
               missed = WIP.addAndGet(this, -missed);
               if (missed == 0) {
                  return;
               }
            } else {
               missed = w;
            }
         }
      }

      void runAsync() {
         int missed = 1;
         Subscriber<? super T> a = this.actual;
         Queue<T> q = this.queue;
         long e = this.produced;

         while(true) {
            long r = this.requested;

            while(e != r) {
               boolean d = this.done;

               T v;
               try {
                  v = (T)q.poll();
               } catch (Throwable var11) {
                  Exceptions.throwIfFatal(var11);
                  this.s.cancel();
                  if (this.sourceMode == 2) {
                     this.queue.clear();
                  } else {
                     Operators.onDiscardQueueWithClear(this.queue, this.actual.currentContext(), null);
                  }

                  this.doError(a, Operators.onOperatorError(var11, this.actual.currentContext()));
                  return;
               }

               boolean empty = v == null;
               if (this.checkTerminated(d, empty, a, v)) {
                  return;
               }

               if (empty) {
                  break;
               }

               a.onNext(v);
               ++e;
               if (e == (long)this.limit) {
                  if (r != Long.MAX_VALUE) {
                     r = REQUESTED.addAndGet(this, -e);
                  }

                  this.s.request(e);
                  e = 0L;
               }
            }

            if (e == r && this.checkTerminated(this.done, q.isEmpty(), a, (T)null)) {
               return;
            }

            int w = this.wip;
            if (missed == w) {
               this.produced = e;
               missed = WIP.addAndGet(this, -missed);
               if (missed == 0) {
                  return;
               }
            } else {
               missed = w;
            }
         }
      }

      void runBackfused() {
         int missed = 1;

         while(!this.cancelled) {
            boolean d = this.done;
            this.actual.onNext((T)null);
            if (d) {
               Throwable e = this.error;
               if (e != null) {
                  this.doError(this.actual, e);
               } else {
                  this.doComplete(this.actual);
               }

               return;
            }

            missed = WIP.addAndGet(this, -missed);
            if (missed == 0) {
               return;
            }
         }

         this.clear();
      }

      void doComplete(Subscriber<?> a) {
         a.onComplete();
         this.worker.dispose();
      }

      void doError(Subscriber<?> a, Throwable e) {
         try {
            a.onError(e);
         } finally {
            this.worker.dispose();
         }

      }

      public void run() {
         if (this.outputFused) {
            this.runBackfused();
         } else if (this.sourceMode == 1) {
            this.runSync();
         } else {
            this.runAsync();
         }

      }

      boolean checkTerminated(boolean d, boolean empty, Subscriber<?> a, @Nullable T v) {
         if (this.cancelled) {
            Operators.onDiscard(v, this.actual.currentContext());
            if (this.sourceMode == 2) {
               this.queue.clear();
            } else {
               Operators.onDiscardQueueWithClear(this.queue, this.actual.currentContext(), null);
            }

            return true;
         } else {
            if (d) {
               if (this.delayError) {
                  if (empty) {
                     Throwable e = this.error;
                     if (e != null) {
                        this.doError(a, e);
                     } else {
                        this.doComplete(a);
                     }

                     return true;
                  }
               } else {
                  Throwable e = this.error;
                  if (e != null) {
                     Operators.onDiscard(v, this.actual.currentContext());
                     if (this.sourceMode == 2) {
                        this.queue.clear();
                     } else {
                        Operators.onDiscardQueueWithClear(this.queue, this.actual.currentContext(), null);
                     }

                     this.doError(a, e);
                     return true;
                  }

                  if (empty) {
                     this.doComplete(a);
                     return true;
                  }
               }
            }

            return false;
         }
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue != null ? this.queue.size() : 0;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.DELAY_ERROR) {
            return this.delayError;
         } else if (key == Scannable.Attr.PREFETCH) {
            return this.prefetch;
         } else if (key == Scannable.Attr.RUN_ON) {
            return this.worker;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      public void clear() {
         if (DISCARD_GUARD.getAndIncrement(this) == 0) {
            int missed = 1;

            while(true) {
               Operators.onDiscardQueueWithClear(this.queue, this.actual.currentContext(), null);
               int dg = this.discardGuard;
               if (missed == dg) {
                  missed = DISCARD_GUARD.addAndGet(this, -missed);
                  if (missed == 0) {
                     return;
                  }
               } else {
                  missed = dg;
               }
            }
         }
      }

      public boolean isEmpty() {
         return this.queue.isEmpty();
      }

      @Nullable
      public T poll() {
         T v = (T)this.queue.poll();
         if (v != null && this.sourceMode != 1) {
            long p = this.produced + 1L;
            if (p == (long)this.limit) {
               this.produced = 0L;
               this.s.request(p);
            } else {
               this.produced = p;
            }
         }

         return v;
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

      public int size() {
         return this.queue.size();
      }
   }
}
