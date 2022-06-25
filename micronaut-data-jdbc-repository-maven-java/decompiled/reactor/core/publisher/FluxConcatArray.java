package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxConcatArray<T> extends Flux<T> implements SourceProducer<T> {
   static final Object WORKING = new Object();
   static final Object DONE = new Object();
   final Publisher<? extends T>[] array;
   final boolean delayError;

   @SafeVarargs
   FluxConcatArray(boolean delayError, Publisher<? extends T>... array) {
      this.array = (Publisher[])Objects.requireNonNull(array, "array");
      this.delayError = delayError;
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      Publisher<? extends T>[] a = this.array;
      if (a.length == 0) {
         Operators.complete(actual);
      } else if (a.length == 1) {
         Publisher<? extends T> p = a[0];
         if (p == null) {
            Operators.error(actual, new NullPointerException("The single source Publisher is null"));
         } else {
            p.subscribe(actual);
         }

      } else if (this.delayError) {
         FluxConcatArray.ConcatArrayDelayErrorSubscriber<T> parent = new FluxConcatArray.ConcatArrayDelayErrorSubscriber<>(actual, a);
         parent.onComplete();
      } else {
         FluxConcatArray.ConcatArraySubscriber<T> parent = new FluxConcatArray.ConcatArraySubscriber<>(actual, a);
         parent.onComplete();
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.DELAY_ERROR) {
         return this.delayError;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   FluxConcatArray<T> concatAdditionalSourceLast(Publisher<? extends T> source) {
      int n = this.array.length;
      Publisher<? extends T>[] newArray = new Publisher[n + 1];
      System.arraycopy(this.array, 0, newArray, 0, n);
      newArray[n] = source;
      return new FluxConcatArray<>(this.delayError, newArray);
   }

   <V> FluxConcatArray<V> concatAdditionalIgnoredLast(Publisher<? extends V> source) {
      int n = this.array.length;
      Publisher<? extends V>[] newArray = new Publisher[n + 1];
      System.arraycopy(this.array, 0, newArray, 0, n);
      newArray[n - 1] = Mono.ignoreElements(newArray[n - 1]);
      newArray[n] = source;
      return new FluxConcatArray<>(this.delayError, newArray);
   }

   FluxConcatArray<T> concatAdditionalSourceFirst(Publisher<? extends T> source) {
      int n = this.array.length;
      Publisher<? extends T>[] newArray = new Publisher[n + 1];
      System.arraycopy(this.array, 0, newArray, 1, n);
      newArray[0] = source;
      return new FluxConcatArray<>(this.delayError, newArray);
   }

   static <T> long activateAndGetRequested(AtomicLongFieldUpdater<T> updater, T instance) {
      long deactivatedRequested;
      long actualRequested;
      do {
         deactivatedRequested = updater.get(instance);
         actualRequested = deactivatedRequested & Long.MAX_VALUE;
      } while(!updater.compareAndSet(instance, deactivatedRequested, actualRequested));

      return actualRequested;
   }

   static <T> void deactivateAndProduce(long produced, AtomicLongFieldUpdater<T> updater, T instance) {
      long actualRequested;
      long deactivatedRequested;
      do {
         actualRequested = updater.get(instance);
         deactivatedRequested = actualRequested == Long.MAX_VALUE ? -1L : actualRequested - produced | Long.MIN_VALUE;
      } while(!updater.compareAndSet(instance, actualRequested, deactivatedRequested));

   }

   @Nullable
   static <T extends FluxConcatArray.SubscriptionAware> Subscription addCapAndGetSubscription(long n, AtomicLongFieldUpdater<T> updater, T instance) {
      long state;
      Subscription s;
      long actualRequested;
      long status;
      do {
         state = updater.get(instance);
         s = instance.upstream();
         actualRequested = state & Long.MAX_VALUE;
         status = state & Long.MIN_VALUE;
         if (actualRequested == Long.MAX_VALUE) {
            return status == Long.MIN_VALUE ? null : s;
         }
      } while(!updater.compareAndSet(instance, state, Operators.addCap(actualRequested, n) | status));

      return status == Long.MIN_VALUE ? null : s;
   }

   static final class ConcatArrayDelayErrorSubscriber<T> extends ThreadLocal<Object> implements InnerOperator<T, T>, FluxConcatArray.SubscriptionAware {
      final CoreSubscriber<? super T> actual;
      final Publisher<? extends T>[] sources;
      int index;
      long produced;
      Subscription s;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxConcatArray.ConcatArrayDelayErrorSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxConcatArray.ConcatArrayDelayErrorSubscriber.class, "requested"
      );
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxConcatArray.ConcatArrayDelayErrorSubscriber, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxConcatArray.ConcatArrayDelayErrorSubscriber.class, Throwable.class, "error"
      );
      volatile boolean cancelled;

      ConcatArrayDelayErrorSubscriber(CoreSubscriber<? super T> actual, Publisher<? extends T>[] sources) {
         this.actual = actual;
         this.sources = sources;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (this.cancelled) {
            this.remove();
            s.cancel();
         } else {
            Subscription previousSubscription = this.s;
            this.s = s;
            if (previousSubscription == null) {
               this.actual.onSubscribe(this);
            } else {
               long actualRequested = FluxConcatArray.activateAndGetRequested(REQUESTED, this);
               if (actualRequested > 0L) {
                  s.request(actualRequested);
               }

            }
         }
      }

      @Override
      public void onNext(T t) {
         ++this.produced;
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         if (!Exceptions.addThrowable(ERROR, this, t)) {
            this.remove();
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.onComplete();
         }
      }

      @Override
      public void onComplete() {
         if (this.get() == FluxConcatArray.WORKING) {
            this.set(FluxConcatArray.DONE);
         } else {
            Publisher<? extends T>[] a = this.sources;

            Object state;
            do {
               this.set(FluxConcatArray.WORKING);
               int i = this.index;
               if (i == a.length) {
                  this.remove();
                  Throwable e = Exceptions.terminate(ERROR, this);
                  if (e == Exceptions.TERMINATED) {
                     return;
                  } else {
                     if (e != null) {
                        this.actual.onError(e);
                     } else {
                        this.actual.onComplete();
                     }

                     return;
                  }
               }

               Publisher<? extends T> p = a[i];
               if (p == null) {
                  this.remove();
                  if (this.cancelled) {
                     return;
                  }

                  NullPointerException npe = new NullPointerException("Source Publisher at index " + i + " is null");
                  if (!Exceptions.addThrowable(ERROR, this, npe)) {
                     Operators.onErrorDropped(npe, this.actual.currentContext());
                     return;
                  }

                  Throwable throwable = Exceptions.terminate(ERROR, this);
                  if (throwable == Exceptions.TERMINATED) {
                     return;
                  }

                  this.actual.onError(throwable);
                  return;
               }

               long c = this.produced;
               if (c != 0L) {
                  this.produced = 0L;
                  FluxConcatArray.deactivateAndProduce(c, REQUESTED, this);
               }

               this.index = ++i;
               if (this.cancelled) {
                  return;
               }

               p.subscribe(this);
               state = this.get();
            } while(state == FluxConcatArray.DONE);

            this.remove();
         }
      }

      @Override
      public void request(long n) {
         Subscription subscription = FluxConcatArray.addCapAndGetSubscription(n, REQUESTED, this);
         if (subscription != null) {
            subscription.request(n);
         }
      }

      @Override
      public void cancel() {
         this.remove();
         this.cancelled = true;
         if ((this.requested & Long.MIN_VALUE) != Long.MIN_VALUE) {
            this.s.cancel();
         }

         Throwable throwable = Exceptions.terminate(ERROR, this);
         if (throwable != null) {
            Operators.onErrorDropped(throwable, this.actual.currentContext());
         }

      }

      @Override
      public Subscription upstream() {
         return this.s;
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.DELAY_ERROR) {
            return true;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.error == Exceptions.TERMINATED;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error != Exceptions.TERMINATED ? this.error : null;
         } else if (key == Scannable.Attr.RUN_STYLE) {
            return Scannable.Attr.RunStyle.SYNC;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else {
            return key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM ? this.requested : InnerOperator.super.scanUnsafe(key);
         }
      }
   }

   static final class ConcatArraySubscriber<T> extends ThreadLocal<Object> implements InnerOperator<T, T>, FluxConcatArray.SubscriptionAware {
      final CoreSubscriber<? super T> actual;
      final Publisher<? extends T>[] sources;
      int index;
      long produced;
      Subscription s;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxConcatArray.ConcatArraySubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxConcatArray.ConcatArraySubscriber.class, "requested"
      );
      volatile boolean cancelled;

      ConcatArraySubscriber(CoreSubscriber<? super T> actual, Publisher<? extends T>[] sources) {
         this.actual = actual;
         this.sources = sources;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (this.cancelled) {
            this.remove();
            s.cancel();
         } else {
            Subscription previousSubscription = this.s;
            this.s = s;
            if (previousSubscription == null) {
               this.actual.onSubscribe(this);
            } else {
               long actualRequested = FluxConcatArray.activateAndGetRequested(REQUESTED, this);
               if (actualRequested > 0L) {
                  s.request(actualRequested);
               }

            }
         }
      }

      @Override
      public void onNext(T t) {
         ++this.produced;
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         this.remove();
         this.actual.onError(t);
      }

      @Override
      public void onComplete() {
         if (this.get() == FluxConcatArray.WORKING) {
            this.set(FluxConcatArray.DONE);
         } else {
            Publisher<? extends T>[] a = this.sources;

            Object state;
            do {
               this.set(FluxConcatArray.WORKING);
               int i = this.index;
               if (i == a.length) {
                  this.remove();
                  if (this.cancelled) {
                     return;
                  } else {
                     this.actual.onComplete();
                     return;
                  }
               }

               Publisher<? extends T> p = a[i];
               if (p == null) {
                  this.remove();
                  if (this.cancelled) {
                     return;
                  }

                  this.actual.onError(new NullPointerException("Source Publisher at index " + i + " is null"));
                  return;
               }

               long c = this.produced;
               if (c != 0L) {
                  this.produced = 0L;
                  FluxConcatArray.deactivateAndProduce(c, REQUESTED, this);
               }

               this.index = ++i;
               if (this.cancelled) {
                  return;
               }

               p.subscribe(this);
               state = this.get();
            } while(state == FluxConcatArray.DONE);

            this.remove();
         }
      }

      @Override
      public void request(long n) {
         Subscription subscription = FluxConcatArray.addCapAndGetSubscription(n, REQUESTED, this);
         if (subscription != null) {
            subscription.request(n);
         }
      }

      @Override
      public void cancel() {
         this.remove();
         this.cancelled = true;
         if ((this.requested & Long.MIN_VALUE) != Long.MIN_VALUE) {
            this.s.cancel();
         }

      }

      @Override
      public Subscription upstream() {
         return this.s;
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.RUN_STYLE) {
            return Scannable.Attr.RunStyle.SYNC;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else {
            return key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM ? this.requested : InnerOperator.super.scanUnsafe(key);
         }
      }
   }

   interface SubscriptionAware {
      Subscription upstream();
   }
}
