package io.micronaut.data.model.jpa.criteria.impl.query;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.jpa.criteria.IExpression;
import io.micronaut.data.model.jpa.criteria.PersistentPropertyPath;
import io.micronaut.data.model.jpa.criteria.impl.IdExpression;
import io.micronaut.data.model.jpa.criteria.impl.LiteralExpression;
import io.micronaut.data.model.jpa.criteria.impl.PredicateVisitable;
import io.micronaut.data.model.jpa.criteria.impl.PredicateVisitor;
import io.micronaut.data.model.jpa.criteria.impl.predicate.AbstractPersistentPropertyPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.ConjunctionPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.DisjunctionPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.ExpressionBinaryPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.NegatedPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyBetweenPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyBinaryPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyInPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyInValuesPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyUnaryPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PredicateBinaryOp;
import io.micronaut.data.model.query.QueryModel;
import io.micronaut.data.model.query.factory.Restrictions;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.ParameterExpression;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Internal
public final class QueryModelPredicateVisitor implements PredicateVisitor {
   private final QueryModel queryModel;
   private QueryModelPredicateVisitor.State state = new QueryModelPredicateVisitor.State();

   public QueryModelPredicateVisitor(QueryModel queryModel) {
      this.queryModel = queryModel;
   }

   public void visit(IExpression<Boolean> expression) {
      if (expression instanceof PredicateVisitable) {
         ((PredicateVisitable)expression).accept(this);
      } else {
         if (!(expression instanceof PersistentPropertyPath)) {
            throw new IllegalStateException("Unknown boolean expression: " + expression);
         }

         PersistentPropertyPath<?> propertyPath = (PersistentPropertyPath)expression;
         this.add(Restrictions.isTrue(this.getPropertyPath(propertyPath)));
      }

   }

   @Override
   public void visit(ConjunctionPredicate conjunction) {
      if (!conjunction.getPredicates().isEmpty()) {
         if (conjunction.getPredicates().size() == 1) {
            this.visit((IExpression<Boolean>)conjunction.getPredicates().iterator().next());
         } else {
            if (this.state.junction != null && !(this.state.junction instanceof QueryModel.Conjunction)) {
               QueryModel.Conjunction junction = new QueryModel.Conjunction();
               QueryModelPredicateVisitor.State prevState = this.pushState();
               this.state.junction = junction;

               for(IExpression<Boolean> expression : conjunction.getPredicates()) {
                  this.visit(expression);
               }

               this.restoreState(prevState);
               this.add(junction);
            } else {
               for(IExpression<Boolean> expression : conjunction.getPredicates()) {
                  this.visit(expression);
               }
            }

         }
      }
   }

   @Override
   public void visit(DisjunctionPredicate disjunction) {
      if (!disjunction.getPredicates().isEmpty()) {
         if (disjunction.getPredicates().size() == 1) {
            this.visit((IExpression<Boolean>)disjunction.getPredicates().iterator().next());
         } else {
            QueryModel.Disjunction junction = new QueryModel.Disjunction();
            QueryModelPredicateVisitor.State prevState = this.pushState();
            this.state.junction = junction;

            for(IExpression<Boolean> expression : disjunction.getPredicates()) {
               this.visit(expression);
            }

            this.restoreState(prevState);
            this.add(junction);
         }
      }
   }

   @Override
   public void visit(NegatedPredicate negate) {
      QueryModelPredicateVisitor.State prevState = this.pushState();
      this.state.negated = true;
      this.visit(negate.getNegated());
      this.restoreState(prevState);
   }

   @Override
   public void visit(PersistentPropertyBinaryPredicate<?> propertyToExpressionOp) {
      PersistentPropertyPath<?> propertyPath = propertyToExpressionOp.getPropertyPath();
      PredicateBinaryOp op = propertyToExpressionOp.getOp();
      Expression<?> expression = propertyToExpressionOp.getExpression();
      this.visitPropertyPathPredicate(propertyPath, expression, op);
   }

   private void visitPropertyPathPredicate(PersistentPropertyPath<?> propertyPath, Expression<?> expression, PredicateBinaryOp op) {
      if (expression instanceof PersistentPropertyPath) {
         this.add(this.getPropertyToPropertyRestriction(op, propertyPath, (PersistentPropertyPath<?>)expression));
      } else if (expression instanceof ParameterExpression) {
         this.add(this.getPropertyToValueRestriction(op, propertyPath, expression));
      } else {
         if (!(expression instanceof LiteralExpression)) {
            throw new IllegalStateException("Unsupported expression: " + expression);
         }

         this.add(this.getPropertyToValueRestriction(op, propertyPath, ((LiteralExpression)expression).getValue()));
      }

   }

   @Override
   public void visit(ExpressionBinaryPredicate expressionBinaryPredicate) {
      Expression<?> left = expressionBinaryPredicate.getLeft();
      PredicateBinaryOp op = expressionBinaryPredicate.getOp();
      if (left instanceof PersistentPropertyPath) {
         this.visitPropertyPathPredicate((PersistentPropertyPath<?>)left, expressionBinaryPredicate.getRight(), op);
      } else {
         if (!(left instanceof IdExpression)) {
            throw new IllegalStateException("Unsupported expression: " + left);
         }

         if (op != PredicateBinaryOp.EQUALS) {
            throw new IllegalStateException("Unsupported ID expression OP: " + op);
         }

         this.add(Restrictions.idEq(this.asValue(expressionBinaryPredicate.getRight())));
      }

   }

   private QueryModel.Criterion getPropertyToValueRestriction(PredicateBinaryOp op, PersistentPropertyPath<?> left, Object right) {
      String leftProperty = this.getPropertyPath(left);
      Object rightProperty = this.asValue(right);
      switch(op) {
         case EQUALS:
            PersistentProperty property = left.getProperty();
            PersistentEntity owner = property.getOwner();
            if (left.getAssociations().isEmpty() && owner.hasIdentity() && owner.getIdentity() == property) {
               return Restrictions.idEq(rightProperty);
            } else {
               if (left.getAssociations().isEmpty() && owner.getVersion() == property) {
                  return Restrictions.versionEq(rightProperty);
               }

               return Restrictions.eq(leftProperty, rightProperty);
            }
         case NOT_EQUALS:
            return Restrictions.ne(leftProperty, rightProperty);
         case GREATER_THAN:
            return Restrictions.gt(leftProperty, rightProperty);
         case GREATER_THAN_OR_EQUALS:
            return Restrictions.gte(leftProperty, rightProperty);
         case LESS_THAN:
            return Restrictions.lt(leftProperty, rightProperty);
         case LESS_THAN_OR_EQUALS:
            return Restrictions.lte(leftProperty, rightProperty);
         case CONTAINS:
            return Restrictions.contains(leftProperty, rightProperty);
         case ENDS_WITH:
            return Restrictions.endsWith(leftProperty, rightProperty);
         case STARTS_WITH:
            return Restrictions.startsWith(leftProperty, rightProperty);
         case ILIKE:
            return Restrictions.ilike(leftProperty, rightProperty);
         case RLIKE:
            return Restrictions.rlike(leftProperty, rightProperty);
         case LIKE:
            return Restrictions.like(leftProperty, rightProperty);
         case REGEX:
            return Restrictions.regex(leftProperty, rightProperty);
         case EQUALS_IGNORE_CASE:
            return Restrictions.eq(leftProperty, rightProperty).ignoreCase(true);
         case NOT_EQUALS_IGNORE_CASE:
            return Restrictions.ne(leftProperty, rightProperty).ignoreCase(true);
         case STARTS_WITH_IGNORE_CASE:
            return Restrictions.startsWith(leftProperty, rightProperty).ignoreCase(true);
         case ENDS_WITH_IGNORE_CASE:
            return Restrictions.endsWith(leftProperty, rightProperty).ignoreCase(true);
         default:
            throw new IllegalStateException("Unsupported property to value operation: " + op);
      }
   }

   private QueryModel.Criterion getPropertyToPropertyRestriction(PredicateBinaryOp op, PersistentPropertyPath<?> left, PersistentPropertyPath<?> right) {
      String leftProperty = this.getPropertyPath(left);
      String rightProperty = this.getPropertyPath(right);
      switch(op) {
         case EQUALS:
            return Restrictions.eqProperty(leftProperty, rightProperty);
         case NOT_EQUALS:
            return Restrictions.neProperty(leftProperty, rightProperty);
         case GREATER_THAN:
            return Restrictions.gtProperty(leftProperty, rightProperty);
         case GREATER_THAN_OR_EQUALS:
            return Restrictions.geProperty(leftProperty, rightProperty);
         case LESS_THAN:
            return Restrictions.ltProperty(leftProperty, rightProperty);
         case LESS_THAN_OR_EQUALS:
            return Restrictions.leProperty(leftProperty, rightProperty);
         default:
            throw new IllegalStateException("Unsupported property to property operation: " + op);
      }
   }

   @Override
   public void visit(PersistentPropertyUnaryPredicate<?> propertyOp) {
      String propertyPath = this.getPropertyPath(propertyOp);
      switch(propertyOp.getOp()) {
         case IS_NULL:
            this.add(Restrictions.isNull(propertyPath));
            break;
         case IS_NON_NULL:
            this.add(Restrictions.isNotNull(propertyPath));
            break;
         case IS_TRUE:
            this.add(Restrictions.isTrue(propertyPath));
            break;
         case IS_FALSE:
            this.add(Restrictions.isFalse(propertyPath));
            break;
         case IS_EMPTY:
            this.add(Restrictions.isEmpty(propertyPath));
            break;
         case IS_NOT_EMPTY:
            this.add(Restrictions.isNotEmpty(propertyPath));
            break;
         default:
            throw new IllegalStateException("Unknown op: " + propertyOp.getOp());
      }

   }

   @Override
   public void visit(PersistentPropertyBetweenPredicate<?> propertyBetweenPredicate) {
      this.add(
         Restrictions.between(
            this.getPropertyPath(propertyBetweenPredicate), this.asValue(propertyBetweenPredicate.getFrom()), this.asValue(propertyBetweenPredicate.getTo())
         )
      );
   }

   @Override
   public void visit(PersistentPropertyInPredicate<?> propertyIn) {
      Collection<?> values = propertyIn.getValues();
      Object value = values;
      if (!values.isEmpty()) {
         Object first = values.iterator().next();
         if (first instanceof ParameterExpression) {
            value = this.asValue(first);
         }
      }

      if (this.state.negated) {
         this.state.negated = false;
         this.add(Restrictions.notIn(this.getPropertyPath(propertyIn), value));
      } else {
         this.add(Restrictions.in(this.getPropertyPath(propertyIn), value));
      }

   }

   @Override
   public void visit(PersistentPropertyInValuesPredicate<?> inValues) {
      Collection<?> values = inValues.getValues();
      if (!values.isEmpty()) {
         Iterator<?> iterator = values.iterator();
         Object first = iterator.next();
         if (first instanceof ParameterExpression) {
            if (iterator.hasNext()) {
               throw new IllegalStateException("Only one parameter is supported for IN expression!");
            }

            if (this.state.negated) {
               this.state.negated = false;
               this.add(Restrictions.notIn(this.getPropertyPath(inValues), this.asValue(first)));
            } else {
               this.add(Restrictions.in(this.getPropertyPath(inValues), this.asValue(first)));
            }

            return;
         }
      }

      if (this.state.negated) {
         this.state.negated = false;
         this.add(Restrictions.notIn(this.getPropertyPath(inValues), values.stream().map(this::asValue).collect(Collectors.toList())));
      } else {
         this.add(Restrictions.in(this.getPropertyPath(inValues), values.stream().map(this::asValue).collect(Collectors.toList())));
      }

   }

   private Object asValue(Object value) {
      return value instanceof LiteralExpression ? ((LiteralExpression)value).getValue() : value;
   }

   private String getPropertyPath(AbstractPersistentPropertyPredicate<?> propertyPredicate) {
      PersistentPropertyPath<?> propertyPath = propertyPredicate.getPropertyPath();
      return this.getPropertyPath(propertyPath);
   }

   private String getPropertyPath(PersistentPropertyPath<?> propertyPath) {
      return this.asPath(propertyPath.getAssociations(), propertyPath.getProperty());
   }

   private String asPath(List<Association> associations, PersistentProperty property) {
      if (associations.isEmpty()) {
         return property.getName();
      } else {
         StringJoiner joiner = new StringJoiner(".");

         for(Association association : associations) {
            joiner.add(association.getName());
         }

         joiner.add(property.getName());
         return joiner.toString();
      }
   }

   private void add(QueryModel.Criterion criterion) {
      if (this.state.negated) {
         QueryModel.Negation negation = new QueryModel.Negation();
         negation.add(criterion);
         criterion = negation;
      }

      if (this.state.junction == null) {
         this.queryModel.add(criterion);
      } else {
         this.state.junction.add(criterion);
      }

   }

   private QueryModelPredicateVisitor.State pushState() {
      QueryModelPredicateVisitor.State prevState = this.state;
      QueryModelPredicateVisitor.State newState = new QueryModelPredicateVisitor.State();
      newState.junction = prevState.junction;
      newState.negated = prevState.negated;
      this.state = newState;
      return prevState;
   }

   private QueryModelPredicateVisitor.State restoreState(QueryModelPredicateVisitor.State state) {
      QueryModelPredicateVisitor.State oldState = this.state;
      this.state = state;
      return oldState;
   }

   private static final class State {
      boolean negated;
      QueryModel.Junction junction;

      private State() {
      }
   }
}
