package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.Consumer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxOnBackpressureDrop<T> extends InternalFluxOperator<T, T> {
   static final Consumer<Object> NOOP = t -> {
   };
   final Consumer<? super T> onDrop;

   FluxOnBackpressureDrop(Flux<? extends T> source) {
      super(source);
      this.onDrop = NOOP;
   }

   FluxOnBackpressureDrop(Flux<? extends T> source, Consumer<? super T> onDrop) {
      super(source);
      this.onDrop = (Consumer)Objects.requireNonNull(onDrop, "onDrop");
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxOnBackpressureDrop.DropSubscriber<>(actual, this.onDrop);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class DropSubscriber<T> implements InnerOperator<T, T> {
      final CoreSubscriber<? super T> actual;
      final Context ctx;
      final Consumer<? super T> onDrop;
      Subscription s;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxOnBackpressureDrop.DropSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxOnBackpressureDrop.DropSubscriber.class, "requested"
      );
      boolean done;

      DropSubscriber(CoreSubscriber<? super T> actual, Consumer<? super T> onDrop) {
         this.actual = actual;
         this.ctx = actual.currentContext();
         this.onDrop = onDrop;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
         }

      }

      @Override
      public void cancel() {
         this.s.cancel();
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(T t) {
         if (this.done) {
            try {
               this.onDrop.accept(t);
            } catch (Throwable var5) {
               Operators.onErrorDropped(var5, this.ctx);
            }

            Operators.onDiscard(t, this.ctx);
         } else {
            long r = this.requested;
            if (r != 0L) {
               this.actual.onNext(t);
               if (r != Long.MAX_VALUE) {
                  Operators.produced(REQUESTED, this, 1L);
               }
            } else {
               try {
                  this.onDrop.accept(t);
               } catch (Throwable var6) {
                  this.onError(Operators.onOperatorError(this.s, var6, t, this.ctx));
               }

               Operators.onDiscard(t, this.ctx);
            }

         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.actual.onComplete();
         }
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.PREFETCH) {
            return Integer.MAX_VALUE;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }
   }
}
