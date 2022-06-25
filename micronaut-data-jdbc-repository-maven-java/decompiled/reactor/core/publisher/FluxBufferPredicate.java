package reactor.core.publisher;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxBufferPredicate<T, C extends Collection<? super T>> extends InternalFluxOperator<T, C> {
   final Predicate<? super T> predicate;
   final Supplier<C> bufferSupplier;
   final FluxBufferPredicate.Mode mode;

   FluxBufferPredicate(Flux<? extends T> source, Predicate<? super T> predicate, Supplier<C> bufferSupplier, FluxBufferPredicate.Mode mode) {
      super(source);
      this.predicate = (Predicate)Objects.requireNonNull(predicate, "predicate");
      this.bufferSupplier = (Supplier)Objects.requireNonNull(bufferSupplier, "bufferSupplier");
      this.mode = mode;
   }

   @Override
   public int getPrefetch() {
      return 1;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super C> actual) {
      C initialBuffer = (C)Objects.requireNonNull(this.bufferSupplier.get(), "The bufferSupplier returned a null initial buffer");
      return new FluxBufferPredicate.BufferPredicateSubscriber<>(actual, initialBuffer, this.bufferSupplier, this.predicate, this.mode);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class BufferPredicateSubscriber<T, C extends Collection<? super T>>
      extends AbstractQueue<C>
      implements Fuseable.ConditionalSubscriber<T>,
      InnerOperator<T, C>,
      BooleanSupplier {
      final CoreSubscriber<? super C> actual;
      final Supplier<C> bufferSupplier;
      final FluxBufferPredicate.Mode mode;
      final Predicate<? super T> predicate;
      @Nullable
      C buffer;
      boolean done;
      volatile boolean fastpath;
      volatile long requestedBuffers;
      static final AtomicLongFieldUpdater<FluxBufferPredicate.BufferPredicateSubscriber> REQUESTED_BUFFERS = AtomicLongFieldUpdater.newUpdater(
         FluxBufferPredicate.BufferPredicateSubscriber.class, "requestedBuffers"
      );
      volatile long requestedFromSource;
      static final AtomicLongFieldUpdater<FluxBufferPredicate.BufferPredicateSubscriber> REQUESTED_FROM_SOURCE = AtomicLongFieldUpdater.newUpdater(
         FluxBufferPredicate.BufferPredicateSubscriber.class, "requestedFromSource"
      );
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<FluxBufferPredicate.BufferPredicateSubscriber, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         FluxBufferPredicate.BufferPredicateSubscriber.class, Subscription.class, "s"
      );

      BufferPredicateSubscriber(
         CoreSubscriber<? super C> actual, C initialBuffer, Supplier<C> bufferSupplier, Predicate<? super T> predicate, FluxBufferPredicate.Mode mode
      ) {
         this.actual = actual;
         this.buffer = initialBuffer;
         this.bufferSupplier = bufferSupplier;
         this.predicate = predicate;
         this.mode = mode;
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n)) {
            if (n == Long.MAX_VALUE) {
               this.fastpath = true;
               REQUESTED_BUFFERS.set(this, Long.MAX_VALUE);
               REQUESTED_FROM_SOURCE.set(this, Long.MAX_VALUE);
               this.s.request(Long.MAX_VALUE);
            } else if (!DrainUtils.postCompleteRequest(n, this.actual, this, REQUESTED_BUFFERS, this, this)) {
               Operators.addCap(REQUESTED_FROM_SOURCE, this, n);
               this.s.request(n);
            }
         }

      }

      @Override
      public void cancel() {
         synchronized(this) {
            C b = this.buffer;
            this.buffer = null;
            Operators.onDiscardMultiple(b, this.actual.currentContext());
         }

         this.cleanup();
         Operators.terminate(S, this);
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.setOnce(S, this, s)) {
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         if (!this.tryOnNext(t)) {
            this.s.request(1L);
         }

      }

      @Override
      public boolean tryOnNext(T t) {
         if (this.done) {
            Operators.onNextDropped(t, this.actual.currentContext());
            return true;
         } else {
            boolean match;
            try {
               match = this.predicate.test(t);
            } catch (Throwable var5) {
               Context ctx = this.actual.currentContext();
               this.onError(Operators.onOperatorError(this.s, var5, t, ctx));
               Operators.onDiscard(t, ctx);
               return true;
            }

            if (this.mode == FluxBufferPredicate.Mode.UNTIL && match) {
               if (this.cancelledWhileAdding(t)) {
                  return true;
               }

               this.onNextNewBuffer();
            } else if (this.mode == FluxBufferPredicate.Mode.UNTIL_CUT_BEFORE && match) {
               this.onNextNewBuffer();
               if (this.cancelledWhileAdding(t)) {
                  return true;
               }
            } else if (this.mode == FluxBufferPredicate.Mode.WHILE && !match) {
               this.onNextNewBuffer();
            } else if (this.cancelledWhileAdding(t)) {
               return true;
            }

            if (this.fastpath) {
               return true;
            } else {
               boolean isNotExpectingFromSource = REQUESTED_FROM_SOURCE.decrementAndGet(this) == 0L;
               boolean isStillExpectingBuffer = REQUESTED_BUFFERS.get(this) > 0L;
               return !isNotExpectingFromSource || !isStillExpectingBuffer || !REQUESTED_FROM_SOURCE.compareAndSet(this, 0L, 1L);
            }
         }
      }

      boolean cancelledWhileAdding(T value) {
         synchronized(this) {
            C b = this.buffer;
            if (b != null && this.s != Operators.cancelledSubscription()) {
               b.add(value);
               return false;
            } else {
               Operators.onDiscard(value, this.actual.currentContext());
               return true;
            }
         }
      }

      @Nullable
      C triggerNewBuffer() {
         C b;
         synchronized(this) {
            b = this.buffer;
            if (b == null || this.s == Operators.cancelledSubscription()) {
               return null;
            }
         }

         if (b.isEmpty()) {
            return null;
         } else {
            C c;
            try {
               c = (C)Objects.requireNonNull(this.bufferSupplier.get(), "The bufferSupplier returned a null buffer");
            } catch (Throwable var6) {
               this.onError(Operators.onOperatorError(this.s, var6, this.actual.currentContext()));
               return null;
            }

            synchronized(this) {
               if (this.buffer == null) {
                  return null;
               } else {
                  this.buffer = c;
                  return b;
               }
            }
         }
      }

      private void onNextNewBuffer() {
         C b = this.triggerNewBuffer();
         if (b != null) {
            if (this.fastpath) {
               this.actual.onNext((T)b);
               return;
            }

            long r = REQUESTED_BUFFERS.getAndDecrement(this);
            if (r > 0L) {
               this.actual.onNext((T)b);
               return;
            }

            this.cancel();
            this.actual.onError(Exceptions.failWithOverflow("Could not emit buffer due to lack of requests"));
         }

      }

      @Override
      public CoreSubscriber<? super C> actual() {
         return this.actual;
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            C b;
            synchronized(this) {
               b = this.buffer;
               this.buffer = null;
            }

            this.cleanup();
            Operators.onDiscardMultiple(b, this.actual.currentContext());
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            this.cleanup();
            DrainUtils.postComplete(this.actual, this, REQUESTED_BUFFERS, this, this);
         }
      }

      void cleanup() {
         if (this.predicate instanceof Disposable) {
            ((Disposable)this.predicate).dispose();
         }

      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.getAsBoolean();
         } else if (key == Scannable.Attr.CAPACITY) {
            C b = this.buffer;
            return b != null ? b.size() : 0;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requestedBuffers;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      public boolean getAsBoolean() {
         return this.s == Operators.cancelledSubscription();
      }

      public Iterator<C> iterator() {
         return this.isEmpty() ? Collections.emptyIterator() : Collections.singleton(this.buffer).iterator();
      }

      public boolean offer(C objects) {
         throw new IllegalArgumentException();
      }

      @Nullable
      public C poll() {
         C b = this.buffer;
         if (b != null && !b.isEmpty()) {
            synchronized(this) {
               this.buffer = null;
               return b;
            }
         } else {
            return null;
         }
      }

      @Nullable
      public C peek() {
         return this.buffer;
      }

      public int size() {
         C b = this.buffer;
         return b != null && !b.isEmpty() ? 1 : 0;
      }

      public String toString() {
         return "FluxBufferPredicate";
      }
   }

   static class ChangedPredicate<T, K> implements Predicate<T>, Disposable {
      private Function<? super T, ? extends K> keySelector;
      private BiPredicate<? super K, ? super K> keyComparator;
      private K lastKey;

      ChangedPredicate(Function<? super T, ? extends K> keySelector, BiPredicate<? super K, ? super K> keyComparator) {
         this.keySelector = keySelector;
         this.keyComparator = keyComparator;
      }

      @Override
      public void dispose() {
         this.lastKey = null;
      }

      public boolean test(T t) {
         K k = (K)this.keySelector.apply(t);
         if (null == this.lastKey) {
            this.lastKey = k;
            return false;
         } else {
            boolean match = this.keyComparator.test(this.lastKey, k);
            this.lastKey = k;
            return !match;
         }
      }
   }

   public static enum Mode {
      UNTIL,
      UNTIL_CUT_BEFORE,
      WHILE;
   }
}
