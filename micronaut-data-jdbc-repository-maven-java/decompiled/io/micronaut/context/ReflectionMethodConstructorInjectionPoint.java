package io.micronaut.context;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ConstructorInjectionPoint;

@Internal
class ReflectionMethodConstructorInjectionPoint extends ReflectionMethodInjectionPoint implements ConstructorInjectionPoint {
   ReflectionMethodConstructorInjectionPoint(
      BeanDefinition declaringBean, Class<?> declaringType, String methodName, @Nullable Argument[] arguments, @Nullable AnnotationMetadata annotationMetadata
   ) {
      super(declaringBean, declaringType, methodName, arguments, annotationMetadata);
      if (ClassUtils.REFLECTION_LOGGER.isDebugEnabled()) {
         ClassUtils.REFLECTION_LOGGER
            .debug("Bean of type [" + declaringBean.getBeanType() + "] defines constructor [" + methodName + "] that requires the use of reflection to inject");
      }

   }

   @Override
   public Object invoke(Object... args) {
      throw new UnsupportedOperationException("Use MethodInjectionPoint#invoke(..) instead");
   }
}
