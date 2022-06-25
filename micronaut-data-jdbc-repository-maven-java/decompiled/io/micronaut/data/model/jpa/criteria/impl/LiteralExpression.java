package io.micronaut.data.model.jpa.criteria.impl;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.jpa.criteria.IExpression;

@Internal
public final class LiteralExpression<T> implements IExpression<T>, SelectionVisitable {
   private final T value;

   public LiteralExpression(T object) {
      this.value = object;
   }

   @Override
   public void accept(SelectionVisitor selectionVisitor) {
      selectionVisitor.visit(this);
   }

   public T getValue() {
      return this.value;
   }

   @Override
   public boolean isBoolean() {
      return this.value instanceof Boolean;
   }

   @Override
   public boolean isNumeric() {
      return this.value instanceof Number;
   }

   public Class<? extends T> getJavaType() {
      return this.value.getClass();
   }

   public String toString() {
      return "LiteralExpression{value=" + this.value + '}';
   }
}
