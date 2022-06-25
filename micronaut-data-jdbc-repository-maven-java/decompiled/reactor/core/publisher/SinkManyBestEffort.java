package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.stream.Stream;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class SinkManyBestEffort<T> extends Flux<T> implements InternalManySink<T>, Scannable, DirectInnerContainer<T> {
   static final SinkManyBestEffort.DirectInner[] EMPTY = new SinkManyBestEffort.DirectInner[0];
   static final SinkManyBestEffort.DirectInner[] TERMINATED = new SinkManyBestEffort.DirectInner[0];
   final boolean allOrNothing;
   Throwable error;
   volatile SinkManyBestEffort.DirectInner<T>[] subscribers;
   static final AtomicReferenceFieldUpdater<SinkManyBestEffort, SinkManyBestEffort.DirectInner[]> SUBSCRIBERS = AtomicReferenceFieldUpdater.newUpdater(
      SinkManyBestEffort.class, SinkManyBestEffort.DirectInner[].class, "subscribers"
   );

   static final <T> SinkManyBestEffort<T> createBestEffort() {
      return new SinkManyBestEffort<>(false);
   }

   static final <T> SinkManyBestEffort<T> createAllOrNothing() {
      return new SinkManyBestEffort<>(true);
   }

   SinkManyBestEffort(boolean allOrNothing) {
      this.allOrNothing = allOrNothing;
      SUBSCRIBERS.lazySet(this, EMPTY);
   }

   @Override
   public Context currentContext() {
      return Operators.multiSubscribersContext(this.subscribers);
   }

   @Override
   public Stream<? extends Scannable> inners() {
      return Stream.of(this.subscribers);
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.TERMINATED) {
         return this.subscribers == TERMINATED;
      } else {
         return key == Scannable.Attr.ERROR ? this.error : null;
      }
   }

   @Override
   public Sinks.EmitResult tryEmitNext(T t) {
      Objects.requireNonNull(t, "tryEmitNext(null) is forbidden");
      SinkManyBestEffort.DirectInner<T>[] subs = this.subscribers;
      if (subs == EMPTY) {
         return Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER;
      } else if (subs == TERMINATED) {
         return Sinks.EmitResult.FAIL_TERMINATED;
      } else {
         int expectedEmitted = subs.length;
         int cancelledCount = 0;
         if (this.allOrNothing) {
            long commonRequest = Long.MAX_VALUE;

            for(SinkManyBestEffort.DirectInner<T> sub : subs) {
               long subRequest = sub.requested;
               if (sub.isCancelled()) {
                  ++cancelledCount;
               } else if (subRequest < commonRequest) {
                  commonRequest = subRequest;
               }
            }

            if (commonRequest == 0L) {
               return Sinks.EmitResult.FAIL_OVERFLOW;
            }

            if (cancelledCount == expectedEmitted) {
               return Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER;
            }
         }

         int emittedCount = 0;
         cancelledCount = 0;

         for(SinkManyBestEffort.DirectInner<T> sub : subs) {
            if (sub.isCancelled()) {
               ++cancelledCount;
            } else if (sub.tryEmitNext(t)) {
               ++emittedCount;
            } else if (sub.isCancelled()) {
               ++cancelledCount;
            }
         }

         if (cancelledCount == expectedEmitted) {
            return Sinks.EmitResult.FAIL_ZERO_SUBSCRIBER;
         } else if (cancelledCount + emittedCount == expectedEmitted) {
            return Sinks.EmitResult.OK;
         } else {
            return emittedCount > 0 && !this.allOrNothing ? Sinks.EmitResult.OK : Sinks.EmitResult.FAIL_OVERFLOW;
         }
      }
   }

   @Override
   public Sinks.EmitResult tryEmitComplete() {
      SinkManyBestEffort.DirectInner<T>[] subs = (SinkManyBestEffort.DirectInner[])SUBSCRIBERS.getAndSet(this, TERMINATED);
      if (subs == TERMINATED) {
         return Sinks.EmitResult.FAIL_TERMINATED;
      } else {
         for(SinkManyBestEffort.DirectInner<?> s : subs) {
            s.emitComplete();
         }

         return Sinks.EmitResult.OK;
      }
   }

   @Override
   public Sinks.EmitResult tryEmitError(Throwable error) {
      Objects.requireNonNull(error, "tryEmitError(null) is forbidden");
      SinkManyBestEffort.DirectInner<T>[] subs = (SinkManyBestEffort.DirectInner[])SUBSCRIBERS.getAndSet(this, TERMINATED);
      if (subs == TERMINATED) {
         return Sinks.EmitResult.FAIL_TERMINATED;
      } else {
         this.error = error;

         for(SinkManyBestEffort.DirectInner<?> s : subs) {
            s.emitError(error);
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
   public void subscribe(CoreSubscriber<? super T> actual) {
      Objects.requireNonNull(actual, "subscribe(null) is forbidden");
      SinkManyBestEffort.DirectInner<T> p = new SinkManyBestEffort.DirectInner<>(actual, this);
      actual.onSubscribe(p);
      if (!p.isCancelled()) {
         if (this.add(p)) {
            if (p.isCancelled()) {
               this.remove(p);
            }
         } else {
            Throwable e = this.error;
            if (e != null) {
               actual.onError(e);
            } else {
               actual.onComplete();
            }
         }

      }
   }

   @Override
   public boolean add(SinkManyBestEffort.DirectInner<T> s) {
      SinkManyBestEffort.DirectInner<T>[] a = this.subscribers;
      if (a == TERMINATED) {
         return false;
      } else {
         SinkManyBestEffort.DirectInner<T>[] b;
         do {
            a = this.subscribers;
            if (a == TERMINATED) {
               return false;
            }

            int len = a.length;
            b = new SinkManyBestEffort.DirectInner[len + 1];
            System.arraycopy(a, 0, b, 0, len);
            b[len] = s;
         } while(!SUBSCRIBERS.compareAndSet(this, a, b));

         return true;
      }
   }

   @Override
   public void remove(SinkManyBestEffort.DirectInner<T> s) {
      SinkManyBestEffort.DirectInner<T>[] a = this.subscribers;
      if (a != TERMINATED && a != EMPTY) {
         SinkManyBestEffort.DirectInner<T>[] b;
         do {
            a = this.subscribers;
            if (a == TERMINATED || a == EMPTY) {
               return;
            }

            int len = a.length;
            int j = -1;

            for(int i = 0; i < len; ++i) {
               if (a[i] == s) {
                  j = i;
                  break;
               }
            }

            if (j < 0) {
               return;
            }

            if (len == 1) {
               b = EMPTY;
            } else {
               b = new SinkManyBestEffort.DirectInner[len - 1];
               System.arraycopy(a, 0, b, 0, j);
               System.arraycopy(a, j + 1, b, j, len - j - 1);
            }
         } while(!SUBSCRIBERS.compareAndSet(this, a, b));

      }
   }

   static class DirectInner<T> extends AtomicBoolean implements InnerProducer<T> {
      final CoreSubscriber<? super T> actual;
      final DirectInnerContainer<T> parent;
      volatile long requested;
      static final AtomicLongFieldUpdater<SinkManyBestEffort.DirectInner> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         SinkManyBestEffort.DirectInner.class, "requested"
      );

      DirectInner(CoreSubscriber<? super T> actual, DirectInnerContainer<T> parent) {
         this.actual = actual;
         this.parent = parent;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
         }

      }

      @Override
      public void cancel() {
         if (this.compareAndSet(false, true)) {
            this.parent.remove(this);
         }

      }

      boolean isCancelled() {
         return this.get();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.parent;
         } else {
            return key == Scannable.Attr.CANCELLED ? this.isCancelled() : InnerProducer.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      boolean tryEmitNext(T value) {
         if (this.requested != 0L) {
            if (this.isCancelled()) {
               return false;
            } else {
               this.actual.onNext(value);
               if (this.requested != Long.MAX_VALUE) {
                  REQUESTED.decrementAndGet(this);
               }

               return true;
            }
         } else {
            return false;
         }
      }

      void directEmitNext(T value) {
         if (this.requested != 0L) {
            this.actual.onNext(value);
            if (this.requested != Long.MAX_VALUE) {
               REQUESTED.decrementAndGet(this);
            }

         } else {
            this.parent.remove(this);
            this.actual.onError(Exceptions.failWithOverflow("Can't deliver value due to lack of requests"));
         }
      }

      void emitError(Throwable e) {
         if (!this.isCancelled()) {
            this.actual.onError(e);
         }
      }

      void emitComplete() {
         if (!this.isCancelled()) {
            this.actual.onComplete();
         }
      }
   }
}
