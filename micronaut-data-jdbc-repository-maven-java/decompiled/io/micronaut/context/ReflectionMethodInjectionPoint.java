package io.micronaut.context;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;

@Internal
class ReflectionMethodInjectionPoint extends DefaultMethodInjectionPoint {
   ReflectionMethodInjectionPoint(
      BeanDefinition declaringBean, Class<?> declaringType, String methodName, @Nullable Argument[] arguments, @Nullable AnnotationMetadata annotationMetadata
   ) {
      super(declaringBean, declaringType, methodName, arguments, annotationMetadata);
      if (ClassUtils.REFLECTION_LOGGER.isDebugEnabled()) {
         ClassUtils.REFLECTION_LOGGER
            .debug("Bean of type [" + declaringBean.getBeanType() + "] defines method [" + methodName + "] that requires the use of reflection to inject");
      }

   }

   @Override
   public boolean requiresReflection() {
      return true;
   }
}
