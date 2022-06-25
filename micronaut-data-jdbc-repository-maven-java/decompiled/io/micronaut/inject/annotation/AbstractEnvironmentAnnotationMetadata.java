package io.micronaut.inject.annotation;

import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertyPlaceholderResolver;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.value.OptionalValues;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Internal
public abstract class AbstractEnvironmentAnnotationMetadata implements AnnotationMetadata {
   private final EnvironmentAnnotationMetadata environmentAnnotationMetadata;

   protected AbstractEnvironmentAnnotationMetadata(AnnotationMetadata targetMetadata) {
      if (targetMetadata instanceof EnvironmentAnnotationMetadata) {
         this.environmentAnnotationMetadata = (EnvironmentAnnotationMetadata)targetMetadata;
      } else {
         this.environmentAnnotationMetadata = new AnnotationMetadataHierarchy(targetMetadata);
      }

   }

   public AnnotationMetadata getAnnotationMetadata() {
      return this.environmentAnnotationMetadata;
   }

   @Nullable
   @Override
   public <T extends Annotation> T synthesize(@NonNull Class<T> annotationClass) {
      return this.environmentAnnotationMetadata.synthesize(annotationClass);
   }

   @Nullable
   @Override
   public <T extends Annotation> T synthesizeDeclared(@NonNull Class<T> annotationClass) {
      return this.environmentAnnotationMetadata.synthesizeDeclared(annotationClass);
   }

   @Override
   public <T> Optional<T> getValue(@NonNull String annotation, @NonNull String member, @NonNull Argument<T> requiredType) {
      Environment environment = this.getEnvironment();
      return environment != null ? this.environmentAnnotationMetadata.getValue(annotation, member, requiredType, o -> {
         PropertyPlaceholderResolver placeholderResolver = environment.getPlaceholderResolver();
         if (o instanceof String) {
            String v = (String)o;
            if (v.contains("${")) {
               return placeholderResolver.resolveRequiredPlaceholders(v);
            }
         } else if (o instanceof String[]) {
            return AnnotationValue.resolveStringArray((String[])o, o1 -> {
               String vx = (String)o1;
               return vx.contains("${") ? placeholderResolver.resolveRequiredPlaceholders(vx) : vx;
            });
         }

         return o;
      }) : this.environmentAnnotationMetadata.getValue(annotation, member, requiredType);
   }

   @Override
   public <T> Class<T>[] classValues(@NonNull String annotation, @NonNull String member) {
      return this.environmentAnnotationMetadata.classValues(annotation, member);
   }

   @Override
   public <T> Class<T>[] classValues(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      return this.environmentAnnotationMetadata.classValues(annotation, member);
   }

   @Override
   public boolean isTrue(@NonNull String annotation, @NonNull String member) {
      Environment environment = this.getEnvironment();
      return environment != null ? this.environmentAnnotationMetadata.isTrue(annotation, member, o -> {
         if (o instanceof String) {
            String v = (String)o;
            if (v.contains("${")) {
               return environment.getPlaceholderResolver().resolveRequiredPlaceholders(v);
            }
         }

         return o;
      }) : this.environmentAnnotationMetadata.isTrue(annotation, member);
   }

   @Override
   public boolean isFalse(@NonNull String annotation, @NonNull String member) {
      Environment environment = this.getEnvironment();
      if (environment != null) {
         return !this.environmentAnnotationMetadata.isTrue(annotation, member, o -> {
            if (o instanceof String) {
               String v = (String)o;
               if (v.contains("${")) {
                  return environment.getPlaceholderResolver().resolveRequiredPlaceholders(v);
               }
            }

            return o;
         });
      } else {
         return !this.environmentAnnotationMetadata.isTrue(annotation, member);
      }
   }

   @NonNull
   @Override
   public Optional<Class<? extends Annotation>> getAnnotationTypeByStereotype(@NonNull Class<? extends Annotation> stereotype) {
      return this.environmentAnnotationMetadata.getAnnotationTypeByStereotype(stereotype);
   }

   @NonNull
   @Override
   public Optional<Class<? extends Annotation>> getAnnotationTypeByStereotype(@Nullable String stereotype) {
      return this.environmentAnnotationMetadata.getAnnotationTypeByStereotype(stereotype);
   }

   @NonNull
   @Override
   public Optional<Class> classValue(@NonNull String annotation, @NonNull String member) {
      Function<Object, Object> valueMapper = this.getEnvironmentValueMapper();
      return this.environmentAnnotationMetadata.classValue(annotation, member, valueMapper);
   }

   @NonNull
   @Override
   public Optional<Class> classValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      Function<Object, Object> valueMapper = this.getEnvironmentValueMapper();
      return this.environmentAnnotationMetadata.classValue(annotation, member, valueMapper);
   }

   @Override
   public <E extends Enum> Optional<E> enumValue(@NonNull String annotation, @NonNull String member, Class<E> enumType) {
      Function<Object, Object> valueMapper = this.getEnvironmentValueMapper();
      return this.environmentAnnotationMetadata.enumValue(annotation, member, enumType, valueMapper);
   }

   @Override
   public <E extends Enum> Optional<E> enumValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member, Class<E> enumType) {
      Function<Object, Object> valueMapper = this.getEnvironmentValueMapper();
      return this.environmentAnnotationMetadata.enumValue(annotation, member, enumType, valueMapper);
   }

   @Override
   public <E extends Enum> E[] enumValues(@NonNull String annotation, @NonNull String member, Class<E> enumType) {
      Function<Object, Object> valueMapper = this.getEnvironmentValueMapper();
      return (E[])this.environmentAnnotationMetadata.enumValues(annotation, member, enumType, valueMapper);
   }

   @Override
   public Optional<Boolean> booleanValue(@NonNull String annotation, @NonNull String member) {
      Function<Object, Object> valueMapper = this.getEnvironmentValueMapper();
      return this.environmentAnnotationMetadata.booleanValue(annotation, member, valueMapper);
   }

   @Override
   public Optional<Boolean> booleanValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      Function<Object, Object> valueMapper = this.getEnvironmentValueMapper();
      return this.environmentAnnotationMetadata.booleanValue(annotation, member, valueMapper);
   }

   @NonNull
   @Override
   public Optional<String> stringValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      Function<Object, Object> valueMapper = this.getEnvironmentValueMapper();
      return this.environmentAnnotationMetadata.stringValue(annotation, member, valueMapper);
   }

   @NonNull
   @Override
   public String[] stringValues(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      Environment environment = this.getEnvironment();
      if (environment != null) {
         PropertyPlaceholderResolver resolver = environment.getPlaceholderResolver();
         Function<Object, Object> valueMapper = val -> {
            String[] values;
            if (val instanceof CharSequence) {
               values = new String[]{val.toString()};
            } else {
               if (!(val instanceof String[])) {
                  return null;
               }

               values = (String[])val;
            }

            String[] resolvedValues = (String[])Arrays.copyOf(values, values.length);
            boolean expandValues = false;

            for(int i = 0; i < values.length; ++i) {
               String value = values[i];
               if (value != null && value.contains(resolver.getPrefix())) {
                  value = resolver.resolveRequiredPlaceholders(value);
                  if (value.contains(",")) {
                     expandValues = true;
                  }
               }

               resolvedValues[i] = value;
            }

            return expandValues
               ? Stream.of(resolvedValues)
                  .flatMap(s -> s.contains(",") ? Arrays.stream(resolver.resolveRequiredPlaceholder(s, String[].class)) : Stream.of(s))
                  .toArray(x$0 -> new String[x$0])
               : resolvedValues;
         };
         return this.environmentAnnotationMetadata.stringValues(annotation, member, valueMapper);
      } else {
         return this.environmentAnnotationMetadata.stringValues(annotation, member, null);
      }
   }

   @NonNull
   @Override
   public Optional<String> stringValue(@NonNull String annotation, @NonNull String member) {
      Function<Object, Object> valueMapper = this.getEnvironmentValueMapper();
      return this.environmentAnnotationMetadata.stringValue(annotation, member, valueMapper);
   }

   @Override
   public OptionalLong longValue(@NonNull String annotation, @NonNull String member) {
      Function<Object, Object> valueMapper = this.getEnvironmentValueMapper();
      return this.environmentAnnotationMetadata.longValue(annotation, member, valueMapper);
   }

   @Override
   public OptionalLong longValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      Function<Object, Object> valueMapper = this.getEnvironmentValueMapper();
      return this.environmentAnnotationMetadata.longValue(annotation, member, valueMapper);
   }

   @NonNull
   @Override
   public OptionalInt intValue(@NonNull String annotation, @NonNull String member) {
      Function<Object, Object> valueMapper = this.getEnvironmentValueMapper();
      return this.environmentAnnotationMetadata.intValue(annotation, member, valueMapper);
   }

   @NonNull
   @Override
   public OptionalInt intValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      Function<Object, Object> valueMapper = this.getEnvironmentValueMapper();
      return this.environmentAnnotationMetadata.intValue(annotation, member, valueMapper);
   }

   @NonNull
   @Override
   public OptionalDouble doubleValue(@NonNull String annotation, @NonNull String member) {
      Function<Object, Object> valueMapper = this.getEnvironmentValueMapper();
      return this.environmentAnnotationMetadata.doubleValue(annotation, member, valueMapper);
   }

   @NonNull
   @Override
   public OptionalDouble doubleValue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      Function<Object, Object> valueMapper = this.getEnvironmentValueMapper();
      return this.environmentAnnotationMetadata.doubleValue(annotation, member, valueMapper);
   }

   @Override
   public boolean isTrue(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      Function<Object, Object> valueMapper = this.getEnvironmentValueMapper();
      return this.environmentAnnotationMetadata.isTrue(annotation, member, valueMapper);
   }

   @Override
   public boolean isFalse(@NonNull Class<? extends Annotation> annotation, @NonNull String member) {
      Function<Object, Object> valueMapper = this.getEnvironmentValueMapper();
      return !this.environmentAnnotationMetadata.isTrue(annotation, member, valueMapper);
   }

   @NonNull
   @Override
   public Optional<Class<? extends Annotation>> getAnnotationType(@NonNull String name) {
      ArgumentUtils.requireNonNull("name", name);
      return this.environmentAnnotationMetadata.getAnnotationType(name);
   }

   @NonNull
   @Override
   public Optional<Class<? extends Annotation>> getAnnotationType(@NonNull String name, @NonNull ClassLoader classLoader) {
      ArgumentUtils.requireNonNull("name", name);
      return this.environmentAnnotationMetadata.getAnnotationType(name, classLoader);
   }

   @NonNull
   @Override
   public <T extends Annotation> List<AnnotationValue<T>> getAnnotationValuesByType(@NonNull Class<T> annotationType) {
      ArgumentUtils.requireNonNull("annotationType", (T)annotationType);
      Environment environment = this.getEnvironment();
      List<AnnotationValue<T>> values = this.environmentAnnotationMetadata.getAnnotationValuesByType(annotationType);
      return environment != null
         ? (List)values.stream().map(entries -> new EnvironmentAnnotationValue(environment, entries)).collect(Collectors.toList())
         : values;
   }

   @NonNull
   @Override
   public <T extends Annotation> List<AnnotationValue<T>> getDeclaredAnnotationValuesByType(@NonNull Class<T> annotationType) {
      ArgumentUtils.requireNonNull("annotationType", (T)annotationType);
      Environment environment = this.getEnvironment();
      List<AnnotationValue<T>> values = this.environmentAnnotationMetadata.getDeclaredAnnotationValuesByType(annotationType);
      return environment != null
         ? (List)values.stream().map(entries -> new EnvironmentAnnotationValue(environment, entries)).collect(Collectors.toList())
         : values;
   }

   @NonNull
   @Override
   public <T extends Annotation> T[] synthesizeAnnotationsByType(@NonNull Class<T> annotationClass) {
      ArgumentUtils.requireNonNull("annotationClass", (T)annotationClass);
      Environment environment = this.getEnvironment();
      if (environment != null) {
         List<AnnotationValue<T>> values = this.environmentAnnotationMetadata.getAnnotationValuesByType(annotationClass);
         return (T[])values.stream()
            .map(entries -> AnnotationMetadataSupport.buildAnnotation(annotationClass, new EnvironmentAnnotationValue<>(environment, entries)))
            .toArray(value -> (Annotation[])Array.newInstance(annotationClass, value));
      } else {
         return (T[])this.environmentAnnotationMetadata.synthesizeAnnotationsByType(annotationClass);
      }
   }

   @NonNull
   @Override
   public <T extends Annotation> T[] synthesizeDeclaredAnnotationsByType(@NonNull Class<T> annotationClass) {
      ArgumentUtils.requireNonNull("annotationClass", (T)annotationClass);
      Environment environment = this.getEnvironment();
      if (environment != null) {
         List<AnnotationValue<T>> values = this.environmentAnnotationMetadata.getDeclaredAnnotationValuesByType(annotationClass);
         return (T[])values.stream()
            .map(entries -> AnnotationMetadataSupport.buildAnnotation(annotationClass, new EnvironmentAnnotationValue<>(environment, entries)))
            .toArray(value -> (Annotation[])Array.newInstance(annotationClass, value));
      } else {
         return (T[])this.environmentAnnotationMetadata.synthesizeDeclaredAnnotationsByType(annotationClass);
      }
   }

   @Override
   public boolean hasDeclaredAnnotation(@Nullable String annotation) {
      return this.environmentAnnotationMetadata.hasDeclaredAnnotation(annotation);
   }

   @Override
   public boolean hasAnnotation(@Nullable String annotation) {
      return this.environmentAnnotationMetadata.hasAnnotation(annotation);
   }

   @Override
   public boolean hasStereotype(@Nullable String annotation) {
      return this.environmentAnnotationMetadata.hasStereotype(annotation);
   }

   @Override
   public boolean hasDeclaredStereotype(@Nullable String annotation) {
      return this.environmentAnnotationMetadata.hasDeclaredStereotype(annotation);
   }

   @NonNull
   @Override
   public List<String> getAnnotationNamesByStereotype(String stereotype) {
      return this.environmentAnnotationMetadata.getAnnotationNamesByStereotype(stereotype);
   }

   @NonNull
   @Override
   public Set<String> getAnnotationNames() {
      return this.environmentAnnotationMetadata.getAnnotationNames();
   }

   @NonNull
   @Override
   public Set<String> getDeclaredAnnotationNames() {
      return this.environmentAnnotationMetadata.getDeclaredAnnotationNames();
   }

   @NonNull
   @Override
   public List<String> getDeclaredAnnotationNamesByStereotype(String stereotype) {
      return this.environmentAnnotationMetadata.getDeclaredAnnotationNamesByStereotype(stereotype);
   }

   @NonNull
   @Override
   public <T extends Annotation> Optional<AnnotationValue<T>> findAnnotation(@NonNull String annotation) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      Environment env = this.getEnvironment();
      Optional<AnnotationValue<T>> values = this.environmentAnnotationMetadata.findAnnotation(annotation);
      return env != null ? values.map(av -> new EnvironmentAnnotationValue(env, av)) : values;
   }

   @NonNull
   @Override
   public <T extends Annotation> Optional<AnnotationValue<T>> findDeclaredAnnotation(@NonNull String annotation) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      Environment env = this.getEnvironment();
      Optional<AnnotationValue<T>> values = this.environmentAnnotationMetadata.findDeclaredAnnotation(annotation);
      return env != null ? values.map(av -> new EnvironmentAnnotationValue(env, av)) : values;
   }

   @NonNull
   @Override
   public <T> OptionalValues<T> getValues(@NonNull String annotation, @NonNull Class<T> valueType) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      ArgumentUtils.requireNonNull("valueType", (T)valueType);
      if (this.environmentAnnotationMetadata instanceof DefaultAnnotationMetadata) {
         Environment environment = this.getEnvironment();
         return this.resolveOptionalValuesForEnvironment(annotation, valueType, Collections.singleton(this.environmentAnnotationMetadata), environment);
      } else if (this.environmentAnnotationMetadata instanceof AnnotationMetadataHierarchy) {
         AnnotationMetadataHierarchy hierarchy = (AnnotationMetadataHierarchy)this.environmentAnnotationMetadata;
         Environment environment = this.getEnvironment();
         return this.resolveOptionalValuesForEnvironment(annotation, valueType, hierarchy, environment);
      } else {
         return OptionalValues.empty();
      }
   }

   @NonNull
   @Override
   public <T> Optional<T> getDefaultValue(@NonNull String annotation, @NonNull String member, @NonNull Class<T> requiredType) {
      return this.environmentAnnotationMetadata.getDefaultValue(annotation, member, requiredType);
   }

   @NonNull
   @Override
   public <T> Optional<T> getDefaultValue(@NonNull String annotation, @NonNull String member, @NonNull Argument<T> requiredType) {
      return this.environmentAnnotationMetadata.getDefaultValue(annotation, member, requiredType);
   }

   @Nullable
   protected abstract Environment getEnvironment();

   @Nullable
   private Function<Object, Object> getEnvironmentValueMapper() {
      Environment env = this.getEnvironment();
      return env != null ? o -> {
         if (o instanceof String) {
            String v = (String)o;
            if (v.contains("${")) {
               return env.getPlaceholderResolver().resolveRequiredPlaceholders(v);
            }
         }

         return o;
      } : null;
   }

   private <T> OptionalValues<T> resolveOptionalValuesForEnvironment(
      String annotation, Class<T> valueType, Iterable<AnnotationMetadata> metadata, Environment environment
   ) {
      Map<CharSequence, Object> finalValues = new LinkedHashMap();

      for(AnnotationMetadata annotationMetadata : metadata) {
         if (annotationMetadata instanceof DefaultAnnotationMetadata) {
            Map<String, Map<CharSequence, Object>> allAnnotations = ((DefaultAnnotationMetadata)annotationMetadata).allAnnotations;
            Map<String, Map<CharSequence, Object>> allStereotypes = ((DefaultAnnotationMetadata)annotationMetadata).allStereotypes;
            if (allAnnotations != null && StringUtils.isNotEmpty(annotation)) {
               this.processMap(annotation, finalValues, allStereotypes);
               this.processMap(annotation, finalValues, allAnnotations);
            }
         }
      }

      return (OptionalValues<T>)(environment != null
         ? new EnvironmentOptionalValuesMap<>(valueType, finalValues, environment)
         : OptionalValues.of(valueType, finalValues));
   }

   private void processMap(String annotation, Map<CharSequence, Object> finalValues, Map<String, Map<CharSequence, Object>> allStereotypes) {
      if (allStereotypes != null) {
         Map<CharSequence, Object> values = (Map)allStereotypes.get(annotation);
         if (values != null) {
            for(Entry<CharSequence, Object> entry : values.entrySet()) {
               finalValues.putIfAbsent(entry.getKey(), entry.getValue());
            }
         }
      }

   }
}
