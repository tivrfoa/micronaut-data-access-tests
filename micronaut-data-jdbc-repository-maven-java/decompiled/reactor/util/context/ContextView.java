package reactor.util.context;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Stream;
import reactor.util.annotation.Nullable;

public interface ContextView {
   <T> T get(Object var1);

   default <T> T get(Class<T> key) {
      T v = this.get(key);
      if (key.isInstance(v)) {
         return v;
      } else {
         throw new NoSuchElementException("Context does not contain a value of type " + key.getName());
      }
   }

   @Nullable
   default <T> T getOrDefault(Object key, @Nullable T defaultValue) {
      return (T)(!this.hasKey(key) ? defaultValue : this.get(key));
   }

   default <T> Optional<T> getOrEmpty(Object key) {
      return this.hasKey(key) ? Optional.of(this.get(key)) : Optional.empty();
   }

   boolean hasKey(Object var1);

   default boolean isEmpty() {
      return this.size() == 0;
   }

   int size();

   Stream<Entry<Object, Object>> stream();
}
