package io.micronaut.data.operations.reactive;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.runtime.DeleteBatchOperation;
import io.micronaut.data.model.runtime.DeleteOperation;
import io.micronaut.data.model.runtime.InsertBatchOperation;
import io.micronaut.data.model.runtime.InsertOperation;
import io.micronaut.data.model.runtime.PagedQuery;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.model.runtime.UpdateBatchOperation;
import io.micronaut.data.model.runtime.UpdateOperation;
import java.io.Serializable;
import org.reactivestreams.Publisher;

public interface ReactiveRepositoryOperations {
   @NonNull
   @SingleResult
   <T> Publisher<T> findOne(@NonNull Class<T> type, @NonNull Serializable id);

   <T> Publisher<Boolean> exists(@NonNull PreparedQuery<T, Boolean> preparedQuery);

   @SingleResult
   @NonNull
   <T, R> Publisher<R> findOne(@NonNull PreparedQuery<T, R> preparedQuery);

   @NonNull
   @SingleResult
   <T> Publisher<T> findOptional(@NonNull Class<T> type, @NonNull Serializable id);

   @SingleResult
   @NonNull
   <T, R> Publisher<R> findOptional(@NonNull PreparedQuery<T, R> preparedQuery);

   @NonNull
   <T> Publisher<T> findAll(PagedQuery<T> pagedQuery);

   @SingleResult
   @NonNull
   <T> Publisher<Long> count(PagedQuery<T> pagedQuery);

   @NonNull
   <T, R> Publisher<R> findAll(@NonNull PreparedQuery<T, R> preparedQuery);

   @SingleResult
   @NonNull
   <T> Publisher<T> persist(@NonNull InsertOperation<T> operation);

   @SingleResult
   @NonNull
   <T> Publisher<T> update(@NonNull UpdateOperation<T> operation);

   @NonNull
   default <T> Publisher<T> updateAll(@NonNull UpdateBatchOperation<T> operation) {
      throw new UnsupportedOperationException("The updateAll is required to be implemented.");
   }

   @NonNull
   <T> Publisher<T> persistAll(@NonNull InsertBatchOperation<T> operation);

   @NonNull
   @SingleResult
   Publisher<Number> executeUpdate(@NonNull PreparedQuery<?, Number> preparedQuery);

   @NonNull
   @SingleResult
   default Publisher<Number> executeDelete(@NonNull PreparedQuery<?, Number> preparedQuery) {
      return this.executeUpdate(preparedQuery);
   }

   @SingleResult
   @NonNull
   <T> Publisher<Number> delete(@NonNull DeleteOperation<T> operation);

   @SingleResult
   @NonNull
   <T> Publisher<Number> deleteAll(@NonNull DeleteBatchOperation<T> operation);

   @SingleResult
   @NonNull
   <R> Publisher<Page<R>> findPage(@NonNull PagedQuery<R> pagedQuery);
}
