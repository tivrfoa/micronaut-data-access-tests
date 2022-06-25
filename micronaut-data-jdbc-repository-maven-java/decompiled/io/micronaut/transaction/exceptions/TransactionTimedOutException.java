package io.micronaut.transaction.exceptions;

public class TransactionTimedOutException extends TransactionException {
   public TransactionTimedOutException(String msg) {
      super(msg);
   }

   public TransactionTimedOutException(String msg, Throwable cause) {
      super(msg, cause);
   }
}
