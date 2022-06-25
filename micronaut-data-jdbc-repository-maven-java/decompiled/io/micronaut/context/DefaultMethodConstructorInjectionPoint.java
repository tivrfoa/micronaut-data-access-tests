package io.micronaut.context;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ConstructorInjectionPoint;

@Internal
class DefaultMethodConstructorInjectionPoint<T> extends DefaultMethodInjectionPoint<T, T> implements ConstructorInjectionPoint<T> {
   DefaultMethodConstructorInjectionPoint(
      BeanDefinition<T> declaringBean,
      Class<?> declaringType,
      String methodName,
      @Nullable Argument<?>[] arguments,
      @Nullable AnnotationMetadata annotationMetadata
   ) {
      super(declaringBean, declaringType, methodName, arguments, annotationMetadata);
   }

   @Override
   public T invoke(Object... args) {
      throw new UnsupportedOperationException("Use MethodInjectionPoint#invoke(..) instead");
   }
}
