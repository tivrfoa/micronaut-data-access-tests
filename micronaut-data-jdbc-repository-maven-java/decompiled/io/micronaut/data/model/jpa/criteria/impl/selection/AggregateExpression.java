package io.micronaut.data.model.jpa.criteria.impl.selection;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.jpa.criteria.IExpression;
import io.micronaut.data.model.jpa.criteria.impl.CriteriaUtils;
import io.micronaut.data.model.jpa.criteria.impl.SelectionVisitable;
import io.micronaut.data.model.jpa.criteria.impl.SelectionVisitor;
import jakarta.persistence.criteria.Expression;

@Internal
public final class AggregateExpression<T, E> implements IExpression<E>, SelectionVisitable {
   private final AggregateType type;
   private final Expression<T> expression;
   private final Class<E> expressionType;

   public AggregateExpression(Expression<T> expression, AggregateType type) {
      this(expression, type, null);
   }

   public AggregateExpression(Expression<T> expression, AggregateType type, Class<E> expressionType) {
      this.expression = expression;
      this.type = type;
      this.expressionType = expressionType;
   }

   @Override
   public void accept(SelectionVisitor selectionVisitor) {
      selectionVisitor.visit(this);
   }

   @Override
   public boolean isBoolean() {
      return false;
   }

   @Override
   public boolean isNumeric() {
      return this.expressionType != null ? CriteriaUtils.isNumeric(this.expressionType) : true;
   }

   public Class<E> getJavaType() {
      return this.expressionType == null ? this.expression.getJavaType() : this.expressionType;
   }

   public AggregateType getType() {
      return this.type;
   }

   public Expression<T> getExpression() {
      return this.expression;
   }

   @Nullable
   public Class<E> getExpressionType() {
      return this.expressionType;
   }
}
