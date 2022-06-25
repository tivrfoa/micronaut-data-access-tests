package io.micronaut.data.runtime.event.listeners;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.data.annotation.AutoPopulated;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.event.PrePersist;
import io.micronaut.data.annotation.event.PreUpdate;
import io.micronaut.data.event.EntityEventContext;
import io.micronaut.data.model.runtime.PropertyAutoPopulator;
import io.micronaut.data.model.runtime.RuntimePersistentProperty;
import io.micronaut.data.runtime.convert.DataConversionService;
import io.micronaut.data.runtime.date.DateTimeProvider;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Singleton
public class AutoTimestampEntityEventListener extends AutoPopulatedEntityEventListener implements PropertyAutoPopulator<DateUpdated> {
   private final DateTimeProvider<?> dateTimeProvider;
   private final DataConversionService<?> conversionService;

   public AutoTimestampEntityEventListener(DateTimeProvider<?> dateTimeProvider, DataConversionService<?> conversionService) {
      this.dateTimeProvider = dateTimeProvider;
      this.conversionService = conversionService;
   }

   @NonNull
   @Override
   protected List<Class<? extends Annotation>> getEventTypes() {
      return Arrays.asList(PrePersist.class, PreUpdate.class);
   }

   @NonNull
   @Override
   protected Predicate<RuntimePersistentProperty<Object>> getPropertyPredicate() {
      return prop -> {
         AnnotationMetadata annotationMetadata = prop.getAnnotationMetadata();
         return annotationMetadata.hasAnnotation(DateCreated.class) || annotationMetadata.hasAnnotation(DateUpdated.class);
      };
   }

   @Override
   public boolean prePersist(@NonNull EntityEventContext<Object> context) {
      this.autoTimestampIfNecessary(context, false);
      return true;
   }

   @Override
   public boolean preUpdate(@NonNull EntityEventContext<Object> context) {
      this.autoTimestampIfNecessary(context, true);
      return true;
   }

   @NonNull
   @Override
   public Object populate(RuntimePersistentProperty<?> property, @Nullable Object previousValue) {
      Object now = this.dateTimeProvider.getNow();
      ChronoUnit truncateToValue = this.truncateToDateUpdated(property.getAnnotationMetadata());
      now = this.truncate(now, truncateToValue);
      return this.conversionService.convertRequired(now, property.getArgument());
   }

   private Object truncate(Object now, ChronoUnit truncateToValue) {
      if (truncateToValue != null) {
         if (now instanceof OffsetDateTime) {
            now = ((OffsetDateTime)now).truncatedTo(truncateToValue);
         } else {
            now = ((Instant)this.conversionService.convertRequired(now, Instant.class)).truncatedTo(truncateToValue);
         }
      }

      return now;
   }

   private void autoTimestampIfNecessary(@NonNull EntityEventContext<Object> context, boolean isUpdate) {
      RuntimePersistentProperty<Object>[] applicableProperties = this.getApplicableProperties(context.getPersistentEntity());
      Object now = this.dateTimeProvider.getNow();

      for(RuntimePersistentProperty<Object> property : applicableProperties) {
         if (!isUpdate || property.getAnnotationMetadata().booleanValue(AutoPopulated.class, "updateable").orElse(true)) {
            BeanProperty<Object, Object> beanProperty = property.getProperty();
            Class<?> propertyType = property.getType();
            ChronoUnit truncateToValue;
            if (isUpdate) {
               truncateToValue = this.truncateToDateUpdated(property.getAnnotationMetadata());
            } else {
               truncateToValue = this.truncateToDateCreated(property.getAnnotationMetadata());
               if (truncateToValue == null) {
                  truncateToValue = this.truncateToDateUpdated(property.getAnnotationMetadata());
               }
            }

            Object propertyNow = this.truncate(now, truncateToValue);
            if (propertyType.isInstance(propertyNow)) {
               context.setProperty(beanProperty, propertyNow);
            } else {
               this.conversionService.convert(propertyNow, propertyType).ifPresent(o -> context.setProperty(beanProperty, o));
            }
         }
      }

   }

   @Nullable
   private ChronoUnit truncateToDateCreated(@NonNull AnnotationMetadata annotationMetadata) {
      return (ChronoUnit)annotationMetadata.enumValue(DateCreated.class, "truncatedTo", ChronoUnit.class).filter(cu -> cu != ChronoUnit.FOREVER).orElse(null);
   }

   @Nullable
   private ChronoUnit truncateToDateUpdated(@NonNull AnnotationMetadata annotationMetadata) {
      return (ChronoUnit)annotationMetadata.enumValue(DateUpdated.class, "truncatedTo", ChronoUnit.class).filter(cu -> cu != ChronoUnit.FOREVER).orElse(null);
   }
}
