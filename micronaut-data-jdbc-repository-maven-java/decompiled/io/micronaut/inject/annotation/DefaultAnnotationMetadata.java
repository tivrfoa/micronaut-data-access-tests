package io.micronaut.inject.annotation;

import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.AnnotationClassValue;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.TypeConverter;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.value.OptionalValues;
import io.micronaut.inject.ast.ClassElement;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Internal
public class DefaultAnnotationMetadata extends AbstractAnnotationMetadata implements AnnotationMetadata, Cloneable, EnvironmentAnnotationMetadata {
   @Nullable
   Map<String, Map<CharSequence, Object>> declaredAnnotations;
   @Nullable
   Map<String, Map<CharSequence, Object>> allAnnotations;
   @Nullable
   Map<String, Map<CharSequence, Object>> declaredStereotypes;
   @Nullable
   Map<String, Map<CharSequence, Object>> allStereotypes;
   @Nullable
   Map<String, List<String>> annotationsByStereotype;
   @Nullable
   Map<String, Map<CharSequence, Object>> annotationDefaultValues;
   Map<String, String> repeated = null;
   private Map<String, List> annotationValuesByType = new ConcurrentHashMap(2);
   private Set<String> sourceRetentionAnnotations;
   private final boolean hasPropertyExpressions;
   private final boolean useRepeatableDefaults;

   @Internal
   protected DefaultAnnotationMetadata() {
      this.hasPropertyExpressions = false;
      this.useRepeatableDefaults = false;
   }

   @Internal
   public DefaultAnnotationMetadata(
      @Nullable Map<String, Map<CharSequence, Object>> declaredAnnotations,
      @Nullable Map<String, Map<CharSequence, Object>> declaredStereotypes,
      @Nullable Map<String, Map<CharSequence, Object>> allStereotypes,
      @Nullable Map<String, Map<CharSequence, Object>> allAnnotations,
      @Nullable Map<String, List<String>> annotationsByStereotype
   ) {
      this(declaredAnnotations, declaredStereotypes, allStereotypes, allAnnotations, annotationsByStereotype, true);
   }

   @Internal
   public DefaultAnnotationMetadata(
      @Nullable Map<String, Map<CharSequence, Object>> declaredAnnotations,
      @Nullable Map<String, Map<CharSequence, Object>> declaredStereotypes,
      @Nullable Map<String, Map<CharSequence, Object>> allStereotypes,
      @Nullable Map<String, Map<CharSequence, Object>> allAnnotations,
      @Nullable Map<String, List<String>> annotationsByStereotype,
      boolean hasPropertyExpressions
   ) {
      this(declaredAnnotations, declaredStereotypes, allStereotypes, allAnnotations, annotationsByStereotype, hasPropertyExpressions, false);
   }

   @Internal
   public DefaultAnnotationMetadata(
      @Nullable Map<String, Map<CharSequence, Object>> declaredAnnotations,
      @Nullable Map<String, Map<CharSequence, Object>> declaredStereotypes,
      @Nullable Map<String, Map<CharSequence, Object>> allStereotypes,
      @Nullable Map<String, Map<CharSequence, Object>> allAnnotations,
      @Nullable Map<String, List<String>> annotationsByStereotype,
      boolean hasPropertyExpressions,
      boolean useRepeatableDefaults
   ) {
      super(declaredAnnotations, allAnnotations);
      this.declaredAnnotations = declaredAnnotations;
      this.declaredStereotypes = declaredStereotypes;
      this.allStereotypes = allStereotypes;
      this.allAnnotations = allAnnotations;
      this.annotationsByStereotype = annotationsByStereotype;
      this.hasPropertyExpressions = hasPropertyExpressions;
      this.useRepeatableDefaults = useRepeatableDefaults;
   }

   @NonNull
   @Override
   public AnnotationMetadata getDeclaredMetadata() {
      return new DefaultAnnotationMetadata(
         this.declaredAnnotations, this.declaredStereotypes, null, null, this.annotationsByStereotype, this.hasPropertyExpressions
      );
   }

   @Override
   public boolean hasPropertyExpressions() {
      return this.hasPropertyExpressions;
   }

   @Internal
   Set<String> getSourceRetentionAnnotations() {
      return this.sourceRetentionAnnotations != null ? Collections.unmodifiableSet(this.sourceRetentionAnnotations) : Collections.emptySet();
   }

   @NonNull
   @Override
   public Map<String, Object> getDefaultValues(@NonNull String annotation) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      return AnnotationMetadataSupport.getDefaultValues(annotation);
   }

   @Override
   public boolean isPresent(@NonNull String annotation, @NonNull String member) {
      boolean isPresent = false;
      if (this.allAnnotations != null && StringUtils.isNotEmpty(annotation)) {
         Map<CharSequence, Object> values = (Map)this.allAnnotations.get(annotation);
         if (values != null) {
            isPresent = values.containsKey(member);
         } else if (this.allStereotypes != null) {
            values = (Map)this.allStereotypes.get(annotation);
            if (values != null) {
               isPresent = values.containsKey(member);
            }
         }
      }

      return isPresent;
   }

   @Override
   public <E extends Enum> Optional<E> enumValue(@NonNull String annotation, Class<E> enumType) {
      return this.enumValue(annotation, "value", enumType, null);
   }

   @Override
   public <E extends Enum> Optional<E> enumValue(@NonNull String annotation, @NonNull String member, Class<E> enumType) {
      return this.enumValue(annotation, member, enumType, null);
   }

   @Override
   public <E extends Enum> Optional<E> enumValue(@NonNull Class<? extends Annotation> annotation, Class<E> enumType) {
      return this.enumValue(annotation, "value", enumType);
   }

   @Override
   public <E extends Enum> Optional<E> enumValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, Class<E> enumType) {
      return this.enumValue(annotation, member, enumType, null);
   }

   @Internal
   @Override
   public <E extends Enum> Optional<E> enumValue(
      @NonNull Class<? extends Annotation> annotation, @NonNull String member, Class<E> enumType, @Nullable Function<Object, Object> valueMapper
   ) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      Repeatable repeatable = (Repeatable)annotation.getAnnotation(Repeatable.class);
      if (repeatable != null) {
         Object v = this.getRawSingleValue(repeatable.value().getName(), "value", valueMapper);
         return v instanceof AnnotationValue ? ((AnnotationValue)v).enumValue(member, enumType, valueMapper) : Optional.empty();
      } else {
         return this.enumValue(annotation.getName(), member, enumType, valueMapper);
      }
   }

   @Override
   public <E extends Enum> E[] enumValues(@NonNull String annotation, Class<E> enumType) {
      return (E[])this.enumValues(annotation, "value", enumType, null);
   }

   @Override
   public <E extends Enum> E[] enumValues(@NonNull String annotation, @NonNull String member, Class<E> enumType) {
      return (E[])this.enumValues(annotation, member, enumType, null);
   }

   @Override
   public <E extends Enum> E[] enumValues(@NonNull Class<? extends Annotation> annotation, Class<E> enumType) {
      return (E[])this.enumValues(annotation, "value", enumType, null);
   }

   @Override
   public <E extends Enum> E[] enumValues(@NonNull Class<? extends Annotation> annotation, @NonNull String member, Class<E> enumType) {
      return (E[])this.enumValues(annotation, member, enumType, null);
   }

   @Override
   public <E extends Enum> E[] enumValues(
      @NonNull Class<? extends Annotation> annotation, @NonNull String member, Class<E> enumType, @Nullable Function<Object, Object> valueMapper
   ) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("enumType", enumType);
      Repeatable repeatable = (Repeatable)annotation.getAnnotation(Repeatable.class);
      if (repeatable != null) {
         Object v = this.getRawValue(repeatable.value().getName(), member);
         return (E[])(v instanceof AnnotationValue ? ((AnnotationValue)v).enumValues(member, enumType) : (Enum[])Array.newInstance(enumType, 0));
      } else {
         Object v = this.getRawValue(annotation.getName(), member);
         return (E[])AnnotationValue.resolveEnumValues(enumType, v);
      }
   }

   @Override
   public <E extends Enum> E[] enumValues(@NonNull String annotation, @NonNull String member, Class<E> enumType, @Nullable Function<Object, Object> valueMapper) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("enumType", enumType);
      Object v = this.getRawValue(annotation, member);
      return (E[])AnnotationValue.resolveEnumValues(enumType, v);
   }

   @Internal
   @Override
   public <E extends Enum> Optional<E> enumValue(
      @NonNull String annotation, @NonNull String member, Class<E> enumType, @Nullable Function<Object, Object> valueMapper
   ) {
      Object rawValue = this.getRawSingleValue(annotation, member, valueMapper);
      return this.enumValueOf(enumType, rawValue);
   }

   private <E extends Enum> Optional<E> enumValueOf(Class<E> enumType, Object rawValue) {
      if (rawValue != null) {
         if (enumType.isInstance(rawValue)) {
            return Optional.of((Enum)rawValue);
         } else {
            try {
               return Optional.of(Enum.valueOf(enumType, rawValue.toString()));
            } catch (Exception var4) {
               return Optional.empty();
            }
         }
      } else {
         return Optional.empty();
      }
   }

   @Override
   public <T> Class<T>[] classValues(@NonNull String annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      ArgumentUtils.requireNonNull("member", (T)member);
      Object rawSingleValue = this.getRawValue(annotation, member);
      Class<T>[] classes = (Class[])AnnotationValue.resolveClassValues(rawSingleValue);
      return classes != null ? classes : ReflectionUtils.EMPTY_CLASS_ARRAY;
   }

   @Override
   public <T> Class<T>[] classValues(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      ArgumentUtils.requireNonNull("member", (T)member);
      Repeatable repeatable = (Repeatable)annotation.getAnnotation(Repeatable.class);
      if (repeatable != null) {
         Object v = this.getRawSingleValue(repeatable.value().getName(), member, null);
         if (v instanceof AnnotationValue) {
            Class<?>[] classes = ((AnnotationValue)v).classValues(member);
            return (Class<T>[])classes;
         } else {
            return ReflectionUtils.EMPTY_CLASS_ARRAY;
         }
      } else {
         return this.classValues(annotation.getName(), member);
      }
   }

   @NonNull
   @Override
   public Optional<Class> classValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.classValue(annotation, member, null);
   }

   @Override
   public Optional<Class> classValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, Function<Object, Object> valueMapper) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      Repeatable repeatable = (Repeatable)annotation.getAnnotation(Repeatable.class);
      if (repeatable != null) {
         Object v = this.getRawSingleValue(repeatable.value().getName(), member, valueMapper);
         return v instanceof AnnotationValue ? ((AnnotationValue)v).classValue(member, valueMapper) : Optional.empty();
      } else {
         return this.classValue(annotation.getName(), member, valueMapper);
      }
   }

   @NonNull
   @Override
   public Optional<Class> classValue(@NonNull String annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return this.classValue(annotation, member, null);
   }

   @Internal
   @Override
   public Optional<Class> classValue(@NonNull String annotation, @NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      Object rawValue = this.getRawSingleValue(annotation, member, valueMapper);
      if (rawValue instanceof AnnotationClassValue) {
         return ((AnnotationClassValue)rawValue).getType();
      } else if (rawValue instanceof Class) {
         return Optional.of((Class)rawValue);
      } else {
         return rawValue != null ? ConversionService.SHARED.convert(rawValue, Class.class) : Optional.empty();
      }
   }

   @NonNull
   @Override
   public OptionalInt intValue(@NonNull String annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return this.intValue(annotation, member, null);
   }

   @NonNull
   @Override
   public OptionalInt intValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.intValue(annotation, member, null);
   }

   @Internal
   @Override
   public OptionalInt intValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      Repeatable repeatable = (Repeatable)annotation.getAnnotation(Repeatable.class);
      if (repeatable != null) {
         Object v = this.getRawSingleValue(repeatable.value().getName(), "value", valueMapper);
         return v instanceof AnnotationValue ? ((AnnotationValue)v).intValue(member, valueMapper) : OptionalInt.empty();
      } else {
         return this.intValue(annotation.getName(), member, valueMapper);
      }
   }

   @Override
   public Optional<Boolean> booleanValue(@NonNull String annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return this.booleanValue(annotation, member, null);
   }

   @Override
   public Optional<Boolean> booleanValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.booleanValue(annotation, member, null);
   }

   @Override
   public Optional<Boolean> booleanValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, Function<Object, Object> valueMapper) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      Repeatable repeatable = (Repeatable)annotation.getAnnotation(Repeatable.class);
      if (repeatable != null) {
         Object v = this.getRawSingleValue(repeatable.value().getName(), "value", null);
         return v instanceof AnnotationValue ? ((AnnotationValue)v).booleanValue(member, valueMapper) : Optional.empty();
      } else {
         return this.booleanValue(annotation.getName(), member, valueMapper);
      }
   }

   @NonNull
   @Override
   public Optional<Boolean> booleanValue(@NonNull String annotation, @NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      Object rawValue = this.getRawSingleValue(annotation, member, valueMapper);
      if (rawValue instanceof Boolean) {
         return Optional.of((Boolean)rawValue);
      } else {
         return rawValue != null ? Optional.of(StringUtils.isTrue(rawValue.toString())) : Optional.empty();
      }
   }

   @NonNull
   @Override
   public OptionalLong longValue(@NonNull String annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return this.longValue(annotation, member, null);
   }

   @NonNull
   @Override
   public OptionalLong longValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.longValue(annotation, member, null);
   }

   @Internal
   @Override
   public OptionalLong longValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      Repeatable repeatable = (Repeatable)annotation.getAnnotation(Repeatable.class);
      if (repeatable != null) {
         Object v = this.getRawSingleValue(repeatable.value().getName(), "value", valueMapper);
         return v instanceof AnnotationValue ? ((AnnotationValue)v).longValue(member, valueMapper) : OptionalLong.empty();
      } else {
         return this.longValue(annotation.getName(), member, valueMapper);
      }
   }

   @NonNull
   @Override
   public OptionalLong longValue(@NonNull String annotation, @NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      Object rawValue = this.getRawSingleValue(annotation, member, valueMapper);
      if (rawValue instanceof Number) {
         return OptionalLong.of(((Number)rawValue).longValue());
      } else {
         if (rawValue instanceof CharSequence) {
            String str = rawValue.toString();
            if (StringUtils.isNotEmpty(str)) {
               try {
                  long i = Long.parseLong(str);
                  return OptionalLong.of(i);
               } catch (NumberFormatException var8) {
                  throw new ConfigurationException(
                     "Invalid value [" + str + "] of [" + member + "] of annotation [" + annotation + "]: " + var8.getMessage(), var8
                  );
               }
            }
         }

         return OptionalLong.empty();
      }
   }

   @NonNull
   @Override
   public OptionalInt intValue(@NonNull String annotation, @NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      Object rawValue = this.getRawSingleValue(annotation, member, valueMapper);
      if (rawValue instanceof Number) {
         return OptionalInt.of(((Number)rawValue).intValue());
      } else {
         if (rawValue instanceof CharSequence) {
            String str = rawValue.toString();
            if (StringUtils.isNotEmpty(str)) {
               try {
                  int i = Integer.parseInt(str);
                  return OptionalInt.of(i);
               } catch (NumberFormatException var7) {
                  throw new ConfigurationException(
                     "Invalid value [" + str + "] of [" + member + "] of annotation [" + annotation + "]: " + var7.getMessage(), var7
                  );
               }
            }
         }

         return OptionalInt.empty();
      }
   }

   @NonNull
   @Override
   public Optional<String> stringValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.stringValue(annotation, member, null);
   }

   @Override
   public Optional<String> stringValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, Function<Object, Object> valueMapper) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      Repeatable repeatable = (Repeatable)annotation.getAnnotation(Repeatable.class);
      if (repeatable != null) {
         Object v = this.getRawSingleValue(repeatable.value().getName(), "value", valueMapper);
         return v instanceof AnnotationValue ? ((AnnotationValue)v).stringValue(member, valueMapper) : Optional.empty();
      } else {
         return this.stringValue(annotation.getName(), member, valueMapper);
      }
   }

   @NonNull
   @Override
   public String[] stringValues(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.stringValues(annotation.getName(), member, null);
   }

   @NonNull
   @Override
   public String[] stringValues(@NonNull String annotation, @NonNull String member) {
      return this.stringValues(annotation, member, null);
   }

   @NonNull
   @Override
   public String[] stringValues(@NonNull Class<? extends Annotation> annotation, @NonNull String member, Function<Object, Object> valueMapper) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      Repeatable repeatable = (Repeatable)annotation.getAnnotation(Repeatable.class);
      if (repeatable != null) {
         Object v = this.getRawValue(repeatable.value().getName(), member);
         return v instanceof AnnotationValue ? ((AnnotationValue)v).stringValues(member, valueMapper) : StringUtils.EMPTY_STRING_ARRAY;
      } else {
         Object v = this.getRawValue(annotation.getName(), member);
         String[] strings = AnnotationValue.resolveStringValues(v, valueMapper);
         return strings != null ? strings : StringUtils.EMPTY_STRING_ARRAY;
      }
   }

   @NonNull
   @Override
   public String[] stringValues(@NonNull String annotation, @NonNull String member, Function<Object, Object> valueMapper) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      Object v = this.getRawValue(annotation, member);
      String[] strings = AnnotationValue.resolveStringValues(v, valueMapper);
      return strings != null ? strings : StringUtils.EMPTY_STRING_ARRAY;
   }

   @NonNull
   @Override
   public Optional<String> stringValue(@NonNull String annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return this.stringValue(annotation, member, null);
   }

   @NonNull
   @Override
   public Optional<String> stringValue(@NonNull String annotation, @NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      Object rawValue = this.getRawSingleValue(annotation, member, valueMapper);
      if (rawValue instanceof CharSequence) {
         return Optional.of(rawValue.toString());
      } else if (rawValue instanceof Class) {
         String name = ((Class)rawValue).getName();
         return Optional.of(name);
      } else {
         return rawValue != null ? Optional.of(rawValue.toString()) : Optional.empty();
      }
   }

   @Override
   public boolean isTrue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.isTrue(annotation, member, null);
   }

   @Override
   public boolean isTrue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, Function<Object, Object> valueMapper) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      Repeatable repeatable = (Repeatable)annotation.getAnnotation(Repeatable.class);
      if (repeatable != null) {
         Object v = this.getRawSingleValue(repeatable.value().getName(), "value", valueMapper);
         return v instanceof AnnotationValue ? ((AnnotationValue)v).isTrue(member, valueMapper) : false;
      } else {
         return this.isTrue(annotation.getName(), member, valueMapper);
      }
   }

   @Override
   public boolean isTrue(@NonNull String annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return this.isTrue(annotation, member, null);
   }

   @Override
   public boolean isTrue(@NonNull String annotation, @NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      Object rawValue = this.getRawSingleValue(annotation, member, valueMapper);
      if (rawValue instanceof Boolean) {
         return (Boolean)rawValue;
      } else if (rawValue != null) {
         String booleanString = rawValue.toString().toLowerCase(Locale.ENGLISH);
         return StringUtils.isTrue(booleanString);
      } else {
         return false;
      }
   }

   @Override
   public boolean isFalse(@NonNull String annotation, @NonNull String member) {
      return !this.isTrue(annotation, member);
   }

   @NonNull
   @Override
   public OptionalDouble doubleValue(@NonNull String annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return this.doubleValue(annotation, member, null);
   }

   @NonNull
   @Override
   public OptionalDouble doubleValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.doubleValue(annotation, member, null);
   }

   @Internal
   @Override
   public OptionalDouble doubleValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      Repeatable repeatable = (Repeatable)annotation.getAnnotation(Repeatable.class);
      if (repeatable != null) {
         Object v = this.getRawSingleValue(repeatable.value().getName(), "value", valueMapper);
         return v instanceof AnnotationValue ? ((AnnotationValue)v).doubleValue(member, valueMapper) : OptionalDouble.empty();
      } else {
         return this.doubleValue(annotation.getName(), member);
      }
   }

   @NonNull
   @Internal
   @Override
   public OptionalDouble doubleValue(@NonNull String annotation, @NonNull String member, Function<Object, Object> valueMapper) {
      Object rawValue = this.getRawSingleValue(annotation, member, valueMapper);
      if (rawValue instanceof Number) {
         return OptionalDouble.of(((Number)rawValue).doubleValue());
      } else {
         if (rawValue instanceof CharSequence) {
            String str = rawValue.toString();
            if (StringUtils.isNotEmpty(str)) {
               try {
                  double i = Double.parseDouble(str);
                  return OptionalDouble.of(i);
               } catch (NumberFormatException var8) {
                  throw new ConfigurationException(
                     "Invalid value [" + str + "] of member [" + member + "] of annotation [" + annotation + "]: " + var8.getMessage(), var8
                  );
               }
            }
         }

         return OptionalDouble.empty();
      }
   }

   @NonNull
   @Override
   public <T> Optional<T> getValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, @NonNull Class<T> requiredType) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      ArgumentUtils.requireNonNull("member", (T)member);
      ArgumentUtils.requireNonNull("requiredType", (T)requiredType);
      Repeatable repeatable = (Repeatable)annotation.getAnnotation(Repeatable.class);
      boolean isRepeatable = repeatable != null;
      if (isRepeatable) {
         List<? extends AnnotationValue<? extends Annotation>> values = this.getAnnotationValuesByType(annotation);
         return !values.isEmpty() ? ((AnnotationValue)values.iterator().next()).get(member, requiredType) : Optional.empty();
      } else {
         return this.getValue(annotation.getName(), member, requiredType);
      }
   }

   @NonNull
   @Override
   public <T> Optional<T> getValue(@NonNull String annotation, @NonNull String member, @NonNull Argument<T> requiredType) {
      return this.getValue(annotation, member, requiredType, null);
   }

   @NonNull
   @Override
   public <T> Optional<T> getValue(
      @NonNull String annotation, @NonNull String member, @NonNull Argument<T> requiredType, @Nullable Function<Object, Object> valueMapper
   ) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      ArgumentUtils.requireNonNull("member", (T)member);
      ArgumentUtils.requireNonNull("requiredType", requiredType);
      Optional<T> resolved = Optional.empty();
      if (this.allAnnotations != null && StringUtils.isNotEmpty(annotation)) {
         Map<CharSequence, Object> values = (Map)this.allAnnotations.get(annotation);
         if (values != null) {
            Object rawValue = values.get(member);
            if (rawValue != null) {
               if (valueMapper != null) {
                  rawValue = valueMapper.apply(rawValue);
               }

               resolved = ConversionService.SHARED.convert(rawValue, requiredType);
            }
         } else if (this.allStereotypes != null) {
            values = (Map)this.allStereotypes.get(annotation);
            if (values != null) {
               Object rawValue = values.get(member);
               if (rawValue != null) {
                  if (valueMapper != null) {
                     rawValue = valueMapper.apply(rawValue);
                  }

                  resolved = ConversionService.SHARED.convert(rawValue, requiredType);
               }
            }
         }
      }

      return !resolved.isPresent() && this.hasStereotype(annotation) ? this.getDefaultValue(annotation, member, requiredType) : resolved;
   }

   @NonNull
   @Override
   public <T> Optional<T> getDefaultValue(@NonNull String annotation, @NonNull String member, @NonNull Class<T> requiredType) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      ArgumentUtils.requireNonNull("member", (T)member);
      ArgumentUtils.requireNonNull("requiredType", (T)requiredType);
      Map<String, Object> defaultValues = this.getDefaultValues(annotation);
      if (defaultValues.containsKey(member)) {
         Object v = defaultValues.get(member);
         return requiredType.isInstance(v) ? Optional.of(v) : ConversionService.SHARED.convert(v, requiredType);
      } else {
         return Optional.empty();
      }
   }

   @NonNull
   @Override
   public <T extends Annotation> List<AnnotationValue<T>> getAnnotationValuesByType(@Nullable Class<T> annotationType) {
      if (annotationType != null) {
         String annotationTypeName = annotationType.getName();
         List<AnnotationValue<T>> results = (List)this.annotationValuesByType.get(annotationTypeName);
         if (results == null) {
            results = this.resolveAnnotationValuesByType(annotationType, this.allAnnotations, this.allStereotypes);
            if (results != null) {
               return results;
            }

            if (this.allAnnotations != null) {
               Map<CharSequence, Object> values = (Map)this.allAnnotations.get(annotationTypeName);
               if (values != null) {
                  results = Collections.singletonList(new AnnotationValue(annotationTypeName, values));
               }
            }

            if (results == null) {
               results = Collections.emptyList();
            }

            this.annotationValuesByType.put(annotationTypeName, results);
         }

         return results;
      } else {
         return Collections.emptyList();
      }
   }

   @Override
   public <T extends Annotation> List<AnnotationValue<T>> getAnnotationValuesByName(String annotationType) {
      if (annotationType != null) {
         String repeatableTypeName = this.getRepeatedName(annotationType);
         if (repeatableTypeName == null) {
            repeatableTypeName = AnnotationMetadataSupport.getRepeatableAnnotation(annotationType);
         }

         if (repeatableTypeName != null) {
            List<AnnotationValue<T>> results = this.resolveRepeatableAnnotations(repeatableTypeName, this.allAnnotations, this.allStereotypes);
            if (results != null) {
               return results;
            }

            if (this.allAnnotations != null) {
               Map<CharSequence, Object> values = (Map)this.allAnnotations.get(annotationType);
               if (values != null) {
                  results = Collections.singletonList(new AnnotationValue(annotationType, values));
               }
            }

            if (results == null) {
               results = Collections.emptyList();
            }

            this.annotationValuesByType.put(annotationType, results);
         }
      }

      return Collections.emptyList();
   }

   @NonNull
   @Override
   public <T extends Annotation> List<AnnotationValue<T>> getDeclaredAnnotationValuesByType(@NonNull Class<T> annotationType) {
      if (annotationType != null) {
         Map<String, Map<CharSequence, Object>> sourceAnnotations = this.declaredAnnotations;
         Map<String, Map<CharSequence, Object>> sourceStereotypes = this.declaredStereotypes;
         List<AnnotationValue<T>> results = this.resolveAnnotationValuesByType(annotationType, sourceAnnotations, sourceStereotypes);
         if (results != null) {
            return results;
         }
      }

      return Collections.emptyList();
   }

   @Override
   public <T extends Annotation> List<AnnotationValue<T>> getDeclaredAnnotationValuesByName(String annotationType) {
      if (annotationType != null) {
         Map<String, Map<CharSequence, Object>> sourceAnnotations = this.declaredAnnotations;
         Map<String, Map<CharSequence, Object>> sourceStereotypes = this.declaredStereotypes;
         String repeatableTypeName = this.getRepeatedName(annotationType);
         if (repeatableTypeName == null) {
            repeatableTypeName = AnnotationMetadataSupport.getRepeatableAnnotation(annotationType);
         }

         List<AnnotationValue<T>> results = this.resolveRepeatableAnnotations(repeatableTypeName, sourceAnnotations, sourceStereotypes);
         if (results != null) {
            return results;
         }
      }

      return Collections.emptyList();
   }

   @Override
   public <T extends Annotation> T[] synthesizeAnnotationsByType(@NonNull Class<T> annotationClass) {
      if (annotationClass != null) {
         List<AnnotationValue<T>> values = this.getAnnotationValuesByType(annotationClass);
         return (T[])values.stream()
            .map(entries -> AnnotationMetadataSupport.buildAnnotation(annotationClass, entries))
            .toArray(value -> (Annotation[])Array.newInstance(annotationClass, value));
      } else {
         return (T[])AnnotationUtil.ZERO_ANNOTATIONS;
      }
   }

   @Override
   public <T extends Annotation> T[] synthesizeDeclaredAnnotationsByType(@NonNull Class<T> annotationClass) {
      if (annotationClass != null) {
         List<AnnotationValue<T>> values = this.getAnnotationValuesByType(annotationClass);
         return (T[])values.stream()
            .map(entries -> AnnotationMetadataSupport.buildAnnotation(annotationClass, entries))
            .toArray(value -> (Annotation[])Array.newInstance(annotationClass, value));
      } else {
         return (T[])AnnotationUtil.ZERO_ANNOTATIONS;
      }
   }

   @Override
   public boolean isEmpty() {
      return this.allAnnotations == null || this.allAnnotations.isEmpty();
   }

   @Override
   public boolean hasDeclaredAnnotation(String annotation) {
      return this.declaredAnnotations != null && StringUtils.isNotEmpty(annotation) && this.declaredAnnotations.containsKey(annotation);
   }

   @Override
   public boolean hasAnnotation(String annotation) {
      return this.hasDeclaredAnnotation(annotation)
         || this.allAnnotations != null && StringUtils.isNotEmpty(annotation) && this.allAnnotations.containsKey(annotation);
   }

   @Override
   public boolean hasStereotype(String annotation) {
      return this.hasAnnotation(annotation) || this.allStereotypes != null && StringUtils.isNotEmpty(annotation) && this.allStereotypes.containsKey(annotation);
   }

   @Override
   public boolean hasDeclaredStereotype(String annotation) {
      return this.hasDeclaredAnnotation(annotation)
         || this.declaredStereotypes != null && StringUtils.isNotEmpty(annotation) && this.declaredStereotypes.containsKey(annotation);
   }

   @NonNull
   @Override
   public Optional<Class<? extends Annotation>> getAnnotationTypeByStereotype(@Nullable String stereotype) {
      if (stereotype != null) {
         if (this.annotationsByStereotype != null) {
            List<String> annotations = (List)this.annotationsByStereotype.get(stereotype);
            if (CollectionUtils.isNotEmpty(annotations)) {
               return this.getAnnotationType((String)annotations.get(0));
            }
         }

         if (this.allAnnotations != null && this.allAnnotations.containsKey(stereotype)) {
            return this.getAnnotationType(stereotype);
         }

         if (this.declaredAnnotations != null && this.declaredAnnotations.containsKey(stereotype)) {
            return this.getAnnotationType(stereotype);
         }
      }

      return Optional.empty();
   }

   @NonNull
   @Override
   public Optional<String> getAnnotationNameByStereotype(@Nullable String stereotype) {
      if (stereotype != null) {
         if (this.annotationsByStereotype != null) {
            List<String> annotations = (List)this.annotationsByStereotype.get(stereotype);
            if (CollectionUtils.isNotEmpty(annotations)) {
               return Optional.of(annotations.get(0));
            }
         }

         if (this.allAnnotations != null && this.allAnnotations.containsKey(stereotype)) {
            return Optional.of(stereotype);
         }

         if (this.declaredAnnotations != null && this.declaredAnnotations.containsKey(stereotype)) {
            return Optional.of(stereotype);
         }
      }

      return Optional.empty();
   }

   @NonNull
   @Override
   public List<String> getAnnotationNamesByStereotype(@Nullable String stereotype) {
      if (stereotype == null) {
         return Collections.emptyList();
      } else {
         if (this.annotationsByStereotype != null) {
            List<String> annotations = (List)this.annotationsByStereotype.get(stereotype);
            if (annotations != null) {
               return Collections.unmodifiableList(annotations);
            }
         }

         if (this.allAnnotations != null && this.allAnnotations.containsKey(stereotype)) {
            return StringUtils.internListOf(stereotype);
         } else {
            return this.declaredAnnotations != null && this.declaredAnnotations.containsKey(stereotype)
               ? StringUtils.internListOf(stereotype)
               : Collections.emptyList();
         }
      }
   }

   @Override
   public <T extends Annotation> List<AnnotationValue<T>> getAnnotationValuesByStereotype(String stereotype) {
      if (stereotype == null) {
         return Collections.emptyList();
      } else {
         if (this.annotationsByStereotype != null) {
            List<String> annotations = (List)this.annotationsByStereotype.get(stereotype);
            if (annotations != null) {
               List<AnnotationValue<T>> result = new ArrayList(annotations.size());

               for(String annotation : annotations) {
                  String repeatableTypeName = this.getRepeatedName(annotation);
                  if (repeatableTypeName == null) {
                     repeatableTypeName = AnnotationMetadataSupport.getRepeatableAnnotation(annotation);
                  }

                  if (repeatableTypeName != null) {
                     List<AnnotationValue<T>> results = this.resolveRepeatableAnnotations(repeatableTypeName, this.allAnnotations, this.allStereotypes);
                     if (results != null) {
                        result.addAll(results);
                     }
                  } else {
                     result.add(this.getAnnotation(annotation));
                  }
               }

               return Collections.unmodifiableList(result);
            }
         }

         if (this.allAnnotations != null) {
            return this.getAnnotationValuesByName(stereotype);
         } else {
            return this.declaredAnnotations != null ? this.getDeclaredAnnotationValuesByName(stereotype) : Collections.emptyList();
         }
      }
   }

   @NonNull
   @Override
   public Set<String> getAnnotationNames() {
      return this.allAnnotations != null ? this.allAnnotations.keySet() : Collections.emptySet();
   }

   @Override
   public Set<String> getStereotypeAnnotationNames() {
      return this.allStereotypes != null ? Collections.unmodifiableSet(this.allStereotypes.keySet()) : Collections.emptySet();
   }

   @Override
   public Set<String> getDeclaredStereotypeAnnotationNames() {
      return this.declaredStereotypes != null ? Collections.unmodifiableSet(this.declaredStereotypes.keySet()) : Collections.emptySet();
   }

   @NonNull
   @Override
   public Set<String> getDeclaredAnnotationNames() {
      return this.declaredAnnotations != null ? this.declaredAnnotations.keySet() : Collections.emptySet();
   }

   @NonNull
   @Override
   public List<String> getDeclaredAnnotationNamesByStereotype(@Nullable String stereotype) {
      if (stereotype == null) {
         return Collections.emptyList();
      } else {
         if (this.annotationsByStereotype != null) {
            List<String> annotations = (List)this.annotationsByStereotype.get(stereotype);
            if (annotations != null) {
               annotations = new ArrayList(annotations);
               if (this.declaredAnnotations != null) {
                  annotations.removeIf(s -> !this.declaredAnnotations.containsKey(s));
                  return Collections.unmodifiableList(annotations);
               }

               return Collections.emptyList();
            }
         }

         return this.declaredAnnotations != null && this.declaredAnnotations.containsKey(stereotype)
            ? StringUtils.internListOf(stereotype)
            : Collections.emptyList();
      }
   }

   @NonNull
   @Override
   public Optional<Class<? extends Annotation>> getAnnotationType(@NonNull String name) {
      return AnnotationMetadataSupport.getAnnotationType(name);
   }

   @NonNull
   @Override
   public Optional<Class<? extends Annotation>> getAnnotationType(@NonNull String name, @NonNull ClassLoader classLoader) {
      return AnnotationMetadataSupport.getAnnotationType(name, classLoader);
   }

   @NonNull
   @Override
   public <T extends Annotation> Optional<AnnotationValue<T>> findAnnotation(@NonNull String annotation) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      if (this.allAnnotations != null && StringUtils.isNotEmpty(annotation)) {
         Map<CharSequence, Object> values = (Map)this.allAnnotations.get(annotation);
         if (values != null) {
            return Optional.of(new AnnotationValue(annotation, values, this.getDefaultValues(annotation)));
         }

         if (this.allStereotypes != null) {
            values = (Map)this.allStereotypes.get(annotation);
            if (values != null) {
               return Optional.of(new AnnotationValue(annotation, values, this.getDefaultValues(annotation)));
            }
         }
      }

      return Optional.empty();
   }

   @NonNull
   @Override
   public <T extends Annotation> Optional<AnnotationValue<T>> findDeclaredAnnotation(@NonNull String annotation) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      if (this.declaredAnnotations != null && StringUtils.isNotEmpty(annotation)) {
         Map<CharSequence, Object> values = (Map)this.declaredAnnotations.get(annotation);
         if (values != null) {
            return Optional.of(new AnnotationValue(annotation, values, this.getDefaultValues(annotation)));
         }

         if (this.declaredStereotypes != null) {
            values = (Map)this.declaredStereotypes.get(annotation);
            if (values != null) {
               return Optional.of(new AnnotationValue(annotation, values, this.getDefaultValues(annotation)));
            }
         }
      }

      return Optional.empty();
   }

   @NonNull
   @Override
   public <T> OptionalValues<T> getValues(@NonNull String annotation, @NonNull Class<T> valueType) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      ArgumentUtils.requireNonNull("valueType", (T)valueType);
      if (this.allAnnotations != null && StringUtils.isNotEmpty(annotation)) {
         Map<CharSequence, Object> values = (Map)this.allAnnotations.get(annotation);
         if (values != null) {
            return OptionalValues.of(valueType, values);
         }

         if (this.allStereotypes != null) {
            values = (Map)this.allStereotypes.get(annotation);
            if (values != null) {
               return OptionalValues.of(valueType, values);
            }
         }
      }

      return OptionalValues.empty();
   }

   @NonNull
   @Override
   public Map<CharSequence, Object> getValues(@NonNull String annotation) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      if (this.allAnnotations != null && StringUtils.isNotEmpty(annotation)) {
         Map<CharSequence, Object> values = (Map)this.allAnnotations.get(annotation);
         if (values != null) {
            return Collections.unmodifiableMap(values);
         }

         if (this.allStereotypes != null) {
            values = (Map)this.allStereotypes.get(annotation);
            if (values != null) {
               return Collections.unmodifiableMap(values);
            }
         }
      }

      return Collections.emptyMap();
   }

   @NonNull
   @Override
   public <T> Optional<T> getDefaultValue(@NonNull String annotation, @NonNull String member, @NonNull Argument<T> requiredType) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      ArgumentUtils.requireNonNull("member", (T)member);
      ArgumentUtils.requireNonNull("requiredType", requiredType);
      Map<String, Object> defaultValues = this.getDefaultValues(annotation);
      return defaultValues.containsKey(member) ? ConversionService.SHARED.convert(defaultValues.get(member), requiredType) : Optional.empty();
   }

   @Override
   public boolean isRepeatableAnnotation(Class<? extends Annotation> annotation) {
      return this.useRepeatableDefaults ? this.isRepeatableAnnotation(annotation.getName()) : super.isRepeatableAnnotation(annotation);
   }

   @Override
   public boolean isRepeatableAnnotation(String annotation) {
      return AnnotationMetadataSupport.getRepeatableAnnotation(annotation) != null;
   }

   @Override
   public Optional<String> findRepeatableAnnotation(Class<? extends Annotation> annotation) {
      return this.useRepeatableDefaults ? this.findRepeatableAnnotation(annotation.getName()) : super.findRepeatableAnnotation(annotation);
   }

   @Override
   public Optional<String> findRepeatableAnnotation(String annotation) {
      return Optional.ofNullable(AnnotationMetadataSupport.getRepeatableAnnotation(annotation));
   }

   public DefaultAnnotationMetadata clone() {
      DefaultAnnotationMetadata cloned = new DefaultAnnotationMetadata(
         this.declaredAnnotations != null ? this.cloneMapOfMapValue(this.declaredAnnotations) : null,
         this.declaredStereotypes != null ? this.cloneMapOfMapValue(this.declaredStereotypes) : null,
         this.allStereotypes != null ? this.cloneMapOfMapValue(this.allStereotypes) : null,
         this.allAnnotations != null ? this.cloneMapOfMapValue(this.allAnnotations) : null,
         this.annotationsByStereotype != null ? this.cloneMapOfListValue(this.annotationsByStereotype) : null,
         this.hasPropertyExpressions
      );
      if (this.repeated != null) {
         cloned.repeated = new HashMap(this.repeated);
      }

      return cloned;
   }

   protected final <X, Y, K> Map<K, Map<X, Y>> cloneMapOfMapValue(Map<K, Map<X, Y>> toClone) {
      return (Map<K, Map<X, Y>>)toClone.entrySet()
         .stream()
         .map(e -> new SimpleEntry(e.getKey(), this.cloneMap((Map)e.getValue())))
         .collect(
            Collectors.toMap(
               SimpleEntry::getKey, SimpleEntry::getValue, (a, b) -> a, () -> (HashMap)(toClone instanceof HashMap ? new HashMap() : new LinkedHashMap())
            )
         );
   }

   protected final <K, V> Map<K, List<V>> cloneMapOfListValue(Map<K, List<V>> toClone) {
      return (Map<K, List<V>>)toClone.entrySet()
         .stream()
         .map(e -> new SimpleEntry(e.getKey(), new ArrayList((Collection)e.getValue())))
         .collect(
            Collectors.toMap(
               SimpleEntry::getKey, SimpleEntry::getValue, (a, b) -> a, () -> (HashMap)(toClone instanceof HashMap ? new HashMap() : new LinkedHashMap())
            )
         );
   }

   protected final <K, V> Map<K, V> cloneMap(Map<K, V> map) {
      if (map instanceof HashMap) {
         return (Map<K, V>)((HashMap)map).clone();
      } else {
         return (Map<K, V>)(map instanceof LinkedHashMap ? (Map)((LinkedHashMap)map).clone() : new HashMap(map));
      }
   }

   protected void addAnnotation(String annotation, Map<CharSequence, Object> values) {
      this.addAnnotation(annotation, values, RetentionPolicy.RUNTIME);
   }

   protected void addAnnotation(String annotation, Map<CharSequence, Object> values, RetentionPolicy retentionPolicy) {
      if (annotation != null) {
         String repeatedName = this.getRepeatedName(annotation);
         Object v = values.get("value");
         if (v instanceof AnnotationValue[]) {
            AnnotationValue[] avs = (AnnotationValue[])v;

            for(AnnotationValue av : avs) {
               this.addRepeatable(annotation, av);
            }
         } else if (v instanceof Iterable && repeatedName != null) {
            for(Object o : (Iterable)v) {
               if (o instanceof AnnotationValue) {
                  this.addRepeatable(annotation, (AnnotationValue)o);
               }
            }
         } else {
            Map<String, Map<CharSequence, Object>> allAnnotations = this.getAllAnnotations();
            this.addAnnotation(annotation, values, null, allAnnotations, false, retentionPolicy);
         }
      }

   }

   protected final void addDefaultAnnotationValues(String annotation, Map<CharSequence, Object> values) {
      if (annotation != null) {
         Map<String, Map<CharSequence, Object>> annotationDefaults = this.annotationDefaultValues;
         if (annotationDefaults == null) {
            this.annotationDefaultValues = new LinkedHashMap();
            annotationDefaults = this.annotationDefaultValues;
         }

         this.putValues(annotation, values, annotationDefaults);
      }

   }

   @Internal
   public static boolean areAnnotationDefaultsRegistered(String annotation) {
      return AnnotationMetadataSupport.hasDefaultValues(annotation);
   }

   @Internal
   public static void registerAnnotationDefaults(String annotation, Map<String, Object> defaultValues) {
      AnnotationMetadataSupport.registerDefaultValues(annotation, defaultValues);
   }

   @Internal
   public static void registerAnnotationDefaults(AnnotationClassValue<?> annotation, Map<String, Object> defaultValues) {
      AnnotationMetadataSupport.registerDefaultValues(annotation, defaultValues);
   }

   @Internal
   public static void registerAnnotationType(AnnotationClassValue<?> annotation) {
      AnnotationMetadataSupport.registerAnnotationType(annotation);
   }

   @Internal
   public static void registerRepeatableAnnotations(Map<String, String> repeatableAnnotations) {
      AnnotationMetadataSupport.registerRepeatableAnnotations(repeatableAnnotations);
   }

   protected void addRepeatable(String annotationName, AnnotationValue annotationValue) {
      this.addRepeatable(annotationName, annotationValue, annotationValue.getRetentionPolicy());
   }

   protected void addRepeatable(String annotationName, AnnotationValue annotationValue, RetentionPolicy retentionPolicy) {
      if (StringUtils.isNotEmpty(annotationName) && annotationValue != null) {
         Map<String, Map<CharSequence, Object>> allAnnotations = this.getAllAnnotations();
         this.addRepeatableInternal(annotationName, annotationValue, allAnnotations, retentionPolicy);
      }

   }

   protected void addRepeatableStereotype(List<String> parents, String stereotype, AnnotationValue annotationValue) {
      Map<String, Map<CharSequence, Object>> allStereotypes = this.getAllStereotypes();
      List<String> annotationList = this.getAnnotationsByStereotypeInternal(stereotype);

      for(String parentAnnotation : parents) {
         if (!annotationList.contains(parentAnnotation)) {
            annotationList.add(parentAnnotation);
         }
      }

      this.addRepeatableInternal(stereotype, annotationValue, allStereotypes, RetentionPolicy.RUNTIME);
   }

   protected void addDeclaredRepeatableStereotype(List<String> parents, String stereotype, AnnotationValue annotationValue) {
      Map<String, Map<CharSequence, Object>> declaredStereotypes = this.getDeclaredStereotypesInternal();
      List<String> annotationList = this.getAnnotationsByStereotypeInternal(stereotype);

      for(String parentAnnotation : parents) {
         if (!annotationList.contains(parentAnnotation)) {
            annotationList.add(parentAnnotation);
         }
      }

      this.addRepeatableInternal(stereotype, annotationValue, declaredStereotypes, RetentionPolicy.RUNTIME);
      this.addRepeatableInternal(stereotype, annotationValue, this.getAllStereotypes(), RetentionPolicy.RUNTIME);
   }

   protected void addDeclaredRepeatable(String annotationName, AnnotationValue annotationValue) {
      this.addDeclaredRepeatable(annotationName, annotationValue, annotationValue.getRetentionPolicy());
   }

   protected void addDeclaredRepeatable(String annotationName, AnnotationValue annotationValue, RetentionPolicy retentionPolicy) {
      if (StringUtils.isNotEmpty(annotationName) && annotationValue != null) {
         Map<String, Map<CharSequence, Object>> allAnnotations = this.getDeclaredAnnotationsInternal();
         this.addRepeatableInternal(annotationName, annotationValue, allAnnotations, retentionPolicy);
         this.addRepeatable(annotationName, annotationValue);
      }

   }

   protected final void addStereotype(List<String> parentAnnotations, String stereotype, Map<CharSequence, Object> values) {
      this.addStereotype(parentAnnotations, stereotype, values, RetentionPolicy.RUNTIME);
   }

   protected final void addStereotype(List<String> parentAnnotations, String stereotype, Map<CharSequence, Object> values, RetentionPolicy retentionPolicy) {
      if (stereotype != null) {
         String repeatedName = this.getRepeatedName(stereotype);
         if (repeatedName != null) {
            Object v = values.get("value");
            if (v instanceof AnnotationValue[]) {
               AnnotationValue[] avs = (AnnotationValue[])v;

               for(AnnotationValue av : avs) {
                  this.addRepeatableStereotype(parentAnnotations, stereotype, av);
               }
            } else if (v instanceof Iterable) {
               for(Object o : (Iterable)v) {
                  if (o instanceof AnnotationValue) {
                     this.addRepeatableStereotype(parentAnnotations, stereotype, (AnnotationValue)o);
                  }
               }
            }
         } else {
            Map<String, Map<CharSequence, Object>> allStereotypes = this.getAllStereotypes();
            List<String> annotationList = this.getAnnotationsByStereotypeInternal(stereotype);
            if (!parentAnnotations.isEmpty()) {
               String parentAnnotation = CollectionUtils.last(parentAnnotations);
               if (!annotationList.contains(parentAnnotation)) {
                  annotationList.add(parentAnnotation);
               }
            }

            this.addAnnotation(stereotype, values, null, allStereotypes, false, retentionPolicy);
         }
      }

   }

   protected void addDeclaredStereotype(List<String> parentAnnotations, String stereotype, Map<CharSequence, Object> values) {
      this.addDeclaredStereotype(parentAnnotations, stereotype, values, RetentionPolicy.RUNTIME);
   }

   protected void addDeclaredStereotype(List<String> parentAnnotations, String stereotype, Map<CharSequence, Object> values, RetentionPolicy retentionPolicy) {
      if (stereotype != null) {
         String repeatedName = this.getRepeatedName(stereotype);
         if (repeatedName != null) {
            Object v = values.get("value");
            if (v instanceof AnnotationValue[]) {
               AnnotationValue[] avs = (AnnotationValue[])v;

               for(AnnotationValue av : avs) {
                  this.addDeclaredRepeatableStereotype(parentAnnotations, stereotype, av);
               }
            } else if (v instanceof Iterable) {
               for(Object o : (Iterable)v) {
                  if (o instanceof AnnotationValue) {
                     this.addDeclaredRepeatableStereotype(parentAnnotations, stereotype, (AnnotationValue)o);
                  }
               }
            }
         } else {
            Map<String, Map<CharSequence, Object>> declaredStereotypes = this.getDeclaredStereotypesInternal();
            Map<String, Map<CharSequence, Object>> allStereotypes = this.getAllStereotypes();
            List<String> annotationList = this.getAnnotationsByStereotypeInternal(stereotype);
            if (!parentAnnotations.isEmpty()) {
               String parentAnnotation = CollectionUtils.last(parentAnnotations);
               if (!annotationList.contains(parentAnnotation)) {
                  annotationList.add(parentAnnotation);
               }
            }

            this.addAnnotation(stereotype, values, declaredStereotypes, allStereotypes, true, retentionPolicy);
         }
      }

   }

   protected void addDeclaredAnnotation(String annotation, Map<CharSequence, Object> values) {
      this.addDeclaredAnnotation(annotation, values, RetentionPolicy.RUNTIME);
   }

   protected void addDeclaredAnnotation(String annotation, Map<CharSequence, Object> values, RetentionPolicy retentionPolicy) {
      if (annotation != null) {
         boolean hasOtherMembers = false;
         String repeatedName = this.getRepeatedName(annotation);
         if (repeatedName != null) {
            for(Entry<CharSequence, Object> entry : values.entrySet()) {
               if (((CharSequence)entry.getKey()).equals("value")) {
                  Object v = entry.getValue();
                  if (v instanceof AnnotationValue[]) {
                     AnnotationValue[] avs = (AnnotationValue[])v;

                     for(AnnotationValue av : avs) {
                        this.addDeclaredRepeatable(annotation, av);
                     }
                  } else if (v instanceof Iterable) {
                     for(Object o : (Iterable)v) {
                        if (o instanceof AnnotationValue) {
                           this.addDeclaredRepeatable(annotation, (AnnotationValue)o);
                        }
                     }
                  }
               } else {
                  hasOtherMembers = true;
               }
            }
         }

         if (repeatedName == null || hasOtherMembers) {
            Map<String, Map<CharSequence, Object>> declaredAnnotations = this.getDeclaredAnnotationsInternal();
            Map<String, Map<CharSequence, Object>> allAnnotations = this.getAllAnnotations();
            this.addAnnotation(annotation, values, declaredAnnotations, allAnnotations, true, retentionPolicy);
         }
      }

   }

   @Internal
   void dump() {
      System.out.println("declaredAnnotations = " + this.declaredAnnotations);
      System.out.println("declaredStereotypes = " + this.declaredStereotypes);
      System.out.println("allAnnotations = " + this.allAnnotations);
      System.out.println("allStereotypes = " + this.allStereotypes);
      System.out.println("annotationsByStereotype = " + this.annotationsByStereotype);
   }

   private <T extends Annotation> List<AnnotationValue<T>> resolveAnnotationValuesByType(
      Class<T> annotationType, Map<String, Map<CharSequence, Object>> sourceAnnotations, Map<String, Map<CharSequence, Object>> sourceStereotypes
   ) {
      Repeatable repeatable = (Repeatable)annotationType.getAnnotation(Repeatable.class);
      if (repeatable != null) {
         Class<? extends Annotation> repeatableType = repeatable.value();
         String repeatableTypeName = repeatableType.getName();
         return this.resolveRepeatableAnnotations(repeatableTypeName, sourceStereotypes, sourceAnnotations);
      } else {
         return null;
      }
   }

   @Nullable
   private <T extends Annotation> List<AnnotationValue<T>> resolveRepeatableAnnotations(
      String repeatableTypeName, Map<String, Map<CharSequence, Object>> sourceStereotypes, Map<String, Map<CharSequence, Object>> sourceAnnotations
   ) {
      if (this.hasStereotype(repeatableTypeName)) {
         List<AnnotationValue<T>> results = new ArrayList();
         if (sourceAnnotations != null) {
            Map<CharSequence, Object> values = (Map)sourceAnnotations.get(repeatableTypeName);
            this.addAnnotationValuesFromData(results, values);
         }

         if (sourceStereotypes != null) {
            Map<CharSequence, Object> values = (Map)sourceStereotypes.get(repeatableTypeName);
            this.addAnnotationValuesFromData(results, values);
         }

         return results;
      } else {
         return null;
      }
   }

   private void addAnnotation(
      String annotation,
      Map<CharSequence, Object> values,
      Map<String, Map<CharSequence, Object>> declaredAnnotations,
      Map<String, Map<CharSequence, Object>> allAnnotations,
      boolean isDeclared,
      RetentionPolicy retentionPolicy
   ) {
      if (isDeclared && declaredAnnotations != null) {
         this.putValues(annotation, values, declaredAnnotations);
      }

      this.putValues(annotation, values, allAnnotations);
      if (retentionPolicy == RetentionPolicy.SOURCE) {
         this.addSourceRetentionAnnotation(annotation);
      }

   }

   private void addSourceRetentionAnnotation(String annotation) {
      if (this.sourceRetentionAnnotations == null) {
         this.sourceRetentionAnnotations = new HashSet(5);
      }

      this.sourceRetentionAnnotations.add(annotation);
   }

   private void putValues(String annotation, Map<CharSequence, Object> values, Map<String, Map<CharSequence, Object>> currentAnnotationValues) {
      Map<CharSequence, Object> existing = (Map)currentAnnotationValues.get(annotation);
      boolean hasValues = CollectionUtils.isNotEmpty(values);
      if (existing != null && hasValues) {
         if (existing.isEmpty()) {
            existing = new LinkedHashMap();
            currentAnnotationValues.put(annotation, existing);
         }

         for(CharSequence key : values.keySet()) {
            if (!existing.containsKey(key)) {
               existing.put(key, values.get(key));
            }
         }
      } else {
         if (!hasValues) {
            existing = existing == null ? Collections.emptyMap() : existing;
         } else {
            existing = new LinkedHashMap(values.size());
            existing.putAll(values);
         }

         currentAnnotationValues.put(annotation, existing);
      }

   }

   private Map<String, Map<CharSequence, Object>> getAllStereotypes() {
      Map<String, Map<CharSequence, Object>> stereotypes = this.allStereotypes;
      if (stereotypes == null) {
         stereotypes = new HashMap(3);
         this.allStereotypes = stereotypes;
      }

      return stereotypes;
   }

   private Map<String, Map<CharSequence, Object>> getDeclaredStereotypesInternal() {
      Map<String, Map<CharSequence, Object>> stereotypes = this.declaredStereotypes;
      if (stereotypes == null) {
         stereotypes = new HashMap(3);
         this.declaredStereotypes = stereotypes;
      }

      return stereotypes;
   }

   private Map<String, Map<CharSequence, Object>> getAllAnnotations() {
      Map<String, Map<CharSequence, Object>> annotations = this.allAnnotations;
      if (annotations == null) {
         annotations = new HashMap(3);
         this.allAnnotations = annotations;
      }

      return annotations;
   }

   private Map<String, Map<CharSequence, Object>> getDeclaredAnnotationsInternal() {
      Map<String, Map<CharSequence, Object>> annotations = this.declaredAnnotations;
      if (annotations == null) {
         annotations = new HashMap(3);
         this.declaredAnnotations = annotations;
      }

      return annotations;
   }

   private List<String> getAnnotationsByStereotypeInternal(String stereotype) {
      return (List<String>)this.getAnnotationsByStereotypeInternal().computeIfAbsent(stereotype, s -> new ArrayList());
   }

   private String getRepeatedName(String annotation) {
      return this.repeated != null ? (String)this.repeated.get(annotation) : null;
   }

   private Map<String, List<String>> getAnnotationsByStereotypeInternal() {
      Map<String, List<String>> annotations = this.annotationsByStereotype;
      if (annotations == null) {
         annotations = new HashMap(3);
         this.annotationsByStereotype = annotations;
      }

      return annotations;
   }

   @Nullable
   private Object getRawSingleValue(@NonNull String annotation, @NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      Object rawValue = this.getRawValue(annotation, member);
      if (rawValue != null) {
         if (rawValue.getClass().isArray()) {
            int len = Array.getLength(rawValue);
            if (len > 0) {
               rawValue = Array.get(rawValue, 0);
            }
         } else if (rawValue instanceof Iterable) {
            Iterator i = ((Iterable)rawValue).iterator();
            if (i.hasNext()) {
               rawValue = i.next();
            }
         }
      }

      return valueMapper != null && rawValue instanceof CharSequence ? valueMapper.apply(rawValue) : rawValue;
   }

   @Nullable
   private Object getRawValue(@NonNull String annotation, @NonNull String member) {
      Object rawValue = null;
      if (this.allAnnotations != null && StringUtils.isNotEmpty(annotation)) {
         Map<CharSequence, Object> values = (Map)this.allAnnotations.get(annotation);
         if (values != null) {
            rawValue = values.get(member);
         } else if (this.allStereotypes != null) {
            values = (Map)this.allStereotypes.get(annotation);
            if (values != null) {
               rawValue = values.get(member);
            }
         }
      }

      return rawValue;
   }

   private void addRepeatableInternal(
      String annotationName, AnnotationValue annotationValue, Map<String, Map<CharSequence, Object>> allAnnotations, RetentionPolicy retentionPolicy
   ) {
      this.addRepeatableInternal(annotationName, "value", annotationValue, allAnnotations, retentionPolicy);
   }

   private void addRepeatableInternal(
      String annotationName,
      String member,
      AnnotationValue annotationValue,
      Map<String, Map<CharSequence, Object>> allAnnotations,
      RetentionPolicy retentionPolicy
   ) {
      if (this.repeated == null) {
         this.repeated = new HashMap(2);
      }

      this.repeated.put(annotationName, annotationValue.getAnnotationName());
      if (retentionPolicy == RetentionPolicy.SOURCE) {
         this.addSourceRetentionAnnotation(annotationName);
      }

      Map<CharSequence, Object> values = (Map)allAnnotations.computeIfAbsent(annotationName, s -> new HashMap());
      Object v = values.get(member);
      if (v != null) {
         if (v.getClass().isArray()) {
            Object[] array = v;
            Set newValues = new LinkedHashSet(array.length + 1);
            newValues.addAll(Arrays.asList(array));
            newValues.add(annotationValue);
            values.put(member, newValues);
         } else if (v instanceof Collection) {
            ((Collection)v).add(annotationValue);
         }
      } else {
         Set<Object> newValues = new LinkedHashSet(2);
         newValues.add(annotationValue);
         values.put(member, newValues);
      }

   }

   @Internal
   public static AnnotationMetadata mutateMember(AnnotationMetadata annotationMetadata, String annotationName, String member, Object value) {
      return mutateMember(annotationMetadata, annotationName, Collections.singletonMap(member, value));
   }

   @Internal
   public static void contributeDefaults(AnnotationMetadata target, AnnotationMetadata source) {
      if (source instanceof AnnotationMetadataHierarchy) {
         source = source.getDeclaredMetadata();
      }

      if (target instanceof DefaultAnnotationMetadata && source instanceof DefaultAnnotationMetadata) {
         DefaultAnnotationMetadata damTarget = (DefaultAnnotationMetadata)target;
         DefaultAnnotationMetadata damSource = (DefaultAnnotationMetadata)source;
         Map<String, Map<CharSequence, Object>> existingDefaults = damTarget.annotationDefaultValues;
         if (existingDefaults != null) {
            Map<String, Map<CharSequence, Object>> additionalDefaults = damSource.annotationDefaultValues;
            if (additionalDefaults != null) {
               existingDefaults.putAll(additionalDefaults);
            }
         } else {
            Map<String, Map<CharSequence, Object>> additionalDefaults = damSource.annotationDefaultValues;
            if (additionalDefaults != null) {
               additionalDefaults.forEach(damTarget::addDefaultAnnotationValues);
            }
         }
      }

      contributeRepeatable(target, source);
   }

   @Internal
   public static void contributeRepeatable(AnnotationMetadata target, AnnotationMetadata source) {
      if (source instanceof AnnotationMetadataHierarchy) {
         source = source.getDeclaredMetadata();
      }

      if (target instanceof DefaultAnnotationMetadata && source instanceof DefaultAnnotationMetadata) {
         DefaultAnnotationMetadata damTarget = (DefaultAnnotationMetadata)target;
         DefaultAnnotationMetadata damSource = (DefaultAnnotationMetadata)source;
         if (damSource.repeated != null && !damSource.repeated.isEmpty()) {
            if (damTarget.repeated == null) {
               damTarget.repeated = new HashMap(damSource.repeated);
            } else {
               damTarget.repeated.putAll(damSource.repeated);
            }
         }
      }

   }

   @Internal
   public static void contributeRepeatable(AnnotationMetadata target, ClassElement classElement) {
      contributeRepeatable(target, classElement, new HashSet());
   }

   private static void contributeRepeatable(AnnotationMetadata target, ClassElement classElement, Set<ClassElement> alreadySeen) {
      alreadySeen.add(classElement);
      contributeRepeatable(target, classElement.getAnnotationMetadata());

      for(ClassElement element : classElement.getTypeArguments().values()) {
         if (!alreadySeen.contains(classElement)) {
            contributeRepeatable(target, element);
         }
      }

   }

   @Internal
   public static AnnotationMetadata mutateMember(AnnotationMetadata annotationMetadata, String annotationName, Map<CharSequence, Object> members) {
      if (StringUtils.isEmpty(annotationName)) {
         throw new IllegalArgumentException("Argument [annotationName] cannot be blank");
      } else {
         if (!members.isEmpty()) {
            for(Entry<CharSequence, Object> entry : members.entrySet()) {
               if (StringUtils.isEmpty((CharSequence)entry.getKey())) {
                  throw new IllegalArgumentException("Argument [members] cannot have a blank key");
               }

               if (entry.getValue() == null) {
                  throw new IllegalArgumentException("Argument [members] cannot have a null value. Key [" + entry.getKey() + "]");
               }
            }
         }

         if (!(annotationMetadata instanceof DefaultAnnotationMetadata)) {
            MutableAnnotationMetadata mutableAnnotationMetadata = new MutableAnnotationMetadata();
            mutableAnnotationMetadata.addDeclaredAnnotation(annotationName, members);
            return mutableAnnotationMetadata;
         } else {
            DefaultAnnotationMetadata defaultMetadata = (DefaultAnnotationMetadata)annotationMetadata;
            defaultMetadata = defaultMetadata.clone();
            defaultMetadata.addDeclaredAnnotation(annotationName, members);
            return defaultMetadata;
         }
      }
   }

   protected <A extends Annotation> void removeAnnotationIf(@NonNull Predicate<AnnotationValue<A>> predicate) {
      this.removeAnnotationsIf(predicate, this.declaredAnnotations);
      this.removeAnnotationsIf(predicate, this.allAnnotations);
   }

   private <A extends Annotation> void removeAnnotationsIf(@NonNull Predicate<AnnotationValue<A>> predicate, Map<String, Map<CharSequence, Object>> annotations) {
      if (annotations != null) {
         annotations.entrySet().removeIf(entry -> {
            String annotationName = (String)entry.getKey();
            if (predicate.test(new AnnotationValue(annotationName, (Map<CharSequence, Object>)entry.getValue()))) {
               this.removeFromStereotypes(annotationName, annotations);
               return true;
            } else {
               return false;
            }
         });
      }

   }

   protected void removeAnnotation(String annotationType) {
      if (annotationType != null) {
         if (this.annotationDefaultValues != null) {
            this.annotationDefaultValues.remove(annotationType);
         }

         if (this.allAnnotations != null) {
            this.allAnnotations.remove(annotationType);
         }

         Map<String, Map<CharSequence, Object>> declaredAnnotations = this.declaredAnnotations;
         if (declaredAnnotations != null) {
            declaredAnnotations.remove(annotationType);
            this.removeFromStereotypes(annotationType, declaredAnnotations);
         }

         if (this.repeated != null) {
            this.repeated.remove(annotationType);
         }
      }

   }

   protected void removeStereotype(String annotationType) {
      if (annotationType != null && this.annotationsByStereotype != null && this.annotationsByStereotype.remove(annotationType) != null) {
         if (this.allStereotypes != null) {
            this.allStereotypes.remove(annotationType);
         }

         if (this.declaredStereotypes != null) {
            this.declaredStereotypes.remove(annotationType);
         }

         Iterator<Entry<String, List<String>>> i = this.annotationsByStereotype.entrySet().iterator();

         while(i.hasNext()) {
            Entry<String, List<String>> entry = (Entry)i.next();
            List<String> value = (List)entry.getValue();
            if (value.remove(annotationType) && value.isEmpty()) {
               i.remove();
            }
         }
      }

   }

   private void removeFromStereotypes(String annotationType, Map<String, Map<CharSequence, Object>> declaredAnnotations) {
      if (this.annotationsByStereotype != null) {
         Iterator<Entry<String, List<String>>> i = this.annotationsByStereotype.entrySet().iterator();
         Set<String> toBeRemoved = CollectionUtils.setOf(annotationType);

         while(i.hasNext()) {
            Entry<String, List<String>> entry = (Entry)i.next();
            String stereotypeName = (String)entry.getKey();
            List<String> value = (List)entry.getValue();
            if (value.removeAll(toBeRemoved)) {
               if (value.isEmpty()) {
                  toBeRemoved.add(stereotypeName);
                  i.remove();
                  if (this.allStereotypes != null) {
                     this.allStereotypes.remove(stereotypeName);
                  }

                  if (this.declaredStereotypes != null) {
                     this.declaredStereotypes.remove(stereotypeName);
                  }

                  if (this.annotationDefaultValues != null) {
                     this.annotationDefaultValues.remove(stereotypeName);
                  }
               }

               if ("io.micronaut.aop.Around".equals(stereotypeName)
                  || "io.micronaut.aop.Introduction".equals(stereotypeName)
                  || "io.micronaut.aop.AroundConstruct".equals(stereotypeName)) {
                  this.purgeInterceptorBindings(declaredAnnotations, toBeRemoved);
                  this.purgeInterceptorBindings(this.allAnnotations, toBeRemoved);
               }
            }
         }
      }

   }

   private void purgeInterceptorBindings(Map<String, Map<CharSequence, Object>> declaredAnnotations, Set<String> toBeRemoved) {
      if (declaredAnnotations != null) {
         Map<CharSequence, Object> v = (Map)declaredAnnotations.get("io.micronaut.aop.InterceptorBindingDefinitions");
         if (v != null) {
            Object o = v.get("value");
            if (o instanceof Collection) {
               Collection<AnnotationValue<?>> col = (Collection)o;
               col.removeIf(av -> Arrays.stream(av.annotationClassValues("value")).anyMatch(acv -> toBeRemoved.contains(acv.getName())));
               if (col.isEmpty()) {
                  declaredAnnotations.remove("io.micronaut.aop.InterceptorBindingDefinitions");
               }
            }
         }
      }

   }

   static {
      ConversionService.SHARED.addConverter(AnnotationValue.class, Annotation.class, (TypeConverter)((object, targetType, context) -> {
         Optional<Class> annotationClass = ClassUtils.forName(object.getAnnotationName(), targetType.getClassLoader());
         return annotationClass.map(aClass -> AnnotationMetadataSupport.buildAnnotation(aClass, object));
      }));
      ConversionService.SHARED.addConverter(AnnotationValue[].class, Object[].class, (TypeConverter)((object, targetType, context) -> {
         List result = new ArrayList();
         Class annotationClass = null;

         for(AnnotationValue annotationValue : object) {
            if (annotationClass == null) {
               Optional<Class> aClass = ClassUtils.forName(annotationValue.getAnnotationName(), targetType.getClassLoader());
               if (!aClass.isPresent()) {
                  break;
               }

               annotationClass = (Class)aClass.get();
            }

            Annotation annotation = AnnotationMetadataSupport.buildAnnotation(annotationClass, annotationValue);
            result.add(annotation);
         }

         return !result.isEmpty() ? Optional.of(result.toArray(Array.newInstance(annotationClass, result.size()))) : Optional.empty();
      }));
   }
}
