package io.micronaut.data.repository.jpa.criteria;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public interface QuerySpecification<T> {
   QuerySpecification<?> ALL = (root, query, criteriaBuilder) -> null;

   @NonNull
   static <T> QuerySpecification<T> not(@Nullable QuerySpecification<T> spec) {
      return spec == null ? ALL : (root, query, criteriaBuilder) -> criteriaBuilder.not(spec.toPredicate(root, query, criteriaBuilder));
   }

   @NonNull
   static <T> QuerySpecification<T> where(@Nullable QuerySpecification<T> spec) {
      return spec == null ? ALL : spec;
   }

   @NonNull
   static <T> QuerySpecification<T> where(@Nullable PredicateSpecification<T> spec) {
      return spec == null ? ALL : (root, query, criteriaBuilder) -> spec.toPredicate(root, criteriaBuilder);
   }

   @NonNull
   default QuerySpecification<T> and(@Nullable QuerySpecification<T> other) {
      return SpecificationComposition.composed(this, other, CriteriaBuilder::and);
   }

   @NonNull
   default QuerySpecification<T> or(@Nullable QuerySpecification<T> other) {
      return SpecificationComposition.composed(this, other, CriteriaBuilder::or);
   }

   @NonNull
   default QuerySpecification<T> and(@Nullable PredicateSpecification<T> other) {
      return SpecificationComposition.composed(this, other, CriteriaBuilder::and);
   }

   @NonNull
   default QuerySpecification<T> or(@Nullable PredicateSpecification<T> other) {
      return SpecificationComposition.composed(this, other, CriteriaBuilder::or);
   }

   @Nullable
   Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder criteriaBuilder);
}
