package io.micronaut.context.env;

import io.micronaut.core.order.Ordered;
import java.util.LinkedHashMap;
import java.util.Map;

public interface PropertySource extends Iterable<String>, Ordered {
   String CONTEXT = "context";

   String getName();

   Object get(String key);

   default PropertySource.PropertyConvention getConvention() {
      return PropertySource.PropertyConvention.JAVA_PROPERTIES;
   }

   static PropertySource of(String name, Map<String, Object> map) {
      return new MapPropertySource(name, map);
   }

   static PropertySource of(String name, Map<String, Object> map, PropertySource.PropertyConvention convention) {
      return new MapPropertySource(name, map) {
         @Override
         public PropertySource.PropertyConvention getConvention() {
            return convention;
         }
      };
   }

   static PropertySource of(String name, Object... values) {
      return new MapPropertySource(name, mapOf(values));
   }

   static Map<String, Object> mapOf(Object... values) {
      int len = values.length;
      if (len % 2 != 0) {
         throw new IllegalArgumentException("Number of arguments should be an even number representing the keys and values");
      } else {
         Map<String, Object> answer = new LinkedHashMap(len / 2);
         int i = 0;

         while(i < values.length - 1) {
            Object k = values[i++];
            if (k != null) {
               answer.put(k.toString(), values[i++]);
            }
         }

         return answer;
      }
   }

   static PropertySource of(String name, Map<String, Object> map, int priority) {
      return new MapPropertySource(name, map) {
         @Override
         public int getOrder() {
            return priority;
         }
      };
   }

   static PropertySource of(Map<String, Object> map) {
      return new MapPropertySource("application", map);
   }

   public static enum PropertyConvention {
      ENVIRONMENT_VARIABLE,
      JAVA_PROPERTIES;
   }
}
