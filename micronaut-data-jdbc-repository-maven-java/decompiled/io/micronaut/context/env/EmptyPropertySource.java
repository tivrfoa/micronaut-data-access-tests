package io.micronaut.context.env;

import java.util.Iterator;

public class EmptyPropertySource implements PropertySource {
   private final String name;

   public EmptyPropertySource() {
      this("empty");
   }

   public EmptyPropertySource(String name) {
      this.name = name;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public Object get(String key) {
      return null;
   }

   public Iterator<String> iterator() {
      return new Iterator<String>() {
         public boolean hasNext() {
            return false;
         }

         public String next() {
            throw new UnsupportedOperationException("next");
         }
      };
   }
}
