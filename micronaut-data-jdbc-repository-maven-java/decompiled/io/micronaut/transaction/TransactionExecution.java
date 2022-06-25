package io.micronaut.transaction;

public interface TransactionExecution {
   boolean isNewTransaction();

   void setRollbackOnly();

   boolean isRollbackOnly();

   boolean isCompleted();
}
