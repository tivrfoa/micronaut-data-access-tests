package reactor.core.publisher;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.core.scheduler.Schedulers;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class BlockingOptionalMonoSubscriber<T> extends CountDownLatch implements InnerConsumer<T>, Disposable {
   T value;
   Throwable error;
   Subscription s;
   volatile boolean cancelled;

   BlockingOptionalMonoSubscriber() {
      super(1);
   }

   @Override
   public void onNext(T t) {
      if (this.value == null) {
         this.value = t;
         this.countDown();
      }

   }

   @Override
   public void onError(Throwable t) {
      if (this.value == null) {
         this.error = t;
      }

      this.countDown();
   }

   @Override
   public final void onSubscribe(Subscription s) {
      this.s = s;
      if (!this.cancelled) {
         s.request(Long.MAX_VALUE);
      }

   }

   @Override
   public final void onComplete() {
      this.countDown();
   }

   @Override
   public Context currentContext() {
      return Context.empty();
   }

   @Override
   public final void dispose() {
      this.cancelled = true;
      Subscription s = this.s;
      if (s != null) {
         this.s = null;
         s.cancel();
      }

   }

   final Optional<T> blockingGet() {
      if (Schedulers.isInNonBlockingThread()) {
         throw new IllegalStateException("blockOptional() is blocking, which is not supported in thread " + Thread.currentThread().getName());
      } else {
         if (this.getCount() != 0L) {
            try {
               this.await();
            } catch (InterruptedException var3) {
               this.dispose();
               RuntimeException re = Exceptions.propagate(var3);
               re.addSuppressed(new Exception("#blockOptional() has been interrupted"));
               throw re;
            }
         }

         Throwable e = this.error;
         if (e != null) {
            RuntimeException re = Exceptions.propagate(e);
            re.addSuppressed(new Exception("#block terminated with an error"));
            throw re;
         } else {
            return Optional.ofNullable(this.value);
         }
      }
   }

   final Optional<T> blockingGet(long timeout, TimeUnit unit) {
      if (Schedulers.isInNonBlockingThread()) {
         throw new IllegalStateException("blockOptional() is blocking, which is not supported in thread " + Thread.currentThread().getName());
      } else {
         if (this.getCount() != 0L) {
            try {
               if (!this.await(timeout, unit)) {
                  this.dispose();
                  throw new IllegalStateException("Timeout on blocking read for " + timeout + " " + unit);
               }
            } catch (InterruptedException var6) {
               this.dispose();
               RuntimeException re = Exceptions.propagate(var6);
               re.addSuppressed(new Exception("#blockOptional(timeout) has been interrupted"));
               throw re;
            }
         }

         Throwable e = this.error;
         if (e != null) {
            RuntimeException re = Exceptions.propagate(e);
            re.addSuppressed(new Exception("#block terminated with an error"));
            throw re;
         } else {
            return Optional.ofNullable(this.value);
         }
      }
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.TERMINATED) {
         return this.getCount() == 0L;
      } else if (key == Scannable.Attr.PARENT) {
         return this.s;
      } else if (key == Scannable.Attr.CANCELLED) {
         return this.cancelled;
      } else if (key == Scannable.Attr.ERROR) {
         return this.error;
      } else if (key == Scannable.Attr.PREFETCH) {
         return Integer.MAX_VALUE;
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   @Override
   public boolean isDisposed() {
      return this.cancelled || this.getCount() == 0L;
   }

   @Override
   public String stepName() {
      return "blockOptional";
   }
}
