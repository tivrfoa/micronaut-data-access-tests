package io.micronaut.data.event.listeners;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.event.PostUpdate;
import io.micronaut.data.event.EntityEventContext;
import io.micronaut.data.event.EntityEventListener;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import java.lang.annotation.Annotation;

public interface PostUpdateEventListener<T> extends EntityEventListener<T> {
   void postUpdate(@NonNull T entity);

   @Override
   default void postUpdate(@NonNull EntityEventContext<T> context) {
      this.postUpdate(context.getEntity());
   }

   @Override
   default boolean supports(RuntimePersistentEntity<T> entity, Class<? extends Annotation> eventType) {
      return eventType == PostUpdate.class;
   }
}
