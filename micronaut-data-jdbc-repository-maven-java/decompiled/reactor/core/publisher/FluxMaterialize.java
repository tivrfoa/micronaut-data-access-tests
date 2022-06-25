package reactor.core.publisher;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.BooleanSupplier;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class FluxMaterialize<T> extends InternalFluxOperator<T, Signal<T>> {
   FluxMaterialize(Flux<T> source) {
      super(source);
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super Signal<T>> actual) {
      return new FluxMaterialize.MaterializeSubscriber<>(actual);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class MaterializeSubscriber<T> extends AbstractQueue<Signal<T>> implements InnerOperator<T, Signal<T>>, BooleanSupplier {
      final CoreSubscriber<? super Signal<T>> actual;
      final Context cachedContext;
      Signal<T> terminalSignal;
      volatile boolean cancelled;
      volatile long requested;
      static final AtomicLongFieldUpdater<FluxMaterialize.MaterializeSubscriber> REQUESTED = AtomicLongFieldUpdater.newUpdater(
         FluxMaterialize.MaterializeSubscriber.class, "requested"
      );
      long produced;
      Subscription s;
      static final Signal empty = new ImmutableSignal(Context.empty(), SignalType.ON_NEXT, (T)null, null, null);

      MaterializeSubscriber(CoreSubscriber<? super Signal<T>> subscriber) {
         this.actual = subscriber;
         this.cachedContext = this.actual.currentContext();
      }

      @Override
      public Context currentContext() {
         return this.cachedContext;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.terminalSignal != null;
         } else if (key == Scannable.Attr.ERROR) {
            return this.terminalSignal != null ? this.terminalSignal.getThrowable() : null;
         } else if (key == Scannable.Attr.CANCELLED) {
            return this.getAsBoolean();
         } else if (key == Scannable.Attr.REQUESTED_FROM_DOWNSTREAM) {
            return this.requested;
         } else if (key == Scannable.Attr.BUFFERED) {
            return this.size();
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public CoreSubscriber<? super Signal<T>> actual() {
         return this.actual;
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T ev) {
         if (this.terminalSignal != null) {
            Operators.onNextDropped(ev, this.cachedContext);
         } else {
            ++this.produced;
            this.actual.onNext(Signal.next(ev, this.cachedContext));
         }
      }

      @Override
      public void onError(Throwable ev) {
         if (this.terminalSignal != null) {
            Operators.onErrorDropped(ev, this.cachedContext);
         } else {
            this.terminalSignal = Signal.error(ev, this.cachedContext);
            long p = this.produced;
            if (p != 0L) {
               Operators.addCap(REQUESTED, this, -p);
            }

            DrainUtils.postComplete(this.actual, this, REQUESTED, this, this);
         }
      }

      @Override
      public void onComplete() {
         if (this.terminalSignal == null) {
            this.terminalSignal = Signal.complete(this.cachedContext);
            long p = this.produced;
            if (p != 0L) {
               Operators.addCap(REQUESTED, this, -p);
            }

            DrainUtils.postComplete(this.actual, this, REQUESTED, this, this);
         }
      }

      @Override
      public void request(long n) {
         if (Operators.validate(n) && !DrainUtils.postCompleteRequest(n, this.actual, this, REQUESTED, this, this)) {
            this.s.request(n);
         }

      }

      @Override
      public void cancel() {
         if (!this.cancelled) {
            this.cancelled = true;
            this.s.cancel();
         }
      }

      public boolean getAsBoolean() {
         return this.cancelled;
      }

      public boolean offer(Signal<T> e) {
         throw new UnsupportedOperationException();
      }

      @Nullable
      public Signal<T> poll() {
         Signal<T> v = this.terminalSignal;
         if (v != null && v != empty) {
            this.terminalSignal = empty;
            return v;
         } else {
            return null;
         }
      }

      @Nullable
      public Signal<T> peek() {
         return empty == this.terminalSignal ? null : this.terminalSignal;
      }

      public Iterator<Signal<T>> iterator() {
         throw new UnsupportedOperationException();
      }

      public int size() {
         return this.terminalSignal != null && this.terminalSignal != empty ? 1 : 0;
      }

      public String toString() {
         return "MaterializeSubscriber";
      }
   }
}
