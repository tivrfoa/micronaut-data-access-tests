package io.micronaut.data.repository.jpa.criteria;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public interface DeleteSpecification<T> {
   DeleteSpecification<?> ALL = (root, query, criteriaBuilder) -> null;

   @NonNull
   static <T> DeleteSpecification<T> not(@Nullable DeleteSpecification<T> spec) {
      return spec == null ? ALL : (root, query, criteriaBuilder) -> criteriaBuilder.not(spec.toPredicate(root, query, criteriaBuilder));
   }

   @NonNull
   static <T> DeleteSpecification<T> where(@Nullable DeleteSpecification<T> spec) {
      return spec == null ? ALL : spec;
   }

   @NonNull
   static <T> DeleteSpecification<T> where(@Nullable PredicateSpecification<T> spec) {
      return spec == null ? ALL : (root, query, criteriaBuilder) -> spec.toPredicate(root, criteriaBuilder);
   }

   @NonNull
   default DeleteSpecification<T> and(@Nullable DeleteSpecification<T> other) {
      return SpecificationComposition.composed(this, other, CriteriaBuilder::and);
   }

   @NonNull
   default DeleteSpecification<T> or(@Nullable DeleteSpecification<T> other) {
      return SpecificationComposition.composed(this, other, CriteriaBuilder::or);
   }

   @NonNull
   default DeleteSpecification<T> and(@Nullable PredicateSpecification<T> other) {
      return SpecificationComposition.composed(this, other, CriteriaBuilder::and);
   }

   @NonNull
   default DeleteSpecification<T> or(@Nullable PredicateSpecification<T> other) {
      return SpecificationComposition.composed(this, other, CriteriaBuilder::or);
   }

   @Nullable
   Predicate toPredicate(@NonNull Root<T> root, @NonNull CriteriaDelete<?> query, @NonNull CriteriaBuilder criteriaBuilder);
}
