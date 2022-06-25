package reactor.core.publisher;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class ParallelCollect<T, C> extends ParallelFlux<C> implements Scannable, Fuseable {
   final ParallelFlux<? extends T> source;
   final Supplier<? extends C> initialCollection;
   final BiConsumer<? super C, ? super T> collector;

   ParallelCollect(ParallelFlux<? extends T> source, Supplier<? extends C> initialCollection, BiConsumer<? super C, ? super T> collector) {
      this.source = source;
      this.initialCollection = initialCollection;
      this.collector = collector;
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

   @Override
   public int getPrefetch() {
      return Integer.MAX_VALUE;
   }

   @Override
   public void subscribe(CoreSubscriber<? super C>[] subscribers) {
      if (this.validate(subscribers)) {
         int n = subscribers.length;
         CoreSubscriber<T>[] parents = new CoreSubscriber[n];

         for(int i = 0; i < n; ++i) {
            C initialValue;
            try {
               initialValue = (C)Objects.requireNonNull(this.initialCollection.get(), "The initialSupplier returned a null value");
            } catch (Throwable var7) {
               this.reportError(subscribers, Operators.onOperatorError(var7, subscribers[i].currentContext()));
               return;
            }

            parents[i] = new ParallelCollect.ParallelCollectSubscriber<>(subscribers[i], initialValue, this.collector);
         }

         this.source.subscribe(parents);
      }
   }

   void reportError(Subscriber<?>[] subscribers, Throwable ex) {
      for(Subscriber<?> s : subscribers) {
         Operators.error(s, ex);
      }

   }

   @Override
   public int parallelism() {
      return this.source.parallelism();
   }

   static final class ParallelCollectSubscriber<T, C> extends Operators.MonoSubscriber<T, C> {
      final BiConsumer<? super C, ? super T> collector;
      C collection;
      Subscription s;
      boolean done;

      ParallelCollectSubscriber(CoreSubscriber<? super C> subscriber, C initialValue, BiConsumer<? super C, ? super T> collector) {
         super(subscriber);
         this.collection = initialValue;
         this.collector = collector;
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
               this.collector.accept(this.collection, t);
            } catch (Throwable var3) {
               this.onError(Operators.onOperatorError(this, var3, t, this.actual.currentContext()));
            }

         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            this.collection = null;
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            C c = this.collection;
            this.collection = null;
            this.complete(c);
         }
      }

      @Override
      public void cancel() {
         super.cancel();
         this.s.cancel();
      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
      }
   }
}
