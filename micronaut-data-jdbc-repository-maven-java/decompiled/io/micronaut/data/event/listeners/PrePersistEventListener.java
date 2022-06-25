package io.micronaut.data.event.listeners;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.event.PrePersist;
import io.micronaut.data.event.EntityEventContext;
import io.micronaut.data.event.EntityEventListener;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import java.lang.annotation.Annotation;

@FunctionalInterface
public interface PrePersistEventListener<T> extends EntityEventListener<T> {
   boolean prePersist(@NonNull T entity);

   @Override
   default boolean prePersist(@NonNull EntityEventContext<T> context) {
      return this.prePersist(context.getEntity());
   }

   @Override
   default boolean supports(RuntimePersistentEntity<T> entity, Class<? extends Annotation> eventType) {
      return eventType == PrePersist.class;
   }
}
