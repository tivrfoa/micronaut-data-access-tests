package io.micronaut.context;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.AbstractBeanConstructor;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ConstructorInjectionPoint;
import io.micronaut.inject.annotation.AnnotationMetadataHierarchy;
import java.util.Objects;

public abstract class AbstractConstructorInjectionPoint<T> extends AbstractBeanConstructor<T> implements ConstructorInjectionPoint<T>, EnvironmentConfigurable {
   private final BeanDefinition<T> beanDefinition;

   protected AbstractConstructorInjectionPoint(BeanDefinition<T> beanDefinition) {
      super(
         ((BeanDefinition)Objects.requireNonNull(beanDefinition, "Bean definition cannot be null")).getBeanType(),
         new AnnotationMetadataHierarchy(beanDefinition.getAnnotationMetadata(), beanDefinition.getConstructor().getAnnotationMetadata()),
         beanDefinition.getConstructor().getArguments()
      );
      this.beanDefinition = (BeanDefinition)Objects.requireNonNull(beanDefinition, "Bean definition is required");
   }

   @NonNull
   @Override
   public final BeanDefinition<T> getDeclaringBean() {
      return this.beanDefinition;
   }

   @Override
   public final boolean requiresReflection() {
      return false;
   }
}
