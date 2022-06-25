package reactor.core.publisher;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;

final class FluxExpand<T> extends InternalFluxOperator<T, T> {
   final boolean breadthFirst;
   final Function<? super T, ? extends Publisher<? extends T>> expander;
   final int capacityHint;

   FluxExpand(Flux<T> source, Function<? super T, ? extends Publisher<? extends T>> expander, boolean breadthFirst, int capacityHint) {
      super(source);
      this.expander = expander;
      this.breadthFirst = breadthFirst;
      this.capacityHint = capacityHint;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> s) {
      if (this.breadthFirst) {
         FluxExpand.ExpandBreathSubscriber<T> parent = new FluxExpand.ExpandBreathSubscriber<>(s, this.expander, this.capacityHint);
         parent.queue.offer(this.source);
         s.onSubscribe(parent);
         parent.drainQueue();
      } else {
         FluxExpand.ExpandDepthSubscription<T> parent = new FluxExpand.ExpandDepthSubscription<>(s, this.expander, this.capacityHint);
         parent.source = this.source;
         s.onSubscribe(parent);
      }

      return null;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class ExpandBreathSubscriber<T> extends Operators.MultiSubscriptionSubscriber<T, T> {
      final Function<? super T, ? extends Publisher<? extends T>> expander;
      final Queue<Publisher<? extends T>> queue;
      volatile boolean active;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxExpand.ExpandBreathSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxExpand.ExpandBreathSubscriber.class, "wip"
      );
      long produced;

      ExpandBreathSubscriber(CoreSubscriber<? super T> actual, Function<? super T, ? extends Publisher<? extends T>> expander, int capacityHint) {
         super(actual);
         this.expander = expander;
         this.queue = (Queue)Queues.unbounded(capacityHint).get();
      }

      @Override
      public void onSubscribe(Subscription s) {
         this.set(s);
      }

      @Override
      public void onNext(T t) {
         ++this.produced;
         this.actual.onNext(t);

         Publisher<? extends T> p;
         try {
            p = (Publisher)Objects.requireNonNull(this.expander.apply(t), "The expander returned a null Publisher");
         } catch (Throwable var4) {
            Exceptions.throwIfFatal(var4);
            super.cancel();
            this.actual.onError(var4);
            this.drainQueue();
            return;
         }

         this.queue.offer(p);
      }

      @Override
      public void onError(Throwable t) {
         this.set(Operators.cancelledSubscription());
         super.cancel();
         this.actual.onError(t);
         this.drainQueue();
      }

      @Override
      public void onComplete() {
         this.active = false;
         this.drainQueue();
      }

      @Override
      public void cancel() {
         super.cancel();
         this.drainQueue();
      }

      void drainQueue() {
         if (WIP.getAndIncrement(this) == 0) {
            do {
               Queue<Publisher<? extends T>> q = this.queue;
               if (this.isCancelled()) {
                  q.clear();
               } else if (!this.active) {
                  if (q.isEmpty()) {
                     this.set(Operators.cancelledSubscription());
                     super.cancel();
                     this.actual.onComplete();
                  } else {
                     Publisher<? extends T> p = (Publisher)q.poll();
                     long c = this.produced;
                     if (c != 0L) {
                        this.produced = 0L;
                        this.produced(c);
                     }

                     this.active = true;
                     p.subscribe(this);
                  }
               }
            } while(WIP.decrementAndGet(this) != 0);
         }

      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.BUFFERED) {
            return this.queue != null ? this.queue.size() : 0;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         }
      }
   }

   static final class ExpandDepthSubscriber<T> implements InnerConsumer<T> {
      FluxExpand.ExpandDepthSubscription<T> parent;
      volatile boolean done;
      volatile T value;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<FluxExpand.ExpandDepthSubscriber, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         FluxExpand.ExpandDepthSubscriber.class, Subscription.class, "s"
      );

      ExpandDepthSubscriber(FluxExpand.ExpandDepthSubscription<T> parent) {
         this.parent = parent;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            s.request(1L);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.s != Operators.cancelledSubscription()) {
            this.value = t;
            this.parent.innerNext();
         }

      }

      @Override
      public void onError(Throwable t) {
         if (this.s != Operators.cancelledSubscription()) {
            this.parent.innerError(this, t);
         }

      }

      @Override
      public void onComplete() {
         if (this.s != Operators.cancelledSubscription()) {
            this.parent.innerComplete(this);
         }

      }

      void requestOne() {
         this.s.request(1L);
      }

      void dispose() {
         Operators.terminate(S, this);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.parent.actual;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public Context currentContext() {
         return this.parent.actual().currentContext();
      }
   }

   static final class ExpandDepthSubscription<T> implements InnerProducer<T> {
      final CoreSubscriber<? super T> actual;
      final Function<? super T, ? extends Publisher<? extends T>> expander;
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxExpand.ExpandDepthSubscription, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxExpand.ExpandDepthSubscription.class, Throwable.class, "error"
      );
      volatile int active;
      static final AtomicIntegerFieldUpdater<FluxExpand.ExpandDepthSubscription> ACTIVE = AtomicIntegerFieldUpdater.newUpdater(
         FluxExpand.ExpandDepthSubscription.class, "active"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxExpand.ExpandDepthSubscription> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxExpand.ExpandDepthSubscription.class, "requested"
      );
      volatile Object current;
      static final AtomicReferenceFieldUpdater<FluxExpand.ExpandDepthSubscription, Object> CURRENT = AtomicReferenceFieldUpdater.newUpdater(
         FluxExpand.ExpandDepthSubscription.class, Object.class, "current"
      );
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxExpand.ExpandDepthSubscription> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxExpand.ExpandDepthSubscription.class, "wip"
      );
      Deque<FluxExpand.ExpandDepthSubscriber<T>> subscriptionStack;
      volatile boolean cancelled;
      CorePublisher<? extends T> source;
      long consumed;

      ExpandDepthSubscription(CoreSubscriber<? super T> actual, Function<? super T, ? extends Publisher<? extends T>> expander, int capacityHint) {
         this.actual = actual;
         this.expander = expander;
         this.subscriptionStack = new ArrayDeque(capacityHint);
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
            this.drainQueue();
         }

      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            Deque<FluxExpand.ExpandDepthSubscriber<T>> q;
            synchronized(this) {
               q = this.subscriptionStack;
               this.subscriptionStack = null;
            }

            if (q != null) {
               while(!q.isEmpty()) {
                  ((FluxExpand.ExpandDepthSubscriber)q.poll()).dispose();
               }
            }

            Object o = CURRENT.getAndSet(this, this);
            if (o != this && o != null) {
               ((FluxExpand.ExpandDepthSubscriber)o).dispose();
            }
         }

      }

      @Nullable
      FluxExpand.ExpandDepthSubscriber<T> pop() {
         synchronized(this) {
            Deque<FluxExpand.ExpandDepthSubscriber<T>> q = this.subscriptionStack;
            return q != null ? (FluxExpand.ExpandDepthSubscriber)q.pollFirst() : null;
         }
      }

      boolean push(FluxExpand.ExpandDepthSubscriber<T> subscriber) {
         synchronized(this) {
            Deque<FluxExpand.ExpandDepthSubscriber<T>> q = this.subscriptionStack;
            if (q != null) {
               q.offerFirst(subscriber);
               return true;
            } else {
               return false;
            }
         }
      }

      boolean setCurrent(FluxExpand.ExpandDepthSubscriber<T> inner) {
         Object o;
         do {
            o = CURRENT.get(this);
            if (o == this) {
               inner.dispose();
               return false;
            }
         } while(!CURRENT.compareAndSet(this, o, inner));

         return true;
      }

      void drainQueue() {
         if (WIP.getAndIncrement(this) == 0) {
            int missed = 1;
            Subscriber<? super T> a = this.actual;
            long e = this.consumed;

            while(true) {
               Object o = this.current;
               if (this.cancelled || o == this) {
                  this.source = null;
                  return;
               }

               FluxExpand.ExpandDepthSubscriber<T> curr = (FluxExpand.ExpandDepthSubscriber)o;
               Publisher<? extends T> p = this.source;
               if (curr == null && p != null) {
                  this.source = null;
                  ACTIVE.getAndIncrement(this);
                  FluxExpand.ExpandDepthSubscriber<T> eds = new FluxExpand.ExpandDepthSubscriber<>(this);
                  if (!this.setCurrent(eds)) {
                     return;
                  }

                  p.subscribe(eds);
               } else {
                  if (curr == null) {
                     return;
                  }

                  boolean currentDone = curr.done;
                  T v = curr.value;
                  boolean newSource = false;
                  if (v != null && e != this.requested) {
                     curr.value = null;
                     a.onNext(v);
                     ++e;

                     try {
                        p = (Publisher)Objects.requireNonNull(this.expander.apply(v), "The expander returned a null Publisher");
                     } catch (Throwable var12) {
                        Exceptions.throwIfFatal(var12);
                        p = null;
                        curr.dispose();
                        curr.done = true;
                        currentDone = true;
                        v = null;
                        Exceptions.addThrowable(ERROR, this, var12);
                     }

                     if (p != null && this.push(curr)) {
                        ACTIVE.getAndIncrement(this);
                        curr = new FluxExpand.ExpandDepthSubscriber<>(this);
                        if (!this.setCurrent(curr)) {
                           return;
                        }

                        p.subscribe(curr);
                        newSource = true;
                     }
                  }

                  if (!newSource && currentDone && v == null) {
                     if (ACTIVE.decrementAndGet(this) == 0) {
                        Throwable ex = Exceptions.terminate(ERROR, this);
                        if (ex != null) {
                           a.onError(ex);
                        } else {
                           a.onComplete();
                        }

                        return;
                     }

                     curr = this.pop();
                     if (curr != null && this.setCurrent(curr)) {
                        curr.requestOne();
                        continue;
                     }

                     return;
                  }
               }

               int w = this.wip;
               if (missed == w) {
                  this.consumed = e;
                  missed = WIP.addAndGet(this, -missed);
                  if (missed == 0) {
                     return;
                  }
               } else {
                  missed = w;
               }
            }
         }
      }

      void innerNext() {
         this.drainQueue();
      }

      void innerError(FluxExpand.ExpandDepthSubscriber inner, Throwable t) {
         Exceptions.addThrowable(ERROR, this, t);
         inner.done = true;
         this.drainQueue();
      }

      void innerComplete(FluxExpand.ExpandDepthSubscriber inner) {
         inner.done = true;
         this.drainQueue();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else {
            return key == Scannable.Attr.ERROR ? this.error : InnerProducer.super.scanUnsafe(key);
         }
      }
   }
}
