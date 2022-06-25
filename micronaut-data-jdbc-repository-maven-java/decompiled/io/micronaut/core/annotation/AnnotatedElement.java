package io.micronaut.core.annotation;

import io.micronaut.core.naming.Named;

public interface AnnotatedElement extends AnnotationMetadataProvider, Named {
   default boolean isDeclaredNullable() {
      return this.getAnnotationMetadata().hasDeclaredStereotype("javax.annotation.Nullable");
   }

   default boolean isNullable() {
      return this.getAnnotationMetadata().hasStereotype("javax.annotation.Nullable");
   }

   default boolean isNonNull() {
      return this.getAnnotationMetadata().hasStereotype("javax.annotation.Nonnull");
   }

   default boolean isDeclaredNonNull() {
      return this.getAnnotationMetadata().hasDeclaredStereotype("javax.annotation.Nonnull");
   }
}
