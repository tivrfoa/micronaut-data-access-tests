package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import java.util.stream.Stream;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxSwitchMapNoPrefetch<T, R> extends InternalFluxOperator<T, R> {
   final Function<? super T, ? extends Publisher<? extends R>> mapper;
   static int INDEX_OFFSET = 32;
   static int HAS_REQUEST_OFFSET = 4;
   static long TERMINATED = -1L;
   static long INNER_WIP_MASK = 1L;
   static long INNER_SUBSCRIBED_MASK = 2L;
   static long INNER_COMPLETED_MASK = 4L;
   static long COMPLETED_MASK = 8L;
   static long HAS_REQUEST_MASK = 4294967280L;
   static int MAX_HAS_REQUEST = 268435455;

   FluxSwitchMapNoPrefetch(Flux<? extends T> source, Function<? super T, ? extends Publisher<? extends R>> mapper) {
      super(source);
      this.mapper = (Function)Objects.requireNonNull(mapper, "mapper");
   }

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      return FluxFlatMap.trySubscribeScalarMap(this.source, actual, this.mapper, false, false)
         ? null
         : new FluxSwitchMapNoPrefetch.SwitchMapMain<>(actual, this.mapper);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static long setTerminated(FluxSwitchMapNoPrefetch.SwitchMapMain<?, ?> instance) {
      long state;
      do {
         state = instance.state;
         if (state == TERMINATED) {
            return TERMINATED;
         }
      } while(!FluxSwitchMapNoPrefetch.SwitchMapMain.STATE.compareAndSet(instance, state, TERMINATED));

      return state;
   }

   static long setMainCompleted(FluxSwitchMapNoPrefetch.SwitchMapMain<?, ?> instance) {
      long state;
      do {
         state = instance.state;
         if (state == TERMINATED) {
            return TERMINATED;
         }

         if ((state & COMPLETED_MASK) == COMPLETED_MASK) {
            return state;
         }
      } while(!FluxSwitchMapNoPrefetch.SwitchMapMain.STATE.compareAndSet(instance, state, state | COMPLETED_MASK));

      return state;
   }

   static long addRequest(FluxSwitchMapNoPrefetch.SwitchMapMain<?, ?> instance, long previousRequested) {
      long state;
      long nextState;
      do {
         state = instance.state;
         if (state == TERMINATED) {
            return TERMINATED;
         }

         int hasRequest = hasRequest(state);
         if (hasRequest == 0 && previousRequested > 0L) {
            return state;
         }

         nextState = state(index(state), isWip(state), hasRequest + 1, isInnerSubscribed(state), hasMainCompleted(state), hasInnerCompleted(state));
      } while(!FluxSwitchMapNoPrefetch.SwitchMapMain.STATE.compareAndSet(instance, state, nextState));

      return nextState;
   }

   static long incrementIndex(FluxSwitchMapNoPrefetch.SwitchMapMain<?, ?> instance) {
      long state = instance.state;
      if (state == TERMINATED) {
         return TERMINATED;
      } else {
         int nextIndex = nextIndex(state);

         while(
            !FluxSwitchMapNoPrefetch.SwitchMapMain.STATE.compareAndSet(instance, state, state(nextIndex, isWip(state), hasRequest(state), false, false, false))
         ) {
            state = instance.state;
            if (state == TERMINATED) {
               return TERMINATED;
            }
         }

         return state;
      }
   }

   static long setInnerSubscribed(FluxSwitchMapNoPrefetch.SwitchMapMain<?, ?> instance, int expectedIndex) {
      long state;
      do {
         state = instance.state;
         if (state == TERMINATED) {
            return TERMINATED;
         }

         int actualIndex = index(state);
         if (expectedIndex != actualIndex) {
            return state;
         }
      } while(
         !FluxSwitchMapNoPrefetch.SwitchMapMain.STATE
            .compareAndSet(instance, state, state(expectedIndex, false, hasRequest(state), true, hasMainCompleted(state), false))
      );

      return state;
   }

   static long setWip(FluxSwitchMapNoPrefetch.SwitchMapMain<?, ?> instance, int expectedIndex) {
      long state;
      do {
         state = instance.state;
         if (state == TERMINATED) {
            return TERMINATED;
         }

         int actualIndex = index(state);
         if (expectedIndex != actualIndex) {
            return state;
         }
      } while(
         !FluxSwitchMapNoPrefetch.SwitchMapMain.STATE
            .compareAndSet(instance, state, state(expectedIndex, true, hasRequest(state), true, hasMainCompleted(state), false))
      );

      return state;
   }

   static long unsetWip(FluxSwitchMapNoPrefetch.SwitchMapMain<?, ?> instance, int expectedIndex, boolean isDemandFulfilled, int expectedRequest) {
      long state;
      int actualIndex;
      int actualRequest;
      do {
         state = instance.state;
         if (state == TERMINATED) {
            return TERMINATED;
         }

         actualIndex = index(state);
         actualRequest = hasRequest(state);
         boolean sameIndex = expectedIndex == actualIndex;
         if (isDemandFulfilled && expectedRequest < actualRequest && sameIndex) {
            return state;
         }
      } while(
         !FluxSwitchMapNoPrefetch.SwitchMapMain.STATE
            .compareAndSet(
               instance,
               state,
               state(
                  actualIndex,
                  false,
                  isDemandFulfilled && expectedRequest == actualRequest ? 0 : actualRequest,
                  isInnerSubscribed(state),
                  hasMainCompleted(state),
                  false
               )
            )
      );

      return state;
   }

   static long setInnerCompleted(FluxSwitchMapNoPrefetch.SwitchMapMain<?, ?> instance) {
      long state;
      boolean isInnerSubscribed;
      do {
         state = instance.state;
         if (state == TERMINATED) {
            return TERMINATED;
         }

         isInnerSubscribed = isInnerSubscribed(state);
      } while(
         !FluxSwitchMapNoPrefetch.SwitchMapMain.STATE
            .compareAndSet(instance, state, state(index(state), false, hasRequest(state), isInnerSubscribed, hasMainCompleted(state), isInnerSubscribed))
      );

      return state;
   }

   static long state(int index, boolean wip, int hasRequest, boolean innerSubscribed, boolean mainCompleted, boolean innerCompleted) {
      return (long)index << INDEX_OFFSET
         | (wip ? INNER_WIP_MASK : 0L)
         | (long)Math.max(Math.min(hasRequest, MAX_HAS_REQUEST), 0) << HAS_REQUEST_OFFSET
         | (innerSubscribed ? INNER_SUBSCRIBED_MASK : 0L)
         | (mainCompleted ? COMPLETED_MASK : 0L)
         | (innerCompleted ? INNER_COMPLETED_MASK : 0L);
   }

   static boolean isInnerSubscribed(long state) {
      return (state & INNER_SUBSCRIBED_MASK) == INNER_SUBSCRIBED_MASK;
   }

   static boolean hasMainCompleted(long state) {
      return (state & COMPLETED_MASK) == COMPLETED_MASK;
   }

   static boolean hasInnerCompleted(long state) {
      return (state & INNER_COMPLETED_MASK) == INNER_COMPLETED_MASK;
   }

   static int hasRequest(long state) {
      return (int)(state & HAS_REQUEST_MASK) >> HAS_REQUEST_OFFSET;
   }

   static int index(long state) {
      return (int)(state >>> INDEX_OFFSET);
   }

   static int nextIndex(long state) {
      return (int)(state >>> INDEX_OFFSET) + 1;
   }

   static boolean isWip(long state) {
      return (state & INNER_WIP_MASK) == INNER_WIP_MASK;
   }

   static final class SwitchMapInner<T, R> implements InnerConsumer<R> {
      final FluxSwitchMapNoPrefetch.SwitchMapMain<T, R> parent;
      final CoreSubscriber<? super R> actual;
      final int index;
      Subscription s;
      long produced;
      long requested;
      boolean done;
      T nextElement;
      FluxSwitchMapNoPrefetch.SwitchMapInner<T, R> nextInner;

      SwitchMapInner(FluxSwitchMapNoPrefetch.SwitchMapMain<T, R> parent, CoreSubscriber<? super R> actual, int index) {
         this.parent = parent;
         this.actual = actual;
         this.index = index;
      }

      @Override
      public Context currentContext() {
         return this.parent.currentContext();
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.isCancelledByParent();
         } else if (key == Scannable.Attr.PARENT) {
            return this.parent;
         } else if (key == Scannable.Attr.ACTUAL) {
            return this.actual;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            int expectedIndex = this.index;
            FluxSwitchMapNoPrefetch.SwitchMapMain<T, R> parent = this.parent;
            long state = FluxSwitchMapNoPrefetch.setInnerSubscribed(parent, expectedIndex);
            if (state == FluxSwitchMapNoPrefetch.TERMINATED) {
               s.cancel();
               return;
            }

            int actualIndex = FluxSwitchMapNoPrefetch.index(state);
            if (expectedIndex != actualIndex) {
               s.cancel();
               parent.subscribeInner(this.nextElement, this.nextInner, actualIndex);
               return;
            }

            if (FluxSwitchMapNoPrefetch.hasRequest(state) > 0) {
               long requested = parent.requested;
               this.requested = requested;
               s.request(requested);
            }
         }

      }

      @Override
      public void onNext(R t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            FluxSwitchMapNoPrefetch.SwitchMapMain<T, R> parent = this.parent;
            Subscription s = this.s;
            int expectedIndex = this.index;
            long requested = this.requested;
            long state = FluxSwitchMapNoPrefetch.setWip(parent, expectedIndex);
            if (state == FluxSwitchMapNoPrefetch.TERMINATED) {
               Operators.onDiscard(t, this.actual.currentContext());
            } else {
               int actualIndex = FluxSwitchMapNoPrefetch.index(state);
               if (actualIndex != expectedIndex) {
                  Operators.onDiscard(t, this.actual.currentContext());
               } else {
                  this.actual.onNext(t);
                  long produced = 0L;
                  boolean isDemandFulfilled = false;
                  int expectedHasRequest = FluxSwitchMapNoPrefetch.hasRequest(state);
                  if (requested != Long.MAX_VALUE) {
                     produced = this.produced + 1L;
                     this.produced = produced;
                     if (expectedHasRequest > 1) {
                        long actualRequested = parent.requested;
                        long toRequestInAddition = actualRequested - requested;
                        if (toRequestInAddition > 0L) {
                           requested = actualRequested;
                           this.requested = actualRequested;
                           if (actualRequested == Long.MAX_VALUE) {
                              produced = 0L;
                              this.produced = 0L;
                              s.request(Long.MAX_VALUE);
                           } else {
                              s.request(toRequestInAddition);
                           }
                        }
                     }

                     isDemandFulfilled = produced == requested;
                     if (isDemandFulfilled) {
                        this.produced = 0L;
                        requested = FluxSwitchMapNoPrefetch.SwitchMapMain.REQUESTED.addAndGet(parent, -produced);
                        this.requested = requested;
                        produced = 0L;
                        isDemandFulfilled = requested == 0L;
                        if (!isDemandFulfilled) {
                           s.request(requested);
                        }
                     }
                  }

                  while(true) {
                     state = FluxSwitchMapNoPrefetch.unsetWip(parent, expectedIndex, isDemandFulfilled, expectedHasRequest);
                     if (state == FluxSwitchMapNoPrefetch.TERMINATED) {
                        return;
                     }

                     actualIndex = FluxSwitchMapNoPrefetch.index(state);
                     if (expectedIndex != actualIndex) {
                        if (produced > 0L) {
                           this.produced = 0L;
                           this.requested = 0L;
                           FluxSwitchMapNoPrefetch.SwitchMapMain.REQUESTED.addAndGet(parent, -produced);
                        }

                        parent.subscribeInner(this.nextElement, this.nextInner, actualIndex);
                        return;
                     }

                     int actualHasRequest = FluxSwitchMapNoPrefetch.hasRequest(state);
                     if (!isDemandFulfilled || expectedHasRequest >= actualHasRequest) {
                        return;
                     }

                     expectedHasRequest = actualHasRequest;
                     long currentRequest = parent.requested;
                     long toRequestInAddition = currentRequest - requested;
                     if (toRequestInAddition > 0L) {
                        requested = currentRequest;
                        this.requested = currentRequest;
                        isDemandFulfilled = false;
                        s.request(currentRequest == Long.MAX_VALUE ? Long.MAX_VALUE : toRequestInAddition);
                     }
                  }
               }
            }
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            FluxSwitchMapNoPrefetch.SwitchMapMain<T, R> parent = this.parent;
            if (!Exceptions.addThrowable(FluxSwitchMapNoPrefetch.SwitchMapMain.THROWABLE, parent, t)) {
               Operators.onErrorDropped(t, this.actual.currentContext());
            } else {
               long state = FluxSwitchMapNoPrefetch.setTerminated(parent);
               if (state != FluxSwitchMapNoPrefetch.TERMINATED) {
                  if (!FluxSwitchMapNoPrefetch.hasMainCompleted(state)) {
                     parent.s.cancel();
                  }

                  this.actual.onError(Exceptions.terminate(FluxSwitchMapNoPrefetch.SwitchMapMain.THROWABLE, parent));
               }
            }
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            FluxSwitchMapNoPrefetch.SwitchMapMain<T, R> parent = this.parent;
            int expectedIndex = this.index;
            long state = FluxSwitchMapNoPrefetch.setWip(parent, expectedIndex);
            if (state != FluxSwitchMapNoPrefetch.TERMINATED) {
               int actualIndex = FluxSwitchMapNoPrefetch.index(state);
               if (actualIndex == expectedIndex) {
                  long produced = this.produced;
                  if (produced > 0L) {
                     this.produced = 0L;
                     this.requested = 0L;
                     FluxSwitchMapNoPrefetch.SwitchMapMain.REQUESTED.addAndGet(parent, -produced);
                  }

                  if (FluxSwitchMapNoPrefetch.hasMainCompleted(state)) {
                     this.actual.onComplete();
                  } else {
                     state = FluxSwitchMapNoPrefetch.setInnerCompleted(parent);
                     if (state != FluxSwitchMapNoPrefetch.TERMINATED) {
                        actualIndex = FluxSwitchMapNoPrefetch.index(state);
                        if (expectedIndex != actualIndex) {
                           parent.subscribeInner(this.nextElement, this.nextInner, actualIndex);
                        } else if (FluxSwitchMapNoPrefetch.hasMainCompleted(state)) {
                           this.actual.onComplete();
                        }

                     }
                  }
               }
            }
         }
      }

      void request(long n) {
         long requested = this.requested;
         this.requested = Operators.addCap(requested, n);
         this.s.request(n);
      }

      boolean isCancelledByParent() {
         long state = this.parent.state;
         return this.index != FluxSwitchMapNoPrefetch.index(state) && !this.done || !this.parent.done && state == FluxSwitchMapNoPrefetch.TERMINATED;
      }

      void cancelFromParent() {
         this.s.cancel();
      }
   }

   static final class SwitchMapMain<T, R> implements InnerOperator<T, R> {
      final Function<? super T, ? extends Publisher<? extends R>> mapper;
      final CoreSubscriber<? super R> actual;
      Subscription s;
      boolean done;
      FluxSwitchMapNoPrefetch.SwitchMapInner<T, R> inner;
      volatile Throwable throwable;
      static final AtomicReferenceFieldUpdater<FluxSwitchMapNoPrefetch.SwitchMapMain, Throwable> THROWABLE = AtomicReferenceFieldUpdater.newUpdater(
         FluxSwitchMapNoPrefetch.SwitchMapMain.class, Throwable.class, "throwable"
      );
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxSwitchMapNoPrefetch.SwitchMapMain> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxSwitchMapNoPrefetch.SwitchMapMain.class, "requested"
      );
      volatile long state;
      static final AtomicLongFieldUpdater<FluxSwitchMapNoPrefetch.SwitchMapMain> STATE = AtomicLongFieldUpdater.newUpdater(
         FluxSwitchMapNoPrefetch.SwitchMapMain.class, "state"
      );

      SwitchMapMain(CoreSubscriber<? super R> actual, Function<? super T, ? extends Publisher<? extends R>> mapper) {
         this.actual = actual;
         this.mapper = mapper;
      }

      @Override
      public final CoreSubscriber<? super R> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         long state = this.state;
         if (key != Scannable.Attr.CANCELLED) {
            if (key == Scannable.Attr.PARENT) {
               return this.s;
            } else if (key == Scannable.Attr.TERMINATED) {
               return this.done;
            } else if (key == Scannable.Attr.ERROR) {
               return this.throwable;
            } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
               return this.requested;
            } else {
               return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
            }
         } else {
            return !this.done && state == FluxSwitchMapNoPrefetch.TERMINATED;
         }
      }

      @Override
      public Stream<? extends Scannable> inners() {
         return Stream.of(this.inner);
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
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            FluxSwitchMapNoPrefetch.SwitchMapInner<T, R> si = this.inner;
            boolean hasInner = si != null;
            if (!hasInner) {
               FluxSwitchMapNoPrefetch.SwitchMapInner<T, R> nsi = new FluxSwitchMapNoPrefetch.SwitchMapInner<>(this, this.actual, 0);
               this.inner = nsi;
               this.subscribeInner(t, nsi, 0);
            } else {
               int nextIndex = si.index + 1;
               FluxSwitchMapNoPrefetch.SwitchMapInner<T, R> nsi = new FluxSwitchMapNoPrefetch.SwitchMapInner<>(this, this.actual, nextIndex);
               this.inner = nsi;
               si.nextInner = nsi;
               si.nextElement = t;
               long state = FluxSwitchMapNoPrefetch.incrementIndex(this);
               if (state == FluxSwitchMapNoPrefetch.TERMINATED) {
                  Operators.onDiscard(t, this.actual.currentContext());
               } else {
                  if (FluxSwitchMapNoPrefetch.isInnerSubscribed(state)) {
                     si.cancelFromParent();
                     if (!FluxSwitchMapNoPrefetch.isWip(state)) {
                        long produced = si.produced;
                        if (produced > 0L) {
                           si.produced = 0L;
                           if (this.requested != Long.MAX_VALUE) {
                              si.requested = 0L;
                              REQUESTED.addAndGet(this, -produced);
                           }
                        }

                        this.subscribeInner(t, nsi, nextIndex);
                     }
                  }

               }
            }
         }
      }

      void subscribeInner(T nextElement, FluxSwitchMapNoPrefetch.SwitchMapInner<T, R> nextInner, int nextIndex) {
         CoreSubscriber<? super R> actual = this.actual;

         Context context;
         for(context = actual.currentContext(); nextInner.index != nextIndex; nextInner = nextInner.nextInner) {
            Operators.onDiscard(nextElement, context);
            nextElement = nextInner.nextElement;
         }

         Publisher<? extends R> p;
         try {
            p = (Publisher)Objects.requireNonNull(this.mapper.apply(nextElement), "The mapper returned a null publisher");
         } catch (Throwable var8) {
            this.onError(Operators.onOperatorError(this.s, var8, nextElement, context));
            return;
         }

         p.subscribe(nextInner);
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            if (!Exceptions.addThrowable(THROWABLE, this, t)) {
               Operators.onErrorDropped(t, this.actual.currentContext());
            } else {
               long state = FluxSwitchMapNoPrefetch.setTerminated(this);
               if (state != FluxSwitchMapNoPrefetch.TERMINATED) {
                  FluxSwitchMapNoPrefetch.SwitchMapInner<T, R> inner = this.inner;
                  if (inner != null && FluxSwitchMapNoPrefetch.isInnerSubscribed(state)) {
                     inner.cancelFromParent();
                  }

                  this.actual.onError(Exceptions.terminate(THROWABLE, this));
               }
            }
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            long state = FluxSwitchMapNoPrefetch.setMainCompleted(this);
            if (state != FluxSwitchMapNoPrefetch.TERMINATED) {
               FluxSwitchMapNoPrefetch.SwitchMapInner<T, R> inner = this.inner;
               if (inner == null || FluxSwitchMapNoPrefetch.hasInnerCompleted(state)) {
                  this.actual.onComplete();
               }

            }
         }
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            long previousRequested = Operators.addCap(REQUESTED, this, n);
            long state = FluxSwitchMapNoPrefetch.addRequest(this, previousRequested);
            if (state == FluxSwitchMapNoPrefetch.TERMINATED) {
               return;
            }

            if (FluxSwitchMapNoPrefetch.hasRequest(state) == 1
               && FluxSwitchMapNoPrefetch.isInnerSubscribed(state)
               && !FluxSwitchMapNoPrefetch.hasInnerCompleted(state)) {
               FluxSwitchMapNoPrefetch.SwitchMapInner<T, R> inner = this.inner;
               if (inner.index == FluxSwitchMapNoPrefetch.index(state)) {
                  inner.request(n);
               }
            }
         }

      }

      @Override
      public void cancel() {
         long state = FluxSwitchMapNoPrefetch.setTerminated(this);
         if (state != FluxSwitchMapNoPrefetch.TERMINATED) {
            FluxSwitchMapNoPrefetch.SwitchMapInner<T, R> inner = this.inner;
            if (inner != null
               && FluxSwitchMapNoPrefetch.isInnerSubscribed(state)
               && !FluxSwitchMapNoPrefetch.hasInnerCompleted(state)
               && inner.index == FluxSwitchMapNoPrefetch.index(state)) {
               inner.cancelFromParent();
            }

            if (!FluxSwitchMapNoPrefetch.hasMainCompleted(state)) {
               this.s.cancel();
               Throwable e = Exceptions.terminate(THROWABLE, this);
               if (e != null) {
                  Operators.onErrorDropped(e, this.actual.currentContext());
               }
            }

         }
      }
   }
}
