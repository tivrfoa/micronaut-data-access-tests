package io.micronaut.inject;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.DefaultArgument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import java.util.Collections;
import java.util.Set;

public interface BeanType<T> extends AnnotationMetadataProvider, BeanContextConditional {
   default boolean isPrimary() {
      return this.getAnnotationMetadata().hasDeclaredStereotype(Primary.class);
   }

   Class<T> getBeanType();

   default boolean isContainerType() {
      return DefaultArgument.CONTAINER_TYPES.contains(this.getBeanType());
   }

   @NonNull
   default Set<Class<?>> getExposedTypes() {
      AnnotationMetadata annotationMetadata = this.getAnnotationMetadata();
      String beanAnn = Bean.class.getName();
      if (annotationMetadata.hasDeclaredAnnotation(beanAnn)) {
         Class<?>[] exposedTypes = annotationMetadata.classValues(beanAnn, "typed");
         if (ArrayUtils.isNotEmpty(exposedTypes)) {
            return Collections.unmodifiableSet(CollectionUtils.setOf((T[])exposedTypes));
         }
      }

      return Collections.emptySet();
   }

   default boolean isCandidateBean(@Nullable Argument<?> beanType) {
      if (beanType == null) {
         return false;
      } else {
         Set<Class<?>> exposedTypes = this.getExposedTypes();
         if (CollectionUtils.isNotEmpty(exposedTypes)) {
            return exposedTypes.contains(beanType.getType());
         } else {
            Class<T> exposedType = this.getBeanType();
            return beanType.isAssignableFrom(exposedType) || beanType.getType() == exposedType || this.isContainerType();
         }
      }
   }

   default String getName() {
      return this.getBeanType().getName();
   }

   default boolean requiresMethodProcessing() {
      return false;
   }
}
