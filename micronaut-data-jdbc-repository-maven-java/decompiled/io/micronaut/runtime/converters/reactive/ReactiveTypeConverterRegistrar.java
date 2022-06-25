package io.micronaut.runtime.converters.reactive;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.TypeConverterRegistrar;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;

@Singleton
@Requires(
   classes = {Publishers.class}
)
public class ReactiveTypeConverterRegistrar implements TypeConverterRegistrar {
   @Override
   public void register(ConversionService<?> conversionService) {
      conversionService.addConverter(Object.class, Publisher.class, obj -> obj instanceof Publisher ? (Publisher)obj : Publishers.just(obj));
   }
}
