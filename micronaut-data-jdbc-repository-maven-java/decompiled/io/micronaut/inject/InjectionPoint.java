package io.micronaut.inject;

import io.micronaut.core.annotation.AnnotationMetadataProvider;
import io.micronaut.core.annotation.NonNull;

public interface InjectionPoint<T> extends AnnotationMetadataProvider {
   @NonNull
   BeanDefinition<T> getDeclaringBean();

   boolean requiresReflection();
}
