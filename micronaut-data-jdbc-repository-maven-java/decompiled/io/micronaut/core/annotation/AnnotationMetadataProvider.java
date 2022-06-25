package io.micronaut.core.annotation;

import java.lang.annotation.Annotation;
import java.util.Optional;

public interface AnnotationMetadataProvider extends AnnotationSource {
   @NonNull
   default AnnotationMetadata getAnnotationMetadata() {
      return AnnotationMetadata.EMPTY_METADATA;
   }

   @Override
   default <T extends Annotation> T synthesize(Class<T> annotationClass) {
      return this.getAnnotationMetadata().synthesize(annotationClass);
   }

   @Override
   default Annotation[] synthesizeAll() {
      return this.getAnnotationMetadata().synthesizeAll();
   }

   @Override
   default Annotation[] synthesizeDeclared() {
      return this.getAnnotationMetadata().synthesizeDeclared();
   }

   @Override
   default boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
      return this.getAnnotationMetadata().isAnnotationPresent(annotationClass);
   }

   @Override
   default boolean isDeclaredAnnotationPresent(Class<? extends Annotation> annotationClass) {
      return this.getAnnotationMetadata().isDeclaredAnnotationPresent(annotationClass);
   }

   @Override
   default <T extends Annotation> T synthesizeDeclared(Class<T> annotationClass) {
      return this.getAnnotationMetadata().synthesizeDeclared(annotationClass);
   }

   @Override
   default <T extends Annotation> T[] synthesizeAnnotationsByType(Class<T> annotationClass) {
      return (T[])this.getAnnotationMetadata().synthesizeAnnotationsByType(annotationClass);
   }

   @Override
   default <T extends Annotation> T[] synthesizeDeclaredAnnotationsByType(Class<T> annotationClass) {
      return (T[])this.getAnnotationMetadata().synthesizeDeclaredAnnotationsByType(annotationClass);
   }

   @Override
   default <T extends Annotation> Optional<AnnotationValue<T>> findAnnotation(String annotation) {
      return this.getAnnotationMetadata().findAnnotation(annotation);
   }

   @Override
   default <T extends Annotation> Optional<AnnotationValue<T>> findAnnotation(Class<T> annotationClass) {
      return this.getAnnotationMetadata().findAnnotation(annotationClass);
   }

   @Override
   default <T extends Annotation> Optional<AnnotationValue<T>> findDeclaredAnnotation(String annotation) {
      return this.getAnnotationMetadata().findDeclaredAnnotation(annotation);
   }

   @Override
   default <T extends Annotation> Optional<AnnotationValue<T>> findDeclaredAnnotation(Class<T> annotationClass) {
      return this.getAnnotationMetadata().findDeclaredAnnotation(annotationClass);
   }
}
