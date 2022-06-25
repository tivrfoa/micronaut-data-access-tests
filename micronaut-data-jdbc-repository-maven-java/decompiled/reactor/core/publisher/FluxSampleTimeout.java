package reactor.core.publisher;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxSampleTimeout<T, U> extends InternalFluxOperator<T, T> {
   final Function<? super T, ? extends Publisher<U>> throttler;
   final Supplier<Queue<Object>> queueSupplier;

   FluxSampleTimeout(Flux<? extends T> source, Function<? super T, ? extends Publisher<U>> throttler, Supplier<Queue<Object>> queueSupplier) {
      super(source);
      this.throttler = (Function)Objects.requireNonNull(throttler, "throttler");
      this.queueSupplier = (Supplier)Objects.requireNonNull(queueSupplier, "queueSupplier");
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      Queue<FluxSampleTimeout.SampleTimeoutOther<T, U>> q = (Queue)this.queueSupplier.get();
      FluxSampleTimeout.SampleTimeoutMain<T, U> main = new FluxSampleTimeout.SampleTimeoutMain<>(actual, this.throttler, q);
      actual.onSubscribe(main);
      return main;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class SampleTimeoutMain<T, U> implements InnerOperator<T, T> {
      final Function<? super T, ? extends Publisher<U>> throttler;
      final Queue<FluxSampleTimeout.SampleTimeoutOther<T, U>> queue;
      final CoreSubscriber<? super T> actual;
      final Context ctx;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<FluxSampleTimeout.SampleTimeoutMain, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         FluxSampleTimeout.SampleTimeoutMain.class, Subscription.class, "s"
      );
      volatile Subscription other;
      static final AtomicReferenceFieldUpdater<FluxSampleTimeout.SampleTimeoutMain, Subscription> OTHER = AtomicReferenceFieldUpdater.newUpdater(
         FluxSampleTimeout.SampleTimeoutMain.class, Subscription.class, "other"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxSampleTimeout.SampleTimeoutMain> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxSampleTimeout.SampleTimeoutMain.class, "requested"
      );
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxSampleTimeout.SampleTimeoutMain> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxSampleTimeout.SampleTimeoutMain.class, "wip"
      );
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxSampleTimeout.SampleTimeoutMain, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxSampleTimeout.SampleTimeoutMain.class, Throwable.class, "error"
      );
      volatile boolean done;
      volatile boolean cancelled;
      volatile long index;
      static final AtomicLongFieldUpdater<FluxSampleTimeout.SampleTimeoutMain> INDEX = AtomicLongFieldUpdater.newUpdater(
         FluxSampleTimeout.SampleTimeoutMain.class, "index"
      );

      SampleTimeoutMain(
         CoreSubscriber<? super T> actual, Function<? super T, ? extends Publisher<U>> throttler, Queue<FluxSampleTimeout.SampleTimeoutOther<T, U>> queue
      ) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.throttler = throttler;
         this.queue = queue;
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(Scannable.from(this.other));
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue.size();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public final CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
         }

      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            Operators.terminate(S, this);
            Operators.terminate(OTHER, this);
            Operators.onDiscardQueueWithClear(this.queue, this.ctx, FluxSampleTimeout.SampleTimeoutOther::toStream);
         }

      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
         long idx = INDEX.incrementAndGet(this);
         if (Operators.set(OTHER, this, Operators.emptySubscription())) {
            Publisher<U> p;
            try {
               p = (Publisher)Objects.requireNonNull(this.throttler.apply(t), "throttler returned a null publisher");
            } catch (Throwable var6) {
               this.onError(Operators.onOperatorError(this.s, var6, t, this.ctx));
               return;
            }

            FluxSampleTimeout.SampleTimeoutOther<T, U> os = new FluxSampleTimeout.SampleTimeoutOther<>(this, t, idx);
            if (Operators.replace(OTHER, this, os)) {
               p.subscribe(os);
            }

         }
      }

      void error(Throwable t) {
         if (Exceptions.addThrowable(ERROR, this, t)) {
            this.done = true;
            this.drain();
         } else {
            Operators.onErrorDropped(t, this.ctx);
         }

      }

      @Override
      public void onError(Throwable t) {
         Operators.terminate(OTHER, this);
         this.error(t);
      }

      @Override
      public void onComplete() {
         Subscription o = this.other;
         if (o instanceof FluxSampleTimeout.SampleTimeoutOther) {
            FluxSampleTimeout.SampleTimeoutOther<?, ?> os = (FluxSampleTimeout.SampleTimeoutOther)o;
            os.cancel();
            os.onComplete();
         }

         this.done = true;
         this.drain();
      }

      void otherNext(FluxSampleTimeout.SampleTimeoutOther<T, U> other) {
         this.queue.offer(other);
         this.drain();
      }

      void otherError(long idx, Throwable e) {
         if (idx == this.index) {
            Operators.terminate(S, this);
            this.error(e);
         } else {
            Operators.onErrorDropped(e, this.ctx);
         }

      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            Subscriber<? super T> a = this.actual;
            Queue<FluxSampleTimeout.SampleTimeoutOther<T, U>> q = this.queue;
            int missed = 1;

            while(true) {
               boolean d = this.done;
               FluxSampleTimeout.SampleTimeoutOther<T, U> o = (FluxSampleTimeout.SampleTimeoutOther)q.poll();
               boolean empty = o == null;
               if (this.checkTerminated(d, empty, a, q)) {
                  return;
               }

               if (empty) {
                  missed = WIP.addAndGet(this, -missed);
                  if (missed == 0) {
                     return;
                  }
               } else if (o.index == this.index) {
                  long r = this.requested;
                  if (r == 0L) {
                     this.cancel();
                     Operators.onDiscardQueueWithClear(q, this.ctx, FluxSampleTimeout.SampleTimeoutOther::toStream);
                     Throwable e = Exceptions.failWithOverflow("Could not emit value due to lack of requests");
                     Exceptions.addThrowable(ERROR, this, e);
                     Throwable var10 = Exceptions.terminate(ERROR, this);
                     a.onError(var10);
                     return;
                  }

                  a.onNext(o.value);
                  if (r != Long.MAX_VALUE) {
                     REQUESTED.decrementAndGet(this);
                  }
               }
            }
         }
      }

      boolean checkTerminated(boolean d, boolean empty, Subscriber<?> a, Queue<FluxSampleTimeout.SampleTimeoutOther<T, U>> q) {
         if (this.cancelled) {
            Operators.onDiscardQueueWithClear(q, this.ctx, FluxSampleTimeout.SampleTimeoutOther::toStream);
            return true;
         } else {
            if (d) {
               Throwable e = Exceptions.terminate(ERROR, this);
               if (e != null && e != Exceptions.TERMINATED) {
                  this.cancel();
                  Operators.onDiscardQueueWithClear(q, this.ctx, FluxSampleTimeout.SampleTimeoutOther::toStream);
                  a.onError(e);
                  return true;
               }

               if (empty) {
                  a.onComplete();
                  return true;
               }
            }

            return false;
         }
      }
   }

   static final class SampleTimeoutOther<T, U> extends Operators.DeferredSubscription implements InnerConsumer<U> {
      final FluxSampleTimeout.SampleTimeoutMain<T, U> main;
      final T value;
      final long index;
      volatile int once;
      static final AtomicIntegerFieldUpdater<FluxSampleTimeout.SampleTimeoutOther> ONCE = AtomicIntegerFieldUpdater.newUpdater(
         FluxSampleTimeout.SampleTimeoutOther.class, "once"
      );

      SampleTimeoutOther(FluxSampleTimeout.SampleTimeoutMain<T, U> main, T value, long index) {
         this.main = main;
         this.value = value;
         this.index = index;
      }

      @Override
      public Context currentContext() {
         return this.main.currentContext();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.once == 1;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.main;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (this.set(s)) {
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(U t) {
         if (ONCE.compareAndSet(this, 0, 1)) {
            this.cancel();
            this.main.otherNext(this);
         }

      }

      @Override
      public void onError(Throwable t) {
         if (ONCE.compareAndSet(this, 0, 1)) {
            this.main.otherError(this.index, t);
         } else {
            Operators.onErrorDropped(t, this.main.currentContext());
         }

      }

      @Override
      public void onComplete() {
         if (ONCE.compareAndSet(this, 0, 1)) {
            this.main.otherNext(this);
         }

      }

      final Stream<T> toStream() {
         return Stream.of(this.value);
      }
   }
}
