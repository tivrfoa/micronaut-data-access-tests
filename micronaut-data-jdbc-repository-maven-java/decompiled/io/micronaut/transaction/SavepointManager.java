package io.micronaut.transaction;

import io.micronaut.transaction.exceptions.TransactionException;

public interface SavepointManager {
   Object createSavepoint() throws TransactionException;

   void rollbackToSavepoint(Object savepoint) throws TransactionException;

   void releaseSavepoint(Object savepoint) throws TransactionException;
}
