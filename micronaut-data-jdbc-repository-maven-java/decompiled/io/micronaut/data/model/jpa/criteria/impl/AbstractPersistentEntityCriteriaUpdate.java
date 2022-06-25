package io.micronaut.data.model.jpa.criteria.impl;

import io.micronaut.data.annotation.Join;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.jpa.criteria.IExpression;
import io.micronaut.data.model.jpa.criteria.PersistentEntityCriteriaUpdate;
import io.micronaut.data.model.jpa.criteria.PersistentEntityRoot;
import io.micronaut.data.model.jpa.criteria.impl.predicate.ConjunctionPredicate;
import io.micronaut.data.model.jpa.criteria.impl.query.QueryModelPredicateVisitor;
import io.micronaut.data.model.jpa.criteria.impl.util.Joiner;
import io.micronaut.data.model.query.QueryModel;
import io.micronaut.data.model.query.builder.QueryBuilder;
import io.micronaut.data.model.query.builder.QueryResult;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractPersistentEntityCriteriaUpdate<T> implements PersistentEntityCriteriaUpdate<T>, QueryResultPersistentEntityCriteriaQuery {
   protected Predicate predicate;
   protected PersistentEntityRoot<T> entityRoot;
   protected Map<String, Object> updateValues = new LinkedHashMap();

   @Override
   public QueryResult buildQuery(QueryBuilder queryBuilder) {
      return queryBuilder.buildUpdate(this.getQueryModel(), this.updateValues);
   }

   @NotNull
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
   public PersistentEntityRoot<T> getRoot() {
      return this.entityRoot;
   }

   @Override
   public <Y, X extends Y> PersistentEntityCriteriaUpdate<T> set(SingularAttribute<? super T, Y> attribute, X value) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @Override
   public <Y> PersistentEntityCriteriaUpdate<T> set(SingularAttribute<? super T, Y> attribute, Expression<? extends Y> value) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @Override
   public <Y, X extends Y> PersistentEntityCriteriaUpdate<T> set(Path<Y> attribute, X value) {
      this.setValue(CriteriaUtils.requireProperty(attribute).getProperty().getName(), value);
      return this;
   }

   @Override
   public <Y> PersistentEntityCriteriaUpdate<T> set(Path<Y> attribute, Expression<? extends Y> value) {
      this.setValue(CriteriaUtils.requireProperty(attribute).getProperty().getName(), CriteriaUtils.requireParameter(value));
      return this;
   }

   @Override
   public PersistentEntityCriteriaUpdate<T> set(String attributeName, Object value) {
      this.setValue(attributeName, value);
      return this;
   }

   protected void setValue(String attributeName, Object value) {
      this.updateValues.put(attributeName, value);
   }

   @Override
   public PersistentEntityCriteriaUpdate<T> where(Expression<Boolean> restriction) {
      this.predicate = new ConjunctionPredicate(Collections.singleton((IExpression)restriction));
      return this;
   }

   @Override
   public PersistentEntityCriteriaUpdate<T> where(Predicate... restrictions) {
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

   public final Predicate getRestriction() {
      return this.predicate;
   }

   public <U> Subquery<U> subquery(Class<U> type) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public final boolean hasVersionRestriction() {
      return this.entityRoot.getPersistentEntity().getVersion() == null ? false : CriteriaUtils.hasVersionPredicate(this.predicate);
   }

   public final Map<String, Object> getUpdateValues() {
      return this.updateValues;
   }

   @Override
   public Set<ParameterExpression<?>> getParameters() {
      return CriteriaUtils.extractPredicateParameters(this.predicate);
   }
}
