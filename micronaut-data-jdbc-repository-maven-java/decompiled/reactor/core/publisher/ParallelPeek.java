package reactor.core.publisher;

import java.util.function.Consumer;
import java.util.function.LongConsumer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class ParallelPeek<T> extends ParallelFlux<T> implements SignalPeek<T> {
   final ParallelFlux<T> source;
   final Consumer<? super T> onNext;
   final Consumer<? super T> onAfterNext;
   final Consumer<? super Throwable> onError;
   final Runnable onComplete;
   final Runnable onAfterTerminated;
   final Consumer<? super Subscription> onSubscribe;
   final LongConsumer onRequest;
   final Runnable onCancel;

   ParallelPeek(
      ParallelFlux<T> source,
      @Nullable Consumer<? super T> onNext,
      @Nullable Consumer<? super T> onAfterNext,
      @Nullable Consumer<? super Throwable> onError,
      @Nullable Runnable onComplete,
      @Nullable Runnable onAfterTerminated,
      @Nullable Consumer<? super Subscription> onSubscribe,
      @Nullable LongConsumer onRequest,
      @Nullable Runnable onCancel
   ) {
      this.source = source;
      this.onNext = onNext;
      this.onAfterNext = onAfterNext;
      this.onError = onError;
      this.onComplete = onComplete;
      this.onAfterTerminated = onAfterTerminated;
      this.onSubscribe = onSubscribe;
      this.onRequest = onRequest;
      this.onCancel = onCancel;
   }

   @Override
   public void subscribe(CoreSubscriber<? super T>[] subscribers) {
      if (this.validate(subscribers)) {
         int n = subscribers.length;
         CoreSubscriber<? super T>[] parents = new CoreSubscriber[n];
         boolean conditional = subscribers[0] instanceof Fuseable.ConditionalSubscriber;

         for(int i = 0; i < n; ++i) {
            if (conditional) {
               parents[i] = new FluxPeekFuseable.PeekConditionalSubscriber<>((Fuseable.ConditionalSubscriber<? super T>)subscribers[i], this);
            } else {
               parents[i] = new FluxPeek.PeekSubscriber<>(subscribers[i], this);
            }
         }

         this.source.subscribe(parents);
      }
   }

   @Override
   public int parallelism() {
      return this.source.parallelism();
   }

   @Override
   public int getPrefetch() {
      return this.source.getPrefetch();
   }

   @Nullable
   @Override
   public Consumer<? super Subscription> onSubscribeCall() {
      return this.onSubscribe;
   }

   @Nullable
   @Override
   public Consumer<? super T> onNextCall() {
      return this.onNext;
   }

   @Nullable
   @Override
   public Consumer<? super Throwable> onErrorCall() {
      return this.onError;
   }

   @Nullable
   @Override
   public Runnable onCompleteCall() {
      return this.onComplete;
   }

   @Override
   public Runnable onAfterTerminateCall() {
      return this.onAfterTerminated;
   }

   @Nullable
   @Override
   public LongConsumer onRequestCall() {
      return this.onRequest;
   }

   @Nullable
   @Override
   public Runnable onCancelCall() {
      return this.onCancel;
   }

   @Nullable
   @Override
   public Consumer<? super T> onAfterNextCall() {
      return this.onAfterNext;
   }

   @Nullable
   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else if (key == Scannable.Attr.PREFETCH) {
         return this.getPrefetch();
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }
}
