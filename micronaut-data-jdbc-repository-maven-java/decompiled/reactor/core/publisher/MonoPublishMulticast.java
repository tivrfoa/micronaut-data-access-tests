package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.stream.Stream;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class MonoPublishMulticast<T, R> extends InternalMonoOperator<T, R> implements Fuseable {
   final Function<? super Mono<T>, ? extends Mono<? extends R>> transform;

   MonoPublishMulticast(Mono<? extends T> source, Function<? super Mono<T>, ? extends Mono<? extends R>> transform) {
      super(source);
      this.transform = (Function)Objects.requireNonNull(transform, "transform");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      MonoPublishMulticast.MonoPublishMulticaster<T> multicast = new MonoPublishMulticast.MonoPublishMulticaster<>(actual.currentContext());
      Mono<? extends R> out = (Mono)Objects.requireNonNull(this.transform.apply(fromDirect(multicast)), "The transform returned a null Mono");
      if (out instanceof Fuseable) {
         out.subscribe(new FluxPublishMulticast.CancelFuseableMulticaster<>(actual, multicast));
      } else {
         out.subscribe(new FluxPublishMulticast.CancelMulticaster<>(actual, multicast));
      }

      return multicast;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class MonoPublishMulticaster<T> extends Mono<T> implements InnerConsumer<T>, FluxPublishMulticast.PublishMulticasterParent {
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<MonoPublishMulticast.MonoPublishMulticaster, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         MonoPublishMulticast.MonoPublishMulticaster.class, Subscription.class, "s"
      );
      volatile int wip;
      static final AtomicIntegerFieldUpdater<MonoPublishMulticast.MonoPublishMulticaster> WIP = AtomicIntegerFieldUpdater.newUpdater(
         MonoPublishMulticast.MonoPublishMulticaster.class, "wip"
      );
      volatile MonoPublishMulticast.PublishMulticastInner<T>[] subscribers;
      static final AtomicReferenceFieldUpdater<MonoPublishMulticast.MonoPublishMulticaster, MonoPublishMulticast.PublishMulticastInner[]> SUBSCRIBERS = AtomicReferenceFieldUpdater.newUpdater(
         MonoPublishMulticast.MonoPublishMulticaster.class, MonoPublishMulticast.PublishMulticastInner[].class, "subscribers"
      );
      static final MonoPublishMulticast.PublishMulticastInner[] EMPTY = new MonoPublishMulticast.PublishMulticastInner[0];
      static final MonoPublishMulticast.PublishMulticastInner[] TERMINATED = new MonoPublishMulticast.PublishMulticastInner[0];
      volatile boolean done;
      @Nullable
      T value;
      Throwable error;
      volatile boolean connected;
      final Context context;

      MonoPublishMulticaster(Context ctx) {
         SUBSCRIBERS.lazySet(this, EMPTY);
         this.context = ctx;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.s == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.PREFETCH) {
            return 1;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.value != null ? 1 : 0;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.subscribers);
      }

      @Override
      public Context currentContext() {
         return this.context;
      }

      @Override
      public void subscribe(CoreSubscriber<? super T> actual) {
         MonoPublishMulticast.PublishMulticastInner<T> pcs = new MonoPublishMulticast.PublishMulticastInner<>(this, actual);
         actual.onSubscribe(pcs);
         if (this.add(pcs)) {
            if (pcs.cancelled == 1) {
               this.remove(pcs);
               return;
            }

            this.drain();
         } else {
            Throwable ex = this.error;
            if (ex != null) {
               actual.onError(ex);
            } else {
               actual.onComplete();
            }
         }

      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            this.connected = true;
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.context);
         } else {
            this.value = t;
            this.done = true;
            this.drain();
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.context);
         } else {
            this.error = t;
            this.done = true;
            this.drain();
         }
      }

      @Override
      public void onComplete() {
         this.done = true;
         this.drain();
      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            int missed = 1;

            do {
               if (this.connected) {
                  if (this.s == Operators.cancelledSubscription()) {
                     this.value = null;
                     return;
                  }

                  T v = this.value;
                  MonoPublishMulticast.PublishMulticastInner<T>[] a = this.subscribers;
                  int n = a.length;
                  if (n != 0) {
                     if (this.s == Operators.cancelledSubscription()) {
                        this.value = null;
                        return;
                     }

                     MonoPublishMulticast.PublishMulticastInner<T>[] castedArray = (MonoPublishMulticast.PublishMulticastInner[])SUBSCRIBERS.getAndSet(
                        this, TERMINATED
                     );
                     a = castedArray;
                     n = castedArray.length;
                     Throwable ex = this.error;
                     if (ex != null) {
                        for(int i = 0; i < n; ++i) {
                           a[i].actual.onError(ex);
                        }
                     } else if (v == null) {
                        for(int i = 0; i < n; ++i) {
                           a[i].actual.onComplete();
                        }
                     } else {
                        for(int i = 0; i < n; ++i) {
                           a[i].actual.onNext(v);
                           a[i].actual.onComplete();
                        }

                        this.value = null;
                     }

                     return;
                  }
               }

               missed = WIP.addAndGet(this, -missed);
            } while(missed != 0);

         }
      }

      boolean add(MonoPublishMulticast.PublishMulticastInner<T> s) {
         MonoPublishMulticast.PublishMulticastInner<T>[] a;
         MonoPublishMulticast.PublishMulticastInner<T>[] b;
         do {
            a = this.subscribers;
            if (a == TERMINATED) {
               return false;
            }

            int n = a.length;
            b = new MonoPublishMulticast.PublishMulticastInner[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = s;
         } while(!SUBSCRIBERS.compareAndSet(this, a, b));

         return true;
      }

      void remove(MonoPublishMulticast.PublishMulticastInner<T> s) {
         while(true) {
            MonoPublishMulticast.PublishMulticastInner<T>[] a = this.subscribers;
            if (a != TERMINATED && a != EMPTY) {
               int n = a.length;
               int j = -1;

               for(int i = 0; i < n; ++i) {
                  if (a[i] == s) {
                     j = i;
                     break;
                  }
               }

               if (j < 0) {
                  return;
               }

               MonoPublishMulticast.PublishMulticastInner<T>[] b;
               if (n == 1) {
                  b = EMPTY;
               } else {
                  b = new MonoPublishMulticast.PublishMulticastInner[n - 1];
                  System.arraycopy(a, 0, b, 0, j);
                  System.arraycopy(a, j + 1, b, j, n - j - 1);
               }

               if (!SUBSCRIBERS.compareAndSet(this, a, b)) {
                  continue;
               }

               return;
            }

            return;
         }
      }

      @Override
      public void terminate() {
         Operators.terminate(S, this);
         if (WIP.getAndIncrement(this) == 0 && this.connected) {
            this.value = null;
         }

      }
   }

   static final class PublishMulticastInner<T> implements InnerProducer<T> {
      final MonoPublishMulticast.MonoPublishMulticaster<T> parent;
      final CoreSubscriber<? super T> actual;
      volatile int cancelled;
      static final AtomicIntegerFieldUpdater<MonoPublishMulticast.PublishMulticastInner> CANCELLED = AtomicIntegerFieldUpdater.newUpdater(
         MonoPublishMulticast.PublishMulticastInner.class, "cancelled"
      );

      PublishMulticastInner(MonoPublishMulticast.MonoPublishMulticaster<T> parent, CoreSubscriber<? super T> actual) {
         this.parent = parent;
         this.actual = actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.parent;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled == 1;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerProducer.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            this.parent.drain();
         }

      }

      @Override
      public void cancel() {
         if (CANCELLED.compareAndSet(this, 0, 1)) {
            this.parent.remove(this);
            this.parent.drain();
         }

      }
   }
}
