package io.micronaut.data.runtime.event.listeners;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.data.annotation.Version;
import io.micronaut.data.annotation.event.PrePersist;
import io.micronaut.data.annotation.event.PreRemove;
import io.micronaut.data.annotation.event.PreUpdate;
import io.micronaut.data.event.EntityEventContext;
import io.micronaut.data.event.EntityEventListener;
import io.micronaut.data.exceptions.DataAccessException;
import io.micronaut.data.model.runtime.PropertyAutoPopulator;
import io.micronaut.data.model.runtime.RuntimePersistentEntity;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import io.micronaut.data.runtime.convert.DataConversionService;
import io.micronaut.data.runtime.date.DateTimeProvider;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.List;

@Singleton
public class VersionGeneratingEntityEventListener implements EntityEventListener<Object>, PropertyAutoPopulator<Version> {
   private static final List<Class<? extends Annotation>> SUPPORTED_EVENTS = Arrays.asList(PrePersist.class, PreUpdate.class, PreRemove.class);
   private final DateTimeProvider dateTimeProvider;
   private final DataConversionService<?> conversionService;

   public VersionGeneratingEntityEventListener(DateTimeProvider dateTimeProvider, DataConversionService<?> conversionService) {
      this.dateTimeProvider = dateTimeProvider;
      this.conversionService = conversionService;
   }

   private boolean shouldSkip(@NonNull EntityEventContext<Object> context) {
      return !context.supportsEventSystem();
   }

   @Override
   public boolean supports(RuntimePersistentEntity<Object> entity, Class<? extends Annotation> eventType) {
      return entity.getVersion() != null && SUPPORTED_EVENTS.contains(eventType);
   }

   @Override
   public boolean prePersist(@NonNull EntityEventContext<Object> context) {
      if (this.shouldSkip(context)) {
         return true;
      } else {
         BeanProperty<Object, Object> property = context.getPersistentEntity().getVersion().getProperty();
         Object newVersion = this.init(property.getType());
         context.setProperty(property, newVersion);
         return true;
      }
   }

   @Override
   public boolean preUpdate(@NonNull EntityEventContext<Object> context) {
      if (this.shouldSkip(context)) {
         return true;
      } else {
         Object entity = context.getEntity();
         BeanProperty<Object, Object> property = context.getPersistentEntity().getVersion().getProperty();
         Object newVersion = this.increment(property.get(entity), property.getType());
         context.setProperty(property, newVersion);
         return true;
      }
   }

   @Override
   public boolean preRemove(@NonNull EntityEventContext<Object> context) {
      return this.preUpdate(context);
   }

   @NonNull
   @Override
   public Object populate(RuntimePersistentProperty<?> property, @Nullable Object previousValue) {
      Class<?> type = property.getType();
      return this.increment(previousValue, type);
   }

   private Object increment(Object previousValue, Class<?> type) {
      if (previousValue == null) {
         throw new IllegalStateException("@Version value cannot be null");
      } else if (Temporal.class.isAssignableFrom(type)) {
         return this.newTemporal(type);
      } else if (type == Integer.class) {
         return (Integer)previousValue + 1;
      } else if (type == Long.class) {
         return (Long)previousValue + 1L;
      } else if (type == Short.class) {
         return (Short)previousValue + 1;
      } else {
         throw new DataAccessException("Unsupported @Version type: " + type);
      }
   }

   private Object init(Class<?> valueType) {
      if (Temporal.class.isAssignableFrom(valueType)) {
         return this.newTemporal(valueType);
      } else if (valueType == Integer.class) {
         return 0;
      } else if (valueType == Long.class) {
         return 0L;
      } else if (valueType == Short.class) {
         return (short)0;
      } else {
         throw new DataAccessException("Unsupported @Version type: " + valueType);
      }
   }

   private Object newTemporal(Class<?> type) {
      Object now = this.dateTimeProvider.getNow();
      return this.conversionService.convertRequired(now, type);
   }
}
