package io.micronaut.data.runtime.event.listeners;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanMethod;
import io.micronaut.data.annotation.event.PostLoad;
import io.micronaut.data.annotation.event.PostPersist;
import io.micronaut.data.annotation.event.PostRemove;
import io.micronaut.data.annotation.event.PostUpdate;
import io.micronaut.data.annotation.event.PrePersist;
import io.micronaut.data.annotation.event.PreRemove;
import io.micronaut.data.annotation.event.PreUpdate;
import io.micronaut.data.event.EntityEventContext;
import io.micronaut.data.event.EntityEventListener;
import io.micronaut.data.event.PersistenceEventException;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Collection;

@Singleton
public class AnnotatedMethodInvokingEntityEventListener implements EntityEventListener<Object> {
   @Override
   public boolean supports(RuntimePersistentEntity<Object> entity, Class<? extends Annotation> eventType) {
      BeanIntrospection<Object> introspection = entity.getIntrospection();
      Collection<BeanMethod<Object, Object>> beanMethods = introspection.getBeanMethods();
      return beanMethods.stream().anyMatch(beanMethod -> beanMethod.isAnnotationPresent(eventType));
   }

   @Override
   public boolean prePersist(@NonNull EntityEventContext<Object> context) {
      this.triggerEvent(context, PrePersist.class.getName());
      return true;
   }

   @Override
   public void postPersist(@NonNull EntityEventContext<Object> context) {
      this.triggerEvent(context, PostPersist.class.getName());
   }

   @Override
   public void postLoad(@NonNull EntityEventContext<Object> context) {
      this.triggerEvent(context, PostLoad.class.getName());
   }

   @Override
   public boolean preRemove(@NonNull EntityEventContext<Object> context) {
      this.triggerEvent(context, PreRemove.class.getName());
      return true;
   }

   @Override
   public void postRemove(@NonNull EntityEventContext<Object> context) {
      this.triggerEvent(context, PostRemove.class.getName());
   }

   @Override
   public boolean preUpdate(@NonNull EntityEventContext<Object> context) {
      this.triggerEvent(context, PreUpdate.class.getName());
      return true;
   }

   @Override
   public void postUpdate(@NonNull EntityEventContext<Object> context) {
      this.triggerEvent(context, PostUpdate.class.getName());
   }

   private void triggerEvent(@NonNull EntityEventContext<Object> context, String annotationName) {
      if (context.supportsEventSystem()) {
         RuntimePersistentEntity<Object> persistentEntity = context.getPersistentEntity();
         persistentEntity.getIntrospection()
            .getBeanMethods()
            .forEach(
               beanMethod -> {
                  if (beanMethod.getAnnotationMetadata().hasAnnotation(annotationName)) {
                     try {
                        beanMethod.invoke(context.getEntity(), new Object[0]);
                     } catch (Exception var5) {
                        throw new PersistenceEventException(
                           "Error invoking persistence event method ["
                              + beanMethod.getName()
                              + "] on entity ["
                              + persistentEntity.getName()
                              + "]: "
                              + var5.getMessage(),
                           var5
                        );
                     }
                  }
      
               }
            );
      }

   }
}
