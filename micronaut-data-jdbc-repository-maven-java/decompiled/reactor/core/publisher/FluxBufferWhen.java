package reactor.core.publisher;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxBufferWhen<T, OPEN, CLOSE, BUFFER extends Collection<? super T>> extends InternalFluxOperator<T, BUFFER> {
   final Publisher<OPEN> start;
   final Function<? super OPEN, ? extends Publisher<CLOSE>> end;
   final Supplier<BUFFER> bufferSupplier;
   final Supplier<? extends Queue<BUFFER>> queueSupplier;

   FluxBufferWhen(
      Flux<? extends T> source,
      Publisher<OPEN> start,
      Function<? super OPEN, ? extends Publisher<CLOSE>> end,
      Supplier<BUFFER> bufferSupplier,
      Supplier<? extends Queue<BUFFER>> queueSupplier
   ) {
      super(source);
      this.start = (Publisher)Objects.requireNonNull(start, "start");
      this.end = (Function)Objects.requireNonNull(end, "end");
      this.bufferSupplier = (Supplier)Objects.requireNonNull(bufferSupplier, "bufferSupplier");
      this.queueSupplier = (Supplier)Objects.requireNonNull(queueSupplier, "queueSupplier");
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super BUFFER> actual) {
      FluxBufferWhen.BufferWhenMainSubscriber<T, OPEN, CLOSE, BUFFER> main = new FluxBufferWhen.BufferWhenMainSubscriber<>(
         actual, this.bufferSupplier, this.queueSupplier, this.start, this.end
      );
      actual.onSubscribe(main);
      FluxBufferWhen.BufferWhenOpenSubscriber<OPEN> bos = new FluxBufferWhen.BufferWhenOpenSubscriber<>(main);
      if (main.subscribers.add(bos)) {
         this.start.subscribe(bos);
         return main;
      } else {
         return null;
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class BufferWhenCloseSubscriber<T, BUFFER extends Collection<? super T>> implements Disposable, InnerConsumer<Object> {
      volatile Subscription subscription;
      static final AtomicReferenceFieldUpdater<FluxBufferWhen.BufferWhenCloseSubscriber, Subscription> SUBSCRIPTION = AtomicReferenceFieldUpdater.newUpdater(
         FluxBufferWhen.BufferWhenCloseSubscriber.class, Subscription.class, "subscription"
      );
      final FluxBufferWhen.BufferWhenMainSubscriber<T, ?, ?, BUFFER> parent;
      final long index;

      BufferWhenCloseSubscriber(FluxBufferWhen.BufferWhenMainSubscriber<T, ?, ?, BUFFER> parent, long index) {
         this.parent = parent;
         this.index = index;
      }

      @Override
      public Context currentContext() {
         return this.parent.currentContext();
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(SUBSCRIPTION, this, s)) {
            this.subscription.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void dispose() {
         Operators.terminate(SUBSCRIPTION, this);
      }

      @Override
      public boolean isDisposed() {
         return this.subscription == Operators.cancelledSubscription();
      }

      @Override
      public void onNext(Object t) {
         Subscription s = this.subscription;
         if (s != Operators.cancelledSubscription()) {
            SUBSCRIPTION.lazySet(this, Operators.cancelledSubscription());
            s.cancel();
            this.parent.close(this, this.index);
         }

      }

      @Override
      public void onError(Throwable t) {
         if (this.subscription != Operators.cancelledSubscription()) {
            SUBSCRIPTION.lazySet(this, Operators.cancelledSubscription());
            this.parent.boundaryError(this, t);
         } else {
            Operators.onErrorDropped(t, this.parent.ctx);
         }

      }

      @Override
      public void onComplete() {
         if (this.subscription != Operators.cancelledSubscription()) {
            SUBSCRIPTION.lazySet(this, Operators.cancelledSubscription());
            this.parent.close(this, this.index);
         }

      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.ACTUAL) {
            return this.parent;
         } else if (key == Scannable.Attr.PARENT) {
            return this.subscription;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return Long.MAX_VALUE;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.isDisposed();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }
   }

   static final class BufferWhenMainSubscriber<T, OPEN, CLOSE, BUFFER extends Collection<? super T>> implements InnerOperator<T, BUFFER> {
      final CoreSubscriber<? super BUFFER> actual;
      final Context ctx;
      final Publisher<? extends OPEN> bufferOpen;
      final Function<? super OPEN, ? extends Publisher<? extends CLOSE>> bufferClose;
      final Supplier<BUFFER> bufferSupplier;
      final Disposable.Composite subscribers;
      final Queue<BUFFER> queue;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxBufferWhen.BufferWhenMainSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxBufferWhen.BufferWhenMainSubscriber.class, "requested"
      );
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<FluxBufferWhen.BufferWhenMainSubscriber, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         FluxBufferWhen.BufferWhenMainSubscriber.class, Subscription.class, "s"
      );
      volatile Throwable errors;
      static final AtomicReferenceFieldUpdater<FluxBufferWhen.BufferWhenMainSubscriber, Throwable> ERRORS = AtomicReferenceFieldUpdater.newUpdater(
         FluxBufferWhen.BufferWhenMainSubscriber.class, Throwable.class, "errors"
      );
      volatile int windows;
      static final AtomicIntegerFieldUpdater<FluxBufferWhen.BufferWhenMainSubscriber> WINDOWS = AtomicIntegerFieldUpdater.newUpdater(
         FluxBufferWhen.BufferWhenMainSubscriber.class, "windows"
      );
      volatile boolean done;
      volatile boolean cancelled;
      long index;
      LinkedHashMap<Long, BUFFER> buffers;
      long emitted;

      BufferWhenMainSubscriber(
         CoreSubscriber<? super BUFFER> actual,
         Supplier<BUFFER> bufferSupplier,
         Supplier<? extends Queue<BUFFER>> queueSupplier,
         Publisher<? extends OPEN> bufferOpen,
         Function<? super OPEN, ? extends Publisher<? extends CLOSE>> bufferClose
      ) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.bufferOpen = bufferOpen;
         this.bufferClose = bufferClose;
         this.bufferSupplier = bufferSupplier;
         this.queue = (Queue)queueSupplier.get();
         this.buffers = new LinkedHashMap();
         this.subscribers = Disposables.composite();
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public CoreSubscriber<? super BUFFER> actual() {
         return this.actual;
      }

      @Override
      public void onNext(T t) {
         synchronized(this) {
            Map<Long, BUFFER> bufs = this.buffers;
            if (bufs != null) {
               if (bufs.isEmpty()) {
                  Operators.onDiscard(t, this.ctx);
               } else {
                  for(BUFFER b : bufs.values()) {
                     b.add(t);
                  }

               }
            }
         }
      }

      @Override
      public void onError(Throwable t) {
         if (Exceptions.addThrowable(ERRORS, this, t)) {
            this.subscribers.dispose();
            Map<Long, BUFFER> bufs;
            synchronized(this) {
               bufs = this.buffers;
               this.buffers = null;
            }

            this.done = true;
            this.drain();
            if (bufs != null) {
               for(BUFFER b : bufs.values()) {
                  Operators.onDiscardMultiple(b, this.ctx);
               }
            }
         } else {
            Operators.onErrorDropped(t, this.ctx);
         }

      }

      @Override
      public void onComplete() {
         this.subscribers.dispose();
         synchronized(this) {
            Map<Long, BUFFER> bufs = this.buffers;
            if (bufs == null) {
               return;
            }

            for(BUFFER b : bufs.values()) {
               this.queue.offer(b);
            }

            this.buffers = null;
         }

         this.done = true;
         this.drain();
      }

      @Override
      public void request(long n) {
         Operators.addCap(REQUESTED, this, n);
         this.drain();
      }

      @Override
      public void cancel() {
         if (Operators.terminate(S, this)) {
            this.cancelled = true;
            this.subscribers.dispose();
            Map<Long, BUFFER> bufs;
            synchronized(this) {
               bufs = this.buffers;
               this.buffers = null;
            }

            if (WINDOWS.getAndIncrement(this) == 0) {
               Operators.onDiscardQueueWithClear(this.queue, this.ctx, Collection::stream);
            }

            if (bufs != null && !bufs.isEmpty()) {
               for(BUFFER buffer : bufs.values()) {
                  Operators.onDiscardMultiple(buffer, this.ctx);
               }
            }
         }

      }

      void drain() {
         if (WINDOWS.getAndIncrement(this) == 0) {
            int missed = 1;
            long e = this.emitted;
            Subscriber<? super BUFFER> a = this.actual;
            Queue<BUFFER> q = this.queue;

            do {
               long r;
               for(r = this.requested; e != r; ++e) {
                  if (this.cancelled) {
                     Operators.onDiscardQueueWithClear(q, this.ctx, Collection::stream);
                     return;
                  }

                  boolean d = this.done;
                  if (d && this.errors != null) {
                     Operators.onDiscardQueueWithClear(q, this.ctx, Collection::stream);
                     Throwable ex = Exceptions.terminate(ERRORS, this);
                     a.onError(ex);
                     return;
                  }

                  BUFFER v = (BUFFER)q.poll();
                  boolean empty = v == null;
                  if (d && empty) {
                     a.onComplete();
                     return;
                  }

                  if (empty) {
                     break;
                  }

                  a.onNext((T)v);
               }

               if (e == r) {
                  if (this.cancelled) {
                     Operators.onDiscardQueueWithClear(q, this.ctx, Collection::stream);
                     return;
                  }

                  if (this.done) {
                     if (this.errors != null) {
                        Operators.onDiscardQueueWithClear(q, this.ctx, Collection::stream);
                        Throwable ex = Exceptions.terminate(ERRORS, this);
                        a.onError(ex);
                        return;
                     }

                     if (q.isEmpty()) {
                        a.onComplete();
                        return;
                     }
                  }
               }

               this.emitted = e;
               missed = WINDOWS.addAndGet(this, -missed);
            } while(missed != 0);

         }
      }

      void open(OPEN token) {
         Publisher<? extends CLOSE> p;
         BUFFER buf;
         try {
            buf = (BUFFER)Objects.requireNonNull(this.bufferSupplier.get(), "The bufferSupplier returned a null Collection");
            p = (Publisher)Objects.requireNonNull(this.bufferClose.apply(token), "The bufferClose returned a null Publisher");
         } catch (Throwable var10) {
            Exceptions.throwIfFatal(var10);
            Operators.terminate(S, this);
            if (Exceptions.addThrowable(ERRORS, this, var10)) {
               this.subscribers.dispose();
               Map<Long, BUFFER> bufs;
               synchronized(this) {
                  bufs = this.buffers;
                  this.buffers = null;
               }

               this.done = true;
               this.drain();
               if (bufs != null) {
                  for(BUFFER buffer : bufs.values()) {
                     Operators.onDiscardMultiple(buffer, this.ctx);
                  }
               }
            } else {
               Operators.onErrorDropped(var10, this.ctx);
            }

            return;
         }

         long idx = (long)(this.index++);
         synchronized(this) {
            Map<Long, BUFFER> bufs = this.buffers;
            if (bufs == null) {
               return;
            }

            bufs.put(idx, buf);
         }

         FluxBufferWhen.BufferWhenCloseSubscriber<T, BUFFER> bc = new FluxBufferWhen.BufferWhenCloseSubscriber<>(this, idx);
         this.subscribers.add(bc);
         p.subscribe(bc);
      }

      void openComplete(FluxBufferWhen.BufferWhenOpenSubscriber<OPEN> os) {
         this.subscribers.remove(os);
         if (this.subscribers.size() == 0) {
            Operators.terminate(S, this);
            this.done = true;
            this.drain();
         }

      }

      void close(FluxBufferWhen.BufferWhenCloseSubscriber<T, BUFFER> closer, long idx) {
         this.subscribers.remove(closer);
         boolean makeDone = false;
         if (this.subscribers.size() == 0) {
            makeDone = true;
            Operators.terminate(S, this);
         }

         synchronized(this) {
            Map<Long, BUFFER> bufs = this.buffers;
            if (bufs == null) {
               return;
            }

            this.queue.offer(this.buffers.remove(idx));
         }

         if (makeDone) {
            this.done = true;
         }

         this.drain();
      }

      void boundaryError(Disposable boundary, Throwable ex) {
         Operators.terminate(S, this);
         this.subscribers.remove(boundary);
         if (Exceptions.addThrowable(ERRORS, this, ex)) {
            this.subscribers.dispose();
            Map<Long, BUFFER> bufs;
            synchronized(this) {
               bufs = this.buffers;
               this.buffers = null;
            }

            this.done = true;
            this.drain();
            if (bufs != null) {
               for(BUFFER buffer : bufs.values()) {
                  Operators.onDiscardMultiple(buffer, this.ctx);
               }
            }
         } else {
            Operators.onErrorDropped(ex, this.ctx);
         }

      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.actual;
         } else if (key == Scannable.Attr.PREFETCH) {
            return Integer.MAX_VALUE;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.buffers.values().stream().mapToInt(Collection::size).sum();
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.ERROR) {
            return this.errors;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }
   }

   static final class BufferWhenOpenSubscriber<OPEN> implements Disposable, InnerConsumer<OPEN> {
      volatile Subscription subscription;
      static final AtomicReferenceFieldUpdater<FluxBufferWhen.BufferWhenOpenSubscriber, Subscription> SUBSCRIPTION = AtomicReferenceFieldUpdater.newUpdater(
         FluxBufferWhen.BufferWhenOpenSubscriber.class, Subscription.class, "subscription"
      );
      final FluxBufferWhen.BufferWhenMainSubscriber<?, OPEN, ?, ?> parent;

      BufferWhenOpenSubscriber(FluxBufferWhen.BufferWhenMainSubscriber<?, OPEN, ?, ?> parent) {
         this.parent = parent;
      }

      @Override
      public Context currentContext() {
         return this.parent.currentContext();
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(SUBSCRIPTION, this, s)) {
            this.subscription.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void dispose() {
         Operators.terminate(SUBSCRIPTION, this);
      }

      @Override
      public boolean isDisposed() {
         return this.subscription == Operators.cancelledSubscription();
      }

      @Override
      public void onNext(OPEN t) {
         this.parent.open(t);
      }

      @Override
      public void onError(Throwable t) {
         SUBSCRIPTION.lazySet(this, Operators.cancelledSubscription());
         this.parent.boundaryError(this, t);
      }

      @Override
      public void onComplete() {
         SUBSCRIPTION.lazySet(this, Operators.cancelledSubscription());
         this.parent.openComplete(this);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.ACTUAL) {
            return this.parent;
         } else if (key == Scannable.Attr.PARENT) {
            return this.subscription;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return Long.MAX_VALUE;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.isDisposed();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }
   }
}
