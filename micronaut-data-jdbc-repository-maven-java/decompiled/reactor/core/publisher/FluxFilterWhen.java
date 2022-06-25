package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;

class FluxFilterWhen<T> extends InternalFluxOperator<T, T> {
   final Function<? super T, ? extends Publisher<Boolean>> asyncPredicate;
   final int bufferSize;

   FluxFilterWhen(Flux<T> source, Function<? super T, ? extends Publisher<Boolean>> asyncPredicate, int bufferSize) {
      super(source);
      this.asyncPredicate = asyncPredicate;
      this.bufferSize = bufferSize;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxFilterWhen.FluxFilterWhenSubscriber<>(actual, this.asyncPredicate, this.bufferSize);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class FilterWhenInner implements InnerConsumer<Boolean> {
      final FluxFilterWhen.FluxFilterWhenSubscriber<?> parent;
      final boolean cancelOnNext;
      boolean done;
      volatile Subscription sub;
      static final AtomicReferenceFieldUpdater<FluxFilterWhen.FilterWhenInner, Subscription> SUB = AtomicReferenceFieldUpdater.newUpdater(
         FluxFilterWhen.FilterWhenInner.class, Subscription.class, "sub"
      );

      FilterWhenInner(FluxFilterWhen.FluxFilterWhenSubscriber<?> parent, boolean cancelOnNext) {
         this.parent = parent;
         this.cancelOnNext = cancelOnNext;
      }

      @Override
      public Context currentContext() {
         return this.parent.currentContext();
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(SUB, this, s)) {
            s.request(Long.MAX_VALUE);
         }

      }

      public void onNext(Boolean t) {
         if (!this.done) {
            if (this.cancelOnNext) {
               this.sub.cancel();
            }

            this.done = true;
            this.parent.innerResult(t);
         }

      }

      @Override
      public void onError(Throwable t) {
         if (!this.done) {
            this.done = true;
            this.parent.innerError(t);
         } else {
            Operators.onErrorDropped(t, this.parent.currentContext());
         }

      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.parent.innerComplete();
         }

      }

      void cancel() {
         Operators.terminate(SUB, this);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.parent;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.sub;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.sub == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.PREFETCH) {
            return Integer.MAX_VALUE;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.done ? 0L : 1L;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }
   }

   static final class FluxFilterWhenSubscriber<T> implements InnerOperator<T, T> {
      final Function<? super T, ? extends Publisher<Boolean>> asyncPredicate;
      final int bufferSize;
      final AtomicReferenceArray<T> toFilter;
      final CoreSubscriber<? super T> actual;
      final Context ctx;
      int consumed;
      long consumerIndex;
      long emitted;
      Boolean innerResult;
      long producerIndex;
      Subscription upstream;
      volatile boolean cancelled;
      volatile FluxFilterWhen.FilterWhenInner current;
      volatile boolean done;
      volatile Throwable error;
      volatile long requested;
      volatile int state;
      volatile int wip;
      static final AtomicReferenceFieldUpdater<FluxFilterWhen.FluxFilterWhenSubscriber, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxFilterWhen.FluxFilterWhenSubscriber.class, Throwable.class, "error"
      );
      static final AtomicLongFieldUpdater<FluxFilterWhen.FluxFilterWhenSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxFilterWhen.FluxFilterWhenSubscriber.class, "requested"
      );
      static final AtomicIntegerFieldUpdater<FluxFilterWhen.FluxFilterWhenSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxFilterWhen.FluxFilterWhenSubscriber.class, "wip"
      );
      static final AtomicReferenceFieldUpdater<FluxFilterWhen.FluxFilterWhenSubscriber, FluxFilterWhen.FilterWhenInner> CURRENT = AtomicReferenceFieldUpdater.newUpdater(
         FluxFilterWhen.FluxFilterWhenSubscriber.class, FluxFilterWhen.FilterWhenInner.class, "current"
      );
      static final FluxFilterWhen.FilterWhenInner INNER_CANCELLED = new FluxFilterWhen.FilterWhenInner(null, false);
      static final int STATE_FRESH = 0;
      static final int STATE_RUNNING = 1;
      static final int STATE_RESULT = 2;

      FluxFilterWhenSubscriber(CoreSubscriber<? super T> actual, Function<? super T, ? extends Publisher<Boolean>> asyncPredicate, int bufferSize) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.toFilter = new AtomicReferenceArray(Queues.ceilingNextPowerOfTwo(bufferSize));
         this.asyncPredicate = asyncPredicate;
         this.bufferSize = bufferSize;
      }

      @Override
      public final CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Override
      public void onNext(T t) {
         long pi = this.producerIndex;
         int m = this.toFilter.length() - 1;
         int offset = (int)pi & m;
         this.toFilter.lazySet(offset, t);
         this.producerIndex = pi + 1L;
         this.drain();
      }

      @Override
      public void onError(Throwable t) {
         ERROR.set(this, t);
         this.done = true;
         this.drain();
      }

      @Override
      public void onComplete() {
         this.done = true;
         this.drain();
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
            this.drain();
         }

      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            this.upstream.cancel();
            this.cancelInner();
            if (WIP.getAndIncrement(this) == 0) {
               this.clear();
            }
         }

      }

      void cancelInner() {
         FluxFilterWhen.FilterWhenInner a = (FluxFilterWhen.FilterWhenInner)CURRENT.get(this);
         if (a != INNER_CANCELLED) {
            a = (FluxFilterWhen.FilterWhenInner)CURRENT.getAndSet(this, INNER_CANCELLED);
            if (a != null && a != INNER_CANCELLED) {
               a.cancel();
            }
         }

      }

      void clear() {
         int n = this.toFilter.length();

         for(int i = 0; i < n; ++i) {
            T old = (T)this.toFilter.getAndSet(i, null);
            Operators.onDiscard(old, this.ctx);
         }

         this.innerResult = null;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.upstream, s)) {
            this.upstream = s;
            this.actual.onSubscribe(this);
            s.request((long)this.bufferSize);
         }

      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            int missed = 1;
            int limit = Operators.unboundedOrLimit(this.bufferSize);
            long e = this.emitted;
            long ci = this.consumerIndex;
            int f = this.consumed;
            int m = this.toFilter.length() - 1;
            Subscriber<? super T> a = this.actual;

            while(true) {
               long r = this.requested;

               while(e != r) {
                  if (this.cancelled) {
                     this.clear();
                     return;
                  }

                  boolean d = this.done;
                  int offset = (int)ci & m;
                  T t = (T)this.toFilter.get(offset);
                  boolean empty = t == null;
                  if (d && empty) {
                     Throwable ex = Exceptions.terminate(ERROR, this);
                     if (ex == null) {
                        a.onComplete();
                     } else {
                        a.onError(ex);
                     }

                     return;
                  }

                  if (empty) {
                     break;
                  }

                  int s = this.state;
                  if (s == 0) {
                     Publisher<Boolean> p;
                     try {
                        p = (Publisher)Objects.requireNonNull(this.asyncPredicate.apply(t), "The asyncPredicate returned a null value");
                     } catch (Throwable var21) {
                        Exceptions.throwIfFatal(var21);
                        Exceptions.addThrowable(ERROR, this, var21);
                        p = null;
                     }

                     if (p != null) {
                        if (p instanceof Callable) {
                           Boolean u;
                           try {
                              u = (Boolean)((Callable)p).call();
                           } catch (Throwable var20) {
                              Exceptions.throwIfFatal(var20);
                              Exceptions.addThrowable(ERROR, this, var20);
                              u = null;
                           }

                           if (u != null && u) {
                              a.onNext(t);
                              ++e;
                           } else {
                              Operators.onDiscard(t, this.ctx);
                           }
                        } else {
                           FluxFilterWhen.FilterWhenInner inner = new FluxFilterWhen.FilterWhenInner(this, !(p instanceof Mono));
                           if (CURRENT.compareAndSet(this, null, inner)) {
                              this.state = 1;
                              p.subscribe(inner);
                              break;
                           }
                        }
                     }

                     T old = (T)this.toFilter.getAndSet(offset, null);
                     Operators.onDiscard(old, this.ctx);
                     ++ci;
                     if (++f == limit) {
                        f = 0;
                        this.upstream.request((long)limit);
                     }
                  } else {
                     if (s != 2) {
                        break;
                     }

                     Boolean u = this.innerResult;
                     this.innerResult = null;
                     if (u != null && u) {
                        a.onNext(t);
                        ++e;
                     } else {
                        Operators.onDiscard(t, this.ctx);
                     }

                     this.toFilter.lazySet(offset, null);
                     ++ci;
                     if (++f == limit) {
                        f = 0;
                        this.upstream.request((long)limit);
                     }

                     this.state = 0;
                  }
               }

               if (e == r) {
                  if (this.cancelled) {
                     this.clear();
                     return;
                  }

                  boolean d = this.done;
                  int offset = (int)ci & m;
                  T t = (T)this.toFilter.get(offset);
                  boolean empty = t == null;
                  if (d && empty) {
                     Throwable ex = Exceptions.terminate(ERROR, this);
                     if (ex == null) {
                        a.onComplete();
                     } else {
                        a.onError(ex);
                     }

                     return;
                  }
               }

               int w = this.wip;
               if (missed == w) {
                  this.consumed = f;
                  this.consumerIndex = ci;
                  this.emitted = e;
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

      void clearCurrent() {
         FluxFilterWhen.FilterWhenInner c = this.current;
         if (c != INNER_CANCELLED) {
            CURRENT.compareAndSet(this, c, null);
         }

      }

      void innerResult(Boolean item) {
         this.innerResult = item;
         this.state = 2;
         this.clearCurrent();
         this.drain();
      }

      void innerError(Throwable ex) {
         Exceptions.addThrowable(ERROR, this, ex);
         this.state = 2;
         this.clearCurrent();
         this.drain();
      }

      void innerComplete() {
         this.state = 2;
         this.clearCurrent();
         this.drain();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.upstream;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.CAPACITY) {
            return this.toFilter.length();
         } else if (key == Scannable.Attr.LARGE_BUFFERED) {
            return this.producerIndex - this.consumerIndex;
         } else if (key == Scannable.Attr.BUFFERED) {
            long realBuffered = this.producerIndex - this.consumerIndex;
            return realBuffered <= 2147483647L ? (int)realBuffered : Integer.MIN_VALUE;
         } else if (key == Scannable.Attr.PREFETCH) {
            return this.bufferSize;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         FluxFilterWhen.FilterWhenInner c = this.current;
         return c == null ? Stream.empty() : Stream.of(c);
      }
   }
}
