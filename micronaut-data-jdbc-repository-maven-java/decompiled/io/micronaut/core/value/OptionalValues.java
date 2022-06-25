package io.micronaut.core.value;

import io.micronaut.core.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

public interface OptionalValues<V> extends Iterable<CharSequence> {
   OptionalValues EMPTY_VALUES = of(Object.class, Collections.emptyMap());

   Optional<V> get(CharSequence name);

   Collection<V> values();

   default boolean isEmpty() {
      return this.values().isEmpty();
   }

   default void forEach(BiConsumer<CharSequence, ? super V> action) {
      Objects.requireNonNull(action);

      for(CharSequence k : this) {
         this.get(k).ifPresent(v -> action.accept(k, v));
      }

   }

   static <T> OptionalValues<T> empty() {
      return EMPTY_VALUES;
   }

   static <T> OptionalValues<T> of(Class<T> type, @Nullable Map<CharSequence, ?> values) {
      return (OptionalValues<T>)(values == null ? empty() : new OptionalValuesMap<>(type, values));
   }
}
