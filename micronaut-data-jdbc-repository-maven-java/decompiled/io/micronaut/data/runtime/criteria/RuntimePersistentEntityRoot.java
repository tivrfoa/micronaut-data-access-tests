package io.micronaut.data.runtime.criteria;

import io.micronaut.data.model.Association;
import io.micronaut.data.model.jpa.criteria.PersistentEntityRoot;
import io.micronaut.data.model.jpa.criteria.impl.CriteriaUtils;
import io.micronaut.data.model.jpa.criteria.impl.SelectionVisitor;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import jakarta.persistence.metamodel.EntityType;
import java.util.Collections;
import java.util.List;

final class RuntimePersistentEntityRoot<T>
   extends AbstractRuntimePersistentEntityJoinSupport<T, T>
   implements RuntimePersistentEntityPath<T>,
   PersistentEntityRoot<T> {
   private final RuntimePersistentEntity<T> runtimePersistentEntity;

   public RuntimePersistentEntityRoot(RuntimePersistentEntity<T> runtimePersistentEntity) {
      this.runtimePersistentEntity = runtimePersistentEntity;
   }

   @Override
   public void accept(SelectionVisitor selectionVisitor) {
      selectionVisitor.visit(this);
   }

   @Override
   public RuntimePersistentEntity<T> getPersistentEntity() {
      return this.runtimePersistentEntity;
   }

   @Override
   public Class<? extends T> getJavaType() {
      return this.runtimePersistentEntity.getIntrospection().getBeanType();
   }

   @Override
   public boolean isBoolean() {
      return false;
   }

   @Override
   public boolean isNumeric() {
      return false;
   }

   public EntityType<T> getModel() {
      throw CriteriaUtils.notSupportedOperation();
   }

   @Override
   protected List<Association> getCurrentPath() {
      return Collections.emptyList();
   }

   public String toString() {
      return "RuntimePersistentEntityRoot{runtimePersistentEntity=" + this.runtimePersistentEntity + '}';
   }
}
