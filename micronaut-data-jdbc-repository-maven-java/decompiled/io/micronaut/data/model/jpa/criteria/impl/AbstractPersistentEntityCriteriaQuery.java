package io.micronaut.data.model.jpa.criteria.impl;

import io.micronaut.data.annotation.Join;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.Sort;
import io.micronaut.data.model.jpa.criteria.IExpression;
import io.micronaut.data.model.jpa.criteria.PersistentEntityCriteriaQuery;
import io.micronaut.data.model.jpa.criteria.PersistentEntityRoot;
import io.micronaut.data.model.jpa.criteria.PersistentPropertyPath;
import io.micronaut.data.model.jpa.criteria.impl.predicate.ConjunctionPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.DisjunctionPredicate;
import io.micronaut.data.model.jpa.criteria.impl.predicate.PersistentPropertyBinaryPredicate;
import io.micronaut.data.model.jpa.criteria.impl.query.QueryModelPredicateVisitor;
import io.micronaut.data.model.jpa.criteria.impl.query.QueryModelSelectionVisitor;
import io.micronaut.data.model.jpa.criteria.impl.selection.CompoundSelection;
import io.micronaut.data.model.jpa.criteria.impl.util.Joiner;
import io.micronaut.data.model.query.QueryModel;
import io.micronaut.data.model.query.builder.QueryBuilder;
import io.micronaut.data.model.query.builder.QueryResult;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.EntityType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractPersistentEntityCriteriaQuery<T> implements PersistentEntityCriteriaQuery<T>, QueryResultPersistentEntityCriteriaQuery {
   protected Predicate predicate;
   protected Selection<?> selection;
   protected PersistentEntityRoot<?> entityRoot;
   protected List<Order> orders;
   protected int max = -1;
   protected int offset = 0;
   protected boolean forUpdate;
   protected boolean distinct;

   @Override
   public QueryResult buildQuery(QueryBuilder queryBuilder) {
      return queryBuilder.buildQuery(this.getQueryModel());
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

         if (this.selection instanceof SelectionVisitable) {
            SelectionVisitable selection = (SelectionVisitable)this.selection;
            selection.accept(new QueryModelSelectionVisitor(qm, this.distinct));
            selection.accept(joiner);
            SelectionVisitable entityRoot = (SelectionVisitable)this.entityRoot;
            entityRoot.accept(joiner);
         } else {
            SelectionVisitable entityRoot = (SelectionVisitable)this.entityRoot;
            entityRoot.accept(new QueryModelSelectionVisitor(qm, this.distinct));
            entityRoot.accept(joiner);
         }

         if (this.orders != null && !this.orders.isEmpty()) {
            List<Sort.Order> sortOrders = (List)this.orders.stream().map(o -> {
               PersistentPropertyPath<?> propertyPath = CriteriaUtils.requireProperty(o.getExpression());
               joiner.joinIfNeeded(propertyPath);
               String name = propertyPath.getProperty().getName();
               return o.isAscending() ? Sort.Order.asc(name) : Sort.Order.desc(name);
            }).collect(Collectors.toList());
            qm.sort(Sort.of(sortOrders));
         }

         for(Entry<String, Joiner.Joined> e : joiner.getJoins().entrySet()) {
            qm.join(
               (String)e.getKey(),
               (Join.Type)Optional.ofNullable(((Joiner.Joined)e.getValue()).getType()).orElse(Join.Type.DEFAULT),
               ((Joiner.Joined)e.getValue()).getAlias()
            );
         }

         qm.max(this.max);
         qm.offset((long)this.offset);
         if (this.forUpdate) {
            qm.forUpdate();
         }

         return qm;
      }
   }

   @Override
   public PersistentEntityCriteriaQuery<T> max(int max) {
      this.max = max;
      return this;
   }

   @Override
   public PersistentEntityCriteriaQuery<T> offset(int offset) {
      this.offset = offset;
      return this;
   }

   @Override
   public PersistentEntityCriteriaQuery<T> forUpdate(boolean forUpdate) {
      this.forUpdate = forUpdate;
      return this;
   }

   @Override
   public PersistentEntityCriteriaQuery<T> select(Selection<? extends T> selection) {
      this.selection = (Selection)Objects.requireNonNull(selection);
      return this;
   }

   @Override
   public PersistentEntityCriteriaQuery<T> multiselect(Selection<?>... selections) {
      Objects.requireNonNull(selections);
      if (selections.length > 0) {
         this.selection = (Selection<?>)(selections.length == 1 ? selections[0] : new CompoundSelection(Arrays.asList(selections)));
      } else {
         this.selection = null;
      }

      return this;
   }

   @Override
   public PersistentEntityCriteriaQuery<T> multiselect(List<Selection<?>> selectionList) {
      Objects.requireNonNull(selectionList);
      if (!selectionList.isEmpty()) {
         this.selection = (Selection<?>)(selectionList.size() == 1 ? (Selection)selectionList.iterator().next() : new CompoundSelection(selectionList));
      } else {
         this.selection = null;
      }

      return this;
   }

   @Override
   public abstract <X> PersistentEntityRoot<X> from(Class<X> entityClass);

   @Override
   public abstract <X> PersistentEntityRoot<X> from(PersistentEntity persistentEntity);

   @Override
   public <X> PersistentEntityRoot<X> from(EntityType<X> entity) {
      if (this.entityRoot != null) {
         throw new IllegalStateException("The root entity is already specified!");
      } else {
         return null;
      }
   }

   @Override
   public PersistentEntityCriteriaQuery<T> where(Expression<Boolean> restriction) {
      this.predicate = new ConjunctionPredicate(Collections.singleton((IExpression)restriction));
      return this;
   }

   @Override
   public PersistentEntityCriteriaQuery<T> where(Predicate... restrictions) {
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
   public PersistentEntityCriteriaQuery<T> groupBy(Expression<?>... grouping) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @Override
   public PersistentEntityCriteriaQuery<T> groupBy(List<Expression<?>> grouping) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @Override
   public PersistentEntityCriteriaQuery<T> having(Expression<Boolean> restriction) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @Override
   public PersistentEntityCriteriaQuery<T> having(Predicate... restrictions) {
      throw CriteriaUtils.notSupportedOperation();
   }

   @Override
   public PersistentEntityCriteriaQuery<T> orderBy(Order... o) {
      this.orders = Arrays.asList(Objects.requireNonNull(o));
      return this;
   }

   @Override
   public PersistentEntityCriteriaQuery<T> orderBy(List<Order> o) {
      this.orders = (List)Objects.requireNonNull(o);
      return this;
   }

   @Override
   public PersistentEntityCriteriaQuery<T> distinct(boolean distinct) {
      this.distinct = distinct;
      return this;
   }

   public Set<Root<?>> getRoots() {
      return this.entityRoot != null ? Collections.singleton(this.entityRoot) : Collections.emptySet();
   }

   public List<Expression<?>> getGroupList() {
      throw CriteriaUtils.notSupportedOperation();
   }

   public Predicate getGroupRestriction() {
      throw CriteriaUtils.notSupportedOperation();
   }

   public boolean isDistinct() {
      return false;
   }

   public Class<T> getResultType() {
      return null;
   }

   public List<Order> getOrderList() {
      throw CriteriaUtils.notSupportedOperation();
   }

   public Set<ParameterExpression<?>> getParameters() {
      throw CriteriaUtils.notSupportedOperation();
   }

   public <U> Subquery<U> subquery(Class<U> type) {
      throw CriteriaUtils.notSupportedOperation();
   }

   public Selection<T> getSelection() {
      return this.selection;
   }

   public Predicate getRestriction() {
      return this.predicate;
   }

   public final boolean hasOnlyIdRestriction() {
      return this.isOnlyIdRestriction(this.predicate);
   }

   private boolean isOnlyIdRestriction(Expression<?> predicate) {
      if (predicate instanceof PersistentPropertyBinaryPredicate) {
         PersistentPropertyBinaryPredicate<?> pp = (PersistentPropertyBinaryPredicate)predicate;
         return pp.getProperty() == pp.getProperty().getOwner().getIdentity();
      } else {
         if (predicate instanceof ConjunctionPredicate) {
            ConjunctionPredicate conjunctionPredicate = (ConjunctionPredicate)predicate;
            if (conjunctionPredicate.getPredicates().size() == 1) {
               return this.isOnlyIdRestriction((Expression<?>)conjunctionPredicate.getPredicates().iterator().next());
            }
         }

         if (predicate instanceof DisjunctionPredicate) {
            DisjunctionPredicate disjunctionPredicate = (DisjunctionPredicate)predicate;
            if (disjunctionPredicate.getPredicates().size() == 1) {
               return this.isOnlyIdRestriction((Expression<?>)disjunctionPredicate.getPredicates().iterator().next());
            }
         }

         return false;
      }
   }

   public final boolean hasVersionRestriction() {
      return this.entityRoot.getPersistentEntity().getVersion() == null ? false : CriteriaUtils.hasVersionPredicate(this.predicate);
   }
}
