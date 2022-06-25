package io.micronaut.data.event.listeners;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.event.PostRemove;
import io.micronaut.data.event.EntityEventContext;
import io.micronaut.data.event.EntityEventListener;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import java.lang.annotation.Annotation;

public interface PostRemoveEventListener<T> extends EntityEventListener<T> {
   void postRemove(@NonNull T entity);

   @Override
   default void postRemove(@NonNull EntityEventContext<T> context) {
      this.postRemove(context.getEntity());
   }

   @Override
   default boolean supports(RuntimePersistentEntity<T> entity, Class<? extends Annotation> eventType) {
      return eventType == PostRemove.class;
   }
}
