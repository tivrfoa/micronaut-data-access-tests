package reactor.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import reactor.util.annotation.Nullable;

public abstract class Exceptions {
   public static final String BACKPRESSURE_ERROR_QUEUE_FULL = "Queue is full: Reactive Streams source doesn't respect backpressure";
   public static final Throwable TERMINATED = new Exceptions.StaticThrowable("Operator has been terminated");
   static final RejectedExecutionException REJECTED_EXECUTION = new Exceptions.StaticRejectedExecutionException("Scheduler unavailable");
   static final RejectedExecutionException NOT_TIME_CAPABLE_REJECTED_EXECUTION = new Exceptions.StaticRejectedExecutionException(
      "Scheduler is not capable of time-based scheduling"
   );

   public static <T> boolean addThrowable(AtomicReferenceFieldUpdater<T, Throwable> field, T instance, Throwable exception) {
      Throwable current;
      Throwable update;
      do {
         current = (Throwable)field.get(instance);
         if (current == TERMINATED) {
            return false;
         }

         if (current instanceof Exceptions.CompositeException) {
            current.addSuppressed(exception);
            return true;
         }

         if (current == null) {
            update = exception;
         } else {
            update = multiple(current, exception);
         }
      } while(!field.compareAndSet(instance, current, update));

      return true;
   }

   public static RuntimeException multiple(Throwable... throwables) {
      Exceptions.CompositeException multiple = new Exceptions.CompositeException();
      if (throwables != null) {
         for(Throwable t : throwables) {
            multiple.addSuppressed(t);
         }
      }

      return multiple;
   }

   public static RuntimeException multiple(Iterable<Throwable> throwables) {
      Exceptions.CompositeException multiple = new Exceptions.CompositeException();
      if (throwables != null) {
         for(Throwable t : throwables) {
            multiple.addSuppressed(t);
         }
      }

      return multiple;
   }

   public static RuntimeException bubble(Throwable t) {
      throwIfFatal(t);
      return new Exceptions.BubblingException(t);
   }

   public static IllegalStateException duplicateOnSubscribeException() {
      return new IllegalStateException("Spec. Rule 2.12 - Subscriber.onSubscribe MUST NOT be called more than once (based on object equality)");
   }

   public static UnsupportedOperationException errorCallbackNotImplemented(Throwable cause) {
      Objects.requireNonNull(cause, "cause");
      return new Exceptions.ErrorCallbackNotImplemented(cause);
   }

   public static RuntimeException failWithCancel() {
      return new Exceptions.CancelException();
   }

   public static IllegalStateException failWithOverflow() {
      return new Exceptions.OverflowException("The receiver is overrun by more signals than expected (bounded queue...)");
   }

   public static IllegalStateException failWithOverflow(String message) {
      return new Exceptions.OverflowException(message);
   }

   public static RejectedExecutionException failWithRejected() {
      return REJECTED_EXECUTION;
   }

   public static RejectedExecutionException failWithRejectedNotTimeCapable() {
      return NOT_TIME_CAPABLE_REJECTED_EXECUTION;
   }

   public static RejectedExecutionException failWithRejected(Throwable cause) {
      return (RejectedExecutionException)(cause instanceof Exceptions.ReactorRejectedExecutionException
         ? (RejectedExecutionException)cause
         : new Exceptions.ReactorRejectedExecutionException("Scheduler unavailable", cause));
   }

   public static RejectedExecutionException failWithRejected(String message) {
      return new Exceptions.ReactorRejectedExecutionException(message);
   }

   public static RuntimeException retryExhausted(String message, @Nullable Throwable cause) {
      return cause == null ? new Exceptions.RetryExhaustedException(message) : new Exceptions.RetryExhaustedException(message, cause);
   }

   public static boolean isOverflow(@Nullable Throwable t) {
      return t instanceof Exceptions.OverflowException;
   }

   public static boolean isBubbling(@Nullable Throwable t) {
      return t instanceof Exceptions.BubblingException;
   }

   public static boolean isCancel(@Nullable Throwable t) {
      return t instanceof Exceptions.CancelException;
   }

   public static boolean isErrorCallbackNotImplemented(@Nullable Throwable t) {
      return t instanceof Exceptions.ErrorCallbackNotImplemented;
   }

   public static boolean isMultiple(@Nullable Throwable t) {
      return t instanceof Exceptions.CompositeException;
   }

   public static boolean isRetryExhausted(@Nullable Throwable t) {
      return t instanceof Exceptions.RetryExhaustedException;
   }

   public static boolean isTraceback(@Nullable Throwable t) {
      return t == null ? false : "reactor.core.publisher.FluxOnAssembly.OnAssemblyException".equals(t.getClass().getCanonicalName());
   }

   public static IllegalArgumentException nullOrNegativeRequestException(long elements) {
      return new IllegalArgumentException("Spec. Rule 3.9 - Cannot request a non strictly positive number: " + elements);
   }

   public static RuntimeException propagate(Throwable t) {
      throwIfFatal(t);
      return (RuntimeException)(t instanceof RuntimeException ? (RuntimeException)t : new Exceptions.ReactiveException(t));
   }

   @Nullable
   public static <T> Throwable terminate(AtomicReferenceFieldUpdater<T, Throwable> field, T instance) {
      Throwable current = (Throwable)field.get(instance);
      if (current != TERMINATED) {
         current = (Throwable)field.getAndSet(instance, TERMINATED);
      }

      return current;
   }

   public static void throwIfFatal(@Nullable Throwable t) {
      if (t instanceof Exceptions.BubblingException) {
         throw (Exceptions.BubblingException)t;
      } else if (t instanceof Exceptions.ErrorCallbackNotImplemented) {
         throw (Exceptions.ErrorCallbackNotImplemented)t;
      } else {
         throwIfJvmFatal(t);
      }
   }

   public static void throwIfJvmFatal(@Nullable Throwable t) {
      if (t instanceof VirtualMachineError) {
         throw (VirtualMachineError)t;
      } else if (t instanceof ThreadDeath) {
         throw (ThreadDeath)t;
      } else if (t instanceof LinkageError) {
         throw (LinkageError)t;
      }
   }

   public static Throwable unwrap(Throwable t) {
      Throwable _t = t;

      while(_t instanceof Exceptions.ReactiveException) {
         _t = _t.getCause();
      }

      return _t == null ? t : _t;
   }

   public static List<Throwable> unwrapMultiple(@Nullable Throwable potentialMultiple) {
      if (potentialMultiple == null) {
         return Collections.emptyList();
      } else {
         return isMultiple(potentialMultiple) ? Arrays.asList(potentialMultiple.getSuppressed()) : Collections.singletonList(potentialMultiple);
      }
   }

   public static List<Throwable> unwrapMultipleExcludingTracebacks(@Nullable Throwable potentialMultiple) {
      if (potentialMultiple == null) {
         return Collections.emptyList();
      } else if (isMultiple(potentialMultiple)) {
         Throwable[] suppressed = potentialMultiple.getSuppressed();
         List<Throwable> filtered = new ArrayList(suppressed.length);

         for(Throwable t : suppressed) {
            if (!isTraceback(t)) {
               filtered.add(t);
            }
         }

         return filtered;
      } else {
         return Collections.singletonList(potentialMultiple);
      }
   }

   public static final RuntimeException addSuppressed(RuntimeException original, Throwable suppressed) {
      if (original == suppressed) {
         return original;
      } else if (original != REJECTED_EXECUTION && original != NOT_TIME_CAPABLE_REJECTED_EXECUTION) {
         original.addSuppressed(suppressed);
         return original;
      } else {
         RejectedExecutionException ree = new RejectedExecutionException(original.getMessage());
         ree.addSuppressed(suppressed);
         return ree;
      }
   }

   public static final Throwable addSuppressed(Throwable original, Throwable suppressed) {
      if (original == suppressed) {
         return original;
      } else if (original == TERMINATED) {
         return original;
      } else if (original != REJECTED_EXECUTION && original != NOT_TIME_CAPABLE_REJECTED_EXECUTION) {
         original.addSuppressed(suppressed);
         return original;
      } else {
         RejectedExecutionException ree = new RejectedExecutionException(original.getMessage());
         ree.addSuppressed(suppressed);
         return ree;
      }
   }

   Exceptions() {
   }

   static class BubblingException extends Exceptions.ReactiveException {
      private static final long serialVersionUID = 2491425277432776142L;

      BubblingException(String message) {
         super(message);
      }

      BubblingException(Throwable cause) {
         super(cause);
      }
   }

   static final class CancelException extends Exceptions.BubblingException {
      private static final long serialVersionUID = 2491425227432776144L;

      CancelException() {
         super("The subscriber has denied dispatching");
      }
   }

   static class CompositeException extends Exceptions.ReactiveException {
      private static final long serialVersionUID = 8070744939537687606L;

      CompositeException() {
         super("Multiple exceptions");
      }
   }

   static final class ErrorCallbackNotImplemented extends UnsupportedOperationException {
      private static final long serialVersionUID = 2491425227432776143L;

      ErrorCallbackNotImplemented(Throwable cause) {
         super(cause);
      }

      public synchronized Throwable fillInStackTrace() {
         return this;
      }
   }

   static final class OverflowException extends IllegalStateException {
      OverflowException(String s) {
         super(s);
      }
   }

   static class ReactiveException extends RuntimeException {
      private static final long serialVersionUID = 2491425227432776143L;

      ReactiveException(Throwable cause) {
         super(cause);
      }

      ReactiveException(String message) {
         super(message);
      }

      public synchronized Throwable fillInStackTrace() {
         return this.getCause() != null ? this.getCause().fillInStackTrace() : super.fillInStackTrace();
      }
   }

   static class ReactorRejectedExecutionException extends RejectedExecutionException {
      ReactorRejectedExecutionException(String message, Throwable cause) {
         super(message, cause);
      }

      ReactorRejectedExecutionException(String message) {
         super(message);
      }
   }

   static final class RetryExhaustedException extends IllegalStateException {
      RetryExhaustedException(String message) {
         super(message);
      }

      RetryExhaustedException(String message, Throwable cause) {
         super(message, cause);
      }
   }

   static final class StaticRejectedExecutionException extends RejectedExecutionException {
      StaticRejectedExecutionException(String message, Throwable cause) {
         super(message, cause);
      }

      StaticRejectedExecutionException(String message) {
         super(message);
      }

      public synchronized Throwable fillInStackTrace() {
         return this;
      }
   }

   static final class StaticThrowable extends Error {
      StaticThrowable(String message) {
         super(message, null, false, false);
      }

      StaticThrowable(String message, Throwable cause) {
         super(message, cause, false, false);
      }

      StaticThrowable(Throwable cause) {
         super(cause.toString(), cause, false, false);
      }
   }
}
