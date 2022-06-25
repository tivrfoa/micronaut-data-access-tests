package io.micronaut.core.convert.value;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.reflect.GenericTypeUtils;
import io.micronaut.core.type.Argument;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

public interface ConvertibleMultiValues<V> extends ConvertibleValues<List<V>> {
   List<V> getAll(CharSequence name);

   @Nullable
   V get(CharSequence name);

   @Override
   default boolean isEmpty() {
      return this == ConvertibleMultiValuesMap.EMPTY || this.names().isEmpty();
   }

   default void forEachValue(BiConsumer<String, V> action) {
      Objects.requireNonNull(action, "Consumer cannot be null");

      for(String headerName : this.names()) {
         for(V value : this.getAll(headerName)) {
            action.accept(headerName, value);
         }
      }

   }

   @Override
   default void forEach(BiConsumer<String, List<V>> action) {
      Objects.requireNonNull(action, "Consumer cannot be null");

      for(String headerName : this.names()) {
         List<V> values = this.getAll(headerName);
         action.accept(headerName, values);
      }

   }

   @Override
   default Iterator<Entry<String, List<V>>> iterator() {
      final Iterator<String> headerNames = this.names().iterator();
      return new Iterator<Entry<String, List<V>>>() {
         public boolean hasNext() {
            return headerNames.hasNext();
         }

         public Entry<String, List<V>> next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               final String name = (String)headerNames.next();
               return new Entry<String, List<V>>() {
                  public String getKey() {
                     return name;
                  }

                  public List<V> getValue() {
                     return ConvertibleMultiValues.this.getAll(name);
                  }

                  public List<V> setValue(List<V> value) {
                     throw new UnsupportedOperationException("Not mutable");
                  }
               };
            }
         }
      };
   }

   default Optional<V> getFirst(CharSequence name) {
      Optional<Class> type = GenericTypeUtils.resolveInterfaceTypeArgument(this.getClass(), ConvertibleMultiValues.class);
      return this.getFirst(name, (Class<V>)type.orElse(Object.class));
   }

   default <T> Optional<T> getFirst(CharSequence name, Class<T> requiredType) {
      return this.getFirst(name, Argument.of(requiredType));
   }

   default <T> Optional<T> getFirst(CharSequence name, Argument<T> requiredType) {
      V v = this.get(name);
      return v != null ? ConversionService.SHARED.convert(v, ConversionContext.of(requiredType)) : Optional.empty();
   }

   default <T> Optional<T> getFirst(CharSequence name, ArgumentConversionContext<T> conversionContext) {
      V v = this.get(name);
      return v != null ? ConversionService.SHARED.convert(v, conversionContext) : Optional.empty();
   }

   default <T> T getFirst(CharSequence name, Class<T> requiredType, T defaultValue) {
      return (T)this.getFirst(name, requiredType).orElse(defaultValue);
   }

   static <T> ConvertibleMultiValues<T> of(Map<CharSequence, List<T>> values) {
      return new ConvertibleMultiValuesMap<>(values);
   }

   static <V> ConvertibleMultiValues<V> empty() {
      return ConvertibleMultiValuesMap.EMPTY;
   }
}
