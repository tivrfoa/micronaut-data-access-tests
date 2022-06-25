package io.micronaut.data.model.jpa.criteria.impl.predicate;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.jpa.criteria.IExpression;
import io.micronaut.data.model.jpa.criteria.impl.PredicateVisitor;
import java.util.Collection;

@Internal
public final class ConjunctionPredicate extends AbstractPredicate {
   private final Collection<? extends IExpression<Boolean>> predicates;

   public ConjunctionPredicate(Collection<? extends IExpression<Boolean>> predicates) {
      this.predicates = predicates;
   }

   public Collection<? extends IExpression<Boolean>> getPredicates() {
      return this.predicates;
   }

   @Override
   public void accept(PredicateVisitor predicateVisitor) {
      predicateVisitor.visit(this);
   }

   public String toString() {
      return "ConjunctionPredicate{predicates=" + this.predicates + '}';
   }
}
