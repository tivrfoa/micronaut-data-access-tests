package io.micronaut.transaction;

import io.micronaut.core.annotation.Blocking;
import io.micronaut.core.annotation.NonNull;

@Blocking
public interface TransactionOperations<T> {
   @NonNull
   T getConnection();

   boolean hasConnection();

   <R> R execute(@NonNull TransactionDefinition definition, @NonNull TransactionCallback<T, R> callback);

   <R> R executeRead(@NonNull TransactionCallback<T, R> callback);

   <R> R executeWrite(@NonNull TransactionCallback<T, R> callback);
}
