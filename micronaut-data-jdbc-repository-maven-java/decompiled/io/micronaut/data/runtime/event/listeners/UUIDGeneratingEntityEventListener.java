package io.micronaut.data.runtime.event.listeners;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.data.annotation.event.PrePersist;
import io.micronaut.data.event.EntityEventContext;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Singleton
public class UUIDGeneratingEntityEventListener extends AutoPopulatedEntityEventListener {
   private static final Predicate<RuntimePersistentProperty<Object>> UUID_PREDICATE = p -> p.getType() == UUID.class;

   @NonNull
   @Override
   protected final List<Class<? extends Annotation>> getEventTypes() {
      return Collections.singletonList(PrePersist.class);
   }

   @NonNull
   @Override
   protected Predicate<RuntimePersistentProperty<Object>> getPropertyPredicate() {
      return UUID_PREDICATE;
   }

   @Override
   public boolean prePersist(@NonNull EntityEventContext<Object> context) {
      RuntimePersistentProperty<Object>[] persistentProperties = this.getApplicableProperties(context.getPersistentEntity());

      for(RuntimePersistentProperty<Object> persistentProperty : persistentProperties) {
         BeanProperty<Object, Object> property = persistentProperty.getProperty();
         context.setProperty(property, UUID.randomUUID());
      }

      return true;
   }
}
