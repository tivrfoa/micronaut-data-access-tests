package io.micronaut.transaction.exceptions;

public class InvalidIsolationLevelException extends TransactionUsageException {
   public InvalidIsolationLevelException(String msg) {
      super(msg);
   }
}
