package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxConcatMapNoPrefetch<T, R> extends InternalFluxOperator<T, R> {
   final Function<? super T, ? extends Publisher<? extends R>> mapper;
   final FluxConcatMap.ErrorMode errorMode;

   FluxConcatMapNoPrefetch(Flux<? extends T> source, Function<? super T, ? extends Publisher<? extends R>> mapper, FluxConcatMap.ErrorMode errorMode) {
      super(source);
      this.mapper = (Function)Objects.requireNonNull(mapper, "mapper");
      this.errorMode = errorMode;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      return FluxFlatMap.trySubscribeScalarMap(this.source, actual, this.mapper, false, true)
         ? null
         : new FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber<>(actual, this.mapper, this.errorMode);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   @Override
   public int getPrefetch() {
      return 0;
   }

   static final class FluxConcatMapNoPrefetchSubscriber<T, R> implements FluxConcatMap.FluxConcatMapSupport<T, R> {
      volatile FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State state;
      static final AtomicReferenceFieldUpdater<FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber, FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State> STATE = AtomicReferenceFieldUpdater.newUpdater(
         FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.class, FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State.class, "state"
      );
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.class, Throwable.class, "error"
      );
      final CoreSubscriber<? super R> actual;
      final FluxConcatMap.ConcatMapInner<R> inner;
      final Function<? super T, ? extends Publisher<? extends R>> mapper;
      final FluxConcatMap.ErrorMode errorMode;
      Subscription upstream;

      FluxConcatMapNoPrefetchSubscriber(
         CoreSubscriber<? super R> actual, Function<? super T, ? extends Publisher<? extends R>> mapper, FluxConcatMap.ErrorMode errorMode
      ) {
         this.actual = actual;
         this.mapper = mapper;
         this.errorMode = errorMode;
         this.inner = new FluxConcatMap.ConcatMapInner<>(this);
         STATE.lazySet(this, FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State.INITIAL);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.upstream;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.state == FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State.TERMINATED;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.state == FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State.CANCELLED;
         } else if (key == Scannable.Attr.DELAY_ERROR) {
            return this.errorMode != FluxConcatMap.ErrorMode.IMMEDIATE;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : FluxConcatMap.FluxConcatMapSupport.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super R> actual() {
         return this.actual;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.upstream, s)) {
            this.upstream = s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         if (!STATE.compareAndSet(
            this,
            FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State.REQUESTED,
            FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State.ACTIVE
         )) {
            switch(this.state) {
               case CANCELLED:
                  Operators.onDiscard(t, this.currentContext());
                  break;
               case TERMINATED:
                  Operators.onNextDropped(t, this.currentContext());
            }

         } else {
            try {
               Publisher<? extends R> p = (Publisher)this.mapper.apply(t);
               Objects.requireNonNull(p, "The mapper returned a null Publisher");
               if (p instanceof Callable) {
                  Callable<R> callable = (Callable)p;
                  R result = (R)callable.call();
                  if (result == null) {
                     this.innerComplete();
                     return;
                  }

                  if (this.inner.isUnbounded()) {
                     this.actual.onNext(result);
                     this.innerComplete();
                     return;
                  }

                  this.inner.set(new FluxConcatMap.WeakScalarSubscription<>(result, this.inner));
                  return;
               }

               p.subscribe(this.inner);
            } catch (Throwable var5) {
               Context ctx = this.actual.currentContext();
               Operators.onDiscard(t, ctx);
               if (!this.maybeOnError(Operators.onNextError(t, var5, ctx), ctx, this.upstream)) {
                  this.innerComplete();
               }
            }

         }
      }

      @Override
      public void onError(Throwable t) {
         Context ctx = this.currentContext();
         if (!this.maybeOnError(t, ctx, this.inner)) {
            this.onComplete();
         }

      }

      @Override
      public void onComplete() {
         FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State previousState = this.state;

         while(true) {
            switch(previousState) {
               case INITIAL:
               case REQUESTED:
                  if (STATE.compareAndSet(this, previousState, FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State.TERMINATED)) {
                     Throwable ex = this.error;
                     if (ex != null) {
                        this.actual.onError(ex);
                        return;
                     }

                     this.actual.onComplete();
                     return;
                  }
                  break;
               case ACTIVE:
                  if (STATE.compareAndSet(this, previousState, FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State.LAST_ACTIVE)) {
                     return;
                  }
                  break;
               default:
                  return;
            }

            previousState = this.state;
         }
      }

      @Override
      public synchronized void innerNext(R value) {
         switch(this.state) {
            case ACTIVE:
            case LAST_ACTIVE:
               this.actual.onNext(value);
               break;
            default:
               Operators.onDiscard(value, this.currentContext());
         }

      }

      @Override
      public void innerComplete() {
         FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State previousState = this.state;

         while(true) {
            switch(previousState) {
               case ACTIVE:
                  if (STATE.compareAndSet(this, previousState, FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State.REQUESTED)) {
                     this.upstream.request(1L);
                     return;
                  }
                  break;
               case LAST_ACTIVE:
                  if (STATE.compareAndSet(this, previousState, FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State.TERMINATED)) {
                     Throwable ex = this.error;
                     if (ex != null) {
                        this.actual.onError(ex);
                        return;
                     }

                     this.actual.onComplete();
                     return;
                  }
                  break;
               default:
                  return;
            }

            previousState = this.state;
         }
      }

      @Override
      public void innerError(Throwable e) {
         Context ctx = this.currentContext();
         if (!this.maybeOnError(Operators.onNextInnerError(e, ctx, null), ctx, this.upstream)) {
            this.innerComplete();
         }

      }

      private boolean maybeOnError(@Nullable Throwable e, Context ctx, Subscription subscriptionToCancel) {
         if (e == null) {
            return false;
         } else {
            if (!ERROR.compareAndSet(this, null, e)) {
               Operators.onErrorDropped(e, ctx);
            }

            if (this.errorMode == FluxConcatMap.ErrorMode.END) {
               return false;
            } else {
               FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State previousState = this.state;

               while(true) {
                  switch(previousState) {
                     case CANCELLED:
                     case TERMINATED:
                        return true;
                     default:
                        if (STATE.compareAndSet(this, previousState, FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State.TERMINATED)) {
                           subscriptionToCancel.cancel();
                           synchronized(this) {
                              this.actual.onError(this.error);
                              return true;
                           }
                        }

                        previousState = this.state;
                  }
               }
            }
         }
      }

      @Override
      public void request(long n) {
         if (STATE.compareAndSet(
            this,
            FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State.INITIAL,
            FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State.REQUESTED
         )) {
            this.upstream.request(1L);
         }

         this.inner.request(n);
      }

      @Override
      public void cancel() {
         switch((FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State)STATE.getAndSet(
            this, FluxConcatMapNoPrefetch.FluxConcatMapNoPrefetchSubscriber.State.CANCELLED
         )) {
            case CANCELLED:
               break;
            case TERMINATED:
               this.inner.cancel();
               break;
            default:
               this.inner.cancel();
               this.upstream.cancel();
         }

      }

      static enum State {
         INITIAL,
         REQUESTED,
         ACTIVE,
         LAST_ACTIVE,
         TERMINATED,
         CANCELLED;
      }
   }
}
