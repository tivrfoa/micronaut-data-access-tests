package reactor.core.publisher;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.concurrent.Queues;

final class FluxJoin<TLeft, TRight, TLeftEnd, TRightEnd, R> extends InternalFluxOperator<TLeft, R> {
   final Publisher<? extends TRight> other;
   final Function<? super TLeft, ? extends Publisher<TLeftEnd>> leftEnd;
   final Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd;
   final BiFunction<? super TLeft, ? super TRight, ? extends R> resultSelector;

   FluxJoin(
      Flux<TLeft> source,
      Publisher<? extends TRight> other,
      Function<? super TLeft, ? extends Publisher<TLeftEnd>> leftEnd,
      Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd,
      BiFunction<? super TLeft, ? super TRight, ? extends R> resultSelector
   ) {
      super(source);
      this.other = (Publisher)Objects.requireNonNull(other, "other");
      this.leftEnd = (Function)Objects.requireNonNull(leftEnd, "leftEnd");
      this.rightEnd = (Function)Objects.requireNonNull(rightEnd, "rightEnd");
      this.resultSelector = (BiFunction)Objects.requireNonNull(resultSelector, "resultSelector");
   }

   @Override
   public CoreSubscriber<? super TLeft> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      FluxJoin.JoinSubscription<TLeft, TRight, TLeftEnd, TRightEnd, R> parent = new FluxJoin.JoinSubscription<>(
         actual, this.leftEnd, this.rightEnd, this.resultSelector
      );
      actual.onSubscribe(parent);
      FluxGroupJoin.LeftRightSubscriber left = new FluxGroupJoin.LeftRightSubscriber(parent, true);
      parent.cancellations.add(left);
      FluxGroupJoin.LeftRightSubscriber right = new FluxGroupJoin.LeftRightSubscriber(parent, false);
      parent.cancellations.add(right);
      this.source.subscribe(left);
      this.other.subscribe(right);
      return null;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class JoinSubscription<TLeft, TRight, TLeftEnd, TRightEnd, R> implements FluxGroupJoin.JoinSupport<R> {
      final Queue<Object> queue;
      final BiPredicate<Object, Object> queueBiOffer;
      final Disposable.Composite cancellations;
      final Map<Integer, TLeft> lefts;
      final Map<Integer, TRight> rights;
      final Function<? super TLeft, ? extends Publisher<TLeftEnd>> leftEnd;
      final Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd;
      final BiFunction<? super TLeft, ? super TRight, ? extends R> resultSelector;
      final CoreSubscriber<? super R> actual;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxJoin.JoinSubscription> WIP = AtomicIntegerFieldUpdater.newUpdater(FluxJoin.JoinSubscription.class, "wip");
      volatile int active;
      static final AtomicIntegerFieldUpdater<FluxJoin.JoinSubscription> ACTIVE = AtomicIntegerFieldUpdater.newUpdater(FluxJoin.JoinSubscription.class, "active");
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxJoin.JoinSubscription> REQUESTED = AtomicLongFieldUpdater.newUpdater(FluxJoin.JoinSubscription.class, "requested");
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxJoin.JoinSubscription, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxJoin.JoinSubscription.class, Throwable.class, "error"
      );
      int leftIndex;
      int rightIndex;
      static final Integer LEFT_VALUE = 1;
      static final Integer RIGHT_VALUE = 2;
      static final Integer LEFT_CLOSE = 3;
      static final Integer RIGHT_CLOSE = 4;

      JoinSubscription(
         CoreSubscriber<? super R> actual,
         Function<? super TLeft, ? extends Publisher<TLeftEnd>> leftEnd,
         Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd,
         BiFunction<? super TLeft, ? super TRight, ? extends R> resultSelector
      ) {
         this.actual = actual;
         this.cancellations = Disposables.composite();
         this.queue = (Queue)Queues.unboundedMultiproducer().get();
         this.queueBiOffer = (BiPredicate)this.queue;
         this.lefts = new LinkedHashMap();
         this.rights = new LinkedHashMap();
         this.leftEnd = leftEnd;
         this.rightEnd = rightEnd;
         this.resultSelector = resultSelector;
         ACTIVE.lazySet(this, 2);
      }

      @Override
      public final CoreSubscriber<? super R> actual() {
         return this.actual;
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Scannable.from(this.cancellations).inners();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.cancellations.isDisposed();
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.queue.size() / 2;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.active == 0;
         } else if (key == Scannable.Attr.ERROR) {
            return this.error;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : FluxGroupJoin.JoinSupport.super.scanUnsafe(key);
         }
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            Operators.addCap(REQUESTED, this, n);
         }

      }

      @Override
      public void cancel() {
         if (!this.cancellations.isDisposed()) {
            this.cancellations.dispose();
            if (WIP.getAndIncrement(this) == 0) {
               this.queue.clear();
            }

         }
      }

      void errorAll(Subscriber<?> a) {
         Throwable ex = Exceptions.terminate(ERROR, this);
         this.lefts.clear();
         this.rights.clear();
         a.onError(ex);
      }

      void drain() {
         if (WIP.getAndIncrement(this) == 0) {
            int missed = 1;
            Queue<Object> q = this.queue;
            Subscriber<? super R> a = this.actual;

            while(!this.cancellations.isDisposed()) {
               Throwable ex = this.error;
               if (ex != null) {
                  q.clear();
                  this.cancellations.dispose();
                  this.errorAll(a);
                  return;
               }

               boolean d = this.active == 0;
               Integer mode = (Integer)q.poll();
               boolean empty = mode == null;
               if (d && empty) {
                  this.lefts.clear();
                  this.rights.clear();
                  this.cancellations.dispose();
                  a.onComplete();
                  return;
               }

               if (empty) {
                  missed = WIP.addAndGet(this, -missed);
                  if (missed == 0) {
                     return;
                  }
               } else {
                  Object val = q.poll();
                  if (mode == LEFT_VALUE) {
                     TLeft left = (TLeft)val;
                     int idx = this.leftIndex++;
                     this.lefts.put(idx, val);

                     Publisher<TLeftEnd> p;
                     try {
                        p = (Publisher)Objects.requireNonNull(this.leftEnd.apply(left), "The leftEnd returned a null Publisher");
                     } catch (Throwable var24) {
                        Exceptions.addThrowable(ERROR, this, Operators.onOperatorError(this, var24, val, this.actual.currentContext()));
                        this.errorAll(a);
                        return;
                     }

                     FluxGroupJoin.LeftRightEndSubscriber end = new FluxGroupJoin.LeftRightEndSubscriber(this, true, idx);
                     this.cancellations.add(end);
                     p.subscribe(end);
                     ex = this.error;
                     if (ex != null) {
                        q.clear();
                        this.cancellations.dispose();
                        this.errorAll(a);
                        return;
                     }

                     long r = this.requested;
                     long e = 0L;

                     for(TRight right : this.rights.values()) {
                        R w;
                        try {
                           w = (R)Objects.requireNonNull(this.resultSelector.apply(left, right), "The resultSelector returned a null value");
                        } catch (Throwable var23) {
                           Exceptions.addThrowable(ERROR, this, Operators.onOperatorError(this, var23, right, this.actual.currentContext()));
                           this.errorAll(a);
                           return;
                        }

                        if (e == r) {
                           Exceptions.addThrowable(ERROR, this, Exceptions.failWithOverflow("Could not emit value due to lack of requests"));
                           q.clear();
                           this.cancellations.dispose();
                           this.errorAll(a);
                           return;
                        }

                        a.onNext(w);
                        ++e;
                     }

                     if (e != 0L) {
                        Operators.produced(REQUESTED, this, e);
                     }
                  } else if (mode != RIGHT_VALUE) {
                     if (mode == LEFT_CLOSE) {
                        FluxGroupJoin.LeftRightEndSubscriber end = (FluxGroupJoin.LeftRightEndSubscriber)val;
                        this.lefts.remove(end.index);
                        this.cancellations.remove(end);
                     } else if (mode == RIGHT_CLOSE) {
                        FluxGroupJoin.LeftRightEndSubscriber end = (FluxGroupJoin.LeftRightEndSubscriber)val;
                        this.rights.remove(end.index);
                        this.cancellations.remove(end);
                     }
                  } else {
                     TRight right = (TRight)val;
                     int idx = this.rightIndex++;
                     this.rights.put(idx, val);

                     Publisher<TRightEnd> p;
                     try {
                        p = (Publisher)Objects.requireNonNull(this.rightEnd.apply(right), "The rightEnd returned a null Publisher");
                     } catch (Throwable var22) {
                        Exceptions.addThrowable(ERROR, this, Operators.onOperatorError(this, var22, val, this.actual.currentContext()));
                        this.errorAll(a);
                        return;
                     }

                     FluxGroupJoin.LeftRightEndSubscriber end = new FluxGroupJoin.LeftRightEndSubscriber(this, false, idx);
                     this.cancellations.add(end);
                     p.subscribe(end);
                     ex = this.error;
                     if (ex != null) {
                        q.clear();
                        this.cancellations.dispose();
                        this.errorAll(a);
                        return;
                     }

                     long r = this.requested;
                     long e = 0L;

                     for(TLeft left : this.lefts.values()) {
                        R w;
                        try {
                           w = (R)Objects.requireNonNull(this.resultSelector.apply(left, right), "The resultSelector returned a null value");
                        } catch (Throwable var21) {
                           Exceptions.addThrowable(ERROR, this, Operators.onOperatorError(this, var21, left, this.actual.currentContext()));
                           this.errorAll(a);
                           return;
                        }

                        if (e == r) {
                           Exceptions.addThrowable(ERROR, this, Exceptions.failWithOverflow("Could not emit value due to lack of requests"));
                           q.clear();
                           this.cancellations.dispose();
                           this.errorAll(a);
                           return;
                        }

                        a.onNext(w);
                        ++e;
                     }

                     if (e != 0L) {
                        Operators.produced(REQUESTED, this, e);
                     }
                  }
               }
            }

            q.clear();
         }
      }

      @Override
      public void innerError(Throwable ex) {
         if (Exceptions.addThrowable(ERROR, this, ex)) {
            ACTIVE.decrementAndGet(this);
            this.drain();
         } else {
            Operators.onErrorDropped(ex, this.actual.currentContext());
         }

      }

      @Override
      public void innerComplete(FluxGroupJoin.LeftRightSubscriber sender) {
         this.cancellations.remove(sender);
         ACTIVE.decrementAndGet(this);
         this.drain();
      }

      @Override
      public void innerValue(boolean isLeft, Object o) {
         this.queueBiOffer.test(isLeft ? LEFT_VALUE : RIGHT_VALUE, o);
         this.drain();
      }

      @Override
      public void innerClose(boolean isLeft, FluxGroupJoin.LeftRightEndSubscriber index) {
         this.queueBiOffer.test(isLeft ? LEFT_CLOSE : RIGHT_CLOSE, index);
         this.drain();
      }

      @Override
      public void innerCloseError(Throwable ex) {
         if (Exceptions.addThrowable(ERROR, this, ex)) {
            this.drain();
         } else {
            Operators.onErrorDropped(ex, this.actual.currentContext());
         }

      }
   }
}
