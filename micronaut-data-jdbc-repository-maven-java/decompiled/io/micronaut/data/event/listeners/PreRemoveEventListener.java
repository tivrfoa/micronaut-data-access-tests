package io.micronaut.data.event.listeners;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.event.PreRemove;
import io.micronaut.data.event.EntityEventContext;
import io.micronaut.data.event.EntityEventListener;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import java.lang.annotation.Annotation;

@FunctionalInterface
public interface PreRemoveEventListener<T> extends EntityEventListener<T> {
   boolean preRemove(@NonNull T entity);

   @Override
   default boolean preRemove(@NonNull EntityEventContext<T> context) {
      return this.preRemove(context.getEntity());
   }

   @Override
   default boolean supports(RuntimePersistentEntity<T> entity, Class<? extends Annotation> eventType) {
      return eventType == PreRemove.class;
   }
}
