package io.micronaut.data.model.jpa.criteria.impl.predicate;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.jpa.criteria.IPredicate;
import io.micronaut.data.model.jpa.criteria.impl.PredicateVisitable;
import io.micronaut.data.model.jpa.criteria.impl.SelectionVisitable;
import io.micronaut.data.model.jpa.criteria.impl.SelectionVisitor;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Predicate.BooleanOperator;
import java.util.Collections;
import java.util.List;

@Internal
public abstract class AbstractPredicate implements IPredicate, PredicateVisitable, SelectionVisitable {
   public BooleanOperator getOperator() {
      return BooleanOperator.AND;
   }

   public Predicate not() {
      return new NegatedPredicate(this);
   }

   public boolean isNegated() {
      return false;
   }

   public List<Expression<Boolean>> getExpressions() {
      return Collections.emptyList();
   }

   public Class<? extends Boolean> getJavaType() {
      return Boolean.class;
   }

   @Override
   public String getAlias() {
      return null;
   }

   @Override
   public void accept(SelectionVisitor selectionVisitor) {
      selectionVisitor.visit(this);
   }
}
