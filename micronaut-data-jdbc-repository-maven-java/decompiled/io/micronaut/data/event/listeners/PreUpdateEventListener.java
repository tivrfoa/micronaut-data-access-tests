package io.micronaut.data.event.listeners;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.event.PreUpdate;
import io.micronaut.data.event.EntityEventContext;
import io.micronaut.data.event.EntityEventListener;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import java.lang.annotation.Annotation;

public interface PreUpdateEventListener<T> extends EntityEventListener<T> {
   boolean preUpdate(@NonNull T entity);

   @Override
   default boolean preUpdate(@NonNull EntityEventContext<T> context) {
      return this.preUpdate(context.getEntity());
   }

   @Override
   default boolean supports(RuntimePersistentEntity<T> entity, Class<? extends Annotation> eventType) {
      return eventType == PreUpdate.class;
   }
}
