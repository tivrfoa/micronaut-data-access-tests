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
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.concurrent.Queues;
import reactor.util.context.Context;

final class FluxGroupJoin<TLeft, TRight, TLeftEnd, TRightEnd, R> extends InternalFluxOperator<TLeft, R> {
   final Publisher<? extends TRight> other;
   final Function<? super TLeft, ? extends Publisher<TLeftEnd>> leftEnd;
   final Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd;
   final BiFunction<? super TLeft, ? super Flux<TRight>, ? extends R> resultSelector;
   final Supplier<? extends Queue<TRight>> processorQueueSupplier;

   FluxGroupJoin(
      Flux<TLeft> source,
      Publisher<? extends TRight> other,
      Function<? super TLeft, ? extends Publisher<TLeftEnd>> leftEnd,
      Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd,
      BiFunction<? super TLeft, ? super Flux<TRight>, ? extends R> resultSelector,
      Supplier<? extends Queue<Object>> queueSupplier,
      Supplier<? extends Queue<TRight>> processorQueueSupplier
   ) {
      super(source);
      this.other = (Publisher)Objects.requireNonNull(other, "other");
      this.leftEnd = (Function)Objects.requireNonNull(leftEnd, "leftEnd");
      this.rightEnd = (Function)Objects.requireNonNull(rightEnd, "rightEnd");
      this.processorQueueSupplier = (Supplier)Objects.requireNonNull(processorQueueSupplier, "processorQueueSupplier");
      this.resultSelector = (BiFunction)Objects.requireNonNull(resultSelector, "resultSelector");
   }

   @Override
   public CoreSubscriber<? super TLeft> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      FluxGroupJoin.GroupJoinSubscription<TLeft, TRight, TLeftEnd, TRightEnd, R> parent = new FluxGroupJoin.GroupJoinSubscription<>(
         actual, this.leftEnd, this.rightEnd, this.resultSelector, this.processorQueueSupplier
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

   static final class GroupJoinSubscription<TLeft, TRight, TLeftEnd, TRightEnd, R> implements FluxGroupJoin.JoinSupport<R> {
      final Queue<Object> queue;
      final BiPredicate<Object, Object> queueBiOffer;
      final Disposable.Composite cancellations;
      final Map<Integer, Sinks.Many<TRight>> lefts;
      final Map<Integer, TRight> rights;
      final Function<? super TLeft, ? extends Publisher<TLeftEnd>> leftEnd;
      final Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd;
      final BiFunction<? super TLeft, ? super Flux<TRight>, ? extends R> resultSelector;
      final Supplier<? extends Queue<TRight>> processorQueueSupplier;
      final CoreSubscriber<? super R> actual;
      int leftIndex;
      int rightIndex;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxGroupJoin.GroupJoinSubscription> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxGroupJoin.GroupJoinSubscription.class, "wip"
      );
      volatile int active;
      static final AtomicIntegerFieldUpdater<FluxGroupJoin.GroupJoinSubscription> ACTIVE = AtomicIntegerFieldUpdater.newUpdater(
         FluxGroupJoin.GroupJoinSubscription.class, "active"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxGroupJoin.GroupJoinSubscription> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxGroupJoin.GroupJoinSubscription.class, "requested"
      );
      volatile Throwable error;
      static final AtomicReferenceFieldUpdater<FluxGroupJoin.GroupJoinSubscription, Throwable> ERROR = AtomicReferenceFieldUpdater.newUpdater(
         FluxGroupJoin.GroupJoinSubscription.class, Throwable.class, "error"
      );
      static final Integer LEFT_VALUE = 1;
      static final Integer RIGHT_VALUE = 2;
      static final Integer LEFT_CLOSE = 3;
      static final Integer RIGHT_CLOSE = 4;

      GroupJoinSubscription(
         CoreSubscriber<? super R> actual,
         Function<? super TLeft, ? extends Publisher<TLeftEnd>> leftEnd,
         Function<? super TRight, ? extends Publisher<TRightEnd>> rightEnd,
         BiFunction<? super TLeft, ? super Flux<TRight>, ? extends R> resultSelector,
         Supplier<? extends Queue<TRight>> processorQueueSupplier
      ) {
         this.actual = actual;
         this.cancellations = Disposables.composite();
         this.processorQueueSupplier = processorQueueSupplier;
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
         return Stream.concat(this.lefts.values().stream().map(Scannable::from), Scannable.from(this.cancellations).inners());
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
         } else {
            return key == Scannable.Attr.ERROR ? this.error : FluxGroupJoin.JoinSupport.super.scanUnsafe(key);
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

         for(Sinks.Many<TRight> up : this.lefts.values()) {
            up.emitError(ex, Sinks.EmitFailureHandler.FAIL_FAST);
         }

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
                  for(Sinks.Many<?> up : this.lefts.values()) {
                     up.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
                  }

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
                     Sinks.Many<TRight> up = Sinks.unsafe().many().unicast().onBackpressureBuffer((Queue<TRight>)this.processorQueueSupplier.get());
                     int idx = this.leftIndex++;
                     this.lefts.put(idx, up);

                     Publisher<TLeftEnd> p;
                     try {
                        p = (Publisher)Objects.requireNonNull(this.leftEnd.apply(left), "The leftEnd returned a null Publisher");
                     } catch (Throwable var19) {
                        Exceptions.addThrowable(ERROR, this, Operators.onOperatorError(this, var19, val, this.actual.currentContext()));
                        this.errorAll(a);
                        return;
                     }

                     FluxGroupJoin.LeftRightEndSubscriber end = new FluxGroupJoin.LeftRightEndSubscriber(this, true, idx);
                     this.cancellations.add(end);
                     p.subscribe(end);
                     ex = this.error;
                     if (ex != null) {
                        this.cancellations.dispose();
                        q.clear();
                        this.errorAll(a);
                        return;
                     }

                     R w;
                     try {
                        w = (R)Objects.requireNonNull(this.resultSelector.apply(left, up.asFlux()), "The resultSelector returned a null value");
                     } catch (Throwable var21) {
                        Exceptions.addThrowable(ERROR, this, Operators.onOperatorError(this, var21, up, this.actual.currentContext()));
                        this.errorAll(a);
                        return;
                     }

                     long r = this.requested;
                     if (r == 0L) {
                        Exceptions.addThrowable(ERROR, this, Exceptions.failWithOverflow());
                        this.errorAll(a);
                        return;
                     }

                     a.onNext(w);
                     Operators.produced(REQUESTED, this, 1L);

                     for(TRight right : this.rights.values()) {
                        up.emitNext(right, Sinks.EmitFailureHandler.FAIL_FAST);
                     }
                  } else if (mode == RIGHT_VALUE) {
                     TRight right = (TRight)val;
                     int idx = this.rightIndex++;
                     this.rights.put(idx, val);

                     Publisher<TRightEnd> p;
                     try {
                        p = (Publisher)Objects.requireNonNull(this.rightEnd.apply(right), "The rightEnd returned a null Publisher");
                     } catch (Throwable var20) {
                        Exceptions.addThrowable(ERROR, this, Operators.onOperatorError(this, var20, val, this.actual.currentContext()));
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

                     for(Sinks.Many<TRight> up : this.lefts.values()) {
                        up.emitNext(right, Sinks.EmitFailureHandler.FAIL_FAST);
                     }
                  } else if (mode == LEFT_CLOSE) {
                     FluxGroupJoin.LeftRightEndSubscriber end = (FluxGroupJoin.LeftRightEndSubscriber)val;
                     Sinks.Many<TRight> up = (Sinks.Many)this.lefts.remove(end.index);
                     this.cancellations.remove(end);
                     if (up != null) {
                        up.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
                     }
                  } else if (mode == RIGHT_CLOSE) {
                     FluxGroupJoin.LeftRightEndSubscriber end = (FluxGroupJoin.LeftRightEndSubscriber)val;
                     this.rights.remove(end.index);
                     this.cancellations.remove(end);
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

   interface JoinSupport<T> extends InnerProducer<T> {
      void innerError(Throwable var1);

      void innerComplete(FluxGroupJoin.LeftRightSubscriber var1);

      void innerValue(boolean var1, Object var2);

      void innerClose(boolean var1, FluxGroupJoin.LeftRightEndSubscriber var2);

      void innerCloseError(Throwable var1);
   }

   static final class LeftRightEndSubscriber implements InnerConsumer<Object>, Disposable {
      final FluxGroupJoin.JoinSupport<?> parent;
      final boolean isLeft;
      final int index;
      volatile Subscription subscription;
      static final AtomicReferenceFieldUpdater<FluxGroupJoin.LeftRightEndSubscriber, Subscription> SUBSCRIPTION = AtomicReferenceFieldUpdater.newUpdater(
         FluxGroupJoin.LeftRightEndSubscriber.class, Subscription.class, "subscription"
      );

      LeftRightEndSubscriber(FluxGroupJoin.JoinSupport<?> parent, boolean isLeft, int index) {
         this.parent = parent;
         this.isLeft = isLeft;
         this.index = index;
      }

      @Override
      public void dispose() {
         Operators.terminate(SUBSCRIPTION, this);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.subscription;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.isDisposed();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public boolean isDisposed() {
         return Operators.cancelledSubscription() == this.subscription;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(SUBSCRIPTION, this, s)) {
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(Object t) {
         if (Operators.terminate(SUBSCRIPTION, this)) {
            this.parent.innerClose(this.isLeft, this);
         }

      }

      @Override
      public void onError(Throwable t) {
         this.parent.innerError(t);
      }

      @Override
      public void onComplete() {
         this.parent.innerClose(this.isLeft, this);
      }

      @Override
      public Context currentContext() {
         return this.parent.actual().currentContext();
      }
   }

   static final class LeftRightSubscriber implements InnerConsumer<Object>, Disposable {
      final FluxGroupJoin.JoinSupport<?> parent;
      final boolean isLeft;
      volatile Subscription subscription;
      static final AtomicReferenceFieldUpdater<FluxGroupJoin.LeftRightSubscriber, Subscription> SUBSCRIPTION = AtomicReferenceFieldUpdater.newUpdater(
         FluxGroupJoin.LeftRightSubscriber.class, Subscription.class, "subscription"
      );

      LeftRightSubscriber(FluxGroupJoin.JoinSupport<?> parent, boolean isLeft) {
         this.parent = parent;
         this.isLeft = isLeft;
      }

      @Override
      public void dispose() {
         Operators.terminate(SUBSCRIPTION, this);
      }

      @Override
      public Context currentContext() {
         return this.parent.actual().currentContext();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.subscription;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.parent;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.isDisposed();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public boolean isDisposed() {
         return Operators.cancelledSubscription() == this.subscription;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(SUBSCRIPTION, this, s)) {
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(Object t) {
         this.parent.innerValue(this.isLeft, t);
      }

      @Override
      public void onError(Throwable t) {
         this.parent.innerError(t);
      }

      @Override
      public void onComplete() {
         this.parent.innerComplete(this);
      }
   }
}
