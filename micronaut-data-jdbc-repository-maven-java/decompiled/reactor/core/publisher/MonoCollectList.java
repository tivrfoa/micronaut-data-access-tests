package reactor.core.publisher;

import java.util.ArrayList;
import java.util.List;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.Fuseable;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoCollectList<T> extends MonoFromFluxOperator<T, List<T>> implements Fuseable {
   MonoCollectList(Flux<? extends T> source) {
      super(source);
   }

   @Override
   public CoreSubscriber<? super T> subscribeOrReturn(CoreSubscriber<? super List<T>> actual) {
      return new MonoCollectList.MonoCollectListSubscriber<>(actual);
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
   }

   static final class MonoCollectListSubscriber<T> extends Operators.MonoSubscriber<T, List<T>> {
      List<T> list = new ArrayList();
      Subscription s;
      boolean done;

      MonoCollectListSubscriber(CoreSubscriber<? super List<T>> actual) {
         super(actual);
      }

      @Nullable
      @Override
      public Object scanUnsafe(Scannable.Attr key) {
         if (key == Scannable.Attr.PARENT) {
            return this.s;
         } else if (key == Scannable.Attr.TERMINATED) {
            return this.done;
         } else {
            return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : super.scanUnsafe(key);
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
            synchronized(this) {
               List<T> l = this.list;
               if (l != null) {
                  l.add(t);
                  return;
               }
            }

            Operators.onDiscard(t, this.actual.currentContext());
         }
      }

      @Override
      public void onError(Throwable t) {
         if (this.done) {
            Operators.onErrorDropped(t, this.actual.currentContext());
         } else {
            this.done = true;
            List<T> l;
            synchronized(this) {
               l = this.list;
               this.list = null;
            }

            this.discard(l);
            this.actual.onError(t);
         }
      }

      @Override
      public void onComplete() {
         if (!this.done) {
            this.done = true;
            List<T> l;
            synchronized(this) {
               l = this.list;
               this.list = null;
            }

            if (l != null) {
               this.complete(l);
            }

         }
      }

      protected void discard(List<T> v) {
         Operators.onDiscardMultiple(v, this.actual.currentContext());
      }

      @Override
      public void cancel() {
         List<T> l;
         synchronized(this) {
            int state = STATE.getAndSet(this, 4);
            if (state != 4) {
               this.s.cancel();
            }

            if (state <= 2) {
               l = this.list;
               this.value = null;
               this.list = null;
            } else {
               l = null;
            }
         }

         if (l != null) {
            this.discard(l);
         }

      }
   }
}
