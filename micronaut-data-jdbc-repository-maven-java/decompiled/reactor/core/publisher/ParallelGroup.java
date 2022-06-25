package reactor.core.publisher;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class ParallelGroup<T> extends Flux<GroupedFlux<Integer, T>> implements Scannable, Fuseable {
   final ParallelFlux<? extends T> source;

   ParallelGroup(ParallelFlux<? extends T> source) {
      this.source = source;
   }

   @Override
   public void subscribe(CoreSubscriber<? super GroupedFlux<Integer, T>> actual) {
      int n = this.source.parallelism();
      ParallelGroup.ParallelInnerGroup<T>[] groups = new ParallelGroup.ParallelInnerGroup[n];

      for(int i = 0; i < n; ++i) {
         groups[i] = new ParallelGroup.ParallelInnerGroup<>(i);
      }

      FluxArray.subscribe(actual, groups);
      this.source.subscribe(groups);
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else if (key == Scannable.Attr.PREFETCH) {
         return this.getPrefetch();
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   static final class ParallelInnerGroup<T> extends GroupedFlux<Integer, T> implements InnerOperator<T, T> {
      final int key;
      volatile int once;
      static final AtomicIntegerFieldUpdater<ParallelGroup.ParallelInnerGroup> ONCE = AtomicIntegerFieldUpdater.newUpdater(
         ParallelGroup.ParallelInnerGroup.class, "once"
      );
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<ParallelGroup.ParallelInnerGroup, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         ParallelGroup.ParallelInnerGroup.class, Subscription.class, "s"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<ParallelGroup.ParallelInnerGroup> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         ParallelGroup.ParallelInnerGroup.class, "requested"
      );
      CoreSubscriber<? super T> actual;

      ParallelInnerGroup(int key) {
         this.key = key;
      }

      public Integer key() {
         return this.key;
      }

      @Override
      public void subscribe(CoreSubscriber<? super T> actual) {
         if (ONCE.compareAndSet(this, 0, 1)) {
            this.actual = actual;
            actual.onSubscribe(this);
         } else {
            Operators.error(actual, new IllegalStateException("This ParallelGroup can be subscribed to at most once."));
         }

      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.s == Operators.cancelledSubscription();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            long r = REQUESTED.getAndSet(this, 0L);
            if (r != 0L) {
               s.request(r);
            }
         }

      }

      @Override
      public void onNext(T t) {
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         this.actual.onError(t);
      }

      @Override
      public void onComplete() {
         this.actual.onComplete();
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Subscription a = this.s;
            if (a == null) {
               Operators.addCap(REQUESTED, this, n);
               a = this.s;
               if (a != null) {
                  long r = REQUESTED.getAndSet(this, 0L);
                  if (r != 0L) {
                     a.request(n);
                  }
               }
            } else {
               a.request(n);
            }
         }

      }

      @Override
      public void cancel() {
         Operators.terminate(S, this);
      }
   }
}
