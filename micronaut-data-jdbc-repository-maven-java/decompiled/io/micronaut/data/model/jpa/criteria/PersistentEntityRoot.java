package io.micronaut.data.model.jpa.criteria;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.PersistentEntity;
import io.micronaut.data.model.jpa.criteria.impl.IdExpression;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;

public interface PersistentEntityRoot<T> extends Root<T>, PersistentEntityFrom<T, T> {
   @NonNull
   default <Y> Expression<Y> id() {
      PersistentEntity persistentEntity = this.getPersistentEntity();
      if (persistentEntity.hasIdentity()) {
         return this.get(persistentEntity.getIdentity().getName());
      } else if (persistentEntity.hasCompositeIdentity()) {
         return new IdExpression<>(this);
      } else {
         throw new IllegalStateException("No identity is present");
      }
   }

   @NonNull
   default <Y> PersistentPropertyPath<Y> version() {
      PersistentEntity persistentEntity = this.getPersistentEntity();
      if (persistentEntity.getVersion() == null) {
         throw new IllegalStateException("No version is present");
      } else {
         return this.get(persistentEntity.getVersion().getName());
      }
   }
}
