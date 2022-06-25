package io.micronaut.data.runtime.operations;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.data.exceptions.EmptyResultException;
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
import io.micronaut.data.operations.async.AsyncRepositoryOperations;
import io.micronaut.transaction.support.TransactionSynchronizationManager;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class ExecutorAsyncOperations implements AsyncRepositoryOperations {
   private final RepositoryOperations datastore;
   private final Executor executor;

   public ExecutorAsyncOperations(@NonNull RepositoryOperations operations, @NonNull Executor executor) {
      ArgumentUtils.requireNonNull("operations", operations);
      ArgumentUtils.requireNonNull("executor", executor);
      this.datastore = operations;
      this.executor = executor;
   }

   private <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
      Supplier<T> decoratedTxSupplier = TransactionSynchronizationManager.decorateToPropagateState(supplier);
      return CompletableFuture.supplyAsync(decoratedTxSupplier, this.executor);
   }

   @Override
   public Executor getExecutor() {
      return this.executor;
   }

   @NonNull
   public <T> CompletableFuture<T> findOne(@NonNull Class<T> type, @NonNull Serializable id) {
      return this.supplyAsync(() -> {
         T r = this.datastore.findOne(type, id);
         if (r != null) {
            return r;
         } else {
            throw new EmptyResultException();
         }
      });
   }

   public <T> CompletableFuture<Boolean> exists(@NonNull PreparedQuery<T, Boolean> preparedQuery) {
      return this.supplyAsync(() -> this.datastore.exists(preparedQuery));
   }

   @NonNull
   public <T, R> CompletableFuture<R> findOne(@NonNull PreparedQuery<T, R> preparedQuery) {
      return this.supplyAsync(() -> {
         R r = this.datastore.findOne(preparedQuery);
         if (r != null) {
            return r;
         } else {
            throw new EmptyResultException();
         }
      });
   }

   @NonNull
   public <T> CompletableFuture<T> findOptional(@NonNull Class<T> type, @NonNull Serializable id) {
      return this.supplyAsync(() -> {
         T r = this.datastore.findOne(type, id);
         if (r != null) {
            return r;
         } else {
            throw new EmptyResultException();
         }
      });
   }

   @NonNull
   public <T, R> CompletableFuture<R> findOptional(@NonNull PreparedQuery<T, R> preparedQuery) {
      return this.supplyAsync(() -> this.datastore.<T, R>findOne(preparedQuery));
   }

   @NonNull
   public <T> CompletableFuture<Iterable<T>> findAll(@NonNull PagedQuery<T> pagedQuery) {
      return this.supplyAsync(() -> this.datastore.findAll(pagedQuery));
   }

   public <T> CompletableFuture<Long> count(@NonNull PagedQuery<T> pagedQuery) {
      return this.supplyAsync(() -> this.datastore.count(pagedQuery));
   }

   @NonNull
   public <T, R> CompletableFuture<Iterable<R>> findAll(@NonNull PreparedQuery<T, R> preparedQuery) {
      return this.supplyAsync(() -> this.datastore.findAll(preparedQuery));
   }

   @NonNull
   public <T> CompletableFuture<T> persist(@NonNull InsertOperation<T> entity) {
      return this.supplyAsync(() -> this.datastore.<T>persist(entity));
   }

   @NonNull
   public <T> CompletableFuture<T> update(@NonNull UpdateOperation<T> operation) {
      return this.supplyAsync(() -> this.datastore.<T>update(operation));
   }

   @NonNull
   public <T> CompletableFuture<Iterable<T>> updateAll(@NonNull UpdateBatchOperation<T> operation) {
      return this.supplyAsync(() -> this.datastore.updateAll(operation));
   }

   @NonNull
   public <T> CompletableFuture<Number> delete(@NonNull DeleteOperation<T> operation) {
      return this.supplyAsync(() -> this.datastore.delete(operation));
   }

   @NonNull
   public <T> CompletableFuture<Iterable<T>> persistAll(@NonNull InsertBatchOperation<T> operation) {
      return this.supplyAsync(() -> this.datastore.persistAll(operation));
   }

   @NonNull
   public CompletableFuture<Number> executeUpdate(@NonNull PreparedQuery<?, Number> preparedQuery) {
      return this.supplyAsync(() -> (Number)this.datastore.executeUpdate(preparedQuery).orElse(0));
   }

   @Override
   public CompletionStage<Number> executeDelete(PreparedQuery<?, Number> preparedQuery) {
      return this.supplyAsync(() -> (Number)this.datastore.executeDelete(preparedQuery).orElse(0));
   }

   @NonNull
   public <T> CompletableFuture<Number> deleteAll(@NonNull DeleteBatchOperation<T> operation) {
      return this.supplyAsync(() -> (Number)this.datastore.deleteAll(operation).orElse(0));
   }

   public <R> CompletableFuture<Page<R>> findPage(@NonNull PagedQuery<R> pagedQuery) {
      return this.supplyAsync(() -> this.datastore.findPage(pagedQuery));
   }
}
