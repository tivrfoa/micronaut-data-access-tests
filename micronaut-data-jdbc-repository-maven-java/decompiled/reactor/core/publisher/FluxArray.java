package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import org.reactivestreams.Subscriber;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxArray<T> extends Flux<T> implements Fuseable, SourceProducer<T> {
   final T[] array;

   @SafeVarargs
   public FluxArray(T... array) {
      this.array = (T[])((Object[])Objects.requireNonNull(array, "array"));
   }

   public static <T> void subscribe(CoreSubscriber<? super T> s, T[] array) {
      if (array.length == 0) {
         Operators.complete(s);
      } else {
         if (s instanceof Fuseable.ConditionalSubscriber) {
            s.onSubscribe(new FluxArray.ArrayConditionalSubscription<>((Fuseable.ConditionalSubscriber<? super T>)s, array));
         } else {
            s.onSubscribe(new FluxArray.ArraySubscription<>(s, array));
         }

      }
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      subscribe(actual, this.array);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.BUFFERED) {
         return this.array.length;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   static final class ArrayConditionalSubscription<T> implements InnerProducer<T>, Fuseable.SynchronousSubscription<T> {
      final Fuseable.ConditionalSubscriber<? super T> actual;
      final T[] array;
      int index;
      volatile boolean cancelled;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxArray.ArrayConditionalSubscription> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxArray.ArrayConditionalSubscription.class, "requested"
      );

      ArrayConditionalSubscription(Fuseable.ConditionalSubscriber<? super T> actual, T[] array) {
         this.actual = actual;
         this.array = array;
      }

      @Override
      public CoreSubscriber<? super T> actual() {
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

      void slowPath(long n) {
         T[] a = this.array;
         int len = a.length;
         Fuseable.ConditionalSubscriber<? super T> s = this.actual;
         int i = this.index;
         int e = 0;

         while(!this.cancelled) {
            while(i != len && (long)e != n) {
               T t = a[i];
               if (t == null) {
                  s.onError(new NullPointerException("The " + i + "th array element was null"));
                  return;
               }

               boolean b = s.tryOnNext(t);
               if (this.cancelled) {
                  return;
               }

               ++i;
               if (b) {
                  ++e;
               }
            }

            if (i == len) {
               s.onComplete();
               return;
            }

            n = this.requested;
            if (n == (long)e) {
               this.index = i;
               n = REQUESTED.addAndGet(this, (long)(-e));
               if (n == 0L) {
                  return;
               }

               e = 0;
            }
         }

      }

      void fastPath() {
         T[] a = this.array;
         int len = a.length;
         Subscriber<? super T> s = this.actual;

         for(int i = this.index; i != len; ++i) {
            if (this.cancelled) {
               return;
            }

            T t = a[i];
            if (t == null) {
               s.onError(new NullPointerException("The " + i + "th array element was null"));
               return;
            }

            s.onNext(t);
         }

         if (!this.cancelled) {
            s.onComplete();
         }
      }

      @Override
      public void cancel() {
         this.cancelled = true;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.isEmpty();
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.size();
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else {
            return key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM ? this.requested : InnerProducer.super.scanUnsafe(key);
         }
      }

      @Nullable
      public T poll() {
         int i = this.index;
         T[] a = this.array;
         if (i != a.length) {
            T t = (T)Objects.requireNonNull(a[i], "Array returned null value");
            this.index = i + 1;
            return t;
         } else {
            return null;
         }
      }

      public boolean isEmpty() {
         return this.index == this.array.length;
      }

      public void clear() {
         this.index = this.array.length;
      }

      public int size() {
         return this.array.length - this.index;
      }
   }

   static final class ArraySubscription<T> implements InnerProducer<T>, Fuseable.SynchronousSubscription<T> {
      final CoreSubscriber<? super T> actual;
      final T[] array;
      int index;
      volatile boolean cancelled;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxArray.ArraySubscription> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxArray.ArraySubscription.class, "requested"
      );

      ArraySubscription(CoreSubscriber<? super T> actual, T[] array) {
         this.actual = actual;
         this.array = array;
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

      void slowPath(long n) {
         T[] a = this.array;
         int len = a.length;
         Subscriber<? super T> s = this.actual;
         int i = this.index;
         int e = 0;

         while(!this.cancelled) {
            while(i != len && (long)e != n) {
               T t = a[i];
               if (t == null) {
                  s.onError(new NullPointerException("The " + i + "th array element was null"));
                  return;
               }

               s.onNext(t);
               if (this.cancelled) {
                  return;
               }

               ++i;
               ++e;
            }

            if (i == len) {
               s.onComplete();
               return;
            }

            n = this.requested;
            if (n == (long)e) {
               this.index = i;
               n = REQUESTED.addAndGet(this, (long)(-e));
               if (n == 0L) {
                  return;
               }

               e = 0;
            }
         }

      }

      void fastPath() {
         T[] a = this.array;
         int len = a.length;
         Subscriber<? super T> s = this.actual;

         for(int i = this.index; i != len; ++i) {
            if (this.cancelled) {
               return;
            }

            T t = a[i];
            if (t == null) {
               s.onError(new NullPointerException("The " + i + "th array element was null"));
               return;
            }

            s.onNext(t);
         }

         if (!this.cancelled) {
            s.onComplete();
         }
      }

      @Override
      public void cancel() {
         this.cancelled = true;
      }

      @Nullable
      public T poll() {
         int i = this.index;
         T[] a = this.array;
         if (i != a.length) {
            T t = a[i];
            Objects.requireNonNull(t);
            this.index = i + 1;
            return t;
         } else {
            return null;
         }
      }

      public boolean isEmpty() {
         return this.index == this.array.length;
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      public void clear() {
         this.index = this.array.length;
      }

      public int size() {
         return this.array.length - this.index;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.isEmpty();
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.size();
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else {
            return key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM ? this.requested : InnerProducer.super.scanUnsafe(key);
         }
      }
   }
}
