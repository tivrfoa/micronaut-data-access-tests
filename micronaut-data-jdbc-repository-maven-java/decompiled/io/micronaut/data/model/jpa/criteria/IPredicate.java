package io.micronaut.data.model.jpa.criteria;

import jakarta.persistence.criteria.Predicate;

public interface IPredicate extends Predicate, IExpression<Boolean> {
   @Override
   default boolean isBoolean() {
      return true;
   }

   @Override
   default boolean isNumeric() {
      return false;
   }
}
