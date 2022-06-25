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
import org.reactivestreams.Publisher;

public interface ReactiveStreamsJpaSpecificationExecutor<T> {
   @NonNull
   Publisher<T> findOne(@Nullable QuerySpecification<T> spec);

   @NonNull
   Publisher<T> findOne(@Nullable PredicateSpecification<T> spec);

   @NonNull
   Publisher<T> findAll(@Nullable QuerySpecification<T> spec);

   @NonNull
   Publisher<T> findAll(@Nullable PredicateSpecification<T> spec);

   @NonNull
   Publisher<Page<T>> findAll(@Nullable QuerySpecification<T> spec, Pageable pageable);

   @NonNull
   Publisher<Page<T>> findAll(@Nullable PredicateSpecification<T> spec, Pageable pageable);

   @NonNull
   Publisher<T> findAll(@Nullable QuerySpecification<T> spec, Sort sort);

   @NonNull
   Publisher<T> findAll(@Nullable PredicateSpecification<T> spec, Sort sort);

   @NonNull
   Publisher<Long> count(@Nullable QuerySpecification<T> spec);

   @NonNull
   Publisher<Long> count(@Nullable PredicateSpecification<T> spec);

   @NonNull
   Publisher<Long> deleteAll(@Nullable DeleteSpecification<T> spec);

   @NonNull
   Publisher<Long> deleteAll(@Nullable PredicateSpecification<T> spec);

   @NonNull
   Publisher<Long> updateAll(@Nullable UpdateSpecification<T> spec);
}
