package reactor.core.publisher;

import java.util.Collection;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collector;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;
import reactor.util.context.Context;

final class MonoStreamCollector<T, A, R> extends MonoFromFluxOperator<T, R> implements Fuseable {
   final Collector<? super T, A, ? extends R> collector;

   MonoStreamCollector(Flux<? extends T> source, Collector<? super T, A, ? extends R> collector) {
      super(source);
      this.collector = (Collector)Objects.requireNonNull(collector, "collector");
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super R> actual) {
      A container = (A)this.collector.supplier().get();
      BiConsumer<? super A, ? super T> accumulator = this.collector.accumulator();
      Function<? super A, ? extends R> finisher = this.collector.finisher();
      return new MonoStreamCollector.StreamCollectorSubscriber<>(actual, container, accumulator, finisher);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class StreamCollectorSubscriber<T, A, R> extends Operators.MonoSubscriber<T, R> {
      final BiConsumer<? super A, ? super T> accumulator;
      final Function<? super A, ? extends R> finisher;
      A container;
      Subscription s;
      boolean done;

      StreamCollectorSubscriber(
         CoreSubscriber<? super R> actual, A container, BiConsumer<? super A, ? super T> accumulator, Function<? super A, ? extends R> finisher
      ) {
         super(actual);
         this.container = container;
         this.accumulator = accumulator;
         this.finisher = finisher;
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
         }
      }

      protected void discardIntermediateContainer(A a) {
         Context ctx = this.actual.currentContext();
         if (a instanceof Collection) {
            Operators.onDiscardMultiple((Collection<?>)a, ctx);
         } else {
            Operators.onDiscard(a, ctx);
         }

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
            try {
               this.accumulator.accept(this.container, t);
            } catch (Throwable var4) {
               Context ctx = this.actual.currentContext();
               Operators.onDiscard(t, ctx);
               this.onError(Operators.onOperatorError(this.s, var4, t, ctx));
            }

         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            this.discardIntermediateContainer(this.container);
            this.container = null;
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            A a = this.container;
            this.container = null;

            R r;
            try {
               r = (R)this.finisher.apply(a);
            } catch (Throwable var4) {
               this.discardIntermediateContainer(a);
               this.actual.onError(Operators.onOperatorError(var4, this.actual.currentContext()));
               return;
            }

            if (r == null) {
               this.actual.onError(Operators.onOperatorError(new NullPointerException("Collector returned null"), this.actual.currentContext()));
            } else {
               this.complete(r);
            }
         }
      }

      @Override
      public void cancel() {
         super.cancel();
         this.s.cancel();
         this.discardIntermediateContainer(this.container);
         this.container = null;
      }
   }
}
