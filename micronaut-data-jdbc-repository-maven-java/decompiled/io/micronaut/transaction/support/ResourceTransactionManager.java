package io.micronaut.transaction.support;

import io.micronaut.transaction.SynchronousTransactionManager;

public interface ResourceTransactionManager<R, T> extends SynchronousTransactionManager<T> {
   R getResourceFactory();
}
