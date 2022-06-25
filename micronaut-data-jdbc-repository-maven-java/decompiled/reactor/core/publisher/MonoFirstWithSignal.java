package reactor.core.publisher;

import java.util.Iterator;
import java.util.Objects;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoFirstWithSignal<T> extends Mono<T> implements SourceProducer<T> {
   final Mono<? extends T>[] array;
   final Iterable<? extends Mono<? extends T>> iterable;

   @SafeVarargs
   MonoFirstWithSignal(Mono<? extends T>... array) {
      this.array = (Mono[])Objects.requireNonNull(array, "array");
      this.iterable = null;
   }

   MonoFirstWithSignal(Iterable<? extends Mono<? extends T>> iterable) {
      this.array = null;
      this.iterable = (Iterable)Objects.requireNonNull(iterable);
   }

   @Nullable
   Mono<T> orAdditionalSource(Mono<? extends T> other) {
      if (this.array != null) {
         int n = this.array.length;
         Mono<? extends T>[] newArray = new Mono[n + 1];
         System.arraycopy(this.array, 0, newArray, 0, n);
         newArray[n] = other;
         return new MonoFirstWithSignal<>(newArray);
      } else {
         return null;
      }
   }

   @Override
   public void subscribe(CoreSubscriber<? super T> actual) {
      Publisher<? extends T>[] a = this.array;
      int n;
      if (a == null) {
         n = 0;
         a = new Publisher[8];

         Iterator<? extends Publisher<? extends T>> it;
         try {
            it = (Iterator)Objects.requireNonNull(this.iterable.iterator(), "The iterator returned is null");
         } catch (Throwable var10) {
            Operators.error(actual, Operators.onOperatorError(var10, actual.currentContext()));
            return;
         }

         while(true) {
            boolean b;
            try {
               b = it.hasNext();
            } catch (Throwable var8) {
               Operators.error(actual, Operators.onOperatorError(var8, actual.currentContext()));
               return;
            }

            if (!b) {
               break;
            }

            Publisher<? extends T> p;
            try {
               p = (Publisher)Objects.requireNonNull(it.next(), "The Publisher returned by the iterator is null");
            } catch (Throwable var9) {
               Operators.error(actual, Operators.onOperatorError(var9, actual.currentContext()));
               return;
            }

            if (n == a.length) {
               Publisher<? extends T>[] c = new Publisher[n + (n >> 2)];
               System.arraycopy(a, 0, c, 0, n);
               a = c;
            }

            a[n++] = p;
         }
      } else {
         n = a.length;
      }

      if (n == 0) {
         Operators.complete(actual);
      } else if (n == 1) {
         Publisher<? extends T> p = a[0];
         if (p == null) {
            Operators.error(actual, Operators.onOperatorError(new NullPointerException("The single source Publisher is null"), actual.currentContext()));
         } else {
            p.subscribe(actual);
         }

      } else {
         FluxFirstWithSignal.RaceCoordinator<T> coordinator = new FluxFirstWithSignal.RaceCoordinator<>(n);
         coordinator.subscribe(a, n, actual);
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }
}
