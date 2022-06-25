package io.micronaut.core.annotation;

import io.micronaut.core.naming.NameUtils;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.ReflectionUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.value.OptionalValues;
import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

public interface AnnotationMetadata extends AnnotationSource {
   AnnotationMetadata EMPTY_METADATA = new EmptyAnnotationMetadata();
   String VALUE_MEMBER = "value";
   String CLASS_NAME_SUFFIX = "$$AnnotationMetadata";

   @NonNull
   default AnnotationMetadata getDeclaredMetadata() {
      return this;
   }

   default boolean hasPropertyExpressions() {
      return true;
   }

   @NonNull
   default List<String> getAnnotationNamesByStereotype(@Nullable String stereotype) {
      return Collections.emptyList();
   }

   @NonNull
   default <T extends Annotation> List<AnnotationValue<T>> getAnnotationValuesByStereotype(@Nullable String stereotype) {
      return Collections.emptyList();
   }

   @NonNull
   default Set<String> getAnnotationNames() {
      return Collections.emptySet();
   }

   @NonNull
   default Set<String> getStereotypeAnnotationNames() {
      return Collections.emptySet();
   }

   @NonNull
   default Set<String> getDeclaredStereotypeAnnotationNames() {
      return Collections.emptySet();
   }

   @NonNull
   default Set<String> getDeclaredAnnotationNames() {
      return Collections.emptySet();
   }

   @NonNull
   default List<String> getDeclaredAnnotationNamesByStereotype(@Nullable String stereotype) {
      return Collections.emptyList();
   }

   @NonNull
   default <T> OptionalValues<T> getValues(@NonNull String annotation, @NonNull Class<T> valueType) {
      return OptionalValues.empty();
   }

   @NonNull
   default Map<CharSequence, Object> getValues(@NonNull String annotation) {
      AnnotationValue<Annotation> ann = this.getAnnotation(annotation);
      return ann != null ? ann.getValues() : Collections.emptyMap();
   }

   default <T> Optional<T> getDefaultValue(@NonNull String annotation, @NonNull String member, @NonNull Argument<T> requiredType) {
      return Optional.empty();
   }

   @NonNull
   default <T extends Annotation> List<AnnotationValue<T>> getAnnotationValuesByType(@NonNull Class<T> annotationType) {
      return Collections.emptyList();
   }

   @NonNull
   default <T extends Annotation> List<AnnotationValue<T>> getAnnotationValuesByName(@NonNull String annotationType) {
      return Collections.emptyList();
   }

   @NonNull
   default <T extends Annotation> List<AnnotationValue<T>> getDeclaredAnnotationValuesByType(@NonNull Class<T> annotationType) {
      return Collections.emptyList();
   }

   @NonNull
   default <T extends Annotation> List<AnnotationValue<T>> getDeclaredAnnotationValuesByName(@NonNull String annotationType) {
      return Collections.emptyList();
   }

   default boolean hasDeclaredAnnotation(@Nullable String annotation) {
      return false;
   }

   default boolean hasAnnotation(@Nullable String annotation) {
      return false;
   }

   default boolean hasSimpleAnnotation(@Nullable String annotation) {
      if (annotation == null) {
         return false;
      } else {
         for(String a : this.getAnnotationNames()) {
            if (NameUtils.getSimpleName(a).equalsIgnoreCase(annotation)) {
               return true;
            }
         }

         return false;
      }
   }

   default boolean hasSimpleDeclaredAnnotation(@Nullable String annotation) {
      if (annotation == null) {
         return false;
      } else {
         for(String a : this.getDeclaredAnnotationNames()) {
            if (NameUtils.getSimpleName(a).equalsIgnoreCase(annotation)) {
               return true;
            }
         }

         return false;
      }
   }

   default boolean hasStereotype(@Nullable String annotation) {
      return false;
   }

   default boolean hasDeclaredStereotype(@Nullable String annotation) {
      return false;
   }

   default boolean hasDeclaredStereotype(@Nullable String... annotations) {
      if (ArrayUtils.isEmpty(annotations)) {
         return false;
      } else {
         for(String annotation : annotations) {
            if (this.hasDeclaredStereotype(annotation)) {
               return true;
            }
         }

         return false;
      }
   }

   @NonNull
   default Map<String, Object> getDefaultValues(@NonNull String annotation) {
      return Collections.emptyMap();
   }

   default <T> Optional<T> getDefaultValue(@NonNull String annotation, @NonNull String member, @NonNull Class<T> requiredType) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      ArgumentUtils.requireNonNull("member", (T)member);
      ArgumentUtils.requireNonNull("requiredType", (T)requiredType);
      return this.getDefaultValue(annotation, member, Argument.of(requiredType));
   }

   default <T> Optional<T> getDefaultValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, @NonNull Argument<T> requiredType) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      ArgumentUtils.requireNonNull("member", (T)member);
      ArgumentUtils.requireNonNull("requiredType", requiredType);
      return this.getDefaultValue(annotation.getName(), member, requiredType);
   }

   @Override
   default boolean isAnnotationPresent(@NonNull Class<? extends Annotation> annotationClass) {
      return annotationClass == null ? false : this.hasAnnotation(annotationClass);
   }

   @Override
   default boolean isDeclaredAnnotationPresent(@NonNull Class<? extends Annotation> annotationClass) {
      return annotationClass == null ? false : this.hasDeclaredAnnotation(annotationClass);
   }

   @Override
   default boolean isAnnotationPresent(@NonNull String annotationName) {
      return annotationName == null ? false : this.hasAnnotation(annotationName);
   }

   @Override
   default boolean isDeclaredAnnotationPresent(@NonNull String annotationName) {
      return annotationName == null ? false : this.hasDeclaredAnnotation(annotationName);
   }

   default <T> Optional<T> getDefaultValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, @NonNull Class<T> requiredType) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      return this.getDefaultValue(annotation.getName(), member, requiredType);
   }

   default <T> Optional<T> getValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, @NonNull Class<T> requiredType) {
      ArgumentUtils.requireNonNull("requiredType", (T)requiredType);
      return this.getValue(annotation, member, Argument.of(requiredType));
   }

   default <T> Optional<T> getValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, @NonNull Argument<T> requiredType) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      ArgumentUtils.requireNonNull("member", (T)member);
      ArgumentUtils.requireNonNull("requiredType", requiredType);
      if (this.isRepeatableAnnotation(annotation)) {
         List<? extends AnnotationValue<? extends Annotation>> values = this.getAnnotationValuesByType(annotation);
         return !values.isEmpty() ? ((AnnotationValue)values.iterator().next()).get(member, requiredType) : Optional.empty();
      } else {
         Optional<? extends AnnotationValue<? extends Annotation>> values = this.findAnnotation(annotation);
         Optional<T> value = values.flatMap(av -> av.get(member, requiredType));
         return !value.isPresent() && this.hasStereotype(annotation) ? this.getDefaultValue(annotation, member, requiredType) : value;
      }
   }

   default Optional<String> getAnnotationNameByStereotype(@Nullable String stereotype) {
      List<String> annotationNamesByStereotype = this.getAnnotationNamesByStereotype(stereotype);
      return annotationNamesByStereotype.isEmpty() ? Optional.empty() : Optional.of(annotationNamesByStereotype.get(0));
   }

   default Optional<String> getDeclaredAnnotationNameByStereotype(@Nullable String stereotype) {
      List<String> declaredAnnotationNamesByStereotype = this.getDeclaredAnnotationNamesByStereotype(stereotype);
      return declaredAnnotationNamesByStereotype.isEmpty() ? Optional.empty() : Optional.of(declaredAnnotationNamesByStereotype.get(0));
   }

   default Optional<Class<? extends Annotation>> getAnnotationTypeByStereotype(@NonNull Class<? extends Annotation> stereotype) {
      ArgumentUtils.requireNonNull("stereotype", stereotype);
      return this.getAnnotationTypeByStereotype(stereotype.getName());
   }

   default Optional<Class<? extends Annotation>> getDeclaredAnnotationTypeByStereotype(@NonNull Class<? extends Annotation> stereotype) {
      ArgumentUtils.requireNonNull("stereotype", stereotype);
      return this.getDeclaredAnnotationTypeByStereotype(stereotype.getName());
   }

   default Optional<Class<? extends Annotation>> getDeclaredAnnotationTypeByStereotype(@Nullable String stereotype) {
      return this.getDeclaredAnnotationNameByStereotype(stereotype).flatMap(this::getAnnotationType);
   }

   default Optional<Class<? extends Annotation>> getAnnotationType(@NonNull String name, @NonNull ClassLoader classLoader) {
      ArgumentUtils.requireNonNull("name", name);
      Optional<Class> aClass = ClassUtils.forName(name, classLoader);
      Class clazz = (Class)aClass.orElse(null);
      return clazz != null && Annotation.class.isAssignableFrom(clazz) ? aClass : Optional.empty();
   }

   default Optional<Class<? extends Annotation>> getAnnotationType(@NonNull String name) {
      return this.getAnnotationType(name, this.getClass().getClassLoader());
   }

   default Optional<Class<? extends Annotation>> getAnnotationTypeByStereotype(@Nullable String stereotype) {
      return this.getAnnotationNameByStereotype(stereotype).flatMap(this::getAnnotationType);
   }

   default Optional<String> getAnnotationNameByStereotype(@NonNull Class<? extends Annotation> stereotype) {
      ArgumentUtils.requireNonNull("stereotype", stereotype);
      return this.getAnnotationNameByStereotype(stereotype.getName());
   }

   @NonNull
   default <T> OptionalValues<T> getValues(@NonNull Class<? extends Annotation> annotation, @NonNull Class<T> valueType) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      ArgumentUtils.requireNonNull("valueType", (T)valueType);
      return this.getValues(annotation.getName(), valueType);
   }

   @NonNull
   default List<String> getAnnotationNamesByStereotype(@NonNull Class<? extends Annotation> stereotype) {
      ArgumentUtils.requireNonNull("stereotype", stereotype);
      return this.getAnnotationNamesByStereotype(stereotype.getName());
   }

   @NonNull
   default List<Class<? extends Annotation>> getAnnotationTypesByStereotype(@NonNull Class<? extends Annotation> stereotype) {
      return this.getAnnotationTypesByStereotype(stereotype.getName());
   }

   @NonNull
   default List<Class<? extends Annotation>> getAnnotationTypesByStereotype(@NonNull String stereotype) {
      ArgumentUtils.requireNonNull("stereotype", stereotype);
      List<String> names = this.getAnnotationNamesByStereotype(stereotype);
      List<Class<? extends Annotation>> list = new ArrayList(names.size());

      for(String name : names) {
         Optional<Class<? extends Annotation>> opt = this.getAnnotationType(name);
         if (opt.isPresent()) {
            Class<? extends Annotation> aClass = (Class)opt.get();
            list.add(aClass);
         }
      }

      return list;
   }

   @NonNull
   default List<Class<? extends Annotation>> getAnnotationTypesByStereotype(@NonNull Class<? extends Annotation> stereotype, @NonNull ClassLoader classLoader) {
      ArgumentUtils.requireNonNull("stereotype", stereotype);
      List<String> names = this.getAnnotationNamesByStereotype(stereotype.getName());
      List<Class<? extends Annotation>> list = new ArrayList(names.size());

      for(String name : names) {
         Optional<Class<? extends Annotation>> opt = this.getAnnotationType(name, classLoader);
         if (opt.isPresent()) {
            Class<? extends Annotation> aClass = (Class)opt.get();
            list.add(aClass);
         }
      }

      return list;
   }

   @Override
   default <T extends Annotation> Optional<AnnotationValue<T>> findAnnotation(@NonNull Class<T> annotationClass) {
      ArgumentUtils.requireNonNull("annotationClass", (T)annotationClass);
      if (this.isRepeatableAnnotation(annotationClass)) {
         List<AnnotationValue<T>> values = this.getAnnotationValuesByType(annotationClass);
         return values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
      } else {
         return this.findAnnotation(annotationClass.getName());
      }
   }

   @Override
   default <T extends Annotation> Optional<AnnotationValue<T>> findDeclaredAnnotation(@NonNull Class<T> annotationClass) {
      ArgumentUtils.requireNonNull("annotationClass", (T)annotationClass);
      if (this.isRepeatableAnnotation(annotationClass)) {
         List<AnnotationValue<T>> values = this.getDeclaredAnnotationValuesByType(annotationClass);
         return values.isEmpty() ? Optional.empty() : Optional.of(values.get(0));
      } else {
         return this.findDeclaredAnnotation(annotationClass.getName());
      }
   }

   default <T> Optional<T> getValue(@NonNull String annotation, @NonNull String member, @NonNull Class<T> requiredType) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      ArgumentUtils.requireNonNull("member", (T)member);
      ArgumentUtils.requireNonNull("requiredType", (T)requiredType);
      return this.getValue(annotation, member, Argument.of(requiredType));
   }

   default <T> Optional<T> getValue(@NonNull String annotation, @NonNull String member, @NonNull Argument<T> requiredType) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      ArgumentUtils.requireNonNull("member", (T)member);
      ArgumentUtils.requireNonNull("requiredType", requiredType);
      Optional<T> value = this.findAnnotation(annotation).flatMap(av -> av.get(member, requiredType));
      return !value.isPresent() && this.hasStereotype(annotation) ? this.getDefaultValue(annotation, member, requiredType) : value;
   }

   default OptionalLong longValue(@NonNull String annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      Optional<Long> result = this.getValue(annotation, member, Long.class);
      return (OptionalLong)result.map(OptionalLong::of).orElseGet(OptionalLong::empty);
   }

   default OptionalLong longValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      return this.longValue(annotation.getName(), member);
   }

   default <E extends Enum> Optional<E> enumValue(@NonNull String annotation, Class<E> enumType) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      return this.enumValue(annotation, "value", enumType);
   }

   default <E extends Enum> Optional<E> enumValue(@NonNull String annotation, @NonNull String member, Class<E> enumType) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return this.getValue(annotation, member, enumType);
   }

   default <E extends Enum> Optional<E> enumValue(@NonNull Class<? extends Annotation> annotation, Class<E> enumType) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      return this.enumValue(annotation, "value", enumType);
   }

   default <E extends Enum> Optional<E> enumValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, Class<E> enumType) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return this.enumValue(annotation.getName(), member, enumType);
   }

   default <E extends Enum> E[] enumValues(@NonNull String annotation, Class<E> enumType) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      return (E[])this.enumValues(annotation, "value", enumType);
   }

   default <E extends Enum> E[] enumValues(@NonNull String annotation, @NonNull String member, Class<E> enumType) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return (E[])((Enum[])Array.newInstance(enumType, 0));
   }

   default <E extends Enum> E[] enumValues(@NonNull Class<? extends Annotation> annotation, Class<E> enumType) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      return (E[])this.enumValues(annotation, "value", enumType);
   }

   default <E extends Enum> E[] enumValues(@NonNull Class<? extends Annotation> annotation, @NonNull String member, Class<E> enumType) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return (E[])this.enumValues(annotation.getName(), member, enumType);
   }

   @NonNull
   default <T> Class<T>[] classValues(@NonNull String annotation) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      return this.classValues(annotation, "value");
   }

   @NonNull
   default <T> Class<T>[] classValues(@NonNull String annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      ArgumentUtils.requireNonNull("member", (T)member);
      return (Class<T>[])this.getValue(annotation, member, Class[].class).orElse(ReflectionUtils.EMPTY_CLASS_ARRAY);
   }

   @NonNull
   default <T> Class<T>[] classValues(@NonNull Class<? extends Annotation> annotation) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      return this.classValues(annotation, "value");
   }

   @NonNull
   default <T> Class<T>[] classValues(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      ArgumentUtils.requireNonNull("member", (T)member);
      return this.classValues(annotation.getName(), member);
   }

   default Optional<Class> classValue(@NonNull String annotation) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      return this.classValue(annotation, "value");
   }

   default Optional<Class> classValue(@NonNull String annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return this.getValue(annotation, member, Class.class);
   }

   default Optional<Class> classValue(@NonNull Class<? extends Annotation> annotation) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      return this.classValue(annotation, "value");
   }

   default Optional<Class> classValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return this.classValue(annotation.getName(), member);
   }

   default OptionalInt intValue(@NonNull String annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      Optional<Integer> result = this.getValue(annotation, member, Integer.class);
      return (OptionalInt)result.map(OptionalInt::of).orElseGet(OptionalInt::empty);
   }

   default OptionalInt intValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      return this.intValue(annotation.getName(), member);
   }

   default OptionalInt intValue(@NonNull Class<? extends Annotation> annotation) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      return this.intValue(annotation, "value");
   }

   default Optional<String> stringValue(@NonNull String annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return this.getValue(annotation, member, String.class);
   }

   default Optional<String> stringValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      return this.stringValue(annotation.getName(), member);
   }

   @NonNull
   default Optional<String> stringValue(@NonNull Class<? extends Annotation> annotation) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      return this.stringValue(annotation, "value");
   }

   @NonNull
   default Optional<String> stringValue(@NonNull String annotation) {
      return this.stringValue(annotation, "value");
   }

   default Optional<Boolean> booleanValue(@NonNull String annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return this.getValue(annotation, member, Boolean.class);
   }

   default Optional<Boolean> booleanValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      return this.booleanValue(annotation.getName(), member);
   }

   @NonNull
   default Optional<Boolean> booleanValue(@NonNull Class<? extends Annotation> annotation) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      return this.booleanValue(annotation, "value");
   }

   @NonNull
   default Optional<Boolean> booleanValue(@NonNull String annotation) {
      return this.booleanValue(annotation, "value");
   }

   @NonNull
   default String[] stringValues(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return StringUtils.EMPTY_STRING_ARRAY;
   }

   @NonNull
   default String[] stringValues(@NonNull Class<? extends Annotation> annotation) {
      return this.stringValues(annotation, "value");
   }

   @NonNull
   default String[] stringValues(@NonNull String annotation, @NonNull String member) {
      return StringUtils.EMPTY_STRING_ARRAY;
   }

   @NonNull
   default String[] stringValues(@NonNull String annotation) {
      return this.stringValues(annotation, "value");
   }

   @NonNull
   default OptionalDouble doubleValue(@NonNull String annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      Optional<Double> result = this.getValue(annotation, member, Double.class);
      return (OptionalDouble)result.map(OptionalDouble::of).orElseGet(OptionalDouble::empty);
   }

   @NonNull
   default OptionalDouble doubleValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      return this.doubleValue(annotation.getName(), member);
   }

   @NonNull
   default OptionalDouble doubleValue(@NonNull Class<? extends Annotation> annotation) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      return this.doubleValue(annotation, "value");
   }

   @NonNull
   default <T> Optional<T> getValue(@NonNull String annotation, @NonNull Class<T> requiredType) {
      return this.getValue(annotation, "value", requiredType);
   }

   @NonNull
   default Optional<Object> getValue(@NonNull String annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return this.getValue(annotation, member, Object.class);
   }

   @NonNull
   default Optional<Object> getValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return this.getValue(annotation, member, Object.class);
   }

   default boolean isTrue(@NonNull String annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return this.getValue(annotation, member, Boolean.class).orElse(false);
   }

   default boolean isTrue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return this.getValue(annotation.getName(), member, Boolean.class).orElse(false);
   }

   default boolean isPresent(@NonNull String annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return this.findAnnotation(annotation).map(av -> av.contains(member)).orElse(false);
   }

   default boolean isPresent(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return this.isPresent(annotation.getName(), member);
   }

   default boolean isFalse(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return !this.isTrue(annotation, member);
   }

   default boolean isFalse(@NonNull String annotation, @NonNull String member) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      ArgumentUtils.requireNonNull("member", member);
      return !this.isTrue(annotation, member);
   }

   @NonNull
   default Optional<Object> getValue(@NonNull String annotation) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      return this.getValue(annotation, Object.class);
   }

   @NonNull
   default Optional<Object> getValue(@NonNull Class<? extends Annotation> annotation) {
      ArgumentUtils.requireNonNull("annotation", annotation);
      return this.getValue(annotation, "value", Object.class);
   }

   @NonNull
   default <T> Optional<T> getValue(@NonNull Class<? extends Annotation> annotation, @NonNull Class<T> requiredType) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      ArgumentUtils.requireNonNull("requiredType", (T)requiredType);
      return this.getValue(annotation, "value", requiredType);
   }

   @NonNull
   default <T> Optional<T> getValue(@NonNull Class<? extends Annotation> annotation, @NonNull Argument<T> requiredType) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      ArgumentUtils.requireNonNull("requiredType", requiredType);
      return this.getValue(annotation, "value", requiredType);
   }

   @NonNull
   default <T> Optional<T> getValue(@NonNull String annotation, @NonNull Argument<T> requiredType) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      ArgumentUtils.requireNonNull("requiredType", requiredType);
      return this.getValue(annotation, "value", requiredType);
   }

   default boolean hasAnnotation(@Nullable Class<? extends Annotation> annotation) {
      return annotation != null
         ? this.findRepeatableAnnotation(annotation).map(this::hasAnnotation).orElseGet(() -> this.hasAnnotation(annotation.getName()))
         : false;
   }

   default boolean hasStereotype(@Nullable Class<? extends Annotation> annotation) {
      return annotation != null
         ? this.findRepeatableAnnotation(annotation).map(this::hasStereotype).orElseGet(() -> this.hasStereotype(annotation.getName()))
         : false;
   }

   default boolean hasStereotype(@Nullable Class<? extends Annotation>... annotations) {
      if (ArrayUtils.isEmpty(annotations)) {
         return false;
      } else {
         for(Class<? extends Annotation> annotation : annotations) {
            if (this.hasStereotype(annotation)) {
               return true;
            }
         }

         return false;
      }
   }

   default boolean hasStereotype(@Nullable String[] annotations) {
      if (ArrayUtils.isEmpty(annotations)) {
         return false;
      } else {
         for(String annotation : annotations) {
            if (this.hasStereotype(annotation)) {
               return true;
            }
         }

         return false;
      }
   }

   default boolean hasDeclaredAnnotation(@Nullable Class<? extends Annotation> annotation) {
      return annotation != null
         ? this.findRepeatableAnnotation(annotation).map(this::hasDeclaredAnnotation).orElseGet(() -> this.hasDeclaredAnnotation(annotation.getName()))
         : false;
   }

   default boolean hasDeclaredStereotype(@Nullable Class<? extends Annotation> stereotype) {
      return stereotype != null
         ? this.findRepeatableAnnotation(stereotype).map(this::hasDeclaredStereotype).orElseGet(() -> this.hasDeclaredStereotype(stereotype.getName()))
         : false;
   }

   default boolean hasDeclaredStereotype(@Nullable Class<? extends Annotation>... annotations) {
      if (ArrayUtils.isEmpty(annotations)) {
         return false;
      } else {
         for(Class<? extends Annotation> annotation : annotations) {
            if (this.hasDeclaredStereotype(annotation)) {
               return true;
            }
         }

         return false;
      }
   }

   default boolean isRepeatableAnnotation(@NonNull Class<? extends Annotation> annotation) {
      return annotation.getAnnotation(Repeatable.class) != null;
   }

   default boolean isRepeatableAnnotation(@NonNull String annotation) {
      return false;
   }

   default Optional<String> findRepeatableAnnotation(@NonNull Class<? extends Annotation> annotation) {
      return Optional.ofNullable(annotation.getAnnotation(Repeatable.class)).map(repeatable -> repeatable.value().getName());
   }

   default Optional<String> findRepeatableAnnotation(@NonNull String annotation) {
      return Optional.empty();
   }

   default boolean isEmpty() {
      return this == EMPTY_METADATA;
   }
}
