package io.micronaut.transaction.exceptions;

public class NoTransactionException extends TransactionException {
   public NoTransactionException(String message) {
      super(message);
   }

   public NoTransactionException(String message, Throwable cause) {
      super(message, cause);
   }
}
