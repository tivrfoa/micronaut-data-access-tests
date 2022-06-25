package io.micronaut.data.model.jpa.criteria.impl;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.jpa.criteria.PersistentPropertyPath;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;

@Internal
public final class PersistentPropertyOrder<T> implements Order {
   private final PersistentPropertyPath<T> persistentPropertyPath;
   private final boolean ascending;

   public PersistentPropertyOrder(PersistentPropertyPath<T> persistentPropertyPath, boolean ascending) {
      this.persistentPropertyPath = persistentPropertyPath;
      this.ascending = ascending;
   }

   public Order reverse() {
      return new PersistentPropertyOrder<>(this.persistentPropertyPath, !this.ascending);
   }

   public boolean isAscending() {
      return this.ascending;
   }

   public Expression<?> getExpression() {
      return this.persistentPropertyPath;
   }
}
