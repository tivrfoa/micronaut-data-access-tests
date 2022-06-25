package io.micronaut.data.model.jpa.criteria.impl.predicate;

import io.micronaut.data.model.jpa.criteria.PersistentPropertyPath;
import io.micronaut.data.model.jpa.criteria.impl.PredicateVisitor;
import jakarta.persistence.criteria.Predicate;

public final class PersistentPropertyUnaryPredicate<T> extends AbstractPersistentPropertyPredicate<T> {
   private final PredicateUnaryOp op;

   public PersistentPropertyUnaryPredicate(PersistentPropertyPath<T> persistentPropertyPath, PredicateUnaryOp op) {
      super(persistentPropertyPath);
      this.op = op;
   }

   public PredicateUnaryOp getOp() {
      return this.op;
   }

   @Override
   public Predicate not() {
      PredicateUnaryOp negatedOp = this.op.negate();
      return (Predicate)(negatedOp != null ? new PersistentPropertyUnaryPredicate<>(this.getPropertyPath(), negatedOp) : super.not());
   }

   @Override
   public void accept(PredicateVisitor predicateVisitor) {
      predicateVisitor.visit(this);
   }

   public String toString() {
      return "PersistentPropertyUnaryPredicate{persistentPropertyPath=" + this.persistentPropertyPath + ", op=" + this.op + '}';
   }
}
