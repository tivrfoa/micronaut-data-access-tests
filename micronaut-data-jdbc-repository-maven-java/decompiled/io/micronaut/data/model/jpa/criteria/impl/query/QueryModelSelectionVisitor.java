package io.micronaut.data.model.jpa.criteria.impl.query;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.PersistentProperty;
import io.micronaut.data.model.jpa.criteria.PersistentEntityRoot;
import io.micronaut.data.model.jpa.criteria.PersistentPropertyPath;
import io.micronaut.data.model.jpa.criteria.impl.CriteriaUtils;
import io.micronaut.data.model.jpa.criteria.impl.IdExpression;
import io.micronaut.data.model.jpa.criteria.impl.LiteralExpression;
import io.micronaut.data.model.jpa.criteria.impl.SelectionVisitable;
import io.micronaut.data.model.jpa.criteria.impl.SelectionVisitor;
import io.micronaut.data.model.jpa.criteria.impl.selection.AggregateExpression;
import io.micronaut.data.model.jpa.criteria.impl.selection.AliasedSelection;
import io.micronaut.data.model.jpa.criteria.impl.selection.CompoundSelection;
import io.micronaut.data.model.query.QueryModel;
import io.micronaut.data.model.query.factory.Projections;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Selection;

@Internal
public final class QueryModelSelectionVisitor implements SelectionVisitor {
   private final QueryModel queryModel;
   private final boolean distinct;
   private String alias;
   private boolean isCompound;

   public QueryModelSelectionVisitor(QueryModel queryModel, boolean distinct) {
      this.queryModel = queryModel;
      this.distinct = distinct;
   }

   @Override
   public void visit(Predicate predicate) {
      throw new IllegalStateException("Predicate is not allowed as a selection!");
   }

   @Override
   public void visit(PersistentPropertyPath<?> persistentPropertyPath) {
      if (this.distinct) {
         this.addProjection(Projections.distinct(persistentPropertyPath.getProperty().getName()));
      } else {
         this.addProjection(Projections.property(persistentPropertyPath.getProperty().getName()));
      }

   }

   @Override
   public void visit(AggregateExpression<?, ?> aggregateExpression) {
      this.addProjection(this.getProjection(aggregateExpression));
   }

   private QueryModel.Projection getProjection(AggregateExpression<?, ?> aggregateExpression) {
      Expression<?> expression = aggregateExpression.getExpression();
      switch(aggregateExpression.getType()) {
         case SUM:
            return Projections.sum(CriteriaUtils.requireProperty(expression).getProperty().getName());
         case AVG:
            return Projections.avg(CriteriaUtils.requireProperty(expression).getProperty().getName());
         case MAX:
            return Projections.max(CriteriaUtils.requireProperty(expression).getProperty().getName());
         case MIN:
            return Projections.min(CriteriaUtils.requireProperty(expression).getProperty().getName());
         case COUNT:
            if (expression instanceof PersistentEntityRoot) {
               return Projections.count();
            } else {
               if (expression instanceof PersistentPropertyPath) {
                  return Projections.count();
               }

               throw new IllegalStateException("Illegal expression: " + expression + " for count selection!");
            }
         case COUNT_DISTINCT:
            if (expression instanceof PersistentEntityRoot) {
               return Projections.countDistinct(((PersistentPropertyPath)expression).getProperty().getName());
            } else {
               if (expression instanceof PersistentPropertyPath) {
                  return Projections.countDistinct(((PersistentPropertyPath)expression).getProperty().getName());
               }

               throw new IllegalStateException("Illegal expression: " + expression + " for count distinct selection!");
            }
         default:
            throw new IllegalStateException("Unknown aggregation: " + aggregateExpression.getExpression());
      }
   }

   @Override
   public void visit(CompoundSelection<?> compoundSelection) {
      this.isCompound = true;

      for(Selection<?> selection : compoundSelection.getCompoundSelectionItems()) {
         if (!(selection instanceof SelectionVisitable)) {
            throw new IllegalStateException("Unknown selection object: " + selection);
         }

         ((SelectionVisitable)selection).accept(this);
      }

      this.isCompound = false;
   }

   @Override
   public void visit(PersistentEntityRoot<?> entityRoot) {
      if (this.isCompound) {
         throw new IllegalStateException("Entity root cannot be in compound selection!");
      } else {
         if (this.distinct) {
            this.addProjection(Projections.distinct());
         }

      }
   }

   @Override
   public void visit(LiteralExpression<?> literalExpression) {
      this.addProjection(Projections.literal(literalExpression.getValue()));
   }

   @Override
   public void visit(IdExpression<?, ?> idExpression) {
      PersistentEntityRoot<?> root = idExpression.getRoot();
      PersistentEntity persistentEntity = root.getPersistentEntity();
      if (persistentEntity.hasCompositeIdentity()) {
         for(PersistentProperty persistentProperty : persistentEntity.getCompositeIdentity()) {
            if (this.distinct) {
               this.addProjection(Projections.distinct(persistentProperty.getName()));
            } else {
               this.addProjection(Projections.property(persistentProperty.getName()));
            }
         }
      } else {
         PersistentProperty identity = persistentEntity.getIdentity();
         if (this.distinct) {
            this.addProjection(Projections.distinct(identity.getName()));
         } else {
            this.addProjection(Projections.property(identity.getName()));
         }
      }

   }

   @Override
   public void visit(AliasedSelection<?> aliasedSelection) {
      this.alias = aliasedSelection.getAlias();
      ((SelectionVisitable)aliasedSelection.getSelection()).accept(this);
      this.alias = null;
   }

   private void addProjection(QueryModel.Projection projection) {
      if (projection instanceof QueryModel.PropertyProjection && this.alias != null) {
         ((QueryModel.PropertyProjection)projection).setAlias(this.alias);
      }

      this.queryModel.projections().add(projection);
   }
}
