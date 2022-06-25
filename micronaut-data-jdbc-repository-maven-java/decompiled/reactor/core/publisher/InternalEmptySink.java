package reactor.core.publisher;

interface InternalEmptySink<T> extends Sinks.Empty<T>, ContextHolder {
   @Override
   default void emitEmpty(Sinks.EmitFailureHandler failureHandler) {
      Sinks.EmitResult emitResult;
      boolean shouldRetry;
      do {
         emitResult = this.tryEmitEmpty();
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
