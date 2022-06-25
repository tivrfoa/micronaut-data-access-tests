package reactor.core.publisher;

import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxTimeout<T, U, V> extends InternalFluxOperator<T, T> {
   final Publisher<U> firstTimeout;
   final Function<? super T, ? extends Publisher<V>> itemTimeout;
   final Publisher<? extends T> other;
   final String timeoutDescription;

   FluxTimeout(Flux<? extends T> source, Publisher<U> firstTimeout, Function<? super T, ? extends Publisher<V>> itemTimeout, String timeoutDescription) {
      super(source);
      this.firstTimeout = (Publisher)Objects.requireNonNull(firstTimeout, "firstTimeout");
      this.itemTimeout = (Function)Objects.requireNonNull(itemTimeout, "itemTimeout");
      this.other = null;
      this.timeoutDescription = addNameToTimeoutDescription(
         source, (String)Objects.requireNonNull(timeoutDescription, "timeoutDescription is needed when no fallback")
      );
   }

   FluxTimeout(Flux<? extends T> source, Publisher<U> firstTimeout, Function<? super T, ? extends Publisher<V>> itemTimeout, Publisher<? extends T> other) {
      super(source);
      this.firstTimeout = (Publisher)Objects.requireNonNull(firstTimeout, "firstTimeout");
      this.itemTimeout = (Function)Objects.requireNonNull(itemTimeout, "itemTimeout");
      this.other = (Publisher)Objects.requireNonNull(other, "other");
      this.timeoutDescription = null;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return new FluxTimeout.TimeoutMainSubscriber<>(actual, this.firstTimeout, this.itemTimeout, this.other, this.timeoutDescription);
   }

   @Nullable
   static String addNameToTimeoutDescription(Publisher<?> source, @Nullable String timeoutDescription) {
      if (timeoutDescription == null) {
         return null;
      } else {
         Scannable s = Scannable.from(source);
         return s.isScanAvailable() ? timeoutDescription + " in '" + s.name() + "'" : timeoutDescription;
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static enum CancelledIndexedCancellable implements FluxTimeout.IndexedCancellable {
      INSTANCE;

      @Override
      public long index() {
         return Long.MAX_VALUE;
      }

      @Override
      public void cancel() {
      }
   }

   interface IndexedCancellable {
      long index();

      void cancel();
   }

   static final class TimeoutMainSubscriber<T, V> extends Operators.MultiSubscriptionSubscriber<T, T> {
      final Publisher<?> firstTimeout;
      final Function<? super T, ? extends Publisher<V>> itemTimeout;
      final Publisher<? extends T> other;
      final String timeoutDescription;
      Subscription s;
      volatile FluxTimeout.IndexedCancellable timeout;
      static final AtomicReferenceFieldUpdater<FluxTimeout.TimeoutMainSubscriber, FluxTimeout.IndexedCancellable> TIMEOUT = AtomicReferenceFieldUpdater.newUpdater(
         FluxTimeout.TimeoutMainSubscriber.class, FluxTimeout.IndexedCancellable.class, "timeout"
      );
      volatile long index;
      static final AtomicLongFieldUpdater<FluxTimeout.TimeoutMainSubscriber> INDEX = AtomicLongFieldUpdater.newUpdater(
         FluxTimeout.TimeoutMainSubscriber.class, "index"
      );

      TimeoutMainSubscriber(
         CoreSubscriber<? super T> actual,
         Publisher<?> firstTimeout,
         Function<? super T, ? extends Publisher<V>> itemTimeout,
         @Nullable Publisher<? extends T> other,
         @Nullable String timeoutDescription
      ) {
         super(Operators.serialize(actual));
         this.itemTimeout = itemTimeout;
         this.other = other;
         this.timeoutDescription = timeoutDescription;
         this.firstTimeout = firstTimeout;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.set(s);
            FluxTimeout.TimeoutTimeoutSubscriber timeoutSubscriber = new FluxTimeout.TimeoutTimeoutSubscriber(this, 0L);
            this.timeout = timeoutSubscriber;
            this.actual.onSubscribe(this);
            this.firstTimeout.subscribe(timeoutSubscriber);
         }

      }

      @Override
      protected boolean shouldCancelCurrent() {
         return true;
      }

      @Override
      public void onNext(T t) {
         this.timeout.cancel();
         long idx = this.index;
         if (idx == Long.MIN_VALUE) {
            this.s.cancel();
            Operators.onNextDropped(t, this.actual.currentContext());
         } else if (!INDEX.compareAndSet(this, idx, idx + 1L)) {
            this.s.cancel();
            Operators.onNextDropped(t, this.actual.currentContext());
         } else {
            this.actual.onNext(t);
            this.producedOne();

            Publisher<? extends V> p;
            try {
               p = (Publisher)Objects.requireNonNull(this.itemTimeout.apply(t), "The itemTimeout returned a null Publisher");
            } catch (Throwable var6) {
               this.actual.onError(Operators.onOperatorError(this, var6, t, this.actual.currentContext()));
               return;
            }

            FluxTimeout.TimeoutTimeoutSubscriber ts = new FluxTimeout.TimeoutTimeoutSubscriber(this, idx + 1L);
            if (this.setTimeout(ts)) {
               p.subscribe(ts);
            }
         }
      }

      @Override
      public void onError(Throwable t) {
         long idx = this.index;
         if (idx == Long.MIN_VALUE) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else if (!INDEX.compareAndSet(this, idx, Long.MIN_VALUE)) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.cancelTimeout();
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         long idx = this.index;
         if (idx != Long.MIN_VALUE) {
            if (INDEX.compareAndSet(this, idx, Long.MIN_VALUE)) {
               this.cancelTimeout();
               this.actual.onComplete();
            }
         }
      }

      void cancelTimeout() {
         FluxTimeout.IndexedCancellable s = this.timeout;
         if (s != FluxTimeout.CancelledIndexedCancellable.INSTANCE) {
            s = (FluxTimeout.IndexedCancellable)TIMEOUT.getAndSet(this, FluxTimeout.CancelledIndexedCancellable.INSTANCE);
            if (s != null && s != FluxTimeout.CancelledIndexedCancellable.INSTANCE) {
               s.cancel();
            }
         }

      }

      @Override
      public void cancel() {
         this.index = Long.MIN_VALUE;
         this.cancelTimeout();
         super.cancel();
      }

      boolean setTimeout(FluxTimeout.IndexedCancellable newTimeout) {
         FluxTimeout.IndexedCancellable currentTimeout;
         do {
            currentTimeout = this.timeout;
            if (currentTimeout == FluxTimeout.CancelledIndexedCancellable.INSTANCE) {
               newTimeout.cancel();
               return false;
            }

            if (currentTimeout != null && currentTimeout.index() >= newTimeout.index()) {
               newTimeout.cancel();
               return false;
            }
         } while(!TIMEOUT.compareAndSet(this, currentTimeout, newTimeout));

         if (currentTimeout != null) {
            currentTimeout.cancel();
         }

         return true;
      }

      void doTimeout(long i) {
         if (this.index == i && INDEX.compareAndSet(this, i, Long.MIN_VALUE)) {
            this.handleTimeout();
         }

      }

      void doError(long i, Throwable e) {
         if (this.index == i && INDEX.compareAndSet(this, i, Long.MIN_VALUE)) {
            super.cancel();
            this.actual.onError(e);
         }

      }

      void handleTimeout() {
         if (this.other == null) {
            super.cancel();
            this.actual
               .onError(
                  new TimeoutException(
                     "Did not observe any item or terminal signal within " + this.timeoutDescription + " (and no fallback has been configured)"
                  )
               );
         } else {
            this.set(Operators.emptySubscription());
            this.other.subscribe(new FluxTimeout.TimeoutOtherSubscriber<>(this.actual, this));
         }

      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
      }
   }

   static final class TimeoutOtherSubscriber<T> implements InnerConsumer<T> {
      final CoreSubscriber<? super T> actual;
      final Operators.MultiSubscriptionSubscriber<T, T> arbiter;

      TimeoutOtherSubscriber(CoreSubscriber<? super T> actual, Operators.MultiSubscriptionSubscriber<T, T> arbiter) {
         this.actual = actual;
         this.arbiter = arbiter;
      }

      @Override
      public Context currentContext() {
         return this.actual.currentContext();
      }

      @Override
      public void onSubscribe(Subscription s) {
         this.arbiter.set(s);
      }

      @Override
      public void onNext(T t) {
         this.actual.onNext(t);
      }

      @Override
      public void onError(Throwable t) {
         this.actual.onError(t);
      }

      @Override
      public void onComplete() {
         this.actual.onComplete();
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   static final class TimeoutTimeoutSubscriber implements InnerConsumer<Object>, FluxTimeout.IndexedCancellable {
      final FluxTimeout.TimeoutMainSubscriber<?, ?> main;
      final long index;
      volatile Subscription s;
      static final AtomicReferenceFieldUpdater<FluxTimeout.TimeoutTimeoutSubscriber, Subscription> S = AtomicReferenceFieldUpdater.newUpdater(
         FluxTimeout.TimeoutTimeoutSubscriber.class, Subscription.class, "s"
      );

      TimeoutTimeoutSubscriber(FluxTimeout.TimeoutMainSubscriber<?, ?> main, long index) {
         this.main = main;
         this.index = index;
      }

      @Override
      public Context currentContext() {
         return this.main.currentContext();
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (!S.compareAndSet(this, null, s)) {
            s.cancel();
            if (this.s != Operators.cancelledSubscription()) {
               Operators.reportSubscriptionSet();
            }
         } else {
            s.request(Long.MAX_VALUE);
         }

      }

      @Override
      public void onNext(Object t) {
         this.s.cancel();
         this.main.doTimeout(this.index);
      }

      @Override
      public void onError(Throwable t) {
         this.main.doError(this.index, t);
      }

      @Override
      public void onComplete() {
         this.main.doTimeout(this.index);
      }

      @Override
      public void cancel() {
         Subscription a = this.s;
         if (a != Operators.cancelledSubscription()) {
            a = (Subscription)S.getAndSet(this, Operators.cancelledSubscription());
            if (a != null && a != Operators.cancelledSubscription()) {
               a.cancel();
            }
         }

      }

      @Override
      public long index() {
         return this.index;
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }
}
