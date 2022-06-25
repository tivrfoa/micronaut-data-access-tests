package reactor.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.Callable;
import org.reactivestreams.Subscription;
import reactor.util.annotation.Nullable;

public interface Fuseable {
   int NONE = 0;
   int SYNC = 1;
   int ASYNC = 2;
   int ANY = 3;
   int THREAD_BARRIER = 4;

   static String fusionModeName(int mode) {
      return fusionModeName(mode, false);
   }

   static String fusionModeName(int mode, boolean ignoreThreadBarrier) {
      int evaluated = mode;
      String threadBarrierSuffix = "";
      if (mode >= 0) {
         evaluated = mode & -5;
         if (!ignoreThreadBarrier && (mode & 4) == 4) {
            threadBarrierSuffix = "+THREAD_BARRIER";
         }
      }

      switch(evaluated) {
         case -1:
            return "Disabled";
         case 0:
            return "NONE" + threadBarrierSuffix;
         case 1:
            return "SYNC" + threadBarrierSuffix;
         case 2:
            return "ASYNC" + threadBarrierSuffix;
         default:
            return "Unknown(" + evaluated + ")" + threadBarrierSuffix;
      }
   }

   public interface ConditionalSubscriber<T> extends CoreSubscriber<T> {
      boolean tryOnNext(T var1);
   }

   public interface QueueSubscription<T> extends Queue<T>, Subscription {
      String NOT_SUPPORTED_MESSAGE = "Although QueueSubscription extends Queue it is purely internal and only guarantees support for poll/clear/size/isEmpty. Instances shouldn't be used/exposed as Queue outside of Reactor operators.";

      int requestFusion(int var1);

      @Nullable
      default T peek() {
         throw new UnsupportedOperationException(
            "Although QueueSubscription extends Queue it is purely internal and only guarantees support for poll/clear/size/isEmpty. Instances shouldn't be used/exposed as Queue outside of Reactor operators."
         );
      }

      default boolean add(@Nullable T t) {
         throw new UnsupportedOperationException(
            "Although QueueSubscription extends Queue it is purely internal and only guarantees support for poll/clear/size/isEmpty. Instances shouldn't be used/exposed as Queue outside of Reactor operators."
         );
      }

      default boolean offer(@Nullable T t) {
         throw new UnsupportedOperationException(
            "Although QueueSubscription extends Queue it is purely internal and only guarantees support for poll/clear/size/isEmpty. Instances shouldn't be used/exposed as Queue outside of Reactor operators."
         );
      }

      default T remove() {
         throw new UnsupportedOperationException(
            "Although QueueSubscription extends Queue it is purely internal and only guarantees support for poll/clear/size/isEmpty. Instances shouldn't be used/exposed as Queue outside of Reactor operators."
         );
      }

      default T element() {
         throw new UnsupportedOperationException(
            "Although QueueSubscription extends Queue it is purely internal and only guarantees support for poll/clear/size/isEmpty. Instances shouldn't be used/exposed as Queue outside of Reactor operators."
         );
      }

      default boolean contains(@Nullable Object o) {
         throw new UnsupportedOperationException(
            "Although QueueSubscription extends Queue it is purely internal and only guarantees support for poll/clear/size/isEmpty. Instances shouldn't be used/exposed as Queue outside of Reactor operators."
         );
      }

      default Iterator<T> iterator() {
         throw new UnsupportedOperationException(
            "Although QueueSubscription extends Queue it is purely internal and only guarantees support for poll/clear/size/isEmpty. Instances shouldn't be used/exposed as Queue outside of Reactor operators."
         );
      }

      default Object[] toArray() {
         throw new UnsupportedOperationException(
            "Although QueueSubscription extends Queue it is purely internal and only guarantees support for poll/clear/size/isEmpty. Instances shouldn't be used/exposed as Queue outside of Reactor operators."
         );
      }

      default <T1> T1[] toArray(T1[] a) {
         throw new UnsupportedOperationException(
            "Although QueueSubscription extends Queue it is purely internal and only guarantees support for poll/clear/size/isEmpty. Instances shouldn't be used/exposed as Queue outside of Reactor operators."
         );
      }

      default boolean remove(@Nullable Object o) {
         throw new UnsupportedOperationException(
            "Although QueueSubscription extends Queue it is purely internal and only guarantees support for poll/clear/size/isEmpty. Instances shouldn't be used/exposed as Queue outside of Reactor operators."
         );
      }

      default boolean containsAll(Collection<?> c) {
         throw new UnsupportedOperationException(
            "Although QueueSubscription extends Queue it is purely internal and only guarantees support for poll/clear/size/isEmpty. Instances shouldn't be used/exposed as Queue outside of Reactor operators."
         );
      }

      default boolean addAll(Collection<? extends T> c) {
         throw new UnsupportedOperationException(
            "Although QueueSubscription extends Queue it is purely internal and only guarantees support for poll/clear/size/isEmpty. Instances shouldn't be used/exposed as Queue outside of Reactor operators."
         );
      }

      default boolean removeAll(Collection<?> c) {
         throw new UnsupportedOperationException(
            "Although QueueSubscription extends Queue it is purely internal and only guarantees support for poll/clear/size/isEmpty. Instances shouldn't be used/exposed as Queue outside of Reactor operators."
         );
      }

      default boolean retainAll(Collection<?> c) {
         throw new UnsupportedOperationException(
            "Although QueueSubscription extends Queue it is purely internal and only guarantees support for poll/clear/size/isEmpty. Instances shouldn't be used/exposed as Queue outside of Reactor operators."
         );
      }
   }

   public interface ScalarCallable<T> extends Callable<T> {
   }

   public interface SynchronousSubscription<T> extends Fuseable.QueueSubscription<T> {
      @Override
      default int requestFusion(int requestedMode) {
         return (requestedMode & 1) != 0 ? 1 : 0;
      }
   }
}
