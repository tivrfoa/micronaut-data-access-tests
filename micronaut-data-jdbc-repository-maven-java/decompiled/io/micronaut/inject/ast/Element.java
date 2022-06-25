package io.micronaut.inject.ast;

import io.micronaut.core.annotation.AnnotatedElement;
import io.micronaut.core.annotation.AnnotationMetadataDelegate;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.AnnotationValueBuilder;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.naming.Described;
import io.micronaut.core.util.ArgumentUtils;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Element extends AnnotationMetadataDelegate, AnnotatedElement, Described {
   Element[] EMPTY_ELEMENT_ARRAY = new Element[0];

   @NonNull
   @Override
   String getName();

   default boolean isPackagePrivate() {
      return false;
   }

   boolean isProtected();

   boolean isPublic();

   @NonNull
   Object getNativeType();

   default Set<ElementModifier> getModifiers() {
      return Collections.emptySet();
   }

   @NonNull
   default <T extends Annotation> Element annotate(@NonNull String annotationType, @NonNull Consumer<AnnotationValueBuilder<T>> consumer) {
      throw new UnsupportedOperationException("Element of type [" + this.getClass() + "] does not support adding annotations at compilation time");
   }

   default Element removeAnnotation(@NonNull String annotationType) {
      throw new UnsupportedOperationException("Element of type [" + this.getClass() + "] does not support removing annotations at compilation time");
   }

   default <T extends Annotation> Element removeAnnotation(@NonNull Class<T> annotationType) {
      return this.removeAnnotation(((Class)Objects.requireNonNull(annotationType)).getName());
   }

   default <T extends Annotation> Element removeAnnotationIf(@NonNull Predicate<AnnotationValue<T>> predicate) {
      throw new UnsupportedOperationException("Element of type [" + this.getClass() + "] does not support removing annotations at compilation time");
   }

   default Element removeStereotype(@NonNull String annotationType) {
      throw new UnsupportedOperationException("Element of type [" + this.getClass() + "] does not support removing annotations at compilation time");
   }

   default <T extends Annotation> Element removeStereotype(@NonNull Class<T> annotationType) {
      return this.removeStereotype(((Class)Objects.requireNonNull(annotationType)).getName());
   }

   @NonNull
   default Element annotate(@NonNull String annotationType) {
      return this.annotate(annotationType, annotationValueBuilder -> {
      });
   }

   @NonNull
   default <T extends Annotation> Element annotate(@NonNull Class<T> annotationType, @NonNull Consumer<AnnotationValueBuilder<T>> consumer) {
      ArgumentUtils.requireNonNull("annotationType", (T)annotationType);
      ArgumentUtils.requireNonNull("consumer", (T)consumer);
      return this.annotate(annotationType.getName(), consumer);
   }

   @NonNull
   default <T extends Annotation> Element annotate(@NonNull Class<T> annotationType) {
      ArgumentUtils.requireNonNull("annotationType", (T)annotationType);
      return this.annotate(annotationType.getName(), annotationValueBuilder -> {
      });
   }

   @NonNull
   default <T extends Annotation> Element annotate(@NonNull AnnotationValue<T> annotationValue) {
      throw new UnsupportedOperationException("Element of type [" + this.getClass() + "] does not support adding annotations at compilation time");
   }

   @NonNull
   default String getSimpleName() {
      return this.getName();
   }

   default boolean isAbstract() {
      return false;
   }

   default boolean isStatic() {
      return false;
   }

   default Optional<String> getDocumentation() {
      return Optional.empty();
   }

   default boolean isPrivate() {
      return !this.isPublic();
   }

   default boolean isFinal() {
      return false;
   }

   @NonNull
   @Override
   default String getDescription() {
      return this.getDescription(true);
   }

   @NonNull
   @Override
   default String getDescription(boolean simple) {
      return simple ? this.getSimpleName() : this.getName();
   }
}
