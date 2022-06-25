package io.micronaut.transaction.reactive;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.transaction.TransactionExecution;

public interface ReactiveTransactionStatus<T> extends TransactionExecution {
   String ATTRIBUTE = "io.micronaut.tx.ATTRIBUTE";
   String STATUS = "io.micronaut.tx.STATUS";

   @NonNull
   T getConnection();
}
