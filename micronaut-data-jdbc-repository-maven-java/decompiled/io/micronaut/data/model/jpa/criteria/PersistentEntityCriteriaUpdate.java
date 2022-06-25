package io.micronaut.data.model.jpa.criteria;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.PersistentEntity;
import jakarta.persistence.criteria.CriteriaUpdate;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import java.util.Set;

public interface PersistentEntityCriteriaUpdate<T> extends CriteriaUpdate<T> {
   @NonNull
   PersistentEntityRoot<T> from(@NonNull PersistentEntity persistentEntity);

   @NonNull
   PersistentEntityRoot<T> from(@NonNull Class<T> entityClass);

   @NonNull
   PersistentEntityRoot<T> from(@NonNull EntityType<T> entity);

   @NonNull
   PersistentEntityRoot<T> getRoot();

   @NonNull
   <Y, X extends Y> PersistentEntityCriteriaUpdate<T> set(@NonNull SingularAttribute<? super T, Y> attribute, @Nullable X value);

   @NonNull
   <Y> PersistentEntityCriteriaUpdate<T> set(@NonNull SingularAttribute<? super T, Y> attribute, @NonNull Expression<? extends Y> value);

   @NonNull
   <Y, X extends Y> PersistentEntityCriteriaUpdate<T> set(@NonNull Path<Y> attribute, @Nullable X value);

   @NonNull
   <Y> PersistentEntityCriteriaUpdate<T> set(@NonNull Path<Y> attribute, @NonNull Expression<? extends Y> value);

   @NonNull
   PersistentEntityCriteriaUpdate<T> set(@NonNull String attributeName, @Nullable Object value);

   @NonNull
   PersistentEntityCriteriaUpdate<T> where(@NonNull Expression<Boolean> restriction);

   @NonNull
   PersistentEntityCriteriaUpdate<T> where(@NonNull Predicate... restrictions);

   @NonNull
   Set<ParameterExpression<?>> getParameters();
}
