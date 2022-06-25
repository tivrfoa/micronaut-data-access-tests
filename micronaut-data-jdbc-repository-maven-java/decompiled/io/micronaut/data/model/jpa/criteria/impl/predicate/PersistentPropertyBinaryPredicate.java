package io.micronaut.data.model.jpa.criteria.impl.predicate;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.jpa.criteria.PersistentPropertyPath;
import io.micronaut.data.model.jpa.criteria.impl.PredicateVisitor;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

@Internal
public final class PersistentPropertyBinaryPredicate<T> extends AbstractPersistentPropertyPredicate<T> {
   private final Expression<?> expression;
   private final PredicateBinaryOp op;

   public PersistentPropertyBinaryPredicate(PersistentPropertyPath<T> persistentPropertyPath, Expression<?> expression, PredicateBinaryOp op) {
      super(persistentPropertyPath);
      this.expression = expression;
      this.op = op;
   }

   public PredicateBinaryOp getOp() {
      return this.op;
   }

   @Override
   public Predicate not() {
      PredicateBinaryOp negatedOp = this.op.negate();
      return (Predicate)(negatedOp != null ? new PersistentPropertyBinaryPredicate<>(this.getPropertyPath(), this.expression, negatedOp) : super.not());
   }

   public Expression<?> getExpression() {
      return this.expression;
   }

   @Override
   public void accept(PredicateVisitor predicateVisitor) {
      predicateVisitor.visit(this);
   }

   public String toString() {
      return "PersistentPropertyBinaryPredicate{persistentPropertyPath="
         + this.persistentPropertyPath
         + ", expression="
         + this.expression
         + ", op="
         + this.op
         + '}';
   }
}
