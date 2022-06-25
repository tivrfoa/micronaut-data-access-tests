package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiFunction;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class ParallelMergeReduce<T> extends Mono<T> implements Scannable, Fuseable {
   final ParallelFlux<? extends T> source;
   final BiFunction<T, T, T> reducer;

   ParallelMergeReduce(ParallelFlux<? extends T> source, BiFunction<T, T, T> reducer) {
      this.source = source;
      this.reducer = reducer;
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      ParallelMergeReduce.MergeReduceMain<T> parent = new ParallelMergeReduce.MergeReduceMain<>(actual, this.source.parallelism(), this.reducer);
      actual.onSubscribe(parent);
      this.source.subscribe(parent.subscribers);
   }

   static final class MergeReduceInner<T> implements InnerConsumer<T> {
      final ParallelMergeReduce.MergeReduceMain<T> parent;
      final BiFunction<T, T, T> reducer;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<ParallelMergeReduce.MergeReduceInner, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         ParallelMergeReduce.MergeReduceInner.class, Subscription.class, "s"
      );
      T value;
      boolean done;

      MergeReduceInner(ParallelMergeReduce.MergeReduceMain<T> parent, BiFunction<T, T, T> reducer) {
         this.parent = parent;
         this.reducer = reducer;
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
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.parent;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.value != null ? 1 : 0;
         } else if (key == Scannable.Attr.PREFETCH) {
            return Integer.MAX_VALUE;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
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
         if (this.done) {
            Operators.onNextDropped(t, this.currentContext());
         } else {
            T v = this.value;
            if (v == null) {
               this.value = t;
            } else {
               try {
                  v = (T)Objects.requireNonNull(this.reducer.apply(v, t), "The reducer returned a null value");
               } catch (Throwable var4) {
                  this.onError(Operators.onOperatorError(this.s, var4, t, this.currentContext()));
                  return;
               }

               this.value = v;
            }

         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.parent.currentContext());
         } else {
            this.done = true;
            this.parent.innerError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.parent.innerComplete(this.value);
         }
      }

      void cancel() {
         Operators.terminate(S, this);
      }
   }

   static final class MergeReduceMain<T> extends Operators.MonoSubscriber<T, T> {
      final ParallelMergeReduce.MergeReduceInner<T>[] subscribers;
      final BiFunction<T, T, T> reducer;
      volatile ParallelMergeReduce.SlotPair<T> current;
      static final AtomicReferenceFieldUpdater<ParallelMergeReduce.MergeReduceMain, ParallelMergeReduce.SlotPair> CURRENT = AtomicReferenceFieldUpdater.newUpdater(
         ParallelMergeReduce.MergeReduceMain.class, ParallelMergeReduce.SlotPair.class, "current"
      );
      volatile int remaining;
      static final AtomicIntegerFieldUpdater<ParallelMergeReduce.MergeReduceMain> REMAINING = AtomicIntegerFieldUpdater.newUpdater(
         ParallelMergeReduce.MergeReduceMain.class, "remaining"
      );
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<ParallelMergeReduce.MergeReduceMain, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         ParallelMergeReduce.MergeReduceMain.class, Throwable.class, "error"
      );

      MergeReduceMain(CoreSubscriber<? super T> subscriber, int n, BiFunction<T, T, T> reducer) {
         super(subscriber);
         ParallelMergeReduce.MergeReduceInner<T>[] a = new ParallelMergeReduce.MergeReduceInner[n];

         for(int i = 0; i < n; ++i) {
            a[i] = new ParallelMergeReduce.MergeReduceInner<>(this, reducer);
         }

         this.subscribers = a;
         this.reducer = reducer;
         REMAINING.lazySet(this, n);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.TERMINATED) {
            return REMAINING.get(this) == 0;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         }
      }

      @Nullable
      ParallelMergeReduce.SlotPair<T> addValue(T value) {
         while(true) {
            ParallelMergeReduce.SlotPair<T> curr = this.current;
            if (curr == null) {
               curr = new ParallelMergeReduce.SlotPair<>();
               if (!CURRENT.compareAndSet(this, null, curr)) {
                  continue;
               }
            }

            int c = curr.tryAcquireSlot();
            if (c >= 0) {
               if (c == 0) {
                  curr.first = value;
               } else {
                  curr.second = value;
               }

               if (curr.releaseSlot()) {
                  CURRENT.compareAndSet(this, curr, null);
                  return curr;
               }

               return null;
            }

            CURRENT.compareAndSet(this, curr, null);
         }
      }

      @Override
      public void cancel() {
         for(ParallelMergeReduce.MergeReduceInner<T> inner : this.subscribers) {
            inner.cancel();
         }

         super.cancel();
      }

      void innerError(Throwable ex) {
         if (ERROR.compareAndSet(this, null, ex)) {
            this.cancel();
            this.actual.onError(ex);
         } else if (this.error != ex) {
            Operators.onErrorDropped(ex, this.actual.currentContext());
         }

      }

      void innerComplete(@Nullable T value) {
         if (value != null) {
            while(true) {
               ParallelMergeReduce.SlotPair<T> sp = this.addValue(value);
               if (sp == null) {
                  break;
               }

               try {
                  value = (T)Objects.requireNonNull(this.reducer.apply(sp.first, sp.second), "The reducer returned a null value");
               } catch (Throwable var4) {
                  this.innerError(Operators.onOperatorError(this, var4, this.actual.currentContext()));
                  return;
               }
            }
         }

         if (REMAINING.decrementAndGet(this) == 0) {
            ParallelMergeReduce.SlotPair<T> sp = this.current;
            CURRENT.lazySet(this, null);
            if (sp != null) {
               this.complete(sp.first);
            } else {
               this.actual.onComplete();
            }
         }

      }
   }

   static final class SlotPair<T> {
      T first;
      T second;
      volatile int acquireIndex;
      static final AtomicIntegerFieldUpdater<ParallelMergeReduce.SlotPair> ACQ = AtomicIntegerFieldUpdater.newUpdater(
         ParallelMergeReduce.SlotPair.class, "acquireIndex"
      );
      volatile int releaseIndex;
      static final AtomicIntegerFieldUpdater<ParallelMergeReduce.SlotPair> REL = AtomicIntegerFieldUpdater.newUpdater(
         ParallelMergeReduce.SlotPair.class, "releaseIndex"
      );

      int tryAcquireSlot() {
         int acquired;
         do {
            acquired = this.acquireIndex;
            if (acquired >= 2) {
               return -1;
            }
         } while(!ACQ.compareAndSet(this, acquired, acquired + 1));

         return acquired;
      }

      boolean releaseSlot() {
         return REL.incrementAndGet(this) == 2;
      }
   }
}
