package io.micronaut.context.env;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class MapPropertySource implements PropertySource {
   private final String name;
   private final Map map;

   public MapPropertySource(String name, Map map) {
      this.name = name;
      this.map = map;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public Object get(String key) {
      return this.map.get(key);
   }

   public Iterator<String> iterator() {
      final Iterator i = this.map.keySet().iterator();
      return new Iterator<String>() {
         public boolean hasNext() {
            return i.hasNext();
         }

         public String next() {
            return i.next().toString();
         }
      };
   }

   public Map<String, Object> asMap() {
      return Collections.unmodifiableMap(this.map);
   }

   public static MapPropertySource of(String name, Map<String, Object> map) {
      return new MapPropertySource(name, map);
   }

   public String toString() {
      return this.getName();
   }
}
