package io.micronaut.data.runtime.event;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.data.event.EntityEventContext;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;

@Internal
public class DefaultEntityEventContext<T> implements EntityEventContext<T> {
   private final RuntimePersistentEntity<T> persistentEntity;
   private T entity;

   public DefaultEntityEventContext(RuntimePersistentEntity<T> persistentEntity, T entity) {
      this.persistentEntity = persistentEntity;
      this.entity = entity;
   }

   @NonNull
   @Override
   public T getEntity() {
      return this.entity;
   }

   @Override
   public <P> void setProperty(BeanProperty<T, P> property, P newValue) {
      if (property.hasSetterOrConstructorArgument()) {
         if (property.isReadOnly()) {
            this.entity = property.withValue(this.entity, newValue);
         } else {
            property.set(this.entity, newValue);
         }
      }

   }

   @Override
   public RuntimePersistentEntity<T> getPersistentEntity() {
      return this.persistentEntity;
   }
}
