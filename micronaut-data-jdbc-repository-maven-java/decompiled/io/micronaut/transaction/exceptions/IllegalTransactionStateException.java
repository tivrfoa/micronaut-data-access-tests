package io.micronaut.transaction.exceptions;

public class IllegalTransactionStateException extends TransactionUsageException {
   public IllegalTransactionStateException(String msg) {
      super(msg);
   }

   public IllegalTransactionStateException(String msg, Throwable cause) {
      super(msg, cause);
   }
}
