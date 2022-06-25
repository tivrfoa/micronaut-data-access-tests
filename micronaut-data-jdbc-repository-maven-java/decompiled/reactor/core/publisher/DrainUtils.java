package reactor.core.publisher;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.function.BooleanSupplier;
import org.reactivestreams.Subscriber;
import reactor.core.CoreSubscriber;
import reactor.util.annotation.Nullable;

abstract class DrainUtils {
   static final long COMPLETED_MASK = Long.MIN_VALUE;
   static final long REQUESTED_MASK = Long.MAX_VALUE;

   static <T, F> boolean postCompleteRequest(
      long n, Subscriber<? super T> actual, Queue<T> queue, AtomicLongFieldUpdater<F> field, F instance, BooleanSupplier isCancelled
   ) {
      long r;
      long u;
      do {
         r = field.get(instance);
         long r0 = r & Long.MAX_VALUE;
         u = r & Long.MIN_VALUE | Operators.addCap(r0, n);
      } while(!field.compareAndSet(instance, r, u));

      if (r == Long.MIN_VALUE) {
         postCompleteDrain(n | Long.MIN_VALUE, actual, queue, field, instance, isCancelled);
         return true;
      } else {
         return false;
      }
   }

   static <T, F> boolean postCompleteDrain(
      long n, Subscriber<? super T> actual, Queue<T> queue, AtomicLongFieldUpdater<F> field, F instance, BooleanSupplier isCancelled
   ) {
      long e = n & Long.MIN_VALUE;

      while(true) {
         while(e == n) {
            if (isCancelled.getAsBoolean()) {
               return true;
            }

            if (queue.isEmpty()) {
               actual.onComplete();
               return true;
            }

            n = field.get(instance);
            if (n == e) {
               n = field.addAndGet(instance, -(e & Long.MAX_VALUE));
               if ((n & Long.MAX_VALUE) == 0L) {
                  return false;
               }

               e = n & Long.MIN_VALUE;
            }
         }

         if (isCancelled.getAsBoolean()) {
            return true;
         }

         T t = (T)queue.poll();
         if (t == null) {
            actual.onComplete();
            return true;
         }

         actual.onNext(t);
         ++e;
      }
   }

   public static <T, F> void postComplete(
      CoreSubscriber<? super T> actual, Queue<T> queue, AtomicLongFieldUpdater<F> field, F instance, BooleanSupplier isCancelled
   ) {
      if (queue.isEmpty()) {
         actual.onComplete();
      } else if (!postCompleteDrain(field.get(instance), actual, queue, field, instance, isCancelled)) {
         long r;
         long u;
         do {
            r = field.get(instance);
            if ((r & Long.MIN_VALUE) != 0L) {
               return;
            }

            u = r | Long.MIN_VALUE;
         } while(!field.compareAndSet(instance, r, u));

         if (r != 0L) {
            postCompleteDrain(u, actual, queue, field, instance, isCancelled);
         }

      }
   }

   public static <T, F> boolean postCompleteRequestDelayError(
      long n, Subscriber<? super T> actual, Queue<T> queue, AtomicLongFieldUpdater<F> field, F instance, BooleanSupplier isCancelled, Throwable error
   ) {
      long r;
      long u;
      do {
         r = field.get(instance);
         long r0 = r & Long.MAX_VALUE;
         u = r & Long.MIN_VALUE | Operators.addCap(r0, n);
      } while(!field.compareAndSet(instance, r, u));

      if (r == Long.MIN_VALUE) {
         postCompleteDrainDelayError(n | Long.MIN_VALUE, actual, queue, field, instance, isCancelled, error);
         return true;
      } else {
         return false;
      }
   }

   static <T, F> boolean postCompleteDrainDelayError(
      long n, Subscriber<? super T> actual, Queue<T> queue, AtomicLongFieldUpdater<F> field, F instance, BooleanSupplier isCancelled, @Nullable Throwable error
   ) {
      long e = n & Long.MIN_VALUE;

      while(true) {
         while(e == n) {
            if (isCancelled.getAsBoolean()) {
               return true;
            }

            if (queue.isEmpty()) {
               if (error == null) {
                  actual.onComplete();
               } else {
                  actual.onError(error);
               }

               return true;
            }

            n = field.get(instance);
            if (n == e) {
               n = field.addAndGet(instance, -(e & Long.MAX_VALUE));
               if ((n & Long.MAX_VALUE) == 0L) {
                  return false;
               }

               e = n & Long.MIN_VALUE;
            }
         }

         if (isCancelled.getAsBoolean()) {
            return true;
         }

         T t = (T)queue.poll();
         if (t == null) {
            if (error == null) {
               actual.onComplete();
            } else {
               actual.onError(error);
            }

            return true;
         }

         actual.onNext(t);
         ++e;
      }
   }

   public static <T, F> void postCompleteDelayError(
      CoreSubscriber<? super T> actual, Queue<T> queue, AtomicLongFieldUpdater<F> field, F instance, BooleanSupplier isCancelled, @Nullable Throwable error
   ) {
      if (queue.isEmpty()) {
         if (error == null) {
            actual.onComplete();
         } else {
            actual.onError(error);
         }

      } else if (!postCompleteDrainDelayError(field.get(instance), actual, queue, field, instance, isCancelled, error)) {
         long r;
         long u;
         do {
            r = field.get(instance);
            if ((r & Long.MIN_VALUE) != 0L) {
               return;
            }

            u = r | Long.MIN_VALUE;
         } while(!field.compareAndSet(instance, r, u));

         if (r != 0L) {
            postCompleteDrainDelayError(u, actual, queue, field, instance, isCancelled, error);
         }

      }
   }
}
