package io.micronaut.transaction.exceptions;

public class NestedTransactionNotSupportedException extends CannotCreateTransactionException {
   public NestedTransactionNotSupportedException(String msg) {
      super(msg);
   }

   public NestedTransactionNotSupportedException(String msg, Throwable cause) {
      super(msg, cause);
   }
}
