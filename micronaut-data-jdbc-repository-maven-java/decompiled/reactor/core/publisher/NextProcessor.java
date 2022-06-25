package reactor.core.publisher;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

class NextProcessor<O> extends MonoProcessor<O> {
   final boolean isRefCounted;
   volatile NextProcessor.NextInner<O>[] subscribers;
   static final AtomicReferenceFieldUpdater<NextProcessor, NextProcessor.NextInner[]> SUBSCRIBERS = AtomicReferenceFieldUpdater.newUpdater(
      NextProcessor.class, NextProcessor.NextInner[].class, "subscribers"
   );
   static final NextProcessor.NextInner[] EMPTY = new NextProcessor.NextInner[0];
   static final NextProcessor.NextInner[] TERMINATED = new NextProcessor.NextInner[0];
   static final NextProcessor.NextInner[] EMPTY_WITH_SOURCE = new NextProcessor.NextInner[0];
   volatile Subscription subscription;
   static final AtomicReferenceFieldUpdater<NextProcessor, Subscription> UPSTREAM = AtomicReferenceFieldUpdater.newUpdater(
      NextProcessor.class, Subscription.class, "subscription"
   );
   @Nullable
   CorePublisher<? extends O> source;
   @Nullable
   Throwable error;
   @Nullable
   O value;

   NextProcessor(@Nullable CorePublisher<? extends O> source) {
      this(source, false);
   }

   NextProcessor(@Nullable CorePublisher<? extends O> source, boolean isRefCounted) {
      this.source = source;
      this.isRefCounted = isRefCounted;
      SUBSCRIBERS.lazySet(this, source != null ? EMPTY_WITH_SOURCE : EMPTY);
   }

   @Override
   public O peek() {
      if (!this.isTerminated()) {
         return null;
      } else if (this.value != null) {
         return this.value;
      } else if (this.error != null) {
         RuntimeException re = Exceptions.propagate(this.error);
         re = Exceptions.addSuppressed(re, new Exception("Mono#peek terminated with an error"));
         throw re;
      } else {
         return null;
      }
   }

   @Nullable
   @Override
   public O block(@Nullable Duration timeout) {
      try {
         if (this.isTerminated()) {
            return this.peek();
         } else {
            this.connect();
            long delay;
            if (null == timeout) {
               delay = 0L;
            } else {
               delay = System.nanoTime() + timeout.toNanos();
            }

            while(!this.isTerminated()) {
               if (timeout != null && delay < System.nanoTime()) {
                  this.cancel();
                  throw new IllegalStateException("Timeout on Mono blocking read");
               }

               Thread.sleep(1L);
            }

            if (this.error != null) {
               RuntimeException re = Exceptions.propagate(this.error);
               re = Exceptions.addSuppressed(re, new Exception("Mono#block terminated with an error"));
               throw re;
            } else {
               return this.value;
            }
         }
      } catch (InterruptedException var5) {
         Thread.currentThread().interrupt();
         throw new IllegalStateException("Thread Interruption on Mono blocking read");
      }
   }

   @Override
   public final void onComplete() {
      Sinks.EmitResult emitResult = this.tryEmitValue((O)null);
   }

   void emitEmpty(Sinks.EmitFailureHandler failureHandler) {
      Sinks.EmitResult emitResult;
      boolean shouldRetry;
      do {
         emitResult = this.tryEmitValue((O)null);
         if (emitResult.isSuccess()) {
            return;
         }

         shouldRetry = failureHandler.onEmitFailure(SignalType.ON_COMPLETE, emitResult);
      } while(shouldRetry);

      switch(emitResult) {
         case FAIL_ZERO_SUBSCRIBER:
         case FAIL_OVERFLOW:
         case FAIL_CANCELLED:
         case FAIL_TERMINATED:
            return;
         case FAIL_NON_SERIALIZED:
            throw new Sinks.EmissionException(
               emitResult, "Spec. Rule 1.3 - onSubscribe, onNext, onError and onComplete signaled to a Subscriber MUST be signaled serially."
            );
         default:
            throw new Sinks.EmissionException(emitResult, "Unknown emitResult value");
      }
   }

   @Override
   public final void onError(Throwable cause) {
      Sinks.EmitResult emitResult;
      boolean shouldRetry;
      do {
         emitResult = this.tryEmitError(cause);
         if (emitResult.isSuccess()) {
            return;
         }

         shouldRetry = Sinks.EmitFailureHandler.FAIL_FAST.onEmitFailure(SignalType.ON_ERROR, emitResult);
      } while(shouldRetry);

      switch(emitResult) {
         case FAIL_ZERO_SUBSCRIBER:
         case FAIL_OVERFLOW:
         case FAIL_CANCELLED:
            return;
         case FAIL_TERMINATED:
            Operators.onErrorDropped(cause, this.currentContext());
            return;
         case FAIL_NON_SERIALIZED:
            throw new Sinks.EmissionException(
               emitResult, "Spec. Rule 1.3 - onSubscribe, onNext, onError and onComplete signaled to a Subscriber MUST be signaled serially."
            );
         default:
            throw new Sinks.EmissionException(emitResult, "Unknown emitResult value");
      }
   }

   Sinks.EmitResult tryEmitError(Throwable cause) {
      Objects.requireNonNull(cause, "onError cannot be null");
      if (UPSTREAM.getAndSet(this, Operators.cancelledSubscription()) == Operators.cancelledSubscription()) {
         return Sinks.EmitResult.FAIL_TERMINATED;
      } else {
         this.error = cause;
         this.value = null;
         this.source = null;

         for(NextProcessor.NextInner<O> as : (NextProcessor.NextInner[])SUBSCRIBERS.getAndSet(this, TERMINATED)) {
            as.onError(cause);
         }

         return Sinks.EmitResult.OK;
      }
   }

   @Override
   public final void onNext(@Nullable O value) {
      if (value == null) {
         this.emitEmpty(Sinks.EmitFailureHandler.FAIL_FAST);
      } else {
         Sinks.EmitResult emitResult;
         boolean shouldRetry;
         do {
            emitResult = this.tryEmitValue(value);
            if (emitResult.isSuccess()) {
               return;
            }

            shouldRetry = Sinks.EmitFailureHandler.FAIL_FAST.onEmitFailure(SignalType.ON_NEXT, emitResult);
         } while(shouldRetry);

         switch(emitResult) {
            case FAIL_ZERO_SUBSCRIBER:
               return;
            case FAIL_OVERFLOW:
               Operators.onDiscard(value, this.currentContext());
               this.onError(Exceptions.failWithOverflow("Backpressure overflow during Sinks.Many#emitNext"));
               return;
            case FAIL_CANCELLED:
               Operators.onDiscard(value, this.currentContext());
               return;
            case FAIL_TERMINATED:
               Operators.onNextDropped(value, this.currentContext());
               return;
            case FAIL_NON_SERIALIZED:
               throw new Sinks.EmissionException(
                  emitResult, "Spec. Rule 1.3 - onSubscribe, onNext, onError and onComplete signaled to a Subscriber MUST be signaled serially."
               );
            default:
               throw new Sinks.EmissionException(emitResult, "Unknown emitResult value");
         }
      }
   }

   Sinks.EmitResult tryEmitValue(@Nullable O value) {
      Subscription s;
      if ((s = (Subscription)UPSTREAM.getAndSet(this, Operators.cancelledSubscription())) == Operators.cancelledSubscription()) {
         return Sinks.EmitResult.FAIL_TERMINATED;
      } else {
         this.value = value;
         Publisher<? extends O> parent = this.source;
         this.source = null;
         NextProcessor.NextInner<O>[] array = (NextProcessor.NextInner[])SUBSCRIBERS.getAndSet(this, TERMINATED);
         if (value == null) {
            for(NextProcessor.NextInner<O> as : array) {
               as.onComplete();
            }
         } else {
            if (s != null && !(parent instanceof Mono)) {
               s.cancel();
            }

            for(NextProcessor.NextInner<O> as : array) {
               as.complete(value);
            }
         }

         return Sinks.EmitResult.OK;
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.PARENT ? this.subscription : super.scanUnsafe(key);
   }

   @Override
   public Context currentContext() {
      return Operators.multiSubscribersContext(this.subscribers);
   }

   @Override
   public long downstreamCount() {
      return (long)this.subscribers.length;
   }

   @Override
   public void dispose() {
      Subscription s = (Subscription)UPSTREAM.getAndSet(this, Operators.cancelledSubscription());
      if (s != Operators.cancelledSubscription()) {
         this.source = null;
         if (s != null) {
            s.cancel();
         }

         NextProcessor.NextInner<O>[] a;
         if ((a = (NextProcessor.NextInner[])SUBSCRIBERS.getAndSet(this, TERMINATED)) != TERMINATED) {
            Exception e = new CancellationException("Disposed");
            this.error = e;
            this.value = null;

            for(NextProcessor.NextInner<O> as : a) {
               as.onError(e);
            }
         }

      }
   }

   @Override
   public void cancel() {
      if (!this.isTerminated()) {
         Subscription s = (Subscription)UPSTREAM.getAndSet(this, Operators.cancelledSubscription());
         if (s != Operators.cancelledSubscription()) {
            this.source = null;
            if (s != null) {
               s.cancel();
            }

         }
      }
   }

   @Override
   public final void onSubscribe(Subscription subscription) {
      if (Operators.setOnce(UPSTREAM, this, subscription)) {
         subscription.request(Long.MAX_VALUE);
      }

   }

   @Override
   public boolean isCancelled() {
      return this.subscription == Operators.cancelledSubscription() && !this.isTerminated();
   }

   @Override
   public boolean isTerminated() {
      return this.subscribers == TERMINATED;
   }

   @Nullable
   @Override
   public Throwable getError() {
      return this.error;
   }

   boolean add(NextProcessor.NextInner<O> ps) {
      NextProcessor.NextInner<O>[] a;
      NextProcessor.NextInner<O>[] b;
      do {
         a = this.subscribers;
         if (a == TERMINATED) {
            return false;
         }

         int n = a.length;
         b = new NextProcessor.NextInner[n + 1];
         System.arraycopy(a, 0, b, 0, n);
         b[n] = ps;
      } while(!SUBSCRIBERS.compareAndSet(this, a, b));

      Publisher<? extends O> parent = this.source;
      if (parent != null && a == EMPTY_WITH_SOURCE) {
         parent.subscribe(this);
      }

      return true;
   }

   void remove(NextProcessor.NextInner<O> ps) {
      NextProcessor.NextInner<O>[] a;
      boolean disconnect;
      NextProcessor.NextInner<O>[] b;
      do {
         a = this.subscribers;
         int n = a.length;
         if (n == 0) {
            return;
         }

         int j = -1;

         for(int i = 0; i < n; ++i) {
            if (a[i] == ps) {
               j = i;
               break;
            }
         }

         if (j < 0) {
            return;
         }

         disconnect = false;
         if (n == 1) {
            if (this.isRefCounted && this.source != null) {
               b = EMPTY_WITH_SOURCE;
               disconnect = true;
            } else {
               b = EMPTY;
            }
         } else {
            b = new NextProcessor.NextInner[n - 1];
            System.arraycopy(a, 0, b, 0, j);
            System.arraycopy(a, j + 1, b, j, n - j - 1);
         }
      } while(!SUBSCRIBERS.compareAndSet(this, a, b));

      if (disconnect) {
         Subscription oldSubscription = (Subscription)UPSTREAM.getAndSet(this, null);
         if (oldSubscription != null) {
            oldSubscription.cancel();
         }
      }

   }

   @Override
   public void subscribe(CoreSubscriber<? super O> actual) {
      NextProcessor.NextInner<O> as = new NextProcessor.NextInner<>(actual, this);
      actual.onSubscribe(as);
      if (this.add(as)) {
         if (as.isCancelled()) {
            this.remove(as);
         }
      } else {
         Throwable ex = this.error;
         if (ex != null) {
            actual.onError(ex);
         } else {
            O v = this.value;
            if (v != null) {
               as.complete(v);
            } else {
               as.onComplete();
            }
         }
      }

   }

   @Override
   public Stream<? extends Scannable> inners() {
      return Stream.of(this.subscribers);
   }

   void connect() {
      Publisher<? extends O> parent = this.source;
      if (parent != null && SUBSCRIBERS.compareAndSet(this, EMPTY_WITH_SOURCE, EMPTY)) {
         parent.subscribe(this);
      }

   }

   static final class NextInner<T> extends Operators.MonoSubscriber<T, T> {
      final NextProcessor<T> parent;

      NextInner(CoreSubscriber<? super T> actual, NextProcessor<T> parent) {
         super(actual);
         this.parent = parent;
      }

      @Override
      public void cancel() {
         if (STATE.getAndSet(this, 4) != 4) {
            this.parent.remove(this);
         }

      }

      @Override
      public void onComplete() {
         if (!this.isCancelled()) {
            this.actual.onComplete();
         }

      }

      @Override
      public void onError(Throwable t) {
         if (!this.isCancelled()) {
            this.actual.onError(t);
         }

      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.parent;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         }
      }
   }
}
