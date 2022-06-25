package io.micronaut.data.repository.jpa.async;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.data.repository.jpa.criteria.DeleteSpecification;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;
import io.micronaut.data.repository.jpa.criteria.QuerySpecification;
import io.micronaut.data.repository.jpa.criteria.UpdateSpecification;
import java.util.concurrent.CompletableFuture;

public interface AsyncJpaSpecificationExecutor<T> {
   @NonNull
   <S extends T> CompletableFuture<S> findOne(@Nullable QuerySpecification<T> spec);

   @NonNull
   <S extends T> CompletableFuture<S> findOne(@Nullable PredicateSpecification<T> spec);

   @NonNull
   <S extends T> CompletableFuture<? extends Iterable<S>> findAll(@Nullable QuerySpecification<T> spec);

   @NonNull
   <S extends T> CompletableFuture<? extends Iterable<S>> findAll(@Nullable PredicateSpecification<T> spec);

   @NonNull
   CompletableFuture<Page<T>> findAll(@Nullable QuerySpecification<T> spec, Pageable pageable);

   @NonNull
   CompletableFuture<Page<T>> findAll(@Nullable PredicateSpecification<T> spec, Pageable pageable);

   @NonNull
   <S extends T> CompletableFuture<? extends Iterable<S>> findAll(@Nullable QuerySpecification<T> spec, Sort sort);

   @NonNull
   <S extends T> CompletableFuture<? extends Iterable<S>> findAll(@Nullable PredicateSpecification<T> spec, Sort sort);

   @NonNull
   CompletableFuture<Long> count(@Nullable QuerySpecification<T> spec);

   @NonNull
   CompletableFuture<Long> count(@Nullable PredicateSpecification<T> spec);

   @NonNull
   CompletableFuture<Long> deleteAll(@Nullable DeleteSpecification<T> spec);

   @NonNull
   CompletableFuture<Long> deleteAll(@Nullable PredicateSpecification<T> spec);

   @NonNull
   CompletableFuture<Long> updateAll(@Nullable UpdateSpecification<T> spec);
}
