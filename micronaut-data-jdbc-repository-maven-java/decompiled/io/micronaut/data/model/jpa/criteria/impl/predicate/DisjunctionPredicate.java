package io.micronaut.data.model.jpa.criteria.impl.predicate;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.jpa.criteria.IExpression;
import io.micronaut.data.model.jpa.criteria.impl.PredicateVisitor;
import jakarta.persistence.criteria.Predicate.BooleanOperator;
import java.util.Collection;

@Internal
public final class DisjunctionPredicate extends AbstractPredicate {
   private final Collection<? extends IExpression<Boolean>> predicates;

   public DisjunctionPredicate(Collection<? extends IExpression<Boolean>> predicates) {
      this.predicates = predicates;
   }

   public Collection<? extends IExpression<Boolean>> getPredicates() {
      return this.predicates;
   }

   @Override
   public BooleanOperator getOperator() {
      return BooleanOperator.OR;
   }

   @Override
   public void accept(PredicateVisitor predicateVisitor) {
      predicateVisitor.visit(this);
   }

   public String toString() {
      return "DisjunctionPredicate{predicates=" + this.predicates + '}';
   }
}
