package io.micronaut.data.model.jpa.criteria;

import io.micronaut.data.model.PersistentEntity;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.metamodel.EntityType;

public interface PersistentEntityCriteriaDelete<T> extends CriteriaDelete<T> {
   PersistentEntityRoot<T> from(PersistentEntity persistentEntity);

   PersistentEntityRoot<T> from(Class<T> entityClass);

   PersistentEntityRoot<T> from(EntityType<T> entity);

   PersistentEntityRoot<T> getRoot();

   PersistentEntityCriteriaDelete<T> where(Expression<Boolean> restriction);

   PersistentEntityCriteriaDelete<T> where(Predicate... restrictions);
}
