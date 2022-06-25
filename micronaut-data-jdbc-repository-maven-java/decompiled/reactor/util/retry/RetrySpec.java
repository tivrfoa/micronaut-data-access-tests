package reactor.util.retry;

import java.time.Duration;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

public final class RetrySpec extends Retry {
   static final Duration MAX_BACKOFF = Duration.ofMillis(Long.MAX_VALUE);
   static final Consumer<Retry.RetrySignal> NO_OP_CONSUMER = rs -> {
   };
   static final BiFunction<Retry.RetrySignal, Mono<Void>, Mono<Void>> NO_OP_BIFUNCTION = (rs, m) -> m;
   static final BiFunction<RetrySpec, Retry.RetrySignal, Throwable> RETRY_EXCEPTION_GENERATOR = (builder, rs) -> Exceptions.retryExhausted(
         "Retries exhausted: "
            + (
               builder.isTransientErrors
                  ? rs.totalRetriesInARow() + "/" + builder.maxAttempts + " in a row (" + rs.totalRetries() + " total)"
                  : rs.totalRetries() + "/" + builder.maxAttempts
            ),
         rs.failure()
      );
   public final long maxAttempts;
   public final Predicate<Throwable> errorFilter;
   public final boolean isTransientErrors;
   final Consumer<Retry.RetrySignal> doPreRetry;
   final Consumer<Retry.RetrySignal> doPostRetry;
   final BiFunction<Retry.RetrySignal, Mono<Void>, Mono<Void>> asyncPreRetry;
   final BiFunction<Retry.RetrySignal, Mono<Void>, Mono<Void>> asyncPostRetry;
   final BiFunction<RetrySpec, Retry.RetrySignal, Throwable> retryExhaustedGenerator;

   RetrySpec(
      ContextView retryContext,
      long max,
      Predicate<? super Throwable> aThrowablePredicate,
      boolean isTransientErrors,
      Consumer<Retry.RetrySignal> doPreRetry,
      Consumer<Retry.RetrySignal> doPostRetry,
      BiFunction<Retry.RetrySignal, Mono<Void>, Mono<Void>> asyncPreRetry,
      BiFunction<Retry.RetrySignal, Mono<Void>, Mono<Void>> asyncPostRetry,
      BiFunction<RetrySpec, Retry.RetrySignal, Throwable> retryExhaustedGenerator
   ) {
      super(retryContext);
      this.maxAttempts = max;
      this.errorFilter = aThrowablePredicate::test;
      this.isTransientErrors = isTransientErrors;
      this.doPreRetry = doPreRetry;
      this.doPostRetry = doPostRetry;
      this.asyncPreRetry = asyncPreRetry;
      this.asyncPostRetry = asyncPostRetry;
      this.retryExhaustedGenerator = retryExhaustedGenerator;
   }

   public RetrySpec withRetryContext(ContextView retryContext) {
      return new RetrySpec(
         retryContext,
         this.maxAttempts,
         this.errorFilter,
         this.isTransientErrors,
         this.doPreRetry,
         this.doPostRetry,
         this.asyncPreRetry,
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   public RetrySpec maxAttempts(long maxAttempts) {
      return new RetrySpec(
         this.retryContext,
         maxAttempts,
         this.errorFilter,
         this.isTransientErrors,
         this.doPreRetry,
         this.doPostRetry,
         this.asyncPreRetry,
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   public RetrySpec filter(Predicate<? super Throwable> errorFilter) {
      return new RetrySpec(
         this.retryContext,
         this.maxAttempts,
         (Predicate<? super Throwable>)Objects.requireNonNull(errorFilter, "errorFilter"),
         this.isTransientErrors,
         this.doPreRetry,
         this.doPostRetry,
         this.asyncPreRetry,
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   public RetrySpec modifyErrorFilter(Function<Predicate<Throwable>, Predicate<? super Throwable>> predicateAdjuster) {
      Objects.requireNonNull(predicateAdjuster, "predicateAdjuster");
      Predicate<? super Throwable> newPredicate = (Predicate)Objects.requireNonNull(
         predicateAdjuster.apply(this.errorFilter), "predicateAdjuster must return a new predicate"
      );
      return new RetrySpec(
         this.retryContext,
         this.maxAttempts,
         newPredicate,
         this.isTransientErrors,
         this.doPreRetry,
         this.doPostRetry,
         this.asyncPreRetry,
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   public RetrySpec doBeforeRetry(Consumer<Retry.RetrySignal> doBeforeRetry) {
      return new RetrySpec(
         this.retryContext,
         this.maxAttempts,
         this.errorFilter,
         this.isTransientErrors,
         this.doPreRetry.andThen(doBeforeRetry),
         this.doPostRetry,
         this.asyncPreRetry,
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   public RetrySpec doAfterRetry(Consumer<Retry.RetrySignal> doAfterRetry) {
      return new RetrySpec(
         this.retryContext,
         this.maxAttempts,
         this.errorFilter,
         this.isTransientErrors,
         this.doPreRetry,
         this.doPostRetry.andThen(doAfterRetry),
         this.asyncPreRetry,
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   public RetrySpec doBeforeRetryAsync(Function<Retry.RetrySignal, Mono<Void>> doAsyncBeforeRetry) {
      return new RetrySpec(
         this.retryContext,
         this.maxAttempts,
         this.errorFilter,
         this.isTransientErrors,
         this.doPreRetry,
         this.doPostRetry,
         (rs, m) -> ((Mono)this.asyncPreRetry.apply(rs, m)).then((Mono)doAsyncBeforeRetry.apply(rs)),
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   public RetrySpec doAfterRetryAsync(Function<Retry.RetrySignal, Mono<Void>> doAsyncAfterRetry) {
      return new RetrySpec(
         this.retryContext,
         this.maxAttempts,
         this.errorFilter,
         this.isTransientErrors,
         this.doPreRetry,
         this.doPostRetry,
         this.asyncPreRetry,
         (rs, m) -> ((Mono)this.asyncPostRetry.apply(rs, m)).then((Mono)doAsyncAfterRetry.apply(rs)),
         this.retryExhaustedGenerator
      );
   }

   public RetrySpec onRetryExhaustedThrow(BiFunction<RetrySpec, Retry.RetrySignal, Throwable> retryExhaustedGenerator) {
      return new RetrySpec(
         this.retryContext,
         this.maxAttempts,
         this.errorFilter,
         this.isTransientErrors,
         this.doPreRetry,
         this.doPostRetry,
         this.asyncPreRetry,
         this.asyncPostRetry,
         (BiFunction<RetrySpec, Retry.RetrySignal, Throwable>)Objects.requireNonNull(retryExhaustedGenerator, "retryExhaustedGenerator")
      );
   }

   public RetrySpec transientErrors(boolean isTransientErrors) {
      return new RetrySpec(
         this.retryContext,
         this.maxAttempts,
         this.errorFilter,
         isTransientErrors,
         this.doPreRetry,
         this.doPostRetry,
         this.asyncPreRetry,
         this.asyncPostRetry,
         this.retryExhaustedGenerator
      );
   }

   public Flux<Long> generateCompanion(Flux<Retry.RetrySignal> flux) {
      return flux.concatMap(
         retryWhenState -> {
            Retry.RetrySignal copy = retryWhenState.copy();
            Throwable currentFailure = copy.failure();
            long iteration = this.isTransientErrors ? copy.totalRetriesInARow() : copy.totalRetries();
            if (currentFailure == null) {
               return Mono.error(new IllegalStateException("RetryWhenState#failure() not expected to be null"));
            } else if (!this.errorFilter.test(currentFailure)) {
               return Mono.error(currentFailure);
            } else {
               return iteration >= this.maxAttempts
                  ? Mono.error((Throwable)this.retryExhaustedGenerator.apply(this, copy))
                  : applyHooks(copy, Mono.just(iteration), this.doPreRetry, this.doPostRetry, this.asyncPreRetry, this.asyncPostRetry);
            }
         }
      );
   }

   static <T> Mono<T> applyHooks(
      Retry.RetrySignal copyOfSignal,
      Mono<T> originalCompanion,
      Consumer<Retry.RetrySignal> doPreRetry,
      Consumer<Retry.RetrySignal> doPostRetry,
      BiFunction<Retry.RetrySignal, Mono<Void>, Mono<Void>> asyncPreRetry,
      BiFunction<Retry.RetrySignal, Mono<Void>, Mono<Void>> asyncPostRetry
   ) {
      if (doPreRetry != NO_OP_CONSUMER) {
         try {
            doPreRetry.accept(copyOfSignal);
         } catch (Throwable var9) {
            return Mono.error(var9);
         }
      }

      Mono<Void> postRetrySyncMono;
      if (doPostRetry != NO_OP_CONSUMER) {
         postRetrySyncMono = Mono.fromRunnable(() -> doPostRetry.accept(copyOfSignal));
      } else {
         postRetrySyncMono = Mono.empty();
      }

      Mono<Void> preRetryMono = asyncPreRetry == NO_OP_BIFUNCTION ? Mono.empty() : (Mono)asyncPreRetry.apply(copyOfSignal, Mono.empty());
      Mono<Void> postRetryMono = asyncPostRetry != NO_OP_BIFUNCTION ? (Mono)asyncPostRetry.apply(copyOfSignal, postRetrySyncMono) : postRetrySyncMono;
      return preRetryMono.then(originalCompanion).flatMap(postRetryMono::thenReturn);
   }
}
