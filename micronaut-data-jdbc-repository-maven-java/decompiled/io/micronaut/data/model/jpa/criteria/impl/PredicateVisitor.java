package io.micronaut.data.model.jpa.criteria.impl;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.jpa.criteria.impl.predicate.ConjunctionPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.DisjunctionPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.ExpressionBinaryPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.NegatedPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyBetweenPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyBinaryPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyInPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyInValuesPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyUnaryPredicate;

@Internal
public interface PredicateVisitor {
   void visit(ConjunctionPredicate conjunction);

   void visit(DisjunctionPredicate disjunction);

   void visit(NegatedPredicate negate);

   void visit(PersistentPropertyInPredicate<?> propertyIn);

   void visit(PersistentPropertyUnaryPredicate<?> propertyOp);

   void visit(PersistentPropertyBetweenPredicate<?> propertyBetweenPredicate);

   void visit(PersistentPropertyBinaryPredicate<?> propertyToExpressionOp);

   void visit(PersistentPropertyInValuesPredicate<?> inValues);

   void visit(ExpressionBinaryPredicate expressionBinaryPredicate);
}
