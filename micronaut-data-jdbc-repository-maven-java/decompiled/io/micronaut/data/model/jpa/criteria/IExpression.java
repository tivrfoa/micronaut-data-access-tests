package io.micronaut.data.model.jpa.criteria;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.jpa.criteria.impl.CriteriaUtils;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import java.util.Collection;

public interface IExpression<T> extends Expression<T>, ISelection<T> {
   boolean isBoolean();

   boolean isNumeric();

   @NonNull
   default Predicate isNull() {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   default Predicate isNotNull() {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   default Predicate in(Object... values) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   default Predicate in(Expression<?>... values) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   default Predicate in(Collection<?> values) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   default Predicate in(Expression<Collection<?>> values) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @NonNull
   default <X> Expression<X> as(Class<X> type) {
      throw CriteriaUtils.notSupportedOperation();
   }
}
