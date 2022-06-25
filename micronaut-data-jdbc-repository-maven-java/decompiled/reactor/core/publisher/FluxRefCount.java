package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxRefCount<T> extends Flux<T> implements Scannable, Fuseable {
   final ConnectableFlux<? extends T> source;
   final int n;
   @Nullable
   FluxRefCount.RefCountMonitor<T> connection;

   FluxRefCount(ConnectableFlux<? extends T> source, int n) {
      if (n <= 0) {
         throw new IllegalArgumentException("n > 0 required but it was " + n);
      } else {
         this.source = (ConnectableFlux)Objects.requireNonNull(source, "source");
         this.n = n;
      }
   }

   @Override
   public int getPrefetch() {
      return this.source.getPrefetch();
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      boolean connect = false;
      FluxRefCount.RefCountMonitor<T> conn;
      synchronized(this) {
         conn = this.connection;
         if (conn == null || conn.terminated) {
            conn = new FluxRefCount.RefCountMonitor<>(this);
            this.connection = conn;
         }

         long c = (long)(conn.subscribers++);
         if (!conn.connected && c + 1L == (long)this.n) {
            connect = true;
            conn.connected = true;
         }
      }

      this.source.subscribe(new FluxRefCount.RefCountInner<>(actual, conn));
      if (connect) {
         this.source.connect(conn);
      }

   }

   void cancel(FluxRefCount.RefCountMonitor rc) {
      Disposable dispose = null;
      synchronized(this) {
         if (rc.terminated) {
            return;
         }

         long c = rc.subscribers - 1L;
         rc.subscribers = c;
         if (c != 0L || !rc.connected) {
            return;
         }

         if (rc == this.connection) {
            dispose = (Disposable)FluxRefCount.RefCountMonitor.DISCONNECT.getAndSet(rc, Disposables.disposed());
            this.connection = null;
         }
      }

      if (dispose != null) {
         dispose.dispose();
      }

   }

   void terminated(FluxRefCount.RefCountMonitor rc) {
      synchronized(this) {
         if (!rc.terminated) {
            rc.terminated = true;
            this.connection = null;
         }

      }
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

   static final class RefCountInner<T> implements Fuseable.QueueSubscription<T>, InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final FluxRefCount.RefCountMonitor<T> connection;
      Subscription s;
      Fuseable.QueueSubscription<T> qs;
      volatile int parentDone;
      static final AtomicIntegerFieldUpdater<FluxRefCount.RefCountInner> PARENT_DONE = AtomicIntegerFieldUpdater.newUpdater(
         FluxRefCount.RefCountInner.class, "parentDone"
      );

      RefCountInner(CoreSubscriber<? super T> actual, FluxRefCount.RefCountMonitor<T> connection) {
         this.actual = actual;
         this.connection = connection;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.parentDone == 1;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.parentDone == 2;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
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
      public void onNext(T t) {
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         if (PARENT_DONE.compareAndSet(this, 0, 1)) {
            this.connection.upstreamFinished();
            this.actual.onError(t);
         } else {
            Operators.onErrorDropped(t, this.actual.currentContext());
         }

      }

      @Override
      public void onComplete() {
         if (PARENT_DONE.compareAndSet(this, 0, 1)) {
            this.connection.upstreamFinished();
            this.actual.onComplete();
         }

      }

      @Override
      public void request(long n) {
         this.s.request(n);
      }

      @Override
      public void cancel() {
         this.s.cancel();
         if (PARENT_DONE.compareAndSet(this, 0, 2)) {
            this.connection.innerCancelled();
         }

      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
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
   }

   static final class RefCountMonitor<T> implements Consumer<Disposable> {
      final FluxRefCount<? extends T> parent;
      long subscribers;
      boolean terminated;
      boolean connected;
      volatile Disposable disconnect;
      static final AtomicReferenceFieldUpdater<FluxRefCount.RefCountMonitor, Disposable> DISCONNECT = AtomicReferenceFieldUpdater.newUpdater(
         FluxRefCount.RefCountMonitor.class, Disposable.class, "disconnect"
      );

      RefCountMonitor(FluxRefCount<? extends T> parent) {
         this.parent = parent;
      }

      public void accept(Disposable r) {
         OperatorDisposables.replace(DISCONNECT, this, r);
      }

      void innerCancelled() {
         this.parent.cancel(this);
      }

      void upstreamFinished() {
         this.parent.terminated(this);
      }
   }
}
