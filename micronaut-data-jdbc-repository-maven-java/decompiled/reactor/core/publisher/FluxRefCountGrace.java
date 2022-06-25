package reactor.core.publisher;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.Nullable;

final class FluxRefCountGrace<T> extends Flux<T> implements Scannable, Fuseable {
   final ConnectableFlux<T> source;
   final int n;
   final Duration gracePeriod;
   final Scheduler scheduler;
   FluxRefCountGrace.RefConnection connection;

   FluxRefCountGrace(ConnectableFlux<T> source, int n, Duration gracePeriod, Scheduler scheduler) {
      this.source = source;
      this.n = n;
      this.gracePeriod = gracePeriod;
      this.scheduler = scheduler;
   }

   @Override
   public int getPrefetch() {
      return this.source.getPrefetch();
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PREFETCH) {
         return this.getPrefetch();
      } else if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      boolean connect = false;
      FluxRefCountGrace.RefConnection conn;
      synchronized(this) {
         conn = this.connection;
         if (conn == null || conn.isTerminated()) {
            conn = new FluxRefCountGrace.RefConnection(this);
            this.connection = conn;
         }

         long c = conn.subscriberCount;
         if (c == 0L && conn.timer != null) {
            conn.timer.dispose();
         }

         conn.subscriberCount = c + 1L;
         if (!conn.connected && c + 1L == (long)this.n) {
            connect = true;
            conn.connected = true;
         }
      }

      this.source.subscribe(new FluxRefCountGrace.RefCountInner<>(actual, this, conn));
      if (connect) {
         this.source.connect(conn);
      }

   }

   void cancel(FluxRefCountGrace.RefConnection rc) {
      boolean replaceTimer = false;
      Disposable dispose = null;
      Disposable.Swap sd = null;
      synchronized(this) {
         if (rc.terminated) {
            return;
         }

         long c = rc.subscriberCount - 1L;
         rc.subscriberCount = c;
         if (c != 0L || !rc.connected) {
            return;
         }

         if (!this.gracePeriod.isZero()) {
            sd = Disposables.swap();
            rc.timer = sd;
            replaceTimer = true;
         } else if (rc == this.connection) {
            this.connection = null;
            dispose = (Disposable)FluxRefCountGrace.RefConnection.SOURCE_DISCONNECTOR.getAndSet(rc, Disposables.disposed());
         }
      }

      if (replaceTimer) {
         sd.replace(this.scheduler.schedule(rc, this.gracePeriod.toNanos(), TimeUnit.NANOSECONDS));
      } else if (dispose != null) {
         dispose.dispose();
      }

   }

   void terminated(FluxRefCountGrace.RefConnection rc) {
      synchronized(this) {
         if (!rc.terminated) {
            rc.terminated = true;
            this.connection = null;
         }

      }
   }

   void timeout(FluxRefCountGrace.RefConnection rc) {
      Disposable dispose = null;
      synchronized(this) {
         if (rc.subscriberCount == 0L && rc == this.connection) {
            this.connection = null;
            dispose = (Disposable)FluxRefCountGrace.RefConnection.SOURCE_DISCONNECTOR.getAndSet(rc, Disposables.disposed());
         }
      }

      if (dispose != null) {
         dispose.dispose();
      }

   }

   static final class RefConnection implements Runnable, Consumer<Disposable> {
      final FluxRefCountGrace<?> parent;
      Disposable timer;
      long subscriberCount;
      boolean connected;
      boolean terminated;
      volatile Disposable sourceDisconnector;
      static final AtomicReferenceFieldUpdater<FluxRefCountGrace.RefConnection, Disposable> SOURCE_DISCONNECTOR = AtomicReferenceFieldUpdater.newUpdater(
         FluxRefCountGrace.RefConnection.class, Disposable.class, "sourceDisconnector"
      );

      RefConnection(FluxRefCountGrace<?> parent) {
         this.parent = parent;
      }

      boolean isTerminated() {
         Disposable sd = this.sourceDisconnector;
         return this.terminated || sd != null && sd.isDisposed();
      }

      public void run() {
         this.parent.timeout(this);
      }

      public void accept(Disposable t) {
         OperatorDisposables.replace(SOURCE_DISCONNECTOR, this, t);
      }
   }

   static final class RefCountInner<T> implements Fuseable.QueueSubscription<T>, InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final FluxRefCountGrace<T> parent;
      final FluxRefCountGrace.RefConnection connection;
      Subscription s;
      Fuseable.QueueSubscription<T> qs;
      volatile int parentDone;
      static final AtomicIntegerFieldUpdater<FluxRefCountGrace.RefCountInner> PARENT_DONE = AtomicIntegerFieldUpdater.newUpdater(
         FluxRefCountGrace.RefCountInner.class, "parentDone"
      );

      RefCountInner(CoreSubscriber<? super T> actual, FluxRefCountGrace<T> parent, FluxRefCountGrace.RefConnection connection) {
         this.actual = actual;
         this.parent = parent;
         this.connection = connection;
      }

      @Override
      public void onNext(T t) {
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         if (PARENT_DONE.compareAndSet(this, 0, 1)) {
            this.parent.terminated(this.connection);
         }

         this.actual.onError(t);
      }

      @Override
      public void onComplete() {
         if (PARENT_DONE.compareAndSet(this, 0, 1)) {
            this.parent.terminated(this.connection);
         }

         this.actual.onComplete();
      }

      @Override
      public void request(long n) {
         this.s.request(n);
      }

      @Override
      public void cancel() {
         this.s.cancel();
         if (PARENT_DONE.compareAndSet(this, 0, 1)) {
            this.parent.cancel(this.connection);
         }

      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public int requestFusion(int requestedMode) {
         if (this.s instanceof Fuseable.QueueSubscription) {
            this.qs = (Fuseable.QueueSubscription)this.s;
            return this.qs.requestFusion(requestedMode);
         } else {
            return 0;
         }
      }

      @Nullable
      public T poll() {
         return (T)this.qs.poll();
      }

      public int size() {
         return this.qs.size();
      }

      public boolean isEmpty() {
         return this.qs.isEmpty();
      }

      public void clear() {
         this.qs.clear();
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
      }
   }
}
