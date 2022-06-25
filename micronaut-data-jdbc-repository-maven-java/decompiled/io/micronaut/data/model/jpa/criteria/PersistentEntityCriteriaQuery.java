package io.micronaut.data.model.jpa.criteria;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.PersistentEntity;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.metamodel.EntityType;
import java.util.List;

public interface PersistentEntityCriteriaQuery<T> extends CriteriaQuery<T> {
   @NonNull
   <X> PersistentEntityRoot<X> from(@NonNull PersistentEntity persistentEntity);

   @NonNull
   <X> PersistentEntityRoot<X> from(@NonNull Class<X> entityClass);

   @NonNull
   <X> PersistentEntityRoot<X> from(@NonNull EntityType<X> entity);

   @NonNull
   PersistentEntityCriteriaQuery<T> max(int max);

   @NonNull
   PersistentEntityCriteriaQuery<T> offset(int offset);

   @Internal
   @NonNull
   default PersistentEntityCriteriaQuery<T> forUpdate(boolean forUpdate) {
      return this;
   }

   @NonNull
   PersistentEntityCriteriaQuery<T> select(@NonNull Selection<? extends T> selection);

   @NonNull
   PersistentEntityCriteriaQuery<T> multiselect(@NonNull Selection<?>... selections);

   @NonNull
   PersistentEntityCriteriaQuery<T> multiselect(@NonNull List<Selection<?>> selectionList);

   @NonNull
   PersistentEntityCriteriaQuery<T> where(@NonNull Expression<Boolean> restriction);

   @NonNull
   PersistentEntityCriteriaQuery<T> where(@NonNull Predicate... restrictions);

   @NonNull
   PersistentEntityCriteriaQuery<T> groupBy(@NonNull Expression<?>... grouping);

   @NonNull
   PersistentEntityCriteriaQuery<T> groupBy(@NonNull List<Expression<?>> grouping);

   @NonNull
   PersistentEntityCriteriaQuery<T> having(@NonNull Expression<Boolean> restriction);

   @NonNull
   PersistentEntityCriteriaQuery<T> having(@NonNull Predicate... restrictions);

   @NonNull
   PersistentEntityCriteriaQuery<T> orderBy(@NonNull Order... o);

   @NonNull
   PersistentEntityCriteriaQuery<T> orderBy(@NonNull List<Order> o);

   @NonNull
   PersistentEntityCriteriaQuery<T> distinct(boolean distinct);
}
