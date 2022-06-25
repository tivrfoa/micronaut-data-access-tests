package reactor.core.publisher;

import reactor.core.Exceptions;
import reactor.util.annotation.Nullable;

interface InternalOneSink<T> extends Sinks.One<T>, InternalEmptySink<T> {
   @Override
   default void emitValue(@Nullable T value, Sinks.EmitFailureHandler failureHandler) {
      if (value == null) {
         this.emitEmpty(failureHandler);
      } else {
         Sinks.EmitResult emitResult;
         boolean shouldRetry;
         do {
            emitResult = this.tryEmitValue(value);
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
               this.emitError(Exceptions.failWithOverflow("Backpressure overflow during Sinks.One#emitValue"), failureHandler);
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
   }
}
