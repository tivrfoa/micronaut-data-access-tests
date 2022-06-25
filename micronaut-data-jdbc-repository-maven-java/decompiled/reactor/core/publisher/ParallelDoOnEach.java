package reactor.core.publisher;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class ParallelDoOnEach<T> extends ParallelFlux<T> implements Scannable {
   final ParallelFlux<T> source;
   final BiConsumer<Context, ? super T> onNext;
   final BiConsumer<Context, ? super Throwable> onError;
   final Consumer<Context> onComplete;

   ParallelDoOnEach(
      ParallelFlux<T> source,
      @Nullable BiConsumer<Context, ? super T> onNext,
      @Nullable BiConsumer<Context, ? super Throwable> onError,
      @Nullable Consumer<Context> onComplete
   ) {
      this.source = source;
      this.onNext = onNext;
      this.onError = onError;
      this.onComplete = onComplete;
   }

   @Override
   public void subscribe(CoreSubscriber<? super T>[] subscribers) {
      if (this.validate(subscribers)) {
         int n = subscribers.length;
         CoreSubscriber<? super T>[] parents = new CoreSubscriber[n];
         boolean conditional = subscribers[0] instanceof Fuseable.ConditionalSubscriber;

         for(int i = 0; i < n; ++i) {
            CoreSubscriber<? super T> subscriber = subscribers[i];
            SignalPeek<T> signalPeek = new ParallelDoOnEach.DoOnEachSignalPeek(subscriber.currentContext());
            if (conditional) {
               parents[i] = new FluxPeekFuseable.PeekConditionalSubscriber<>((Fuseable.ConditionalSubscriber<? super T>)subscriber, signalPeek);
            } else {
               parents[i] = new FluxPeek.PeekSubscriber<>(subscriber, signalPeek);
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
   public Object scanUnsafe(Scannable.Attr key) {
      if (key == Scannable.Attr.PARENT) {
         return this.source;
      } else if (key == Scannable.Attr.PREFETCH) {
         return this.getPrefetch();
      } else {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
      }
   }

   private class DoOnEachSignalPeek implements SignalPeek<T> {
      Consumer<? super T> onNextCall;
      Consumer<? super Throwable> onErrorCall;
      Runnable onCompleteCall;

      public DoOnEachSignalPeek(Context ctx) {
         this.onNextCall = ParallelDoOnEach.this.onNext != null ? v -> ParallelDoOnEach.this.onNext.accept(ctx, v) : null;
         this.onErrorCall = ParallelDoOnEach.this.onError != null ? e -> ParallelDoOnEach.this.onError.accept(ctx, e) : null;
         this.onCompleteCall = ParallelDoOnEach.this.onComplete != null ? () -> ParallelDoOnEach.this.onComplete.accept(ctx) : null;
      }

      @Override
      public Consumer<? super Subscription> onSubscribeCall() {
         return null;
      }

      @Override
      public Consumer<? super T> onNextCall() {
         return this.onNextCall;
      }

      @Override
      public Consumer<? super Throwable> onErrorCall() {
         return this.onErrorCall;
      }

      @Override
      public Runnable onCompleteCall() {
         return this.onCompleteCall;
      }

      @Override
      public Runnable onAfterTerminateCall() {
         return null;
      }

      @Override
      public LongConsumer onRequestCall() {
         return null;
      }

      @Override
      public Runnable onCancelCall() {
         return null;
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return null;
      }
   }
}
