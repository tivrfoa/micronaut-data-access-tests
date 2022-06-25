package io.micronaut.data.model.jpa.criteria.impl;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.jpa.criteria.PersistentEntityRoot;
import io.micronaut.data.model.jpa.criteria.PersistentPropertyPath;
import io.micronaut.data.model.jpa.criteria.impl.selection.AggregateExpression;
import io.micronaut.data.model.jpa.criteria.impl.selection.AliasedSelection;
import io.micronaut.data.model.jpa.criteria.impl.selection.CompoundSelection;
import jakarta.persistence.criteria.Predicate;

@Internal
public interface SelectionVisitor {
   void visit(Predicate predicate);

   void visit(PersistentPropertyPath<?> persistentPropertyPath);

   void visit(AliasedSelection<?> aliasedSelection);

   void visit(PersistentEntityRoot<?> entityRoot);

   void visit(CompoundSelection<?> compoundSelection);

   void visit(LiteralExpression<?> literalExpression);

   void visit(AggregateExpression<?, ?> aggregateExpression);

   void visit(IdExpression<?, ?> idExpression);
}
