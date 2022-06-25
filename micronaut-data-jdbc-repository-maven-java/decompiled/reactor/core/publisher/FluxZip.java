package reactor.core.publisher;

import java.util.Arrays;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxZip<T, R> extends Flux<R> implements SourceProducer<R> {
   final Publisher<? extends T>[] sources;
   final Iterable<? extends Publisher<? extends T>> sourcesIterable;
   final Function<? super Object[], ? extends R> zipper;
   final Supplier<? extends Queue<T>> queueSupplier;
   final int prefetch;

   <U> FluxZip(
      Publisher<? extends T> p1,
      Publisher<? extends U> p2,
      BiFunction<? super T, ? super U, ? extends R> zipper2,
      Supplier<? extends Queue<T>> queueSupplier,
      int prefetch
   ) {
      this(
         new Publisher[]{(Publisher)Objects.requireNonNull(p1, "p1"), (Publisher)Objects.requireNonNull(p2, "p2")},
         new FluxZip.PairwiseZipper(new BiFunction[]{(BiFunction)Objects.requireNonNull(zipper2, "zipper2")}),
         queueSupplier,
         prefetch
      );
   }

   FluxZip(Publisher<? extends T>[] sources, Function<? super Object[], ? extends R> zipper, Supplier<? extends Queue<T>> queueSupplier, int prefetch) {
      if (prefetch <= 0) {
         throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
      } else {
         this.sources = (Publisher[])Objects.requireNonNull(sources, "sources");
         if (sources.length == 0) {
            throw new IllegalArgumentException("at least one source is required");
         } else {
            this.sourcesIterable = null;
            this.zipper = (Function)Objects.requireNonNull(zipper, "zipper");
            this.queueSupplier = (Supplier)Objects.requireNonNull(queueSupplier, "queueSupplier");
            this.prefetch = prefetch;
         }
      }
   }

   FluxZip(
      Iterable<? extends Publisher<? extends T>> sourcesIterable,
      Function<? super Object[], ? extends R> zipper,
      Supplier<? extends Queue<T>> queueSupplier,
      int prefetch
   ) {
      if (prefetch <= 0) {
         throw new IllegalArgumentException("prefetch > 0 required but it was " + prefetch);
      } else {
         this.sources = null;
         this.sourcesIterable = (Iterable)Objects.requireNonNull(sourcesIterable, "sourcesIterable");
         this.zipper = (Function)Objects.requireNonNull(zipper, "zipper");
         this.queueSupplier = (Supplier)Objects.requireNonNull(queueSupplier, "queueSupplier");
         this.prefetch = prefetch;
      }
   }

   @Override
   public int getPrefetch() {
      return this.prefetch;
   }

   @Nullable
   FluxZip<T, R> zipAdditionalSource(Publisher source, BiFunction zipper) {
      Publisher[] oldSources = this.sources;
      if (oldSources != null && this.zipper instanceof FluxZip.PairwiseZipper) {
         int oldLen = oldSources.length;
         Publisher<? extends T>[] newSources = new Publisher[oldLen + 1];
         System.arraycopy(oldSources, 0, newSources, 0, oldLen);
         newSources[oldLen] = source;
         Function<Object[], R> z = ((FluxZip.PairwiseZipper)this.zipper).then(zipper);
         return new FluxZip<>(newSources, z, this.queueSupplier, this.prefetch);
      } else {
         return null;
      }
   }

   @Override
   public void subscribe(CoreSubscriber<? super R> actual) {
      Publisher<? extends T>[] srcs = this.sources;

      try {
         if (srcs != null) {
            this.handleArrayMode(actual, srcs);
         } else {
            this.handleIterableMode(actual, this.sourcesIterable);
         }

      } catch (Throwable var4) {
         Operators.reportThrowInSubscribe(actual, var4);
      }
   }

   void handleIterableMode(CoreSubscriber<? super R> s, Iterable<? extends Publisher<? extends T>> sourcesIterable) {
      Object[] scalars = new Object[8];
      Publisher<? extends T>[] srcs = new Publisher[8];
      int n = 0;
      int sc = 0;

      for(Publisher<? extends T> p : sourcesIterable) {
         if (p == null) {
            Operators.error(s, Operators.onOperatorError(new NullPointerException("The sourcesIterable returned a null Publisher"), s.currentContext()));
            return;
         }

         if (p instanceof Callable) {
            Callable<T> callable = (Callable)p;

            T v;
            try {
               v = (T)callable.call();
            } catch (Throwable var13) {
               Operators.error(s, Operators.onOperatorError(var13, s.currentContext()));
               return;
            }

            if (v == null) {
               Operators.complete(s);
               return;
            }

            if (n == scalars.length) {
               Object[] b = new Object[n + (n >> 1)];
               System.arraycopy(scalars, 0, b, 0, n);
               Publisher<T>[] c = new Publisher[b.length];
               System.arraycopy(srcs, 0, c, 0, n);
               scalars = b;
               srcs = c;
            }

            scalars[n] = v;
            ++sc;
         } else {
            if (n == srcs.length) {
               Object[] b = new Object[n + (n >> 1)];
               System.arraycopy(scalars, 0, b, 0, n);
               Publisher<T>[] c = new Publisher[b.length];
               System.arraycopy(srcs, 0, c, 0, n);
               scalars = b;
               srcs = c;
            }

            srcs[n] = p;
         }

         ++n;
      }

      if (n == 0) {
         Operators.complete(s);
      } else {
         if (n < scalars.length) {
            scalars = Arrays.copyOfRange(scalars, 0, n, scalars.getClass());
         }

         this.handleBoth(s, srcs, scalars, n, sc);
      }
   }

   void handleArrayMode(CoreSubscriber<? super R> s, Publisher<? extends T>[] srcs) {
      Object[] scalars = null;
      int n = srcs.length;
      int sc = 0;

      for(int j = 0; j < n; ++j) {
         Publisher<? extends T> p = srcs[j];
         if (p == null) {
            Operators.error(s, new NullPointerException("The sources contained a null Publisher"));
            return;
         }

         if (p instanceof Callable) {
            Object v;
            try {
               v = ((Callable)p).call();
            } catch (Throwable var10) {
               Operators.error(s, Operators.onOperatorError(var10, s.currentContext()));
               return;
            }

            if (v == null) {
               Operators.complete(s);
               return;
            }

            if (scalars == null) {
               scalars = new Object[n];
            }

            scalars[j] = v;
            ++sc;
         }
      }

      this.handleBoth(s, srcs, scalars, n, sc);
   }

   void handleBoth(CoreSubscriber<? super R> s, Publisher<? extends T>[] srcs, @Nullable Object[] scalars, int n, int sc) {
      if (sc == 0 || scalars == null) {
         FluxZip.ZipCoordinator<T, R> coordinator = new FluxZip.ZipCoordinator<>(s, this.zipper, n, this.queueSupplier, this.prefetch);
         s.onSubscribe(coordinator);
         coordinator.subscribe(srcs, n);
      } else if (n != sc) {
         FluxZip.ZipSingleCoordinator<T, R> coordinator = new FluxZip.ZipSingleCoordinator<>(s, scalars, n, this.zipper);
         s.onSubscribe(coordinator);
         coordinator.subscribe(n, sc, srcs);
      } else {
         Operators.MonoSubscriber<R, R> sds = new Operators.MonoSubscriber<>(s);
         s.onSubscribe(sds);

         R r;
         try {
            r = (R)Objects.requireNonNull(this.zipper.apply(scalars), "The zipper returned a null value");
         } catch (Throwable var9) {
            s.onError(Operators.onOperatorError(var9, s.currentContext()));
            return;
         }

         sds.complete(r);
      }

   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PREFETCH) {
         return this.prefetch;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   static final class PairwiseZipper<R> implements Function<Object[], R> {
      final BiFunction[] zippers;

      PairwiseZipper(BiFunction[] zippers) {
         this.zippers = zippers;
      }

      public R apply(Object[] args) {
         Object o = this.zippers[0].apply(args[0], args[1]);

         for(int i = 1; i < this.zippers.length; ++i) {
            o = this.zippers[i].apply(o, args[i + 1]);
         }

         return (R)o;
      }

      public FluxZip.PairwiseZipper then(BiFunction zipper) {
         BiFunction[] zippers = this.zippers;
         int n = zippers.length;
         BiFunction[] newZippers = new BiFunction[n + 1];
         System.arraycopy(zippers, 0, newZippers, 0, n);
         newZippers[n] = zipper;
         return new FluxZip.PairwiseZipper(newZippers);
      }
   }

   static final class ZipCoordinator<T, R> implements InnerProducer<R> {
      final CoreSubscriber<? super R> actual;
      final FluxZip.ZipInner<T>[] subscribers;
      final Function<? super Object[], ? extends R> zipper;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxZip.ZipCoordinator> WIP = AtomicIntegerFieldUpdater.newUpdater(FluxZip.ZipCoordinator.class, "wip");
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxZip.ZipCoordinator> REQUESTED = AtomicLongFieldUpdater.newUpdater(FluxZip.ZipCoordinator.class, "requested");
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxZip.ZipCoordinator, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxZip.ZipCoordinator.class, Throwable.class, "error"
      );
      volatile boolean cancelled;
      final Object[] current;

      ZipCoordinator(
         CoreSubscriber<? super R> actual, Function<? super Object[], ? extends R> zipper, int n, Supplier<? extends Queue<T>> queueSupplier, int prefetch
      ) {
         this.actual = actual;
         this.zipper = zipper;
         FluxZip.ZipInner<T>[] a = new FluxZip.ZipInner[n];

         for(int i = 0; i < n; ++i) {
            a[i] = new FluxZip.ZipInner<>(this, prefetch, i, queueSupplier);
         }

         this.current = new Object[n];
         this.subscribers = a;
      }

      void subscribe(Publisher<? extends T>[] sources, int n) {
         FluxZip.ZipInner<T>[] a = this.subscribers;

         for(int i = 0; i < n; ++i) {
            if (this.cancelled || this.error != null) {
               return;
            }

            FluxZip.ZipInner<T> s = a[i];

            try {
               sources[i].subscribe(s);
            } catch (Throwable var7) {
               Operators.reportThrowInSubscribe(s, var7);
            }
         }

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
            this.cancelAll();
         }

      }

      @Override
      public CoreSubscriber<? super R> actual() {
         return this.actual;
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.subscribers);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerProducer.super.scanUnsafe(key);
         }
      }

      void error(Throwable e, int index) {
         if (Exceptions.addThrowable(ERROR, this, e)) {
            this.drain();
         } else {
            Operators.onErrorDropped(e, this.actual.currentContext());
         }

      }

      void cancelAll() {
         for(FluxZip.ZipInner<T> s : this.subscribers) {
            s.cancel();
         }

      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            CoreSubscriber<? super R> a = this.actual;
            FluxZip.ZipInner<T>[] qs = this.subscribers;
            int n = qs.length;
            Object[] values = this.current;
            int missed = 1;

            do {
               long r = this.requested;
               long e = 0L;

               while(r != e) {
                  if (this.cancelled) {
                     return;
                  }

                  if (this.error != null) {
                     this.cancelAll();
                     Throwable ex = Exceptions.terminate(ERROR, this);
                     a.onError(ex);
                     return;
                  }

                  boolean empty = false;

                  for(int j = 0; j < n; ++j) {
                     FluxZip.ZipInner<T> inner = qs[j];
                     if (values[j] == null) {
                        try {
                           boolean d = inner.done;
                           Queue<T> q = inner.queue;
                           T v = (T)(q != null ? q.poll() : null);
                           boolean sourceEmpty = v == null;
                           if (d && sourceEmpty) {
                              this.cancelAll();
                              a.onComplete();
                              return;
                           }

                           if (!sourceEmpty) {
                              values[j] = v;
                           } else {
                              empty = true;
                           }
                        } catch (Throwable var18) {
                           Throwable q = Operators.onOperatorError(var18, this.actual.currentContext());
                           this.cancelAll();
                           Exceptions.addThrowable(ERROR, this, q);
                           q = Exceptions.terminate(ERROR, this);
                           a.onError(q);
                           return;
                        }
                     }
                  }

                  if (empty) {
                     break;
                  }

                  R v;
                  try {
                     v = (R)Objects.requireNonNull(this.zipper.apply(values.clone()), "The zipper returned a null value");
                  } catch (Throwable var17) {
                     Throwable var27 = Operators.onOperatorError(null, var17, values.clone(), this.actual.currentContext());
                     this.cancelAll();
                     Exceptions.addThrowable(ERROR, this, var27);
                     var27 = Exceptions.terminate(ERROR, this);
                     a.onError(var27);
                     return;
                  }

                  a.onNext(v);
                  ++e;
                  Arrays.fill(values, null);
               }

               if (r == e) {
                  if (this.cancelled) {
                     return;
                  }

                  if (this.error != null) {
                     this.cancelAll();
                     Throwable ex = Exceptions.terminate(ERROR, this);
                     a.onError(ex);
                     return;
                  }

                  for(int j = 0; j < n; ++j) {
                     FluxZip.ZipInner<T> inner = qs[j];
                     if (values[j] == null) {
                        try {
                           boolean d = inner.done;
                           Queue<T> q = inner.queue;
                           T v = (T)(q != null ? q.poll() : null);
                           boolean empty = v == null;
                           if (d && empty) {
                              this.cancelAll();
                              a.onComplete();
                              return;
                           }

                           if (!empty) {
                              values[j] = v;
                           }
                        } catch (Throwable var19) {
                           Throwable var29 = Operators.onOperatorError(null, var19, values, this.actual.currentContext());
                           this.cancelAll();
                           Exceptions.addThrowable(ERROR, this, var29);
                           var29 = Exceptions.terminate(ERROR, this);
                           a.onError(var29);
                           return;
                        }
                     }
                  }
               }

               if (e != 0L) {
                  for(int j = 0; j < n; ++j) {
                     FluxZip.ZipInner<T> inner = qs[j];
                     inner.request(e);
                  }

                  if (r != Long.MAX_VALUE) {
                     REQUESTED.addAndGet(this, -e);
                  }
               }

               missed = WIP.addAndGet(this, -missed);
            } while(missed != 0);

         }
      }
   }

   static final class ZipInner<T> implements InnerConsumer<T> {
      final FluxZip.ZipCoordinator<T, ?> parent;
      final int prefetch;
      final int limit;
      final int index;
      final Supplier<? extends Queue<T>> queueSupplier;
      volatile Queue<T> queue;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<FluxZip.ZipInner, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         FluxZip.ZipInner.class, Subscription.class, "s"
      );
      long produced;
      volatile boolean done;
      int sourceMode;

      ZipInner(FluxZip.ZipCoordinator<T, ?> parent, int prefetch, int index, Supplier<? extends Queue<T>> queueSupplier) {
         this.parent = parent;
         this.prefetch = prefetch;
         this.index = index;
         this.queueSupplier = queueSupplier;
         this.limit = Operators.unboundedOrLimit(prefetch);
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            if (s instanceof Fuseable.QueueSubscription) {
               Fuseable.QueueSubscription<T> f = (Fuseable.QueueSubscription)s;
               int m = f.requestFusion(7);
               if (m == 1) {
                  this.sourceMode = 1;
                  this.queue = f;
                  this.done = true;
                  this.parent.drain();
                  return;
               }

               if (m == 2) {
                  this.sourceMode = 2;
                  this.queue = f;
               } else {
                  this.queue = (Queue)this.queueSupplier.get();
               }
            } else {
               this.queue = (Queue)this.queueSupplier.get();
            }

            s.request(Operators.unboundedOrPrefetch(this.prefetch));
         }

      }

      @Override
      public void onNext(T t) {
         if (this.sourceMode != 2 && !this.queue.offer(t)) {
            this.onError(
               Operators.onOperatorError(
                  this.s, Exceptions.failWithOverflow("Queue is full: Reactive Streams source doesn't respect backpressure"), this.currentContext()
               )
            );
         } else {
            this.parent.drain();
         }
      }

      @Override
      public Context currentContext() {
         return this.parent.actual.currentContext();
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.currentContext());
         } else {
            this.done = true;
            this.parent.error(t, this.index);
         }
      }

      @Override
      public void onComplete() {
         this.done = true;
         this.parent.drain();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.parent;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.s == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue != null ? this.queue.size() : 0;
         } else if (key != Scannable.Attr.TERMINATED) {
            if (key == Scannable.Attr.PREFETCH) {
               return this.prefetch;
            } else {
               return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
            }
         } else {
            return this.done && (this.queue == null || this.queue.isEmpty());
         }
      }

      void cancel() {
         Operators.terminate(S, this);
      }

      void request(long n) {
         if (this.sourceMode != 1) {
            long p = this.produced + n;
            if (p >= (long)this.limit) {
               this.produced = 0L;
               this.s.request(p);
            } else {
               this.produced = p;
            }
         }

      }
   }

   static final class ZipSingleCoordinator<T, R> extends Operators.MonoSubscriber<R, R> {
      final Function<? super Object[], ? extends R> zipper;
      final Object[] scalars;
      final FluxZip.ZipSingleSubscriber<T>[] subscribers;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxZip.ZipSingleCoordinator> WIP = AtomicIntegerFieldUpdater.newUpdater(FluxZip.ZipSingleCoordinator.class, "wip");

      ZipSingleCoordinator(CoreSubscriber<? super R> subscriber, Object[] scalars, int n, Function<? super Object[], ? extends R> zipper) {
         super(subscriber);
         this.zipper = zipper;
         this.scalars = scalars;
         FluxZip.ZipSingleSubscriber<T>[] a = new FluxZip.ZipSingleSubscriber[n];

         for(int i = 0; i < n; ++i) {
            if (scalars[i] == null) {
               a[i] = new FluxZip.ZipSingleSubscriber<>(this, i);
            }
         }

         this.subscribers = a;
      }

      void subscribe(int n, int sc, Publisher<? extends T>[] sources) {
         WIP.lazySet(this, n - sc);
         FluxZip.ZipSingleSubscriber<T>[] a = this.subscribers;

         for(int i = 0; i < n && this.wip > 0 && !this.isCancelled(); ++i) {
            FluxZip.ZipSingleSubscriber<T> s = a[i];
            if (s != null) {
               try {
                  sources[i].subscribe(s);
               } catch (Throwable var8) {
                  Operators.reportThrowInSubscribe(s, var8);
               }
            }
         }

      }

      void next(T value, int index) {
         Object[] a = this.scalars;
         a[index] = value;
         if (WIP.decrementAndGet(this) == 0) {
            R r;
            try {
               r = (R)Objects.requireNonNull(this.zipper.apply(a), "The zipper returned a null value");
            } catch (Throwable var6) {
               this.actual.onError(Operators.onOperatorError(this, var6, value, this.actual.currentContext()));
               return;
            }

            this.complete(r);
         }

      }

      void error(Throwable e, int index) {
         if (WIP.getAndSet(this, 0) > 0) {
            this.cancelAll();
            this.actual.onError(e);
         } else {
            Operators.onErrorDropped(e, this.actual.currentContext());
         }

      }

      void complete(int index) {
         if (WIP.getAndSet(this, 0) > 0) {
            this.cancelAll();
            this.actual.onComplete();
         }

      }

      @Override
      public void cancel() {
         super.cancel();
         this.cancelAll();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.wip == 0;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.wip > 0 ? this.scalars.length : 0;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.subscribers);
      }

      void cancelAll() {
         for(FluxZip.ZipSingleSubscriber<T> s : this.subscribers) {
            if (s != null) {
               s.dispose();
            }
         }

      }
   }

   static final class ZipSingleSubscriber<T> implements InnerConsumer<T>, Disposable {
      final FluxZip.ZipSingleCoordinator<T, ?> parent;
      final int index;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<FluxZip.ZipSingleSubscriber, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         FluxZip.ZipSingleSubscriber.class, Subscription.class, "s"
      );
      boolean done;

      ZipSingleSubscriber(FluxZip.ZipSingleCoordinator<T, ?> parent, int index) {
         this.parent = parent;
         this.index = index;
      }

      @Override
      public Context currentContext() {
         return this.parent.currentContext();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.parent;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.s == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.parent.scalars[this.index] == null ? 0 : 1;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            this.s = s;
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.parent.currentContext());
         } else {
            this.done = true;
            Operators.terminate(S, this);
            this.parent.next(t, this.index);
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.parent.currentContext());
         } else {
            this.done = true;
            this.parent.error(t, this.index);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.parent.complete(this.index);
         }
      }

      @Override
      public void dispose() {
         Operators.terminate(S, this);
      }
   }
}
