package reactor.core.publisher;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiPredicate;
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

final class FluxSwitchMap<T, R> extends InternalFluxOperator<T, R> {
   final Function<? super T, ? extends Publisher<? extends R>> mapper;
   final Supplier<? extends Queue<Object>> queueSupplier;
   final int prefetch;
   static final FluxSwitchMap.SwitchMapInner<Object> CANCELLED_INNER = new FluxSwitchMap.SwitchMapInner<>(null, 0, Long.MAX_VALUE);

   FluxSwitchMap(
      Flux<? extends T> source, Function<? super T, ? extends Publisher<? extends R>> mapper, Supplier<? extends Queue<Object>> queueSupplier, int prefetch
   ) {
      super(source);
      if (prefetch <= 0) {
         throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
      } else {
         this.mapper = (Function)Objects.requireNonNull(mapper, "mapper");
         this.queueSupplier = (Supplier)Objects.requireNonNull(queueSupplier, "queueSupplier");
         this.prefetch = prefetch;
      }
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      return FluxFlatMap.trySubscribeScalarMap(this.source, actual, this.mapper, false, false)
         ? null
         : new FluxSwitchMap.SwitchMapMain<>(actual, this.mapper, (Queue<Object>)this.queueSupplier.get(), this.prefetch);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class SwitchMapInner<R> implements InnerConsumer<R>, Subscription {
      final FluxSwitchMap.SwitchMapMain<?, R> parent;
      final int prefetch;
      final int limit;
      final long index;
      volatile int once;
      static final AtomicIntegerFieldUpdater<FluxSwitchMap.SwitchMapInner> ONCE = AtomicIntegerFieldUpdater.newUpdater(
         FluxSwitchMap.SwitchMapInner.class, "once"
      );
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<FluxSwitchMap.SwitchMapInner, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         FluxSwitchMap.SwitchMapInner.class, Subscription.class, "s"
      );
      int produced;

      SwitchMapInner(FluxSwitchMap.SwitchMapMain<?, R> parent, int prefetch, long index) {
         this.parent = parent;
         this.prefetch = prefetch;
         this.limit = Operators.unboundedOrLimit(prefetch);
         this.index = index;
      }

      @Override
      public Context currentContext() {
         return this.parent.currentContext();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.s == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.parent;
         } else if (key == Scannable.Attr.PREFETCH) {
            return this.prefetch;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         Subscription a = this.s;
         if (a == Operators.cancelledSubscription()) {
            s.cancel();
         }

         if (a != null) {
            s.cancel();
            Operators.reportSubscriptionSet();
         } else if (S.compareAndSet(this, null, s)) {
            s.request(Operators.unboundedOrPrefetch(this.prefetch));
         } else {
            a = this.s;
            if (a != Operators.cancelledSubscription()) {
               s.cancel();
               Operators.reportSubscriptionSet();
            }

         }
      }

      @Override
      public void onNext(R t) {
         this.parent.innerNext(this, t);
      }

      @Override
      public void onError(Throwable t) {
         this.parent.innerError(this, t);
      }

      @Override
      public void onComplete() {
         this.parent.innerComplete(this);
      }

      void deactivate() {
         if (ONCE.compareAndSet(this, 0, 1)) {
            this.parent.deactivate();
         }

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

      @Override
      public void request(long n) {
         long p = (long)this.produced + n;
         if (p >= (long)this.limit) {
            this.produced = 0;
            this.s.request(p);
         } else {
            this.produced = (int)p;
         }

      }

      @Override
      public void cancel() {
         Subscription a = this.s;
         if (a != Operators.cancelledSubscription()) {
            a = (Subscription)S.getAndSet(this, Operators.cancelledSubscription());
            if (a != null && a != Operators.cancelledSubscription()) {
               a.cancel();
            }
         }

      }
   }

   static final class SwitchMapMain<T, R> implements InnerOperator<T, R> {
      final Function<? super T, ? extends Publisher<? extends R>> mapper;
      final Queue<Object> queue;
      final BiPredicate<Object, Object> queueBiAtomic;
      final int prefetch;
      final CoreSubscriber<? super R> actual;
      Subscription s;
      volatile boolean done;
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxSwitchMap.SwitchMapMain, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxSwitchMap.SwitchMapMain.class, Throwable.class, "error"
      );
      volatile boolean cancelled;
      volatile int once;
      static final AtomicIntegerFieldUpdater<FluxSwitchMap.SwitchMapMain> ONCE = AtomicIntegerFieldUpdater.newUpdater(FluxSwitchMap.SwitchMapMain.class, "once");
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxSwitchMap.SwitchMapMain> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxSwitchMap.SwitchMapMain.class, "requested"
      );
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxSwitchMap.SwitchMapMain> WIP = AtomicIntegerFieldUpdater.newUpdater(FluxSwitchMap.SwitchMapMain.class, "wip");
      volatile FluxSwitchMap.SwitchMapInner<R> inner;
      static final AtomicReferenceFieldUpdater<FluxSwitchMap.SwitchMapMain, FluxSwitchMap.SwitchMapInner> INNER = AtomicReferenceFieldUpdater.newUpdater(
         FluxSwitchMap.SwitchMapMain.class, FluxSwitchMap.SwitchMapInner.class, "inner"
      );
      volatile long index;
      static final AtomicLongFieldUpdater<FluxSwitchMap.SwitchMapMain> INDEX = AtomicLongFieldUpdater.newUpdater(FluxSwitchMap.SwitchMapMain.class, "index");
      volatile int active;
      static final AtomicIntegerFieldUpdater<FluxSwitchMap.SwitchMapMain> ACTIVE = AtomicIntegerFieldUpdater.newUpdater(
         FluxSwitchMap.SwitchMapMain.class, "active"
      );

      SwitchMapMain(CoreSubscriber<? super R> actual, Function<? super T, ? extends Publisher<? extends R>> mapper, Queue<Object> queue, int prefetch) {
         this.actual = actual;
         this.mapper = mapper;
         this.queue = queue;
         this.prefetch = prefetch;
         this.active = 1;
         if (queue instanceof BiPredicate) {
            this.queueBiAtomic = (BiPredicate)queue;
         } else {
            this.queueBiAtomic = null;
         }

      }

      @Override
      public final CoreSubscriber<? super R> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.PREFETCH) {
            return this.prefetch;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue.size();
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.inner);
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            long idx = INDEX.incrementAndGet(this);
            FluxSwitchMap.SwitchMapInner<R> si = this.inner;
            if (si != null) {
               si.deactivate();
               si.cancel();
            }

            Publisher<? extends R> p;
            try {
               p = (Publisher)Objects.requireNonNull(this.mapper.apply(t), "The mapper returned a null publisher");
            } catch (Throwable var7) {
               this.onError(Operators.onOperatorError(this.s, var7, t, this.actual.currentContext()));
               return;
            }

            FluxSwitchMap.SwitchMapInner<R> innerSubscriber = new FluxSwitchMap.SwitchMapInner<>(this, this.prefetch, idx);
            if (INNER.compareAndSet(this, si, innerSubscriber)) {
               ACTIVE.getAndIncrement(this);
               p.subscribe(innerSubscriber);
            }

         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            if (Exceptions.addThrowable(ERROR, this, t)) {
               if (ONCE.compareAndSet(this, 0, 1)) {
                  this.deactivate();
               }

               this.cancelInner();
               this.done = true;
               this.drain();
            } else {
               Operators.onErrorDropped(t, this.actual.currentContext());
            }

         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            if (ONCE.compareAndSet(this, 0, 1)) {
               this.deactivate();
            }

            this.done = true;
            this.drain();
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
            if (WIP.getAndIncrement(this) == 0) {
               this.cancelAndCleanup(this.queue);
            }
         }

      }

      void deactivate() {
         ACTIVE.decrementAndGet(this);
      }

      void cancelInner() {
         FluxSwitchMap.SwitchMapInner<?> si = (FluxSwitchMap.SwitchMapInner)INNER.getAndSet(this, FluxSwitchMap.CANCELLED_INNER);
         if (si != null && si != FluxSwitchMap.CANCELLED_INNER) {
            si.cancel();
            si.deactivate();
         }

      }

      void cancelAndCleanup(Queue<?> q) {
         this.s.cancel();
         this.cancelInner();
         q.clear();
      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            Subscriber<? super R> a = this.actual;
            Queue<Object> q = this.queue;
            int missed = 1;

            do {
               long r = this.requested;
               long e = 0L;

               while(r != e) {
                  boolean d = this.active == 0;
                  FluxSwitchMap.SwitchMapInner<R> si = (FluxSwitchMap.SwitchMapInner)q.poll();
                  boolean empty = si == null;
                  if (this.checkTerminated(d, empty, a, q)) {
                     return;
                  }

                  if (empty) {
                     break;
                  }

                  Object second;
                  while((second = q.poll()) == null) {
                  }

                  if (this.index == si.index) {
                     a.onNext((R)second);
                     si.requestOne();
                     ++e;
                  }
               }

               if (r == e && this.checkTerminated(this.active == 0, q.isEmpty(), a, q)) {
                  return;
               }

               if (e != 0L && r != Long.MAX_VALUE) {
                  REQUESTED.addAndGet(this, -e);
               }

               missed = WIP.addAndGet(this, -missed);
            } while(missed != 0);

         }
      }

      boolean checkTerminated(boolean d, boolean empty, Subscriber<?> a, Queue<?> q) {
         if (this.cancelled) {
            this.cancelAndCleanup(q);
            return true;
         } else {
            if (d) {
               Throwable e = Exceptions.terminate(ERROR, this);
               if (e != null && e != Exceptions.TERMINATED) {
                  this.cancelAndCleanup(q);
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

      void innerNext(FluxSwitchMap.SwitchMapInner<R> inner, R value) {
         if (this.queueBiAtomic != null) {
            this.queueBiAtomic.test(inner, value);
         } else {
            this.queue.offer(inner);
            this.queue.offer(value);
         }

         this.drain();
      }

      void innerError(FluxSwitchMap.SwitchMapInner<R> inner, Throwable e) {
         if (Exceptions.addThrowable(ERROR, this, e)) {
            this.s.cancel();
            if (ONCE.compareAndSet(this, 0, 1)) {
               this.deactivate();
            }

            inner.deactivate();
            this.drain();
         } else {
            Operators.onErrorDropped(e, this.actual.currentContext());
         }

      }

      void innerComplete(FluxSwitchMap.SwitchMapInner<R> inner) {
         inner.deactivate();
         this.drain();
      }
   }
}
