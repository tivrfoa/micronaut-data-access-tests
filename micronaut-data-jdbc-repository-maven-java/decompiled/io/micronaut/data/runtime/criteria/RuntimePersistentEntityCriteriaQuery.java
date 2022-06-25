package io.micronaut.data.runtime.criteria;

import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.jpa.criteria.PersistentEntityRoot;
import io.micronaut.data.model.jpa.criteria.impl.AbstractPersistentEntityCriteriaQuery;
import io.micronaut.data.model.runtime.RuntimeEntityRegistry;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;

final class RuntimePersistentEntityCriteriaQuery<T> extends AbstractPersistentEntityCriteriaQuery<T> {
   private final RuntimeEntityRegistry runtimeEntityRegistry;

   public RuntimePersistentEntityCriteriaQuery(RuntimeEntityRegistry runtimeEntityRegistry) {
      this.runtimeEntityRegistry = runtimeEntityRegistry;
   }

   @Override
   public <X> PersistentEntityRoot<X> from(Class<X> entityClass) {
      return this.from(this.runtimeEntityRegistry.getEntity(entityClass));
   }

   @Override
   public <X> PersistentEntityRoot<X> from(PersistentEntity persistentEntity) {
      if (this.entityRoot != null) {
         throw new IllegalStateException("The root entity is already specified!");
      } else {
         RuntimePersistentEntityRoot<X> newEntityRoot = new RuntimePersistentEntityRoot<>((RuntimePersistentEntity<X>)persistentEntity);
         this.entityRoot = newEntityRoot;
         return newEntityRoot;
      }
   }
}
