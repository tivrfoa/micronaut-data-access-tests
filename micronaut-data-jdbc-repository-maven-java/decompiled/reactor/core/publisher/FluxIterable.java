package reactor.core.publisher;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import org.reactivestreams.Subscriber;
import reactor.core.CoreSubscriber;
import reactor.core.Exceptions;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.function.Tuple2;

final class FluxIterable<T> extends Flux<T> implements Fuseable, SourceProducer<T> {
   final Iterable<? extends T> iterable;
   @Nullable
   private final Runnable onClose;

   static <T> boolean checkFinite(Iterable<T> iterable) {
      return iterable instanceof Collection || iterable.spliterator().hasCharacteristics(64);
   }

   FluxIterable(Iterable<? extends T> iterable, @Nullable Runnable onClose) {
      this.iterable = (Iterable)Objects.requireNonNull(iterable, "iterable");
      this.onClose = onClose;
   }

   FluxIterable(Iterable<? extends T> iterable) {
      this(iterable, null);
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      boolean knownToBeFinite;
      Iterator<? extends T> it;
      try {
         knownToBeFinite = checkFinite(this.iterable);
         it = this.iterable.iterator();
      } catch (Throwable var5) {
         Operators.error(actual, Operators.onOperatorError(var5, actual.currentContext()));
         return;
      }

      subscribe(actual, it, knownToBeFinite, this.onClose);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.BUFFERED) {
         if (this.iterable instanceof Collection) {
            return ((Collection)this.iterable).size();
         }

         if (this.iterable instanceof Tuple2) {
            return ((Tuple2)this.iterable).size();
         }
      }

      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }

   static <T> void subscribe(CoreSubscriber<? super T> s, Iterator<? extends T> it, boolean knownToBeFinite) {
      subscribe(s, it, knownToBeFinite, null);
   }

   static <T> void subscribe(CoreSubscriber<? super T> s, Iterator<? extends T> it, boolean knownToBeFinite, @Nullable Runnable onClose) {
      if (it == null) {
         Operators.error(s, new NullPointerException("The iterator is null"));
      } else {
         boolean b;
         try {
            b = it.hasNext();
         } catch (Throwable var9) {
            Operators.error(s, Operators.onOperatorError(var9, s.currentContext()));
            if (onClose != null) {
               try {
                  onClose.run();
               } catch (Throwable var7) {
                  Operators.onErrorDropped(var7, s.currentContext());
               }
            }

            return;
         }

         if (!b) {
            Operators.complete(s);
            if (onClose != null) {
               try {
                  onClose.run();
               } catch (Throwable var8) {
                  Operators.onErrorDropped(var8, s.currentContext());
               }
            }

         } else {
            if (s instanceof Fuseable.ConditionalSubscriber) {
               s.onSubscribe(new FluxIterable.IterableSubscriptionConditional<>((Fuseable.ConditionalSubscriber<? super T>)s, it, knownToBeFinite, onClose));
            } else {
               s.onSubscribe(new FluxIterable.IterableSubscription<>(s, it, knownToBeFinite, onClose));
            }

         }
      }
   }

   static final class IterableSubscription<T> implements InnerProducer<T>, Fuseable.SynchronousSubscription<T> {
      final CoreSubscriber<? super T> actual;
      final Iterator<? extends T> iterator;
      final boolean knownToBeFinite;
      final Runnable onClose;
      volatile boolean cancelled;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxIterable.IterableSubscription> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxIterable.IterableSubscription.class, "requested"
      );
      int state;
      static final int STATE_HAS_NEXT_NO_VALUE = 0;
      static final int STATE_HAS_NEXT_HAS_VALUE = 1;
      static final int STATE_NO_NEXT = 2;
      static final int STATE_CALL_HAS_NEXT = 3;
      T current;
      Throwable hasNextFailure;

      IterableSubscription(CoreSubscriber<? super T> actual, Iterator<? extends T> iterator, boolean knownToBeFinite, @Nullable Runnable onClose) {
         this.actual = actual;
         this.iterator = iterator;
         this.knownToBeFinite = knownToBeFinite;
         this.onClose = onClose;
      }

      IterableSubscription(CoreSubscriber<? super T> actual, Iterator<? extends T> iterator, boolean knownToBeFinite) {
         this(actual, iterator, knownToBeFinite, null);
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n) && Operators.addCap(REQUESTED, this, n) == 0L) {
            if (n == Long.MAX_VALUE) {
               this.fastPath();
            } else {
               this.slowPath(n);
            }
         }

      }

      private void onCloseWithDropError() {
         if (this.onClose != null) {
            try {
               this.onClose.run();
            } catch (Throwable var2) {
               Operators.onErrorDropped(var2, this.actual.currentContext());
            }
         }

      }

      void slowPath(long n) {
         Iterator<? extends T> a = this.iterator;
         Subscriber<? super T> s = this.actual;
         long e = 0L;

         while(true) {
            while(e == n) {
               n = this.requested;
               if (n == e) {
                  n = REQUESTED.addAndGet(this, -e);
                  if (n == 0L) {
                     return;
                  }

                  e = 0L;
               }
            }

            T t;
            try {
               t = (T)Objects.requireNonNull(a.next(), "The iterator returned a null value");
            } catch (Throwable var11) {
               s.onError(var11);
               this.onCloseWithDropError();
               return;
            }

            if (this.cancelled) {
               return;
            }

            s.onNext(t);
            if (this.cancelled) {
               return;
            }

            boolean b;
            try {
               b = a.hasNext();
            } catch (Throwable var10) {
               s.onError(var10);
               this.onCloseWithDropError();
               return;
            }

            if (this.cancelled) {
               return;
            }

            if (!b) {
               s.onComplete();
               this.onCloseWithDropError();
               return;
            }

            ++e;
         }
      }

      void fastPath() {
         Iterator<? extends T> a = this.iterator;
         Subscriber<? super T> s = this.actual;

         while(!this.cancelled) {
            T t;
            try {
               t = (T)Objects.requireNonNull(a.next(), "The iterator returned a null value");
            } catch (Exception var6) {
               s.onError(var6);
               this.onCloseWithDropError();
               return;
            }

            if (this.cancelled) {
               return;
            }

            s.onNext(t);
            if (this.cancelled) {
               return;
            }

            boolean b;
            try {
               b = a.hasNext();
            } catch (Exception var7) {
               s.onError(var7);
               this.onCloseWithDropError();
               return;
            }

            if (this.cancelled) {
               return;
            }

            if (!b) {
               s.onComplete();
               this.onCloseWithDropError();
               return;
            }
         }

      }

      @Override
      public void cancel() {
         this.onCloseWithDropError();
         this.cancelled = true;
         Operators.onDiscardMultiple(this.iterator, this.knownToBeFinite, this.actual.currentContext());
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.state == 2;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerProducer.super.scanUnsafe(key);
         }
      }

      public void clear() {
         Operators.onDiscardMultiple(this.iterator, this.knownToBeFinite, this.actual.currentContext());
         this.state = 2;
      }

      public boolean isEmpty() {
         int s = this.state;
         if (s == 2) {
            return true;
         } else if (this.cancelled && !this.knownToBeFinite) {
            return true;
         } else if (s != 1 && s != 0) {
            boolean hasNext;
            try {
               hasNext = this.iterator.hasNext();
            } catch (Throwable var4) {
               this.state = 0;
               this.hasNextFailure = var4;
               return false;
            }

            if (hasNext) {
               this.state = 0;
               return false;
            } else {
               this.state = 2;
               return true;
            }
         } else {
            return false;
         }
      }

      @Nullable
      public T poll() {
         if (this.hasNextFailure != null) {
            this.state = 2;
            throw Exceptions.propagate(this.hasNextFailure);
         } else if (!this.isEmpty()) {
            T c;
            if (this.state == 0) {
               c = (T)this.iterator.next();
            } else {
               c = this.current;
               this.current = null;
            }

            this.state = 3;
            if (c == null) {
               this.onCloseWithDropError();
               throw new NullPointerException("iterator returned a null value");
            } else {
               return c;
            }
         } else {
            this.onCloseWithDropError();
            return null;
         }
      }

      public int size() {
         return this.state == 2 ? 0 : 1;
      }
   }

   static final class IterableSubscriptionConditional<T> implements InnerProducer<T>, Fuseable.SynchronousSubscription<T> {
      final Fuseable.ConditionalSubscriber<? super T> actual;
      final Iterator<? extends T> iterator;
      final boolean knownToBeFinite;
      final Runnable onClose;
      volatile boolean cancelled;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxIterable.IterableSubscriptionConditional> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxIterable.IterableSubscriptionConditional.class, "requested"
      );
      int state;
      static final int STATE_HAS_NEXT_NO_VALUE = 0;
      static final int STATE_HAS_NEXT_HAS_VALUE = 1;
      static final int STATE_NO_NEXT = 2;
      static final int STATE_CALL_HAS_NEXT = 3;
      T current;
      Throwable hasNextFailure;

      IterableSubscriptionConditional(
         Fuseable.ConditionalSubscriber<? super T> actual, Iterator<? extends T> iterator, boolean knownToBeFinite, @Nullable Runnable onClose
      ) {
         this.actual = actual;
         this.iterator = iterator;
         this.knownToBeFinite = knownToBeFinite;
         this.onClose = onClose;
      }

      IterableSubscriptionConditional(Fuseable.ConditionalSubscriber<? super T> actual, Iterator<? extends T> iterator, boolean knownToBeFinite) {
         this(actual, iterator, knownToBeFinite, null);
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n) && Operators.addCap(REQUESTED, this, n) == 0L) {
            if (n == Long.MAX_VALUE) {
               this.fastPath();
            } else {
               this.slowPath(n);
            }
         }

      }

      private void onCloseWithDropError() {
         if (this.onClose != null) {
            try {
               this.onClose.run();
            } catch (Throwable var2) {
               Operators.onErrorDropped(var2, this.actual.currentContext());
            }
         }

      }

      void slowPath(long n) {
         Iterator<? extends T> a = this.iterator;
         Fuseable.ConditionalSubscriber<? super T> s = this.actual;
         long e = 0L;

         while(true) {
            while(e == n) {
               n = this.requested;
               if (n == e) {
                  n = REQUESTED.addAndGet(this, -e);
                  if (n == 0L) {
                     return;
                  }

                  e = 0L;
               }
            }

            T t;
            try {
               t = (T)Objects.requireNonNull(a.next(), "The iterator returned a null value");
            } catch (Throwable var12) {
               s.onError(var12);
               this.onCloseWithDropError();
               return;
            }

            if (this.cancelled) {
               return;
            }

            boolean consumed = s.tryOnNext(t);
            if (this.cancelled) {
               return;
            }

            boolean b;
            try {
               b = a.hasNext();
            } catch (Throwable var11) {
               s.onError(var11);
               this.onCloseWithDropError();
               return;
            }

            if (this.cancelled) {
               return;
            }

            if (!b) {
               s.onComplete();
               this.onCloseWithDropError();
               return;
            }

            if (consumed) {
               ++e;
            }
         }
      }

      void fastPath() {
         Iterator<? extends T> a = this.iterator;
         Fuseable.ConditionalSubscriber<? super T> s = this.actual;

         while(!this.cancelled) {
            T t;
            try {
               t = (T)Objects.requireNonNull(a.next(), "The iterator returned a null value");
            } catch (Exception var6) {
               s.onError(var6);
               this.onCloseWithDropError();
               return;
            }

            if (this.cancelled) {
               return;
            }

            s.tryOnNext(t);
            if (this.cancelled) {
               return;
            }

            boolean b;
            try {
               b = a.hasNext();
            } catch (Exception var7) {
               s.onError(var7);
               this.onCloseWithDropError();
               return;
            }

            if (this.cancelled) {
               return;
            }

            if (!b) {
               s.onComplete();
               this.onCloseWithDropError();
               return;
            }
         }

      }

      @Override
      public void cancel() {
         this.onCloseWithDropError();
         this.cancelled = true;
         Operators.onDiscardMultiple(this.iterator, this.knownToBeFinite, this.actual.currentContext());
      }

      @Override
      public CoreSubscriber<? super T> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.CANCELLED) {
            return this.cancelled;
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.state == 2;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerProducer.super.scanUnsafe(key);
         }
      }

      public void clear() {
         Operators.onDiscardMultiple(this.iterator, this.knownToBeFinite, this.actual.currentContext());
         this.state = 2;
      }

      public boolean isEmpty() {
         int s = this.state;
         if (s == 2) {
            return true;
         } else if (this.cancelled && !this.knownToBeFinite) {
            return true;
         } else if (s != 1 && s != 0) {
            boolean hasNext;
            try {
               hasNext = this.iterator.hasNext();
            } catch (Throwable var4) {
               this.state = 0;
               this.hasNextFailure = var4;
               return false;
            }

            if (hasNext) {
               this.state = 0;
               return false;
            } else {
               this.state = 2;
               return true;
            }
         } else {
            return false;
         }
      }

      @Nullable
      public T poll() {
         if (this.hasNextFailure != null) {
            this.state = 2;
            throw Exceptions.propagate(this.hasNextFailure);
         } else if (!this.isEmpty()) {
            T c;
            if (this.state == 0) {
               c = (T)this.iterator.next();
            } else {
               c = this.current;
               this.current = null;
            }

            this.state = 3;
            return c;
         } else {
            this.onCloseWithDropError();
            return null;
         }
      }

      public int size() {
         return this.state == 2 ? 0 : 1;
      }
   }
}
