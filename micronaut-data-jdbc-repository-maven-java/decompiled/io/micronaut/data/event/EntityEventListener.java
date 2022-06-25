package io.micronaut.data.event;

import io.micronaut.core.annotation.Indexed;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.order.Ordered;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import java.lang.annotation.Annotation;
import java.util.EventListener;

@Indexed(EntityEventListener.class)
public interface EntityEventListener<T> extends EventListener, Ordered {
   EntityEventListener<Object> NOOP = new EntityEventListener() {
      @Override
      public boolean supports(RuntimePersistentEntity entity, Class eventType) {
         return false;
      }
   };

   default boolean supports(RuntimePersistentEntity<T> entity, Class<? extends Annotation> eventType) {
      return true;
   }

   default boolean prePersist(@NonNull EntityEventContext<T> context) {
      return true;
   }

   default void postPersist(@NonNull EntityEventContext<T> context) {
   }

   default void postLoad(@NonNull EntityEventContext<T> context) {
   }

   default boolean preRemove(@NonNull EntityEventContext<T> context) {
      return true;
   }

   default void postRemove(@NonNull EntityEventContext<T> context) {
   }

   default boolean preUpdate(@NonNull EntityEventContext<T> context) {
      return true;
   }

   default boolean preQuery(@NonNull QueryEventContext<T> context) {
      return true;
   }

   default void postUpdate(@NonNull EntityEventContext<T> context) {
   }
}
