package reactor.core.publisher;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.stream.Stream;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class ParallelMergeSort<T> extends Flux<T> implements Scannable {
   final ParallelFlux<List<T>> source;
   final Comparator<? super T> comparator;

   ParallelMergeSort(ParallelFlux<List<T>> source, Comparator<? super T> comparator) {
      this.source = source;
      this.comparator = comparator;
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      ParallelMergeSort.MergeSortMain<T> parent = new ParallelMergeSort.MergeSortMain<>(actual, this.source.parallelism(), this.comparator);
      actual.onSubscribe(parent);
      this.source.subscribe(parent.subscribers);
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

   static final class MergeSortInner<T> implements InnerConsumer<List<T>> {
      final ParallelMergeSort.MergeSortMain<T> parent;
      final int index;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<ParallelMergeSort.MergeSortInner, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         ParallelMergeSort.MergeSortInner.class, Subscription.class, "s"
      );

      MergeSortInner(ParallelMergeSort.MergeSortMain<T> parent, int index) {
         this.parent = parent;
         this.index = index;
      }

      @Override
      public Context currentContext() {
         return this.parent.actual.currentContext();
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

      public void onNext(List<T> t) {
         this.parent.innerNext(t, this.index);
      }

      @Override
      public void onError(Throwable t) {
         this.parent.innerError(t);
      }

      @Override
      public void onComplete() {
      }

      void cancel() {
         Operators.terminate(S, this);
      }
   }

   static final class MergeSortMain<T> implements InnerProducer<T> {
      final ParallelMergeSort.MergeSortInner<T>[] subscribers;
      final List<T>[] lists;
      final int[] indexes;
      final Comparator<? super T> comparator;
      final CoreSubscriber<? super T> actual;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<ParallelMergeSort.MergeSortMain> WIP = AtomicIntegerFieldUpdater.newUpdater(
         ParallelMergeSort.MergeSortMain.class, "wip"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<ParallelMergeSort.MergeSortMain> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         ParallelMergeSort.MergeSortMain.class, "requested"
      );
      volatile boolean cancelled;
      volatile int remaining;
      static final AtomicIntegerFieldUpdater<ParallelMergeSort.MergeSortMain> REMAINING = AtomicIntegerFieldUpdater.newUpdater(
         ParallelMergeSort.MergeSortMain.class, "remaining"
      );
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<ParallelMergeSort.MergeSortMain, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         ParallelMergeSort.MergeSortMain.class, Throwable.class, "error"
      );

      MergeSortMain(CoreSubscriber<? super T> actual, int n, Comparator<? super T> comparator) {
         this.comparator = comparator;
         this.actual = actual;
         ParallelMergeSort.MergeSortInner<T>[] s = new ParallelMergeSort.MergeSortInner[n];

         for(int i = 0; i < n; ++i) {
            s[i] = new ParallelMergeSort.MergeSortInner<>(this, i);
         }

         this.subscribers = s;
         this.lists = new List[n];
         this.indexes = new int[n];
         REMAINING.lazySet(this, n);
      }

      @Override
      public final CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.subscribers.length - this.remaining;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerProducer.super.scanUnsafe(key);
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.subscribers);
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
            if (this.remaining == 0) {
               this.drain();
            }
         }

      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            this.cancelAll();
            if (WIP.getAndIncrement(this) == 0) {
               Arrays.fill(this.lists, null);
            }
         }

      }

      void cancelAll() {
         for(ParallelMergeSort.MergeSortInner<T> s : this.subscribers) {
            s.cancel();
         }

      }

      void innerNext(List<T> value, int index) {
         this.lists[index] = value;
         if (REMAINING.decrementAndGet(this) == 0) {
            this.drain();
         }

      }

      void innerError(Throwable ex) {
         if (ERROR.compareAndSet(this, null, ex)) {
            this.cancelAll();
            this.drain();
         } else if (this.error != ex) {
            Operators.onErrorDropped(ex, this.actual.currentContext());
         }

      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            int missed = 1;
            Subscriber<? super T> a = this.actual;
            List<T>[] lists = this.lists;
            int[] indexes = this.indexes;
            int n = indexes.length;

            while(true) {
               long r = this.requested;

               long e;
               for(e = 0L; e != r; ++e) {
                  if (this.cancelled) {
                     Arrays.fill(lists, null);
                     return;
                  }

                  Throwable ex = this.error;
                  if (ex != null) {
                     this.cancelAll();
                     Arrays.fill(lists, null);
                     a.onError(ex);
                     return;
                  }

                  T min = null;
                  int minIndex = -1;

                  for(int i = 0; i < n; ++i) {
                     List<T> list = lists[i];
                     int index = indexes[i];
                     if (list.size() != index) {
                        if (min == null) {
                           min = (T)list.get(index);
                           minIndex = i;
                        } else {
                           T b = (T)list.get(index);
                           if (this.comparator.compare(min, b) > 0) {
                              min = b;
                              minIndex = i;
                           }
                        }
                     }
                  }

                  if (min == null) {
                     Arrays.fill(lists, null);
                     a.onComplete();
                     return;
                  }

                  a.onNext(min);
                  int var10002 = indexes[minIndex]++;
               }

               if (e == r) {
                  if (this.cancelled) {
                     Arrays.fill(lists, null);
                     return;
                  }

                  Throwable ex = this.error;
                  if (ex != null) {
                     this.cancelAll();
                     Arrays.fill(lists, null);
                     a.onError(ex);
                     return;
                  }

                  boolean empty = true;

                  for(int i = 0; i < n; ++i) {
                     if (indexes[i] != lists[i].size()) {
                        empty = false;
                        break;
                     }
                  }

                  if (empty) {
                     Arrays.fill(lists, null);
                     a.onComplete();
                     return;
                  }
               }

               if (e != 0L && r != Long.MAX_VALUE) {
                  REQUESTED.addAndGet(this, -e);
               }

               int w = this.wip;
               if (w == missed) {
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
   }
}
