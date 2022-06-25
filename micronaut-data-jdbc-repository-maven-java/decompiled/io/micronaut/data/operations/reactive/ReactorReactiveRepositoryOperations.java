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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactorReactiveRepositoryOperations extends ReactiveRepositoryOperations {
   @NonNull
   @SingleResult
   <T> Mono<T> findOne(@NonNull Class<T> type, @NonNull Serializable id);

   <T> Mono<Boolean> exists(@NonNull PreparedQuery<T, Boolean> preparedQuery);

   @SingleResult
   @NonNull
   <T, R> Mono<R> findOne(@NonNull PreparedQuery<T, R> preparedQuery);

   @NonNull
   @SingleResult
   <T> Mono<T> findOptional(@NonNull Class<T> type, @NonNull Serializable id);

   @SingleResult
   @NonNull
   <T, R> Mono<R> findOptional(@NonNull PreparedQuery<T, R> preparedQuery);

   @NonNull
   <T> Flux<T> findAll(PagedQuery<T> pagedQuery);

   @SingleResult
   @NonNull
   <T> Mono<Long> count(PagedQuery<T> pagedQuery);

   @NonNull
   <T, R> Flux<R> findAll(@NonNull PreparedQuery<T, R> preparedQuery);

   @SingleResult
   @NonNull
   <T> Mono<T> persist(@NonNull InsertOperation<T> operation);

   @SingleResult
   @NonNull
   <T> Mono<T> update(@NonNull UpdateOperation<T> operation);

   @NonNull
   <T> Flux<T> updateAll(@NonNull UpdateBatchOperation<T> operation);

   @NonNull
   <T> Flux<T> persistAll(@NonNull InsertBatchOperation<T> operation);

   @NonNull
   @SingleResult
   Mono<Number> executeUpdate(@NonNull PreparedQuery<?, Number> preparedQuery);

   @NonNull
   @SingleResult
   Mono<Number> executeDelete(@NonNull PreparedQuery<?, Number> preparedQuery);

   @SingleResult
   @NonNull
   <T> Mono<Number> delete(@NonNull DeleteOperation<T> operation);

   @SingleResult
   @NonNull
   <T> Mono<Number> deleteAll(@NonNull DeleteBatchOperation<T> operation);

   @SingleResult
   @NonNull
   <R> Mono<Page<R>> findPage(@NonNull PagedQuery<R> pagedQuery);
}
