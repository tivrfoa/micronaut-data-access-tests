package io.micronaut.data.repository.async;

import io.micronaut.core.annotation.NonBlocking;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.repository.GenericRepository;
import java.util.concurrent.CompletableFuture;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@NonBlocking
public interface AsyncCrudRepository<E, ID> extends GenericRepository<E, ID> {
   @NonNull
   <S extends E> CompletableFuture<S> save(@Valid @NotNull @NonNull S entity);

   @NonNull
   <S extends E> CompletableFuture<S> update(@Valid @NotNull @NonNull S entity);

   @NonNull
   <S extends E> CompletableFuture<? extends Iterable<S>> updateAll(@Valid @NotNull @NonNull Iterable<S> entities);

   @NonNull
   <S extends E> CompletableFuture<? extends Iterable<S>> saveAll(@Valid @NotNull @NonNull Iterable<S> entities);

   @NonNull
   CompletableFuture<E> findById(@NotNull @NonNull ID id);

   @NonNull
   CompletableFuture<Boolean> existsById(@NotNull @NonNull ID id);

   @NonNull
   CompletableFuture<? extends Iterable<E>> findAll();

   @NonNull
   CompletableFuture<Long> count();

   @NonNull
   CompletableFuture<Void> deleteById(@NonNull @NotNull ID id);

   @NonNull
   CompletableFuture<Void> delete(@NonNull @NotNull E entity);

   @NonNull
   CompletableFuture<Void> deleteAll(@NonNull @NotNull Iterable<? extends E> entities);

   @NonNull
   CompletableFuture<Void> deleteAll();
}
