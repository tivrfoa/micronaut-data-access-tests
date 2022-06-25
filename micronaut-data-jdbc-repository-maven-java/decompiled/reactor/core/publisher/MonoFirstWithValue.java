package reactor.core.publisher;

import java.util.Iterator;
import java.util.Objects;
import org.reactivestreams.Publisher;
import reactor.core.CoreSubscriber;
import reactor.core.Scannable;
import reactor.util.annotation.Nullable;

final class MonoFirstWithValue<T> extends Mono<T> implements SourceProducer<T> {
   final Mono<? extends T>[] array;
   final Iterable<? extends Mono<? extends T>> iterable;

   private MonoFirstWithValue(Mono<? extends T>[] array) {
      this.array = (Mono[])Objects.requireNonNull(array, "array");
      this.iterable = null;
   }

   @SafeVarargs
   MonoFirstWithValue(Mono<? extends T> first, Mono<? extends T>... others) {
      Objects.requireNonNull(first, "first");
      Objects.requireNonNull(others, "others");
      Mono<? extends T>[] newArray = new Mono[others.length + 1];
      newArray[0] = first;
      System.arraycopy(others, 0, newArray, 1, others.length);
      this.array = newArray;
      this.iterable = null;
   }

   MonoFirstWithValue(Iterable<? extends Mono<? extends T>> iterable) {
      this.array = null;
      this.iterable = (Iterable)Objects.requireNonNull(iterable);
   }

   @Nullable
   @SafeVarargs
   final MonoFirstWithValue<T> firstValuedAdditionalSources(Mono<? extends T>... others) {
      Objects.requireNonNull(others, "others");
      if (others.length == 0) {
         return this;
      } else if (this.array == null) {
         return null;
      } else {
         int currentSize = this.array.length;
         int otherSize = others.length;
         Mono<? extends T>[] newArray = new Mono[currentSize + otherSize];
         System.arraycopy(this.array, 0, newArray, 0, currentSize);
         System.arraycopy(others, 0, newArray, currentSize, otherSize);
         return new MonoFirstWithValue<>(newArray);
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
         FluxFirstWithValue.RaceValuesCoordinator<T> coordinator = new FluxFirstWithValue.RaceValuesCoordinator<>(n);
         coordinator.subscribe(a, n, actual);
      }
   }

   @Override
   public Object scanUnsafe(Scannable.Attr key) {
      return key == Scannable.Attr.RUN_STYLE ? Scannable.Attr.RunStyle.SYNC : null;
   }
}
