package io.micronaut.data.runtime.criteria;

import io.micronaut.data.model.jpa.criteria.PersistentEntityCriteriaDelete;
import io.micronaut.data.model.jpa.criteria.PersistentEntityCriteriaQuery;
import io.micronaut.data.model.jpa.criteria.PersistentEntityCriteriaUpdate;
import io.micronaut.data.model.jpa.criteria.impl.AbstractCriteriaBuilder;
import io.micronaut.data.model.runtime.RuntimeEntityRegistry;

public class RuntimeCriteriaBuilder extends AbstractCriteriaBuilder {
   private final RuntimeEntityRegistry runtimeEntityRegistry;

   public RuntimeCriteriaBuilder(RuntimeEntityRegistry runtimeEntityRegistry) {
      this.runtimeEntityRegistry = runtimeEntityRegistry;
   }

   @Override
   public PersistentEntityCriteriaQuery<Object> createQuery() {
      return new RuntimePersistentEntityCriteriaQuery<>(this.runtimeEntityRegistry);
   }

   @Override
   public <T> PersistentEntityCriteriaQuery<T> createQuery(Class<T> resultClass) {
      return new RuntimePersistentEntityCriteriaQuery<>(this.runtimeEntityRegistry);
   }

   @Override
   public <T> PersistentEntityCriteriaUpdate<T> createCriteriaUpdate(Class<T> targetEntity) {
      return new RuntimePersistentEntityCriteriaUpdate<>(this.runtimeEntityRegistry);
   }

   @Override
   public <T> PersistentEntityCriteriaDelete<T> createCriteriaDelete(Class<T> targetEntity) {
      return new RuntimePersistentEntityCriteriaDelete<>(this.runtimeEntityRegistry, targetEntity);
   }
}
