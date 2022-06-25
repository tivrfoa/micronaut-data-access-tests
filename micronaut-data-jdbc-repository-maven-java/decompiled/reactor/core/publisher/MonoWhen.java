package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class MonoWhen extends Mono<Void> implements SourceProducer<Void> {
   final boolean delayError;
   final Publisher<?>[] sources;
   final Iterable<? extends Publisher<?>> sourcesIterable;

   MonoWhen(boolean delayError, Publisher<?>... sources) {
      this.delayError = delayError;
      this.sources = (Publisher[])Objects.requireNonNull(sources, "sources");
      this.sourcesIterable = null;
   }

   MonoWhen(boolean delayError, Iterable<? extends Publisher<?>> sourcesIterable) {
      this.delayError = delayError;
      this.sources = null;
      this.sourcesIterable = (Iterable)Objects.requireNonNull(sourcesIterable, "sourcesIterable");
   }

   @Nullable
   Mono<Void> whenAdditionalSource(Publisher<?> source) {
      Publisher[] oldSources = this.sources;
      if (oldSources != null) {
         int oldLen = oldSources.length;
         Publisher<?>[] newSources = new Publisher[oldLen + 1];
         System.arraycopy(oldSources, 0, newSources, 0, oldLen);
         newSources[oldLen] = source;
         return new MonoWhen(this.delayError, newSources);
      } else {
         return null;
      }
   }

   @Override
   public void subscribe(CoreSubscriber<? super Void> actual) {
      int n = 0;
      Publisher<?>[] a;
      if (this.sources != null) {
         a = this.sources;
         n = a.length;
      } else {
         a = new Publisher[8];

         for(Publisher<?> m : this.sourcesIterable) {
            if (n == a.length) {
               Publisher<?>[] b = new Publisher[n + (n >> 2)];
               System.arraycopy(a, 0, b, 0, n);
               a = b;
            }

            a[n++] = m;
         }
      }

      if (n == 0) {
         Operators.complete(actual);
      } else {
         MonoWhen.WhenCoordinator parent = new MonoWhen.WhenCoordinator(actual, n, this.delayError);
         actual.onSubscribe(parent);
         parent.subscribe(a);
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

   static final class WhenCoordinator extends Operators.MonoSubscriber<Object, Void> {
      final MonoWhen.WhenInner[] subscribers;
      final boolean delayError;
      volatile int done;
      static final AtomicIntegerFieldUpdater<MonoWhen.WhenCoordinator> DONE = AtomicIntegerFieldUpdater.newUpdater(MonoWhen.WhenCoordinator.class, "done");

      WhenCoordinator(CoreSubscriber<? super Void> subscriber, int n, boolean delayError) {
         super(subscriber);
         this.delayError = delayError;
         this.subscribers = new MonoWhen.WhenInner[n];

         for(int i = 0; i < n; ++i) {
            this.subscribers[i] = new MonoWhen.WhenInner(this);
         }

      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.done == this.subscribers.length;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.subscribers.length;
         } else if (key == Scannable.Attr.DELAY_ERROR) {
            return this.delayError;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.subscribers);
      }

      void subscribe(Publisher<?>[] sources) {
         MonoWhen.WhenInner[] a = this.subscribers;

         for(int i = 0; i < a.length; ++i) {
            sources[i].subscribe(a[i]);
         }

      }

      void signalError(Throwable t) {
         if (this.delayError) {
            this.signal();
         } else {
            int n = this.subscribers.length;
            if (DONE.getAndSet(this, n) != n) {
               this.cancel();
               this.actual.onError(t);
            }
         }

      }

      void signal() {
         MonoWhen.WhenInner[] a = this.subscribers;
         int n = a.length;
         if (DONE.incrementAndGet(this) == n) {
            Throwable error = null;
            Throwable compositeError = null;

            for(int i = 0; i < a.length; ++i) {
               MonoWhen.WhenInner m = a[i];
               Throwable e = m.error;
               if (e != null) {
                  if (compositeError != null) {
                     compositeError.addSuppressed(e);
                  } else if (error != null) {
                     compositeError = Exceptions.multiple(error, e);
                  } else {
                     error = e;
                  }
               }
            }

            if (compositeError != null) {
               this.actual.onError(compositeError);
            } else if (error != null) {
               this.actual.onError(error);
            } else {
               this.actual.onComplete();
            }

         }
      }

      @Override
      public void cancel() {
         if (!this.isCancelled()) {
            super.cancel();

            for(MonoWhen.WhenInner ms : this.subscribers) {
               ms.cancel();
            }
         }

      }
   }

   static final class WhenInner implements InnerConsumer<Object> {
      final MonoWhen.WhenCoordinator parent;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<MonoWhen.WhenInner, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         MonoWhen.WhenInner.class, Subscription.class, "s"
      );
      Throwable error;

      WhenInner(MonoWhen.WhenCoordinator parent) {
         this.parent = parent;
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
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public Context currentContext() {
         return this.parent.currentContext();
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            s.request(Long.MAX_VALUE);
         } else {
            s.cancel();
         }

      }

      @Override
      public void onNext(Object t) {
      }

      @Override
      public void onError(Throwable t) {
         this.error = t;
         this.parent.signalError(t);
      }

      @Override
      public void onComplete() {
         this.parent.signal();
      }

      void cancel() {
         Operators.terminate(S, this);
      }
   }
}
