package io.micronaut.data.operations;

import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.runtime.DeleteBatchOperation;
import io.micronaut.data.model.runtime.DeleteOperation;
import io.micronaut.data.model.runtime.InsertBatchOperation;
import io.micronaut.data.model.runtime.InsertOperation;
import io.micronaut.data.model.runtime.PagedQuery;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.UpdateBatchOperation;
import io.micronaut.data.model.runtime.UpdateOperation;
import java.io.Serializable;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface RepositoryOperations extends HintsCapableRepository, ApplicationContextProvider {
   @NonNull
   default <T> RuntimePersistentEntity<T> getEntity(@NonNull Class<T> type) {
      return PersistentEntity.of(type);
   }

   @Nullable
   <T> T findOne(@NonNull Class<T> type, @NonNull Serializable id);

   @Nullable
   <T, R> R findOne(@NonNull PreparedQuery<T, R> preparedQuery);

   <T> boolean exists(@NonNull PreparedQuery<T, Boolean> preparedQuery);

   @NonNull
   <T> Iterable<T> findAll(@NonNull PagedQuery<T> query);

   <T> long count(PagedQuery<T> pagedQuery);

   @NonNull
   <T, R> Iterable<R> findAll(@NonNull PreparedQuery<T, R> preparedQuery);

   @NonNull
   <T, R> Stream<R> findStream(@NonNull PreparedQuery<T, R> preparedQuery);

   @NonNull
   <T> Stream<T> findStream(@NonNull PagedQuery<T> query);

   <R> Page<R> findPage(@NonNull PagedQuery<R> query);

   @NonNull
   <T> T persist(@NonNull InsertOperation<T> operation);

   @NonNull
   <T> T update(@NonNull UpdateOperation<T> operation);

   @NonNull
   default <T> Iterable<T> updateAll(@NonNull UpdateBatchOperation<T> operation) {
      return (Iterable<T>)operation.split().stream().map(this::update).collect(Collectors.toList());
   }

   @NonNull
   default <T> Iterable<T> persistAll(@NonNull InsertBatchOperation<T> operation) {
      return (Iterable<T>)operation.split().stream().map(this::persist).collect(Collectors.toList());
   }

   @NonNull
   Optional<Number> executeUpdate(@NonNull PreparedQuery<?, Number> preparedQuery);

   @NonNull
   default Optional<Number> executeDelete(@NonNull PreparedQuery<?, Number> preparedQuery) {
      return this.executeUpdate(preparedQuery);
   }

   <T> int delete(@NonNull DeleteOperation<T> operation);

   <T> Optional<Number> deleteAll(@NonNull DeleteBatchOperation<T> operation);

   default ConversionService<?> getConversionService() {
      return ConversionService.SHARED;
   }
}
