package io.micronaut.transaction.exceptions;

public class TransactionException extends RuntimeException {
   public TransactionException(String message) {
      super(message);
   }

   public TransactionException(String message, Throwable cause) {
      super(message, cause);
   }
}
