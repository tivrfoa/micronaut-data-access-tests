package io.micronaut.context;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;

@Internal
class ReflectionFieldInjectionPoint<B, T> extends DefaultFieldInjectionPoint<B, T> {
   ReflectionFieldInjectionPoint(
      BeanDefinition declaringBean,
      Class declaringType,
      Class<T> fieldType,
      String field,
      @Nullable AnnotationMetadata annotationMetadata,
      @Nullable Argument[] typeArguments
   ) {
      super(declaringBean, declaringType, fieldType, field, annotationMetadata, typeArguments);
      if (ClassUtils.REFLECTION_LOGGER.isDebugEnabled()) {
         ClassUtils.REFLECTION_LOGGER
            .debug("Bean of type [" + declaringBean.getBeanType() + "] defines field [" + field + "] that requires the use of reflection to inject");
      }

   }

   @Override
   public boolean requiresReflection() {
      return true;
   }
}
