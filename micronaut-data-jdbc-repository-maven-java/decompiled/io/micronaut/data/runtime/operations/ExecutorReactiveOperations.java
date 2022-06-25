package io.micronaut.data.runtime.operations;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
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
import io.micronaut.data.operations.reactive.ReactiveRepositoryOperations;
import io.micronaut.data.runtime.convert.DataConversionService;
import io.micronaut.transaction.support.TransactionSynchronizationManager;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ExecutorReactiveOperations implements ReactiveRepositoryOperations {
   private final ExecutorAsyncOperations asyncOperations;
   private final ConversionService<?> dataConversionService;

   @Deprecated
   public ExecutorReactiveOperations(@NonNull RepositoryOperations datastore, @NonNull Executor executor) {
      this(datastore, executor, null);
   }

   public ExecutorReactiveOperations(@NonNull RepositoryOperations datastore, @NonNull Executor executor, DataConversionService<?> dataConversionService) {
      this(new ExecutorAsyncOperations(datastore, executor), dataConversionService);
   }

   @Deprecated
   public ExecutorReactiveOperations(@NonNull ExecutorAsyncOperations asyncOperations) {
      this(asyncOperations, null);
   }

   public ExecutorReactiveOperations(@NonNull ExecutorAsyncOperations asyncOperations, DataConversionService dataConversionService) {
      ArgumentUtils.requireNonNull("asyncOperations", asyncOperations);
      this.asyncOperations = asyncOperations;
      this.dataConversionService = (ConversionService<?>)(dataConversionService == null ? ConversionService.SHARED : dataConversionService);
   }

   @NonNull
   @Override
   public <T> Publisher<T> findOne(@NonNull Class<T> type, @NonNull Serializable id) {
      return this.fromCompletableFuture(() -> this.asyncOperations.findOne(type, id));
   }

   @Override
   public <T> Publisher<Boolean> exists(@NonNull PreparedQuery<T, Boolean> preparedQuery) {
      return this.fromCompletableFuture(() -> this.asyncOperations.exists(preparedQuery));
   }

   @NonNull
   @Override
   public <T, R> Publisher<R> findOne(@NonNull PreparedQuery<T, R> preparedQuery) {
      return this.fromCompletableFuture(() -> this.asyncOperations.findOne(preparedQuery));
   }

   @NonNull
   @Override
   public <T> Publisher<T> findOptional(@NonNull Class<T> type, @NonNull Serializable id) {
      return this.fromCompletableFuture(() -> this.asyncOperations.findOptional(type, id));
   }

   @NonNull
   @Override
   public <T, R> Publisher<R> findOptional(@NonNull PreparedQuery<T, R> preparedQuery) {
      return this.fromCompletableFuture(() -> this.asyncOperations.findOptional(preparedQuery))
         .map(
            r -> {
               Argument<R> returnType = preparedQuery.getResultArgument();
               Argument<?> type = (Argument)returnType.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
               return !type.getType().isInstance(r)
                  ? this.dataConversionService.convert(r, type).orElseThrow(() -> new IllegalStateException("Unexpected return type: " + r))
                  : r;
            }
         );
   }

   @NonNull
   @Override
   public <T> Publisher<T> findAll(PagedQuery<T> pagedQuery) {
      return this.fromCompletableFuture(() -> this.asyncOperations.findAll(pagedQuery)).flatMapMany(Flux::fromIterable);
   }

   @NonNull
   @Override
   public <T> Publisher<Long> count(PagedQuery<T> pagedQuery) {
      return this.fromCompletableFuture(() -> this.asyncOperations.count(pagedQuery));
   }

   @NonNull
   @Override
   public <R> Publisher<Page<R>> findPage(@NonNull PagedQuery<R> pagedQuery) {
      return this.fromCompletableFuture(() -> this.asyncOperations.findPage(pagedQuery));
   }

   @NonNull
   @Override
   public <T, R> Publisher<R> findAll(@NonNull PreparedQuery<T, R> preparedQuery) {
      return this.fromCompletableFuture(() -> this.asyncOperations.findAll(preparedQuery)).flatMapMany(Flux::fromIterable);
   }

   @NonNull
   @Override
   public <T> Publisher<T> persist(@NonNull InsertOperation<T> entity) {
      return this.fromCompletableFuture(() -> this.asyncOperations.persist(entity));
   }

   @NonNull
   @Override
   public <T> Publisher<T> update(@NonNull UpdateOperation<T> operation) {
      return this.fromCompletableFuture(() -> this.asyncOperations.update(operation));
   }

   @NonNull
   @Override
   public <T> Publisher<T> updateAll(@NonNull UpdateBatchOperation<T> operation) {
      return this.fromCompletableFuture(() -> this.asyncOperations.updateAll(operation)).flatMapMany(Flux::fromIterable);
   }

   @NonNull
   @Override
   public <T> Publisher<T> persistAll(@NonNull InsertBatchOperation<T> operation) {
      return this.fromCompletableFuture(() -> this.asyncOperations.persistAll(operation)).flatMapMany(Flux::fromIterable);
   }

   @NonNull
   @Override
   public Publisher<Number> executeUpdate(@NonNull PreparedQuery<?, Number> preparedQuery) {
      return this.fromCompletableFuture(() -> this.asyncOperations.executeUpdate(preparedQuery))
         .map(number -> this.convertNumberArgumentIfNecessary(number, preparedQuery.getResultArgument()));
   }

   @NonNull
   @Override
   public <T> Publisher<Number> delete(@NonNull DeleteOperation<T> operation) {
      return this.fromCompletableFuture(() -> this.asyncOperations.delete(operation));
   }

   @NonNull
   @Override
   public <T> Publisher<Number> deleteAll(@NonNull DeleteBatchOperation<T> operation) {
      return this.fromCompletableFuture(() -> this.asyncOperations.deleteAll(operation))
         .map(number -> this.convertNumberArgumentIfNecessary(number, operation.getResultArgument()));
   }

   private <R> Mono<R> fromCompletableFuture(Supplier<CompletableFuture<R>> futureSupplier) {
      Supplier<CompletableFuture<R>> decorated = TransactionSynchronizationManager.decorateToPropagateState(futureSupplier);
      return Mono.fromCompletionStage(decorated);
   }

   @Nullable
   private Number convertNumberArgumentIfNecessary(Number number, Argument<?> argument) {
      Argument<?> firstTypeVar = (Argument)argument.getFirstTypeVariable().orElse(Argument.of(Long.class));
      Class<?> type = firstTypeVar.getType();
      if (type != Object.class && type != Void.class) {
         if (number == null) {
            number = 0;
         }

         return !type.isInstance(number)
            ? (Number)this.dataConversionService
               .convert(number, firstTypeVar)
               .orElseThrow(() -> new IllegalStateException("Unsupported number type for return type: " + firstTypeVar))
            : number;
      } else {
         return null;
      }
   }
}
