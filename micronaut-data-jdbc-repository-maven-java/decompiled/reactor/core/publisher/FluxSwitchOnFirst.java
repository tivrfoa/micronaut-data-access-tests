package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.BiFunction;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class FluxSwitchOnFirst<T, R> extends InternalFluxOperator<T, R> {
   final BiFunction<Signal<? extends T>, Flux<T>, Publisher<? extends R>> transformer;
   final boolean cancelSourceOnComplete;
   static final int HAS_FIRST_VALUE_RECEIVED_FLAG = 1;
   static final int HAS_INBOUND_SUBSCRIBED_ONCE_FLAG = 2;
   static final int HAS_INBOUND_SUBSCRIBER_SET_FLAG = 4;
   static final int HAS_INBOUND_REQUESTED_ONCE_FLAG = 8;
   static final int HAS_FIRST_VALUE_SENT_FLAG = 16;
   static final int HAS_INBOUND_CANCELLED_FLAG = 32;
   static final int HAS_INBOUND_CLOSED_PREMATURELY_FLAG = 64;
   static final int HAS_INBOUND_TERMINATED_FLAG = 128;
   static final int HAS_OUTBOUND_SUBSCRIBED_FLAG = 256;
   static final int HAS_OUTBOUND_CANCELLED_FLAG = 512;
   static final int HAS_OUTBOUND_TERMINATED_FLAG = 1024;

   FluxSwitchOnFirst(Flux<? extends T> source, BiFunction<Signal<? extends T>, Flux<T>, Publisher<? extends R>> transformer, boolean cancelSourceOnComplete) {
      super(source);
      this.transformer = (BiFunction)Objects.requireNonNull(transformer, "transformer");
      this.cancelSourceOnComplete = cancelSourceOnComplete;
   }

   @Override
   public int getPrefetch() {
      return 1;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      return (CoreSubscriber<? super T>)(actual instanceof Fuseable.ConditionalSubscriber
         ? new FluxSwitchOnFirst.SwitchOnFirstConditionalMain<>(
            (Fuseable.ConditionalSubscriber<? super R>)actual, this.transformer, this.cancelSourceOnComplete
         )
         : new FluxSwitchOnFirst.SwitchOnFirstMain<>(actual, this.transformer, this.cancelSourceOnComplete));
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static <T, R> long markFirstValueReceived(FluxSwitchOnFirst.AbstractSwitchOnFirstMain<T, R> instance) {
      while(true) {
         int state = instance.state;
         if (!hasInboundCancelled((long)state) && !hasInboundClosedPrematurely((long)state)) {
            if (!FluxSwitchOnFirst.AbstractSwitchOnFirstMain.STATE.compareAndSet(instance, state, state | 1)) {
               continue;
            }

            return (long)state;
         }

         return (long)state;
      }
   }

   static <T, R> long markInboundSubscribedOnce(FluxSwitchOnFirst.AbstractSwitchOnFirstMain<T, R> instance) {
      int state;
      do {
         state = instance.state;
         if (hasInboundSubscribedOnce((long)state)) {
            return (long)state;
         }
      } while(!FluxSwitchOnFirst.AbstractSwitchOnFirstMain.STATE.compareAndSet(instance, state, state | 2));

      return (long)state;
   }

   static <T, R> long markInboundSubscriberSet(FluxSwitchOnFirst.AbstractSwitchOnFirstMain<T, R> instance) {
      while(true) {
         int state = instance.state;
         if (!hasInboundCancelled((long)state) && !hasInboundClosedPrematurely((long)state)) {
            if (!FluxSwitchOnFirst.AbstractSwitchOnFirstMain.STATE.compareAndSet(instance, state, state | 4)) {
               continue;
            }

            return (long)state;
         }

         return (long)state;
      }
   }

   static <T, R> long markInboundRequestedOnce(FluxSwitchOnFirst.AbstractSwitchOnFirstMain<T, R> instance) {
      while(true) {
         int state = instance.state;
         if (!hasInboundCancelled((long)state) && !hasInboundClosedPrematurely((long)state)) {
            if (!FluxSwitchOnFirst.AbstractSwitchOnFirstMain.STATE.compareAndSet(instance, state, state | 8)) {
               continue;
            }

            return (long)state;
         }

         return (long)state;
      }
   }

   static <T, R> long markFirstValueSent(FluxSwitchOnFirst.AbstractSwitchOnFirstMain<T, R> instance) {
      while(true) {
         int state = instance.state;
         if (!hasInboundCancelled((long)state) && !hasInboundClosedPrematurely((long)state)) {
            if (!FluxSwitchOnFirst.AbstractSwitchOnFirstMain.STATE.compareAndSet(instance, state, state | 16)) {
               continue;
            }

            return (long)state;
         }

         return (long)state;
      }
   }

   static <T, R> long markInboundTerminated(FluxSwitchOnFirst.AbstractSwitchOnFirstMain<T, R> instance) {
      while(true) {
         int state = instance.state;
         if (!hasInboundCancelled((long)state) && !hasInboundClosedPrematurely((long)state)) {
            if (!FluxSwitchOnFirst.AbstractSwitchOnFirstMain.STATE.compareAndSet(instance, state, state | 128)) {
               continue;
            }

            return (long)state;
         }

         return (long)state;
      }
   }

   static <T, R> long markInboundCancelled(FluxSwitchOnFirst.AbstractSwitchOnFirstMain<T, R> instance) {
      int state;
      do {
         state = instance.state;
         if (hasInboundCancelled((long)state)) {
            return (long)state;
         }
      } while(!FluxSwitchOnFirst.AbstractSwitchOnFirstMain.STATE.compareAndSet(instance, state, state | 32));

      return (long)state;
   }

   static <T, R> long markInboundClosedPrematurely(FluxSwitchOnFirst.AbstractSwitchOnFirstMain<T, R> instance) {
      while(true) {
         int state = instance.state;
         if (!hasInboundTerminated((long)state) && !hasInboundCancelled((long)state)) {
            if (!FluxSwitchOnFirst.AbstractSwitchOnFirstMain.STATE.compareAndSet(instance, state, state | 64)) {
               continue;
            }

            return (long)state;
         }

         return (long)state;
      }
   }

   static <T, R> long markInboundCancelledAndOutboundTerminated(FluxSwitchOnFirst.AbstractSwitchOnFirstMain<T, R> instance) {
      while(true) {
         int state = instance.state;
         if (!hasInboundCancelled((long)state) && !hasOutboundCancelled((long)state)) {
            if (!FluxSwitchOnFirst.AbstractSwitchOnFirstMain.STATE.compareAndSet(instance, state, state | 32 | 1024)) {
               continue;
            }

            return (long)state;
         }

         return (long)state;
      }
   }

   static <T, R> long markOutboundSubscribed(FluxSwitchOnFirst.AbstractSwitchOnFirstMain<T, R> instance) {
      int state;
      do {
         state = instance.state;
         if (hasOutboundCancelled((long)state)) {
            return (long)state;
         }
      } while(!FluxSwitchOnFirst.AbstractSwitchOnFirstMain.STATE.compareAndSet(instance, state, state | 256));

      return (long)state;
   }

   static <T, R> long markOutboundTerminated(FluxSwitchOnFirst.AbstractSwitchOnFirstMain<T, R> instance) {
      while(true) {
         int state = instance.state;
         if (!hasOutboundCancelled((long)state) && !hasOutboundTerminated((long)state)) {
            if (!FluxSwitchOnFirst.AbstractSwitchOnFirstMain.STATE.compareAndSet(instance, state, state | 1024)) {
               continue;
            }

            return (long)state;
         }

         return (long)state;
      }
   }

   static <T, R> long markOutboundCancelled(FluxSwitchOnFirst.AbstractSwitchOnFirstMain<T, R> instance) {
      while(true) {
         int state = instance.state;
         if (!hasOutboundTerminated((long)state) && !hasOutboundCancelled((long)state)) {
            if (!FluxSwitchOnFirst.AbstractSwitchOnFirstMain.STATE.compareAndSet(instance, state, state | 512)) {
               continue;
            }

            return (long)state;
         }

         return (long)state;
      }
   }

   static boolean hasInboundCancelled(long state) {
      return (state & 32L) == 32L;
   }

   static boolean hasInboundClosedPrematurely(long state) {
      return (state & 64L) == 64L;
   }

   static boolean hasInboundTerminated(long state) {
      return (state & 128L) == 128L;
   }

   static boolean hasFirstValueReceived(long state) {
      return (state & 1L) == 1L;
   }

   static boolean hasFirstValueSent(long state) {
      return (state & 16L) == 16L;
   }

   static boolean hasInboundSubscribedOnce(long state) {
      return (state & 2L) == 2L;
   }

   static boolean hasInboundSubscriberSet(long state) {
      return (state & 4L) == 4L;
   }

   static boolean hasInboundRequestedOnce(long state) {
      return (state & 8L) == 8L;
   }

   static boolean hasOutboundSubscribed(long state) {
      return (state & 256L) == 256L;
   }

   static boolean hasOutboundCancelled(long state) {
      return (state & 512L) == 512L;
   }

   static boolean hasOutboundTerminated(long state) {
      return (state & 1024L) == 1024L;
   }

   abstract static class AbstractSwitchOnFirstMain<T, R> extends Flux<T> implements InnerOperator<T, R> {
      final FluxSwitchOnFirst.SwitchOnFirstControlSubscriber<? super R> outboundSubscriber;
      final BiFunction<Signal<? extends T>, Flux<T>, Publisher<? extends R>> transformer;
      Subscription s;
      boolean isInboundRequestedOnce;
      boolean isFirstOnNextReceivedOnce;
      T firstValue;
      Throwable throwable;
      boolean done;
      CoreSubscriber<? super T> inboundSubscriber;
      volatile int state;
      static final AtomicIntegerFieldUpdater<FluxSwitchOnFirst.AbstractSwitchOnFirstMain> STATE = AtomicIntegerFieldUpdater.newUpdater(
         FluxSwitchOnFirst.AbstractSwitchOnFirstMain.class, "state"
      );

      AbstractSwitchOnFirstMain(
         CoreSubscriber<? super R> outboundSubscriber,
         BiFunction<Signal<? extends T>, Flux<T>, Publisher<? extends R>> transformer,
         boolean cancelSourceOnComplete
      ) {
         this.outboundSubscriber = (FluxSwitchOnFirst.SwitchOnFirstControlSubscriber<? super R>)(outboundSubscriber instanceof Fuseable.ConditionalSubscriber
            ? new FluxSwitchOnFirst.SwitchOnFirstConditionalControlSubscriber<>(
               this, (Fuseable.ConditionalSubscriber<? super R>)outboundSubscriber, cancelSourceOnComplete
            )
            : new FluxSwitchOnFirst.SwitchOnFirstControlSubscriber<>(this, outboundSubscriber, cancelSourceOnComplete));
         this.transformer = transformer;
      }

      @Nullable
      @Override
      public final Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return FluxSwitchOnFirst.hasInboundCancelled((long)this.state) || FluxSwitchOnFirst.hasInboundClosedPrematurely((long)this.state);
         } else if (key != Scannable.Attr.TERMINATED) {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         } else {
            return FluxSwitchOnFirst.hasInboundTerminated((long)this.state) || FluxSwitchOnFirst.hasInboundClosedPrematurely((long)this.state);
         }
      }

      @Override
      public final CoreSubscriber<? super R> actual() {
         return this.outboundSubscriber;
      }

      @Override
      public final void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.outboundSubscriber.sendSubscription();
            if (!FluxSwitchOnFirst.hasInboundCancelled((long)this.state)) {
               s.request(1L);
            }
         }

      }

      @Override
      public final void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.currentContext());
         } else if (this.isFirstOnNextReceivedOnce) {
            synchronized(this) {
               this.inboundSubscriber.onNext(t);
            }
         } else {
            this.isFirstOnNextReceivedOnce = true;
            this.firstValue = t;
            long previousState = FluxSwitchOnFirst.markFirstValueReceived(this);
            if (!FluxSwitchOnFirst.hasInboundCancelled(previousState) && !FluxSwitchOnFirst.hasInboundClosedPrematurely(previousState)) {
               FluxSwitchOnFirst.SwitchOnFirstControlSubscriber<? super R> o = this.outboundSubscriber;

               Publisher<? extends R> outboundPublisher;
               try {
                  Signal<T> signal = Signal.next(t, o.currentContext());
                  outboundPublisher = (Publisher)Objects.requireNonNull(this.transformer.apply(signal, this), "The transformer returned a null value");
               } catch (Throwable var9) {
                  this.done = true;
                  previousState = FluxSwitchOnFirst.markInboundCancelledAndOutboundTerminated(this);
                  if (!FluxSwitchOnFirst.hasInboundCancelled(previousState) && !FluxSwitchOnFirst.hasOutboundCancelled(previousState)) {
                     this.firstValue = null;
                     Operators.onDiscard(t, o.currentContext());
                     o.errorDirectly(Operators.onOperatorError(this.s, var9, t, o.currentContext()));
                     return;
                  }

                  Operators.onErrorDropped(var9, o.currentContext());
                  return;
               }

               outboundPublisher.subscribe(o);
            } else {
               this.firstValue = null;
               Operators.onDiscard(t, this.outboundSubscriber.currentContext());
            }
         }
      }

      @Override
      public final void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.outboundSubscriber.currentContext());
         } else {
            this.done = true;
            this.throwable = t;
            long previousState = FluxSwitchOnFirst.markInboundTerminated(this);
            if (FluxSwitchOnFirst.hasInboundCancelled(previousState)
               || FluxSwitchOnFirst.hasInboundTerminated(previousState)
               || FluxSwitchOnFirst.hasInboundClosedPrematurely(previousState)) {
               Operators.onErrorDropped(t, this.outboundSubscriber.currentContext());
            } else if (FluxSwitchOnFirst.hasFirstValueSent(previousState)) {
               synchronized(this) {
                  this.inboundSubscriber.onError(t);
               }
            } else {
               if (!FluxSwitchOnFirst.hasFirstValueReceived(previousState)) {
                  CoreSubscriber<? super R> o = this.outboundSubscriber;

                  Publisher<? extends R> result;
                  try {
                     Signal<T> signal = Signal.error(t, o.currentContext());
                     result = (Publisher)Objects.requireNonNull(this.transformer.apply(signal, this), "The transformer returned a null value");
                  } catch (Throwable var8) {
                     o.onError(Exceptions.addSuppressed(t, var8));
                     return;
                  }

                  result.subscribe(o);
               }

            }
         }
      }

      @Override
      public final void onComplete() {
         if (!this.done) {
            this.done = true;
            long previousState = FluxSwitchOnFirst.markInboundTerminated(this);
            if (!FluxSwitchOnFirst.hasInboundCancelled(previousState)
               && !FluxSwitchOnFirst.hasInboundTerminated(previousState)
               && !FluxSwitchOnFirst.hasInboundClosedPrematurely(previousState)) {
               if (FluxSwitchOnFirst.hasFirstValueSent(previousState)) {
                  synchronized(this) {
                     this.inboundSubscriber.onComplete();
                  }
               } else {
                  if (!FluxSwitchOnFirst.hasFirstValueReceived(previousState)) {
                     CoreSubscriber<? super R> o = this.outboundSubscriber;

                     Publisher<? extends R> result;
                     try {
                        Signal<T> signal = Signal.complete(o.currentContext());
                        result = (Publisher)Objects.requireNonNull(this.transformer.apply(signal, this), "The transformer returned a null value");
                     } catch (Throwable var7) {
                        o.onError(var7);
                        return;
                     }

                     result.subscribe(o);
                  }

               }
            }
         }
      }

      @Override
      public final void cancel() {
         long previousState = FluxSwitchOnFirst.markInboundCancelled(this);
         if (!FluxSwitchOnFirst.hasInboundCancelled(previousState)
            && !FluxSwitchOnFirst.hasInboundTerminated(previousState)
            && !FluxSwitchOnFirst.hasInboundClosedPrematurely(previousState)) {
            this.s.cancel();
            if (FluxSwitchOnFirst.hasFirstValueReceived(previousState) && !FluxSwitchOnFirst.hasInboundRequestedOnce(previousState)) {
               T f = this.firstValue;
               this.firstValue = null;
               Operators.onDiscard(f, this.currentContext());
            }

         }
      }

      final void cancelAndError() {
         long previousState = FluxSwitchOnFirst.markInboundClosedPrematurely(this);
         if (!FluxSwitchOnFirst.hasInboundCancelled(previousState) && !FluxSwitchOnFirst.hasInboundTerminated(previousState)) {
            this.s.cancel();
            if (FluxSwitchOnFirst.hasFirstValueReceived(previousState) && !FluxSwitchOnFirst.hasFirstValueSent(previousState)) {
               if (!FluxSwitchOnFirst.hasInboundRequestedOnce(previousState)) {
                  T f = this.firstValue;
                  this.firstValue = null;
                  Operators.onDiscard(f, this.currentContext());
                  if (FluxSwitchOnFirst.hasInboundSubscriberSet(previousState)) {
                     this.inboundSubscriber.onError(new CancellationException("FluxSwitchOnFirst has already been cancelled"));
                  }
               }

            } else {
               if (FluxSwitchOnFirst.hasInboundSubscriberSet(previousState)) {
                  synchronized(this) {
                     this.inboundSubscriber.onError(new CancellationException("FluxSwitchOnFirst has already been cancelled"));
                  }
               }

            }
         }
      }

      @Override
      public final void request(long n) {
         if (Operators.validate(n)) {
            if (!this.isInboundRequestedOnce) {
               this.isInboundRequestedOnce = true;
               if (this.isFirstOnNextReceivedOnce) {
                  long previousState = FluxSwitchOnFirst.markInboundRequestedOnce(this);
                  if (FluxSwitchOnFirst.hasInboundCancelled(previousState) || FluxSwitchOnFirst.hasInboundClosedPrematurely(previousState)) {
                     return;
                  }

                  T first = this.firstValue;
                  this.firstValue = null;
                  boolean wasDelivered = this.sendFirst(first);
                  if (wasDelivered && n != Long.MAX_VALUE) {
                     if (--n > 0L) {
                        this.s.request(n);
                     }

                     return;
                  }
               }
            }

            this.s.request(n);
         }

      }

      @Override
      public final void subscribe(CoreSubscriber<? super T> inboundSubscriber) {
         long previousState = FluxSwitchOnFirst.markInboundSubscribedOnce(this);
         if (FluxSwitchOnFirst.hasInboundSubscribedOnce(previousState)) {
            Operators.error(inboundSubscriber, new IllegalStateException("FluxSwitchOnFirst allows only one Subscriber"));
         } else if (FluxSwitchOnFirst.hasInboundClosedPrematurely(previousState)) {
            Operators.error(inboundSubscriber, new CancellationException("FluxSwitchOnFirst has already been cancelled"));
         } else if (!FluxSwitchOnFirst.hasFirstValueReceived(previousState)) {
            Throwable t = this.throwable;
            if (t != null) {
               Operators.error(inboundSubscriber, t);
            } else {
               Operators.complete(inboundSubscriber);
            }

         } else {
            this.inboundSubscriber = this.convert(inboundSubscriber);
            inboundSubscriber.onSubscribe(this);
            previousState = FluxSwitchOnFirst.markInboundSubscriberSet(this);
            if (FluxSwitchOnFirst.hasInboundClosedPrematurely(previousState)
               && (!FluxSwitchOnFirst.hasInboundRequestedOnce(previousState) || FluxSwitchOnFirst.hasFirstValueSent(previousState))
               && !FluxSwitchOnFirst.hasInboundCancelled(previousState)) {
               inboundSubscriber.onError(new CancellationException("FluxSwitchOnFirst has already been cancelled"));
            }

         }
      }

      abstract CoreSubscriber<? super T> convert(CoreSubscriber<? super T> var1);

      final boolean sendFirst(T firstValue) {
         CoreSubscriber<? super T> a = this.inboundSubscriber;
         boolean sent = this.tryDirectSend(a, firstValue);
         long previousState = FluxSwitchOnFirst.markFirstValueSent(this);
         if (FluxSwitchOnFirst.hasInboundCancelled(previousState)) {
            return sent;
         } else if (FluxSwitchOnFirst.hasInboundClosedPrematurely(previousState)) {
            a.onError(new CancellationException("FluxSwitchOnFirst has already been cancelled"));
            return sent;
         } else {
            if (FluxSwitchOnFirst.hasInboundTerminated(previousState)) {
               Throwable t = this.throwable;
               if (t != null) {
                  a.onError(t);
               } else {
                  a.onComplete();
               }
            }

            return sent;
         }
      }

      abstract boolean tryDirectSend(CoreSubscriber<? super T> var1, T var2);
   }

   static final class SwitchOnFirstConditionalControlSubscriber<T>
      extends FluxSwitchOnFirst.SwitchOnFirstControlSubscriber<T>
      implements InnerOperator<T, T>,
      Fuseable.ConditionalSubscriber<T> {
      final Fuseable.ConditionalSubscriber<? super T> delegate;

      SwitchOnFirstConditionalControlSubscriber(
         FluxSwitchOnFirst.AbstractSwitchOnFirstMain<?, T> parent, Fuseable.ConditionalSubscriber<? super T> delegate, boolean cancelSourceOnComplete
      ) {
         super(parent, delegate, cancelSourceOnComplete);
         this.delegate = delegate;
      }

      @Override
      public boolean tryOnNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.currentContext());
            return true;
         } else {
            return this.delegate.tryOnNext(t);
         }
      }
   }

   static final class SwitchOnFirstConditionalMain<T, R> extends FluxSwitchOnFirst.AbstractSwitchOnFirstMain<T, R> implements Fuseable.ConditionalSubscriber<T> {
      SwitchOnFirstConditionalMain(
         Fuseable.ConditionalSubscriber<? super R> outer,
         BiFunction<Signal<? extends T>, Flux<T>, Publisher<? extends R>> transformer,
         boolean cancelSourceOnComplete
      ) {
         super(outer, transformer, cancelSourceOnComplete);
      }

      @Override
      CoreSubscriber<? super T> convert(CoreSubscriber<? super T> inboundSubscriber) {
         return Operators.toConditionalSubscriber(inboundSubscriber);
      }

      @Override
      public boolean tryOnNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.currentContext());
            return false;
         } else if (this.isFirstOnNextReceivedOnce) {
            synchronized(this) {
               return ((Fuseable.ConditionalSubscriber)this.inboundSubscriber).tryOnNext(t);
            }
         } else {
            this.isFirstOnNextReceivedOnce = true;
            this.firstValue = t;
            long previousState = FluxSwitchOnFirst.markFirstValueReceived(this);
            if (FluxSwitchOnFirst.hasInboundCancelled(previousState)) {
               this.firstValue = null;
               Operators.onDiscard(t, this.outboundSubscriber.currentContext());
               return true;
            } else {
               FluxSwitchOnFirst.SwitchOnFirstControlSubscriber<? super R> o = this.outboundSubscriber;

               Publisher<? extends R> result;
               try {
                  Signal<T> signal = Signal.next(t, o.currentContext());
                  result = (Publisher)Objects.requireNonNull(this.transformer.apply(signal, this), "The transformer returned a null value");
               } catch (Throwable var9) {
                  this.done = true;
                  previousState = FluxSwitchOnFirst.markInboundCancelledAndOutboundTerminated(this);
                  if (!FluxSwitchOnFirst.hasInboundCancelled(previousState) && !FluxSwitchOnFirst.hasOutboundCancelled(previousState)) {
                     this.firstValue = null;
                     Operators.onDiscard(t, o.currentContext());
                     o.errorDirectly(Operators.onOperatorError(this.s, var9, t, o.currentContext()));
                     return true;
                  }

                  Operators.onErrorDropped(var9, o.currentContext());
                  return true;
               }

               result.subscribe(o);
               return true;
            }
         }
      }

      @Override
      boolean tryDirectSend(CoreSubscriber<? super T> actual, T t) {
         return ((Fuseable.ConditionalSubscriber)actual).tryOnNext(t);
      }
   }

   static class SwitchOnFirstControlSubscriber<T> extends Operators.DeferredSubscription implements InnerOperator<T, T>, CoreSubscriber<T> {
      final FluxSwitchOnFirst.AbstractSwitchOnFirstMain<?, T> parent;
      final CoreSubscriber<? super T> delegate;
      final boolean cancelSourceOnComplete;
      boolean done;

      SwitchOnFirstControlSubscriber(
         FluxSwitchOnFirst.AbstractSwitchOnFirstMain<?, T> parent, CoreSubscriber<? super T> delegate, boolean cancelSourceOnComplete
      ) {
         this.parent = parent;
         this.delegate = delegate;
         this.cancelSourceOnComplete = cancelSourceOnComplete;
      }

      final void sendSubscription() {
         this.delegate.onSubscribe(this);
      }

      @Override
      public final void onSubscribe(Subscription s) {
         if (this.set(s)) {
            long previousState = FluxSwitchOnFirst.markOutboundSubscribed(this.parent);
            if (FluxSwitchOnFirst.hasOutboundCancelled(previousState)) {
               s.cancel();
            }
         }

      }

      @Override
      public final CoreSubscriber<? super T> actual() {
         return this.delegate;
      }

      @Override
      public final void onNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.currentContext());
         } else {
            this.delegate.onNext(t);
         }
      }

      @Override
      public final void onError(Throwable throwable) {
         if (this.done) {
            Operators.onErrorDropped(throwable, this.currentContext());
         } else {
            this.done = true;
            FluxSwitchOnFirst.AbstractSwitchOnFirstMain<?, T> parent = this.parent;
            long previousState = FluxSwitchOnFirst.markOutboundTerminated(parent);
            if (!FluxSwitchOnFirst.hasOutboundCancelled(previousState) && !FluxSwitchOnFirst.hasOutboundTerminated(previousState)) {
               if (!FluxSwitchOnFirst.hasInboundCancelled(previousState) && !FluxSwitchOnFirst.hasInboundTerminated(previousState)) {
                  parent.cancelAndError();
               }

               this.delegate.onError(throwable);
            } else {
               Operators.onErrorDropped(throwable, this.delegate.currentContext());
            }
         }
      }

      final void errorDirectly(Throwable t) {
         this.done = true;
         this.delegate.onError(t);
      }

      @Override
      public final void onComplete() {
         if (!this.done) {
            this.done = true;
            FluxSwitchOnFirst.AbstractSwitchOnFirstMain<?, T> parent = this.parent;
            long previousState = FluxSwitchOnFirst.markOutboundTerminated(parent);
            if (this.cancelSourceOnComplete && !FluxSwitchOnFirst.hasInboundCancelled(previousState) && !FluxSwitchOnFirst.hasInboundTerminated(previousState)) {
               parent.cancelAndError();
            }

            this.delegate.onComplete();
         }
      }

      @Override
      public final void cancel() {
         Operators.DeferredSubscription.REQUESTED.lazySet(this, -2L);
         long previousState = FluxSwitchOnFirst.markOutboundCancelled(this.parent);
         if (!FluxSwitchOnFirst.hasOutboundCancelled(previousState) && !FluxSwitchOnFirst.hasOutboundTerminated(previousState)) {
            boolean shouldCancelInbound = !FluxSwitchOnFirst.hasInboundTerminated(previousState) && !FluxSwitchOnFirst.hasInboundCancelled(previousState);
            if (!FluxSwitchOnFirst.hasOutboundSubscribed(previousState)) {
               if (shouldCancelInbound) {
                  this.parent.cancel();
               }

            } else {
               this.s.cancel();
               if (shouldCancelInbound) {
                  this.parent.cancelAndError();
               }

            }
         }
      }

      @Override
      public final Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.parent;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.delegate;
         } else if (key == Scannable.Attr.RUN_STYLE) {
            return Scannable.Attr.RunStyle.SYNC;
         } else if (key == Scannable.Attr.CANCELLED) {
            return FluxSwitchOnFirst.hasOutboundCancelled((long)this.parent.state);
         } else {
            return key == Scannable.Attr.TERMINATED ? FluxSwitchOnFirst.hasOutboundTerminated((long)this.parent.state) : null;
         }
      }
   }

   static final class SwitchOnFirstMain<T, R> extends FluxSwitchOnFirst.AbstractSwitchOnFirstMain<T, R> {
      SwitchOnFirstMain(
         CoreSubscriber<? super R> outer, BiFunction<Signal<? extends T>, Flux<T>, Publisher<? extends R>> transformer, boolean cancelSourceOnComplete
      ) {
         super(outer, transformer, cancelSourceOnComplete);
      }

      @Override
      CoreSubscriber<? super T> convert(CoreSubscriber<? super T> inboundSubscriber) {
         return inboundSubscriber;
      }

      @Override
      boolean tryDirectSend(CoreSubscriber<? super T> actual, T t) {
         actual.onNext(t);
         return true;
      }
   }
}
