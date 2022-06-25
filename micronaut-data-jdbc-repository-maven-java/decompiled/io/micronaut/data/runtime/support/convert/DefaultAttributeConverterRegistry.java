package io.micronaut.data.runtime.support.convert;

import io.micronaut.context.BeanLocator;
import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.runtime.AttributeConverterRegistry;
import io.micronaut.data.model.runtime.convert.AttributeConverter;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
@Internal
final class DefaultAttributeConverterRegistry implements AttributeConverterRegistry {
   private final BeanLocator beanLocator;
   private final List<AttributeConverterProvider> attributeConverterTransformers;

   DefaultAttributeConverterRegistry(BeanLocator beanLocator, List<AttributeConverterProvider> attributeConverterTransformers) {
      this.beanLocator = beanLocator;
      this.attributeConverterTransformers = attributeConverterTransformers;
   }

   @Override
   public AttributeConverter<Object, Object> getConverter(Class<?> converterClass) {
      if (AttributeConverter.class.isAssignableFrom(converterClass)) {
         return this.beanLocator.getBean(converterClass);
      } else {
         for(AttributeConverterProvider transformer : this.attributeConverterTransformers) {
            if (transformer.supports(converterClass)) {
               return transformer.provide(this.beanLocator, converterClass);
            }
         }

         throw new IllegalStateException("Unknown converter type: " + converterClass);
      }
   }
}
