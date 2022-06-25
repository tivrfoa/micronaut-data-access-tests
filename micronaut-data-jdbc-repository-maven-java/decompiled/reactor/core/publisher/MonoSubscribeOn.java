package reactor.core.publisher;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.Nullable;

final class MonoSubscribeOn<T> extends InternalMonoOperator<T, T> {
   final Scheduler scheduler;

   MonoSubscribeOn(Mono<? extends T> source, Scheduler scheduler) {
      super(source);
      this.scheduler = scheduler;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      Scheduler.Worker worker = this.scheduler.createWorker();
      MonoSubscribeOn.SubscribeOnSubscriber<T> parent = new MonoSubscribeOn.SubscribeOnSubscriber<>(this.source, actual, worker);
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
      final Publisher<? extends T> parent;
      final Scheduler.Worker worker;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<MonoSubscribeOn.SubscribeOnSubscriber, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         MonoSubscribeOn.SubscribeOnSubscriber.class, Subscription.class, "s"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<MonoSubscribeOn.SubscribeOnSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         MonoSubscribeOn.SubscribeOnSubscriber.class, "requested"
      );
      volatile Thread thread;
      static final AtomicReferenceFieldUpdater<MonoSubscribeOn.SubscribeOnSubscriber, Thread> THREAD = AtomicReferenceFieldUpdater.newUpdater(
         MonoSubscribeOn.SubscribeOnSubscriber.class, Thread.class, "thread"
      );

      SubscribeOnSubscriber(Publisher<? extends T> parent, CoreSubscriber<? super T> actual, Scheduler.Worker worker) {
         this.actual = actual;
         this.parent = parent;
         this.worker = worker;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.s == Operators.cancelledSubscription();
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.RUN_ON) {
            return this.worker;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.ASYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      public void run() {
         THREAD.lazySet(this, Thread.currentThread());
         this.parent.subscribe(this);
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            long r = REQUESTED.getAndSet(this, 0L);
            if (r != 0L) {
               this.trySchedule(r, s);
            }
         }

      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
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
            THREAD.lazySet(this, null);
         }

      }

      @Override
      public void onComplete() {
         this.actual.onComplete();
         this.worker.dispose();
         THREAD.lazySet(this, null);
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Subscription a = this.s;
            if (a != null) {
               this.trySchedule(n, a);
            } else {
               Operators.addCap(REQUESTED, this, n);
               a = this.s;
               if (a != null) {
                  long r = REQUESTED.getAndSet(this, 0L);
                  if (r != 0L) {
                     this.trySchedule(n, a);
                  }
               }
            }
         }

      }

      void trySchedule(long n, Subscription s) {
         if (Thread.currentThread() == THREAD.get(this)) {
            s.request(n);
         } else {
            try {
               this.worker.schedule(() -> s.request(n));
            } catch (RejectedExecutionException var5) {
               if (!this.worker.isDisposed()) {
                  this.actual.onError(Operators.onRejectedExecution(var5, this, null, null, this.actual.currentContext()));
               }
            }
         }

      }

      @Override
      public void cancel() {
         Operators.terminate(S, this);
         this.worker.dispose();
      }
   }
}
