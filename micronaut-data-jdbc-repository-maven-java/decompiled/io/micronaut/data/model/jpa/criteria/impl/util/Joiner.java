package io.micronaut.data.model.jpa.criteria.impl.util;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.annotation.Relation;
import io.micronaut.data.model.Association;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.jpa.criteria.IExpression;
import io.micronaut.data.model.jpa.criteria.PersistentAssociationPath;
import io.micronaut.data.model.jpa.criteria.PersistentEntityRoot;
import io.micronaut.data.model.jpa.criteria.PersistentPropertyPath;
import io.micronaut.data.model.jpa.criteria.impl.IdExpression;
import io.micronaut.data.model.jpa.criteria.impl.LiteralExpression;
import io.micronaut.data.model.jpa.criteria.impl.PredicateVisitable;
import io.micronaut.data.model.jpa.criteria.impl.PredicateVisitor;
import io.micronaut.data.model.jpa.criteria.impl.SelectionVisitable;
import io.micronaut.data.model.jpa.criteria.impl.SelectionVisitor;
import io.micronaut.data.model.jpa.criteria.impl.predicate.ConjunctionPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.DisjunctionPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.ExpressionBinaryPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.NegatedPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyBetweenPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyBinaryPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyInPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyInValuesPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyUnaryPredicate;
import io.micronaut.data.model.jpa.criteria.impl.selection.AggregateExpression;
import io.micronaut.data.model.jpa.criteria.impl.selection.AliasedSelection;
import io.micronaut.data.model.jpa.criteria.impl.selection.CompoundSelection;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Selection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Internal
public class Joiner implements SelectionVisitor, PredicateVisitor {
   private final Map<String, Joiner.Joined> joins = new TreeMap(Comparator.comparingInt(String::length).thenComparing(String::compareTo));

   public Map<String, Joiner.Joined> getJoins() {
      return this.joins;
   }

   public void joinIfNeeded(PersistentPropertyPath<?> persistentPropertyPath) {
      this.joinIfNeeded(persistentPropertyPath, false);
   }

   private void joinIfNeeded(PersistentPropertyPath<?> persistentPropertyPath, boolean isPredicate) {
      PersistentProperty property = persistentPropertyPath.getProperty();
      if (!isPredicate || !(property instanceof Association)) {
         this.joinAssociation(persistentPropertyPath);
      }
   }

   private void joinAssociation(Path<?> path) {
      if (path instanceof PersistentAssociationPath) {
         PersistentAssociationPath<?, ?> associationPath = (PersistentAssociationPath)path;
         if (associationPath.getAssociation().getKind() == Relation.Kind.EMBEDDED) {
            this.joinAssociation(path.getParentPath());
         } else {
            this.join(associationPath);
         }
      } else if (path instanceof PersistentPropertyPath) {
         PersistentPropertyPath persistentPropertyPath = (PersistentPropertyPath)path;
         Path parentPath = persistentPropertyPath.getParentPath();
         if (parentPath instanceof PersistentAssociationPath) {
            PersistentAssociationPath parent = (PersistentAssociationPath)parentPath;
            if (parent.getAssociation().getAssociatedEntity().getIdentity() == persistentPropertyPath.getProperty()) {
               return;
            }
         }

         this.joinAssociation(parentPath);
      }

   }

   private void join(PersistentAssociationPath<?, ?> associationPath) {
      Joiner.Joined joined = (Joiner.Joined)this.joins
         .computeIfAbsent(
            associationPath.getPathAsString(), s -> new Joiner.Joined(associationPath, associationPath.getAssociationJoinType(), associationPath.getAlias())
         );
      if (joined.association != associationPath) {
         io.micronaut.data.annotation.Join.Type type = associationPath.getAssociationJoinType();
         if (type != io.micronaut.data.annotation.Join.Type.DEFAULT) {
            joined.type = type;
         }

         String alias = associationPath.getAlias();
         if (alias != null) {
            joined.alias = alias;
         }

      }
   }

   @Override
   public void visit(PersistentEntityRoot<?> entityRoot) {
      Set<? extends Join<?, ?>> joins = entityRoot.getJoins();
      this.visitJoins(joins);
   }

   private void visitJoins(Set<? extends Join<?, ?>> joins) {
      for(Join<?, ?> join : joins) {
         if (join instanceof PersistentAssociationPath) {
            PersistentAssociationPath persistentAssociationPath = (PersistentAssociationPath)join;
            if (persistentAssociationPath.getAssociationJoinType() != null) {
               this.joinIfNeeded(persistentAssociationPath, false);
               this.visitJoins(join.getJoins());
            }
         }
      }

   }

   private void visitPredicateExpression(Expression<?> expression) {
      if (expression instanceof PredicateVisitable) {
         ((PredicateVisitable)expression).accept(this);
      } else if (expression instanceof PersistentPropertyPath) {
         this.joinIfNeeded((PersistentPropertyPath<?>)expression, true);
      }

   }

   private void visitSelectionExpression(Expression<?> expression) {
      if (expression instanceof PersistentPropertyPath) {
         this.joinIfNeeded((PersistentPropertyPath<?>)expression, false);
      }

   }

   @Override
   public void visit(PersistentPropertyPath<?> persistentPropertyPath) {
      this.joinIfNeeded(persistentPropertyPath, false);
   }

   @Override
   public void visit(Predicate predicate) {
   }

   @Override
   public void visit(AliasedSelection<?> aliasedSelection) {
      ((SelectionVisitable)aliasedSelection.getSelection()).accept(this);
   }

   @Override
   public void visit(CompoundSelection<?> compoundSelection) {
      for(Selection<?> selection : compoundSelection.getCompoundSelectionItems()) {
         if (!(selection instanceof SelectionVisitable)) {
            throw new IllegalStateException("Unknown selection object: " + selection);
         }

         ((SelectionVisitable)selection).accept(this);
      }

   }

   @Override
   public void visit(LiteralExpression<?> literalExpression) {
   }

   @Override
   public void visit(IdExpression<?, ?> idExpression) {
   }

   @Override
   public void visit(AggregateExpression<?, ?> aggregateExpression) {
      this.visitSelectionExpression(aggregateExpression.getExpression());
   }

   @Override
   public void visit(ConjunctionPredicate conjunction) {
      for(IExpression<Boolean> expression : conjunction.getPredicates()) {
         this.visitPredicateExpression(expression);
      }

   }

   @Override
   public void visit(DisjunctionPredicate disjunction) {
      for(IExpression<Boolean> expression : disjunction.getPredicates()) {
         this.visitPredicateExpression(expression);
      }

   }

   @Override
   public void visit(NegatedPredicate negate) {
      this.visitPredicateExpression(negate.getNegated());
   }

   @Override
   public void visit(PersistentPropertyInPredicate<?> propertyIn) {
      this.joinIfNeeded(propertyIn.getPropertyPath(), true);
   }

   @Override
   public void visit(PersistentPropertyUnaryPredicate<?> propertyOp) {
      this.joinIfNeeded(propertyOp.getPropertyPath(), true);
   }

   @Override
   public void visit(PersistentPropertyBetweenPredicate<?> propertyBetweenPredicate) {
      this.joinIfNeeded(propertyBetweenPredicate.getPropertyPath(), true);
   }

   @Override
   public void visit(PersistentPropertyBinaryPredicate<?> propertyToExpressionOp) {
      this.joinIfNeeded(propertyToExpressionOp.getPropertyPath(), true);
      this.visitPredicateExpression(propertyToExpressionOp.getExpression());
   }

   @Override
   public void visit(PersistentPropertyInValuesPredicate<?> inValues) {
      this.joinIfNeeded(inValues.getPropertyPath(), true);
      inValues.getValues().forEach(this::visitPredicateExpression);
   }

   @Override
   public void visit(ExpressionBinaryPredicate expressionBinaryPredicate) {
      this.visitPredicateExpression(expressionBinaryPredicate.getLeft());
      this.visitPredicateExpression(expressionBinaryPredicate.getRight());
   }

   @Internal
   public static final class Joined {
      private final PersistentAssociationPath<?, ?> association;
      private io.micronaut.data.annotation.Join.Type type;
      private String alias;

      public Joined(PersistentAssociationPath<?, ?> association, io.micronaut.data.annotation.Join.Type type, String alias) {
         this.association = association;
         this.type = type;
         this.alias = alias;
      }

      public PersistentAssociationPath<?, ?> getAssociation() {
         return this.association;
      }

      public io.micronaut.data.annotation.Join.Type getType() {
         return this.type;
      }

      public void setType(io.micronaut.data.annotation.Join.Type type) {
         this.type = type;
      }

      public String getAlias() {
         return this.alias;
      }

      public void setAlias(String alias) {
         this.alias = alias;
      }
   }
}
