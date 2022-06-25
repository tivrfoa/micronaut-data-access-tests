package io.micronaut.data.repository.jpa.criteria;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.io.Serializable;

class SpecificationComposition {
   @NonNull
   static <T> QuerySpecification<T> composed(
      @Nullable QuerySpecification<T> lhs, @Nullable QuerySpecification<T> rhs, SpecificationComposition.Combiner combiner
   ) {
      return (root, query, builder) -> {
         Predicate otherPredicate = toPredicate(lhs, root, query, builder);
         Predicate thisPredicate = toPredicate(rhs, root, query, builder);
         if (thisPredicate == null) {
            return otherPredicate;
         } else {
            return otherPredicate == null ? thisPredicate : combiner.combine(builder, thisPredicate, otherPredicate);
         }
      };
   }

   @NonNull
   static <T> QuerySpecification<T> composed(
      @Nullable QuerySpecification<T> lhs, @Nullable PredicateSpecification<T> rhs, SpecificationComposition.Combiner combiner
   ) {
      return (root, query, builder) -> {
         Predicate otherPredicate = toPredicate(lhs, root, query, builder);
         Predicate thisPredicate = toPredicate(rhs, root, builder);
         if (thisPredicate == null) {
            return otherPredicate;
         } else {
            return otherPredicate == null ? thisPredicate : combiner.combine(builder, thisPredicate, otherPredicate);
         }
      };
   }

   @NonNull
   static <T> UpdateSpecification<T> composed(
      @Nullable UpdateSpecification<T> lhs, @Nullable PredicateSpecification<T> rhs, SpecificationComposition.Combiner combiner
   ) {
      return (root, query, builder) -> {
         Predicate otherPredicate = toPredicate(lhs, root, query, builder);
         Predicate thisPredicate = toPredicate(rhs, root, builder);
         if (thisPredicate == null) {
            return otherPredicate;
         } else {
            return otherPredicate == null ? thisPredicate : combiner.combine(builder, thisPredicate, otherPredicate);
         }
      };
   }

   @NonNull
   static <T> DeleteSpecification<T> composed(
      @Nullable DeleteSpecification<T> lhs, @Nullable DeleteSpecification<T> rhs, SpecificationComposition.Combiner combiner
   ) {
      return (root, query, builder) -> {
         Predicate otherPredicate = toPredicate(lhs, root, query, builder);
         Predicate thisPredicate = toPredicate(rhs, root, query, builder);
         if (thisPredicate == null) {
            return otherPredicate;
         } else {
            return otherPredicate == null ? thisPredicate : combiner.combine(builder, thisPredicate, otherPredicate);
         }
      };
   }

   @NonNull
   static <T> DeleteSpecification<T> composed(
      @Nullable DeleteSpecification<T> lhs, @Nullable PredicateSpecification<T> rhs, SpecificationComposition.Combiner combiner
   ) {
      return (root, query, builder) -> {
         Predicate otherPredicate = toPredicate(lhs, root, query, builder);
         Predicate thisPredicate = toPredicate(rhs, root, builder);
         if (thisPredicate == null) {
            return otherPredicate;
         } else {
            return otherPredicate == null ? thisPredicate : combiner.combine(builder, thisPredicate, otherPredicate);
         }
      };
   }

   @NonNull
   static <T> PredicateSpecification<T> composed(
      @Nullable PredicateSpecification<T> lhs, @Nullable PredicateSpecification<T> rhs, SpecificationComposition.Combiner combiner
   ) {
      return (root, builder) -> {
         Predicate otherPredicate = toPredicate(lhs, root, builder);
         Predicate thisPredicate = toPredicate(rhs, root, builder);
         if (thisPredicate == null) {
            return otherPredicate;
         } else {
            return otherPredicate == null ? thisPredicate : combiner.combine(builder, thisPredicate, otherPredicate);
         }
      };
   }

   @Nullable
   private static <T> Predicate toPredicate(
      @Nullable QuerySpecification<T> specification, @NonNull Root<T> root, @NonNull CriteriaQuery<?> query, @NonNull CriteriaBuilder builder
   ) {
      return specification == null ? null : specification.toPredicate(root, query, builder);
   }

   @Nullable
   private static <T> Predicate toPredicate(
      @Nullable UpdateSpecification<T> specification, @NonNull Root<T> root, @NonNull CriteriaUpdate<?> query, @NonNull CriteriaBuilder builder
   ) {
      return specification == null ? null : specification.toPredicate(root, query, builder);
   }

   @Nullable
   private static <T> Predicate toPredicate(
      @Nullable DeleteSpecification<T> specification, @NonNull Root<T> root, @NonNull CriteriaDelete<?> query, @NonNull CriteriaBuilder builder
   ) {
      return specification == null ? null : specification.toPredicate(root, query, builder);
   }

   @Nullable
   private static <T> Predicate toPredicate(@Nullable PredicateSpecification<T> specification, @NonNull Root<T> root, @NonNull CriteriaBuilder builder) {
      return specification == null ? null : specification.toPredicate(root, builder);
   }

   interface Combiner extends Serializable {
      @NonNull
      Predicate combine(@NonNull CriteriaBuilder builder, @Nullable Predicate lhs, @Nullable Predicate rhs);
   }
}
