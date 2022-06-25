package io.micronaut.data.repository.jpa.criteria;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public interface UpdateSpecification<T> {
   @NonNull
   default UpdateSpecification<T> where(@Nullable PredicateSpecification<T> spec) {
      return spec == null ? this : SpecificationComposition.composed(this, spec, CriteriaBuilder::and);
   }

   @Nullable
   Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaUpdate<?> query, @NonNull CriteriaBuilder criteriaBuilder);
}
