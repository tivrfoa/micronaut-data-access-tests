package io.micronaut.context.converters;

import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.TypeConverter;
import io.micronaut.core.convert.TypeConverterRegistrar;
import io.micronaut.core.reflect.ClassUtils;
import jakarta.inject.Singleton;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Internal
public class ContextConverterRegistrar implements TypeConverterRegistrar {
   private final BeanContext beanContext;
   private final Map<String, Class> classCache = new ConcurrentHashMap(10);

   ContextConverterRegistrar(BeanContext beanContext) {
      this.beanContext = beanContext;
   }

   @Override
   public void register(ConversionService<?> conversionService) {
      conversionService.addConverter(
         String[].class,
         Class[].class,
         (TypeConverter)((object, targetType, context) -> {
            Class[] classes = (Class[])Arrays.stream(object)
               .map(str -> conversionService.convert(str, Class.class))
               .filter(Optional::isPresent)
               .map(Optional::get)
               .toArray(x$0 -> new Class[x$0]);
            return Optional.of(classes);
         })
      );
      conversionService.addConverter(
         String.class,
         Class.class,
         (TypeConverter)((object, targetType, context) -> {
            Class result = (Class)this.classCache
               .computeIfAbsent(
                  object, s -> (Class)ClassUtils.forName(s, this.beanContext.getClassLoader()).orElse(ContextConverterRegistrar.MissingClass.class)
               );
            return result == ContextConverterRegistrar.MissingClass.class ? Optional.empty() : Optional.of(result);
         })
      );
   }

   private final class MissingClass {
   }
}
