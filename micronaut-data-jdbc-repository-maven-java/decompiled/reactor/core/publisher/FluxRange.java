package reactor.core.publisher;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import org.reactivestreams.Subscriber;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxRange extends Flux<Integer> implements Fuseable, SourceProducer<Integer> {
   final long start;
   final long end;

   FluxRange(int start, int count) {
      if (count < 0) {
         throw new IllegalArgumentException("count >= required but it was " + count);
      } else {
         long e = (long)start + (long)count;
         if (e - 1L > 2147483647L) {
            throw new IllegalArgumentException("start + count must be less than Integer.MAX_VALUE + 1");
         } else {
            this.start = (long)start;
            this.end = e;
         }
      }
   }

   @Override
   public void subscribe(CoreSubscriber<? super Integer> actual) {
      long st = this.start;
      long en = this.end;
      if (st == en) {
         Operators.complete(actual);
      } else if (st + 1L == en) {
         actual.onSubscribe(Operators.scalarSubscription(actual, (int)st));
      } else if (actual instanceof Fuseable.ConditionalSubscriber) {
         actual.onSubscribe(new FluxRange.RangeSubscriptionConditional((Fuseable.ConditionalSubscriber<? super Integer>)actual, st, en));
      } else {
         actual.onSubscribe(new FluxRange.RangeSubscription(actual, st, en));
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }

   static final class RangeSubscription implements InnerProducer<Integer>, Fuseable.SynchronousSubscription<Integer> {
      final CoreSubscriber<? super Integer> actual;
      final long end;
      volatile boolean cancelled;
      long index;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxRange.RangeSubscription> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxRange.RangeSubscription.class, "requested"
      );

      RangeSubscription(CoreSubscriber<? super Integer> actual, long start, long end) {
         this.actual = actual;
         this.index = start;
         this.end = end;
      }

      @Override
      public CoreSubscriber<? super Integer> actual() {
         return this.actual;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n) && Operators.addCap(REQUESTED, this, n) == 0L) {
            if (n == Long.MAX_VALUE) {
               this.fastPath();
            } else {
               this.slowPath(n);
            }
         }

      }

      @Override
      public void cancel() {
         this.cancelled = true;
      }

      void fastPath() {
         long e = this.end;
         Subscriber<? super Integer> a = this.actual;

         for(long i = this.index; i != e; ++i) {
            if (this.cancelled) {
               return;
            }

            a.onNext((int)i);
         }

         if (!this.cancelled) {
            a.onComplete();
         }
      }

      void slowPath(long n) {
         Subscriber<? super Integer> a = this.actual;
         long f = this.end;
         long e = 0L;
         long i = this.index;

         while(!this.cancelled) {
            while(e != n && i != f) {
               a.onNext((int)i);
               if (this.cancelled) {
                  return;
               }

               ++e;
               ++i;
            }

            if (this.cancelled) {
               return;
            }

            if (i == f) {
               a.onComplete();
               return;
            }

            n = this.requested;
            if (n == e) {
               this.index = i;
               n = REQUESTED.addAndGet(this, -e);
               if (n == 0L) {
                  return;
               }

               e = 0L;
            }
         }

      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.isEmpty();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerProducer.super.scanUnsafe(key);
         }
      }

      @Nullable
      public Integer poll() {
         long i = this.index;
         if (i == this.end) {
            return null;
         } else {
            this.index = i + 1L;
            return (int)i;
         }
      }

      public boolean isEmpty() {
         return this.index == this.end;
      }

      public void clear() {
         this.index = this.end;
      }

      public int size() {
         return (int)(this.end - this.index);
      }
   }

   static final class RangeSubscriptionConditional implements InnerProducer<Integer>, Fuseable.SynchronousSubscription<Integer> {
      final Fuseable.ConditionalSubscriber<? super Integer> actual;
      final long end;
      volatile boolean cancelled;
      long index;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxRange.RangeSubscriptionConditional> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxRange.RangeSubscriptionConditional.class, "requested"
      );

      RangeSubscriptionConditional(Fuseable.ConditionalSubscriber<? super Integer> actual, long start, long end) {
         this.actual = actual;
         this.index = start;
         this.end = end;
      }

      @Override
      public CoreSubscriber<? super Integer> actual() {
         return this.actual;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n) && Operators.addCap(REQUESTED, this, n) == 0L) {
            if (n == Long.MAX_VALUE) {
               this.fastPath();
            } else {
               this.slowPath(n);
            }
         }

      }

      @Override
      public void cancel() {
         this.cancelled = true;
      }

      void fastPath() {
         long e = this.end;
         Fuseable.ConditionalSubscriber<? super Integer> a = this.actual;

         for(long i = this.index; i != e; ++i) {
            if (this.cancelled) {
               return;
            }

            a.tryOnNext((int)i);
         }

         if (!this.cancelled) {
            a.onComplete();
         }
      }

      void slowPath(long n) {
         Fuseable.ConditionalSubscriber<? super Integer> a = this.actual;
         long f = this.end;
         long e = 0L;
         long i = this.index;

         while(!this.cancelled) {
            for(; e != n && i != f; ++i) {
               boolean b = a.tryOnNext((int)i);
               if (this.cancelled) {
                  return;
               }

               if (b) {
                  ++e;
               }
            }

            if (this.cancelled) {
               return;
            }

            if (i == f) {
               a.onComplete();
               return;
            }

            n = this.requested;
            if (n == e) {
               this.index = i;
               n = REQUESTED.addAndGet(this, -e);
               if (n == 0L) {
                  return;
               }

               e = 0L;
            }
         }

      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.isEmpty();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerProducer.super.scanUnsafe(key);
         }
      }

      @Nullable
      public Integer poll() {
         long i = this.index;
         if (i == this.end) {
            return null;
         } else {
            this.index = i + 1L;
            return (int)i;
         }
      }

      public boolean isEmpty() {
         return this.index == this.end;
      }

      public void clear() {
         this.index = this.end;
      }

      public int size() {
         return (int)(this.end - this.index);
      }
   }
}
