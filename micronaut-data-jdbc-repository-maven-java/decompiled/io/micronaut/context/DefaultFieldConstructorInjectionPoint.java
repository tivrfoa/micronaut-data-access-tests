package io.micronaut.context;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ConstructorInjectionPoint;

@Internal
final class DefaultFieldConstructorInjectionPoint<T> extends DefaultFieldInjectionPoint<T, T> implements ConstructorInjectionPoint<T> {
   DefaultFieldConstructorInjectionPoint(
      BeanDefinition<?> declaringBean, Class<?> declaringType, Class<T> fieldType, String field, @Nullable AnnotationMetadata annotationMetadata
   ) {
      super(declaringBean, declaringType, fieldType, field, annotationMetadata, Argument.ZERO_ARGUMENTS);
   }

   @Override
   public Argument<?>[] getArguments() {
      return Argument.ZERO_ARGUMENTS;
   }

   @Override
   public T invoke(Object... args) {
      throw new UnsupportedOperationException("Use BeanFactory.instantiate(..) instead");
   }
}
