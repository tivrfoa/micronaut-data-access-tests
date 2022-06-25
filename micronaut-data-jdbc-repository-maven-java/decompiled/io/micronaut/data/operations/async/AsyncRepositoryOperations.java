package io.micronaut.data.operations.async;

import io.micronaut.core.annotation.NonBlocking;
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
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

@NonBlocking
public interface AsyncRepositoryOperations {
   @NonNull
   Executor getExecutor();

   @NonNull
   <T> CompletionStage<T> findOne(@NonNull Class<T> type, @NonNull Serializable id);

   <T> CompletionStage<Boolean> exists(@NonNull PreparedQuery<T, Boolean> preparedQuery);

   @NonNull
   <T, R> CompletionStage<R> findOne(@NonNull PreparedQuery<T, R> preparedQuery);

   @NonNull
   <T> CompletionStage<T> findOptional(@NonNull Class<T> type, @NonNull Serializable id);

   @NonNull
   <T, R> CompletionStage<R> findOptional(@NonNull PreparedQuery<T, R> preparedQuery);

   @NonNull
   <T> CompletionStage<Iterable<T>> findAll(PagedQuery<T> pagedQuery);

   @NonNull
   <T> CompletionStage<Long> count(PagedQuery<T> pagedQuery);

   @NonNull
   <T, R> CompletionStage<Iterable<R>> findAll(@NonNull PreparedQuery<T, R> preparedQuery);

   @NonNull
   <T> CompletionStage<T> persist(@NonNull InsertOperation<T> operation);

   @NonNull
   <T> CompletionStage<T> update(@NonNull UpdateOperation<T> operation);

   @NonNull
   default <T> CompletionStage<Iterable<T>> updateAll(@NonNull UpdateBatchOperation<T> operation) {
      throw new UnsupportedOperationException("The updateAll is required to be implemented.");
   }

   @SingleResult
   @NonNull
   <T> CompletionStage<Number> delete(@NonNull DeleteOperation<T> operation);

   @NonNull
   <T> CompletionStage<Iterable<T>> persistAll(@NonNull InsertBatchOperation<T> operation);

   @NonNull
   CompletionStage<Number> executeUpdate(@NonNull PreparedQuery<?, Number> preparedQuery);

   @NonNull
   default CompletionStage<Number> executeDelete(@NonNull PreparedQuery<?, Number> preparedQuery) {
      return this.executeUpdate(preparedQuery);
   }

   @NonNull
   <T> CompletionStage<Number> deleteAll(@NonNull DeleteBatchOperation<T> operation);

   @NonNull
   <R> CompletionStage<Page<R>> findPage(@NonNull PagedQuery<R> pagedQuery);
}
