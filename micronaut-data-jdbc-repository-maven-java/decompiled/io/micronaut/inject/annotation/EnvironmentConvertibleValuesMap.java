package io.micronaut.inject.annotation;

import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertyPlaceholderResolver;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.convert.value.ConvertibleValuesMap;
import io.micronaut.core.type.Argument;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Internal
class EnvironmentConvertibleValuesMap<V> extends ConvertibleValuesMap<V> {
   private final Environment environment;

   EnvironmentConvertibleValuesMap(Map<? extends CharSequence, V> map, Environment environment) {
      super(map, environment);
      this.environment = environment;
   }

   @Override
   public <T> Optional<T> get(CharSequence name, Class<T> requiredType) {
      return this.get(name, ConversionContext.of(requiredType));
   }

   @Override
   public <T> Optional<T> get(CharSequence name, Argument<T> requiredType) {
      return this.get(name, ConversionContext.of(requiredType));
   }

   @Override
   public <T> T get(CharSequence name, Class<T> requiredType, T defaultValue) {
      return (T)this.get(name, ConversionContext.of(requiredType)).orElse(defaultValue);
   }

   @Override
   public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
      V value = (V)this.map.get(name);
      if (value instanceof AnnotationClassValue) {
         AnnotationClassValue acv = (AnnotationClassValue)value;
         return this.environment.convert(acv, conversionContext);
      } else if (value instanceof CharSequence) {
         PropertyPlaceholderResolver placeholderResolver = this.environment.getPlaceholderResolver();
         String str = this.doResolveIfNecessary((CharSequence)value, placeholderResolver);
         return this.environment.convert(str, conversionContext);
      } else if (value instanceof String[]) {
         PropertyPlaceholderResolver placeholderResolver = this.environment.getPlaceholderResolver();
         String[] resolved = (String[])Arrays.stream((String[])value).flatMap(val -> {
            try {
               String[] values = placeholderResolver.resolveRequiredPlaceholder(val, String[].class);
               return Arrays.stream(values);
            } catch (ConfigurationException var4x) {
               return Stream.of(this.doResolveIfNecessary(val, placeholderResolver));
            }
         }).toArray(x$0 -> new String[x$0]);
         return this.environment.convert(resolved, conversionContext);
      } else if (!(value instanceof AnnotationValue[])) {
         if (value instanceof AnnotationValue) {
            AnnotationValue av = (AnnotationValue)value;
            AnnotationValue var9 = new EnvironmentAnnotationValue(this.environment, av);
            return this.environment.convert(var9, conversionContext);
         } else {
            return super.get(name, conversionContext);
         }
      } else {
         AnnotationValue[] annotationValues = (AnnotationValue[])value;
         AnnotationValue[] b = new AnnotationValue[annotationValues.length];

         for(int i = 0; i < annotationValues.length; ++i) {
            AnnotationValue annotationValue = annotationValues[i];
            b[i] = new EnvironmentAnnotationValue(this.environment, annotationValue);
         }

         return this.environment.convert(b, conversionContext);
      }
   }

   @Override
   public Collection<V> values() {
      return (Collection<V>)super.values().stream().map(v -> {
         if (v instanceof CharSequence) {
            v = this.environment.getPlaceholderResolver().resolveRequiredPlaceholders(v.toString());
         }

         return v;
      }).collect(Collectors.toList());
   }

   private String doResolveIfNecessary(CharSequence value, PropertyPlaceholderResolver placeholderResolver) {
      String str = value.toString();
      if (str.contains(placeholderResolver.getPrefix())) {
         str = placeholderResolver.resolveRequiredPlaceholders(str);
      }

      return str;
   }

   static <T> ConvertibleValues<T> of(Environment environment, Map<? extends CharSequence, T> values) {
      return (ConvertibleValues<T>)(values == null ? ConvertibleValuesMap.empty() : new EnvironmentConvertibleValuesMap<>(values, environment));
   }
}
