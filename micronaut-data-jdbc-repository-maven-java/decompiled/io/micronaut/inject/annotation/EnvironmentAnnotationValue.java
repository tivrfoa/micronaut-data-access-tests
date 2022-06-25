package io.micronaut.inject.annotation;

import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertyPlaceholderResolver;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.stream.Stream;

@Internal
class EnvironmentAnnotationValue<A extends Annotation> extends AnnotationValue<A> {
   EnvironmentAnnotationValue(Environment environment, AnnotationValue<A> target) {
      super(
         target,
         AnnotationMetadataSupport.getDefaultValues(target.getAnnotationName()),
         EnvironmentConvertibleValuesMap.of(environment, target.getValues()),
         environment != null
            ? o -> {
               PropertyPlaceholderResolver resolver = environment.getPlaceholderResolver();
               if (o instanceof String) {
                  String v = (String)o;
                  if (v.contains(resolver.getPrefix())) {
                     return resolver.resolveRequiredPlaceholders(v);
                  }
               } else if (o instanceof String[]) {
                  String[] values = (String[])o;
                  String[] resolvedValues = (String[])Arrays.copyOf(values, values.length);
                  boolean expandValues = false;
      
                  for(int i = 0; i < values.length; ++i) {
                     String value = values[i];
                     if (value.contains(resolver.getPrefix())) {
                        value = resolver.resolveRequiredPlaceholders(value);
                        if (value.contains(",")) {
                           expandValues = true;
                        }
                     }
      
                     resolvedValues[i] = value;
                  }
      
                  if (expandValues) {
                     return Stream.of(resolvedValues)
                        .flatMap(s -> s.contains(",") ? Arrays.stream(resolver.resolveRequiredPlaceholder(s, String[].class)) : Stream.of(s))
                        .toArray(x$0 -> new String[x$0]);
                  }
      
                  return resolvedValues;
               }
      
               return o;
            }
            : null
      );
   }
}
