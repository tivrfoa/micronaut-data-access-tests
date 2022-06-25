package io.micronaut.inject.annotation;

import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationUtil;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.value.OptionalValues;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

public final class AnnotationMetadataHierarchy implements AnnotationMetadata, EnvironmentAnnotationMetadata, Iterable<AnnotationMetadata> {
   public static final AnnotationMetadata[] EMPTY_HIERARCHY = new AnnotationMetadata[]{AnnotationMetadata.EMPTY_METADATA, AnnotationMetadata.EMPTY_METADATA};
   private final AnnotationMetadata[] hierarchy;

   public AnnotationMetadataHierarchy(AnnotationMetadata... hierarchy) {
      if (ArrayUtils.isNotEmpty(hierarchy)) {
         int len = hierarchy.length;
         if (len > 1) {
            for(int i = 0; i < len / 2; ++i) {
               AnnotationMetadata temp = hierarchy[i];
               int pos = len - i - 1;
               hierarchy[i] = hierarchy[pos];
               hierarchy[pos] = temp;
            }
         }

         this.hierarchy = hierarchy;
      } else {
         this.hierarchy = EMPTY_HIERARCHY;
      }

   }

   private AnnotationMetadataHierarchy(AnnotationMetadata[] existing, AnnotationMetadata newChild) {
      this.hierarchy = new AnnotationMetadata[existing.length];
      System.arraycopy(existing, 0, this.hierarchy, 0, existing.length);
      this.hierarchy[0] = newChild;
   }

   @Override
   public boolean hasPropertyExpressions() {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         if (annotationMetadata.hasPropertyExpressions()) {
            return true;
         }
      }

      return false;
   }

   @Override
   public Optional<Class<? extends Annotation>> getAnnotationType(@NonNull String name) {
      return this.getAnnotationType((Function<AnnotationMetadata, Optional<Class<? extends Annotation>>>)(metadata -> metadata.getAnnotationType(name)));
   }

   @Override
   public Optional<Class<? extends Annotation>> getAnnotationType(@NonNull String name, @NonNull ClassLoader classLoader) {
      return this.getAnnotationType(
         (Function<AnnotationMetadata, Optional<Class<? extends Annotation>>>)(metadata -> metadata.getAnnotationType(name, classLoader))
      );
   }

   @NonNull
   @Override
   public AnnotationMetadata getDeclaredMetadata() {
      return this.hierarchy[0];
   }

   @NonNull
   public AnnotationMetadata getRootMetadata() {
      return this.hierarchy[this.hierarchy.length - 1];
   }

   @NonNull
   public AnnotationMetadata createSibling(@NonNull AnnotationMetadata child) {
      return (AnnotationMetadata)(this.hierarchy.length > 1 ? new AnnotationMetadataHierarchy(this.hierarchy, child) : child);
   }

   @Nullable
   @Override
   public <T extends Annotation> T synthesize(@NonNull Class<T> annotationClass, @NonNull String sourceAnnotation) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         T a = annotationMetadata.synthesize(annotationClass, sourceAnnotation);
         if (a != null) {
            return a;
         }
      }

      return null;
   }

   @Override
   public Annotation[] synthesizeAll() {
      return (Annotation[])Stream.of(this.hierarchy).flatMap(am -> Arrays.stream(am.synthesizeAll())).toArray(x$0 -> new Annotation[x$0]);
   }

   @Override
   public Annotation[] synthesizeDeclared() {
      return (Annotation[])Stream.of(this.hierarchy).flatMap(am -> Arrays.stream(am.synthesizeDeclared())).toArray(x$0 -> new Annotation[x$0]);
   }

   @Override
   public <T extends Annotation> T[] synthesizeAnnotationsByType(Class<T> annotationClass) {
      return (T[])(annotationClass == null
         ? AnnotationUtil.ZERO_ANNOTATIONS
         : Stream.of(this.hierarchy)
            .flatMap(am -> am.getAnnotationValuesByType(annotationClass).stream())
            .distinct()
            .map(entries -> AnnotationMetadataSupport.buildAnnotation(annotationClass, entries))
            .toArray(value -> (Annotation[])Array.newInstance(annotationClass, value)));
   }

   @Override
   public <T extends Annotation> T[] synthesizeDeclaredAnnotationsByType(Class<T> annotationClass) {
      return (T[])(annotationClass == null
         ? AnnotationUtil.ZERO_ANNOTATIONS
         : Stream.of(this.hierarchy)
            .flatMap(am -> am.getAnnotationValuesByType(annotationClass).stream())
            .distinct()
            .map(entries -> AnnotationMetadataSupport.buildAnnotation(annotationClass, entries))
            .toArray(value -> (Annotation[])Array.newInstance(annotationClass, value)));
   }

   @Nullable
   @Override
   public <T extends Annotation> T synthesizeDeclared(@NonNull Class<T> annotationClass, @NonNull String sourceAnnotation) {
      return this.hierarchy[0].synthesize(annotationClass, sourceAnnotation);
   }

   @Nullable
   @Override
   public <T extends Annotation> T synthesize(@NonNull Class<T> annotationClass) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         T a = annotationMetadata.synthesize(annotationClass);
         if (a != null) {
            return a;
         }
      }

      return null;
   }

   @Nullable
   @Override
   public <T extends Annotation> T synthesizeDeclared(@NonNull Class<T> annotationClass) {
      return this.hierarchy[0].synthesize(annotationClass);
   }

   @NonNull
   @Override
   public <T extends Annotation> Optional<AnnotationValue<T>> findAnnotation(@NonNull String annotation) {
      AnnotationValue<T> ann = null;

      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         AnnotationValue<T> av = annotationMetadata.getAnnotation(annotation);
         if (av != null) {
            if (ann == null) {
               ann = av;
            } else {
               Map<CharSequence, Object> values = av.getValues();
               Map<CharSequence, Object> existing = ann.getValues();
               Map<CharSequence, Object> newValues = new LinkedHashMap(values.size() + existing.size());
               newValues.putAll(existing);

               for(Entry<CharSequence, Object> entry : values.entrySet()) {
                  newValues.putIfAbsent(entry.getKey(), entry.getValue());
               }

               ann = new AnnotationValue<>(annotation, newValues, AnnotationMetadataSupport.getDefaultValues(annotation));
            }
         }
      }

      return Optional.ofNullable(ann);
   }

   @NonNull
   @Override
   public <T extends Annotation> Optional<AnnotationValue<T>> findDeclaredAnnotation(@NonNull String annotation) {
      return this.hierarchy[0].findDeclaredAnnotation(annotation);
   }

   @NonNull
   @Override
   public OptionalDouble doubleValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         OptionalDouble o = annotationMetadata.doubleValue(annotation, member);
         if (o.isPresent()) {
            return o;
         }
      }

      return OptionalDouble.empty();
   }

   @NonNull
   @Override
   public String[] stringValues(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.stringValues(annotation.getName(), member);
   }

   @NonNull
   @Override
   public String[] stringValues(@NonNull String annotation, @NonNull String member) {
      String[] values = this.hierarchy[0].stringValues(annotation, member);

      for(int i = 1; i < this.hierarchy.length; ++i) {
         AnnotationMetadata annotationMetadata = this.hierarchy[i];
         String[] moreValues = annotationMetadata.stringValues(annotation, member);
         if (ArrayUtils.isNotEmpty(moreValues)) {
            values = ArrayUtils.concat((String[])values, (String[])moreValues);
         }
      }

      return values;
   }

   @Override
   public Optional<Boolean> booleanValue(@NonNull String annotation, @NonNull String member) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         Optional<Boolean> o = annotationMetadata.booleanValue(annotation, member);
         if (o.isPresent()) {
            return o;
         }
      }

      return Optional.empty();
   }

   @Override
   public boolean isTrue(@NonNull String annotation, @NonNull String member) {
      for(AnnotationMetadata am : this.hierarchy) {
         if (am.isTrue(annotation, member)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public OptionalLong longValue(@NonNull String annotation, @NonNull String member) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         OptionalLong o = annotationMetadata.longValue(annotation, member);
         if (o.isPresent()) {
            return o;
         }
      }

      return OptionalLong.empty();
   }

   @Override
   public Optional<String> stringValue(@NonNull String annotation, @NonNull String member) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         Optional<String> o = annotationMetadata.stringValue(annotation, member);
         if (o.isPresent()) {
            return o;
         }
      }

      return Optional.empty();
   }

   @Override
   public OptionalInt intValue(@NonNull String annotation, @NonNull String member) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         OptionalInt o = annotationMetadata.intValue(annotation, member);
         if (o.isPresent()) {
            return o;
         }
      }

      return OptionalInt.empty();
   }

   @NonNull
   @Override
   public OptionalDouble doubleValue(@NonNull String annotation, @NonNull String member) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         OptionalDouble o = annotationMetadata.doubleValue(annotation, member);
         if (o.isPresent()) {
            return o;
         }
      }

      return OptionalDouble.empty();
   }

   @Override
   public <E extends Enum> Optional<E> enumValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, Class<E> enumType) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         Optional<E> o = annotationMetadata.enumValue(annotation, member, enumType);
         if (o.isPresent()) {
            return o;
         }
      }

      return Optional.empty();
   }

   @NonNull
   @Override
   public <T> Class<T>[] classValues(@NonNull String annotation, @NonNull String member) {
      List<Class<T>> list = new ArrayList();

      for(AnnotationMetadata am : this.hierarchy) {
         list.addAll(Arrays.asList(am.classValues(annotation, member)));
      }

      return ArrayUtils.toArray(list, x$0 -> new Class[x$0]);
   }

   @Override
   public Optional<Class> classValue(@NonNull String annotation, @NonNull String member) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         Optional<Class> o = annotationMetadata.classValue(annotation, member);
         if (o.isPresent()) {
            return o;
         }
      }

      return Optional.empty();
   }

   @NonNull
   @Override
   public List<String> getAnnotationNamesByStereotype(@Nullable String stereotype) {
      List<String> list = new ArrayList();

      for(AnnotationMetadata am : this.hierarchy) {
         list.addAll(am.getAnnotationNamesByStereotype(stereotype));
      }

      return list;
   }

   @Override
   public <T extends Annotation> List<AnnotationValue<T>> getAnnotationValuesByStereotype(String stereotype) {
      List<AnnotationValue<T>> list = new ArrayList();

      for(AnnotationMetadata am : this.hierarchy) {
         list.addAll(am.getAnnotationValuesByStereotype(stereotype));
      }

      return list;
   }

   @NonNull
   @Override
   public Set<String> getDeclaredAnnotationNames() {
      return this.hierarchy[0].getDeclaredAnnotationNames();
   }

   @NonNull
   @Override
   public Set<String> getAnnotationNames() {
      Set<String> set = new HashSet();

      for(AnnotationMetadata am : this.hierarchy) {
         set.addAll(am.getAnnotationNames());
      }

      return set;
   }

   @NonNull
   @Override
   public <T> OptionalValues<T> getValues(@NonNull String annotation, @NonNull Class<T> valueType) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         OptionalValues<T> values = annotationMetadata.getValues(annotation, valueType);
         if (!values.isEmpty()) {
            return values;
         }
      }

      return OptionalValues.empty();
   }

   @Override
   public <T> Optional<T> getDefaultValue(@NonNull String annotation, @NonNull String member, @NonNull Argument<T> requiredType) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         Optional<T> defaultValue = annotationMetadata.getDefaultValue(annotation, member, requiredType);
         if (defaultValue.isPresent()) {
            return defaultValue;
         }
      }

      return Optional.empty();
   }

   @NonNull
   @Override
   public <T extends Annotation> List<AnnotationValue<T>> getAnnotationValuesByType(@NonNull Class<T> annotationType) {
      List<AnnotationValue<T>> list = new ArrayList(10);
      Set<AnnotationValue<T>> uniqueValues = new HashSet(10);

      for(AnnotationMetadata am : this.hierarchy) {
         for(AnnotationValue<T> tAnnotationValue : am.getAnnotationValuesByType(annotationType)) {
            if (uniqueValues.add(tAnnotationValue)) {
               list.add(tAnnotationValue);
            }
         }
      }

      return list;
   }

   @Override
   public <T extends Annotation> List<AnnotationValue<T>> getAnnotationValuesByName(String annotationType) {
      if (annotationType == null) {
         return Collections.emptyList();
      } else {
         List<AnnotationValue<T>> list = new ArrayList(10);
         Set<AnnotationValue<T>> uniqueValues = new HashSet(10);

         for(AnnotationMetadata am : this.hierarchy) {
            for(AnnotationValue<T> tAnnotationValue : am.getAnnotationValuesByName(annotationType)) {
               if (uniqueValues.add(tAnnotationValue)) {
                  list.add(tAnnotationValue);
               }
            }
         }

         return Collections.unmodifiableList(list);
      }
   }

   @NonNull
   @Override
   public <T extends Annotation> List<AnnotationValue<T>> getDeclaredAnnotationValuesByType(@NonNull Class<T> annotationType) {
      return this.hierarchy[0].getDeclaredAnnotationValuesByType(annotationType);
   }

   @Override
   public <T extends Annotation> List<AnnotationValue<T>> getDeclaredAnnotationValuesByName(String annotationType) {
      return this.hierarchy[0].getDeclaredAnnotationValuesByName(annotationType);
   }

   @Override
   public boolean hasDeclaredAnnotation(@Nullable String annotation) {
      return this.hierarchy[0].hasDeclaredAnnotation(annotation);
   }

   @Override
   public boolean hasAnnotation(@Nullable String annotation) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         if (annotationMetadata.hasAnnotation(annotation)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean hasStereotype(@Nullable String annotation) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         if (annotationMetadata.hasStereotype(annotation)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean hasDeclaredStereotype(@Nullable String annotation) {
      return this.hierarchy[0].hasDeclaredStereotype(annotation);
   }

   @Override
   public <E extends Enum> Optional<E> enumValue(String annotation, String member, Class<E> enumType) {
      return this.enumValue(annotation, member, enumType, null);
   }

   @Override
   public <E extends Enum> E[] enumValues(String annotation, String member, Class<E> enumType) {
      return (E[])this.enumValues(annotation, member, enumType, null);
   }

   @Override
   public OptionalInt intValue(Class<? extends Annotation> annotation, String member) {
      return this.intValue(annotation, member, null);
   }

   @Override
   public boolean isFalse(Class<? extends Annotation> annotation, String member) {
      return !this.booleanValue(annotation, member, null).orElse(false);
   }

   @NonNull
   @Override
   public Map<String, Object> getDefaultValues(@NonNull String annotation) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         Map<String, Object> defaultValues = annotationMetadata.getDefaultValues(annotation);
         if (!defaultValues.isEmpty()) {
            return defaultValues;
         }
      }

      return Collections.emptyMap();
   }

   @Override
   public <E extends Enum> Optional<E> enumValue(
      @NonNull Class<? extends Annotation> annotation, @NonNull String member, Class<E> enumType, @Nullable Function<Object, Object> valueMapper
   ) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         Optional<E> o;
         if (annotationMetadata instanceof EnvironmentAnnotationMetadata) {
            o = ((EnvironmentAnnotationMetadata)annotationMetadata).enumValue(annotation, member, enumType, valueMapper);
         } else {
            o = annotationMetadata.enumValue(annotation, member, enumType);
         }

         if (o.isPresent()) {
            return o;
         }
      }

      return Optional.empty();
   }

   @Override
   public <E extends Enum> Optional<E> enumValue(
      @NonNull String annotation, @NonNull String member, Class<E> enumType, @Nullable Function<Object, Object> valueMapper
   ) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         Optional<E> o;
         if (annotationMetadata instanceof EnvironmentAnnotationMetadata) {
            o = ((EnvironmentAnnotationMetadata)annotationMetadata).enumValue(annotation, member, enumType, valueMapper);
         } else {
            o = annotationMetadata.enumValue(annotation, member, enumType);
         }

         if (o.isPresent()) {
            return o;
         }
      }

      return Optional.empty();
   }

   @Override
   public <E extends Enum> E[] enumValues(
      @NonNull Class<? extends Annotation> annotation, @NonNull String member, Class<E> enumType, @Nullable Function<Object, Object> valueMapper
   ) {
      E[] values = this.hierarchy[0].enumValues(annotation, member, enumType);

      for(int i = 1; i < this.hierarchy.length; ++i) {
         AnnotationMetadata annotationMetadata = this.hierarchy[i];
         E[] moreValues = annotationMetadata.enumValues(annotation, member, enumType);
         if (ArrayUtils.isNotEmpty(moreValues)) {
            values = (E[])ArrayUtils.concat((Object[])values, (Object[])moreValues);
         }
      }

      return values;
   }

   @Override
   public <E extends Enum> E[] enumValues(@NonNull String annotation, @NonNull String member, Class<E> enumType, @Nullable Function<Object, Object> valueMapper) {
      E[] values = this.hierarchy[0].enumValues(annotation, member, enumType);

      for(int i = 1; i < this.hierarchy.length; ++i) {
         AnnotationMetadata annotationMetadata = this.hierarchy[i];
         E[] moreValues = annotationMetadata.enumValues(annotation, member, enumType);
         if (ArrayUtils.isNotEmpty(moreValues)) {
            values = (E[])ArrayUtils.concat((Object[])values, (Object[])moreValues);
         }
      }

      return values;
   }

   @Override
   public Optional<Class> classValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, Function<Object, Object> valueMapper) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         Optional<Class> o;
         if (annotationMetadata instanceof EnvironmentAnnotationMetadata) {
            o = ((EnvironmentAnnotationMetadata)annotationMetadata).classValue(annotation, member, valueMapper);
         } else {
            o = annotationMetadata.classValue(annotation, member);
         }

         if (o.isPresent()) {
            return o;
         }
      }

      return Optional.empty();
   }

   @Override
   public Optional<Class> classValue(@NonNull String annotation, @NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         Optional<Class> o;
         if (annotationMetadata instanceof EnvironmentAnnotationMetadata) {
            o = ((EnvironmentAnnotationMetadata)annotationMetadata).classValue(annotation, member, valueMapper);
         } else {
            o = annotationMetadata.classValue(annotation, member);
         }

         if (o.isPresent()) {
            return o;
         }
      }

      return Optional.empty();
   }

   @Override
   public OptionalInt intValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         OptionalInt o;
         if (annotationMetadata instanceof EnvironmentAnnotationMetadata) {
            o = ((EnvironmentAnnotationMetadata)annotationMetadata).intValue(annotation, member, valueMapper);
         } else {
            o = annotationMetadata.intValue(annotation, member);
         }

         if (o.isPresent()) {
            return o;
         }
      }

      return OptionalInt.empty();
   }

   @Override
   public Optional<Boolean> booleanValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, Function<Object, Object> valueMapper) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         Optional<Boolean> o;
         if (annotationMetadata instanceof EnvironmentAnnotationMetadata) {
            o = ((EnvironmentAnnotationMetadata)annotationMetadata).booleanValue(annotation, member, valueMapper);
         } else {
            o = annotationMetadata.booleanValue(annotation, member);
         }

         if (o.isPresent()) {
            return o;
         }
      }

      return Optional.empty();
   }

   @NonNull
   @Override
   public Optional<Boolean> booleanValue(@NonNull String annotation, @NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         Optional<Boolean> o;
         if (annotationMetadata instanceof EnvironmentAnnotationMetadata) {
            o = ((EnvironmentAnnotationMetadata)annotationMetadata).booleanValue(annotation, member, valueMapper);
         } else {
            o = annotationMetadata.booleanValue(annotation, member);
         }

         if (o.isPresent()) {
            return o;
         }
      }

      return Optional.empty();
   }

   @Override
   public OptionalLong longValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         OptionalLong o;
         if (annotationMetadata instanceof EnvironmentAnnotationMetadata) {
            o = ((EnvironmentAnnotationMetadata)annotationMetadata).longValue(annotation, member, valueMapper);
         } else {
            o = annotationMetadata.longValue(annotation, member);
         }

         if (o.isPresent()) {
            return o;
         }
      }

      return OptionalLong.empty();
   }

   @NonNull
   @Override
   public OptionalLong longValue(@NonNull String annotation, @NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         OptionalLong o;
         if (annotationMetadata instanceof EnvironmentAnnotationMetadata) {
            o = ((EnvironmentAnnotationMetadata)annotationMetadata).longValue(annotation, member, valueMapper);
         } else {
            o = annotationMetadata.longValue(annotation, member);
         }

         if (o.isPresent()) {
            return o;
         }
      }

      return OptionalLong.empty();
   }

   @NonNull
   @Override
   public OptionalInt intValue(@NonNull String annotation, @NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         OptionalInt o;
         if (annotationMetadata instanceof EnvironmentAnnotationMetadata) {
            o = ((EnvironmentAnnotationMetadata)annotationMetadata).intValue(annotation, member, valueMapper);
         } else {
            o = annotationMetadata.intValue(annotation, member);
         }

         if (o.isPresent()) {
            return o;
         }
      }

      return OptionalInt.empty();
   }

   @Override
   public OptionalLong longValue(Class<? extends Annotation> annotation, String member) {
      return this.longValue(annotation, member, null);
   }

   @Override
   public <E extends Enum> E[] enumValues(Class<? extends Annotation> annotation, String member, Class<E> enumType) {
      return (E[])this.enumValues(annotation, member, enumType, null);
   }

   @Override
   public <T> Class<T>[] classValues(Class<? extends Annotation> annotation, String member) {
      List<Class<T>> list = new ArrayList();

      for(AnnotationMetadata am : this.hierarchy) {
         list.addAll(Arrays.asList(am.classValues(annotation, member)));
      }

      return ArrayUtils.toArray(list, x$0 -> new Class[x$0]);
   }

   @Override
   public Optional<Class> classValue(Class<? extends Annotation> annotation, String member) {
      return this.classValue(annotation, member, null);
   }

   @Override
   public Optional<String> stringValue(Class<? extends Annotation> annotation, String member) {
      return this.stringValue(annotation, member, null);
   }

   @Override
   public Optional<Boolean> booleanValue(Class<? extends Annotation> annotation, String member) {
      return this.booleanValue(annotation, member, null);
   }

   @Override
   public boolean isTrue(Class<? extends Annotation> annotation, String member) {
      return this.isTrue(annotation, member, null);
   }

   @Override
   public boolean isPresent(Class<? extends Annotation> annotation, String member) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         if (annotationMetadata.isPresent(annotation, member)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public Optional<String> stringValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, Function<Object, Object> valueMapper) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         Optional<String> o;
         if (annotationMetadata instanceof EnvironmentAnnotationMetadata) {
            o = ((EnvironmentAnnotationMetadata)annotationMetadata).stringValue(annotation, member, valueMapper);
         } else {
            o = annotationMetadata.stringValue(annotation, member);
         }

         if (o.isPresent()) {
            return o;
         }
      }

      return Optional.empty();
   }

   @NonNull
   @Override
   public String[] stringValues(@NonNull Class<? extends Annotation> annotation, @NonNull String member, Function<Object, Object> valueMapper) {
      List<String> strings = new ArrayList();

      for(AnnotationMetadata am : this.hierarchy) {
         if (am instanceof EnvironmentAnnotationMetadata) {
            strings.addAll(Arrays.asList(((EnvironmentAnnotationMetadata)am).stringValues(annotation, member, valueMapper)));
         } else {
            strings.addAll(Arrays.asList(am.stringValues(annotation, member)));
         }
      }

      return ArrayUtils.toArray(strings, x$0 -> new String[x$0]);
   }

   @Override
   public String[] stringValues(String annotation, String member, Function<Object, Object> valueMapper) {
      List<String> strings = new ArrayList();

      for(AnnotationMetadata am : this.hierarchy) {
         if (am instanceof EnvironmentAnnotationMetadata) {
            strings.addAll(Arrays.asList(((EnvironmentAnnotationMetadata)am).stringValues(annotation, member, valueMapper)));
         } else {
            strings.addAll(Arrays.asList(am.stringValues(annotation, member)));
         }
      }

      return ArrayUtils.toArray(strings, x$0 -> new String[x$0]);
   }

   @NonNull
   @Override
   public Optional<String> stringValue(@NonNull String annotation, @NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         Optional<String> o;
         if (annotationMetadata instanceof EnvironmentAnnotationMetadata) {
            o = ((EnvironmentAnnotationMetadata)annotationMetadata).stringValue(annotation, member, valueMapper);
         } else {
            o = annotationMetadata.stringValue(annotation, member);
         }

         if (o.isPresent()) {
            return o;
         }
      }

      return Optional.empty();
   }

   @Override
   public boolean isTrue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, Function<Object, Object> valueMapper) {
      return this.booleanValue(annotation, member, valueMapper).orElse(false);
   }

   @Override
   public boolean isTrue(@NonNull String annotation, @NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      return this.booleanValue(annotation, member, valueMapper).orElse(false);
   }

   @Override
   public OptionalDouble doubleValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, @Nullable Function<Object, Object> valueMapper) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         OptionalDouble o;
         if (annotationMetadata instanceof EnvironmentAnnotationMetadata) {
            o = ((EnvironmentAnnotationMetadata)annotationMetadata).doubleValue(annotation, member, valueMapper);
         } else {
            o = annotationMetadata.doubleValue(annotation, member);
         }

         if (o.isPresent()) {
            return o;
         }
      }

      return OptionalDouble.empty();
   }

   @NonNull
   @Override
   public OptionalDouble doubleValue(@NonNull String annotation, @NonNull String member, Function<Object, Object> valueMapper) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         OptionalDouble o;
         if (annotationMetadata instanceof EnvironmentAnnotationMetadata) {
            o = ((EnvironmentAnnotationMetadata)annotationMetadata).doubleValue(annotation, member, valueMapper);
         } else {
            o = annotationMetadata.doubleValue(annotation, member);
         }

         if (o.isPresent()) {
            return o;
         }
      }

      return OptionalDouble.empty();
   }

   @NonNull
   @Override
   public <T> Optional<T> getValue(
      @NonNull String annotation, @NonNull String member, @NonNull Argument<T> requiredType, @Nullable Function<Object, Object> valueMapper
   ) {
      for(AnnotationMetadata annotationMetadata : this.hierarchy) {
         Optional<T> o;
         if (annotationMetadata instanceof EnvironmentAnnotationMetadata) {
            o = ((EnvironmentAnnotationMetadata)annotationMetadata).getValue(annotation, member, requiredType, valueMapper);
         } else {
            o = annotationMetadata.getValue(annotation, member, requiredType);
         }

         if (o.isPresent()) {
            return o;
         }
      }

      return Optional.empty();
   }

   @NonNull
   public Iterator<AnnotationMetadata> iterator() {
      return ArrayUtils.reverseIterator(this.hierarchy);
   }

   private Optional<Class<? extends Annotation>> getAnnotationType(Function<AnnotationMetadata, Optional<Class<? extends Annotation>>> annotationTypeSupplier) {
      for(AnnotationMetadata metadata : this.hierarchy) {
         Optional<Class<? extends Annotation>> annotationType = (Optional)annotationTypeSupplier.apply(metadata);
         if (annotationType.isPresent()) {
            return annotationType;
         }
      }

      return Optional.empty();
   }

   @Override
   public boolean isEmpty() {
      for(AnnotationMetadata metadata : this.hierarchy) {
         if (!metadata.isEmpty()) {
            return false;
         }
      }

      return true;
   }

   @Override
   public boolean isRepeatableAnnotation(Class<? extends Annotation> annotation) {
      for(AnnotationMetadata metadata : this.hierarchy) {
         if (metadata.isRepeatableAnnotation(annotation)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean isRepeatableAnnotation(String annotation) {
      for(AnnotationMetadata metadata : this.hierarchy) {
         if (metadata.isRepeatableAnnotation(annotation)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public Optional<String> findRepeatableAnnotation(Class<? extends Annotation> annotation) {
      for(AnnotationMetadata metadata : this.hierarchy) {
         Optional<String> repeatable = metadata.findRepeatableAnnotation(annotation);
         if (repeatable.isPresent()) {
            return repeatable;
         }
      }

      return Optional.empty();
   }

   @Override
   public Optional<String> findRepeatableAnnotation(String annotation) {
      for(AnnotationMetadata metadata : this.hierarchy) {
         Optional<String> repeatable = metadata.findRepeatableAnnotation(annotation);
         if (repeatable.isPresent()) {
            return repeatable;
         }
      }

      return Optional.empty();
   }
}
