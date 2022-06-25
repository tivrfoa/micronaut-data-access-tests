package reactor.util.retry;

import java.time.Duration;
import java.util.function.Function;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

public abstract class Retry {
   public final ContextView retryContext;

   public Retry() {
      this(Context.empty());
   }

   protected Retry(ContextView retryContext) {
      this.retryContext = retryContext;
   }

   public abstract Publisher<?> generateCompanion(Flux<Retry.RetrySignal> var1);

   public ContextView retryContext() {
      return this.retryContext;
   }

   public static RetryBackoffSpec backoff(long maxAttempts, Duration minBackoff) {
      return new RetryBackoffSpec(
         Context.empty(),
         maxAttempts,
         t -> true,
         false,
         minBackoff,
         RetrySpec.MAX_BACKOFF,
         0.5,
         Schedulers::parallel,
         RetrySpec.NO_OP_CONSUMER,
         RetrySpec.NO_OP_CONSUMER,
         RetrySpec.NO_OP_BIFUNCTION,
         RetrySpec.NO_OP_BIFUNCTION,
         RetryBackoffSpec.BACKOFF_EXCEPTION_GENERATOR
      );
   }

   public static RetryBackoffSpec fixedDelay(long maxAttempts, Duration fixedDelay) {
      return new RetryBackoffSpec(
         Context.empty(),
         maxAttempts,
         t -> true,
         false,
         fixedDelay,
         fixedDelay,
         0.0,
         Schedulers::parallel,
         RetrySpec.NO_OP_CONSUMER,
         RetrySpec.NO_OP_CONSUMER,
         RetrySpec.NO_OP_BIFUNCTION,
         RetrySpec.NO_OP_BIFUNCTION,
         RetryBackoffSpec.BACKOFF_EXCEPTION_GENERATOR
      );
   }

   public static RetrySpec max(long max) {
      return new RetrySpec(
         Context.empty(),
         max,
         t -> true,
         false,
         RetrySpec.NO_OP_CONSUMER,
         RetrySpec.NO_OP_CONSUMER,
         RetrySpec.NO_OP_BIFUNCTION,
         RetrySpec.NO_OP_BIFUNCTION,
         RetrySpec.RETRY_EXCEPTION_GENERATOR
      );
   }

   public static RetrySpec maxInARow(long maxInARow) {
      return new RetrySpec(
         Context.empty(),
         maxInARow,
         t -> true,
         true,
         RetrySpec.NO_OP_CONSUMER,
         RetrySpec.NO_OP_CONSUMER,
         RetrySpec.NO_OP_BIFUNCTION,
         RetrySpec.NO_OP_BIFUNCTION,
         RetrySpec.RETRY_EXCEPTION_GENERATOR
      );
   }

   public static RetrySpec indefinitely() {
      return new RetrySpec(
         Context.empty(),
         Long.MAX_VALUE,
         t -> true,
         false,
         RetrySpec.NO_OP_CONSUMER,
         RetrySpec.NO_OP_CONSUMER,
         RetrySpec.NO_OP_BIFUNCTION,
         RetrySpec.NO_OP_BIFUNCTION,
         RetrySpec.RETRY_EXCEPTION_GENERATOR
      );
   }

   public static final Retry from(final Function<Flux<Retry.RetrySignal>, ? extends Publisher<?>> function) {
      return new Retry(Context.empty()) {
         @Override
         public Publisher<?> generateCompanion(Flux<Retry.RetrySignal> retrySignalCompanion) {
            return (Publisher<?>)function.apply(retrySignalCompanion);
         }
      };
   }

   public static final Retry withThrowable(final Function<Flux<Throwable>, ? extends Publisher<?>> function) {
      return new Retry(Context.empty()) {
         @Override
         public Publisher<?> generateCompanion(Flux<Retry.RetrySignal> retrySignals) {
            return (Publisher<?>)function.apply(retrySignals.map(Retry.RetrySignal::failure));
         }
      };
   }

   public interface RetrySignal {
      long totalRetries();

      long totalRetriesInARow();

      Throwable failure();

      default ContextView retryContextView() {
         return Context.empty();
      }

      default Retry.RetrySignal copy() {
         return new ImmutableRetrySignal(this.totalRetries(), this.totalRetriesInARow(), this.failure(), this.retryContextView());
      }
   }
}
