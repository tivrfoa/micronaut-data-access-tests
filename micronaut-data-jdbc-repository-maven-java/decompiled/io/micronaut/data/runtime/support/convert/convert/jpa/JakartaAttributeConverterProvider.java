package io.micronaut.data.runtime.support.convert.convert.jpa;

import io.micronaut.context.BeanLocator;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.data.runtime.support.convert.AttributeConverterProvider;
import jakarta.inject.Singleton;
import jakarta.persistence.AttributeConverter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Internal
@Requires(
   classes = {AttributeConverter.class}
)
@Singleton
final class JakartaAttributeConverterProvider implements AttributeConverterProvider {
   private final Map<Class, io.micronaut.data.model.runtime.convert.AttributeConverter<Object, Object>> providersCache = new ConcurrentHashMap();

   @Override
   public io.micronaut.data.model.runtime.convert.AttributeConverter<Object, Object> provide(BeanLocator beanLocator, Class<?> converterType) {
      return (io.micronaut.data.model.runtime.convert.AttributeConverter<Object, Object>)this.providersCache
         .computeIfAbsent(
            converterType,
            c -> {
               AttributeConverter attributeConverter = (AttributeConverter)beanLocator.findBean(converterType)
                  .orElseThrow(
                     () -> new IllegalStateException("Cannot find a converter bean: " + converterType.getName() + " make sure it's annotated with @Converter")
                  );
               return new JakartaAttributeConverterProvider.JxAttributeConverter(attributeConverter);
            }
         );
   }

   @Override
   public boolean supports(Class<?> converterType) {
      return AttributeConverter.class.isAssignableFrom(converterType);
   }

   private static final class JxAttributeConverter implements io.micronaut.data.model.runtime.convert.AttributeConverter<Object, Object> {
      private final AttributeConverter converter;

      private JxAttributeConverter(AttributeConverter converter) {
         this.converter = converter;
      }

      @Override
      public Object convertToPersistedValue(Object entityValue, ConversionContext context) {
         return this.converter.convertToDatabaseColumn(entityValue);
      }

      @Override
      public Object convertToEntityValue(Object persistedValue, ConversionContext context) {
         return this.converter.convertToEntityAttribute(persistedValue);
      }
   }
}
