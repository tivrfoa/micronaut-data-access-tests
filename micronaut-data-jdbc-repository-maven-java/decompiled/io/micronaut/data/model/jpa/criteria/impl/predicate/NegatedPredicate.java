package io.micronaut.data.model.jpa.criteria.impl.predicate;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.jpa.criteria.IExpression;
import io.micronaut.data.model.jpa.criteria.impl.PredicateVisitor;
import jakarta.persistence.criteria.Predicate;

@Internal
public final class NegatedPredicate extends AbstractPredicate {
   private final IExpression<Boolean> negated;

   public NegatedPredicate(IExpression<Boolean> negated) {
      this.negated = negated;
   }

   public IExpression<Boolean> getNegated() {
      return this.negated;
   }

   @Override
   public boolean isNegated() {
      return true;
   }

   @Override
   public Predicate not() {
      if (this.negated instanceof Predicate) {
         return ((Predicate)this.negated).not();
      } else {
         throw new IllegalStateException("Cannot negate predicate: " + this.negated);
      }
   }

   @Override
   public void accept(PredicateVisitor predicateVisitor) {
      predicateVisitor.visit(this);
   }

   public String toString() {
      return "NegatedPredicate{negated=" + this.negated + '}';
   }
}
