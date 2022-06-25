package io.micronaut.transaction.support;

import io.micronaut.core.annotation.Blocking;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.transaction.TransactionCallback;
import io.micronaut.transaction.TransactionDefinition;
import io.micronaut.transaction.TransactionState;

@Blocking
@Internal
public interface TransactionStateOperations<T, S extends TransactionState> {
   <R> R execute(@NonNull S state, @NonNull TransactionDefinition definition, @NonNull TransactionCallback<T, R> callback);

   <R> R executeRead(@NonNull S state, @NonNull TransactionCallback<T, R> callback);

   <R> R executeWrite(@NonNull S state, @NonNull TransactionCallback<T, R> callback);
}
