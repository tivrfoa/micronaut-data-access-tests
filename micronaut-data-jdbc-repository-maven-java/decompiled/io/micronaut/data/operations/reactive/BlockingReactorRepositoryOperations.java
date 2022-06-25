package io.micronaut.data.operations.reactive;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.runtime.DeleteBatchOperation;
import io.micronaut.data.model.runtime.DeleteOperation;
import io.micronaut.data.model.runtime.InsertBatchOperation;
import io.micronaut.data.model.runtime.InsertOperation;
import io.micronaut.data.model.runtime.PagedQuery;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.model.runtime.UpdateBatchOperation;
import io.micronaut.data.model.runtime.UpdateOperation;
import io.micronaut.data.operations.RepositoryOperations;
import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Stream;

public interface BlockingReactorRepositoryOperations extends RepositoryOperations, ReactorReactiveCapableRepository {
   @Nullable
   @Override
   default <T> T findOne(@NonNull Class<T> type, @NonNull Serializable id) {
      return this.reactive().<T>findOne(type, id).block();
   }

   @Nullable
   @Override
   default <T, R> R findOne(@NonNull PreparedQuery<T, R> preparedQuery) {
      return this.reactive().findOne(preparedQuery).block();
   }

   @NonNull
   @Override
   default <T, R> Iterable<R> findAll(@NonNull PreparedQuery<T, R> preparedQuery) {
      return this.reactive().findAll(preparedQuery).toIterable();
   }

   @NonNull
   @Override
   default <T, R> Stream<R> findStream(@NonNull PreparedQuery<T, R> preparedQuery) {
      return this.reactive().findAll(preparedQuery).toStream();
   }

   @NonNull
   @Override
   default <T> T persist(@NonNull InsertOperation<T> operation) {
      return (T)this.reactive().persist(operation).blockOptional().orElse(null);
   }

   @NonNull
   @Override
   default <T> T update(@NonNull UpdateOperation<T> operation) {
      return this.reactive().update(operation).block();
   }

   @Override
   default <T> Iterable<T> updateAll(UpdateBatchOperation<T> operation) {
      return (Iterable<T>)this.reactive().updateAll(operation).collectList().block();
   }

   @NonNull
   @Override
   default <T> Iterable<T> persistAll(@NonNull InsertBatchOperation<T> operation) {
      return (Iterable<T>)this.reactive().persistAll(operation).collectList().block();
   }

   @NonNull
   @Override
   default Optional<Number> executeUpdate(@NonNull PreparedQuery<?, Number> preparedQuery) {
      return this.reactive().executeUpdate(preparedQuery).blockOptional();
   }

   @Override
   default Optional<Number> executeDelete(@NonNull PreparedQuery<?, Number> preparedQuery) {
      return this.reactive().executeDelete(preparedQuery).blockOptional();
   }

   @Override
   default <T> int delete(@NonNull DeleteOperation<T> operation) {
      return ((Number)this.reactive().delete(operation).blockOptional().orElse(0)).intValue();
   }

   @Override
   default <T> Optional<Number> deleteAll(@NonNull DeleteBatchOperation<T> operation) {
      return this.reactive().deleteAll(operation).blockOptional();
   }

   @Override
   default <T> boolean exists(@NonNull PreparedQuery<T, Boolean> preparedQuery) {
      return this.reactive().exists(preparedQuery).blockOptional().orElse(false);
   }

   @Override
   default <R> Page<R> findPage(@NonNull PagedQuery<R> query) {
      return this.reactive().findPage(query).block();
   }

   @NonNull
   @Override
   default <T> Iterable<T> findAll(@NonNull PagedQuery<T> query) {
      return this.reactive().findAll(query).toIterable();
   }

   @Override
   default <T> long count(PagedQuery<T> pagedQuery) {
      return this.reactive().count(pagedQuery).blockOptional().orElse(0L);
   }

   @NonNull
   @Override
   default <T> Stream<T> findStream(@NonNull PagedQuery<T> query) {
      return this.reactive().findAll(query).toStream();
   }
}
