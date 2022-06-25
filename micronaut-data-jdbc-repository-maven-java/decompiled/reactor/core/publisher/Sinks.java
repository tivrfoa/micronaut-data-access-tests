package reactor.core.publisher;

import java.time.Duration;
import java.util.Queue;
import reactor.core.Disposable;
import reactor.core.Scannable;
import reactor.core.scheduler.Scheduler;
import reactor.util.annotation.Nullable;

public final class Sinks {
   private Sinks() {
   }

   public static <T> Sinks.Empty<T> empty() {
      return SinksSpecs.DEFAULT_ROOT_SPEC.empty();
   }

   public static <T> Sinks.One<T> one() {
      return SinksSpecs.DEFAULT_ROOT_SPEC.one();
   }

   public static Sinks.ManySpec many() {
      return SinksSpecs.DEFAULT_ROOT_SPEC.many();
   }

   public static Sinks.RootSpec unsafe() {
      return SinksSpecs.UNSAFE_ROOT_SPEC;
   }

   public static final class EmissionException extends IllegalStateException {
      final Sinks.EmitResult reason;

      public EmissionException(Sinks.EmitResult reason) {
         this(reason, "Sink emission failed with " + reason);
      }

      public EmissionException(Throwable cause, Sinks.EmitResult reason) {
         super("Sink emission failed with " + reason, cause);
         this.reason = reason;
      }

      public EmissionException(Sinks.EmitResult reason, String message) {
         super(message);
         this.reason = reason;
      }

      public Sinks.EmitResult getReason() {
         return this.reason;
      }
   }

   public interface EmitFailureHandler {
      Sinks.EmitFailureHandler FAIL_FAST = (signalType, emission) -> false;

      static Sinks.EmitFailureHandler busyLooping(Duration duration) {
         return new Sinks.OptimisticEmitFailureHandler(duration);
      }

      boolean onEmitFailure(SignalType var1, Sinks.EmitResult var2);
   }

   public static enum EmitResult {
      OK,
      FAIL_TERMINATED,
      FAIL_OVERFLOW,
      FAIL_CANCELLED,
      FAIL_NON_SERIALIZED,
      FAIL_ZERO_SUBSCRIBER;

      public boolean isSuccess() {
         return this == OK;
      }

      public boolean isFailure() {
         return this != OK;
      }

      public void orThrow() {
         if (this != OK) {
            throw new Sinks.EmissionException(this);
         }
      }

      public void orThrowWithCause(Throwable cause) {
         if (this != OK) {
            throw new Sinks.EmissionException(cause, this);
         }
      }
   }

   public interface Empty<T> extends Scannable {
      Sinks.EmitResult tryEmitEmpty();

      Sinks.EmitResult tryEmitError(Throwable var1);

      void emitEmpty(Sinks.EmitFailureHandler var1);

      void emitError(Throwable var1, Sinks.EmitFailureHandler var2);

      int currentSubscriberCount();

      Mono<T> asMono();
   }

   public interface Many<T> extends Scannable {
      Sinks.EmitResult tryEmitNext(T var1);

      Sinks.EmitResult tryEmitComplete();

      Sinks.EmitResult tryEmitError(Throwable var1);

      void emitNext(T var1, Sinks.EmitFailureHandler var2);

      void emitComplete(Sinks.EmitFailureHandler var1);

      void emitError(Throwable var1, Sinks.EmitFailureHandler var2);

      int currentSubscriberCount();

      Flux<T> asFlux();
   }

   public interface ManySpec {
      Sinks.UnicastSpec unicast();

      Sinks.MulticastSpec multicast();

      Sinks.MulticastReplaySpec replay();
   }

   public interface MulticastReplaySpec {
      <T> Sinks.Many<T> all();

      <T> Sinks.Many<T> all(int var1);

      <T> Sinks.Many<T> latest();

      <T> Sinks.Many<T> latestOrDefault(T var1);

      <T> Sinks.Many<T> limit(int var1);

      <T> Sinks.Many<T> limit(Duration var1);

      <T> Sinks.Many<T> limit(Duration var1, Scheduler var2);

      <T> Sinks.Many<T> limit(int var1, Duration var2);

      <T> Sinks.Many<T> limit(int var1, Duration var2, Scheduler var3);
   }

   public interface MulticastSpec {
      <T> Sinks.Many<T> onBackpressureBuffer();

      <T> Sinks.Many<T> onBackpressureBuffer(int var1);

      <T> Sinks.Many<T> onBackpressureBuffer(int var1, boolean var2);

      <T> Sinks.Many<T> directAllOrNothing();

      <T> Sinks.Many<T> directBestEffort();
   }

   public interface One<T> extends Sinks.Empty<T> {
      Sinks.EmitResult tryEmitValue(@Nullable T var1);

      void emitValue(@Nullable T var1, Sinks.EmitFailureHandler var2);
   }

   static class OptimisticEmitFailureHandler implements Sinks.EmitFailureHandler {
      private final long deadline;

      OptimisticEmitFailureHandler(Duration duration) {
         this.deadline = System.nanoTime() + duration.toNanos();
      }

      @Override
      public boolean onEmitFailure(SignalType signalType, Sinks.EmitResult emitResult) {
         return emitResult.equals(Sinks.EmitResult.FAIL_NON_SERIALIZED) && System.nanoTime() < this.deadline;
      }
   }

   public interface RootSpec {
      <T> Sinks.Empty<T> empty();

      <T> Sinks.One<T> one();

      Sinks.ManySpec many();
   }

   public interface UnicastSpec {
      <T> Sinks.Many<T> onBackpressureBuffer();

      <T> Sinks.Many<T> onBackpressureBuffer(Queue<T> var1);

      <T> Sinks.Many<T> onBackpressureBuffer(Queue<T> var1, Disposable var2);

      <T> Sinks.Many<T> onBackpressureError();
   }
}
