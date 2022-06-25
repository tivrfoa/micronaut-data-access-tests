package io.micronaut.data.runtime.operations;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.runtime.DeleteBatchOperation;
import io.micronaut.data.model.runtime.DeleteOperation;
import io.micronaut.data.model.runtime.InsertBatchOperation;
import io.micronaut.data.model.runtime.InsertOperation;
import io.micronaut.data.model.runtime.PagedQuery;
import io.micronaut.data.model.runtime.PreparedQuery;
import io.micronaut.data.model.runtime.UpdateBatchOperation;
import io.micronaut.data.model.runtime.UpdateOperation;
import io.micronaut.data.operations.async.AsyncRepositoryOperations;
import io.micronaut.data.operations.reactive.ReactiveRepositoryOperations;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class AsyncFromReactiveAsyncRepositoryOperation implements AsyncRepositoryOperations {
   private final ReactiveRepositoryOperations reactiveOperations;
   private final Executor executor;

   public AsyncFromReactiveAsyncRepositoryOperation(ReactiveRepositoryOperations reactiveOperations, Executor executor) {
      this.reactiveOperations = reactiveOperations;
      this.executor = executor;
   }

   @NonNull
   @Override
   public Executor getExecutor() {
      return this.executor;
   }

   @NonNull
   @Override
   public <T> CompletionStage<T> findOne(@NonNull Class<T> type, @NonNull Serializable id) {
      return this.toCompletionStage(this.reactiveOperations.findOne(type, id));
   }

   @Override
   public <T> CompletionStage<Boolean> exists(@NonNull PreparedQuery<T, Boolean> preparedQuery) {
      return this.toCompletionStage(this.reactiveOperations.exists(preparedQuery));
   }

   @NonNull
   @Override
   public <T, R> CompletionStage<R> findOne(@NonNull PreparedQuery<T, R> preparedQuery) {
      return this.toCompletionStage(this.reactiveOperations.findOne(preparedQuery));
   }

   @NonNull
   @Override
   public <T> CompletionStage<T> findOptional(@NonNull Class<T> type, @NonNull Serializable id) {
      return this.toCompletionStage(this.reactiveOperations.findOptional(type, id));
   }

   @NonNull
   @Override
   public <T, R> CompletionStage<R> findOptional(@NonNull PreparedQuery<T, R> preparedQuery) {
      return this.toCompletionStage(this.reactiveOperations.findOptional(preparedQuery));
   }

   @NonNull
   @Override
   public <T> CompletionStage<Iterable<T>> findAll(PagedQuery<T> pagedQuery) {
      return this.toIterableCompletionStage(this.reactiveOperations.findAll(pagedQuery));
   }

   @NonNull
   @Override
   public <T> CompletionStage<Long> count(PagedQuery<T> pagedQuery) {
      return this.toCompletionStage(this.reactiveOperations.count(pagedQuery));
   }

   @NonNull
   @Override
   public <T, R> CompletionStage<Iterable<R>> findAll(@NonNull PreparedQuery<T, R> preparedQuery) {
      return this.toIterableCompletionStage(this.reactiveOperations.findAll(preparedQuery));
   }

   @NonNull
   @Override
   public <T> CompletionStage<T> persist(@NonNull InsertOperation<T> operation) {
      return this.toCompletionStage(this.reactiveOperations.persist(operation));
   }

   @NonNull
   @Override
   public <T> CompletionStage<T> update(@NonNull UpdateOperation<T> operation) {
      return this.toCompletionStage(this.reactiveOperations.update(operation));
   }

   @NonNull
   @Override
   public <T> CompletionStage<Number> delete(@NonNull DeleteOperation<T> operation) {
      return this.toCompletionStage(this.reactiveOperations.delete(operation));
   }

   @NonNull
   @Override
   public <T> CompletionStage<Iterable<T>> persistAll(@NonNull InsertBatchOperation<T> operation) {
      return this.toIterableCompletionStage(this.reactiveOperations.persistAll(operation));
   }

   @NonNull
   @Override
   public CompletionStage<Number> executeUpdate(@NonNull PreparedQuery<?, Number> preparedQuery) {
      return this.toCompletionStage(this.reactiveOperations.executeUpdate(preparedQuery));
   }

   @Override
   public CompletionStage<Number> executeDelete(PreparedQuery<?, Number> preparedQuery) {
      return this.toCompletionStage(this.reactiveOperations.executeDelete(preparedQuery));
   }

   @NonNull
   @Override
   public <T> CompletionStage<Number> deleteAll(@NonNull DeleteBatchOperation<T> operation) {
      return this.toCompletionStage(this.reactiveOperations.deleteAll(operation));
   }

   @NonNull
   @Override
   public <R> CompletionStage<Page<R>> findPage(@NonNull PagedQuery<R> pagedQuery) {
      return this.toCompletionStage(this.reactiveOperations.findPage(pagedQuery));
   }

   @NonNull
   @Override
   public <T> CompletionStage<Iterable<T>> updateAll(@NonNull UpdateBatchOperation<T> operation) {
      return this.toIterableCompletionStage(this.reactiveOperations.updateAll(operation));
   }

   private <T> CompletionStage<Iterable<T>> toIterableCompletionStage(Publisher<T> publisher) {
      final CompletableFuture<Iterable<T>> cs = new CompletableFuture();
      publisher.subscribe(new Subscriber<T>() {
         private List<T> values;

         @Override
         public void onSubscribe(Subscription s) {
            this.values = new ArrayList();
            s.request(Long.MAX_VALUE);
         }

         @Override
         public void onNext(T value) {
            synchronized(this) {
               this.values.add(value);
            }
         }

         @Override
         public void onError(Throwable ex) {
            cs.completeExceptionally(ex);
         }

         @Override
         public void onComplete() {
            cs.complete(this.values == null ? Collections.emptyList() : this.values);
         }
      });
      return cs;
   }

   private <T> CompletionStage<T> toCompletionStage(Publisher<T> publisher) {
      final CompletableFuture<T> cs = new CompletableFuture();
      publisher.subscribe(new Subscriber<T>() {
         T value;

         @Override
         public void onSubscribe(Subscription s) {
            s.request(1L);
         }

         @Override
         public void onNext(T value) {
            this.value = value;
         }

         @Override
         public void onError(Throwable ex) {
            cs.completeExceptionally(ex);
         }

         @Override
         public void onComplete() {
            cs.complete(this.value);
         }
      });
      return cs;
   }
}
