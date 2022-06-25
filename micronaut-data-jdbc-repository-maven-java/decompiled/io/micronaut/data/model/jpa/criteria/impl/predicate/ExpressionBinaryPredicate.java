package io.micronaut.data.model.jpa.criteria.impl.predicate;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.jpa.criteria.impl.PredicateVisitable;
import io.micronaut.data.model.jpa.criteria.impl.PredicateVisitor;
import jakarta.persistence.criteria.Expression;

@Internal
public final class ExpressionBinaryPredicate extends AbstractPredicate implements PredicateVisitable {
   private final Expression<?> left;
   private final Expression<?> right;
   private final PredicateBinaryOp op;

   public ExpressionBinaryPredicate(Expression<?> left, Expression<?> right, PredicateBinaryOp op) {
      this.left = left;
      this.right = right;
      this.op = op;
   }

   public PredicateBinaryOp getOp() {
      return this.op;
   }

   public Expression<?> getLeft() {
      return this.left;
   }

   public Expression<?> getRight() {
      return this.right;
   }

   @Override
   public void accept(PredicateVisitor predicateVisitor) {
      predicateVisitor.visit(this);
   }

   public String toString() {
      return "ExpressionBinaryPredicate{left=" + this.left + ", right=" + this.right + ", op=" + this.op + '}';
   }
}
