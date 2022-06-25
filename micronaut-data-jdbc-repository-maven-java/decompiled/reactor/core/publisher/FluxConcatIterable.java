package reactor.core.publisher;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;

final class FluxConcatIterable<T> extends Flux<T> implements SourceProducer<T> {
   final Iterable<? extends Publisher<? extends T>> iterable;

   FluxConcatIterable(Iterable<? extends Publisher<? extends T>> iterable) {
      this.iterable = (Iterable)Objects.requireNonNull(iterable, "iterable");
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      Iterator<? extends Publisher<? extends T>> it;
      try {
         it = (Iterator)Objects.requireNonNull(this.iterable.iterator(), "The Iterator returned is null");
      } catch (Throwable var4) {
         Operators.error(actual, Operators.onOperatorError(var4, actual.currentContext()));
         return;
      }

      FluxConcatIterable.ConcatIterableSubscriber<T> parent = new FluxConcatIterable.ConcatIterableSubscriber<>(actual, it);
      actual.onSubscribe(parent);
      if (!parent.isCancelled()) {
         parent.onComplete();
      }

   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }

   static final class ConcatIterableSubscriber<T> extends Operators.MultiSubscriptionSubscriber<T, T> {
      final Iterator<? extends Publisher<? extends T>> it;
      volatile int wip;
      static final AtomicIntegerFieldUpdater<FluxConcatIterable.ConcatIterableSubscriber> WIP = AtomicIntegerFieldUpdater.newUpdater(
         FluxConcatIterable.ConcatIterableSubscriber.class, "wip"
      );
      long produced;

      ConcatIterableSubscriber(CoreSubscriber<? super T> actual, Iterator<? extends Publisher<? extends T>> it) {
         super(actual);
         this.it = it;
      }

      @Override
      public void onNext(T t) {
         ++this.produced;
         this.actual.onNext(t);
      }

      @Override
      public void onComplete() {
         if (WIP.getAndIncrement(this) == 0) {
            Iterator<? extends Publisher<? extends T>> a = this.it;

            do {
               if (this.isCancelled()) {
                  return;
               }

               boolean b;
               try {
                  b = a.hasNext();
               } catch (Throwable var7) {
                  this.onError(Operators.onOperatorError(this, var7, this.actual.currentContext()));
                  return;
               }

               if (this.isCancelled()) {
                  return;
               }

               if (!b) {
                  this.actual.onComplete();
                  return;
               }

               Publisher<? extends T> p;
               try {
                  p = (Publisher)Objects.requireNonNull(this.it.next(), "The Publisher returned by the iterator is null");
               } catch (Throwable var6) {
                  this.actual.onError(Operators.onOperatorError(this, var6, this.actual.currentContext()));
                  return;
               }

               if (this.isCancelled()) {
                  return;
               }

               long c = this.produced;
               if (c != 0L) {
                  this.produced = 0L;
                  this.produced(c);
               }

               p.subscribe(this);
               if (this.isCancelled()) {
                  return;
               }
            } while(WIP.decrementAndGet(this) != 0);
         }

      }

      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
      }
   }
}
