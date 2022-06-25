package reactor.core.publisher;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;

final class FluxMergeComparing<T> extends Flux<T> implements SourceProducer<T> {
   final int prefetch;
   final Comparator<? super T> valueComparator;
   final Publisher<? extends T>[] sources;
   final boolean delayError;

   @SafeVarargs
   FluxMergeComparing(int prefetch, Comparator<? super T> valueComparator, boolean delayError, Publisher<? extends T>... sources) {
      if (prefetch <= 0) {
         throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
      } else {
         this.sources = (Publisher[])Objects.requireNonNull(sources, "sources must be non-null");

         for(int i = 0; i < sources.length; ++i) {
            Publisher<? extends T> source = sources[i];
            if (source == null) {
               throw new NullPointerException("sources[" + i + "] is null");
            }
         }

         this.prefetch = prefetch;
         this.valueComparator = valueComparator;
         this.delayError = delayError;
      }
   }

   FluxMergeComparing<T> mergeAdditionalSource(Publisher<? extends T> source, Comparator<? super T> otherComparator) {
      int n = this.sources.length;
      Publisher<? extends T>[] newArray = new Publisher[n + 1];
      System.arraycopy(this.sources, 0, newArray, 0, n);
      newArray[n] = source;
      if (!this.valueComparator.equals(otherComparator)) {
         Comparator<T> currentComparator = (Comparator<T>)this.valueComparator;
         Comparator<T> newComparator = currentComparator.thenComparing(otherComparator);
         return new FluxMergeComparing<>(this.prefetch, newComparator, this.delayError, newArray);
      } else {
         return new FluxMergeComparing<>(this.prefetch, this.valueComparator, this.delayError, newArray);
      }
   }

   @Override
   public int getPrefetch() {
      return this.prefetch;
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return this.sources.length > 0 ? this.sources[0] : null;
      } else if (key == Scannable.Attr.PREFETCH) {
         return this.prefetch;
      } else if (key == Scannable.Attr.DELAY_ERROR) {
         return this.delayError;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      FluxMergeComparing.MergeOrderedMainProducer<T> main = new FluxMergeComparing.MergeOrderedMainProducer<>(
         actual, this.valueComparator, this.prefetch, this.sources.length, this.delayError
      );
      actual.onSubscribe(main);
      main.subscribe(this.sources);
   }

   static final class MergeOrderedInnerSubscriber<T> implements InnerOperator<T, T> {
      final FluxMergeComparing.MergeOrderedMainProducer<T> parent;
      final int prefetch;
      final int limit;
      final Queue<T> queue;
      int consumed;
      volatile boolean done;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<FluxMergeComparing.MergeOrderedInnerSubscriber, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         FluxMergeComparing.MergeOrderedInnerSubscriber.class, Subscription.class, "s"
      );

      MergeOrderedInnerSubscriber(FluxMergeComparing.MergeOrderedMainProducer<T> parent, int prefetch) {
         this.parent = parent;
         this.prefetch = prefetch;
         this.limit = prefetch - (prefetch >> 2);
         this.queue = (Queue)Queues.get(prefetch).get();
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            s.request((long)this.prefetch);
         }

      }

      @Override
      public void onNext(T item) {
         if (!this.parent.done && !this.done) {
            this.queue.offer(item);
            this.parent.drain();
         } else {
            Operators.onNextDropped(item, this.actual().currentContext());
         }
      }

      @Override
      public void onError(Throwable throwable) {
         this.parent.onInnerError(this, throwable);
      }

      @Override
      public void onComplete() {
         this.done = true;
         this.parent.drain();
      }

      @Override
      public void request(long n) {
         int c = this.consumed + 1;
         if (c == this.limit) {
            this.consumed = 0;
            Subscription sub = this.s;
            if (sub != this) {
               sub.request((long)c);
            }
         } else {
            this.consumed = c;
         }

      }

      @Override
      public void cancel() {
         Subscription sub = (Subscription)S.getAndSet(this, this);
         if (sub != null && sub != this) {
            sub.cancel();
         }

      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.parent.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.ACTUAL) {
            return this.parent;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.PREFETCH) {
            return this.prefetch;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue.size();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }
   }

   static final class MergeOrderedMainProducer<T> implements InnerProducer<T> {
      static final Object DONE = new Object();
      final CoreSubscriber<? super T> actual;
      final FluxMergeComparing.MergeOrderedInnerSubscriber<T>[] subscribers;
      final Comparator<? super T> comparator;
      final Object[] values;
      final boolean delayError;
      boolean done;
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxMergeComparing.MergeOrderedMainProducer, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxMergeComparing.MergeOrderedMainProducer.class, Throwable.class, "error"
      );
      volatile int cancelled;
      static final AtomicIntegerFieldUpdater<FluxMergeComparing.MergeOrderedMainProducer> CANCELLED = AtomicIntegerFieldUpdater.newUpdater(
         FluxMergeComparing.MergeOrderedMainProducer.class, "cancelled"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxMergeComparing.MergeOrderedMainProducer> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxMergeComparing.MergeOrderedMainProducer.class, "requested"
      );
      volatile long emitted;
      static final AtomicLongFieldUpdater<FluxMergeComparing.MergeOrderedMainProducer> EMITTED = AtomicLongFieldUpdater.newUpdater(
         FluxMergeComparing.MergeOrderedMainProducer.class, "emitted"
      );
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxMergeComparing.MergeOrderedMainProducer> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxMergeComparing.MergeOrderedMainProducer.class, "wip"
      );

      MergeOrderedMainProducer(CoreSubscriber<? super T> actual, Comparator<? super T> comparator, int prefetch, int n, boolean delayError) {
         this.actual = actual;
         this.comparator = comparator;
         this.delayError = delayError;
         FluxMergeComparing.MergeOrderedInnerSubscriber<T>[] mergeOrderedInnerSub = new FluxMergeComparing.MergeOrderedInnerSubscriber[n];
         this.subscribers = mergeOrderedInnerSub;

         for(int i = 0; i < n; ++i) {
            this.subscribers[i] = new FluxMergeComparing.MergeOrderedInnerSubscriber<>(this, prefetch);
         }

         this.values = new Object[n];
      }

      void subscribe(Publisher<? extends T>[] sources) {
         if (sources.length != this.subscribers.length) {
            throw new IllegalArgumentException("must subscribe with " + this.subscribers.length + " sources");
         } else {
            for(int i = 0; i < sources.length; ++i) {
               Objects.requireNonNull(sources[i], "subscribed with a null source: sources[" + i + "]");
               sources[i].subscribe(this.subscribers[i]);
            }

         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public void request(long n) {
         Operators.addCap(REQUESTED, this, n);
         this.drain();
      }

      @Override
      public void cancel() {
         if (CANCELLED.compareAndSet(this, 0, 1)) {
            for(FluxMergeComparing.MergeOrderedInnerSubscriber<T> subscriber : this.subscribers) {
               subscriber.cancel();
            }

            if (WIP.getAndIncrement(this) == 0) {
               this.discardData();
            }
         }

      }

      void onInnerError(FluxMergeComparing.MergeOrderedInnerSubscriber<T> inner, Throwable ex) {
         Throwable e = Operators.onNextInnerError(ex, this.actual().currentContext(), this);
         if (e != null) {
            if (Exceptions.addThrowable(ERROR, this, e)) {
               if (!this.delayError) {
                  this.done = true;
               }

               inner.done = true;
               this.drain();
            } else {
               inner.done = true;
               Operators.onErrorDropped(e, this.actual.currentContext());
            }
         } else {
            inner.done = true;
            this.drain();
         }

      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            int missed = 1;
            CoreSubscriber<? super T> actual = this.actual;
            Comparator<? super T> comparator = this.comparator;
            FluxMergeComparing.MergeOrderedInnerSubscriber<T>[] subscribers = this.subscribers;
            int n = subscribers.length;
            Object[] values = this.values;
            long e = this.emitted;

            label109:
            while(true) {
               long r = this.requested;

               while(true) {
                  boolean d = this.done;
                  if (this.cancelled != 0) {
                     Arrays.fill(values, null);

                     for(FluxMergeComparing.MergeOrderedInnerSubscriber<T> inner : subscribers) {
                        inner.queue.clear();
                     }

                     return;
                  }

                  int innerDoneCount = 0;
                  int nonEmpty = 0;

                  for(int i = 0; i < n; ++i) {
                     Object o = values[i];
                     if (o == DONE) {
                        ++innerDoneCount;
                        ++nonEmpty;
                     } else if (o == null) {
                        boolean innerDone = subscribers[i].done;
                        o = subscribers[i].queue.poll();
                        if (o != null) {
                           values[i] = o;
                           ++nonEmpty;
                        } else if (innerDone) {
                           values[i] = DONE;
                           ++innerDoneCount;
                           ++nonEmpty;
                        }
                     } else {
                        ++nonEmpty;
                     }
                  }

                  if (this.checkTerminated(d || innerDoneCount == n, actual)) {
                     return;
                  }

                  if (nonEmpty != n || e >= r) {
                     this.emitted = e;
                     missed = WIP.addAndGet(this, -missed);
                     if (missed == 0) {
                        break label109;
                     }
                     break;
                  }

                  T min = null;
                  int minIndex = -1;
                  int i = 0;

                  for(Object o : values) {
                     if (o != DONE) {
                        boolean smaller;
                        try {
                           smaller = min == null || comparator.compare(min, o) > 0;
                        } catch (Throwable var23) {
                           Exceptions.addThrowable(ERROR, this, var23);
                           this.cancel();
                           actual.onError(Exceptions.terminate(ERROR, this));
                           return;
                        }

                        if (smaller) {
                           min = (T)o;
                           minIndex = i;
                        }
                     }

                     ++i;
                  }

                  values[minIndex] = null;
                  actual.onNext(min);
                  ++e;
                  subscribers[minIndex].request(1L);
               }
            }

         }
      }

      boolean checkTerminated(boolean d, Subscriber<?> a) {
         if (this.cancelled != 0) {
            this.discardData();
            return true;
         } else if (!d) {
            return false;
         } else {
            if (this.delayError) {
               Throwable e = this.error;
               if (e != null && e != Exceptions.TERMINATED) {
                  e = Exceptions.terminate(ERROR, this);
                  a.onError(e);
               } else {
                  a.onComplete();
               }
            } else {
               Throwable e = this.error;
               if (e != null && e != Exceptions.TERMINATED) {
                  e = Exceptions.terminate(ERROR, this);
                  this.cancel();
                  this.discardData();
                  a.onError(e);
               } else {
                  a.onComplete();
               }
            }

            return true;
         }
      }

      private void discardData() {
         Context ctx = this.actual().currentContext();

         for(Object v : this.values) {
            if (v != DONE) {
               Operators.onDiscard(v, ctx);
            }
         }

         Arrays.fill(this.values, null);

         for(FluxMergeComparing.MergeOrderedInnerSubscriber<T> subscriber : this.subscribers) {
            Operators.onDiscardQueueWithClear(subscriber.queue, ctx, null);
         }

      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.ACTUAL) {
            return this.actual;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled > 0;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.DELAY_ERROR) {
            return this.delayError;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested - this.emitted;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }
   }
}
