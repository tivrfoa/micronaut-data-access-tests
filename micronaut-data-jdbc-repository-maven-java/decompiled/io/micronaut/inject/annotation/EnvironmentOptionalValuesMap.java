package io.micronaut.inject.annotation;

import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertyPlaceholderResolver;
import io.micronaut.core.value.OptionalValuesMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

class EnvironmentOptionalValuesMap<V> extends OptionalValuesMap<V> {
   EnvironmentOptionalValuesMap(Class<?> type, Map<CharSequence, ?> values, Environment environment) {
      super(type, resolveValues(environment, values));
   }

   private static Map<CharSequence, ?> resolveValues(Environment environment, Map<CharSequence, ?> values) {
      PropertyPlaceholderResolver placeholderResolver = environment.getPlaceholderResolver();
      return (Map<CharSequence, ?>)values.entrySet().stream().map(entry -> {
         Object value = entry.getValue();
         if (value instanceof CharSequence) {
            value = placeholderResolver.resolveRequiredPlaceholders(value.toString());
         } else if (value instanceof String[]) {
            String[] a = (String[])value;

            for(int i = 0; i < a.length; ++i) {
               a[i] = placeholderResolver.resolveRequiredPlaceholders(a[i]);
            }
         }

         final Object finalValue = value;
         return new Entry<CharSequence, Object>() {
            Object val = finalValue;

            public CharSequence getKey() {
               return (CharSequence)entry.getKey();
            }

            public Object getValue() {
               return this.val;
            }

            public Object setValue(Object value) {
               Object old = this.val;
               this.val = value;
               return old;
            }
         };
      }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
   }
}
