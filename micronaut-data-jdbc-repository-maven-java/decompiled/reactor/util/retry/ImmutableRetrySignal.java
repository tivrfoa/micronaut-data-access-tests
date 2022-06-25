package reactor.util.retry;

import reactor.util.context.Context;
import reactor.util.context.ContextView;

final class ImmutableRetrySignal implements Retry.RetrySignal {
   final long failureTotalIndex;
   final long failureSubsequentIndex;
   final Throwable failure;
   final ContextView retryContext;

   ImmutableRetrySignal(long failureTotalIndex, long failureSubsequentIndex, Throwable failure) {
      this(failureTotalIndex, failureSubsequentIndex, failure, Context.empty());
   }

   ImmutableRetrySignal(long failureTotalIndex, long failureSubsequentIndex, Throwable failure, ContextView retryContext) {
      this.failureTotalIndex = failureTotalIndex;
      this.failureSubsequentIndex = failureSubsequentIndex;
      this.failure = failure;
      this.retryContext = retryContext;
   }

   @Override
   public long totalRetries() {
      return this.failureTotalIndex;
   }

   @Override
   public long totalRetriesInARow() {
      return this.failureSubsequentIndex;
   }

   @Override
   public Throwable failure() {
      return this.failure;
   }

   @Override
   public ContextView retryContextView() {
      return this.retryContext;
   }

   @Override
   public Retry.RetrySignal copy() {
      return this;
   }

   public String toString() {
      return "attempt #" + (this.failureTotalIndex + 1L) + " (" + (this.failureSubsequentIndex + 1L) + " in a row), last failure={" + this.failure + '}';
   }
}
