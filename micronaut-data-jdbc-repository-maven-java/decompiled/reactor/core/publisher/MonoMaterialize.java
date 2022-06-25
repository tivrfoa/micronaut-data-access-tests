package reactor.core.publisher;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoMaterialize<T> extends InternalMonoOperator<T, Signal<T>> {
   MonoMaterialize(Mono<T> source) {
      super(source);
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super Signal<T>> actual) {
      return new MonoMaterialize.MaterializeSubscriber<>(actual);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class MaterializeSubscriber<T> implements InnerOperator<T, Signal<T>> {
      final CoreSubscriber<? super Signal<T>> actual;
      boolean alreadyReceivedSignalFromSource;
      Subscription s;
      volatile boolean requested;
      @Nullable
      volatile Signal<T> signalToReplayUponFirstRequest;

      MaterializeSubscriber(CoreSubscriber<? super Signal<T>> actual) {
         this.actual = actual;
      }

      @Override
      public CoreSubscriber<? super Signal<T>> actual() {
         return this.actual;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : InnerOperator.super.scanUnsafe(key);
         }
      }

      @Override
      public void onSubscribe(Subscription s) {
         if (Operators.validate(this.s, s)) {
            this.s = s;
            this.actual.onSubscribe(this);
         }

      }

      @Override
      public void onNext(T t) {
         if (!this.alreadyReceivedSignalFromSource && this.requested) {
            this.alreadyReceivedSignalFromSource = true;
            Signal<T> signal = Signal.next(t, this.currentContext());
            this.actual.onNext(signal);
            this.actual.onComplete();
         } else {
            Operators.onNextDropped(t, this.currentContext());
         }
      }

      @Override
      public void onError(Throwable throwable) {
         if (this.alreadyReceivedSignalFromSource) {
            Operators.onErrorDropped(throwable, this.currentContext());
         } else {
            this.alreadyReceivedSignalFromSource = true;
            this.signalToReplayUponFirstRequest = Signal.error(throwable, this.currentContext());
            this.drain();
         }
      }

      @Override
      public void onComplete() {
         if (!this.alreadyReceivedSignalFromSource) {
            this.alreadyReceivedSignalFromSource = true;
            this.signalToReplayUponFirstRequest = Signal.complete(this.currentContext());
            this.drain();
         }
      }

      boolean drain() {
         Signal<T> signal = this.signalToReplayUponFirstRequest;
         if (signal != null && this.requested) {
            this.actual.onNext(signal);
            this.actual.onComplete();
            this.signalToReplayUponFirstRequest = null;
            return true;
         } else {
            return false;
         }
      }

      @Override
      public void request(long l) {
         if (!this.requested && Operators.validate(l)) {
            this.requested = true;
            if (this.drain()) {
               return;
            }

            this.s.request(l);
         }

      }

      @Override
      public void cancel() {
         this.s.cancel();
      }
   }
}
