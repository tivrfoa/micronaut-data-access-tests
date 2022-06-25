package io.micronaut.transaction;

import io.micronaut.core.annotation.NonNull;
import java.io.Flushable;

public interface TransactionStatus<T> extends TransactionExecution, SavepointManager, Flushable {
   boolean hasSavepoint();

   void flush();

   @NonNull
   Object getTransaction();

   @NonNull
   T getConnection();
}
