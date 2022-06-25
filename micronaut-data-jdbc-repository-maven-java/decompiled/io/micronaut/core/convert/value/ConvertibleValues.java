package io.micronaut.core.convert.value;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.reflect.GenericTypeUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.value.ValueResolver;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public interface ConvertibleValues<V> extends ValueResolver<CharSequence>, Iterable<Entry<String, V>> {
   ConvertibleValues EMPTY = new ConvertibleValuesMap(Collections.emptyMap());

   Set<String> names();

   Collection<V> values();

   default boolean isEmpty() {
      return this == EMPTY || this.names().isEmpty();
   }

   default Class<V> getValueType() {
      Optional<Class> type = GenericTypeUtils.resolveInterfaceTypeArgument(this.getClass(), ConvertibleValues.class);
      return (Class<V>)type.orElse(Object.class);
   }

   default boolean contains(String name) {
      return this.get(name, Argument.OBJECT_ARGUMENT).isPresent();
   }

   @Nullable
   default V getValue(CharSequence name) {
      return (V)this.get(name, Argument.OBJECT_ARGUMENT).orElse(null);
   }

   default void forEach(BiConsumer<String, V> action) {
      Objects.requireNonNull(action, "Consumer cannot be null");

      for(String headerName : this.names()) {
         Optional<V> vOptional = this.get(headerName, this.getValueType());
         vOptional.ifPresent(v -> action.accept(headerName, v));
      }

   }

   default Map<String, V> asMap() {
      Map<String, V> newMap = new LinkedHashMap();

      for(Entry<String, V> entry : this) {
         String key = (String)entry.getKey();
         newMap.put(key, entry.getValue());
      }

      return newMap;
   }

   default <KT, VT> Map<KT, VT> asMap(Class<KT> keyType, Class<VT> valueType) {
      Map<KT, VT> newMap = new LinkedHashMap();

      for(Entry<String, V> entry : this) {
         String key = (String)entry.getKey();
         Optional<KT> convertedKey = ConversionService.SHARED.convert(key, keyType);
         if (convertedKey.isPresent()) {
            Optional<VT> convertedValue = ConversionService.SHARED.convert(entry.getValue(), valueType);
            convertedValue.ifPresent(vt -> newMap.put(convertedKey.get(), vt));
         }
      }

      return newMap;
   }

   default Properties asProperties() {
      Properties props = new Properties();

      for(Entry<String, V> entry : this) {
         String key = (String)entry.getKey();
         V value = (V)entry.getValue();
         if (value instanceof CharSequence || value instanceof Number) {
            props.setProperty(key, value.toString());
         }
      }

      return props;
   }

   default Map<String, V> subMap(String prefix, Class<V> valueType) {
      return this.subMap(prefix, Argument.of(valueType));
   }

   default Map<String, V> subMap(String prefix, Argument<V> valueType) {
      return this.subMap(prefix, ConversionContext.of(valueType));
   }

   default Map<String, V> subMap(String prefix, ArgumentConversionContext<V> valueType) {
      String finalPrefix = prefix + '.';
      return (Map<String, V>)this.names()
         .stream()
         .filter(name -> name.startsWith(finalPrefix))
         .collect(Collectors.toMap(name -> name.substring(finalPrefix.length()), name -> this.get(name, valueType).orElse(null)));
   }

   default Iterator<Entry<String, V>> iterator() {
      final Iterator<String> names = this.names().iterator();
      return new Iterator<Entry<String, V>>() {
         public boolean hasNext() {
            return names.hasNext();
         }

         public Entry<String, V> next() {
            if (!this.hasNext()) {
               throw new NoSuchElementException();
            } else {
               final String name = (String)names.next();
               return new Entry<String, V>() {
                  public String getKey() {
                     return name;
                  }

                  public V getValue() {
                     return (V)ConvertibleValues.this.get(name, ConvertibleValues.this.getValueType()).orElse(null);
                  }

                  public V setValue(V value) {
                     throw new UnsupportedOperationException("Not mutable");
                  }
               };
            }
         }
      };
   }

   static <T> ConvertibleValues<T> of(Map<? extends CharSequence, T> values) {
      return (ConvertibleValues<T>)(values == null ? ConvertibleValuesMap.empty() : new ConvertibleValuesMap<>(values));
   }

   static <V> ConvertibleValues<V> empty() {
      return EMPTY;
   }
}
