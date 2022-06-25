package io.micronaut.data.repository.jpa.criteria;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public interface PredicateSpecification<T> {
   PredicateSpecification<?> ALL = (root, criteriaBuilder) -> null;

   @NonNull
   static <T> PredicateSpecification<T> where(@Nullable PredicateSpecification<T> spec) {
      return spec == null ? ALL : spec;
   }

   @NonNull
   static <T> PredicateSpecification<T> not(@Nullable PredicateSpecification<T> spec) {
      return spec == null ? ALL : (root, criteriaBuilder) -> criteriaBuilder.not(spec.toPredicate(root, criteriaBuilder));
   }

   @NonNull
   default PredicateSpecification<T> and(@Nullable PredicateSpecification<T> other) {
      return SpecificationComposition.composed(this, other, CriteriaBuilder::and);
   }

   @NonNull
   default PredicateSpecification<T> or(@Nullable PredicateSpecification<T> other) {
      return SpecificationComposition.composed(this, other, CriteriaBuilder::or);
   }

   @Nullable
   Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaBuilder criteriaBuilder);
}
