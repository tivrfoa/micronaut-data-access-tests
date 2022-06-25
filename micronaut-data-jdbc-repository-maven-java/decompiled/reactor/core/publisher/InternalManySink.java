package reactor.core.publisher;

import reactor.core.Exceptions;

interface InternalManySink<T> extends Sinks.Many<T>, ContextHolder {
   @Override
   default void emitNext(T value, Sinks.EmitFailureHandler failureHandler) {
      Sinks.EmitResult emitResult;
      boolean shouldRetry;
      do {
         emitResult = this.tryEmitNext(value);
         if (emitResult.isSuccess()) {
            return;
         }

         shouldRetry = failureHandler.onEmitFailure(SignalType.ON_NEXT, emitResult);
      } while(shouldRetry);

      switch(emitResult) {
         case FAIL_ZERO_SUBSCRIBER:
            return;
         case FAIL_OVERFLOW:
            Operators.onDiscard(value, this.currentContext());
            this.emitError(Exceptions.failWithOverflow("Backpressure overflow during Sinks.Many#emitNext"), failureHandler);
            return;
         case FAIL_CANCELLED:
            Operators.onDiscard(value, this.currentContext());
            return;
         case FAIL_TERMINATED:
            Operators.onNextDropped(value, this.currentContext());
            return;
         case FAIL_NON_SERIALIZED:
            throw new Sinks.EmissionException(
               emitResult, "Spec. Rule 1.3 - onSubscribe, onNext, onError and onComplete signaled to a Subscriber MUST be signaled serially."
            );
         default:
            throw new Sinks.EmissionException(emitResult, "Unknown emitResult value");
      }
   }

   @Override
   default void emitComplete(Sinks.EmitFailureHandler failureHandler) {
      Sinks.EmitResult emitResult;
      boolean shouldRetry;
      do {
         emitResult = this.tryEmitComplete();
         if (emitResult.isSuccess()) {
            return;
         }

         shouldRetry = failureHandler.onEmitFailure(SignalType.ON_COMPLETE, emitResult);
      } while(shouldRetry);

      switch(emitResult) {
         case FAIL_ZERO_SUBSCRIBER:
         case FAIL_OVERFLOW:
         case FAIL_CANCELLED:
         case FAIL_TERMINATED:
            return;
         case FAIL_NON_SERIALIZED:
            throw new Sinks.EmissionException(
               emitResult, "Spec. Rule 1.3 - onSubscribe, onNext, onError and onComplete signaled to a Subscriber MUST be signaled serially."
            );
         default:
            throw new Sinks.EmissionException(emitResult, "Unknown emitResult value");
      }
   }

   @Override
   default void emitError(Throwable error, Sinks.EmitFailureHandler failureHandler) {
      Sinks.EmitResult emitResult;
      boolean shouldRetry;
      do {
         emitResult = this.tryEmitError(error);
         if (emitResult.isSuccess()) {
            return;
         }

         shouldRetry = failureHandler.onEmitFailure(SignalType.ON_ERROR, emitResult);
      } while(shouldRetry);

      switch(emitResult) {
         case FAIL_ZERO_SUBSCRIBER:
         case FAIL_OVERFLOW:
         case FAIL_CANCELLED:
            return;
         case FAIL_TERMINATED:
            Operators.onErrorDropped(error, this.currentContext());
            return;
         case FAIL_NON_SERIALIZED:
            throw new Sinks.EmissionException(
               emitResult, "Spec. Rule 1.3 - onSubscribe, onNext, onError and onComplete signaled to a Subscriber MUST be signaled serially."
            );
         default:
            throw new Sinks.EmissionException(emitResult, "Unknown emitResult value");
      }
   }
}
