package io.micronaut.data.model.jpa.criteria.impl;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.jpa.criteria.IExpression;
import io.micronaut.data.model.jpa.criteria.PersistentEntityRoot;

@Internal
public final class IdExpression<E, T> implements IExpression<T>, SelectionVisitable {
   private final PersistentEntityRoot<E> root;

   public IdExpression(PersistentEntityRoot<E> root) {
      this.root = root;
   }

   public PersistentEntityRoot<E> getRoot() {
      return this.root;
   }

   @Override
   public void accept(SelectionVisitor selectionVisitor) {
      selectionVisitor.visit(this);
   }

   @Override
   public boolean isBoolean() {
      PersistentEntity persistentEntity = this.root.getPersistentEntity();
      return persistentEntity.hasCompositeIdentity() ? false : this.root.get(persistentEntity.getIdentity().getName()).isBoolean();
   }

   @Override
   public boolean isNumeric() {
      PersistentEntity persistentEntity = this.root.getPersistentEntity();
      return persistentEntity.hasCompositeIdentity() ? false : this.root.get(persistentEntity.getIdentity().getName()).isNumeric();
   }

   public Class<? extends T> getJavaType() {
      PersistentEntity persistentEntity = this.root.getPersistentEntity();
      if (persistentEntity.hasCompositeIdentity()) {
         throw new IllegalStateException("IdClass is unknown!");
      } else {
         return this.root.get(persistentEntity.getIdentity().getName()).getJavaType();
      }
   }

   public String toString() {
      return "IdExpression{root=" + this.root + '}';
   }
}
