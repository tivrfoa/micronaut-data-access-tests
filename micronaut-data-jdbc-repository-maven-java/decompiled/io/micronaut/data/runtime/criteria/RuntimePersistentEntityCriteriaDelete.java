package io.micronaut.data.runtime.criteria;

import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.jpa.criteria.PersistentEntityRoot;
import io.micronaut.data.model.jpa.criteria.impl.AbstractPersistentEntityCriteriaDelete;
import io.micronaut.data.model.runtime.RuntimeEntityRegistry;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;

final class RuntimePersistentEntityCriteriaDelete<T> extends AbstractPersistentEntityCriteriaDelete<T> {
   private final RuntimeEntityRegistry runtimeEntityRegistry;

   public RuntimePersistentEntityCriteriaDelete(RuntimeEntityRegistry runtimeEntityRegistry, Class<T> root) {
      this.runtimeEntityRegistry = runtimeEntityRegistry;
   }

   @Override
   public PersistentEntityRoot<T> from(Class<T> entityClass) {
      return this.from(this.runtimeEntityRegistry.getEntity(entityClass));
   }

   @Override
   public PersistentEntityRoot<T> from(PersistentEntity persistentEntity) {
      if (this.entityRoot != null) {
         throw new IllegalStateException("The root entity is already specified!");
      } else {
         RuntimePersistentEntityRoot<T> newEntityRoot = new RuntimePersistentEntityRoot<>((RuntimePersistentEntity<T>)persistentEntity);
         this.entityRoot = newEntityRoot;
         return newEntityRoot;
      }
   }
}
