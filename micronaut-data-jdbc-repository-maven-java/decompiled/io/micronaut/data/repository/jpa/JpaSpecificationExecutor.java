package io.micronaut.data.repository.jpa;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.data.repository.jpa.criteria.DeleteSpecification;
import io.micronaut.data.repository.jpa.criteria.PredicateSpecification;
import io.micronaut.data.repository.jpa.criteria.QuerySpecification;
import io.micronaut.data.repository.jpa.criteria.UpdateSpecification;
import java.util.List;
import java.util.Optional;

public interface JpaSpecificationExecutor<T> {
   Optional<T> findOne(@Nullable QuerySpecification<T> spec);

   Optional<T> findOne(@Nullable PredicateSpecification<T> spec);

   @NonNull
   List<T> findAll(@Nullable QuerySpecification<T> spec);

   @NonNull
   List<T> findAll(@Nullable PredicateSpecification<T> spec);

   @NonNull
   Page<T> findAll(@Nullable QuerySpecification<T> spec, Pageable pageable);

   @NonNull
   Page<T> findAll(@Nullable PredicateSpecification<T> spec, Pageable pageable);

   @NonNull
   List<T> findAll(@Nullable QuerySpecification<T> spec, Sort sort);

   @NonNull
   List<T> findAll(@Nullable PredicateSpecification<T> spec, Sort sort);

   long count(@Nullable QuerySpecification<T> spec);

   long count(@Nullable PredicateSpecification<T> spec);

   long deleteAll(@Nullable DeleteSpecification<T> spec);

   long deleteAll(@Nullable PredicateSpecification<T> spec);

   long updateAll(@Nullable UpdateSpecification<T> spec);
}
