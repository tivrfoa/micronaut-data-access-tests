package io.micronaut.data.runtime.support;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.context.BeanRegistration;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.data.annotation.event.PostLoad;
import io.micronaut.data.annotation.event.PostPersist;
import io.micronaut.data.annotation.event.PostRemove;
import io.micronaut.data.annotation.event.PostUpdate;
import io.micronaut.data.annotation.event.PrePersist;
import io.micronaut.data.annotation.event.PreRemove;
import io.micronaut.data.annotation.event.PreUpdate;
import io.micronaut.data.event.EntityEventListener;
import io.micronaut.data.model.runtime.AttributeConverterRegistry;
import io.micronaut.data.model.runtime.PropertyAutoPopulator;
import io.micronaut.data.model.runtime.RuntimeEntityRegistry;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import io.micronaut.data.model.runtime.convert.AttributeConverter;
import io.micronaut.data.runtime.event.EntityEventRegistry;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Internal
final class DefaultRuntimeEntityRegistry implements RuntimeEntityRegistry, ApplicationContextProvider {
   private final Map<Class, RuntimePersistentEntity> entities = new ConcurrentHashMap(10);
   private final Map<Class<? extends Annotation>, PropertyAutoPopulator<?>> propertyPopulators;
   private final EntityEventRegistry eventRegistry;
   private final ApplicationContext applicationContext;
   private final AttributeConverterRegistry attributeConverterRegistry;

   public DefaultRuntimeEntityRegistry(
      EntityEventRegistry eventRegistry,
      Collection<BeanRegistration<PropertyAutoPopulator<?>>> propertyPopulators,
      ApplicationContext applicationContext,
      AttributeConverterRegistry attributeConverterRegistry
   ) {
      this.eventRegistry = eventRegistry;
      this.propertyPopulators = new HashMap(propertyPopulators.size());
      this.attributeConverterRegistry = attributeConverterRegistry;

      for(BeanRegistration<PropertyAutoPopulator<?>> propertyPopulator : propertyPopulators) {
         PropertyAutoPopulator<?> populator = propertyPopulator.getBean();
         List<Argument<?>> typeArguments = propertyPopulator.getBeanDefinition().getTypeArguments(PropertyAutoPopulator.class);
         if (!typeArguments.isEmpty()) {
            Class<? extends Annotation> annotationType = ((Argument)typeArguments.iterator().next()).getType();
            if (this.propertyPopulators.containsKey(annotationType)) {
               throw new IllegalStateException("Multiple property populators for annotation of type are not allowed: " + annotationType);
            }

            this.propertyPopulators.put(annotationType, populator);
         }
      }

      this.applicationContext = applicationContext;
   }

   @NonNull
   @Override
   public Object autoPopulateRuntimeProperty(@NonNull RuntimePersistentProperty<?> persistentProperty, Object previousValue) {
      for(Entry<Class<? extends Annotation>, PropertyAutoPopulator<?>> entry : this.propertyPopulators.entrySet()) {
         if (persistentProperty.getAnnotationMetadata().hasAnnotation((Class<? extends Annotation>)entry.getKey())) {
            PropertyAutoPopulator<?> populator = (PropertyAutoPopulator)entry.getValue();
            return Objects.requireNonNull(
               populator.populate(persistentProperty, previousValue), () -> "PropertyAutoPopulator illegally returned null: " + populator.getClass()
            );
         }
      }

      throw new IllegalStateException(
         "Cannot auto populate property: " + persistentProperty.getName() + " for entity: " + persistentProperty.getOwner().getName()
      );
   }

   @NonNull
   @Override
   public EntityEventListener<Object> getEntityEventListener() {
      return this.eventRegistry;
   }

   @NonNull
   @Override
   public <T> RuntimePersistentEntity<T> getEntity(@NonNull Class<T> type) {
      ArgumentUtils.requireNonNull("type", (T)type);
      RuntimePersistentEntity<T> entity = (RuntimePersistentEntity)this.entities.get(type);
      if (entity == null) {
         entity = this.newEntity(type);
         this.entities.put(type, entity);
      }

      return entity;
   }

   @NonNull
   @Override
   public <T> RuntimePersistentEntity<T> newEntity(@NonNull Class<T> type) {
      return new RuntimePersistentEntity<T>(type) {
         final boolean hasPrePersistEventListeners = DefaultRuntimeEntityRegistry.this.eventRegistry.supports(this, PrePersist.class);
         final boolean hasPreRemoveEventListeners = DefaultRuntimeEntityRegistry.this.eventRegistry.supports(this, PreRemove.class);
         final boolean hasPreUpdateEventListeners = DefaultRuntimeEntityRegistry.this.eventRegistry.supports(this, PreUpdate.class);
         final boolean hasPostPersistEventListeners = DefaultRuntimeEntityRegistry.this.eventRegistry.supports(this, PostPersist.class);
         final boolean hasPostRemoveEventListeners = DefaultRuntimeEntityRegistry.this.eventRegistry.supports(this, PostRemove.class);
         final boolean hasPostUpdateEventListeners = DefaultRuntimeEntityRegistry.this.eventRegistry.supports(this, PostUpdate.class);
         final boolean hasPostLoadEventListeners = DefaultRuntimeEntityRegistry.this.eventRegistry.supports(this, PostLoad.class);

         @Override
         protected AttributeConverter<Object, Object> resolveConverter(Class<?> converterClass) {
            return DefaultRuntimeEntityRegistry.this.attributeConverterRegistry.getConverter(converterClass);
         }

         @Override
         protected RuntimePersistentEntity<T> getEntity(Class<T> type) {
            return DefaultRuntimeEntityRegistry.this.getEntity(type);
         }

         @Override
         public boolean hasPostUpdateEventListeners() {
            return this.hasPostUpdateEventListeners;
         }

         @Override
         public boolean hasPostRemoveEventListeners() {
            return this.hasPostRemoveEventListeners;
         }

         @Override
         public boolean hasPostLoadEventListeners() {
            return this.hasPostLoadEventListeners;
         }

         @Override
         public boolean hasPrePersistEventListeners() {
            return this.hasPrePersistEventListeners;
         }

         @Override
         public boolean hasPreUpdateEventListeners() {
            return this.hasPreUpdateEventListeners;
         }

         @Override
         public boolean hasPreRemoveEventListeners() {
            return this.hasPreRemoveEventListeners;
         }

         @Override
         public boolean hasPostPersistEventListeners() {
            return this.hasPostPersistEventListeners;
         }
      };
   }

   @Override
   public ApplicationContext getApplicationContext() {
      return this.applicationContext;
   }
}
