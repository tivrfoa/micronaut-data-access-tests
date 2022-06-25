package reactor.core.publisher;

import java.util.function.Consumer;
import java.util.function.LongConsumer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoPeekFuseable<T> extends InternalMonoOperator<T, T> implements Fuseable, SignalPeek<T> {
   final Consumer<? super Subscription> onSubscribeCall;
   final Consumer<? super T> onNextCall;
   final LongConsumer onRequestCall;
   final Runnable onCancelCall;

   MonoPeekFuseable(
      Mono<? extends T> source,
      @Nullable Consumer<? super Subscription> onSubscribeCall,
      @Nullable Consumer<? super T> onNextCall,
      @Nullable LongConsumer onRequestCall,
      @Nullable Runnable onCancelCall
   ) {
      super(source);
      this.onSubscribeCall = onSubscribeCall;
      this.onNextCall = onNextCall;
      this.onRequestCall = onRequestCall;
      this.onCancelCall = onCancelCall;
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super T> actual) {
      return (CoreSubscriber<? super T>)(actual instanceof Fuseable.ConditionalSubscriber
         ? new FluxPeekFuseable.PeekFuseableConditionalSubscriber<>((Fuseable.ConditionalSubscriber<? super T>)actual, this)
         : new FluxPeekFuseable.PeekFuseableSubscriber<>(actual, this));
   }

   @Nullable
   @Override
   public Consumer<? super Subscription> onSubscribeCall() {
      return this.onSubscribeCall;
   }

   @Nullable
   @Override
   public Consumer<? super T> onNextCall() {
      return this.onNextCall;
   }

   @Nullable
   @Override
   public Consumer<? super Throwable> onErrorCall() {
      return null;
   }

   @Nullable
   @Override
   public Runnable onCompleteCall() {
      return null;
   }

   @Nullable
   @Override
   public Runnable onAfterTerminateCall() {
      return null;
   }

   @Nullable
   @Override
   public LongConsumer onRequestCall() {
      return this.onRequestCall;
   }

   @Nullable
   @Override
   public Runnable onCancelCall() {
      return this.onCancelCall;
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }
}
