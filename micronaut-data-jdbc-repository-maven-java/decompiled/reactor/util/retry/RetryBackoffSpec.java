package reactor.util.retry;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.util.annotation.Nullable;
import reactor.util.context.ContextView;

public final class RetryBackoffSpec extends Retry {
   static final BiFunction<RetryBackoffSpec, Retry.RetrySignal, Throwable> BACKOFF_EXCEPTION_GENERATOR = (builder, rs) -> Exceptions.retryExhausted(
         "Retries exhausted: "
            + (
               builder.isTransientErrors
                  ? rs.totalRetriesInARow() + "/" + builder.maxAttempts + " in a row (" + rs.totalRetries() + " total)"
                  : rs.totalRetries() + "/" + builder.maxAttempts
            ),
         rs.failure()
      );
   public final Duration minBackoff;
   public final Duration maxBackoff;
   public final double jitterFactor;
   public final Supplier<Scheduler> backoffSchedulerSupplier;
   public final long maxAttempts;
   public final Predicate<Throwable> errorFilter;
   public final boolean isTransientErrors;
   final Consumer<Retry.RetrySignal> syncPreRetry;
   final Consumer<Retry.RetrySignal> syncPostRetry;
   final BiFunction<Retry.RetrySignal, Mono<Void>, Mono<Void>> asyncPreRetry;
   final BiFunction<Retry.RetrySignal, Mono<Void>, Mono<Void>> asyncPostRetry;
   final BiFunction<RetryBackoffSpec, Retry.RetrySignal, Throwable> retryExhaustedGenerator;

   RetryBackoffSpec(
      ContextView retryContext,
      long max,
      Predicate<? super Throwable> aThrowablePredicate,
      boolean isTransientErrors,
      Duration minBackoff,
      Duration maxBackoff,
      double jitterFactor,
      Supplier<Scheduler> backoffSchedulerSupplier,
      Consumer<Retry.RetrySignal> doPreRetry,
      Consumer<Retry.RetrySignal> doPostRetry,
      BiFunction<Retry.RetrySignal, Mono<Void>, Mono<Void>> asyncPreRetry,
      BiFunction<Retry.RetrySignal, Mono<Void>, Mono<Void>> asyncPostRetry,
      BiFunction<RetryBackoffSpec, Retry.RetrySignal, Throwable> retryExhaustedGenerator
   ) {
      super(retryContext);
      this.maxAttempts = max;
      this.errorFilter = aThrowablePredicate::test;
      this.isTransientErrors = isTransientErrors;
      this.minBackoff = minBackoff;
      this.maxBackoff = maxBackoff;
      this.jitterFactor = jitterFactor;
      this.backoffSchedulerSupplier = backoffSchedulerSupplier;
      this.syncPreRetry = doPreRetry;
      this.syncPostRetry = doPostRetry;
      this.asyncPreRetry = asyncPreRetry;
      this.asyncPostRetry = asyncPostRetry;
      this.retryExhaustedGenerator = retryExhaustedGenerator;
   }

   public RetryBackoffSpec withRetryContext(ContextView retryContext) {
      return new RetryBackoffSpec(
         retryContext,
         this.maxAttempts,
         this.errorFilter,
         this.isTransientErrors,
         this.minBackoff,
         this.maxBackoff,
         this.jitterFactor,
         this.backoffSchedulerSupplier,
         this.syncPreRetry,
         this.syncPostRetry,
         this.asyncPreRetry,
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   public RetryBackoffSpec maxAttempts(long maxAttempts) {
      return new RetryBackoffSpec(
         this.retryContext,
         maxAttempts,
         this.errorFilter,
         this.isTransientErrors,
         this.minBackoff,
         this.maxBackoff,
         this.jitterFactor,
         this.backoffSchedulerSupplier,
         this.syncPreRetry,
         this.syncPostRetry,
         this.asyncPreRetry,
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   public RetryBackoffSpec filter(Predicate<? super Throwable> errorFilter) {
      return new RetryBackoffSpec(
         this.retryContext,
         this.maxAttempts,
         (Predicate<? super Throwable>)Objects.requireNonNull(errorFilter, "errorFilter"),
         this.isTransientErrors,
         this.minBackoff,
         this.maxBackoff,
         this.jitterFactor,
         this.backoffSchedulerSupplier,
         this.syncPreRetry,
         this.syncPostRetry,
         this.asyncPreRetry,
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   public RetryBackoffSpec modifyErrorFilter(Function<Predicate<Throwable>, Predicate<? super Throwable>> predicateAdjuster) {
      Objects.requireNonNull(predicateAdjuster, "predicateAdjuster");
      Predicate<? super Throwable> newPredicate = (Predicate)Objects.requireNonNull(
         predicateAdjuster.apply(this.errorFilter), "predicateAdjuster must return a new predicate"
      );
      return new RetryBackoffSpec(
         this.retryContext,
         this.maxAttempts,
         newPredicate,
         this.isTransientErrors,
         this.minBackoff,
         this.maxBackoff,
         this.jitterFactor,
         this.backoffSchedulerSupplier,
         this.syncPreRetry,
         this.syncPostRetry,
         this.asyncPreRetry,
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   public RetryBackoffSpec doBeforeRetry(Consumer<Retry.RetrySignal> doBeforeRetry) {
      return new RetryBackoffSpec(
         this.retryContext,
         this.maxAttempts,
         this.errorFilter,
         this.isTransientErrors,
         this.minBackoff,
         this.maxBackoff,
         this.jitterFactor,
         this.backoffSchedulerSupplier,
         this.syncPreRetry.andThen(doBeforeRetry),
         this.syncPostRetry,
         this.asyncPreRetry,
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   public RetryBackoffSpec doAfterRetry(Consumer<Retry.RetrySignal> doAfterRetry) {
      return new RetryBackoffSpec(
         this.retryContext,
         this.maxAttempts,
         this.errorFilter,
         this.isTransientErrors,
         this.minBackoff,
         this.maxBackoff,
         this.jitterFactor,
         this.backoffSchedulerSupplier,
         this.syncPreRetry,
         this.syncPostRetry.andThen(doAfterRetry),
         this.asyncPreRetry,
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   public RetryBackoffSpec doBeforeRetryAsync(Function<Retry.RetrySignal, Mono<Void>> doAsyncBeforeRetry) {
      return new RetryBackoffSpec(
         this.retryContext,
         this.maxAttempts,
         this.errorFilter,
         this.isTransientErrors,
         this.minBackoff,
         this.maxBackoff,
         this.jitterFactor,
         this.backoffSchedulerSupplier,
         this.syncPreRetry,
         this.syncPostRetry,
         (rs, m) -> ((Mono)this.asyncPreRetry.apply(rs, m)).then((Mono)doAsyncBeforeRetry.apply(rs)),
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   public RetryBackoffSpec doAfterRetryAsync(Function<Retry.RetrySignal, Mono<Void>> doAsyncAfterRetry) {
      return new RetryBackoffSpec(
         this.retryContext,
         this.maxAttempts,
         this.errorFilter,
         this.isTransientErrors,
         this.minBackoff,
         this.maxBackoff,
         this.jitterFactor,
         this.backoffSchedulerSupplier,
         this.syncPreRetry,
         this.syncPostRetry,
         this.asyncPreRetry,
         (rs, m) -> ((Mono)this.asyncPostRetry.apply(rs, m)).then((Mono)doAsyncAfterRetry.apply(rs)),
         this.retryExhaustedGenerator
      );
   }

   public RetryBackoffSpec onRetryExhaustedThrow(BiFunction<RetryBackoffSpec, Retry.RetrySignal, Throwable> retryExhaustedGenerator) {
      return new RetryBackoffSpec(
         this.retryContext,
         this.maxAttempts,
         this.errorFilter,
         this.isTransientErrors,
         this.minBackoff,
         this.maxBackoff,
         this.jitterFactor,
         this.backoffSchedulerSupplier,
         this.syncPreRetry,
         this.syncPostRetry,
         this.asyncPreRetry,
         this.asyncPostRetry,
         (BiFunction<RetryBackoffSpec, Retry.RetrySignal, Throwable>)Objects.requireNonNull(retryExhaustedGenerator, "retryExhaustedGenerator")
      );
   }

   public RetryBackoffSpec transientErrors(boolean isTransientErrors) {
      return new RetryBackoffSpec(
         this.retryContext,
         this.maxAttempts,
         this.errorFilter,
         isTransientErrors,
         this.minBackoff,
         this.maxBackoff,
         this.jitterFactor,
         this.backoffSchedulerSupplier,
         this.syncPreRetry,
         this.syncPostRetry,
         this.asyncPreRetry,
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   public RetryBackoffSpec minBackoff(Duration minBackoff) {
      return new RetryBackoffSpec(
         this.retryContext,
         this.maxAttempts,
         this.errorFilter,
         this.isTransientErrors,
         minBackoff,
         this.maxBackoff,
         this.jitterFactor,
         this.backoffSchedulerSupplier,
         this.syncPreRetry,
         this.syncPostRetry,
         this.asyncPreRetry,
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   public RetryBackoffSpec maxBackoff(Duration maxBackoff) {
      return new RetryBackoffSpec(
         this.retryContext,
         this.maxAttempts,
         this.errorFilter,
         this.isTransientErrors,
         this.minBackoff,
         maxBackoff,
         this.jitterFactor,
         this.backoffSchedulerSupplier,
         this.syncPreRetry,
         this.syncPostRetry,
         this.asyncPreRetry,
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   public RetryBackoffSpec jitter(double jitterFactor) {
      return new RetryBackoffSpec(
         this.retryContext,
         this.maxAttempts,
         this.errorFilter,
         this.isTransientErrors,
         this.minBackoff,
         this.maxBackoff,
         jitterFactor,
         this.backoffSchedulerSupplier,
         this.syncPreRetry,
         this.syncPostRetry,
         this.asyncPreRetry,
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   public RetryBackoffSpec scheduler(@Nullable Scheduler backoffScheduler) {
      return new RetryBackoffSpec(
         this.retryContext,
         this.maxAttempts,
         this.errorFilter,
         this.isTransientErrors,
         this.minBackoff,
         this.maxBackoff,
         this.jitterFactor,
         backoffScheduler == null ? Schedulers::parallel : () -> backoffScheduler,
         this.syncPreRetry,
         this.syncPostRetry,
         this.asyncPreRetry,
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   protected void validateArguments() {
      if (this.jitterFactor < 0.0 || this.jitterFactor > 1.0) {
         throw new IllegalArgumentException("jitterFactor must be between 0 and 1 (default 0.5)");
      }
   }

   public Flux<Long> generateCompanion(Flux<Retry.RetrySignal> t) {
      this.validateArguments();
      return t.concatMap(
         retryWhenState -> {
            Retry.RetrySignal copy = retryWhenState.copy();
            Throwable currentFailure = copy.failure();
            long iteration = this.isTransientErrors ? copy.totalRetriesInARow() : copy.totalRetries();
            if (currentFailure == null) {
               return Mono.error(new IllegalStateException("Retry.RetrySignal#failure() not expected to be null"));
            } else if (!this.errorFilter.test(currentFailure)) {
               return Mono.error(currentFailure);
            } else if (iteration >= this.maxAttempts) {
               return Mono.error((Throwable)this.retryExhaustedGenerator.apply(this, copy));
            } else {
               Duration nextBackoff;
               try {
                  nextBackoff = this.minBackoff.multipliedBy((long)Math.pow(2.0, (double)iteration));
                  if (nextBackoff.compareTo(this.maxBackoff) > 0) {
                     nextBackoff = this.maxBackoff;
                  }
               } catch (ArithmeticException var18) {
                  nextBackoff = this.maxBackoff;
               }
   
               if (nextBackoff.isZero()) {
                  return RetrySpec.applyHooks(copy, Mono.just(iteration), this.syncPreRetry, this.syncPostRetry, this.asyncPreRetry, this.asyncPostRetry);
               } else {
                  ThreadLocalRandom random = ThreadLocalRandom.current();
   
                  long jitterOffset;
                  try {
                     jitterOffset = nextBackoff.multipliedBy((long)(100.0 * this.jitterFactor)).dividedBy(100L).toMillis();
                  } catch (ArithmeticException var17) {
                     jitterOffset = Math.round(9.223372E18F * this.jitterFactor);
                  }
   
                  long lowBound = Math.max(this.minBackoff.minus(nextBackoff).toMillis(), -jitterOffset);
                  long highBound = Math.min(this.maxBackoff.minus(nextBackoff).toMillis(), jitterOffset);
                  long jitter;
                  if (highBound == lowBound) {
                     if (highBound == 0L) {
                        jitter = 0L;
                     } else {
                        jitter = random.nextLong(highBound);
                     }
                  } else {
                     jitter = random.nextLong(lowBound, highBound);
                  }
   
                  Duration effectiveBackoff = nextBackoff.plusMillis(jitter);
                  return RetrySpec.applyHooks(
                     copy,
                     Mono.delay(effectiveBackoff, (Scheduler)this.backoffSchedulerSupplier.get()),
                     this.syncPreRetry,
                     this.syncPostRetry,
                     this.asyncPreRetry,
                     this.asyncPostRetry
                  );
               }
            }
         }
      );
   }
}
