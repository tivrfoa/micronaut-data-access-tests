package io.micronaut.transaction.interceptor;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.transaction.TransactionDefinition;

public interface TransactionAttribute extends TransactionDefinition {
   @Nullable
   String getQualifier();

   boolean rollbackOn(Throwable ex);
}
