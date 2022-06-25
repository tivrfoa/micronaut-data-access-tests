package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.reactivestreams.Subscription;
import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.Nullable;

final class FluxSubscribeOn<T> extends InternalFluxOperator<T, T> {
   final Scheduler scheduler;
   final boolean requestOnSeparateThread;

   FluxSubscribeOn(Flux<? extends T> source, Scheduler scheduler, boolean requestOnSeparateThread) {
      super(source);
      this.scheduler = (Scheduler)Objects.requireNonNull(scheduler, "scheduler");
      this.requestOnSeparateThread = requestOnSeparateThread;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      Scheduler.Worker worker = (Scheduler.Worker)Objects.requireNonNull(this.scheduler.createWorker(), "The scheduler returned a null Function");
      FluxSubscribeOn.SubscribeOnSubscriber<T> parent = new FluxSubscribeOn.SubscribeOnSubscriber<>(this.source, actual, worker, this.requestOnSeparateThread);
      actual.onSubscribe(parent);

      try {
         worker.schedule(parent);
      } catch (RejectedExecutionException var5) {
         if (parent.s != Operators.cancelledSubscription()) {
            actual.onError(Operators.onRejectedExecution(var5, parent, null, null, actual.currentContext()));
         }
      }

      return null;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.RUN_ON) {
         return this.scheduler;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : super.scanUnsafe(key);
      }
   }

   static final class SubscribeOnSubscriber<T> implements InnerOperator<T, T>, Runnable {
      final CoreSubscriber<? super T> actual;
      final CorePublisher<? extends T> source;
      final Scheduler.Worker worker;
      final boolean requestOnSeparateThread;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<FluxSubscribeOn.SubscribeOnSubscriber, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         FluxSubscribeOn.SubscribeOnSubscriber.class, Subscription.class, "s"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxSubscribeOn.SubscribeOnSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxSubscribeOn.SubscribeOnSubscriber.class, "requested"
      );
      volatile Thread thread;
      static final AtomicReferenceFieldUpdater<FluxSubscribeOn.SubscribeOnSubscriber, Thread> THREAD = AtomicReferenceFieldUpdater.newUpdater(
         FluxSubscribeOn.SubscribeOnSubscriber.class, Thread.class, "thread"
      );

      SubscribeOnSubscriber(CorePublisher<? extends T> source, CoreSubscriber<? super T> actual, Scheduler.Worker worker, boolean requestOnSeparateThread) {
         this.actual = actual;
         this.worker = worker;
         this.source = source;
         this.requestOnSeparateThread = requestOnSeparateThread;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            long r = REQUESTED.getAndSet(this, 0L);
            if (r != 0L) {
               this.requestUpstream(r, s);
            }
         }

      }

      void requestUpstream(long n, Subscription s) {
         if (this.requestOnSeparateThread && Thread.currentThread() != THREAD.get(this)) {
            try {
               this.worker.schedule(() -> s.request(n));
            } catch (RejectedExecutionException var5) {
               if (!this.worker.isDisposed()) {
                  throw Operators.onRejectedExecution(var5, this, null, null, this.actual.currentContext());
               }
            }
         } else {
            s.request(n);
         }

      }

      @Override
      public void onNext(T t) {
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         try {
            this.actual.onError(t);
         } finally {
            this.worker.dispose();
         }

      }

      @Override
      public void onComplete() {
         this.actual.onComplete();
         this.worker.dispose();
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Subscription s = (Subscription)S.get(this);
            if (s != null) {
               this.requestUpstream(n, s);
            } else {
               Operators.addCap(REQUESTED, this, n);
               s = (Subscription)S.get(this);
               if (s != null) {
                  long r = REQUESTED.getAndSet(this, 0L);
                  if (r != 0L) {
                     this.requestUpstream(r, s);
                  }
               }
            }
         }

      }

      public void run() {
         THREAD.lazySet(this, Thread.currentThread());
         this.source.subscribe(this);
      }

      @Override
      public void cancel() {
         Subscription a = this.s;
         if (a != Operators.cancelledSubscription()) {
            a = (Subscription)S.getAndSet(this, Operators.cancelledSubscription());
            if (a != null && a != Operators.cancelledSubscription()) {
               a.cancel();
            }
         }

         this.worker.dispose();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.s == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.RUN_ON) {
            return this.worker;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }
   }
}
