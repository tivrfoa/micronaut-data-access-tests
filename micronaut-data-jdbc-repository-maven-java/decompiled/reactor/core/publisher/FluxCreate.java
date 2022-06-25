package reactor.core.publisher;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import org.reactivestreams.Subscriber;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

final class FluxCreate<T> extends Flux<T> implements SourceProducer<T> {
   final Consumer<? super FluxSink<T>> source;
   final FluxSink.OverflowStrategy backpressure;
   final FluxCreate.CreateMode createMode;

   FluxCreate(Consumer<? super FluxSink<T>> source, FluxSink.OverflowStrategy backpressure, FluxCreate.CreateMode createMode) {
      this.source = (Consumer)Objects.requireNonNull(source, "source");
      this.backpressure = (FluxSink.OverflowStrategy)Objects.requireNonNull(backpressure, "backpressure");
      this.createMode = createMode;
   }

   static <T> FluxCreate.BaseSink<T> createSink(CoreSubscriber<? super T> t, FluxSink.OverflowStrategy backpressure) {
      switch(backpressure) {
         case IGNORE:
            return new FluxCreate.IgnoreSink<>(t);
         case ERROR:
            return new FluxCreate.ErrorAsyncSink<>(t);
         case DROP:
            return new FluxCreate.DropAsyncSink<>(t);
         case LATEST:
            return new FluxCreate.LatestAsyncSink<>(t);
         default:
            return new FluxCreate.BufferAsyncSink<>(t, Queues.SMALL_BUFFER_SIZE);
      }
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      FluxCreate.BaseSink<T> sink = createSink(actual, this.backpressure);
      actual.onSubscribe(sink);

      try {
         this.source.accept(this.createMode == FluxCreate.CreateMode.PUSH_PULL ? new FluxCreate.SerializedFluxSink<>(sink) : sink);
      } catch (Throwable var4) {
         Exceptions.throwIfFatal(var4);
         sink.error(Operators.onOperatorError(var4, actual.currentContext()));
      }

   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : null;
   }

   abstract static class BaseSink<T> extends AtomicBoolean implements FluxSink<T>, InnerProducer<T> {
      static final Disposable TERMINATED = OperatorDisposables.DISPOSED;
      static final Disposable CANCELLED = Disposables.disposed();
      final CoreSubscriber<? super T> actual;
      final Context ctx;
      volatile Disposable disposable;
      static final AtomicReferenceFieldUpdater<FluxCreate.BaseSink, Disposable> DISPOSABLE = AtomicReferenceFieldUpdater.newUpdater(
         FluxCreate.BaseSink.class, Disposable.class, "disposable"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxCreate.BaseSink> REQUESTED = AtomicLongFieldUpdater.newUpdater(FluxCreate.BaseSink.class, "requested");
      volatile LongConsumer requestConsumer;
      static final AtomicReferenceFieldUpdater<FluxCreate.BaseSink, LongConsumer> REQUEST_CONSUMER = AtomicReferenceFieldUpdater.newUpdater(
         FluxCreate.BaseSink.class, LongConsumer.class, "requestConsumer"
      );

      BaseSink(CoreSubscriber<? super T> actual) {
         this.actual = actual;
         this.ctx = actual.currentContext();
      }

      @Deprecated
      @Override
      public Context currentContext() {
         return this.actual.currentContext();
      }

      @Override
      public ContextView contextView() {
         return this.actual.currentContext();
      }

      @Override
      public void complete() {
         if (!this.isTerminated()) {
            try {
               this.actual.onComplete();
            } finally {
               this.disposeResource(false);
            }

         }
      }

      @Override
      public void error(Throwable e) {
         if (this.isTerminated()) {
            Operators.onOperatorError(e, this.ctx);
         } else {
            try {
               this.actual.onError(e);
            } finally {
               this.disposeResource(false);
            }

         }
      }

      @Override
      public final void cancel() {
         this.disposeResource(true);
         this.onCancel();
      }

      void disposeResource(boolean isCancel) {
         Disposable disposed = isCancel ? CANCELLED : TERMINATED;
         Disposable d = this.disposable;
         if (d != TERMINATED && d != CANCELLED) {
            d = (Disposable)DISPOSABLE.getAndSet(this, disposed);
            if (d != null && d != TERMINATED && d != CANCELLED) {
               if (isCancel && d instanceof FluxCreate.SinkDisposable) {
                  ((FluxCreate.SinkDisposable)d).cancel();
               }

               d.dispose();
            }
         }

      }

      @Override
      public long requestedFromDownstream() {
         return this.requested;
      }

      void onCancel() {
      }

      @Override
      public final boolean isCancelled() {
         return this.disposable == CANCELLED;
      }

      final boolean isTerminated() {
         return this.disposable == TERMINATED;
      }

      @Override
      public final void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
            LongConsumer consumer = this.requestConsumer;
            if (n > 0L && consumer != null && !this.isCancelled()) {
               consumer.accept(n);
            }

            this.onRequestedFromDownstream();
         }

      }

      void onRequestedFromDownstream() {
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public FluxSink<T> onRequest(LongConsumer consumer) {
         Objects.requireNonNull(consumer, "onRequest");
         this.onRequest(consumer, n -> {
         }, Long.MAX_VALUE);
         return this;
      }

      protected void onRequest(LongConsumer initialRequestConsumer, LongConsumer requestConsumer, long value) {
         if (!REQUEST_CONSUMER.compareAndSet(this, null, requestConsumer)) {
            throw new IllegalStateException("A consumer has already been assigned to consume requests");
         } else {
            if (value > 0L) {
               initialRequestConsumer.accept(value);
            }

         }
      }

      @Override
      public final FluxSink<T> onCancel(Disposable d) {
         Objects.requireNonNull(d, "onCancel");
         FluxCreate.SinkDisposable sd = new FluxCreate.SinkDisposable(null, d);
         if (!DISPOSABLE.compareAndSet(this, null, sd)) {
            Disposable c = this.disposable;
            if (c == CANCELLED) {
               d.dispose();
            } else if (c instanceof FluxCreate.SinkDisposable) {
               FluxCreate.SinkDisposable current = (FluxCreate.SinkDisposable)c;
               if (current.onCancel == null) {
                  current.onCancel = d;
               } else {
                  d.dispose();
               }
            }
         }

         return this;
      }

      @Override
      public final FluxSink<T> onDispose(Disposable d) {
         Objects.requireNonNull(d, "onDispose");
         FluxCreate.SinkDisposable sd = new FluxCreate.SinkDisposable(d, null);
         if (!DISPOSABLE.compareAndSet(this, null, sd)) {
            Disposable c = this.disposable;
            if (c == TERMINATED || c == CANCELLED) {
               d.dispose();
            } else if (c instanceof FluxCreate.SinkDisposable) {
               FluxCreate.SinkDisposable current = (FluxCreate.SinkDisposable)c;
               if (current.disposable == null) {
                  current.disposable = d;
               } else {
                  d.dispose();
               }
            }
         }

         return this;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.disposable == TERMINATED;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.disposable == CANCELLED;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : InnerProducer.super.scanUnsafe(key);
         }
      }

      public String toString() {
         return "FluxSink";
      }
   }

   static final class BufferAsyncSink<T> extends FluxCreate.BaseSink<T> {
      final Queue<T> queue;
      Throwable error;
      volatile boolean done;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxCreate.BufferAsyncSink> WIP = AtomicIntegerFieldUpdater.newUpdater(FluxCreate.BufferAsyncSink.class, "wip");

      BufferAsyncSink(CoreSubscriber<? super T> actual, int capacityHint) {
         super(actual);
         this.queue = (Queue)Queues.unbounded(capacityHint).get();
      }

      @Override
      public FluxSink<T> next(T t) {
         this.queue.offer(t);
         this.drain();
         return this;
      }

      @Override
      public void error(Throwable e) {
         this.error = e;
         this.done = true;
         this.drain();
      }

      @Override
      public void complete() {
         this.done = true;
         this.drain();
      }

      @Override
      void onRequestedFromDownstream() {
         this.drain();
      }

      @Override
      void onCancel() {
         this.drain();
      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            Subscriber<? super T> a = this.actual;
            Queue<T> q = this.queue;

            while(true) {
               long r = this.requested;
               long e = 0L;

               while(e != r) {
                  if (this.isCancelled()) {
                     Operators.onDiscardQueueWithClear(q, this.ctx, null);
                     if (WIP.decrementAndGet(this) == 0) {
                        return;
                     }
                  } else {
                     boolean d = this.done;
                     T o = (T)q.poll();
                     boolean empty = o == null;
                     if (d && empty) {
                        Throwable ex = this.error;
                        if (ex != null) {
                           super.error(ex);
                        } else {
                           super.complete();
                        }

                        return;
                     }

                     if (empty) {
                        break;
                     }

                     a.onNext(o);
                     ++e;
                  }
               }

               if (e == r) {
                  if (this.isCancelled()) {
                     Operators.onDiscardQueueWithClear(q, this.ctx, null);
                     if (WIP.decrementAndGet(this) == 0) {
                        return;
                     }
                     continue;
                  }

                  boolean d = this.done;
                  boolean empty = q.isEmpty();
                  if (d && empty) {
                     Throwable ex = this.error;
                     if (ex != null) {
                        super.error(ex);
                     } else {
                        super.complete();
                     }

                     return;
                  }
               }

               if (e != 0L) {
                  Operators.produced(REQUESTED, this, e);
               }

               if (WIP.decrementAndGet(this) == 0) {
                  return;
               }
            }
         }
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.BUFFERED) {
            return this.queue.size();
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else {
            return key == Scannable.Attr.ERROR ? this.error : super.scanUnsafe(key);
         }
      }

      @Override
      public String toString() {
         return "FluxSink(" + FluxSink.OverflowStrategy.BUFFER + ")";
      }
   }

   static enum CreateMode {
      PUSH_ONLY,
      PUSH_PULL;
   }

   static final class DropAsyncSink<T> extends FluxCreate.NoOverflowBaseAsyncSink<T> {
      DropAsyncSink(CoreSubscriber<? super T> actual) {
         super(actual);
      }

      @Override
      void onOverflow() {
      }

      @Override
      public String toString() {
         return "FluxSink(" + FluxSink.OverflowStrategy.DROP + ")";
      }
   }

   static final class ErrorAsyncSink<T> extends FluxCreate.NoOverflowBaseAsyncSink<T> {
      ErrorAsyncSink(CoreSubscriber<? super T> actual) {
         super(actual);
      }

      @Override
      void onOverflow() {
         this.error(Exceptions.failWithOverflow());
      }

      @Override
      public String toString() {
         return "FluxSink(" + FluxSink.OverflowStrategy.ERROR + ")";
      }
   }

   static final class IgnoreSink<T> extends FluxCreate.BaseSink<T> {
      IgnoreSink(CoreSubscriber<? super T> actual) {
         super(actual);
      }

      @Override
      public FluxSink<T> next(T t) {
         if (this.isTerminated()) {
            Operators.onNextDropped(t, this.ctx);
            return this;
         } else if (this.isCancelled()) {
            Operators.onDiscard(t, this.ctx);
            return this;
         } else {
            this.actual.onNext(t);

            long r;
            do {
               r = this.requested;
            } while(r != 0L && !REQUESTED.compareAndSet(this, r, r - 1L));

            return this;
         }
      }

      @Override
      public String toString() {
         return "FluxSink(" + FluxSink.OverflowStrategy.IGNORE + ")";
      }
   }

   static final class LatestAsyncSink<T> extends FluxCreate.BaseSink<T> {
      final AtomicReference<T> queue = new AtomicReference();
      Throwable error;
      volatile boolean done;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxCreate.LatestAsyncSink> WIP = AtomicIntegerFieldUpdater.newUpdater(FluxCreate.LatestAsyncSink.class, "wip");

      LatestAsyncSink(CoreSubscriber<? super T> actual) {
         super(actual);
      }

      @Override
      public FluxSink<T> next(T t) {
         T old = (T)this.queue.getAndSet(t);
         Operators.onDiscard(old, this.ctx);
         this.drain();
         return this;
      }

      @Override
      public void error(Throwable e) {
         this.error = e;
         this.done = true;
         this.drain();
      }

      @Override
      public void complete() {
         this.done = true;
         this.drain();
      }

      @Override
      void onRequestedFromDownstream() {
         this.drain();
      }

      @Override
      void onCancel() {
         this.drain();
      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            Subscriber<? super T> a = this.actual;
            AtomicReference<T> q = this.queue;

            while(true) {
               long r = this.requested;
               long e = 0L;

               while(e != r) {
                  if (this.isCancelled()) {
                     T old = (T)q.getAndSet(null);
                     Operators.onDiscard(old, this.ctx);
                     if (WIP.decrementAndGet(this) == 0) {
                        return;
                     }
                  } else {
                     boolean d = this.done;
                     T o = (T)q.getAndSet(null);
                     boolean empty = o == null;
                     if (d && empty) {
                        Throwable ex = this.error;
                        if (ex != null) {
                           super.error(ex);
                        } else {
                           super.complete();
                        }

                        return;
                     }

                     if (empty) {
                        break;
                     }

                     a.onNext(o);
                     ++e;
                  }
               }

               if (e == r) {
                  if (this.isCancelled()) {
                     T old = (T)q.getAndSet(null);
                     Operators.onDiscard(old, this.ctx);
                     if (WIP.decrementAndGet(this) == 0) {
                        return;
                     }
                     continue;
                  }

                  boolean d = this.done;
                  boolean empty = q.get() == null;
                  if (d && empty) {
                     Throwable ex = this.error;
                     if (ex != null) {
                        super.error(ex);
                     } else {
                        super.complete();
                     }

                     return;
                  }
               }

               if (e != 0L) {
                  Operators.produced(REQUESTED, this, e);
               }

               if (WIP.decrementAndGet(this) == 0) {
                  return;
               }
            }
         }
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.BUFFERED) {
            return this.queue.get() == null ? 0 : 1;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else {
            return key == Scannable.Attr.ERROR ? this.error : super.scanUnsafe(key);
         }
      }

      @Override
      public String toString() {
         return "FluxSink(" + FluxSink.OverflowStrategy.LATEST + ")";
      }
   }

   abstract static class NoOverflowBaseAsyncSink<T> extends FluxCreate.BaseSink<T> {
      NoOverflowBaseAsyncSink(CoreSubscriber<? super T> actual) {
         super(actual);
      }

      @Override
      public final FluxSink<T> next(T t) {
         if (this.isTerminated()) {
            Operators.onNextDropped(t, this.ctx);
            return this;
         } else {
            if (this.requested != 0L) {
               this.actual.onNext(t);
               Operators.produced(REQUESTED, this, 1L);
            } else {
               this.onOverflow();
               Operators.onDiscard(t, this.ctx);
            }

            return this;
         }
      }

      abstract void onOverflow();
   }

   static class SerializeOnRequestSink<T> implements FluxSink<T>, Scannable {
      final FluxCreate.BaseSink<T> baseSink;
      FluxCreate.SerializedFluxSink<T> serializedSink;
      FluxSink<T> sink;

      SerializeOnRequestSink(FluxCreate.BaseSink<T> sink) {
         this.baseSink = sink;
         this.sink = sink;
      }

      @Deprecated
      @Override
      public Context currentContext() {
         return this.sink.currentContext();
      }

      @Override
      public ContextView contextView() {
         return this.sink.contextView();
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return this.serializedSink != null ? this.serializedSink.scanUnsafe(key) : this.baseSink.scanUnsafe(key);
      }

      @Override
      public void complete() {
         this.sink.complete();
      }

      @Override
      public void error(Throwable e) {
         this.sink.error(e);
      }

      @Override
      public FluxSink<T> next(T t) {
         this.sink.next(t);
         return (FluxSink<T>)(this.serializedSink == null ? this : this.serializedSink);
      }

      @Override
      public long requestedFromDownstream() {
         return this.sink.requestedFromDownstream();
      }

      @Override
      public boolean isCancelled() {
         return this.sink.isCancelled();
      }

      @Override
      public FluxSink<T> onRequest(LongConsumer consumer) {
         if (this.serializedSink == null) {
            this.serializedSink = new FluxCreate.SerializedFluxSink<>(this.baseSink);
            this.sink = this.serializedSink;
         }

         return this.sink.onRequest(consumer);
      }

      @Override
      public FluxSink<T> onCancel(Disposable d) {
         this.sink.onCancel(d);
         return this.sink;
      }

      @Override
      public FluxSink<T> onDispose(Disposable d) {
         this.sink.onDispose(d);
         return this;
      }

      public String toString() {
         return this.baseSink.toString();
      }
   }

   static final class SerializedFluxSink<T> implements FluxSink<T>, Scannable {
      final FluxCreate.BaseSink<T> sink;
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxCreate.SerializedFluxSink, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxCreate.SerializedFluxSink.class, Throwable.class, "error"
      );
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxCreate.SerializedFluxSink> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxCreate.SerializedFluxSink.class, "wip"
      );
      final Queue<T> mpscQueue;
      volatile boolean done;

      SerializedFluxSink(FluxCreate.BaseSink<T> sink) {
         this.sink = sink;
         this.mpscQueue = (Queue)Queues.unboundedMultiproducer().get();
      }

      @Deprecated
      @Override
      public Context currentContext() {
         return this.sink.currentContext();
      }

      @Override
      public ContextView contextView() {
         return this.sink.contextView();
      }

      @Override
      public FluxSink<T> next(T t) {
         Objects.requireNonNull(t, "t is null in sink.next(t)");
         if (!this.sink.isTerminated() && !this.done) {
            if (WIP.get(this) == 0 && WIP.compareAndSet(this, 0, 1)) {
               try {
                  this.sink.next(t);
               } catch (Throwable var3) {
                  Operators.onOperatorError(this.sink, var3, t, this.sink.currentContext());
               }

               if (WIP.decrementAndGet(this) == 0) {
                  return this;
               }
            } else {
               this.mpscQueue.offer(t);
               if (WIP.getAndIncrement(this) != 0) {
                  return this;
               }
            }

            this.drainLoop();
            return this;
         } else {
            Operators.onNextDropped(t, this.sink.currentContext());
            return this;
         }
      }

      @Override
      public void error(Throwable t) {
         Objects.requireNonNull(t, "t is null in sink.error(t)");
         if (!this.sink.isTerminated() && !this.done) {
            if (Exceptions.addThrowable(ERROR, this, t)) {
               this.done = true;
               this.drain();
            } else {
               Context ctx = this.sink.currentContext();
               Operators.onDiscardQueueWithClear(this.mpscQueue, ctx, null);
               Operators.onOperatorError(t, ctx);
            }

         } else {
            Operators.onOperatorError(t, this.sink.currentContext());
         }
      }

      @Override
      public void complete() {
         if (!this.sink.isTerminated() && !this.done) {
            this.done = true;
            this.drain();
         }
      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            this.drainLoop();
         }

      }

      void drainLoop() {
         Context ctx = this.sink.currentContext();
         FluxCreate.BaseSink<T> e = this.sink;
         Queue<T> q = this.mpscQueue;

         do {
            while(!e.isCancelled()) {
               if (ERROR.get(this) != null) {
                  Operators.onDiscardQueueWithClear(q, ctx, null);
                  e.error(Exceptions.terminate(ERROR, this));
                  return;
               }

               boolean d = this.done;
               T v = (T)q.poll();
               boolean empty = v == null;
               if (d && empty) {
                  e.complete();
                  return;
               }

               if (empty) {
                  if (WIP.decrementAndGet(this) == 0) {
                     return;
                  }
               } else {
                  try {
                     e.next(v);
                  } catch (Throwable var8) {
                     Operators.onOperatorError(this.sink, var8, v, this.sink.currentContext());
                  }
               }
            }

            Operators.onDiscardQueueWithClear(q, ctx, null);
         } while(WIP.decrementAndGet(this) != 0);

      }

      @Override
      public FluxSink<T> onRequest(LongConsumer consumer) {
         this.sink.onRequest(consumer, consumer, this.sink.requested);
         return this;
      }

      @Override
      public FluxSink<T> onCancel(Disposable d) {
         this.sink.onCancel(d);
         return this;
      }

      @Override
      public FluxSink<T> onDispose(Disposable d) {
         this.sink.onDispose(d);
         return this;
      }

      @Override
      public long requestedFromDownstream() {
         return this.sink.requestedFromDownstream();
      }

      @Override
      public boolean isCancelled() {
         return this.sink.isCancelled();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.BUFFERED) {
            return this.mpscQueue.size();
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else {
            return key == Scannable.Attr.TERMINATED ? this.done : this.sink.scanUnsafe(key);
         }
      }

      public String toString() {
         return this.sink.toString();
      }
   }

   static final class SinkDisposable implements Disposable {
      Disposable onCancel;
      Disposable disposable;

      SinkDisposable(@Nullable Disposable disposable, @Nullable Disposable onCancel) {
         this.disposable = disposable;
         this.onCancel = onCancel;
      }

      @Override
      public void dispose() {
         if (this.disposable != null) {
            this.disposable.dispose();
         }

      }

      public void cancel() {
         if (this.onCancel != null) {
            this.onCancel.dispose();
         }

      }
   }
}
