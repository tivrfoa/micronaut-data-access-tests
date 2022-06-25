package io.micronaut.data.model.jpa.criteria.impl;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.annotation.Join;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.jpa.criteria.IExpression;
import io.micronaut.data.model.jpa.criteria.PersistentEntityCriteriaDelete;
import io.micronaut.data.model.jpa.criteria.PersistentEntityRoot;
import io.micronaut.data.model.jpa.criteria.impl.predicate.ConjunctionPredicate;
import io.micronaut.data.model.jpa.criteria.impl.query.QueryModelPredicateVisitor;
import io.micronaut.data.model.jpa.criteria.impl.util.Joiner;
import io.micronaut.data.model.query.QueryModel;
import io.micronaut.data.model.query.builder.QueryBuilder;
import io.micronaut.data.model.query.builder.QueryResult;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.EntityType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Internal
public abstract class AbstractPersistentEntityCriteriaDelete<T> implements PersistentEntityCriteriaDelete<T>, QueryResultPersistentEntityCriteriaQuery {
   protected Predicate predicate;
   protected PersistentEntityRoot<T> entityRoot;

   @Override
   public QueryModel getQueryModel() {
      if (this.entityRoot == null) {
         throw new IllegalStateException("The root entity must be specified!");
      } else {
         QueryModel qm = QueryModel.from(this.entityRoot.getPersistentEntity());
         Joiner joiner = new Joiner();
         if (this.predicate instanceof PredicateVisitable) {
            PredicateVisitable predicate = (PredicateVisitable)this.predicate;
            predicate.accept(new QueryModelPredicateVisitor(qm));
            predicate.accept(joiner);
         }

         for(Entry<String, Joiner.Joined> e : joiner.getJoins().entrySet()) {
            qm.join(
               (String)e.getKey(),
               (Join.Type)Optional.ofNullable(((Joiner.Joined)e.getValue()).getType()).orElse(Join.Type.DEFAULT),
               ((Joiner.Joined)e.getValue()).getAlias()
            );
         }

         return qm;
      }
   }

   @Override
   public QueryResult buildQuery(QueryBuilder queryBuilder) {
      return queryBuilder.buildDelete(this.getQueryModel());
   }

   @Override
   public abstract PersistentEntityRoot<T> from(Class<T> entityClass);

   @Override
   public abstract PersistentEntityRoot<T> from(PersistentEntity persistentEntity);

   @Override
   public PersistentEntityRoot<T> from(EntityType<T> entity) {
      if (this.entityRoot != null) {
         throw new IllegalStateException("The root entity is already specified!");
      } else {
         return null;
      }
   }

   @Override
   public PersistentEntityCriteriaDelete<T> where(Expression<Boolean> restriction) {
      this.predicate = new ConjunctionPredicate(Collections.singleton((IExpression)restriction));
      return this;
   }

   @Override
   public PersistentEntityCriteriaDelete<T> where(Predicate... restrictions) {
      Objects.requireNonNull(restrictions);
      if (restrictions.length > 0) {
         this.predicate = (Predicate)(restrictions.length == 1
            ? restrictions[0]
            : new ConjunctionPredicate(
               (Collection<? extends IExpression<Boolean>>)((Stream)Arrays.stream(restrictions).sequential())
                  .map(x -> (IExpression)x)
                  .collect(Collectors.toList())
            ));
      } else {
         this.predicate = null;
      }

      return this;
   }

   @Override
   public PersistentEntityRoot<T> getRoot() {
      return this.entityRoot;
   }

   public Predicate getRestriction() {
      return this.predicate;
   }

   public <U> Subquery<U> subquery(Class<U> type) {
      throw new IllegalStateException("Unsupported!");
   }

   public final boolean hasVersionRestriction() {
      return this.entityRoot.getPersistentEntity().getVersion() == null ? false : CriteriaUtils.hasVersionPredicate(this.predicate);
   }
}
