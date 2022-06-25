package io.micronaut.core.annotation;

import io.micronaut.core.util.ArgumentUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Optional;

public interface AnnotationSource {
   AnnotationSource EMPTY = new AnnotationSource() {
   };

   @Nullable
   default <T extends Annotation> T synthesize(@NonNull Class<T> annotationClass) {
      ArgumentUtils.requireNonNull("annotationClass", (T)annotationClass);
      return null;
   }

   @Nullable
   default <T extends Annotation> T synthesize(@NonNull Class<T> annotationClass, @NonNull String sourceAnnotation) {
      ArgumentUtils.requireNonNull("annotationClass", (T)annotationClass);
      ArgumentUtils.requireNonNull("sourceAnnotation", (T)sourceAnnotation);
      return null;
   }

   @Nullable
   default <T extends Annotation> T synthesizeDeclared(@NonNull Class<T> annotationClass, @NonNull String sourceAnnotation) {
      ArgumentUtils.requireNonNull("annotationClass", (T)annotationClass);
      ArgumentUtils.requireNonNull("sourceAnnotation", (T)sourceAnnotation);
      return null;
   }

   @Nullable
   default <T extends Annotation> T synthesizeDeclared(@NonNull Class<T> annotationClass) {
      ArgumentUtils.requireNonNull("annotationClass", (T)annotationClass);
      return null;
   }

   @NonNull
   default Annotation[] synthesizeAll() {
      return AnnotationUtil.ZERO_ANNOTATIONS;
   }

   @NonNull
   default Annotation[] synthesizeDeclared() {
      return AnnotationUtil.ZERO_ANNOTATIONS;
   }

   @NonNull
   default <T extends Annotation> T[] synthesizeAnnotationsByType(@NonNull Class<T> annotationClass) {
      ArgumentUtils.requireNonNull("annotationClass", (T)annotationClass);
      return (T[])((Annotation[])Array.newInstance(annotationClass, 0));
   }

   @NonNull
   default <T extends Annotation> T[] synthesizeDeclaredAnnotationsByType(@NonNull Class<T> annotationClass) {
      ArgumentUtils.requireNonNull("annotationClass", (T)annotationClass);
      return (T[])((Annotation[])Array.newInstance(annotationClass, 0));
   }

   @NonNull
   default <T extends Annotation> Optional<AnnotationValue<T>> findAnnotation(@NonNull String annotation) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      return Optional.empty();
   }

   @NonNull
   default <T extends Annotation> Optional<AnnotationValue<T>> findAnnotation(@NonNull Class<T> annotationClass) {
      ArgumentUtils.requireNonNull("annotationClass", (T)annotationClass);
      return Optional.empty();
   }

   @NonNull
   default <T extends Annotation> Optional<AnnotationValue<T>> findDeclaredAnnotation(@NonNull String annotation) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      return Optional.empty();
   }

   @NonNull
   default <T extends Annotation> Optional<AnnotationValue<T>> findDeclaredAnnotation(@NonNull Class<T> annotationClass) {
      ArgumentUtils.requireNonNull("annotationClass", (T)annotationClass);
      return Optional.empty();
   }

   @Nullable
   default <T extends Annotation> AnnotationValue<T> getAnnotation(@NonNull String annotation) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      return (AnnotationValue<T>)this.findAnnotation(annotation).orElse(null);
   }

   @Nullable
   default <T extends Annotation> AnnotationValue<T> getAnnotation(@NonNull Class<T> annotationClass) {
      ArgumentUtils.requireNonNull("annotationClass", (T)annotationClass);
      return (AnnotationValue<T>)this.findAnnotation(annotationClass).orElse(null);
   }

   @Nullable
   default <T extends Annotation> AnnotationValue<T> getDeclaredAnnotation(@NonNull String annotation) {
      ArgumentUtils.requireNonNull("annotation", (T)annotation);
      return (AnnotationValue<T>)this.findDeclaredAnnotation(annotation).orElse(null);
   }

   @Nullable
   default <T extends Annotation> AnnotationValue<T> getDeclaredAnnotation(@NonNull Class<T> annotationClass) {
      ArgumentUtils.requireNonNull("annotationClass", (T)annotationClass);
      return (AnnotationValue<T>)this.findDeclaredAnnotation(annotationClass).orElse(null);
   }

   default boolean isAnnotationPresent(@NonNull Class<? extends Annotation> annotationClass) {
      ArgumentUtils.requireNonNull("annotationClass", annotationClass);
      return false;
   }

   default boolean isDeclaredAnnotationPresent(@NonNull Class<? extends Annotation> annotationClass) {
      ArgumentUtils.requireNonNull("annotationClass", annotationClass);
      return false;
   }

   default boolean isAnnotationPresent(@NonNull String annotationName) {
      ArgumentUtils.requireNonNull("annotationClass", annotationName);
      return false;
   }

   default boolean isDeclaredAnnotationPresent(@NonNull String annotationName) {
      ArgumentUtils.requireNonNull("annotationClass", annotationName);
      return false;
   }
}
