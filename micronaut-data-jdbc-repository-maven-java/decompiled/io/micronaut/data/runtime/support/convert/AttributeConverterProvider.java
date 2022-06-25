package io.micronaut.data.runtime.support.convert;

import io.micronaut.context.BeanLocator;
import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.runtime.convert.AttributeConverter;

@Internal
public interface AttributeConverterProvider {
   AttributeConverter<Object, Object> provide(BeanLocator beanLocator, Class<?> converterType);

   boolean supports(Class<?> converterType);
}
