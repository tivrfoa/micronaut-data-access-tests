package io.micronaut.core.value;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class OptionalValuesMap<T> implements OptionalValues<T> {
   protected final ValueResolver resolver;
   protected final Map<CharSequence, ?> values;
   private final Class<?> type;

   protected OptionalValuesMap(Class<?> type, Map<CharSequence, ?> values) {
      this.type = type;
      this.values = values;
      this.resolver = new MapValueResolver(values);
   }

   @Override
   public Optional<T> get(CharSequence name) {
      return this.resolver.get(name, this.type);
   }

   @Override
   public Collection<T> values() {
      return this.values.values();
   }

   public Iterator<CharSequence> iterator() {
      return this.values.keySet().iterator();
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         OptionalValuesMap that = (OptionalValuesMap)o;
         return this.values.equals(that.values);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.values.hashCode();
   }
}
