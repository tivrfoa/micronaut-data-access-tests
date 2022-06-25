package io.micronaut.data.repository.jpa.reactive;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.data.repository.jpa.criteria.DeleteSpecification;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;
import io.micronaut.data.repository.jpa.criteria.QuerySpecification;
import io.micronaut.data.repository.jpa.criteria.UpdateSpecification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactorJpaSpecificationExecutor<T> extends ReactiveStreamsJpaSpecificationExecutor<T> {
   @NonNull
   Mono<T> findOne(@Nullable QuerySpecification<T> spec);

   @NonNull
   Mono<T> findOne(@Nullable PredicateSpecification<T> spec);

   @NonNull
   Flux<T> findAll(@Nullable QuerySpecification<T> spec);

   @NonNull
   Flux<T> findAll(@Nullable PredicateSpecification<T> spec);

   Mono<Page<T>> findAll(QuerySpecification<T> spec, Pageable pageable);

   Mono<Page<T>> findAll(PredicateSpecification<T> spec, Pageable pageable);

   @NonNull
   Flux<T> findAll(@Nullable QuerySpecification<T> spec, Sort sort);

   @NonNull
   Flux<T> findAll(@Nullable PredicateSpecification<T> spec, Sort sort);

   @NonNull
   Mono<Long> count(@Nullable QuerySpecification<T> spec);

   @NonNull
   Mono<Long> count(@Nullable PredicateSpecification<T> spec);

   @NonNull
   Mono<Long> deleteAll(@Nullable DeleteSpecification<T> spec);

   @NonNull
   Mono<Long> deleteAll(@Nullable PredicateSpecification<T> spec);

   @NonNull
   Mono<Long> updateAll(@Nullable UpdateSpecification<T> spec);
}
