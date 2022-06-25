package io.micronaut.data.model.jpa.criteria;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.jpa.criteria.impl.CriteriaUtils;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyInPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyInValuesPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyUnaryPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PredicateUnaryOp;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public interface PersistentPropertyPath<T> extends Path<T>, IExpression<T> {
   @NonNull
   PersistentProperty getProperty();

   @NonNull
   List<Association> getAssociations();

   @NonNull
   default String getPathAsString() {
      StringJoiner joiner = new StringJoiner(".");

      for(Association association : this.getAssociations()) {
         joiner.add(association.getName());
      }

      joiner.add(this.getProperty().getName());
      return joiner.toString();
   }

   @Override
   default boolean isBoolean() {
      return this.getProperty().isAssignable(Boolean.class) || this.getProperty().isAssignable(Boolean.TYPE);
   }

   @Override
   default boolean isNumeric() {
      return CriteriaUtils.isNumeric(this.getJavaType());
   }

   @Override
   default Predicate isNull() {
      return new PersistentPropertyUnaryPredicate<>(this, PredicateUnaryOp.IS_NULL);
   }

   @Override
   default Predicate isNotNull() {
      return new PersistentPropertyUnaryPredicate<>(this, PredicateUnaryOp.IS_NON_NULL);
   }

   @Override
   default Predicate in(Object... values) {
      return new PersistentPropertyInPredicate<>(this, Arrays.asList(Objects.requireNonNull(values)));
   }

   @Override
   default Predicate in(Collection<?> values) {
      return new PersistentPropertyInPredicate<>(this, (Collection<?>)Objects.requireNonNull(values));
   }

   @Override
   default Predicate in(Expression<?>... values) {
      return new PersistentPropertyInValuesPredicate<>(this, Arrays.asList(values));
   }
}
