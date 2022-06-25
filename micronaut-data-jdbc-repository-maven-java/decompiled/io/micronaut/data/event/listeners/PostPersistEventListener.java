package io.micronaut.data.event.listeners;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.annotation.event.PostPersist;
import io.micronaut.data.event.EntityEventContext;
import io.micronaut.data.event.EntityEventListener;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import java.lang.annotation.Annotation;

public interface PostPersistEventListener<T> extends EntityEventListener<T> {
   void postPersist(@NonNull T entity);

   @Override
   default void postPersist(@NonNull EntityEventContext<T> context) {
      this.postPersist(context.getEntity());
   }

   @Override
   default boolean supports(RuntimePersistentEntity<T> entity, Class<? extends Annotation> eventType) {
      return eventType == PostPersist.class;
   }
}
