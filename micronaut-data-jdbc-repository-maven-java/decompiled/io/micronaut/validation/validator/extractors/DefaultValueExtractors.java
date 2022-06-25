package io.micronaut.validation.validator.extractors;

import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.beans.BeanWrapper;
import io.micronaut.core.type.TypeInformation;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.CollectionUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.Map.Entry;
import javax.validation.valueextraction.UnwrapByDefault;
import javax.validation.valueextraction.ValueExtractor;

@Singleton
@Introspected
public class DefaultValueExtractors implements ValueExtractorRegistry {
   public static final String ITERABLE_ELEMENT_NODE_NAME = "<iterable element>";
   public static final String LIST_ELEMENT_NODE_NAME = "<list element>";
   public static final String MAP_VALUE_NODE_NAME = "<map value>";
   private final UnwrapByDefaultValueExtractor<Optional> optionalValueExtractor = (originalValue, receiver) -> receiver.value(null, originalValue.orElse(null));
   private final UnwrapByDefaultValueExtractor<OptionalInt> optionalIntValueExtractor = (originalValue, receiver) -> receiver.value(
         null, originalValue.isPresent() ? originalValue.getAsInt() : null
      );
   private final UnwrapByDefaultValueExtractor<OptionalLong> optionalLongValueExtractor = (originalValue, receiver) -> receiver.value(
         null, originalValue.isPresent() ? originalValue.getAsLong() : null
      );
   private final UnwrapByDefaultValueExtractor<OptionalDouble> optionalDoubleValueExtractor = (originalValue, receiver) -> receiver.value(
         null, originalValue.isPresent() ? originalValue.getAsDouble() : null
      );
   private final ValueExtractor<Iterable> iterableValueExtractor = (originalValue, receiver) -> {
      if (originalValue instanceof List) {
         int i = 0;

         for(Object o : originalValue) {
            receiver.indexedValue("<list element>", i++, o);
         }
      } else {
         for(Object var6x : originalValue) {
            receiver.iterableValue("<iterable element>", var6x);
         }
      }

   };
   private final ValueExtractor<Map<?, ?>> mapValueExtractor = (originalValue, receiver) -> {
      for(Entry<?, ?> entry : originalValue.entrySet()) {
         receiver.keyedValue("<map value>", entry.getKey(), entry.getValue());
      }

   };
   private final ValueExtractor<Object[]> objectArrayValueExtractor = (originalValue, receiver) -> {
      for(int i = 0; i < originalValue.length; ++i) {
         receiver.indexedValue("<list element>", i, originalValue[i]);
      }

   };
   private final ValueExtractor<int[]> intArrayValueExtractor = (originalValue, receiver) -> {
      for(int i = 0; i < originalValue.length; ++i) {
         receiver.indexedValue("<list element>", i, originalValue[i]);
      }

   };
   private final ValueExtractor<byte[]> byteArrayValueExtractor = (originalValue, receiver) -> {
      for(int i = 0; i < originalValue.length; ++i) {
         receiver.indexedValue("<list element>", i, originalValue[i]);
      }

   };
   private final ValueExtractor<boolean[]> booleanArrayValueExtractor = (originalValue, receiver) -> {
      for(int i = 0; i < originalValue.length; ++i) {
         receiver.indexedValue("<list element>", i, originalValue[i]);
      }

   };
   private final ValueExtractor<double[]> doubleArrayValueExtractor = (originalValue, receiver) -> {
      for(int i = 0; i < originalValue.length; ++i) {
         receiver.indexedValue("<list element>", i, originalValue[i]);
      }

   };
   private final ValueExtractor<char[]> charArrayValueExtractor = (originalValue, receiver) -> {
      for(int i = 0; i < originalValue.length; ++i) {
         receiver.indexedValue("<list element>", i, originalValue[i]);
      }

   };
   private final ValueExtractor<float[]> floatArrayValueExtractor = (originalValue, receiver) -> {
      for(int i = 0; i < originalValue.length; ++i) {
         receiver.indexedValue("<list element>", i, originalValue[i]);
      }

   };
   private final ValueExtractor<short[]> shortArrayValueExtractor = (originalValue, receiver) -> {
      for(int i = 0; i < originalValue.length; ++i) {
         receiver.indexedValue("<list element>", i, originalValue[i]);
      }

   };
   private final Map<Class, ValueExtractor> valueExtractors;
   private final Set<Class> unwrapByDefaultTypes = new HashSet(5);

   public DefaultValueExtractors() {
      this(null);
   }

   @Inject
   protected DefaultValueExtractors(@Nullable BeanContext beanContext) {
      BeanWrapper<DefaultValueExtractors> wrapper = (BeanWrapper)BeanWrapper.findWrapper(this).orElse(null);
      Map<Class, ValueExtractor> extractorMap = new HashMap();
      if (beanContext != null && beanContext.containsBean(ValueExtractor.class)) {
         Collection<BeanRegistration<ValueExtractor>> valueExtractors = beanContext.getBeanRegistrations(ValueExtractor.class);
         if (CollectionUtils.isNotEmpty(valueExtractors)) {
            for(BeanRegistration<ValueExtractor> reg : valueExtractors) {
               ValueExtractor valueExtractor = reg.getBean();
               Class[] typeParameters = reg.getBeanDefinition().getTypeParameters(ValueExtractor.class);
               if (ArrayUtils.isNotEmpty(typeParameters)) {
                  Class targetType = typeParameters[0];
                  extractorMap.put(targetType, valueExtractor);
                  if (valueExtractor instanceof UnwrapByDefaultValueExtractor || valueExtractor.getClass().isAnnotationPresent(UnwrapByDefault.class)) {
                     this.unwrapByDefaultTypes.add(targetType);
                  }
               }
            }
         }
      }

      if (wrapper != null) {
         for(BeanProperty<DefaultValueExtractors, Object> property : wrapper.getBeanProperties()) {
            if (ValueExtractor.class.isAssignableFrom(property.getType())) {
               ValueExtractor valueExtractor = (ValueExtractor)wrapper.getProperty(property.getName(), ValueExtractor.class).orElse(null);
               Class<?> targetType = (Class)property.asArgument().getFirstTypeVariable().map(TypeInformation::getType).orElse(null);
               extractorMap.put(targetType, valueExtractor);
               if (valueExtractor instanceof UnwrapByDefaultValueExtractor || valueExtractor.getClass().isAnnotationPresent(UnwrapByDefault.class)) {
                  this.unwrapByDefaultTypes.add(targetType);
               }
            }
         }

         this.valueExtractors = new HashMap(extractorMap.size());
         this.valueExtractors.putAll(extractorMap);
      } else {
         this.valueExtractors = Collections.emptyMap();
      }

   }

   public UnwrapByDefaultValueExtractor<Optional> getOptionalValueExtractor() {
      return this.optionalValueExtractor;
   }

   public UnwrapByDefaultValueExtractor<OptionalInt> getOptionalIntValueExtractor() {
      return this.optionalIntValueExtractor;
   }

   public UnwrapByDefaultValueExtractor<OptionalLong> getOptionalLongValueExtractor() {
      return this.optionalLongValueExtractor;
   }

   public UnwrapByDefaultValueExtractor<OptionalDouble> getOptionalDoubleValueExtractor() {
      return this.optionalDoubleValueExtractor;
   }

   public ValueExtractor<Iterable> getIterableValueExtractor() {
      return this.iterableValueExtractor;
   }

   public ValueExtractor<Map<?, ?>> getMapValueExtractor() {
      return this.mapValueExtractor;
   }

   public ValueExtractor<Object[]> getObjectArrayValueExtractor() {
      return this.objectArrayValueExtractor;
   }

   public ValueExtractor<int[]> getIntArrayValueExtractor() {
      return this.intArrayValueExtractor;
   }

   public ValueExtractor<byte[]> getByteArrayValueExtractor() {
      return this.byteArrayValueExtractor;
   }

   public ValueExtractor<char[]> getCharArrayValueExtractor() {
      return this.charArrayValueExtractor;
   }

   public ValueExtractor<boolean[]> getBooleanArrayValueExtractor() {
      return this.booleanArrayValueExtractor;
   }

   public ValueExtractor<double[]> getDoubleArrayValueExtractor() {
      return this.doubleArrayValueExtractor;
   }

   public ValueExtractor<float[]> getFloatArrayValueExtractor() {
      return this.floatArrayValueExtractor;
   }

   public ValueExtractor<short[]> getShortArrayValueExtractor() {
      return this.shortArrayValueExtractor;
   }

   @NonNull
   @Override
   public <T> Optional<ValueExtractor<T>> findValueExtractor(@NonNull Class<T> targetType) {
      ValueExtractor valueExtractor = (ValueExtractor)this.valueExtractors.get(targetType);
      return valueExtractor != null
         ? Optional.of(valueExtractor)
         : this.valueExtractors.entrySet().stream().filter(entry -> ((Class)entry.getKey()).isAssignableFrom(targetType)).map(Entry::getValue).findFirst();
   }

   @NonNull
   @Override
   public <T> Optional<ValueExtractor<T>> findUnwrapValueExtractor(@NonNull Class<T> targetType) {
      return this.unwrapByDefaultTypes.contains(targetType) ? Optional.ofNullable(this.valueExtractors.get(targetType)) : Optional.empty();
   }
}
