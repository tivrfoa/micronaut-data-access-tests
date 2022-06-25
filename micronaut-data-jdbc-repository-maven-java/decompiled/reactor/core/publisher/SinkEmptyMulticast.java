package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.stream.Stream;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

class SinkEmptyMulticast<T> extends Mono<T> implements InternalEmptySink<T> {
   volatile SinkEmptyMulticast.Inner<T>[] subscribers;
   static final AtomicReferenceFieldUpdater<SinkEmptyMulticast, SinkEmptyMulticast.Inner[]> SUBSCRIBERS = AtomicReferenceFieldUpdater.newUpdater(
      SinkEmptyMulticast.class, SinkEmptyMulticast.Inner[].class, "subscribers"
   );
   static final SinkEmptyMulticast.Inner[] EMPTY = new SinkEmptyMulticast.Inner[0];
   static final SinkEmptyMulticast.Inner[] TERMINATED = new SinkEmptyMulticast.Inner[0];
   @Nullable
   Throwable error;

   SinkEmptyMulticast() {
      SUBSCRIBERS.lazySet(this, EMPTY);
   }

   @Override
   public int currentSubscriberCount() {
      return this.subscribers.length;
   }

   @Override
   public Mono<T> asMono() {
      return this;
   }

   @Override
   public Sinks.EmitResult tryEmitEmpty() {
      SinkEmptyMulticast.Inner<?>[] array = (SinkEmptyMulticast.Inner[])SUBSCRIBERS.getAndSet(this, TERMINATED);
      if (array == TERMINATED) {
         return Sinks.EmitResult.FAIL_TERMINATED;
      } else {
         for(SinkEmptyMulticast.Inner<?> as : array) {
            as.complete();
         }

         return Sinks.EmitResult.OK;
      }
   }

   @Override
   public Sinks.EmitResult tryEmitError(Throwable cause) {
      Objects.requireNonNull(cause, "onError cannot be null");
      SinkEmptyMulticast.Inner<T>[] prevSubscribers = (SinkEmptyMulticast.Inner[])SUBSCRIBERS.getAndSet(this, TERMINATED);
      if (prevSubscribers == TERMINATED) {
         return Sinks.EmitResult.FAIL_TERMINATED;
      } else {
         this.error = cause;

         for(SinkEmptyMulticast.Inner<T> as : prevSubscribers) {
            as.error(cause);
         }

         return Sinks.EmitResult.OK;
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.TERMINATED) {
         return this.subscribers == TERMINATED;
      } else if (key == Scannable.Attr.ERROR) {
         return this.error;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   @Override
   public Context currentContext() {
      return Operators.multiSubscribersContext(this.subscribers);
   }

   boolean add(SinkEmptyMulticast.Inner<T> ps) {
      SinkEmptyMulticast.Inner<T>[] a;
      SinkEmptyMulticast.Inner<T>[] b;
      do {
         a = this.subscribers;
         if (a == TERMINATED) {
            return false;
         }

         int n = a.length;
         b = new SinkEmptyMulticast.Inner[n + 1];
         System.arraycopy(a, 0, b, 0, n);
         b[n] = ps;
      } while(!SUBSCRIBERS.compareAndSet(this, a, b));

      return true;
   }

   void remove(SinkEmptyMulticast.Inner<T> ps) {
      SinkEmptyMulticast.Inner<T>[] a;
      SinkEmptyMulticast.Inner<T>[] b;
      do {
         a = this.subscribers;
         int n = a.length;
         if (n == 0) {
            return;
         }

         int j = -1;

         for(int i = 0; i < n; ++i) {
            if (a[i] == ps) {
               j = i;
               break;
            }
         }

         if (j < 0) {
            return;
         }

         if (n == 1) {
            b = EMPTY;
         } else {
            b = new SinkEmptyMulticast.Inner[n - 1];
            System.arraycopy(a, 0, b, 0, j);
            System.arraycopy(a, j + 1, b, j, n - j - 1);
         }
      } while(!SUBSCRIBERS.compareAndSet(this, a, b));

   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      SinkEmptyMulticast.Inner<T> as = new SinkEmptyMulticast.VoidInner<>(actual, this);
      actual.onSubscribe(as);
      if (this.add(as)) {
         if (as.isCancelled()) {
            this.remove(as);
         }
      } else {
         Throwable ex = this.error;
         if (ex != null) {
            actual.onError(ex);
         } else {
            as.complete();
         }
      }

   }

   @Override
   public Stream<? extends Scannable> inners() {
      return Stream.of(this.subscribers);
   }

   interface Inner<T> extends InnerProducer<T> {
      void error(Throwable var1);

      void complete(T var1);

      void complete();

      boolean isCancelled();
   }

   static final class VoidInner<T> extends AtomicBoolean implements SinkEmptyMulticast.Inner<T> {
      final SinkEmptyMulticast<T> parent;
      final CoreSubscriber<? super T> actual;

      VoidInner(CoreSubscriber<? super T> actual, SinkEmptyMulticast<T> parent) {
         this.actual = actual;
         this.parent = parent;
      }

      @Override
      public void cancel() {
         if (!this.getAndSet(true)) {
            this.parent.remove(this);
         }
      }

      @Override
      public boolean isCancelled() {
         return this.get();
      }

      @Override
      public void request(long l) {
         Operators.validate(l);
      }

      @Override
      public void complete(T value) {
      }

      @Override
      public void complete() {
         if (!this.get()) {
            this.actual.onComplete();
         }
      }

      @Override
      public void error(Throwable t) {
         if (this.get()) {
            Operators.onOperatorError(t, this.actual.currentContext());
         } else {
            this.actual.onError(t);
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.parent;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.get();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : SinkEmptyMulticast.Inner.super.scanUnsafe(key);
         }
      }
   }
}
