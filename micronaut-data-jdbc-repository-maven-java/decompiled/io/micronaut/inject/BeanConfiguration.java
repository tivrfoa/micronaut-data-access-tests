package io.micronaut.inject;

import io.micronaut.core.annotation.AnnotationMetadataProvider;

public interface BeanConfiguration extends AnnotationMetadataProvider, BeanContextConditional {
   Package getPackage();

   String getName();

   String getVersion();

   boolean isWithin(BeanDefinitionReference beanDefinitionReference);

   boolean isWithin(String className);

   default boolean isWithin(Class cls) {
      return this.isWithin(cls.getName());
   }
}
